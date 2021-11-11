package ca.nait.dmit.dmit2504.dmit2504projectsapanarora

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DataViewModel : ViewModel() {

    // "mutableSelectedData" will contain the data shared between the host activity "QuizActivity" and its fragment
    // From Fragment to QuizActivity, the string value is "Correct"
    // From QuizActivity to Fragment, the string value is a number (GameScore) in a string format
    private val mutableSelectedData = MutableLiveData<String>()
    // Below statement is used to retrieve the value from this ViewModel
    val selectedData : LiveData<String> get() = mutableSelectedData
    // Below method is to set the value in the "mutableSelectedData"
    fun setData(changedValue : String) {
        mutableSelectedData.value = changedValue
    }

    // "mutableSelectedDataObject" will contain an object of type "QuizDetail"
    // This object will be send from host activity ie "QuizActivity" to its fragment
    private val mutableSelectedDataObject = MutableLiveData<QuizResultDetail>()
    // Below statement is used to retrieve the value from this ViewModel
    val selectedUserDetails : LiveData<QuizResultDetail> get() = mutableSelectedDataObject
    // Below method is to set the value in the "mutableSelectedData"
    fun setPlayerData(player : QuizResultDetail) {
        mutableSelectedDataObject.value = player
    }

}