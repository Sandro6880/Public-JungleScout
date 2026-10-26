package at.htl.android_frontend.map

import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.model.PolylineOptions
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


class FetchUrl : AsyncTask<String?, Void?, String>() {
    private var route: PolylineOptions = PolylineOptions()
    protected override fun doInBackground(vararg url: String?): String {

        // For storing data from web service
        var data = ""
        try {
            // Fetching the data from web service
            data = downloadUrl(url[0].toString()).toString()
            Log.d("Background Task data", data)
        } catch (e: Exception) {
            Log.d("Background Task", e.toString())
        }
        return data
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        val parserTask = ParserTask()

        // Invokes the thread for parsing the JSON data
        parserTask.execute(result)
        route = parserTask.getPolyLineOptions()
        Log.d("ROUTE:",route.zIndex.toString())
    }
    fun getRoute(): PolylineOptions{
        return route;
    }
    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String? {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)

            // Creating an http connection to communicate with url
            urlConnection = url.openConnection() as HttpURLConnection

            // Connecting to url
            urlConnection.connect()

            // Reading data from url
            iStream = urlConnection.getInputStream()
            val br = BufferedReader(InputStreamReader(iStream))
            val sb = StringBuffer()
            var line: String? = ""
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            data = sb.toString()
            Log.d("downloadUrl", data)
            br.close()
        } catch (e: Exception) {
            Log.d("Exception", e.toString())
        } finally {
            if (iStream != null) {
                iStream.close()
            }
            if (urlConnection != null) {
                urlConnection.disconnect()
            }
        }
        return data
    }
}
