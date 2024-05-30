import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket

fun main() = singleWindowApplication {
    App()
}

@Composable
fun App() {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var loggedIn by remember { mutableStateOf(false) }
    var accountInfo by remember { mutableStateOf("") }
    var receiverAccount by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var transferMessage by remember { mutableStateOf<String?>(null) }
    var transactionHistory by remember { mutableStateOf("") }

    if (loggedIn) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Номер счета: $accountInfo")
                Button(onClick = {
                    loggedIn = false
                    accountInfo = ""
                    login = ""
                    password = ""
                    message = null
                    // Обнуляем значение transferMessage при выходе из учетной записи
                    transferMessage = null
                }) {
                    Text("Exit")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = receiverAccount,
                onValueChange = { receiverAccount = it },
                label = { Text("Receiver Account ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = transferAmount,
                onValueChange = { transferAmount = it },
                label = { Text("Transfer Amount") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val response = sendRequest("1 ${accountInfo.split(" ")[0]}, $receiverAccount, $transferAmount")
                transferMessage = response
                if (response.contains("успешно")) {
                    accountInfo = getAccountInfo(login)
                    transactionHistory = getTransactionHistory(accountInfo.split(" ")[0].toInt())
                }
            }) {
                Text("Send Transfer")
            }
            transferMessage?.let {
                Text(text = it)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Transaction History\nНомер Транзакции, Номер отправителя, Номер получателя, Сумма")
            Spacer(modifier = Modifier.height(8.dp))
            transactionHistory = getTransactionHistory(accountInfo.split(" ")[0].toInt())
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Разделяем строки и добавляем переносы на новую строку после каждой операции
                transactionHistory.split(";").forEach {
                    Text("$it\n")
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Login") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    val response = sendLoginRequest(login, password)
                    message = response.message
                    if (response.success) {
                        loggedIn = true
                        accountInfo = getAccountInfo(login)
                        transactionHistory = getTransactionHistory(accountInfo.split(" ")[0].toInt())
                    }
                }) {
                    Text("Login")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val response = sendRegisterRequest(login, password)
                    message = response.message
                    if (response.success) {
                        loggedIn = true
                        accountInfo = getAccountInfo(login)
                        transactionHistory = getTransactionHistory(accountInfo.split(" ")[0].toInt())
                    }
                }) {
                    Text("Register")
                }
            }
            if (message != null) {
                Text(text = message!!)
            }
        }
    }
}

data class LoginResponse(val success: Boolean, val message: String)

fun sendLoginRequest(login: String, password: String): LoginResponse {
    val response = sendRequest("3 $login $password")
    return parseResponse(response)
}

fun sendRegisterRequest(login: String, password: String): LoginResponse {
    val response = sendRequest("4 $login $password")
    return parseResponse(response)
}

fun getAccountInfo(login: String): String {
    val response = sendRequest("5 $login")
    return parseAccountInfoResponse(response)
}

fun getTransactionHistory(accountId: Int): String {
    val response = sendRequest("2 $accountId")
    return response
}

fun sendRequest(request: String): String {
    var response = ""
    var socket: Socket? = null
    try {
        socket = Socket("localhost", 8080)
        val pw = PrintWriter(socket.getOutputStream(), true)
        val br = BufferedReader(socket.getInputStream().reader())

        pw.println(request)
        response = br.readLine()

        pw.close()
        br.close()
    } catch (e: Exception) {
        response = "Error: ${e.message}"
    } finally {
        socket?.close()
    }
    return response
}

fun parseResponse(response: String): LoginResponse {
    val success = response.contains("успешно вошел в систему") || response.contains("успешно зарегистрирован")
    return LoginResponse(success, response)
}

fun parseAccountInfoResponse(response: String): String {
    // Ожидаем формат "Номер_счета Сумма_на_счету"
    return response
}
