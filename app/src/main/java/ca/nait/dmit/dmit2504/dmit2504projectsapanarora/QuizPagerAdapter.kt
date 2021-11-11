package ca.nait.dmit.dmit2504.dmit2504projectsapanarora

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter

// Variables declared to send the data from "FragmentStateAdapter" to its individual fragment
private const val OBJ_QUESTION = "ca.nait.dmit.dmit2504.dmit2504projectsapanarora.question"
private const val OBJ_CORRECTANSWER = "ca.nait.dmit.dmit2504.dmit2504projectsapanarora.correctanswer"
private const val OBJ_OPTIONS = "ca.nait.dmit.dmit2504.dmit2504projectsapanarora.options"
private const val OBJ_CURRENTPOSITION = "ca.nait.dmit.dmit2504.dmit2504projectsapanarora.currentposition"

// Variables created to retain the state of fragment when the fragment is recreated which in our case happens when:
// The user swipes through pages and goes back to the page which was destroyed by ViewPager2
// The user rotates the screen which causes both the fragment and the activity to be recreated
private const val  RECREATED_CORRECTOPTION = "ca.nait.dmit.dmit2504.dmit2504projectsapanarora.RECREATED_CORRECTOPTION"
private const val RECREATED_SELECTEDOPTION = "ca.nait.dmit.dmit2504.dmit2504projectsapanarora.RECREATED_SELECTEDOPTION"
private const val RECREATED_BOOLEAN = "ca.nait.dmit.dmit2504.dmit2504projectsapanarora.RECREATED_BOOLEAN"
private const val RECREATED_SUBMITBUTTONSTATUS = "ca.nait.dmit.dmit2504.dmit2504projectsapanarora.RECREATED_SUBMITBUTTONSTATUS"

// ViewPager2 uses the FragmentStateAdapter to supply it pages to be displayed in the ViewPager2
// Below class "QuizPagerAdapter" is being supplied the activity (QuizActivity) and the question answer list and it extends the FragmentStateAdapter
// The FragmentStateAdapter requires the getItemCount() function to return the number of pages (fragments) which in our case is total of 10 fragments/pages/questions
// The FragmentStateAdapter implements the createFragment() method to supply the instances of the "QuestionAnswerFragment" class (created below the "QuizPagerAdapter" class) as new pages
class QuizPagerAdapter (fa : AppCompatActivity, questionAnswerList : List<QuizDetail>) : FragmentStateAdapter(fa) {

    private val _questionAnswerList : List<QuizDetail>

    init {
        _questionAnswerList = questionAnswerList
    }

    override fun getItemCount(): Int {
        return _questionAnswerList.size
    }

    // Below method sends a bundle to "QuestionAnswerFragment" class and returns a new fragment instance (page) of "QuestionAnswerFragment"
    override fun createFragment(position: Int): Fragment {
        val fragment = QuestionAnswerFragment()
        fragment.arguments = Bundle().apply {
            putString(OBJ_QUESTION, _questionAnswerList[position].question)
            putString(OBJ_CORRECTANSWER, _questionAnswerList[position].correctAnswer)
            putStringArrayList(OBJ_OPTIONS, _questionAnswerList[position].totalOptions)
            putString(OBJ_CURRENTPOSITION, (position+1).toString())
        }
        return fragment
    }
}

// Below class is the heart of this app where everything a user is interacting with ViewPager2 (specified in the "QuizActivity") is taking place
// Summary: The user is displayed question with four options. User has to select one option and press the "Submit" button.
// If the user selected option is the correct answer then the user is displayed "Correct" on screen and the Score on the screen is increased by one
// If the user selected option is wrong answer then the user selected option is displayed with red background and the correct answer option is displayed with yellow background color. The score remains unchanged
// Now when the screen is rotated while playing the game, the host activity "QuizActivity" and fragments will be destroyed and recreated
// To retain the fragment state (user answered questions), "onSaveInstanceState()" method is used to prepare a bundle of important details
// The bundle "savedInstanceState" in the "onCreateView()" method will be used to retain the state when the fragment is recreated

class QuestionAnswerFragment : Fragment() {

    // Variable declared to receive the correct answer being sent by "QuizPagerAdapter" class through bundle
    var correctAnswer : String = ""
    // TextView to keep a track of which option the user has selected for an answer
    lateinit var selectedOption : TextView
    // Based on the user selected option, below variable will keep the value of the selected option
    var selectedOptionText : String = ""
    // Below TextView will be decided based on the bundle value received from the "QuizPagerAdapter" class for correct answer
    lateinit var correctAnswerTextView : TextView
    // An instance of "DataViewModel" class is created to share data between the fragments and the host activity "QuizActivity"
    private val dataViewModel : DataViewModel by activityViewModels()
    // Variable declared to keep the value of current fragment position in the ViewPager2. This value is received from "QuizPagerAdapter" in the bundle
    private var currentPosition : String = ""

    // Below four variables are added for maintaining the state of a fragment
    // "selectedOptionNumber" will carry an option number that the user has selected
    var selectedOptionNumber: Int = 0
    // "correctOptionNumber" will carry the option number which has correct answer in it
    var correctOptionNumber : Int = 0
    // If "checkFlag" is true - > user selected option and the correct option is same which means user has answered correctly
    // If "checkFlag" is false - > user selected option is different from the correct answer option which means the user has answered incorrectly
    // Please note that this is for individual fragment
    var checkFlag : Boolean = false
    // If "submitButtonClickedStatus" is "Yes" -> the user has pressed the "Submit" button which means the user has locked the answer
    // If "submitButtonClickedStatus" is "No" -> the user has not pressed the "Submit" button which means the user has not locked the answer
    var submitButtonClickedStatus = "No"

    // Below "onCreateView()" method returns the fragment layout view
    // The view is used along with savedInstanceState bundle to retain the fragment state if the user has rotated the screen which destroys and creates the fragment again
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_quiz_viewpager_fragment_page, container, false)

        if (savedInstanceState==null) {
            // Means the fragment is created for the first time
        }
        else {
            // The fragment is recreated
            // Below variables are to retain the values stored in the bundle in "onSaveInstanceState() method" when the fragment was being destroyed
            val received_flag = savedInstanceState.getBoolean(RECREATED_BOOLEAN)
            val received_correctOptionNumber = savedInstanceState.getInt(RECREATED_CORRECTOPTION)
            val received_selectOptionNumber = savedInstanceState.getInt(RECREATED_SELECTEDOPTION)
            val received_submitButtonStatus = savedInstanceState.getString(RECREATED_SUBMITBUTTONSTATUS)

            // Suppose the user goes back to recreated fragment and sees the state as preserved through bundle. The user leaves the page and goes back to the same page again (after recreation) but this time the state is not
            // preserved because the first time user was on recreated page the bundle worked. The "onSaveInstanceState()" will be saved with the initialized values because of which the state is not saved for the second time
            // In order to preserve the state for the lifecycle of game, the values are stored back to below variables which is used in bundle in "onSaveInstanceState()" method
            correctOptionNumber = received_correctOptionNumber
            selectedOptionNumber = received_selectOptionNumber
            checkFlag = received_flag
            submitButtonClickedStatus=received_submitButtonStatus!!
//            Toast.makeText(activity, "This fragment is recreated", Toast.LENGTH_SHORT).show()

            // Below if statement ensures that the state is retained only when:
            // User has actually answered to a question be it correct or incorrect answer. "received_submitButtonStatus" equals "Yes" ensures that the user has answered
            // Based on the user answer, the "received_correctOptionNumber" and "received_selectedOptionNumber" are updated:
            // If user answer is correct then "received_correctOptionNumber" and "received_selectedOptionNumber" have same value
            // If user answer is incorrect then "received_correctOptionNumber" value is different from "received_selectedOptionNumber" value
            if(received_correctOptionNumber>0 && received_selectOptionNumber>0 && received_submitButtonStatus=="Yes") {

                val optionOneQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_optionOneTextView)
                val optionTwoQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_optionTwoTextView)
                val optionThreeQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_optionThreeTextView)
                val optionFourQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_optionFourTextView)
                val submitButton = view.findViewById<Button>(R.id.activity_quiz_viewPager_fragment_submitButton)

                fun disableOptions() {
                    optionOneQuiz.isEnabled = false
                    optionTwoQuiz.isEnabled = false
                    optionThreeQuiz.isEnabled = false
                    optionFourQuiz.isEnabled = false
                    submitButton.isEnabled = false
                }

                fun updateCorrectOptionNumberTextView(received_correctOptionNumber : Int) {

                    when(received_correctOptionNumber) {
                        1 -> {
                            val optionOneQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_optionOneTextView)
                            optionOneQuiz.setBackgroundResource(R.drawable.activity_quiz_textview_correct_answer)
                            optionOneQuiz.setTextColor(Color.parseColor("#000000"))
                            submitButton.setText("Please swipe right.")
                            submitButton.setTextColor(Color.parseColor("#E7E6E3"))

                        }
                        2 -> {
                            val optionTwoQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_optionTwoTextView)
                            optionTwoQuiz.setBackgroundResource(R.drawable.activity_quiz_textview_correct_answer)
                            optionTwoQuiz.setTextColor(Color.parseColor("#000000"))
                            submitButton.setText("Please swipe right.")
                            submitButton.setTextColor(Color.parseColor("#E7E6E3"))

                        }
                        3 -> {
                            val optionThreeQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_optionThreeTextView)
                            optionThreeQuiz.setBackgroundResource(R.drawable.activity_quiz_textview_correct_answer)
                            optionThreeQuiz.setTextColor(Color.parseColor("#000000"))
                            submitButton.setText("Please swipe right.")
                            submitButton.setTextColor(Color.parseColor("#E7E6E3"))

                        }
                        4 -> {
                            val optionFourQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_optionFourTextView)
                            optionFourQuiz.setBackgroundResource(R.drawable.activity_quiz_textview_correct_answer)
                            optionFourQuiz.setTextColor(Color.parseColor("#000000"))
                            submitButton.setText("Please swipe right.")
                            submitButton.setTextColor(Color.parseColor("#E7E6E3"))

                        }
                    }

                }

                // If "received_flag" is true - > user selected option and the correct option is same which means user has answered correctly
                // If "received_flag" is false - > user selected option is different from the correct answer option which means the user has answered incorrectly
                if (received_flag==true) {
                    updateCorrectOptionNumberTextView(received_correctOptionNumber)
                    disableOptions()
                }
                else if (received_flag==false){
                    updateCorrectOptionNumberTextView(received_correctOptionNumber)
                    when(received_selectOptionNumber) {
                        1 -> {
                            optionOneQuiz.setBackgroundResource(R.drawable.activity_quiz_textview_incorrect_answer)
                            optionOneQuiz.setTextColor(Color.parseColor("#000000"))

                        }
                        2 -> {
                            optionTwoQuiz.setBackgroundResource(R.drawable.activity_quiz_textview_incorrect_answer)
                            optionTwoQuiz.setTextColor(Color.parseColor("#000000"))

                        }
                        3 -> {
                            optionThreeQuiz.setBackgroundResource(R.drawable.activity_quiz_textview_incorrect_answer)
                            optionThreeQuiz.setTextColor(Color.parseColor("#000000"))

                        }
                        4 -> {
                            optionFourQuiz.setBackgroundResource(R.drawable.activity_quiz_textview_incorrect_answer)
                            optionFourQuiz.setTextColor(Color.parseColor("#000000"))

                        }
                    }
                    disableOptions()
                }
            }
        }
        return view
    }

    // Below method contains the logic that lets the user to select an option and decides if the user answer is correct or incorrect
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Views initialized to be displayed on screen
        val questionQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_questionTextView)
        val optionOneQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_optionOneTextView)
        val optionTwoQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_optionTwoTextView)
        val optionThreeQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_optionThreeTextView)
        val optionFourQuiz = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_optionFourTextView)
        val submitButton = view.findViewById<Button>(R.id.activity_quiz_viewPager_fragment_submitButton)
        val progressBar = view.findViewById<ProgressBar>(R.id.activity_quiz_viewPager_fragment_progressBar)
        val finishButtonQuiz = view.findViewById<Button>(R.id.activity_quiz_viewPager_fragment_finishQuizButton)
        // An object of QuizResultDetail class is created to receive the values from object being sent by host activity "QuizActivity" when the user is on last page/fragment/question
        val receivedPlayerDetails = QuizResultDetail()

        // arguments is used to retrieve the values from the bundle sent by "QuizPagerAdapter" class to its individual fragment
        arguments?.takeIf { it.containsKey(OBJ_QUESTION) }?.apply {
            questionQuiz.setText("${getString(OBJ_QUESTION)}")
        }

        arguments?.takeIf { it.containsKey(OBJ_CORRECTANSWER) }?.apply {

            correctAnswer = getString(OBJ_CORRECTANSWER)!!

        }

        arguments?.takeIf { it.containsKey(OBJ_OPTIONS) }?.apply {
            val optionList : ArrayList<String> = getStringArrayList(OBJ_OPTIONS) as ArrayList<String>
            optionOneQuiz.text = optionList[0]
            optionTwoQuiz.text = optionList[1]
            optionThreeQuiz.text = optionList[2]
            optionFourQuiz.text = optionList[3]

        }

        // Below method will select the correct option based on the "correctAnswer" retrieved from the bundle sent by "QuizPagerAdapter"
        // It will also save the option number to the "correctOptionNumber" which will be used to retain the state of this fragment
        fun findCorrectAnswerTextView(correctAnswer: String) {
            if (correctAnswer == optionOneQuiz.text) {
                correctAnswerTextView = view.findViewById(R.id.activity_quiz_viewPager_fragment_optionOneTextView)
                correctOptionNumber = 1
            }
            else if (correctAnswer==optionTwoQuiz.text) {
                correctAnswerTextView = view.findViewById(R.id.activity_quiz_viewPager_fragment_optionTwoTextView)
                correctOptionNumber = 2
            }
            else if (correctAnswer==optionThreeQuiz.text) {
                correctAnswerTextView = view.findViewById(R.id.activity_quiz_viewPager_fragment_optionThreeTextView)
                correctOptionNumber = 3
            }
            else {
                correctAnswerTextView=view.findViewById(R.id.activity_quiz_viewPager_fragment_optionFourTextView)
                correctOptionNumber = 4
            }
        }

        // The correctAnswer retrieved from "QuizPagerAdapter" bundle is send to "findCorrectAnswerTextView()" method
        findCorrectAnswerTextView(correctAnswer)


        // Below method will reset the TextView properties. This will be used before every time the user press an option on the screen
        fun resetTextViewProperties() {
            optionOneQuiz.setBackgroundResource(R.drawable.activity_quiz_viewpager_textview_background)
            optionOneQuiz.setTextColor(Color.parseColor("#E7E6E3"))
            optionTwoQuiz.setBackgroundResource(R.drawable.activity_quiz_viewpager_textview_background)
            optionTwoQuiz.setTextColor(Color.parseColor("#E7E6E3"))
            optionThreeQuiz.setBackgroundResource(R.drawable.activity_quiz_viewpager_textview_background)
            optionThreeQuiz.setTextColor(Color.parseColor("#E7E6E3"))
            optionFourQuiz.setBackgroundResource(R.drawable.activity_quiz_viewpager_textview_background)
            optionFourQuiz.setTextColor(Color.parseColor("#E7E6E3"))
        }

        // Below method will disable the options and submit button. It happens when the user have locked an answer option with pressing "SUBMIT" button
        fun disableOptions() {
            optionOneQuiz.isEnabled = false
            optionTwoQuiz.isEnabled = false
            optionThreeQuiz.isEnabled = false
            optionFourQuiz.isEnabled = false
            submitButton.isEnabled = false
        }

        // This variable ensures that when the user presses an option or a button on screen, an action associated with it takes place
        // selectedOption will be assigned the selected option. The text of it will be saved in selectedOptionText which be used to compared with correct answer when the user presee the SUBMIT button
        // selectedOptionNumber will be used in bundle inside "onSaveInstanceState()" method and will be used to retain the state of the fragment
        val listener = View.OnClickListener { optionView ->
            when (optionView.getId()) {
                R.id.activity_quiz_viewPager_fragment_optionOneTextView -> {
                    resetTextViewProperties()
                    optionOneQuiz.setBackgroundResource(R.drawable.activity_quiz_textview_selectedoption)
                    optionOneQuiz.setTextColor(Color.parseColor("#000000"))
                    selectedOption = view.findViewById(R.id.activity_quiz_viewPager_fragment_optionOneTextView)
                    selectedOptionText = optionOneQuiz.text.toString()
                    selectedOptionNumber = 1
                }
                R.id.activity_quiz_viewPager_fragment_optionTwoTextView -> {
                    resetTextViewProperties()
                    optionTwoQuiz.setBackgroundResource(R.drawable.activity_quiz_textview_selectedoption)
                    optionTwoQuiz.setTextColor(Color.parseColor("#000000"))
                    selectedOption = view.findViewById(R.id.activity_quiz_viewPager_fragment_optionTwoTextView)
                    selectedOptionText = optionTwoQuiz.text.toString()
                    selectedOptionNumber = 2

                }
                R.id.activity_quiz_viewPager_fragment_optionThreeTextView -> {
                    resetTextViewProperties()
                    optionThreeQuiz.setBackgroundResource(R.drawable.activity_quiz_textview_selectedoption)
                    optionThreeQuiz.setTextColor(Color.parseColor("#000000"))
                    selectedOption = view.findViewById(R.id.activity_quiz_viewPager_fragment_optionThreeTextView)
                    selectedOptionText = optionThreeQuiz.text.toString()
                    selectedOptionNumber = 3
                }
                R.id.activity_quiz_viewPager_fragment_optionFourTextView -> {
                    resetTextViewProperties()
                    optionFourQuiz.setBackgroundResource(R.drawable.activity_quiz_textview_selectedoption)
                    optionFourQuiz.setTextColor(Color.parseColor("#000000"))
                    selectedOption = view.findViewById(R.id.activity_quiz_viewPager_fragment_optionFourTextView)
                    selectedOptionText = optionFourQuiz.text.toString()
                    selectedOptionNumber = 4
                }
                R.id.activity_quiz_viewPager_fragment_submitButton -> {
                    if(selectedOptionText=="") {
                        Toast.makeText(activity, "Please select an option", Toast.LENGTH_LONG).show()
                    } else {
                        submitButtonClickedStatus="Yes"
                        if(selectedOptionText==correctAnswer) {
                            Toast.makeText(activity, "Correct answer!", Toast.LENGTH_SHORT).show()
                            submitButton.setText("Please swipe right.")
                            // When the user answer is correct, with the help of ViewModel, a string of "Correct" is send by an individual fragment to the host activity "QuizActivity"
                            // The host activity "QuizActivity" will updated the Score in the textview on the user screen
                            dataViewModel.setData("Correct")
                            disableOptions()
                            submitButton.setTextColor(Color.parseColor("#E7E6E3"))
                            checkFlag = true
                            // On the last question page, the fragment will receive an object from the host activity "QuizActivity". The score in it will be increased by one in case the user answered correctly
                            // The object values will be send in the intent as a bundle to the "QuizResultActivity"
                            if(currentPosition=="10") {
                                receivedPlayerDetails.score = receivedPlayerDetails.score + 1
                            }

                        }
                        else {
                            Toast.makeText(activity, "Wrong answer!", Toast.LENGTH_SHORT).show()
                            correctAnswerTextView.setBackgroundResource(R.drawable.activity_quiz_textview_correct_answer)
                            correctAnswerTextView.setTextColor(Color.parseColor("#000000"))
                            selectedOption.setBackgroundResource(R.drawable.activity_quiz_textview_incorrect_answer)
                            selectedOption.setTextColor(Color.parseColor("#000000"))
                            submitButton.setText("Please swipe right.")
                            disableOptions()
                            submitButton.setTextColor(Color.parseColor("#E7E6E3"))
                            checkFlag = false
                        }
                        if (currentPosition=="10") {
                            submitButton.setText("Please Review")
                        }
                    }

                }
                R.id.activity_quiz_viewPager_fragment_finishQuizButton -> {
                    finishButtonQuiz.setText("Please Wait...")
                    // A bundle with user details and the game score will be send to the "QuizResultActivity"
                    object : CountDownTimer(1500, 500) {
                        override fun onTick(millisUntilFinished: Long) {
                            // Do nothing
                        }
                        override fun onFinish() {
                            val quizResultActivityIntent = Intent(activity, QuizResultActivity::class.java)
                            val bundle = Bundle()
                            bundle.putString("PLAYERNAME", "${receivedPlayerDetails.playerName}")
                            bundle.putString("GAMEDIFFICULTY", "${receivedPlayerDetails.gameDifficulty}")
                            bundle.putInt("FINALSCORE", receivedPlayerDetails.score)
                            quizResultActivityIntent.putExtras(bundle)
                            startActivity(quizResultActivityIntent)
                        }
                    }.start()
                }
            }
        }

        // Setting up the listener to the options and buttons
        optionOneQuiz.setOnClickListener(listener)
        optionTwoQuiz.setOnClickListener(listener)
        optionThreeQuiz.setOnClickListener(listener)
        optionFourQuiz.setOnClickListener(listener)
        submitButton.setOnClickListener(listener)
        finishButtonQuiz.setOnClickListener(listener)

        // Setting up the progress bar on the screen with current fragment position
        arguments?.takeIf { it.containsKey(OBJ_CURRENTPOSITION) }?.apply {
            currentPosition = getString(OBJ_CURRENTPOSITION)!!
            val currentFragmentTextView = view.findViewById<TextView>(R.id.activity_quiz_viewPager_fragment_currentFragment)
            when(currentPosition) {
                "1" -> progressBar.setProgress(10)
                "2" -> progressBar.setProgress(20)
                "3" -> progressBar.setProgress(30)
                "4" -> progressBar.setProgress(40)
                "5" -> progressBar.setProgress(50)
                "6" -> progressBar.setProgress(60)
                "7" -> progressBar.setProgress(70)
                "8" -> progressBar.setProgress(80)
                "9" -> progressBar.setProgress(90)
                "10" -> progressBar.setProgress(100)
            }
            currentFragmentTextView.setText("${currentPosition}/10")
            if(currentPosition=="10") {
                finishButtonQuiz.visibility=View.VISIBLE
            }
        }

        // Below viewModel listener will get the score when the user is on last fragment page ie question 10
        activity?.let {
            dataViewModel.selectedUserDetails.observe(it, androidx.lifecycle.Observer { playerDetails : QuizResultDetail ->
                receivedPlayerDetails.playerName = playerDetails.playerName
                receivedPlayerDetails.gameDifficulty = playerDetails.gameDifficulty
                receivedPlayerDetails.score = playerDetails.score
            })
        }

        if (currentPosition=="10" && submitButtonClickedStatus=="Yes") {
            submitButton.setText("Please Review")
        }
    }

    // Below method creates a bundle "outState" and puts the essential values required to retain the state in it
    // When the activity is temporarily destroyed, below method gets invoked. The "onCreateView()" callback method receive the same bundle when the fragment is recreated
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(RECREATED_CORRECTOPTION, correctOptionNumber)
        outState.putInt(RECREATED_SELECTEDOPTION, selectedOptionNumber)
        outState.putBoolean(RECREATED_BOOLEAN, checkFlag)
        outState.putString(RECREATED_SUBMITBUTTONSTATUS, submitButtonClickedStatus)
    }

}