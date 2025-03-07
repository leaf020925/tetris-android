package com.example.tetris

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random
import android.os.Handler
import android.os.Looper

class TetrisGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val NUM_ROWS = 20
    private val NUM_COLS = 10
    private val BLOCK_SIZE = 50f
    private val board = Array(NUM_ROWS) { IntArray(NUM_COLS) { 0 } }
    private val paint = Paint()
    
    var isGameRunning = false
    private var currentPiece: TetrominoShape? = null
    private var currentRow = 0
    private var currentCol = 0
    private val handler = Handler(Looper.getMainLooper())
    private val gameLoop = object : Runnable {
        override fun run() {
            if (isGameRunning) {
                update()
                handler.postDelayed(this, 500) // 每500毫秒更新一次
            }
        }
    }

    init {
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBoard(canvas)
        drawCurrentPiece(canvas)
    }

    private fun drawBoard(canvas: Canvas) {
        for (row in 0 until NUM_ROWS) {
            for (col in 0 until NUM_COLS) {
                val left = col * BLOCK_SIZE
                val top = row * BLOCK_SIZE
                val right = left + BLOCK_SIZE
                val bottom = top + BLOCK_SIZE

                paint.color = when (board[row][col]) {
                    0 -> Color.WHITE
                    1 -> Color.CYAN
                    2 -> Color.YELLOW
                    3 -> Color.MAGENTA
                    4 -> Color.RED
                    else -> Color.BLUE
                }
                canvas.drawRect(left, top, right, bottom, paint)
                
                // 绘制网格线
                paint.color = Color.GRAY
                canvas.drawLine(left, top, right, top, paint)
                canvas.drawLine(left, top, left, bottom, paint)
            }
        }
    }

    private fun drawCurrentPiece(canvas: Canvas) {
        currentPiece?.let { piece ->
            paint.color = piece.color
            for (row in piece.shape.indices) {
                for (col in piece.shape[0].indices) {
                    if (piece.shape[row][col] == 1) {
                        val left = (currentCol + col) * BLOCK_SIZE
                        val top = (currentRow + row) * BLOCK_SIZE
                        val right = left + BLOCK_SIZE
                        val bottom = top + BLOCK_SIZE
                        canvas.drawRect(left, top, right, bottom, paint)
                    }
                }
            }
        }
    }

    fun startGame() {
        isGameRunning = true
        clearBoard()
        spawnNewPiece()
        handler.post(gameLoop)
    }

    fun pauseGame() {
        isGameRunning = false
    }

    private fun update() {
        if (!moveDown()) {
            placePiece()
            clearLines()
            spawnNewPiece()
            if (!isValidPosition(currentRow, currentCol)) {
                gameOver()
            }
        }
        invalidate()
    }

    fun moveLeft() {
        if (isValidPosition(currentRow, currentCol - 1)) {
            currentCol--
            invalidate()
        }
    }

    fun moveRight() {
        if (isValidPosition(currentRow, currentCol + 1)) {
            currentCol++
            invalidate()
        }
    }

    fun moveDown(): Boolean {
        if (isValidPosition(currentRow + 1, currentCol)) {
            currentRow++
            invalidate()
            return true
        }
        return false
    }

    fun rotate() {
        currentPiece?.let {
            val rotated = it.getRotated()
            if (isValidPosition(currentRow, currentCol, rotated)) {
                currentPiece = TetrominoShape(rotated, it.color)
                invalidate()
            }
        }
    }

    private fun isValidPosition(newRow: Int = currentRow, newCol: Int = currentCol, shape: Array<IntArray> = currentPiece?.shape ?: return false): Boolean {
        for (row in shape.indices) {
            for (col in shape[0].indices) {
                if (shape[row][col] == 1) {
                    val boardRow = newRow + row
                    val boardCol = newCol + col
                    if (boardRow < 0 || boardRow >= NUM_ROWS || 
                        boardCol < 0 || boardCol >= NUM_COLS ||
                        board[boardRow][boardCol] != 0) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun placePiece() {
        currentPiece?.let { piece ->
            for (row in piece.shape.indices) {
                for (col in piece.shape[0].indices) {
                    if (piece.shape[row][col] == 1) {
                        board[currentRow + row][currentCol + col] = 1
                    }
                }
            }
        }
    }

    private fun clearLines() {
        var linesCleared = 0
        for (row in NUM_ROWS - 1 downTo 0) {
            if (board[row].all { it != 0 }) {
                // 移动上方的行下来
                for (r in row downTo 1) {
                    board[r] = board[r - 1].clone()
                }
                // 清空最上面的行
                board[0] = IntArray(NUM_COLS) { 0 }
                linesCleared++
            }
        }
        if (linesCleared > 0) {
            // 更新分数
            (context as? MainActivity)?.updateScore()
        }
    }

    private fun spawnNewPiece() {
        currentPiece = TetrominoShape.random()
        currentRow = 0
        currentCol = NUM_COLS / 2 - currentPiece!!.shape[0].size / 2
    }

    private fun clearBoard() {
        for (row in 0 until NUM_ROWS) {
            for (col in 0 until NUM_COLS) {
                board[row][col] = 0
            }
        }
    }

    private fun gameOver() {
        isGameRunning = false
        // 可以在这里添加游戏结束的处理逻辑
    }
}

class TetrominoShape(val shape: Array<IntArray>, val color: Int) {
    companion object {
        private val SHAPES = listOf(
            // I形
            arrayOf(
                intArrayOf(1, 1, 1, 1)
            ),
            // O形
            arrayOf(
                intArrayOf(1, 1),
                intArrayOf(1, 1)
            ),
            // T形
            arrayOf(
                intArrayOf(0, 1, 0),
                intArrayOf(1, 1, 1)
            ),
            // L形
            arrayOf(
                intArrayOf(1, 0),
                intArrayOf(1, 0),
                intArrayOf(1, 1)
            ),
            // J形
            arrayOf(
                intArrayOf(0, 1),
                intArrayOf(0, 1),
                intArrayOf(1, 1)
            ),
            // S形
            arrayOf(
                intArrayOf(0, 1, 1),
                intArrayOf(1, 1, 0)
            ),
            // Z形
            arrayOf(
                intArrayOf(1, 1, 0),
                intArrayOf(0, 1, 1)
            )
        )

        private val COLORS = listOf(
            Color.CYAN,
            Color.YELLOW,
            Color.MAGENTA,
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.rgb(255, 165, 0) // 橙色
        )

        fun random(): TetrominoShape {
            val index = Random.nextInt(SHAPES.size)
            return TetrominoShape(SHAPES[index], COLORS[index])
        }
    }

    fun getRotated(): Array<IntArray> {
        val rows = shape.size
        val cols = shape[0].size
        val rotated = Array(cols) { IntArray(rows) }
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                rotated[col][rows - 1 - row] = shape[row][col]
            }
        }
        return rotated
    }
} 