package com.example.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Custom interceptor for automated API retries
    private class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var response: Response? = null
            var exception: IOException? = null
            var tryCount = 0
            
            while (tryCount < maxRetries) {
                try {
                    response = chain.proceed(request)
                    if (response.isSuccessful) {
                        return response
                    } else {
                        // Retry for transient server errors (e.g., 500, 502, 503, 504)
                        if (response.code < 500) {
                            return response // Do not retry client errors (4xx)
                        }
                    }
                } catch (e: IOException) {
                    exception = e
                }
                tryCount++
                // Wait briefly before retrying (exponential backoff simulation)
                try {
                    Thread.sleep(1000L * tryCount)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
            if (response != null) return response
            throw exception ?: IOException("Failed to execute request after $maxRetries retries")
        }
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(RetryInterceptor(maxRetries = 3))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    suspend fun fetchUrlString(url: String): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw java.io.IOException("Unexpected HTTP code: ${response.code}")
                response.body?.string() ?: ""
            }
        }
    }

    val weatherApi: WeatherApi by lazy {
        createRetrofit("https://api.openweathermap.org/data/2.5/").create(WeatherApi::class.java)
    }

    val countriesApi: CountriesApi by lazy {
        createRetrofit("https://restcountries.com/v3.1/").create(CountriesApi::class.java)
    }

    val newsApi: NewsApi by lazy {
        createRetrofit("https://newsapi.org/v2/").create(NewsApi::class.java)
    }

    val jokeApi: JokeApi by lazy {
        createRetrofit("https://v2.jokeapi.dev/").create(JokeApi::class.java)
    }

    val officialJokeApi: OfficialJokeApi by lazy {
        createRetrofit("https://official-joke-api.appspot.com/").create(OfficialJokeApi::class.java)
    }

    val currencyApi: CurrencyApi by lazy {
        createRetrofit("https://api.frankfurter.app/").create(CurrencyApi::class.java)
    }

    val translateApi: TranslateApi by lazy {
        createRetrofit("https://translate.argosopentech.com/").create(TranslateApi::class.java)
    }

    val dictionaryApi: DictionaryApi by lazy {
        createRetrofit("https://api.dictionaryapi.dev/api/v2/").create(DictionaryApi::class.java)
    }
}
