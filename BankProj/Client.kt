import java.io.PrintWriter
import java.net.Socket
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class Client(
    val host: String = "localhost",
    val port: Int = 8080) {

    fun sendRequest(request: String) {
        var socket: Socket? = null
        try {
            socket = Socket(host, port)
            var pw = PrintWriter(socket.getOutputStream())
            pw.println(request)
            pw.flush()

            var br = socket.getInputStream().bufferedReader()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                println(line)
            }

            pw.close()
            br.close()
        } catch (e: Exception) {
            println(e.message)
        } finally {
            socket?.close()
        }
    }

}
