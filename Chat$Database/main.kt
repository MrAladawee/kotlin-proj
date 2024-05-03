import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.DragData
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    var login by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    var is_login by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }

    val lazyListState: LazyListState = rememberLazyListState()

    //login and accept
    Column {

        //messages
        Box(modifier = Modifier.weight(9f)) {

            Column(modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth()) {
                Text(text)
            }

        }
        //message and send message
        Row {
            OutlinedTextField(modifier = Modifier.weight(8f), value = msg, onValueChange = { msg = it })
            Button(onClick = {
                if (is_login) {
                    text += login + ": " + msg + "\n"
                }
                else {
                    is_login = true
                    login = msg
                    text += "Добро пожаловать " + login + "\n"
                }
                msg = ""
            }) {
                if (!is_login) {
                    Text("Ввести логин")
                }
                else {
                    Text("Отправить")
                }
            }
        }
    }

}



fun pass() {

}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
