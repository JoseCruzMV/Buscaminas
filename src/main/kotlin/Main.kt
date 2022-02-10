package minesweeper

import kotlin.random.Random

class Field (val rows: Int = 9,
             val columns: Int = 9,
             val mines: Int)

const val minesSymbol = "X"
const val safeCellsSymbol = "."
const val markedCellsSymbol = "*"

enum class PlayerMoves{
    MARKED_CELL,
    MARKED_CELL_WITH_NUMBER,
    MARKED_CELL_WITH_MINE,
    UNMARKED_NORMAL_CELL,
    UNMARKED_MINE_CELL
}
fun main() {
    val gameSettings = Field(mines = setNumberOfMines())
    val board = builtGameBoard(gameSettings = gameSettings)
    playGame(gameBoard = board, gameSettings = gameSettings)
}

fun playGame(gameBoard: MutableList<MutableList<String>>, gameSettings: Field) {
    val minesLocation = getMinesLocations(gameBoard = gameBoard)
    var score = 0
    var otherCells = 0
    var printBoard = true
    do {
        var keepPlaying = true
        if (printBoard) printGameBoard(gameBoard = gameBoard)
        printBoard = true
        val userCoordinates = askUserPlay()
        val moveResult = checkUserPlay(
            gameBoard = gameBoard,
            userCoordinates = userCoordinates,
            minesLocation = minesLocation
        )
        when(moveResult) {
            PlayerMoves.MARKED_CELL -> otherCells++ // If marked a safe cell
            PlayerMoves.MARKED_CELL_WITH_NUMBER -> {
                println("There is a number here!")
                printBoard = false
            }
            PlayerMoves.MARKED_CELL_WITH_MINE -> score++ // If marked a mine
            PlayerMoves.UNMARKED_NORMAL_CELL -> otherCells-- // If unmarked a safe cell
            PlayerMoves.UNMARKED_MINE_CELL -> score-- // If unmarked a correct mine
        }
        if (score == gameSettings.mines && otherCells == 0) keepPlaying = false
    } while (keepPlaying)

    printGameBoard(gameBoard = gameBoard)
    println("Congratulations! You found all the mines!")
}

fun checkUserPlay(gameBoard: MutableList<MutableList<String>>, userCoordinates: List<Int>, minesLocation: MutableList<String>): PlayerMoves {
    // easier way to understand the coordinates
    val x = userCoordinates[0]
    val y = userCoordinates[1]
    return when {
        gameBoard[x][y] == safeCellsSymbol -> { // if the cell is not a number or mine just chance the symbol
            gameBoard[x][y] = markedCellsSymbol
            PlayerMoves.MARKED_CELL
        }
        // if the chosen cell has a number
        gameBoard[x][y].first().isDigit() -> return PlayerMoves.MARKED_CELL_WITH_NUMBER
        // In case that the chosen cell has mine
        gameBoard[x][y] ==  minesSymbol -> {
            gameBoard[x][y] = markedCellsSymbol
            PlayerMoves.MARKED_CELL_WITH_MINE
        }
        // In case that the chosen cell is already marked then it will be unmarked
        gameBoard[x][y] == markedCellsSymbol -> {
            if (minesLocation.contains("$x $y")) {
                gameBoard[x][y] = minesSymbol
                PlayerMoves.UNMARKED_MINE_CELL
            } else {
                gameBoard[x][y] = safeCellsSymbol
                PlayerMoves.UNMARKED_NORMAL_CELL
            }
        }
        else -> PlayerMoves.MARKED_CELL
    }
}

fun askUserPlay(): List<Int> {
    print("Set/delete mine marks (x and y coordinates): ")
    val aux = readLine()!!.split(" ").map { it.toInt() - 1 } // Minus 1 to change them to array indexes
    return listOf(aux[1], aux[0]) // Change the position of the indexes to match with array format
}

fun getMinesLocations(gameBoard: MutableList<MutableList<String>>): MutableList<String> {
    val locations = mutableListOf<String>()
    for (i in 0 until gameBoard.size) {
        for (j in 0 until gameBoard.size) {
            if (gameBoard[i][j] == minesSymbol) locations.add("$i $j")
        }
    }
    return locations
}

fun printGameBoard(gameBoard: MutableList<MutableList<String>>) {
    println(" |123456789|")
    println("-|---------|")
    var c = 1
    for (rows in gameBoard) {
        print("$c|")
        for (column in rows) {
            if (column == minesSymbol) print(safeCellsSymbol) else print(column)
        }
        print("|\n")
        c++
    }
    println("-|---------|")
}

fun builtGameBoard(gameSettings: Field): MutableList<MutableList<String>> {
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
