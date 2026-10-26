package at.htl.android_frontend.map

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.PolylineOptions

import org.json.JSONObject

import android.os.AsyncTask
import android.util.Log
import java.lang.Exception


class ParserTask :
    AsyncTask<String?, Int?, List<List<HashMap<String, String>>>?>() {
    // Parsing the data in non-ui thread
    private var route: PolylineOptions = PolylineOptions()
    override fun doInBackground(vararg jsonData: String?): List<List<HashMap<String, String>>>? {
        val jObject: JSONObject
        var routes: List<List<HashMap<String, String>>>? = null
        try {
            jObject = JSONObject(jsonData[0])
            Log.d("ParserTask", jsonData[0].toString())
            val parser = DataParser()
            Log.d("ParserTask", parser.toString())

            // Starts parsing data
            routes = parser.parse(jObject)
            Log.d("ParserTask", "Executing routes")
            Log.d("ParserTask", routes.toString())
        } catch (e: Exception) {
            Log.d("ParserTask", e.toString())
            e.printStackTrace()
        }
        return routes
    }

    // Executes in UI thread, after the parsing process
    override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
        var points: ArrayList<LatLng?>
        var lineOptions: PolylineOptions? = null

        // Traversing through all the routes
        for (i in result!!.indices) {
            points = ArrayList()
            lineOptions = PolylineOptions()

            // Fetching i-th route
            val path = result[i]

            // Fetching all the points in i-th route
            for (j in path.indices) {
                val point = path[j]
                val lat = point["lat"]!!.toDouble()
                val lng = point["lng"]!!.toDouble()
                val position = LatLng(lat, lng)
                points.add(position)
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points)
            Log.d("onPostExecute", "onPostExecute lineoptions decoded")
        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            route = lineOptions
        } else {
            Log.d("onPostExecute", "without Polylines drawn")
        }
    }
    fun getPolyLineOptions(): PolylineOptions{
        return route;
    }
}
