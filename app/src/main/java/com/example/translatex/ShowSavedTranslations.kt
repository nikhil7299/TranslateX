package com.example.translatex

import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialStyledDatePickerDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class ShowSavedTranslations : AppCompatActivity(),TextToSpeech.OnInitListener {
    private lateinit var dateText : TextInputEditText
    lateinit var datePickerBtn : MaterialButton
    lateinit var showBtn : MaterialButton
    lateinit var translationsListView : ListView
    private lateinit var textToSpeech : TextToSpeech
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_saved_translations)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        dateText = findViewById(R.id.dateText)
        datePickerBtn = findViewById(R.id.datePickerBtn)
        showBtn = findViewById(R.id.showBtn)
        translationsListView = findViewById(R.id.translationsListView)

        //Text to speech
        textToSpeech = TextToSpeech(this,this)


        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val inputDate = "${day}/${month+1}/${year}"
        val inputFormat = SimpleDateFormat("d/M/yyyy", Locale.US)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val outputDate = outputFormat.format(inputFormat.parse(inputDate))
        dateText.setText(outputDate)

        fun onBackPressed(){
            super.onBackPressed()
            finish()
        }

        datePickerBtn.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setTextInputFormat(SimpleDateFormat("dd/MM/yyyy",Locale.US))
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

            datePicker.show(supportFragmentManager,"Date Picker")
            datePicker.addOnPositiveButtonClickListener {
                    selectedDateInMillis ->
                val selectedDate = Date(selectedDateInMillis)
//                Log.d("selectedDate",selectedDateInMillis.toString())
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                dateText.setText(dateFormat.format(selectedDate))
            }
        }



        showBtn.setOnClickListener {
            val date = dateText.text.toString()
            Log.d("date",date)
            val gson = Gson()
            val preferences = getSharedPreferences("MyTranslations", Context.MODE_PRIVATE)
            val jsonString = preferences.getString(date,null)
            if(jsonString!=null) {
                translationsListView.visibility  = View.VISIBLE
                val type = object : TypeToken<MutableList<String>>() {}.type
                val translationsList: MutableList<String> = gson.fromJson(jsonString, type)
//            val transType = object : TypeToken<Pair<String, String>>(){}.type

                val translations: List<Pair<String, String>> = translationsList.map {
                    val translationPair = Gson().fromJson(it, TranslationPair::class.java)
                    Pair(translationPair.first, translationPair.second)
                }
                Log.d("translations", translations.toString())
                print(translations)

                val translationAdapter = TranslationAdapter(this, translations)
                translationsListView.adapter = translationAdapter
//                if(translations==null){
//                    translationAdapter.clear()
//                }

            }
            else {
                translationsListView.visibility  = View.GONE
                return@setOnClickListener
            }
        }
    }

    data class TranslationPair(
        val first : String,
        val second: String
    )

    inner class TranslationAdapter(
       context: AppCompatActivity,private val translations : List<Pair<String,String>>
    ): ArrayAdapter<Pair<String,String>>(context,R.layout.translation_list_item,translations){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        if (translations==null){
//            parent.removeAllViews()
//        }
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.translation_list_item,parent,false)
        val translation = getItem(position)
        view.findViewById<TextView>(R.id.listLang1).text = "${translation?.first?.substringBefore(" ")} : "
        view.findViewById<TextView>(R.id.listLang1Text).text = translation?.first?.substringAfter(": ")
        view.findViewById<TextView>(R.id.listLang2).text  = "${translation?.second?.substringBefore(" ")} : "
        view.findViewById<TextView>(R.id.listLang2Text).text = translation?.second?.substringAfter(": ")
        view.findViewById<ImageButton>(R.id.lang1Speech).setOnClickListener {
            val lang1Text : String? = translation?.first?.substringAfter(": ")
            textToSpeech.speak(lang1Text,TextToSpeech.QUEUE_FLUSH,null,null)
        }
        view.findViewById<ImageButton>(R.id.lang2Speech).setOnClickListener {
            val lang2Text: String? = translation?.second?.substringAfter(": ")
            textToSpeech.speak(lang2Text,TextToSpeech.QUEUE_FLUSH,null,null)
        }

        return view
    }

    }

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