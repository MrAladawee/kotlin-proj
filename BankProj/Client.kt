import java.io.PrintWriter
import java.net.Socket

class Client(
    val host: String = "localhost",
    val port: Int = 8080) {

    fun sendRequest(request: String){
        var socket : Socket? = null
        try {
            socket = Socket(host, port)

            var pw = PrintWriter(socket.getOutputStream(), true)
            pw.println(request)

            var br = socket.getInputStream().bufferedReader()
            println("Response from server: ${br.readLine()}")

            pw.close()
            br.close()
        }
        catch (e: Exception){
            println(e.message)
        }
        finally{
            socket?.close()
        }
    }
}
