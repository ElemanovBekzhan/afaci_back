package org.example.affaci.Service;


import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;

    @Value("${minio.url}")
    private String endpoint;

    @Value("${minio.bucket}")
    private String bucket;




    public String getObjectUrl(String objectName) {
        try {
            String pictureUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(1, TimeUnit.HOURS)
                            .extraQueryParams(Map.of("response-content-type", "image/jpeg"))
                            .build());

            // internalUrl == http://127.0.0.1:9000/products/Kattama.png?...

            URL url = new URL(pictureUrl);
            String publicPath = "/minio/" + url.getPath();
            String publicUrl = new URL(
                    "https",
                    "aafaci.com",
                    url.getPort()>0?-1:-1,
                    publicPath+"?"+url.getQuery()
            ).toString();
            return publicUrl;
        } catch (ErrorResponseException |
                 InsufficientDataException |
                 InternalException |
                 InvalidKeyException |
                 InvalidResponseException |
                 IOException |
                 NoSuchAlgorithmException |
                 XmlParserException |
                 ServerException e) {
            // Можно логировать или обрабатывать по-другому
            throw new RuntimeException(
                    "Не удалось сгенерировать presigned URL для объекта: " + objectName, e
            );
        }
    }


    public String uploadPhoto(Path path){
        String objectName = path.getFileName().toString();
        try{
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .filename(path.toString())
                            .build()
            );
        }catch (Exception e){
            throw new RuntimeException("Не удалось загрузить " + objectName + " в MinIO", e);
        }
        return objectName;
    }


}