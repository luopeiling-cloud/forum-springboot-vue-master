package com.forum.common.constant;

/**
 * 文件上传路径配置
 * @author xiaozhao
 */
public final class UploadFileConstant {

    private UploadFileConstant() {
    }

    /**
     * 本地文件存储根目录（本地兜底策略使用）
     * 容器内通过 JVM 参数 -Dupload.address=/app/uploads 覆盖
     */
    public static final String ADDRESS = System.getProperty("upload.address",
            "C:/Users/罗培凌/Desktop/forum-springboot-vue-master/uploads/");

    public static final String OSS = "/oss/";
}
