//package es.unizar.mii.tmdad.chatapp.service
//
//import io.minio.DownloadObjectArgs
//import io.minio.MinioClient
//import io.minio.UploadObjectArgs
//import org.springframework.stereotype.Service
//import org.springframework.web.multipart.MultipartFile
//
//@Service
//class MinioService (private val minioClient: MinioClient) {
//
//    private val bucketName ="00000qweqwe"
//    fun uploadFile(files: MultipartFile) {
//        minioClient.uploadObject(
//            UploadObjectArgs.builder()
//                .bucket(bucketName)
//                .`object`(files.name)
//                .filename(files.name)
//                .build()
//        )
//    }
//
//    fun downloadFile(file: String) {
//        minioClient.downloadObject(
//            DownloadObjectArgs.builder()
//                .bucket(bucketName)
//                .`object`(file)
//                .filename(file)
//                .build()
//        )
//    }
//}