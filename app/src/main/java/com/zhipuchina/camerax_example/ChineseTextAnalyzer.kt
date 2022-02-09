package com.zhipuchina.camerax_example

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import java.time.Duration
import java.time.Instant

typealias RecognitionListener = ( text : String) -> Unit

/**
 * 自定义中文分析器
 * @author markrenChina
 */
class ChineseTextAnalyzer(
    listener :RecognitionListener?= null
) : ImageAnalysis.Analyzer{

    private val listeners = ArrayList<RecognitionListener>().apply { listener?.let { add(it) } }

    fun onFrameAnalyzed(listener: RecognitionListener) = listeners.add(listener)

    private val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())


    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        // If there are no listeners attached, we don't need to perform analysis
        if (listeners.isEmpty()) {
            image.close()
            return
        }
        val currentTime : Any
        // Keep track of frames analyzed
        currentTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.now()
        }else {
            System.currentTimeMillis()
        }
        //获取image,用mediaImage.close()是不安全的
        val mediaImage = image.image
        mediaImage?.let {
            val photo = InputImage.fromMediaImage(mediaImage,image.imageInfo.rotationDegrees)
            recognizer.process(photo).addOnSuccessListener { text: Text ->
                listeners.forEach { it(text.text) }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    Log.d("ChineseTextAnalyzer", "analyze: duration ${Duration.between(currentTime as Instant,Instant.now()).nano} nanos")
                }else {
                    Log.d("ChineseTextAnalyzer", "analyze: duration ${System.currentTimeMillis() - (currentTime as Long)} Millis")
                }
            }
        }

        image.close()
    }
}