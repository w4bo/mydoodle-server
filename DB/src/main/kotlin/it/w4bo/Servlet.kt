@file:JvmName("Servlet")

package it.unibo.web

import it.w4bo.getTurni
import it.w4bo.updateDoodle
import it.w4bo.writeUser
import org.json.JSONArray
import org.json.JSONObject
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@WebServlet("/MyDoodle")
class IAMServlet : HttpServlet() {

    @Throws(ServletException::class)
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        write(response, getTurni(request.getParameter("token")).toString())
    }

    @Throws(ServletException::class)
    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        try {
            if (request.getParameter("cmd") == "update") {
                val s = request.getParameter("turni").replace("%40", "@").replace("%5B", "[").replace("%5D", "]").replace("%7B", "{").replace("%7D", "}")
                updateDoodle(JSONArray(s))
            } else {
                writeUser(request.getParameter("id"), request.getParameter("firstname"), request.getParameter("last"), "user", request.getParameter("token"))
            }
            write(response, JSONObject().toString())
        } catch (e: Exception) {
            e.printStackTrace()
            val res = JSONObject()
            res.put("err", e.message)
            write(response, res.toString())
        }
    }

    companion object {
        const val OK = 200
        /**
         * Send the result
         * @param response HTTP response object
         * @param result result
         */
        fun write(response: HttpServletResponse, result: String) {
            response.addHeader("Access-Control-Allow-Origin", "*")
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS")
            response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, X-Auth-Token")
            response.characterEncoding = "UTF-8"
            response.status = OK
            response.outputStream.print(result)
        }
    }
}