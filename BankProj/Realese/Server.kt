import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.regex.Pattern

class Database {

    // U need to write down ur parameters to link with Database
    val USER_NAME : String = "root"
    val PASSWORD : String = ""
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

    // СДЕЛАНО
    fun transfer(accountFromId: Int, accountToId: Int, amount: Double) : String {

        val accountFromExistsQuery = "SELECT COUNT(*) FROM accounts WHERE account_id = $accountFromId"
        val accountToExistsQuery = "SELECT COUNT(*) FROM accounts WHERE account_id = $accountToId"

        val resultSetFromExists = connection.prepareStatement(accountFromExistsQuery).executeQuery()
        resultSetFromExists.next()
        val accountFromExists = resultSetFromExists.getInt(1)

        val resultSetToExists = connection.prepareStatement(accountToExistsQuery).executeQuery()
        resultSetToExists.next()
        val accountToExists = resultSetToExists.getInt(1)

        if (accountFromExists == 0 || accountToExists == 0) {
            return "Ошибка: один из счетов не существует"
        } else {
            val balanceQuery = "SELECT current_amount FROM accounts WHERE account_id = $accountFromId"
            val resultSetBalance = connection.prepareStatement(balanceQuery).executeQuery()
            resultSetBalance.next()
            val balance = resultSetBalance.getDouble(1)

            if (balance < amount) {
                return "Ошибка: недостаточно средств на счете отправителя"
            } else {
                val updateFromQuery = "UPDATE accounts SET current_amount = current_amount - $amount WHERE account_id = $accountFromId"
                val updateToQuery = "UPDATE accounts SET current_amount = current_amount + $amount WHERE account_id = $accountToId"
                val insertTransactionQuery = "INSERT INTO transactions (transaction_id, from_id, to_id, amount) VALUES (?, ?, ?, ?)"

                try {
                    connection.autoCommit = false // Включаем ручное управление транзакциями
                    // Выполняем обновление баланса на счетах отправителя и получателя
                    connection.prepareStatement(updateFromQuery).executeUpdate()
                    connection.prepareStatement(updateToQuery).executeUpdate()

                    // Генерируем идентификатор транзакции
                    val transactionId = generateTransactionId()

                    // Вставляем запись о транзакции в таблицу transactions
                    val preparedStatement = connection.prepareStatement(insertTransactionQuery)
                    preparedStatement.setInt(1, transactionId)
                    preparedStatement.setInt(2, accountFromId)
                    preparedStatement.setInt(3, accountToId)
                    preparedStatement.setDouble(4, amount)
                    preparedStatement.executeUpdate()

                    connection.commit() // Фиксируем транзакцию
                    connection.autoCommit = true // Возвращаем автоматическое управление транзакциями
                    return "Перевод успешно выполнен"
                } catch (e: SQLException) {
                    connection.rollback() // Откатываем транзакцию в случае ошибки
                    return "Ошибка при выполнении перевода: ${e.message}"
                }
            }
        }
    }

    fun generateTransactionId(): Int {
        // Получаем текущее время в миллисекундах
        val currentTimeMillis = System.currentTimeMillis()
        // Возвращаем хэш от строки, состоящей из текущего времени и случайного числа
        return Math.abs((currentTimeMillis.toString() + (Math.random() * 1000).toInt().toString()).hashCode())
    }

    // СДЕЛАНО
    fun generateNewAccountNumber(): Int {
        val sql = "SELECT MAX(account_id) AS max_account FROM Accounts;"
        val preparedStatement = connection.prepareStatement(sql)
        val resultSet = preparedStatement.executeQuery()

        return if (resultSet.next()) {
            val maxAccount = resultSet.getInt("max_account")
            if (resultSet.wasNull()) {
                1000000 // Starting point for new accounts
            } else {
                maxAccount + 1
            }
        } else {
            1000000 // Starting point for new accounts
        }
    }

    // СДЕЛАНО
    fun registerNewUser(login: String, password: String) {
        try { // Generate new account number based on existing accounts
            val newAccountNumber = generateNewAccountNumber()

            // Insert new user into Clients table
            val sqlClients = "INSERT INTO Clients (login, password, account_id) VALUES (?, ?, ?);"
            val preparedStatementClients = connection.prepareStatement(sqlClients)
            preparedStatementClients.setString(1, login)
            preparedStatementClients.setString(2, password)
            preparedStatementClients.setInt(3, newAccountNumber)
            preparedStatementClients.executeUpdate()

            // Insert new account into Accounts table
            val sqlAccounts = "INSERT INTO Accounts (account_id, current_amount) VALUES (?, ?);"
            val preparedStatementAccounts = connection.prepareStatement(sqlAccounts)
            preparedStatementAccounts.setInt(1, newAccountNumber)
            preparedStatementAccounts.setDouble(2, 0.0) // Assuming the initial amount is 0
            preparedStatementAccounts.executeUpdate()

        } catch (e: SQLException) {
            println("Error executing query: ${e.message}")
        }
    }

    // СДЕЛАНО
    fun getTransactionHistory(accountNumber: Int): String {
        val stringBuilder = StringBuilder()
        try {
            val sql = "SELECT * FROM Transactions WHERE from_id = ? OR to_id = ?;"
            val preparedStatement = connection.prepareStatement(sql)
            preparedStatement.setInt(1, accountNumber)
            preparedStatement.setInt(2, accountNumber)
            val resultSet = preparedStatement.executeQuery()

            val metaData = resultSet.metaData
            val columnCount = metaData.columnCount

            // Теперь добавим строки данных
            while (resultSet.next()) {
                for (i in 2..columnCount) {
                    stringBuilder.append(" ${resultSet.getString(i)} ")
                }
                stringBuilder.append(";")
            }
        } catch (e: SQLException) {
            stringBuilder.append("Error executing query: ${e.message}")
        }
        println(stringBuilder.toString())
        return stringBuilder.toString()
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

    // СДЕЛАНО
    fun clientExists(login: String, password: String): String {
        try {
            var sql = "SELECT * FROM Clients WHERE login = ?;"
            val preparedStatement = connection.prepareStatement(sql)
            preparedStatement.setString(1, login)
            var resultSet = preparedStatement.executeQuery()

            if (resultSet.next().toString() != "false") {
                var sql = "SELECT * FROM Clients WHERE login = ? AND password = ?;"
                val preparedStatement = connection.prepareStatement(sql)
                preparedStatement.setString(1, login)
                preparedStatement.setString(2, password)
                resultSet = preparedStatement.executeQuery()

                if (resultSet.next().toString() != "false") {
                    return "1"
                } // All's good

                else {
                    return "2" // Error in password
                }

            }
            else {
                return "3" // Code 2: there is no such login in db
            }

            return resultSet.next().toString()
        } catch (e: SQLException) {
            println("Error executing query: ${e.message}")
            return "-1"
        }
    }

    // СДЕЛАНО
    fun checkAccountBalanceByLogin(login: String): Float {
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

        return balance.toFloat()
    }

    fun takeAccountId(login: String) : Int {
        try {
            val sql = "SELECT account_id FROM Clients WHERE login = ?;"

            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, login)
                val result = preparedStatement.executeQuery()
                if (result.next()) {
                    return result.getInt("account_id")
                } else {
                    return -2 // или любое другое значение, которое указывает на отсутствие аккаунта
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace() // Логгирование ошибки для отладки
            return -1
        }
    }

}

class Server(val port: Int = 8080) {

    val serverSocket = ServerSocket(port)

    fun start() {
        while (true) {
            val clientSocket = serverSocket.accept()
            Thread(ClientHandler(clientSocket)).start()
        }
    }

    inner class ClientHandler(private val clientSocket: Socket) : Runnable {
        override fun run() {
            try {
                clientSocket.use {
                    val br = it.getInputStream().bufferedReader()
                    val request = br.readLine()

                    // Обработка запроса от клиента
                    val response = processRequest(request)

                    val pw = PrintWriter(it.getOutputStream(), true)
                    pw.println(response)

                    br.close()
                    pw.close()
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }

        private fun processRequest(request: String): String {
            val database = Database()

            val patternTransfer = Pattern.compile("^1 ([0-9]+), ([0-9]+), ([0-9]+)$")
            val patternHistory = Pattern.compile("^2 ([0-9]+)$")
            val patternLogin = Pattern.compile("^3 (.+) (.+)$")
            val patternRegister = Pattern.compile("^4 (.+) (.+)$")
            val patternCheckBalance = Pattern.compile("^5 (.+)$")

            val matcherTransfer = patternTransfer.matcher(request)
            val matcherHistory = patternHistory.matcher(request)
            val matcherLogin = patternLogin.matcher(request)
            val matcherRegister = patternRegister.matcher(request)
            val matcherCheckBalance = patternCheckBalance.matcher(request)

            return when {
                matcherTransfer.find() -> {
                    val senderId = matcherTransfer.group(1).toInt()
                    val receiverId = matcherTransfer.group(2).toInt()
                    val amount = matcherTransfer.group(3).toInt()

                    // Выполнение перевода от клиента банка с senderId к клиенту с receiverId на сумму amount
                    database.transfer(senderId, receiverId, amount.toDouble())
                }
                matcherHistory.find() -> {
                    val accountId = matcherHistory.group(1).toInt()
                    database.getTransactionHistory(accountId)
                }
                matcherLogin.find() -> {
                    val login = matcherLogin.group(1)
                    val password = matcherLogin.group(2)
                    val resultCheck = database.clientExists(login, password)
                    if (resultCheck == "1") {
                        "Пользователь $login успешно вошел в систему"
                    } else if (resultCheck == "2") {
                        "Неверный пароль"
                    } else if (resultCheck == "3"){
                        "Такого пользователя нет. Вы хотите создать новый аккаунт?"
                    } else {
                        "Ошибка в запросе"
                    }
                }
                matcherRegister.find() -> {
                    val login = matcherRegister.group(1)
                    val password = matcherRegister.group(2)
                    database.registerNewUser(login, password)
                    "Пользователь $login успешно зарегистрирован"
                }
                matcherCheckBalance.find() -> {
                    val login = matcherCheckBalance.group(1)
                    val balance = database.checkAccountBalanceByLogin(login)
                    val acc_id = database.takeAccountId(login)
                    "$acc_id ($login): $balance"
                }
                else -> "Неверный формат запроса"
            }
        }
    }
}
