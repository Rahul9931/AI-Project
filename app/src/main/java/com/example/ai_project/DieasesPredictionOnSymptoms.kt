package com.example.ai_project

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import com.example.ai_project.databinding.ActivityDieasesPredictionOnSymptomsBinding
import com.example.ai_project.ml.Dieasesinfloat
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DieasesPredictionOnSymptoms : AppCompatActivity() {
    private val binding:ActivityDieasesPredictionOnSymptomsBinding by lazy {
        ActivityDieasesPredictionOnSymptomsBinding.inflate(layoutInflater)
    }
    // Declare and Initialization
    val symList = mutableListOf("itching", "skin_rash", "nodal_skin_eruptions", "continuous_sneezing", "shivering", "chills", "joint_pain", "stomach_pain", "acidity", "ulcers_on_tongue", "muscle_wasting", "vomiting", "burning_micturition", "spotting_ urination", "fatigue", "weight_gain", "anxiety", "cold_hands_and_feets", "mood_swings", "weight_loss", "restlessness", "lethargy", "patches_in_throat", "irregular_sugar_level", "cough", "high_fever", "sunken_eyes", "breathlessness", "sweating", "dehydration", "indigestion", "headache", "yellowish_skin", "dark_urine", "nausea", "loss_of_appetite", "pain_behind_the_eyes", "back_pain", "constipation", "abdominal_pain", "diarrhoea", "mild_fever", "yellow_urine", "yellowing_of_eyes", "acute_liver_failure", "fluid_overload", "swelling_of_stomach", "swelled_lymph_nodes", "malaise", "blurred_and_distorted_vision", "phlegm", "throat_irritation", "redness_of_eyes", "sinus_pressure", "runny_nose", "congestion", "chest_pain", "weakness_in_limbs", "fast_heart_rate", "pain_during_bowel_movements", "pain_in_anal_region", "bloody_stool", "irritation_in_anus", "neck_pain", "dizziness", "cramps", "bruising", "obesity", "swollen_legs", "swollen_blood_vessels", "puffy_face_and_eyes", "enlarged_thyroid", "brittle_nails", "swollen_extremeties", "excessive_hunger", "extra_marital_contacts", "drying_and_tingling_lips", "slurred_speech", "knee_pain", "hip_joint_pain", "muscle_weakness", "stiff_neck", "swelling_joints", "movement_stiffness", "spinning_movements", "loss_of_balance", "unsteadiness", "weakness_of_one_body_side", "loss_of_smell", "bladder_discomfort", "foul_smell_of urine", "continuous_feel_of_urine", "passage_of_gases", "internal_itching", "toxic_look_(typhos)", "depression", "irritability", "muscle_pain", "altered_sensorium", "red_spots_over_body", "belly_pain", "abnormal_menstruation", "dischromic _patches", "watering_from_eyes", "increased_appetite", "polyuria", "family_history", "mucoid_sputum", "rusty_sputum", "lack_of_concentration", "visual_disturbances", "receiving_blood_transfusion", "receiving_unsterile_injections", "coma", "stomach_bleeding", "distention_of_abdomen", "history_of_alcohol_consumption", "fluid_overload.1", "blood_in_sputum", "prominent_veins_on_calf", "palpitations", "painful_walking", "pus_filled_pimples", "blackheads", "scurring", "skin_peeling", "silver_like_dusting", "small_dents_in_nails", "inflammatory_nails", "blister", "red_sore_around_nose", "yellow_crust_ooze")
    val symListInFloat = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    val usersymList = mutableSetOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val adapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, symList)
        val autoCompleteTextView=binding.symptoms
        autoCompleteTextView.setAdapter(adapter)

        // Action On Button Symptoms
        binding.btnSym.setOnClickListener {
            val text_sym = autoCompleteTextView.text.toString()
            usersymList.add(text_sym)
            binding.symResult.setText("$usersymList")
            autoCompleteTextView.text.clear()
            Log.d("symptomlist","${usersymList}")
            Log.d("symListInFloat1","${symListInFloat}")

        }
        // Action on Prediction Button
        binding.btnPredictDieases.setOnClickListener {
            // Creating Symptoms List
            for (k in 0..symList.size-1){
                for( z in usersymList){
                    if(symList[k].equals(z)){
                        symListInFloat[k] = 1
                    }
                }
            }
            // Put Symptoms List into byteBuffer
            var byteBuffer : ByteBuffer = ByteBuffer.allocateDirect(symListInFloat.size*4)
            byteBuffer.order(ByteOrder.nativeOrder())
            for (i in symListInFloat){
                byteBuffer.putFloat(i.toFloat())
            }


            Log.d("symListInFloat","${symListInFloat}")
            val model = Dieasesinfloat.newInstance(this)

// Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 132), DataType.FLOAT32)
            inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
            var inputList = mutableListOf<Float>()
            for (i in 0..inputFeature0.flatSize-1){
                inputList.add(inputFeature0.floatArray[i])
            }
            Log.d("inputList","${inputList}")
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray
            var max = getMax(outputFeature0)
            binding.symResult.setText("${max}")
            val optimumResult = mutableListOf<Float>()
            for (r in 0..outputFeature0.size-1){
                optimumResult.add(outputFeature0[r])
            }
            Log.d("result","${optimumResult}")
// Releases model resources if no longer used.
            model.close()
            usersymList.clear()
            for (zero in 0..symListInFloat.size-1){
                symListInFloat[zero] = 0
            }
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
        binding.logresult.text = max.toString()
        return ind
    }
}