import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.PrintWriter
import java.io.Writer
import java.net.Socket

class Client(val host: String = "localhost", val port: Int = 8080) {

    fun Start(){
        var socket: Socket? = null
        var pw: PrintWriter? = null
        var bw: BufferedReader? = null
        try{
            socket = Socket(host, port)
            pw = PrintWriter(socket.getOutputStream(),true)
            pw.println("hello")
            bw = BufferedReader(InputStreamReader(socket.getInputStream()))
            println(bw.readLine())
        }
        catch (e: Exception){
            println("error")
        }
        finally {
            socket?.close()
            pw?.close()
            bw?.close()
        }
    }
}
