import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {

}

fun main() = application {

    /*Window(onCloseRequest = ::exitApplication) {
        App()
    }*/

    var connection = Database()

    // Test-case 1:
//    connection.createTable("Users", arrayOf("id INT PRIMARY KEY", "name VARCHAR(50)", "age INT"))
//    connection.createTable("a2", arrayOf("id INT PRIMARY KEY", "name VARCHAR(50)", "age INT"))

    // Test-case 2:
//    connection.insertInto("Users", "Mark", 4)
//    connection.insertInto("Users", "Dark", 5)

    // Test-case 3:
//    connection.selectAll("Users")
    
    // Test-case 4:
//    connection.selectAll("Users")
//    connection.truncate("Users")
//    connection.selectAll("Users")
    
    // Test-case 5:
//    connection.desc("a2")
//    connection.dropTable("a2")
//    connection.desc("a2")

    // Test-case 6:
//    connection.checkUser("Users", "Mark")
}
