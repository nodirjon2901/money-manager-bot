package uz.result.moneymanagerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.result.moneymanagerbot.exceptions.NotFoundException;
import uz.result.moneymanagerbot.model.FileEntity;
import uz.result.moneymanagerbot.repository.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class FileService {


    @Value("${photos.files.file.path}")
    private String photoUploadPath;

    @Value("${server.base-url}")
    private String baseUrl;

    private final FileRepository fileRepository;

    public FileEntity savePhotoFromTelegram(String filePath) {
        java.io.File file = new java.io.File(filePath);
        try {
            String contentType = Files.probeContentType(file.toPath());
            String originalName = file.getName();

            if (contentType == null || (!contentType.startsWith("application/") && !contentType.startsWith("image/"))) {
                return null;
            }

            FileEntity fileEntity = new FileEntity(
                    originalName,
                    filePath,
                    baseUrl,
                    contentType);

            return fileRepository.save(fileEntity);
        } catch (IOException e) {
            throw new RuntimeException("Error processing file", e);
        }

    }

    public void deleteFileById(Long id) throws IOException {
        FileEntity fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File is not found with id: " + id));
        deleteFromFile(fileEntity.getSystemPath());
        fileRepository.delete(fileEntity);
    }

    public void deleteFromFile(String filePath) throws IOException {
        try {
            if (filePath != null)
                Files.delete(Paths.get(filePath));
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

}
