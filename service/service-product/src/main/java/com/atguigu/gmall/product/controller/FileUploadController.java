package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * @author Aiden
 * @create 2022-09-13 16:17
 */
@RestController
@RequestMapping("/admin/product")
public class FileUploadController {

    /*
minio:
endpointUrl: http://192.168.230.201:9000
accessKey: admin
secreKey: admin123456
bucketName: gmall

     */
    @Value("${minio.endpointUrl}")
    private String endpointUrl;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secreKey}")
    private String secreKey;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 上传
     * POST/admin/product/fileUpload
     *
     * @param file
     * @return
     */
    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file) {

        String url = "";
        try {
            // Create a minioClient with the MinIO server playground, its access key and secret key.
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(endpointUrl)
                            .credentials(accessKey, secreKey)
                            .build();

            // Make 'asiatrip' bucket if not exist.
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                // 指定的桶不存在，创建桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                System.out.println("Bucket 'asiatrip' already exists.");
            }

            String fileName = UUID.randomUUID().toString().replace("-", "") + file.getOriginalFilename();


            // 将指定的资源上传到指定服务器的桶中
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                                    file.getInputStream(), file.getSize(), -1)
                            .contentType("video/mp4")
                            .build());

            //http://192.168.230.201:9000/库名/图片名
            url = endpointUrl + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            System.out.println("Error occurred: " + e);
            e.printStackTrace();
        }


        //返回数据时文件上传后的路径
        return Result.ok(url);
    }

}
