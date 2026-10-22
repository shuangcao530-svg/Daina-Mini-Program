package com.xiaosuda.controller;

import com.xiaosuda.common.BusinessException;
import com.xiaosuda.common.ErrorCode;
import com.xiaosuda.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "文件", description = "图片上传与访问")
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${upload.url-prefix:http://localhost:8080}")
    private String urlPrefix;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp");

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                               HttpServletRequest request) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_INVALID);
        }

        try {
            // Organize by date: uploads/yyyyMM/
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            Path dir = Paths.get(uploadPath, dateDir);
            Files.createDirectories(dir);

            // Generate unique filename
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;

            // Save file
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());

            // 返回完整访问 URL
            String url = urlPrefix + "/api/files/" + dateDir + "/" + filename;
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("filename", filename);
            return Result.ok(result);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e.getMessage());
        }
    }

    @Operation(summary = "访问图片")
    @GetMapping("/{dateDir}/{filename}")
    public void serveFile(@PathVariable String dateDir,
                          @PathVariable String filename,
                          HttpServletResponse response) {
        Path filePath = Paths.get(uploadPath, dateDir, filename).normalize();

        // 安全检查：确保路径在 uploadPath 内
        if (!filePath.startsWith(Paths.get(uploadPath).normalize())) {
            response.setStatus(403);
            return;
        }

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            response.setStatus(404);
            return;
        }

        try {
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "image/jpeg";
            }
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "max-age=86400");
            Files.copy(filePath, response.getOutputStream());
        } catch (IOException e) {
            response.setStatus(500);
        }
    }

    @Operation(summary = "批量上传图片")
    @PostMapping("/upload/batch")
    public Result<List<Map<String, String>>> uploadBatch(@RequestParam("files") List<MultipartFile> files) {
        List<Map<String, String>> results = new ArrayList<>();
        for (MultipartFile file : files) {
            Result<Map<String, String>> result = upload(file, null);
            if (result.getData() != null) {
                results.add(result.getData());
            }
        }
        return Result.ok(results);
    }
}
