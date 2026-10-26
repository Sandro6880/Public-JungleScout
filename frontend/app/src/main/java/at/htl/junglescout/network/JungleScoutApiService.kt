package at.htl.junglescout.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "http://localhost:8080/api/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface JungleScoutApiService {
    @GET("paths")
    suspend fun getPathProperties(): List<Path>
}

object JungleScoutApi{
    val retrofitService: JungleScoutApiService by lazy {
        retrofit.create(JungleScoutApiService::class.java)
    }
}