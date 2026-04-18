package eafit.gruopChat.file.model;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
@Entity
@Table(name = "files")
public class FileRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;
    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedById;
    @Column(name = "original_name", nullable = false)
    private String originalName;
    @Column(name = "mime_type", nullable = false)
    private String mimeType;
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;
    @Column(name = "storage_path", nullable = false)
    private String storagePath;
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    @PrePersist
    protected void onCreate() { this.uploadedAt = LocalDateTime.now(); }
    public Long getFileId()                          { return fileId; }
    public Long getUploadedById()                    { return uploadedById; }
    public void setUploadedById(Long uploadedById)   { this.uploadedById = uploadedById; }
    public String getOriginalName()                  { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getMimeType()                      { return mimeType; }
    public void setMimeType(String mimeType)         { this.mimeType = mimeType; }
    public Long getSizeBytes()                       { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes)         { this.sizeBytes = sizeBytes; }
    public String getStoragePath()                   { return storagePath; }
    public void setStoragePath(String storagePath)   { this.storagePath = storagePath; }
    public LocalDateTime getUploadedAt()             { return uploadedAt; }
}