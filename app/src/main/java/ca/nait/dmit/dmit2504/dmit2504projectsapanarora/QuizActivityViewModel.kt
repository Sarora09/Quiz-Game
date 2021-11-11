package ca.nait.dmit.dmit2504.dmit2504projectsapanarora

import android.util.Log
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuizActivityViewModel : ViewModel() {

    // List "quizQuestionAnswerList" is created to receive the value from API
    private val quizQuestionAnswerList = mutableListOf<QuizDetail>()
    // Variable declared to receive the difficulty value from the "QuizActivity"
    private var selected_difficulty=""

    // "questionAnswerList" will contain the data received from API and share it with "QuizActivity"
    private val questionAnswerList : MutableLiveData<List<QuizDetail>> by lazy {
        MutableLiveData<List<QuizDetail>>().also {
            loadQuestionAnswer()
        }
    }

    // The QuizActivity will retrieve the question answer list from API through below method
    // Below method also sets the difficulty level
    fun getQuestionAnswers(difficulty:String) : LiveData<List<QuizDetail>> {
        selected_difficulty = difficulty
        return questionAnswerList
    }

    fun loadQuestionAnswer() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val questionAnswerService = retrofit.create(QuestionAnswerInterface::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val responseData= questionAnswerService.getAllQuestionAnswers("10", "${selected_difficulty}", "multiple")
            withContext(Dispatchers.Main) {
                for (item in responseData.results) {
                    val quizDetailObject = QuizDetail()
                    // To decode the special characters in string, HtmlCompat class is used below
                    quizDetailObject.question = HtmlCompat.fromHtml(item.question, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                    quizDetailObject.correctAnswer = item.correct_answer
                    quizDetailObject.totalOptions = item.incorrect_answers
                    quizDetailObject.totalOptions.add(quizDetailObject.correctAnswer)
                    // The "totalOptions" list in "quizDetailObject" is shuffled
                    quizDetailObject.totalOptions.shuffle()
                    // To decode the special characters in string, HtmlCompat class is used below
                    quizDetailObject.totalOptions[0] = HtmlCompat.fromHtml(quizDetailObject.totalOptions[0], HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                    quizDetailObject.totalOptions[1] = HtmlCompat.fromHtml(quizDetailObject.totalOptions[1], HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                    quizDetailObject.totalOptions[2] = HtmlCompat.fromHtml(quizDetailObject.totalOptions[2], HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                    quizDetailObject.totalOptions[3] = HtmlCompat.fromHtml(quizDetailObject.totalOptions[3], HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                    quizQuestionAnswerList.add(quizDetailObject)
                }
                // The mutable live data list "questionAnswerList" is assigned the list retrieved from API
                questionAnswerList.value = quizQuestionAnswerList
            }
        }
    }
}