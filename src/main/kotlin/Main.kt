package minesweeper

import kotlin.random.Random

class Game (val rows: Int = 9,
            val columns: Int = 9,
            val mines: Int)

const val minesSymbol = "X"
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
            board[coordinate[0]][coordinate[1]] = minesSymbol
            mines++
        }
    } while (mines < gameSettings.mines)

    // Check for mine around each cell
    val top = gameSettings.rows - 1 // maximum value in the array
    val corners = listOf("00", "08", "80", "88")
    for (row in board.indices){
        for (column in board.indices) {
            if (board[row][column] != minesSymbol) {
                val coordinate = "$row$column"
                when {
                    corners.contains(coordinate) -> board[row][column] = checkCorners(i = row, j = column, gameBoard = board)
                    row == 0 || column == 0 || row == top || column == top -> board[row][column] = checkSides(i = row, j = column, top = top, gameBoard = board)
                    else -> board[row][column] = checkMiddles(i = row, j = column, gameBoard = board)
                }
            }
        }
    }

    return board
}

fun checkMiddles(i: Int, j: Int, gameBoard: MutableList<MutableList<String>>): String {
    var result = 0
    if (gameBoard[i - 1][j] == minesSymbol) result++
    if (gameBoard[i - 1][j + 1] == minesSymbol) result++
    if (gameBoard[i][j + 1] == minesSymbol) result++
    if (gameBoard[i + 1][j + 1] == minesSymbol) result++
    if (gameBoard[i + 1][j] == minesSymbol) result++
    if (gameBoard[i + 1][j - 1] == minesSymbol) result++
    if (gameBoard[i][j - 1] == minesSymbol) result++
    if (gameBoard[i - 1][j - 1] == minesSymbol) result++
    return if (result != 0) result.toString() else safeCellsSymbol
}

fun checkSides(i: Int, j: Int, top: Int, gameBoard: MutableList<MutableList<String>>): String {
    var result = 0
    if (i == 0) { // top side
        if (gameBoard[i][j - 1] == minesSymbol) result++
        if (gameBoard[i + 1][j - 1] == minesSymbol) result++
        if (gameBoard[i + 1][j] == minesSymbol) result++
        if (gameBoard[i + 1][j + 1] == minesSymbol) result++
        if (gameBoard[i][j + 1] == minesSymbol) result++
    }
    if (j == 0) { // left side
        if (gameBoard[i - 1][j] == minesSymbol) result++
        if (gameBoard[i - 1][j + 1] == minesSymbol) result++
        if (gameBoard[i][j + 1] == minesSymbol) result++
        if (gameBoard[i + 1][j + 1] == minesSymbol) result++
        if (gameBoard[i + 1][j] == minesSymbol) result++
    }
    if (i == top){ // bottom side
        if (gameBoard[i][j - 1] == minesSymbol) result++
        if (gameBoard[i - 1][j - 1] == minesSymbol) result++
        if (gameBoard[i - 1][j] == minesSymbol) result++
        if (gameBoard[i - 1][j + 1] == minesSymbol) result++
        if (gameBoard[i][j + 1] == minesSymbol) result++
    }
    if (j == top) { // right side
        if (gameBoard[i - 1][j] == minesSymbol) result++
        if (gameBoard[i - 1][j - 1] == minesSymbol) result++
        if (gameBoard[i][j - 1] == minesSymbol) result++
        if (gameBoard[i + 1][j - 1] == minesSymbol) result++
        if (gameBoard[i + 1][j] == minesSymbol) result++
    }
    return if (result != 0) result.toString() else safeCellsSymbol
}

fun checkCorners(i: Int, j: Int, gameBoard: MutableList<MutableList<String>>): String {
    var result = 0
    if (i == 0) {
        if (j == 0) { // corner left-top  (A)
            if (gameBoard[i][j + 1] == minesSymbol) result++
            if (gameBoard[i + 1][j + 1] == minesSymbol) result++
            if (gameBoard[i + 1][j] == minesSymbol) result++
        } else { // corner right top (C)
            if (gameBoard[i][j - 1] == minesSymbol) result++
            if (gameBoard[i + 1][j - 1] == minesSymbol) result++
            if (gameBoard[i + 1][j] == minesSymbol) result++
        }
    } else {
        if (j == 0) { // corner left bottom (B)
            if (gameBoard[i - 1][j] == minesSymbol) result++
            if (gameBoard[i - 1][j + 1] == minesSymbol) result++
            if (gameBoard[i][j + 1] == minesSymbol) result++
        } else { // corner right bottom (D)
            if (gameBoard[i - 1][j] == minesSymbol) result++
            if (gameBoard[i - 1][j - 1] == minesSymbol) result++
            if (gameBoard[i][j - 1] == minesSymbol) result++
        }
    }

    return if (result != 0) result.toString() else safeCellsSymbol
}

fun getMineCoordinate() = MutableList(2) { Random.nextInt(0, 8) }

fun setNumberOfMines(): Int {
    print("How many mines do you want on the field? ")
    return readLine()!!.toInt()
}
