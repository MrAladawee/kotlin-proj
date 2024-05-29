import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
    var showDialog by remember { mutableStateOf(false) }
    var transactionHistory by remember { mutableStateOf("") }

    if (loggedIn) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = accountInfo)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                transactionHistory = getTransactionHistory(accountInfo.split(" ")[0].toInt())
                showDialog = true
            }) {
                Text("View Transaction History")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showTransferWindow(accountInfo) }) {
                Text("Perform Transfer")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                loggedIn = false
                accountInfo = ""
                login = ""
                password = ""
                message = null
            }) {
                Text("Exit")
            }
        }

        if (showDialog) {
            Dialog(onCloseRequest = { showDialog = false }) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Transaction History")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(transactionHistory.replace("\t", "\t\t").replace("\n", "\n\n")) // Добавлены двойные отступы
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showDialog = false }) {
                        Text("Close")
                    }
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
    // Ожидаем формат "Номер_счета (Логин): Сумма_на_счету"
    return response
}

fun showTransferWindow(accountInfo: String) {
    val accountId = accountInfo.split(" ")[0].toInt()
    // Реализуйте ваше окно перевода здесь
    // Используйте sendRequest("1 senderId, receiverId, amount") для выполнения перевода
    println("Perform Transfer for account $accountId")
}
