package com.wei.pet.pet_rescue.controller;

import com.wei.pet.pet_rescue.common.AliyunOssUtil;
import com.wei.pet.pet_rescue.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author wyr on 2026/1/17
 */
@Tag(name = "通用接口-文件")
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private AliyunOssUtil aliyunOssUtil;

    @Value("${aliyun.maxFileSize}")
    private int maxFileSize;

    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 1. 校验文件是否为空
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 2. 校验文件大小 (可选，通常在配置文件中做限制)
        if (file.getSize() > maxFileSize * 1024 * 1024) { // 10MB
            return Result.error("文件大小不能超过" + maxFileSize + "MB");
        }

        // 3. 调用工具类上传
        String url = aliyunOssUtil.uploadFile(file);

        if (url != null) {
            // 返回上传成功的图片URL
            return Result.success(url);
        } else {
            return Result.error("文件上传失败，请联系管理员");
        }
    }
}
