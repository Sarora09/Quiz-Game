package ca.nait.dmit.dmit2504.dmit2504projectsapanarora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.text.HtmlCompat
import androidx.viewpager2.widget.ViewPager2
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class QuizActivity : AppCompatActivity() {

    // Mutable list of "QuizDetail" data type is created to hold the data received from API
    // API call specified in "QuizActivityViewModel" class
    private val quizQuestionAnswerList = mutableListOf<QuizDetail>()
    // A ViewPager2 is declared to show the data received from API and stored in the "quizQuestionAnswerList" specified above
    private val viewPager2 : ViewPager2 by lazy { findViewById(R.id.activity_quiz_viewPager2) }
    // An instance of "DataViewModel" class is created to share data between the fragments and this activity "QuizActivity"
    private val dataViewModel : DataViewModel by viewModels()
    // An instance of "QuizActivityViewModal" class is created to hold this activity UI data and to retrieve it when the screen is rotated
    // The activity gets destroyed when the screen is rotated. This ViewModel helps the activity to restore the same data it had before it being destroyed
    val quizActivityRestoreModel: QuizActivityViewModel by viewModels()
    // Based on the communication between fragment and this activity, the game score value will change
    private var score = 0
    // TextView to show the game score
    private val scoreTextView : TextView by lazy { findViewById(R.id.activity_quiz_currentScoreTextView) }
    // Indeterminate circular progress bar to be desplayed on screen before loading question answers in ViewPager2 on screen
    private val progressBarLayout : LinearLayout by lazy { findViewById(R.id.activity_quiz_progressBarLayout) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Variables declared to hold data received from Intent (sent by MainActivity)
        var name: String = ""
        var difficulty: String = ""

        // Receive the NAME and DIFFICULTY value
        if (intent != null && intent.extras != null) {

            val bundle = intent.extras
            name = bundle?.getString("NAME")!!
            difficulty = bundle?.getString("DIFFICULTY")!!
            val nameTextView = findViewById<TextView>(R.id.activity_quiz_playerNameTextView)
            val difficultyTextView = findViewById<TextView>(R.id.activity_quiz_selectedDifficultyTextView)
            nameTextView.setText("Player Name: ${name}")
            difficultyTextView.setText("Game Difficulty: ${difficulty}")

            // An instance of the "QuizPagerAdapter" is crated
            val pagerAdapter = QuizPagerAdapter(this, quizQuestionAnswerList)

            // The ViewPager2 is provided data by pagerAdapter
            viewPager2.adapter = pagerAdapter

            // Below instance of "QuizActivityViewModel" holds the UI data.
            // When the user rotates screen, the configuration changes which involves the activity and fragments being destroyed and gets recreated again
            // If we call the API here then the API call will happen again because of activity being recreated
            // To avoid the API call happening again when screen rotates, we have API being called in the ViewModel helper class "QuizActivityViewModel"
            // The ViewModel objects are automatically retained during configuration changes
            // The data retained from ViewModel is stored in the "quizQuestionAnswerList"
            // Once the list is populated, the adapter is notified about it
            quizActivityRestoreModel.getQuestionAnswers(difficulty)
                .observe(this, androidx.lifecycle.Observer { questionAnswersList ->
                    quizQuestionAnswerList.clear()
                    for (item in questionAnswersList) {
                        quizQuestionAnswerList.add(item)
                        object : CountDownTimer(500, 500) {
                            override fun onTick(millisUntilFinished: Long) {
                                progressBarLayout.visibility = View.VISIBLE
                            }
                            override fun onFinish() {
                                progressBarLayout.visibility = View.GONE
                                pagerAdapter.notifyDataSetChanged()
                            }
                        }.start()
                    }
                })

            // The score text view is initially set to 0/10 on screen
            scoreTextView.setText("Score: 0/10")

            // "scoreViewModel" object is responsible for sharing data between the host activity (QuizActivity) and its fragments
            // The shared data here is of a string data type
            // When the user press the submit button in the quiz game and if the user answer is correct then a string of "Correct" is being send from
            // fragment to this activity through the help of ViewModel ("DataViewModel")
            // This activity then increases the score number and send the score back to ViewModel
            // Now in case the user rotates the screen, this activity gets destroyed and then the else loop below will be called
            // Because in the if loop, we have already saved the current score value in the ViewModel so the score data will be retrieved from ViewModel
            // The retained value is stored in the score and the TextView is set back to the retained score data
            dataViewModel.selectedData.observe(
                this,
                androidx.lifecycle.Observer { receivedValue: String ->
                    if (receivedValue == "Correct") {
                        score++
                        scoreTextView.setText("Score: ${score}/10")
                        dataViewModel.setData("${score}")
                    }
                    else {
                        score = receivedValue.toInt()
                        scoreTextView.setText("Score: ${receivedValue}/10")
                    }
                })

            // On the last question of the game, there is a "Finish Quiz" button which when pressed sends an intent to the "QuizResultActivity"
            // Below function will send the user details through bundle and the ViewModel ("DataViewModel" class) which is being collected in the fragment
            // The fragment will then send that data to the "QuizResultActivity"
            // Below "SetPlayerData" function is setting up communication between this host activity and the fragment
            viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == 9) {
//                    viewModel.setData("${score}")
                        var playerDetails = QuizResultDetail()
                        playerDetails.playerName = name
                        playerDetails.gameDifficulty = difficulty
                        playerDetails.score = score
                        dataViewModel.setPlayerData(playerDetails)
                    }
                }
            })

        }
    }

    // Below function "if" statement ensures that if the user presses the back button on the first page of the view pager then it calls finish()
    // finish() in our case will remove the "QuizActivity" from the back stack and move the user to the "MainActivity" screen
    // "else" statement ensures that the user is directed to previous page (previous question fragment in our case)
    override fun onBackPressed() {
        if(viewPager2.currentItem==0) {
            super.onBackPressed()
        } else {
            viewPager2.currentItem = viewPager2.currentItem-1
        }
    }
}