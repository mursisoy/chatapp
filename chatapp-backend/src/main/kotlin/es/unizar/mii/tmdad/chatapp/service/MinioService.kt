package es.unizar.mii.tmdad.chatapp.service

import io.minio.*
import io.minio.errors.ErrorResponseException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class MinioService (private val minioClient: MinioClient) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun uploadFile(file: MultipartFile, bucketName:String): UUID? {
        var found = false
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())
        } catch (e: ErrorResponseException) {
            logger.error(e.response().toString())
        }
        if (!found) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build()
            )
        }
        try {
            val fileId = UUID.randomUUID()
            val userMetadata = mapOf<String,String>(
                "filename" to file.originalFilename!!
            )
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(fileId.toString())
                    .userMetadata(userMetadata)
                    .stream(
                    file.inputStream, file.size, -1
                )
                    .contentType(file.contentType)
                    .build()
            )
            return fileId
        } catch (e: Exception) {
            logger.debug(e.stackTraceToString())
        }
        return null
    }

    fun statFile(bucketName: String, file: String): StatObjectResponse {
        return minioClient.statObject(
            StatObjectArgs.builder()
                .bucket(bucketName)
                .`object`(file)
                .build()
        )
    }
    fun downloadFile(bucketName: String, file: String): GetObjectResponse {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .`object`(file)
                .build()
        )
    }
}
