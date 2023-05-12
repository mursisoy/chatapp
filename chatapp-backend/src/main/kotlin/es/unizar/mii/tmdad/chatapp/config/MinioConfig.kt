//package es.unizar.mii.tmdad.chatapp.config
//
//import io.minio.MinioClient
//import org.springframework.context.annotation.Bean
//import java.net.ConnectException
//
//class MinioConfig {
//
//    //credenciales a cambiar
//    private val accessKey="Q3AM3UQ867SPQQA43P2F"
//
//    private val accessSecret="zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG"
//
//    private val minioUrl="https://play.min.io"
//
//    @Bean
//    fun generateMinioClient():MinioClient {
//
//        while (true) {
//            try {
//                val cliente= MinioClient.builder()
//                        .endpoint(minioUrl, 9000, false)
//                        .credentials(accessKey, accessSecret)
//                        .build()
//                return cliente
//            } catch (e: ConnectException) {
//                Thread.sleep(5000)
//                // apply retry logic
//            }
//
//        }
//    }
//
//}