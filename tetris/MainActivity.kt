package com.example.tetris

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.view.View

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: TetrisGameView
    private lateinit var scoreTextView: TextView
    private lateinit var startButton: Button
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.gameView)
        scoreTextView = findViewById(R.id.scoreTextView)
        startButton = findViewById(R.id.startButton)

        startButton.setOnClickListener {
            if (!gameView.isGameRunning) {
                startGame()
            } else {
                pauseGame()
            }
        }

        findViewById<Button>(R.id.leftButton).setOnClickListener { gameView.moveLeft() }
        findViewById<Button>(R.id.rightButton).setOnClickListener { gameView.moveRight() }
        findViewById<Button>(R.id.rotateButton).setOnClickListener { gameView.rotate() }
        findViewById<Button>(R.id.downButton).setOnClickListener { gameView.moveDown() }
    }

    private fun startGame() {
        gameView.startGame()
        startButton.text = "暂停"
        score = 0
        updateScore()
    }

    private fun pauseGame() {
        gameView.pauseGame()
        startButton.text = "开始"
    }

    fun updateScore() {
        scoreTextView.text = "分数: $score"
    }
} 