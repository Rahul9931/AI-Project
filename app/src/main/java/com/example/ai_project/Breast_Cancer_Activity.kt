package com.example.ai_project

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ai_project.databinding.ActivityBreastCancerBinding
import com.example.ai_project.ml.AlzimerTfmodel
import com.example.ai_project.ml.BrestCancer
import com.example.ai_project.ml.LungsCancertf
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class Breast_Cancer_Activity : AppCompatActivity() {
    lateinit var bitmap: Bitmap
    lateinit var status:String
    private val binding:ActivityBreastCancerBinding by lazy{
        ActivityBreastCancerBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnSelectBreastCancer.setOnClickListener {
            pickimage.launch("image/*")
        }

        binding.btnPredictBreastCancer.setOnClickListener {
            //predictBreastCancer()
            //predictAlzimer()
            //predictLungsCancer()

        }
    }

    private fun predictLungsCancer() {
        val resized:Bitmap = Bitmap.createScaledBitmap(bitmap,128, 128,true)
        val model = LungsCancertf.newInstance(this)

        val tensorimg = TensorImage(DataType.FLOAT32)
        tensorimg.load(resized)
        val byteBuffer = tensorimg.buffer

// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 128, 128, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        var max = getMax(outputFeature0.floatArray)

        // Set Output
        val resultList = mutableListOf<Float>()
        for (i in 0 until outputFeature0.flatSize){
            resultList.add(outputFeature0.floatArray[i])
        }
        Log.d("resultList","${resultList}")
        binding.resultBreastCancer.setText("${max}")

// Releases model resources if no longer used.
        model.close()

    }

    private fun predictAlzimer() {
        val resized:Bitmap = Bitmap.createScaledBitmap(bitmap,128, 128,true)
        val model = AlzimerTfmodel.newInstance(this)

        val tensorimg = TensorImage(DataType.FLOAT32)
        tensorimg.load(resized)
        val byteBuffer = tensorimg.buffer

// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 128, 128, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        var max = getMax(outputFeature0.floatArray)

        // Set Output
        val resultList = mutableListOf<Float>()
        for (i in 0 until outputFeature0.flatSize){
            resultList.add(outputFeature0.floatArray[i])
        }
        Log.d("resultList","${resultList}")
        binding.resultBreastCancer.setText("${max}")

// Releases model resources if no longer used.
        model.close()
    }

    private fun predictBreastCancer() {
        val resized:Bitmap = Bitmap.createScaledBitmap(bitmap,224, 224,true)
        val model = BrestCancer.newInstance(this)

        val tensorimg = TensorImage(DataType.FLOAT32)
        tensorimg.load(resized)
        val byteBuffer = tensorimg.buffer

// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        var max = getMax(outputFeature0.floatArray)

        // Set Output
        val resultList = mutableListOf<Float>()
        for (i in 0 until outputFeature0.flatSize){
            resultList.add(outputFeature0.floatArray[i])
        }
        Log.d("resultList","${resultList}")
        binding.resultBreastCancer.setText("${max}")

// Releases model resources if no longer used.
        model.close()

    }

    val pickimage = registerForActivityResult(ActivityResultContracts.GetContent()){
            uri ->
        if (uri!=null){
            binding.imageBreastCancer.setImageURI(uri)
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }
    }
    fun getMax(arr:FloatArray) : Int{
        var ind = 0
        var max = 0.0f
        for(i in 0..arr.size-1){
            if(arr[i]>max){
                ind = i
                max = arr[i]
            }
        }
        return ind
    }
}