package no.nav.aap.fordeling.navenhet

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.function.Consumer
import org.reactivestreams.Publisher
import org.springframework.core.ResolvableType
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.util.MimeType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class LoggingJsonDecoder (private val payloadConsumer: Consumer<ByteArray>) : Jackson2JsonDecoder() {

    override fun decodeToMono(input: Publisher<DataBuffer>,
                              elementType: ResolvableType,
                              mimeType: MimeType?,
                              hints: Map<String, Any>?): Mono<Any> {
        val payload = ByteArrayOutputStream()

        // Augment the Flux, and intercept each group of bytes buffered
        val interceptor = Flux.from(input).doOnNext { buffer -> bufferBytes(payload, buffer) }
            .doOnComplete { payloadConsumer.accept(payload.toByteArray()) }

        // Return the original method, giving our augmented Publisher
        return super.decodeToMono(interceptor, elementType, mimeType, hints)
    }

    private fun bufferBytes(bao: ByteArrayOutputStream, buffer: DataBuffer) {
        try {
            bao.write(ByteUtils.extractBytesAndReset(buffer))
        }
        catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}

internal object ByteUtils {
    /**
     * Extracts bytes from the DataBuffer and resets the buffer so that it is ready to be re-read by the regular
     * request sending process.
     *
     * @param data data buffer with encoded data
     * @return copied data as a byte array.
     */
    fun extractBytesAndReset(data: DataBuffer): ByteArray {
        val bytes = ByteArray(data.readableByteCount())
        data.read(bytes)
        data.readPosition(0)
        return bytes
    }
}