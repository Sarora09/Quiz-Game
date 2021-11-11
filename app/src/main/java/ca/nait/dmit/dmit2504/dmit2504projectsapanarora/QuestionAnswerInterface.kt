package ca.nait.dmit.dmit2504.dmit2504projectsapanarora

import ca.nait.dmit.dmit2504.dmit2504projectsapanarora.forretrofit.QuestionAnswerList
import retrofit2.http.GET
import retrofit2.http.Query

interface QuestionAnswerInterface {
    @GET("api.php")
    suspend fun getAllQuestionAnswers(
        @Query("amount") amount : String,
        @Query("difficulty") difficulty:String,
        @Query("type") type: String
    ) : QuestionAnswerList
}