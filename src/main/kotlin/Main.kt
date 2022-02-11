package minesweeper

import kotlin.random.Random

class Field (val rows: Int = 9,
             val columns: Int = 9,
             val mines: Int)

const val minesSymbol = "X"
const val safeCellsSymbol = "."
const val markedCellsSymbol = "*"
const val exploredCellsSymbol = "/"

enum class PlayerMoves{
    MARKED_CELL,
    MARKED_CELL_WITH_MINE,
    UNMARKED_MINE_CELL,
    MINE_EXPLORED,
    ALL_CELL_EXPLORED
}
fun main() {
    val gameSettings = Field(mines = setNumberOfMines())
    playGame(gameSettings = gameSettings)
}

fun playGame(gameSettings: Field) {
    var minesLocation: MutableList<String> = mutableListOf() // Position of the mines
    var score = 0 // Increments when mine is marked
    var winner = false
    var printBoard = true
    var firstShoot = true // First user shoot
    // the board that will be display to the user
    val userBoard = MutableList(gameSettings.rows) {
        MutableList(gameSettings.columns) { safeCellsSymbol }
    }
    var board: MutableList<MutableList<String>> = mutableListOf() // Games board

    do {
        if (printBoard) printGameBoard(gameBoard = userBoard)
        printBoard = true

        val userInput = askUserPlay()
        val userCoordinates = listOf(userInput[1].toInt() - 1, userInput[0].toInt() - 1) // Changing the user axis and minus 1 to match with array format
        val userCommand = userInput[2]

        if (firstShoot) { // In the first shoot of the user we create our field
            board = builtGameBoard(gameSettings = gameSettings, userShoot = userCoordinates)
            minesLocation = getMinesLocations(gameBoard = board)
            firstShoot = false
        }

        val moveResult = checkUserPlay(
            gameBoard = board,
            userBoard = userBoard,
            userCoordinates = userCoordinates,
            minesLocation = minesLocation,
            command = userCommand,
            gameSettings = gameSettings
        )
        when(moveResult) {
            PlayerMoves.MINE_EXPLORED -> {
                printGameBoard(gameBoard = userBoard)
                println("You stepped on a mine and failed!")
                break
            }
            PlayerMoves.MARKED_CELL_WITH_MINE -> score++
            PlayerMoves.UNMARKED_MINE_CELL -> score--
            PlayerMoves.ALL_CELL_EXPLORED -> winner = true
            else -> continue
        }
        if (score == gameSettings.mines || winner) {
            printGameBoard(gameBoard = userBoard)
            println("Congratulations! You found all the mines!")
            break
        }
    } while (true)
}

fun checkUserPlay(gameBoard: MutableList<MutableList<String>>,
                  userBoard: MutableList<MutableList<String>>,
                  userCoordinates: List<Int>,
                  minesLocation: MutableList<String>,
                  command: String,
                  gameSettings: Field
): PlayerMoves
{
    // easier way to understand the coordinates
    val x = userCoordinates[0]
    val y = userCoordinates[1]

    var result = when(command) {
        "free" -> {
            when{
                gameBoard[x][y] == safeCellsSymbol -> { // If cell is clear it opens cell automatically
                    floodFill(gameBoard = gameBoard, userBoard = userBoard, x = x, y = y, gameSettings = gameSettings)
                    PlayerMoves.MARKED_CELL
                }
                gameBoard[x][y].first().isDigit() -> { // If cell contains a hint only shows it
                    userBoard[x][y] = gameBoard[x][y]
                    PlayerMoves.MARKED_CELL
                }
                gameBoard[x][y] == minesSymbol -> { // If cell contains a mine the game ends
                    showMinesLocation(minesLocation = minesLocation, userBoard = userBoard)
                    PlayerMoves.MINE_EXPLORED
                }
                else -> PlayerMoves.MARKED_CELL
            }
        }
        "mine" -> {
            when{
                gameBoard[x][y] == minesSymbol -> {
                    if (userBoard[x][y] == safeCellsSymbol){
                        userBoard[x][y] = markedCellsSymbol
                        PlayerMoves.MARKED_CELL_WITH_MINE
                    } else {
                        userBoard[x][y] = safeCellsSymbol
                        PlayerMoves.UNMARKED_MINE_CELL
                    }
                }
                else -> {
                    if (userBoard[x][y] == safeCellsSymbol) userBoard[x][y] = markedCellsSymbol else userBoard[x][y] = safeCellsSymbol
                    PlayerMoves.MARKED_CELL
                }
            }
        }
        else -> PlayerMoves.MARKED_CELL
    }

    if (checkWinCondition(board = userBoard, gameSettings = gameSettings)) result = PlayerMoves.ALL_CELL_EXPLORED

    return result
}

fun checkWinCondition(board: MutableList<MutableList<String>>, gameSettings: Field): Boolean {
    val limit = gameSettings.rows * gameSettings.columns - gameSettings.mines
    var count = 0
    for (i in board) {
        for (j in i) {
            if (j == exploredCellsSymbol || j.first().isDigit()) count++
        }
    }
    return count == limit
}

fun floodFill(gameBoard: MutableList<MutableList<String>>, userBoard: MutableList<MutableList<String>>, x: Int, y: Int, gameSettings: Field) {
    if (x < 0 || x >= gameSettings.rows || y < 0 || y >= gameSettings.rows) return
    if (gameBoard[x][y].first().isDigit()) userBoard[x][y] = gameBoard[x][y]
    if (gameBoard[x][y] != safeCellsSymbol) return

    gameBoard[x][y] = exploredCellsSymbol
    userBoard[x][y] = exploredCellsSymbol

    floodFill(gameBoard = gameBoard, userBoard = userBoard, x = x + 1, y = y, gameSettings = gameSettings)
    floodFill(gameBoard = gameBoard, userBoard = userBoard, x = x - 1, y = y, gameSettings = gameSettings)
    floodFill(gameBoard = gameBoard, userBoard = userBoard, x = x, y = y + 1, gameSettings = gameSettings)
    floodFill(gameBoard = gameBoard, userBoard = userBoard, x = x, y = y - 1, gameSettings = gameSettings)

    floodFill(gameBoard = gameBoard, userBoard = userBoard, x = x + 1, y = y + 1, gameSettings = gameSettings)
    floodFill(gameBoard = gameBoard, userBoard = userBoard, x = x - 1, y = y - 1, gameSettings = gameSettings)
    floodFill(gameBoard = gameBoard, userBoard = userBoard, x = x - 1, y = y + 1, gameSettings = gameSettings)
    floodFill(gameBoard = gameBoard, userBoard = userBoard, x = x + 1, y = y - 1, gameSettings = gameSettings)
}

fun showMinesLocation(minesLocation: MutableList<String>, userBoard: MutableList<MutableList<String>>) {
    for (coordinates in minesLocation) {
        val (auxX, auxY) = coordinates.split(" ").map { it.toInt() }
        userBoard[auxX][auxY] = minesSymbol
    }
}

fun askUserPlay(): List<String> {
    print("Set/unset mine marks or claim a cell as free: ")
    return readLine()!!.split(" ")
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
            print(column)
        }
        print("|\n")
        c++
    }
    println("-|---------|")
}

fun builtGameBoard(gameSettings: Field, userShoot: List<Int>): MutableList<MutableList<String>> {
    // Create games board full of safe cells symbol "."
    val board = MutableList(gameSettings.rows) {
        MutableList(gameSettings.columns) { safeCellsSymbol }
    }
    /* It will create all the coordinates in the field to choose one randomly
    then that coordinate will be removed from the list */
    val possibleMinesLocation: MutableList<String> = mutableListOf()
    for (i in 0 until gameSettings.rows) {
        for (j in 0 until gameSettings.columns) {
            possibleMinesLocation.add("$i $j")
        }
    }
    // removes the option of a mine in the first shoot of user
    possibleMinesLocation.remove("${userShoot[0]} ${userShoot[1]}")

    var mines = 0
    do { // Iterates until all mines are placed
        // It gets random index from possibleMinesLocation
        val index = getMineCoordinate(limit = possibleMinesLocation.size)
        // It will take de value of possibleMinesLocation and divides it into int variables
        val coordinate = possibleMinesLocation[index].split(" ").map { it.toInt() }
        // If the coordinate its free uses it if no it keeps searching
        if (board[coordinate[0]][coordinate[1]] == safeCellsSymbol) {
            board[coordinate[0]][coordinate[1]] = minesSymbol
            possibleMinesLocation.removeAt(index) // If the coordinate is used it will be removed from options
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

fun getMineCoordinate(limit: Int) = Random.nextInt(0, limit)

fun setNumberOfMines(): Int {
    print("How many mines do you want on the field? ")
    return readLine()!!.toInt()
}
