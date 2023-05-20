package es.unizar.mii.tmdad.chatapp.service

import io.minio.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@Service
class MinioService (private val minioClient: MinioClient) {

    fun uploadFile(file: MultipartFile, bucketName:String) {

        val found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())
        if (!found) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }
        /*val bucketList: List<Bucket> = minioClient.listBuckets()
        for (bucket in bucketList) {
            logger.info("buckets {} ", bucket.name())
        }

        logger.info("filestream {}", file.inputStream)
        logger.info("filesize {}", file.size)
        logger.info("filetype {}", file.contentType)
        logger.info("filename {}", file.originalFilename)*/

        minioClient.putObject(
            PutObjectArgs.builder().bucket(bucketName).`object`(file.originalFilename).stream(
                file.inputStream, file.size, -1
            )
                .contentType(file.contentType)
                .build()
        )


    }

    fun downloadFile(file: String, bucketName: String) : InputStream{
       return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .`object`(file)
                .build()
        )

    }
}
