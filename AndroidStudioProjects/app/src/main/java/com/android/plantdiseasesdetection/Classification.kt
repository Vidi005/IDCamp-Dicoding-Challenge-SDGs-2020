package com.android.plantdiseasesdetection

import android.content.res.AssetManager
import android.graphics.*
import com.priyankvasa.android.cameraviewex.Image
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class Classification (assetManager: AssetManager){

    private val model: Interpreter = Interpreter(getModelByteBuffer(assetManager, MODEL_PATH))
    private val label: List<String> = getLabelList(assetManager, LABEL_PATH)

    companion object {
        private const val MODEL_PATH = "Plant_Leaf_Diseases_Classification_MobileNet.tflite"
        private const val LABEL_PATH = "labels.txt"
        private const val BATCH_SIZE = 1 // process only 1 image at a time
        private const val MODEL_INPUT_SIZE = 224 // 224x224
        private const val BYTES_PER_CHANNEL = 4 // float size
        private const val PIXEL_SIZE = 3 // rgb
        private const val IMAGE_STD = 255f
    }

    private fun getModelByteBuffer(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun getLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        val labels = ArrayList<String>()
        val reader = BufferedReader(InputStreamReader(assetManager.open(labelPath)))
        while (true) {
            val label = reader.readLine() ?: break
            labels.add(label)
        }
        reader.close()
        return labels
    }

    fun recognizeTakenPicture(data: ByteArray): List<Detection> {
        val unscaledBitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        val bitmap = Bitmap.createScaledBitmap(unscaledBitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, false)
        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        val result = Array(BATCH_SIZE) { FloatArray(label.size) }
        model.run(byteBuffer, result)
        return parseResults(result)
    }

    fun recognizePreviewFrames(image: Image): List<Detection> {
        val data: ByteArray
        val yuvImage = YuvImage(
            image.data,
            ImageFormat.NV21,
            image.width,
            image.height,
            null
        )
        val jpegDataStream = ByteArrayOutputStream()
        val previewFrameScale = 0.4f
        yuvImage.compressToJpeg(
            Rect(0, 0, image.width, image.height),
            (100 * previewFrameScale).toInt(),
            jpegDataStream
        )
        data = jpegDataStream.toByteArray()
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val unscaledBitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        val bitmap = Bitmap.createScaledBitmap(unscaledBitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, false)
        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        val result = Array(BATCH_SIZE) { FloatArray(label.size) }
        model.run(byteBuffer, result)
        return parseResults(result)
    }

    fun recognizeImage(bitmap: Bitmap): List<Detection> {
        val bitmap = Bitmap.createScaledBitmap(bitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, false)
        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        val result = Array(BATCH_SIZE) { FloatArray(label.size) }
        model.run(byteBuffer, result)
        return parseResults(result)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * BYTES_PER_CHANNEL * PIXEL_SIZE)
            .apply { order(ByteOrder.nativeOrder()) }
        val pixelValues = IntArray(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE)
        bitmap.getPixels(pixelValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until MODEL_INPUT_SIZE) {
            for (j in 0 until MODEL_INPUT_SIZE) {
                val pixelValue = pixelValues[pixel++]
                byteBuffer.putFloat((pixelValue shr 16 and 0xFF) / IMAGE_STD)
                byteBuffer.putFloat((pixelValue shr 8 and 0xFF) / IMAGE_STD)
                byteBuffer.putFloat((pixelValue and 0xFF) / IMAGE_STD)
            }
        }
        return byteBuffer
    }

    private fun parseResults(result: Array<FloatArray>): List<Detection> {
        val recognitions: MutableList<Detection> = mutableListOf()
        val accuracy = result[0]
        for (position in label.indices) {
            val detection = Detection(
                label[position],
                accuracy[position]
            )
            recognitions.add(detection)
        }
        return recognitions.sortedByDescending { it.accuracy }
    }
}