package moe.sekiu.minilpa.api

import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import moe.sekiu.minilpa.model.ActivationCode
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

private val logger = LoggerFactory.getLogger("QRCodeRoutes")

@Serializable
data class QRCodeParseResponse(
    val activationCode: String,
    val smdp: String,
    val matchingId: String?,
    val oid: String?
)

fun Route.configureQRCodeRoutes() {
    route("/qrcode") {
        /**
         * POST /api/qrcode/parse
         * 解析上传的二维码图片
         */
        post("/parse") {
            try {
                val multipart = call.receiveMultipart()
                var imageBytes: ByteArray? = null

                // 解析 multipart/form-data
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            if (part.name == "image" || part.name == "file") {
                                imageBytes = part.streamProvider().readBytes()
                                logger.info("收到二维码图片，大小: ${imageBytes?.size ?: 0} bytes")
                            }
                        }
                        is PartData.FormItem -> {
                            logger.debug("收到表单项: ${part.name} = ${part.value}")
                        }
                        else -> {
                            logger.debug("收到其他类型的 part: ${part::class.simpleName}")
                        }
                    }
                    part.dispose()
                }

                if (imageBytes == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "未找到图片文件，请使用 'image' 或 'file' 字段上传")
                    )
                }

                // 检查文件大小（限制为 10MB）
                if (imageBytes!!.size > 10 * 1024 * 1024) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "图片文件过大，最大支持 10MB")
                    )
                }

                // 使用 ImageIO 读取图片
                val image = try {
                    ImageIO.read(ByteArrayInputStream(imageBytes!!))
                } catch (e: Exception) {
                    logger.error("读取图片失败", e)
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "无效的图片格式")
                    )
                }

                if (image == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "无法解析图片")
                    )
                }

                // 使用 ZXing 解析二维码
                val source = BufferedImageLuminanceSource(image)
                val bitmap = BinaryBitmap(HybridBinarizer(source))
                val reader = MultiFormatReader()

                val result = try {
                    reader.decode(bitmap)
                } catch (e: Exception) {
                    logger.error("二维码解析失败", e)
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "未能识别二维码，请确保图片清晰且包含有效的二维码")
                    )
                }

                val text = result.text
                logger.info("二维码解析成功: $text")

                // 解析激活码
                val activationCode = ActivationCode.of(text)
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "二维码不包含有效的 LPA 激活码")
                    )

                // 返回解析结果
                call.respond(
                    QRCodeParseResponse(
                        activationCode = text,
                        smdp = activationCode.SMDP,
                        matchingId = activationCode.MatchingID,
                        oid = activationCode.OID
                    )
                )
            } catch (e: Exception) {
                logger.error("处理二维码解析请求失败", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "处理二维码解析请求失败"))
                )
            }
        }
    }
}
