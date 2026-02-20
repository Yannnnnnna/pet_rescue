package com.wei.pet.pet_rescue.common;

import cn.hutool.core.date.DateTime;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author wyr on 2026/1/17
 */
@Component
public class AliyunOssUtil {

    // 读取配置文件中的参数
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.urlPrefix}")
    private String urlPrefix;

    /**
     * 上传文件
     * @param file 前端传来的文件对象
     * @return 文件的完整访问URL
     */
    public String uploadFile(MultipartFile file) {
        try {
            // 1. 创建OSS实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 2. 获取文件流
            InputStream inputStream = file.getInputStream();

            // 3. 构建文件名 (保持不变)
            String originalFilename = file.getOriginalFilename();
            String datePath = new DateTime().toString("yyyy/MM/dd");
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = datePath + "/" + uuid + suffix;

            // 4. 【新增步骤】创建元信息对象，设置 Content-Type
            ObjectMetadata metadata = new ObjectMetadata();
            // 设置内容长度，防止某些情况下的上传失败
            metadata.setContentLength(inputStream.available());
            // 自动判断并设置文件类型 (核心代码)
            metadata.setContentType(getContentType(suffix));

            // 5. 调用OSS方法上传 (带上 metadata 参数)
            ossClient.putObject(bucketName, fileName, inputStream, metadata);

            // 6. 关闭OSSClient
            ossClient.shutdown();

            // 7. 返回文件的完整URL
            return urlPrefix + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 【新增私有方法】根据后缀名获取 Content-Type
     */
    private String getContentType(String suffix) {
        if (suffix.equalsIgnoreCase(".bmp")) {
            return "image/bmp";
        }
        if (suffix.equalsIgnoreCase(".gif")) {
            return "image/gif";
        }
        if (suffix.equalsIgnoreCase(".jpeg") ||
                suffix.equalsIgnoreCase(".jpg") ||
                suffix.equalsIgnoreCase(".png")) {
            return "image/jpeg"; // jpg和png都兼容这个，或者分开写
        }
        if (suffix.equalsIgnoreCase(".png")) {
            return "image/png";
        }
        if (suffix.equalsIgnoreCase(".html")) {
            return "text/html";
        }
        return "application/octet-stream"; // 默认值
    }
}
