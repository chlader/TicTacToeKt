import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.input.MouseButton
import javafx.stage.Stage
import javafx.scene.paint.Color
import javafx.scene.input.MouseEvent

/*
After looking at a lot of inspiration for making the board, a lot of people had their board sizes and the size of the cells, lines
initialized in the beginning.
 */
val cellSize = 150.0
val size     = 3
val width    = size * cellSize
val height   = size * cellSize

val lineWidth = 3.0
val padding = 10.0
val backgroundColor = Color(33 / 250.0, 33 / 250.0, 33 / 250.0, 1.0)

/*
Enumerated the players and the empty board to make it easier to switch players and initialize a new game.
 */

enum class Player { Player_X, Player_O, Empty }

/*
GameState initializes the state of the game and eventually the wins and cats game (draw) are used later on to recognize
a winner or a draw
 */

enum class GameState{ Running, X_wins, O_wins, Cats }


val playerX = Player.Player_X
val playerO = Player.Player_O
val empty   = Player.Empty

val running = GameState.Running
val xWon    = GameState.X_wins
val oWon    = GameState.O_wins
val cats    = GameState.Cats

/*
Cells is the gameboard and has the javafx elements and the logic behind tic-tac-toe implemented in it. I spent some time playing with colors
so I decided to use paint rather than the normal Colors library of javafx
 */

data class Cells(var content: Player, val col: Int, val row: Int){
    fun paint(g: GraphicsContext) {
        when (content) {
            playerX -> {
                g.stroke = Color.GREEN
                g.lineWidth = lineWidth
                g.strokeLine(col * cellSize + padding, row * cellSize + padding, (col + 1) * cellSize - padding, (row + 1) * cellSize - padding)
                g.strokeLine((col + 1) * cellSize - padding, row * cellSize + padding, col * cellSize + padding, (row + 1) * cellSize - padding)
            }
            playerO -> {
                g.stroke = Color.RED
                g.lineWidth = lineWidth

                g.strokeOval(col * cellSize + padding, row * cellSize + padding, cellSize - 2 * padding, cellSize - 2 * padding)

            }
            else -> ""
        }
    }
}


/*
Game is the main class that updates the board when a move is made, switches players, and finds a winner.
 */

data class Game(val cells: List<List<Cells>>, var state: GameState, var player: Player){
    var oPattern = 0
    var xPattern = 0

    fun updateCell(col: Int, row: Int){
        if (col in 0 until size && row in 0 until size && state == running && cells[col][row].content == empty) {
            cells[col][row].content = player

            val bitPosition = row * size + col

            if (player == playerX) {
                xPattern = xPattern or (1 shl bitPosition)
            } else {
                oPattern = oPattern or (1 shl bitPosition)
            }
        }
    }

    fun switchPlayer() {
        player = if (player == playerX) playerO else playerX
    }

    fun paint(g: GraphicsContext){
        g.fill = backgroundColor
        g.fillRect(0.0 ,0.0, width, height)

        repeat(size) { j ->
            g.stroke = Color.DARKGRAY
            g.lineWidth = lineWidth

            g.strokeLine(j * cellSize, 0.0, j * cellSize, size * cellSize)
        }

        repeat(size) { i ->
            g.stroke = Color.DARKGRAY
            g.lineWidth = lineWidth

            g.strokeLine(0.0, i * cellSize, size * cellSize, i * cellSize)
        }

        cells.forEach{it.forEach{ it.paint(g) } }
    }

    fun checkHasWonOrDraw() {
        val currentPattern = if (player == playerX) xPattern else oPattern
        if (winPattern.any {it and currentPattern == it
            }) {
            state = if (player == playerX) xWon else oWon
        }
        if (cells.sumBy { it.count{it.content == empty} } == 0)
            state = cats
    }

    fun reset() {
        cells.forEach{it.forEach{it.content = empty}}
        state = running
        xPattern = 0
        oPattern = 0
    }

    /*
    Used a companion object here for a possible list of winners in a game instead of a class or function
     */

    companion object {
        val winPattern = listOf(
            0b000_000_111,
            0b000_111_000,
            0b111_000_000,

            0b001_001_001,
            0b010_010_010,
            0b100_100_100,

            0b100_010_001,
            0b001_010_100
        )
    }
}
/*
Mostly using javafx here that prints out when a game is over, the winner of that game, what happens when a mouse button
is clicked and finishes and resets the game in order for the users to play again.
*/

class App: Application() {
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Tic Tac Toe"

        val root = Group()
        val scene = Scene(root)
        primaryStage.scene = scene
        val canvas = Canvas(width, height)

        root.children += canvas

        val g = canvas.graphicsContext2D

        val game = Game(List(size) { j ->
            List(size) { i -> Cells(empty, j, i)}
        }, running, playerX).apply { paint(g) }

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED) { event ->
            if (event.button == MouseButton.PRIMARY) {
                val x = event.x
                val y = event.y

                val col = x / cellSize
                val row = y / cellSize

                game.updateCell(col.toInt(), row.toInt())
                game.paint(g)
                game.checkHasWonOrDraw()

                if (game.state == running) {
                    game.switchPlayer()
                    return@addEventHandler
                }

                val msg = when (game.state) {
                    cats -> "CATS GAME"
                    xWon -> "X WINS"
                    oWon -> "O WINS"
                    else -> "???"
                }

                Alert(Alert.AlertType.INFORMATION, msg , ButtonType.OK).showAndWait().ifPresent {
                    game.reset()
                    game.paint(g)
                }
            }
        }

        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(App::class.java, *args)
}
