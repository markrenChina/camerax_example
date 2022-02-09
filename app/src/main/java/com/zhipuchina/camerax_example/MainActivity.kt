package com.zhipuchina.camerax_example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * https://developer.android.google.cn/training/camerax/preview?hl=zh_cn#implementation
 * 没有写申请权限代码
 * @author markrenChina
 */
class MainActivity : AppCompatActivity() {

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    //用于分析的线程
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**
         * 创建的实例ProcessCameraProvider。这用于将摄像机的生命周期绑定到生命周期所有者。
         * 由于CameraX具有生命周期感知功能，因此省去了打开和关闭相机的任务
         */
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        /**
         * 将侦听器添加到中cameraProviderFuture。添加一个Runnable作为一个参数。
         * 添加作为第二个参数。这会返回在主线程上运行的。ContextCompat.getMainExecutor()Executor
         */
        cameraProviderFuture.addListener(Runnable {
            //用于将相机的生命周期绑定到LifecycleOwner应用程序的过程中。
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        //初始化Preview对象，调用其上的对象，从取景器中获取表面提供程序，然后在预览中进行设置。
        val preview : Preview = Preview.Builder()
            .build()

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val previewView = findViewById<PreviewView>(R.id.previewView)
        preview.setSurfaceProvider(previewView.surfaceProvider)

        //分析器
        val imageAnalyzer = ImageAnalysis.Builder()
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, ChineseTextAnalyzer { text ->
                    Log.d("MainActivity", "res: $text")
                })
            }

        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview,imageAnalyzer)
    }
}