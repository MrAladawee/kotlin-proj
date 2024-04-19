import java.io.BufferedReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.regex.Pattern
import javax.xml.crypto.Data

class Database {

    // U need to write down ur parameters to link with Database
    val USER_NAME : String = "root"
    val PASSWORD : String = "root"
    val URL : String = "jdbc:mysql://127.0.0.1:3306/Bank"

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

    fun transfer(accountFromId: Int, accountToId: Int, amount: Double): String {

        val accountFromExistsQuery = "SELECT COUNT(*) FROM accounts WHERE account_id = $accountFromId"
        val accountToExistsQuery = "SELECT COUNT(*) FROM accounts WHERE account_id = $accountToId"

        val resultSetFromExists = connection.prepareStatement(accountFromExistsQuery).executeQuery()
        resultSetFromExists.next()
        val accountFromExists = resultSetFromExists.getInt(1)

        val resultSetToExists = connection.prepareStatement(accountToExistsQuery).executeQuery()
        resultSetToExists.next()
        val accountToExists = resultSetToExists.getInt(1)

        if (accountFromExists == 0 || accountToExists == 0) {
            return("Ошибка: один из счетов не существует")
        } else {
            val balanceQuery = "SELECT current_amount FROM accounts WHERE account_id = $accountFromId"
            val resultSetBalance = connection.prepareStatement(balanceQuery).executeQuery()
            resultSetBalance.next()
            val balance = resultSetBalance.getDouble(1)

            if (balance < amount) {
                return ("Ошибка: недостаточно средств на счете отправителя")
            } else {
                val updateFromQuery = "UPDATE accounts SET current_amount = current_amount - $amount WHERE account_id = $accountFromId"
                val updateToQuery = "UPDATE accounts SET current_amount = current_amount + $amount WHERE account_id = $accountToId"

                connection.prepareStatement(updateFromQuery).executeUpdate()
                connection.prepareStatement(updateToQuery).executeUpdate()

                return("Перевод успешно выполнен")
            }

        }
    }

    fun generateNewAccountNumber(): Int {
        val sql = "SELECT MAX(account_number) AS max_account FROM Accounts;"
        val preparedStatement = connection.prepareStatement(sql)
        val resultSet = preparedStatement.executeQuery()

        return if (resultSet.next()) {
            resultSet.getInt("max_account") + 1
        } else {
            1000000 // Starting point for new accounts
        }
    }

    fun registerNewUser(login: String, password: String) {
        try { // Generate new account number based on existing accounts
            val newAccountNumber = generateNewAccountNumber()

            val sql = "INSERT INTO Clients (login, password, account_id) VALUES (?, ?, ?);"
            val preparedStatement = connection.prepareStatement(sql)
            preparedStatement.setString(1, login)
            preparedStatement.setString(2, password)
            preparedStatement.setInt(3, newAccountNumber)
            preparedStatement.executeUpdate()

        } catch (e: SQLException) {
            println("Error executing query: ${e.message}")
        }
    }

    fun getTransactionHistory(accountNumber: Int): String {
        val result = StringBuilder()
        try {
            val sql = "SELECT * FROM Transactions WHERE from_id = ? OR to_id = ?;"
            val preparedStatement = connection.prepareStatement(sql)
            preparedStatement.setInt(1, accountNumber)
            preparedStatement.setInt(2, accountNumber)
            val resultSet = preparedStatement.executeQuery()
            var metadata = resultSet.metaData
            for (i in 1..metadata.columnCount) {
                result.append("\t${metadata.getColumnName(i)} ")
            }
            result.appendLine()
            while (resultSet.next()) {
                metadata = resultSet.metaData
                for (i in 1..metadata.columnCount) {
                    result.append("\t${resultSet.getString(i)} ")
                }
                result.appendLine()
            }
        } catch (e: SQLException) {
            result.append("Error executing query: ${e.message}")
        }
        return result.toString()
    }

    fun accountExists(accountNumber: Int): Boolean {
        try {
            val sql = "SELECT * FROM Accounts WHERE account_number = ?;"
            val preparedStatement = connection.prepareStatement(sql)
            preparedStatement.setInt(1, accountNumber)
            val resultSet = preparedStatement.executeQuery()
            return resultSet.next()
        } catch (e: SQLException) {
            println("Error executing query: ${e.message}")
            return false
        }
    }

    fun checkAccount(login: String, password: String): Boolean {
        try {
            var sql = "SELECT * FROM Clients WHERE login = ? AND password = ?;"
            val preparedStatement = connection.prepareStatement(sql)
            preparedStatement.setString(1, login)
            preparedStatement.setString(2, password)
            val resultSet = preparedStatement.executeQuery()
            return resultSet.next()
        } catch (e: SQLException) {
            println("Error executing query: ${e.message}")
            return false
        }
    }

    fun checkAccountBalanceByLogin(connection: Connection, login: String): Int {
        var balance = 0

        try {
            val sql = "SELECT a.current_amount " +
                    "FROM Clients c " +
                    "JOIN Accounts a ON c.account_id = a.account_id " +
                    "WHERE c.login = ?;"

            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, login)
                val result = preparedStatement.executeQuery()

                if (result.next()) {
                    balance = result.getInt("current_amount")
                } else {
                    println("Client with login $login not found")
                }
            }
        } catch (e: SQLException) {
            println("Error executing query: ${e.message}")
        }

        return balance
    }

}

class Server(
    val port : Int = 8080) {

    val serverSocket = ServerSocket(port)

    fun start(){
        var clientSocket : Socket? = null
        try {
            clientSocket = serverSocket.accept()
            var br = clientSocket.getInputStream().bufferedReader()
            val request = br.readLine()

            // Обработка запроса от клиента
            val response = processRequest(request)

            var pw = PrintWriter(clientSocket.getOutputStream())
            pw.println(response)
            pw.flush()

            br.close()
            pw.close()

        }
        catch (e: Exception){
            println(e.message)
        }
        finally {
            clientSocket?.close()
            serverSocket.close()
        }
    }

    private fun processRequest(request: String): String {

        val database = Database()

        val patternTransfer = Pattern.compile("^1 ([0-9]+), ([0-9]+), ([0-9]+)$")
        val patternHistory = Pattern.compile("^2 ([0-9]+)$")
        val pattern1 = Regex("^4 (.+), (.+)")

        val matcherTransfer = patternTransfer.matcher(request)
        val matcherHistory = patternHistory.matcher(request)
        val matcher1 = patternHistory.matcher(request)

        if (matcherTransfer.find()) {
            val senderId = matcherTransfer.group(1).toInt()
            val receiverId = matcherTransfer.group(2).toInt()
            val amount = matcherTransfer.group(3).toInt()

            // Выполнение перевода от клиента банка с senderId к клиенту с receiverId на сумму amount

            return database.transfer(senderId, receiverId, amount.toDouble())

        }

        else if (matcherHistory.find()) {
            val clientId = matcherHistory.group(1).toInt()

            // Получение истории запросов для клиента с clientId
            return database.getTransactionHistory(clientId)

        }

        else if (matcher1.find()) {
            val a: Int
            return "1"

        }

        else {
            return "Неверный формат запроса"
        }
    }
}
