package eafit.gruopChat.file.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eafit.gruopChat.file.dto.FileUploadResponseDTO;
import eafit.gruopChat.file.exception.FileNotFoundException;
import eafit.gruopChat.file.model.FileRecord;
import eafit.gruopChat.file.service.FileService;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponseDTO> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Long userId) {
        FileUploadResponseDTO response = fileService.upload(file, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) {
        FileRecord record = fileService.getFile(fileId);

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Paths.get(record.getStoragePath()));
        } catch (IOException e) {
            throw new FileNotFoundException(fileId);
        }

        String disposition = isInlineType(record.getMimeType())
                ? "inline"
                : "attachment; filename=\"" + record.getOriginalName() + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .header(HttpHeaders.CONTENT_TYPE, record.getMimeType())
                .body(bytes);
    }

    private boolean isInlineType(String mimeType) {
        if (mimeType == null) return false;
        return mimeType.startsWith("image/") || mimeType.equals("application/pdf");
    }
}