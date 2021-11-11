package ca.nait.dmit.dmit2504.dmit2504projectsapanarora

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast

class QuizResultActivity : AppCompatActivity() {

    // Textview declared to show the values from the bundle received from QuizActivity
    private val playerName : TextView by lazy { findViewById(R.id.activity_quiz_result_playerNameTextView) }
    private val gameDifficulty : TextView by lazy { findViewById(R.id.activity_quiz_result_difficultyTextView) }
    private val gameScore : TextView by lazy { findViewById(R.id.activity_quiz_result_scoreTextView) }
    private val gameFeedback : TextView by lazy { findViewById(R.id.activity_quiz_result_feedbackTextView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_result)

        if(intent!=null && intent.extras!=null) {
            val bundle = intent.extras
            val name = bundle?.getString("PLAYERNAME")
            val difficulty = bundle?.getString("GAMEDIFFICULTY")
            val score = bundle?.getInt("FINALSCORE")
            playerName.setText("Player name: ${name}")
            gameDifficulty.setText("Game difficulty: ${difficulty}")
            gameScore.setText("Game score: ${score}/10")
            if (score != null) {
                if(score < 5) {
                    gameFeedback.setText("Feedback: you scored less than 50%. Try playing again.")
                }
                else if (score == 5) {
                    gameFeedback.setText("Feedback: you scored 50%. Try playing again.")
                }
                else {
                    gameFeedback.setText("Feedback: you scored more than 50%. Congratulations!")
                }
            }
        }
    }

    // When user presses "PLAY AGAIN" button, an intent is sent from this activity to the MainActivity
    fun onClickHandlerQuizResultActivity(view: View) {
        val MainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(MainActivityIntent)
    }
}