package com.example.ai_project

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.ai_project.ml.IrisFlowerModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

class Iris_prediction : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iris_prediction)
        var edt1 = findViewById<EditText>(R.id.slen)
        var edt2 = findViewById<EditText>(R.id.swid)
        var edt3 = findViewById<EditText>(R.id.plen)
        var edt4 = findViewById<EditText>(R.id.pwid)
        var txt = findViewById<TextView>(R.id.txtview)
        var btn_predict = findViewById<Button>(R.id.btn_predict2)

        btn_predict.setOnClickListener {
            var e1 = edt1.text.toString().toFloat()
            var e2 = edt2.text.toString().toFloat()
            var e3 = edt3.text.toString().toFloat()
            var e4 = edt4.text.toString().toFloat()

            var byteBuffer : ByteBuffer = ByteBuffer.allocateDirect(4*4)
            byteBuffer.putFloat(e1)
            byteBuffer.putFloat(e2)
            byteBuffer.putFloat(e3)
            byteBuffer.putFloat(e4)

            val model = IrisFlowerModel.newInstance(this)
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 4), DataType.FLOAT32)
            inputFeature0.loadBuffer(byteBuffer)
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

            txt.setText("Iris Setosa = "+ outputFeature0[0].toString() + "\n"
                    + "Iris Versicolor = "+ outputFeature0[1].toString() + "\n"
                    + "Iris Virginica\n = "+  outputFeature0[2].toString())
            model.close()

        }


    }
}