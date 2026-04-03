package eafit.gruopChat.file.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import eafit.gruopChat.file.dto.FileUploadResponseDTO;
import eafit.gruopChat.file.exception.FileNotFoundException;
import eafit.gruopChat.file.exception.FileTooLargeException;
import eafit.gruopChat.file.exception.InvalidFileException;
import eafit.gruopChat.file.model.FileRecord;
import eafit.gruopChat.file.repository.FileRepository;
import eafit.gruopChat.file.service.FileService;
import eafit.gruopChat.user.exception.UserNotFoundException;
import eafit.gruopChat.user.model.User;
import eafit.gruopChat.user.repository.UserRepository;

@Service
@Transactional
public class FileServiceImpl implements FileService {

    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    // En application.properties: file.storage.path=/mnt/nfs/files
    @Value("${file.storage.path:/mnt/nfs/files}")
    private String storagePath;

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public FileServiceImpl(FileRepository fileRepository, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    @Override
    public FileUploadResponseDTO upload(MultipartFile file, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("el archivo está vacío");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new FileTooLargeException(MAX_SIZE_BYTES);
        }

        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new UserNotFoundException(uploaderId));

        String mimeType = file.getContentType();
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "application/octet-stream";
        }

        String originalName = sanitizeName(file.getOriginalFilename());

        // Primero guardamos en BD para obtener el fileId
        FileRecord record = new FileRecord();
        record.setUploadedBy(uploader);
        record.setOriginalName(originalName);
        record.setMimeType(mimeType);
        record.setSizeBytes(file.getSize());
        record.setStoragePath("pending"); // temporal
        FileRecord saved = fileRepository.save(record);

        // Construimos la ruta definitiva usando el fileId
        String fileName = saved.getFileId() + "_" + originalName;
        Path targetDir = Paths.get(storagePath);
        Path targetPath = targetDir.resolve(fileName);

        try {
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new InvalidFileException("no se pudo guardar el archivo: " + e.getMessage());
        }

        // Actualizamos la ruta real en BD
        saved.setStoragePath(targetPath.toString());
        fileRepository.save(saved);

        return new FileUploadResponseDTO(
                saved.getFileId(),
                saved.getOriginalName(),
                saved.getMimeType(),
                saved.getSizeBytes(),
                "/api/files/" + saved.getFileId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FileRecord getFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));
    }

    private String sanitizeName(String name) {
        if (name == null || name.isBlank()) return "archivo";
        return name.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }
}