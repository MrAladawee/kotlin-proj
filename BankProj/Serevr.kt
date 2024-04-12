import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class Database {

    // U need to write down ur parameters to link with Database
    val USER_NAME : String = "root"
    val PASSWORD : String = "root"
    val URL : String = "jdbc:mysql://127.0.0.1:3306/Chat"

    val connection: Connection by lazy {
        try {
            DriverManager.getConnection(URL, USER_NAME, PASSWORD).apply {
                println("Connection to MySQL has been established.")
            }
        } catch (e: SQLException) {
            println("Error!")
            println(e.message)
            throw e
        }
    }

    fun createTable(table_name: String, values_arr: Array<String>) {
        try {
            val values = values_arr.joinToString(", ")

            val sql = "CREATE TABLE IF NOT EXISTS $table_name ($values);"
            connection.prepareStatement(sql).executeUpdate()
            println("Table \"$table_name\" has been created OR does it exist")
        } catch (e: SQLException) {
            println("Error creating table.")
            println(e.message)
        }
    }

    fun createUser(login: String, password: String, bank_account: Int) {

        try {
            val max_account: Int

            val sql = "SELECT MAX(bank_account) FROM clients;"
            val result = connection.prepareStatement(sql).executeQuery()

            max_account = result.getInt(1)

            val preparedStatement = connection.prepareStatement("INSERT INTO clients (bank_account, login, password) " +
                    "VALUES (?, ?, ?);")
            preparedStatement.setInt(1, (max_account+1))
            preparedStatement.setString(2, login)
            preparedStatement.setString(3, password)
            preparedStatement.executeUpdate()
            println("Insert completed")

        } catch (e: SQLException) {
            print("Error inserting into table OR selecting: ")
            println(e.message)
        }

    }

    fun insertInto(table_name: String, name: String, age: Int) {
        try {
            val preparedStatement = connection.prepareStatement("INSERT INTO $table_name (name, age) VALUES (?, ?);")
            preparedStatement.setString(1, name)
            preparedStatement.setInt(2, age)
            preparedStatement.executeUpdate()
            println("Insert completed")

        } catch (e: SQLException) {
            print("Error inserting into table: ")
            println(e.message)
        }
    }

    fun truncate(table_name: String) {
        try {
            val sql = "TRUNCATE TABLE $table_name;"
            connection.prepareStatement(sql).executeUpdate()
            println("Truncate was successfully")

        } catch (e: SQLException) {
            print("Truncate error: ")
            println(e.message)
        }
    }

    fun selectAll(table_name: String) {

        try {
            val sql = "SELECT * FROM $table_name;"
            val result = connection.prepareStatement(sql).executeQuery()

            var metadata = result.metaData

            for (i in 1..metadata.columnCount) {
                print("\t${metadata.getColumnName(i)} ")
            }; println()

            while (result.next()) {
                metadata = result.metaData

                for (i in 1..metadata.columnCount) {
                    print("\t${result.getString(i)} ")
                }; println()
            }

        } catch (e: SQLException) {
            println(e.message)
        }

    }

    fun desc(table_name: String) {

        try{
            val sql = "DESC $table_name;"
            val result = connection.prepareStatement(sql).executeQuery()

            var metadata = result.metaData

            for (i in 1..metadata.columnCount) {
                print("\t${metadata.getColumnName(i)} ")
            }; println()

            while (result.next()) {
                metadata = result.metaData

                for (i in 1..metadata.columnCount) {
                    print("\t${result.getString(i)} ")
                }; println()
            }

        } catch (e: SQLException) {
            println(e.message)
        }

    }

    fun dropTable(table_name: String) {

        try {
            val sql = "DROP TABLE $table_name;"
            connection.prepareStatement(sql).executeUpdate()
            println("Table \"$table_name\" has been dropped")
        } catch (e: SQLException){
            println(e.message)
        }

    }

    //

    fun checkUser(table_name: String, user_name: String){

        try {
            val sql = "SELECT * FROM $table_name WHERE name = \"$user_name\";"
            val result = connection.prepareStatement(sql).executeQuery()

            var metadata = result.metaData

            for (i in 1..metadata.columnCount) {
                print("\t${metadata.getColumnName(i)} ")
            }; println()

            while (result.next()) {
                metadata = result.metaData

                for (i in 1..metadata.columnCount) {
                    print("\t${result.getString(i)} ")
                }; println()
            }

        } catch (e: SQLException) {
            println(e.message)
        }

    }

}

class Server(val port: Int = 8080) {
    val socketServer = ServerSocket(port)
    fun Start(){
        var socketClient: Socket? = null
        var text: BufferedReader? = null
        var outputText: PrintWriter? = null
        try{
            socketClient = socketServer.accept()
            text = BufferedReader(InputStreamReader(socketClient.getInputStream()))
            println(text.readLine())
            //var outputText = BufferedWriter(OutputStreamWriter(socketClient.getOutputStream()))
            outputText = PrintWriter(socketClient.getOutputStream(), true)
            outputText.println("hello from server")

        }
        catch (e: Exception){
            println(e.message)
        }
        finally {
            text?.close()
            outputText?.close()
            socketClient?.close()
            //socketServer.close()
        }


    }
}
