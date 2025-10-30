package com.academic.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


@Component
public class FileUtil {

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 上传文件
     * @param file 待上传的文件
     * @param subDir 子目录
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file, String subDir) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IOException("上传文件不能为空");
        }

        // 获取文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件扩展名
        String extension = getFileExtension(originalFilename);
        // 生成新的文件名（避免文件名冲突）
        String newFilename = generateFilename(extension);

        // 创建子目录
        String targetDir = uploadPath + File.separator + subDir;
        File dir = new File(targetDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 构建文件完整路径
        String filePath = targetDir + File.separator + newFilename;
        File dest = new File(filePath);

        // 保存文件
        file.transferTo(dest);

        // 返回文件访问路径
        return "/uploads/" + subDir + "/" + newFilename;
    }

    /**
     * 上传文件（默认目录）
     * @param file 待上传的文件
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, "");
    }

    /**
     * 保存字节数组为文件
     * @param content 文件内容字节数组
     * @param filename 文件名
     * @param subDir 子目录
     * @return 文件访问路径
     */
    public String saveFile(byte[] content, String filename, String subDir) throws IOException {
        // 获取文件扩展名
        String extension = getFileExtension(filename);
        // 生成新的文件名（避免文件名冲突）
        String newFilename = generateFilename(extension);

        // 创建子目录
        String targetDir = uploadPath + File.separator + subDir;
        File dir = new File(targetDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 构建文件完整路径
        String filePath = targetDir + File.separator + newFilename;
        Path path = Paths.get(filePath);

        // 写入文件内容
        Files.write(path, content);

        // 返回文件访问路径
        return "/uploads/" + subDir + "/" + newFilename;
    }

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        // 从URL中提取文件路径
        String filePath = getFilePath(fileUrl);
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 生成新的文件名
     * @param extension 文件扩展名
     * @return 新的文件名
     */
    private String generateFilename(String extension) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return timestamp + "_" + uuid + extension;
    }

    /**
     * 从URL获取文件路径
     * @param fileUrl 文件URL
     * @return 文件路径
     */
    private String getFilePath(String fileUrl) {
        // 移除URL前缀（假设URL格式为 /uploads/...）
        if (fileUrl.startsWith("/uploads/")) {
            fileUrl = fileUrl.substring(8);
        }
        return uploadPath + File.separator + fileUrl;
    }

    /**
     * 读取文件内容
     * @param fileUrl 文件URL
     * @return 文件内容字节数组
     */
    public byte[] readFile(String fileUrl) throws IOException {
        String filePath = getFilePath(fileUrl);
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    /**
     * 获取文件大小
     * @param fileUrl 文件URL
     * @return 文件大小（字节）
     */
    public long getFileSize(String fileUrl) {
        String filePath = getFilePath(fileUrl);
        File file = new File(filePath);
        if (file.exists()) {
            return file.length();
        }
        return 0;
    }

    /**
     * 获取文件类型
     * @param fileUrl 文件URL
     * @return 文件类型
     */
    public String getFileType(String fileUrl) throws IOException {
        String filePath = getFilePath(fileUrl);
        Path path = Paths.get(filePath);
        return Files.probeContentType(path);
    }
    
    /**
     * 获取文件资源对象
     * @param fileUrl 文件URL
     * @return 文件资源对象
     */
    public Resource getFileResource(String fileUrl) {
        String filePath = getFilePath(fileUrl);
        return new FileSystemResource(filePath);
    }
}
