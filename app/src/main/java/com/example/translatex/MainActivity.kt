package com.example.translatex

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.sql.ClientInfoStatus
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(),TextToSpeech.OnInitListener {

    private lateinit var lang1Spinner: Spinner
    private lateinit var lang2Spinner: Spinner
    private lateinit var translateBtn: MaterialButton
//    private lateinit var camBtn : MaterialButton
    private lateinit var micBtn : MaterialButton
    private lateinit var clearBtn1 : MaterialButton
    private lateinit var saveBtn : MaterialButton
    private lateinit var showSavedBtn : MaterialButton
    private lateinit var lang1EditText : TextInputEditText

    private lateinit var textToSpeechBtn1 : MaterialButton
    private lateinit var textToSpeechBtn2 : MaterialButton
    private lateinit var textToSpeech : TextToSpeech

    private lateinit var lang2TextView: MaterialTextView
    private val REQUEST_PERMISSION_CODE= 1




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // init
        lang1Spinner = findViewById(R.id.lang1Spinner)
        lang2Spinner = findViewById(R.id.lang2Spinner)
        translateBtn = findViewById(R.id.translateBtn)
//        camBtn = findViewById(R.id.camBtn)
        micBtn = findViewById(R.id.micBtn)
        clearBtn1 = findViewById(R.id.clearBtn1)
        saveBtn  = findViewById(R.id.saveBtn)
        showSavedBtn  = findViewById(R.id.showSavedBtn)
        lang1EditText = findViewById(R.id.lang1EditText)
        lang2TextView = findViewById(R.id.lang2TextView)

        textToSpeechBtn1 = findViewById(R.id.txtToSpeech1)
        textToSpeechBtn2 = findViewById(R.id.txtToSpeech2)
        textToSpeech = TextToSpeech(this,this)

        textToSpeechBtn1.setOnClickListener {
            val lang1text : String = lang1EditText.text.toString()
            textToSpeech.speak(lang1text,TextToSpeech.QUEUE_FLUSH,null,null)
        }

        textToSpeechBtn2.setOnClickListener {
            val lang2text : String = lang2TextView.text.toString()
            textToSpeech.speak(lang2text,TextToSpeech.QUEUE_FLUSH,null,null)
        }

        var lang1Position =0
        var lang2Position =0
        var fromLanguageCode =0
        var toLanguageCode =0

        var langArray = arrayOf("English", "Japanese","Hindi", "Urdu","Mandarin","Cantonese","Korean","Afrikaans", "Arabic","Belarusian","Bengali","Bulgarian","Catalan","Czech","Turkish","Welsh")

        var fromOption = langArray[0]
        var toOption = langArray[1]

        val lang1Adapter = ArrayAdapter(this,R.layout.spinner_item_layout,langArray)
        lang1Spinner.adapter = lang1Adapter

        lang1Spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                    fromOption = langArray[position]
                    fromLanguageCode = getLanguageCode(langArray[position])
                    lang1Position = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                    fromOption = langArray[0]
                    fromLanguageCode = getLanguageCode(langArray[0])
                    lang1Position = 0
                }
            }


        val lang2Adapter = ArrayAdapter(this,R.layout.spinner_item_layout,langArray)
        lang2Spinner.adapter = lang2Adapter

        lang2Spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                toOption = langArray[position]
                toLanguageCode = getLanguageCode(langArray[position])
                lang2Position = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                toOption = langArray[0]
                toLanguageCode = getLanguageCode(langArray[0])
                lang2Position = 0
            }
        }

        clearBtn1.setOnClickListener {
            lang1EditText.setText("")
            lang2TextView.text=""
        }


        translateBtn.setOnClickListener {

            lang2TextView.text = ""
            if(lang1EditText.text?.isEmpty() == true){
                Toast.makeText(this,"Please enter your text to translate",Toast.LENGTH_LONG).show()
            }
            else{
                translateText(fromLanguageCode,toLanguageCode,lang1EditText.text.toString())
            }
        }

        micBtn.setOnClickListener{
            val micIntent = Intent (RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            micIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            micIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
            micIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"You can speak now")
            try{
                startActivityForResult(micIntent,REQUEST_PERMISSION_CODE)
            }
            catch (e: Exception){
                e.printStackTrace()
                Toast.makeText(this,"${e.message}",Toast.LENGTH_LONG).show()
            }
        }



        saveBtn.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val gson = Gson()
            val fromLang = langArray[lang1Position]
            val toLang = langArray[lang2Position]
            val fromText = lang1EditText.text.toString()
            val toText = lang2TextView.text.toString()

//            val date = "${day}/${month}/${year}"
            val inputDate = "${day}/${month+1}/${year}"
            val inputFormat = SimpleDateFormat("d/M/yyyy", Locale.US)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val date = outputFormat.format(inputFormat.parse(inputDate))

            val innerPair = Pair("${fromLang} : ${fromText}" , "${toLang} : ${toText}")
            val newTranslation = gson.toJson(innerPair)
            val preferences = getSharedPreferences("MyTranslations", Context.MODE_PRIVATE)

            val existingValuesString = preferences.getString(date, null)
            val existingValues = if (existingValuesString != null) {
                gson.fromJson(existingValuesString, Array<String>::class.java).toMutableList()
            } else {
                mutableListOf<String>()
            }
            existingValues.add(newTranslation)

            val updatedValuesJson = gson.toJson(existingValues)

            val editor = preferences.edit()
            editor.putString(date,updatedValuesJson)
            editor.apply()

//            val jsonString = preferences.getString(date,null)
//            val type = object : TypeToken<MutableList<String>>() {}.type
//            val translationsList : MutableList<String> = gson.fromJson(jsonString,type)
//            val transType = object : TypeToken<Pair<String,String>>(){}.type
//            val firstTranslationPair : Pair<String,String> = gson.fromJson(translationsList[0],transType)
//            Log.d("First-translation-pair","first : ${firstTranslationPair.first} : second : ${firstTranslationPair.second}")
//            Log.d("List Size","${translationsList.size}")



        }


        showSavedBtn.setOnClickListener {
            val showSavedIntent = Intent(this,ShowSavedTranslations::class.java)
            startActivity(showSavedIntent)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode ==REQUEST_PERMISSION_CODE && resultCode == RESULT_OK && data != null){
            val result : ArrayList<String>? = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            if(result!=null && result.isNotEmpty()) {
                val recognisedText : String = result[0]
                lang1EditText.setText(recognisedText)
            }
        }
    }

    private fun translateText(fromLanguageCode : Int, toLanguageCode : Int, sourceText : String) {
        lang2TextView.text = "Translating now..."
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(fromLanguageCode)
            .setTargetLanguage(toLanguageCode)
            .build()
        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        val conditions = FirebaseModelDownloadConditions.Builder().build()
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener{
            lang2TextView.text = "Translation Started..."
            translator.translate(sourceText).addOnSuccessListener {
                lang2TextView.text = it
            }.addOnFailureListener {
                Toast.makeText(this,"${it.message} : Failed to Translate",Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this,"${it.message} : Failed to Download Language Model",Toast.LENGTH_LONG).show()
        }
    }

    fun getLanguageCode(langString : String): Int {
        var languageCode = 0
        when(langString){
            "English"-> languageCode = FirebaseTranslateLanguage.EN
            "Hindi" -> languageCode = FirebaseTranslateLanguage.HI
            "Japanese"-> languageCode  = FirebaseTranslateLanguage.JA
            "Urdu"-> languageCode = FirebaseTranslateLanguage.UR
            "Mandarin"-> languageCode = FirebaseTranslateLanguage.ZH
            "Cantonese"-> languageCode = FirebaseTranslateLanguage.ZH
            "Korean"-> languageCode = FirebaseTranslateLanguage.KO
            "Afrikaans"-> languageCode = FirebaseTranslateLanguage.AF
            "Arabic"-> languageCode = FirebaseTranslateLanguage.AR
            "Belarusian"-> languageCode = FirebaseTranslateLanguage.BE
            "Bengali"-> languageCode = FirebaseTranslateLanguage.BN
            "Bulgarian"-> languageCode = FirebaseTranslateLanguage.BG
            "Catalan"-> languageCode = FirebaseTranslateLanguage.CA
            "Czech"-> languageCode = FirebaseTranslateLanguage.CS
            "Welsh"-> languageCode = FirebaseTranslateLanguage.CY
            "Turkish"-> languageCode = FirebaseTranslateLanguage.TR
            else -> languageCode = FirebaseTranslateLanguage.EN
        }

        return languageCode
    }

    //TEXT TO SPEECH

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // TTS is successfully initialized
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

}