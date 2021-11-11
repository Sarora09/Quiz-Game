package ca.nait.dmit.dmit2504.dmit2504projectsapanarora.forretrofit

data class QuestionAnswer(
    var category : String,
    var type : String,
    var difficulty : String,
    var question : String,
    var correct_answer : String,
    var incorrect_answers : ArrayList<String>
) {
}