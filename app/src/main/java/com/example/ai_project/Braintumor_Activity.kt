package com.example.ai_project

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ai_project.databinding.ActivityBraintumorBinding
import com.example.ai_project.ml.BrainTumor10Epochs
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class Braintumor_Activity : AppCompatActivity() {
    lateinit var bitmap:Bitmap
    lateinit var status:String
    private val binding:ActivityBraintumorBinding by lazy {
        ActivityBraintumorBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSelectBraintumor.setOnClickListener {
            pickimage.launch("image/*")
        }

        binding.btnPredictBraintumor.setOnClickListener {
            predictBraintumor()
        }
    }

    private fun predictBraintumor() {
        val resized:Bitmap = Bitmap.createScaledBitmap(bitmap,64,64,true)
        val model = BrainTumor10Epochs.newInstance(this)

        val tensorimg = TensorImage(DataType.FLOAT32)
        tensorimg.load(resized)
        val byteBuffer = tensorimg.buffer

// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 64, 64, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        // Set Output
        val resultList = mutableListOf<Float>()
        for (i in 0 until outputFeature0.flatSize){
            resultList.add(outputFeature0.floatArray[i])
        }
        Log.d("resultList","${resultList}")
        var value = outputFeature0.getIntValue(0)
        if (value==1){
            status = "Brain Tumor H"
        } else{
            status = "Brain Tumor Nahi H"
        }
        binding.outBraintumor.text = status
        //binding.outBraintumor.setText(outputFeature0.floatArray[0].toString())

// Releases model resources if no longer used.
        model.close()

    }

    val pickimage = registerForActivityResult(ActivityResultContracts.GetContent()){
            uri ->
        if (uri!=null){
            binding.imageBraintumor.setImageURI(uri)
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }
    }
}