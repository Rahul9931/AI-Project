package com.example.ai_project

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ai_project.databinding.ActivityBraintumorBinding
import com.example.ai_project.databinding.ActivitySkinCancerBinding
import com.example.ai_project.ml.SkinCancer
import com.example.ai_project.ml.SkinCancerDetection
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class Skin_Cancer_Activity : AppCompatActivity() {
    lateinit var bitmap: Bitmap
    lateinit var status:String
    private val binding: ActivitySkinCancerBinding by lazy {
        ActivitySkinCancerBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSelectSkincancer.setOnClickListener {
            pickimage.launch("image/*")
        }
        binding.btnPredictSkincancer.setOnClickListener {
            predictSkinCancer()
        }
    }

    private fun predictSkinCancer() {
        val resized:Bitmap = Bitmap.createScaledBitmap(bitmap,256, 256,true)
        val model = SkinCancer.newInstance(this)

        val tensorimg = TensorImage(DataType.FLOAT32)
        tensorimg.load(resized)
        val byteBuffer = tensorimg.buffer

// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
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
        binding.outSkincancer.setText("${max}")



// Releases model resources if no longer used.
        model.close()

    }

    val pickimage = registerForActivityResult(ActivityResultContracts.GetContent()){
            uri ->
        if (uri!=null){
            binding.imageSkincancer.setImageURI(uri)
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