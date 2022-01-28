
import kotlin.random.Random

class Game (val rows: Int = 9,
            val columns: Int = 9,
            val mines: Int)

const val minesSymbols = "X"
const val safeCellsSymbol = "."
fun main() {
    val gameSettings = Game(mines = setNumberOfMines())
    val board = builtGameBoard(gameSettings = gameSettings)
    printGameBoard(gameBoard = board)
}

fun printGameBoard(gameBoard: MutableList<MutableList<String>>) {
    for (rows in gameBoard) {
        println(rows.joinToString(""))
    }
}

fun builtGameBoard(gameSettings: Game): MutableList<MutableList<String>> {
    // Create games board full of safe cells symbol "."
    val board = MutableList(gameSettings.rows) {
        MutableList(gameSettings.columns) { safeCellsSymbol }
    }
    var mines = 0
    do {
        // It gets a random coordinate for mine
        val coordinate = getMineCoordinate()
        // If the coordinate its free uses it if no it keeps searching
        if (board[coordinate[0]][coordinate[1]] == safeCellsSymbol) {
            board[coordinate[0]][coordinate[1]] = minesSymbols
            mines++
        }
    } while (mines < gameSettings.mines)

    return board
}

fun getMineCoordinate() = MutableList(2) { Random.nextInt(0, 8) }

fun setNumberOfMines(): Int {
    print("How many mines do you want on the field? ")
    return readLine()!!.toInt()
}
