package ca.nait.dmit.dmit2504.dmit2504projectsapanarora

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    // Variables declared for user input
    val nameEditText : EditText by lazy { findViewById(R.id.activity_main_personNameEditText) }
    val difficultyRadioGroup : RadioGroup by lazy { findViewById(R.id.activity_main_difficultyRadioGroup) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // Function to retrieve the user input for the radio button
    fun getSelectedDifficulty(checkButtonId : Int) : String {
        var difficulty : String
        when (checkButtonId) {
            R.id.activity_main_easyDifficultyRadioButton -> difficulty="easy"
            R.id.activity_main_mediumDifficultyRadioButton -> difficulty="medium"
            R.id.activity_main_hardDifficultyRadioButton -> difficulty="hard"
            else -> difficulty = ""
        }
        return difficulty
    }

    // Below function will operate when user presses the "START QUIZ" button
    fun onClickHandler(view: View) {

        // Taking the input from the user
        val name = nameEditText.text
        val difficulty = getSelectedDifficulty(difficultyRadioGroup.checkedRadioButtonId)

        // Ensuring that the user input is not blank for the name and the user has selected one of the radiobutton for difficulty selection
        if(name.isEmpty() && difficulty.isEmpty()) {
            Toast.makeText(this, "Name and Difficulty selection is required.", Toast.LENGTH_LONG).show()
        } else if(name.isEmpty()) {
            Toast.makeText(this, "Please enter the name.", Toast.LENGTH_LONG).show()
        }
        else if (difficulty.isEmpty()) {
            Toast.makeText(this, "Please select the difficulty.", Toast.LENGTH_LONG).show()
        }
        else {
            // If above conditions are met, an intent is passed to "QuizActivity" with a bundle containing the user data
            val quizActivityIntent = Intent(this, QuizActivity::class.java)
            val bundle = Bundle()
            bundle.putString("NAME","${name}")
            bundle.putString("DIFFICULTY","${difficulty}")
            quizActivityIntent.putExtras(bundle)
            startActivity(quizActivityIntent)
            nameEditText.setText("")
            difficultyRadioGroup.clearCheck()
        }
    }

}