package com.forum.util.oss.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.UUID;
import com.forum.common.exception.ForumException;
import com.forum.common.utils.SpringContextUtils;
import com.forum.util.oss.factory.OssFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;

/**
 * 七牛云对象存储（S3 协议空间）
 * bucket forum-photo 为 S3 协议空间（region cn-south-1 华南），经典上传协议不可用，
 * 上传/删除走 AWS S3 协议（SigV4）；对外展示使用外链域名（公开空间可匿名读取）。
 */
@Slf4j
public class QiNiuYunUploadFile implements OssFactory {

    private static volatile S3Client s3Client;

    private static volatile String bucketName;

    private static volatile String domain;

    private static S3Client client() {
        if (s3Client == null) {
            synchronized (QiNiuYunUploadFile.class) {
                if (s3Client == null) {
                    Environment env = SpringContextUtils.getBean(Environment.class);
                    String accessKey = env.getProperty("qiNiuYun.accessKey");
                    String secretKey = env.getProperty("qiNiuYun.secretKey");
                    // S3 协议端点与区域（默认七牛华南 cn-south-1）
                    String endpoint = env.getProperty("qiNiuYun.s3Endpoint", "s3.cn-south-1.qiniucs.com");
                    String region = env.getProperty("qiNiuYun.region", "cn-south-1");
                    bucketName = env.getProperty("qiNiuYun.bucketName");
                    domain = env.getProperty("qiNiuYun.domain");
                    s3Client = S3Client.builder()
                            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                            .endpointOverride(URI.create("https://" + endpoint))
                            .region(Region.of(region))
                            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                            .build();
                }
            }
        }
        return s3Client;
    }

    @Override
    public String save(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

            //1 在文件名称里面添加随机唯一的值
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            // yuy76t5rew01.jpg
            String fileName = uuid + suffix;

            //2 把文件按照日期进行分类
            //   2019/11/12
            String datePath = new DateTime().toString("yyyy/MM/dd");
            //  2019/11/12/ewtqr313401.jpg
            String key = datePath + "/" + fileName;

            S3Client client = client(); // 必须先初始化 client（懒加载单例会填充 bucketName/domain），再构建请求
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return "http://" + domain + "/" + key;
        } catch (ForumException e) {
            throw e;
        } catch (Exception e) {
            log.error("七牛云上传失败, file={}", file.getOriginalFilename(), e);
            throw new ForumException("七牛云上传失败");
        }
    }

    @Override
    public Boolean delete(String path) {
        try {
            S3Client client = client(); // 同 save：先初始化，再读 bucketName/domain
            String key = path.replace("http://" + domain + "/", "");
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            client.deleteObject(request);
            return true;
        } catch (Exception e) {
            log.error("七牛云删除失败, path={}", path, e);
            throw new ForumException("七牛云删除失败");
        }
    }
}
