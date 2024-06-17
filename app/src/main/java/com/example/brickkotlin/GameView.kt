
package com.example.brickandroid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.media.MediaPlayer
import android.os.Handler
import android.view.Display
import android.view.MotionEvent
import android.view.View
import com.example.brickkotlin.Brick
import com.example.brickkotlin.GameOver
import com.example.brickkotlin.R
import com.example.brickkotlin.Velocity
import kotlin.random.Random



class GameView(context: Context) : View(context) {

    private val context: Context
    private var ballX: Float = 0f
    private var ballY: Float = 0f
    private val velocity = Velocity(25, 32)
    private val handler: Handler
    private val UPDATE_MILLIS: Long = 30
    private val runnable: Runnable
    private val textPaint = Paint()
    private val healthPaint = Paint()
    private val brickPaint = Paint()
    private val TEXT_SIZE = 120f
    private var paddleX: Float = 0f
    private var paddleY: Float = 0f
    private var oldX: Float = 0f
    private var oldPaddleX: Float = 0f
    private var points: Int = 0
    private var life: Int = 3
    private var ball: Bitmap? = null
    private var paddle: Bitmap? = null
    private var dWidth: Int = 0
    private var dHeight: Int = 0
    private var ballWidth: Int = 0
    private var ballHeight: Int = 0
    private var mpHit: MediaPlayer? = null
    private var mpMiss: MediaPlayer? = null
    private var mpBreak: MediaPlayer? = null
    private val random: Random = Random
    private val bricks: Array<Brick?> = arrayOfNulls(30)
    private var numBricks: Int = 0
    private var brokenBricks: Int = 0
    private var gameOver: Boolean = false
    private val desiredBallWidth = 100
    private val desiredPaddleWidth = 200

    init {
        this.context = context
        ball = BitmapFactory.decodeResource(resources, R.drawable.ball)
        paddle = BitmapFactory.decodeResource(resources, R.drawable.paddle)


        // Resize the ball and paddle images
        ball?.let {
            val resizedBall = Bitmap.createScaledBitmap(it, desiredBallWidth, desiredBallWidth, false)
            ball = resizedBall
        }

        paddle?.let {
            val resizedPaddle = Bitmap.createScaledBitmap(it, desiredPaddleWidth, it.height, false)
            paddle = resizedPaddle
        }


        handler = Handler()

        runnable = Runnable {
            invalidate()
        }

        mpHit = MediaPlayer.create(context, R.raw.hit)
        mpMiss = MediaPlayer.create(context, R.raw.mis)
        mpBreak = MediaPlayer.create(context, R.raw.braking)

        textPaint.color = Color.RED
        textPaint.textSize = TEXT_SIZE
        textPaint.textAlign = Paint.Align.LEFT
        healthPaint.color = Color.GREEN
        brickPaint.color = Color.argb(255, 249, 129, 0)
        val display: Display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        dWidth = size.x
        dHeight = size.y
        ballX = random.nextInt(dWidth - 50).toFloat()
        ballY = dHeight / 3f
        paddleY = (dHeight * 4) / 5f
        paddleX = (dWidth / 2 - paddle!!.width / 2).toFloat()
        ballWidth = ball!!.width
        ballHeight = ball!!.height
        createBricks()
    }

    private fun createBricks() {
        val brickWidth = dWidth / 8
        val brickHeight = dHeight / 16
        for (column in 0 until 8) {
            for (row in 0 until 3) {
                bricks[numBricks] = Brick(true, row, column, brickWidth, brickHeight)
                numBricks++
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)
        ballX += velocity.x
        ballY += velocity.y
        if (ballX >= dWidth - ball!!.width || ballX <= 0) {
            velocity.x = velocity.x * -1
        }
        if (ballY <= 0) {
            velocity.y = velocity.y * -1
        }
        if (ballY > paddleY + paddle!!.height) {
            ballX = 1 + random.nextInt(dWidth - ball!!.width - 1).toFloat()
            ballY = dHeight / 3f
            mpMiss?.start()
            velocity.x = xVelocity()
            velocity.y = 32
            life--
            if (life == 0) {
                gameOver = true
                launchGameOver()
            }
        }

        if (ballX + ball!!.width >= paddleX && ballX <= paddleX + paddle!!.width && ballY + ball!!.height >= paddleY && ballY + ball!!.height <= paddleY + paddle!!.height) {
            mpHit?.start()
            velocity.x = velocity.x + 1
            velocity.y = (velocity.y + 1) * -1
        }

        canvas.drawBitmap(ball!!, ballX, ballY, null)
        canvas.drawBitmap(paddle!!, paddleX, paddleY, null)
        for (i in 0 until numBricks) {
            if (bricks[i]?.isVisible == true) {
                canvas.drawRect(
                    bricks[i]!!.column * bricks[i]!!.width + 1f,
                    bricks[i]!!.row * bricks[i]!!.height + 1f,
                    bricks[i]!!.column * bricks[i]!!.width + bricks[i]!!.width - 1f,
                    bricks[i]!!.row * bricks[i]!!.height + bricks[i]!!.height - 1f,
                    brickPaint
                )
            }
        }

        canvas.drawText("$points", 20f, TEXT_SIZE, textPaint)
        if (life == 2) {
            healthPaint.color = Color.YELLOW
        } else if (life == 1) {
            healthPaint.color = Color.RED
        }
        canvas.drawRect(
            (dWidth - 200).toFloat(),
            30f,
            (dWidth - 200 + 60 * life).toFloat(),
            80f,
            healthPaint
        )
        for (i in 0 until numBricks) {
            if (bricks[i]?.isVisible == true) {
                if (ballX + ballWidth >= bricks[i]!!.column * bricks[i]!!.width && ballX <= bricks[i]!!.column * bricks[i]!!.width + bricks[i]!!.width && ballY <= bricks[i]!!.row * bricks[i]!!.height + bricks[i]!!.height && ballY >= bricks[i]!!.row * bricks[i]!!.height) {
                    mpBreak?.start()
                    velocity.y = (velocity.y + 1) * -1
                    bricks[i]?.setInvisible()
                    points += 10
                    brokenBricks++
                    if (brokenBricks == 24) {
                        launchGameOver()
                    }
                }
            }
        }

        if (brokenBricks == numBricks) {
            gameOver = true
        }
        if (!gameOver) {
            handler.postDelayed(runnable, UPDATE_MILLIS)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        if (touchY >= paddleY) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldX = event.x
                    oldPaddleX = paddleX
                }
                MotionEvent.ACTION_MOVE -> {
                    val shift = oldX - touchX
                    val newPaddleX = oldPaddleX - shift
                    paddleX = when {
                        newPaddleX <= 0 -> 0f
                        newPaddleX >= dWidth - paddle!!.width -> (dWidth - paddle!!.width).toFloat()
                        else -> newPaddleX
                    }
                }
            }
        }
        return true
    }

    private fun launchGameOver() {
        handler.removeCallbacksAndMessages(null)
        val intent = Intent(context, GameOver::class.java)
        intent.putExtra("points", points)
        context.startActivity(intent)
        (context as Activity).finish()
    }

    private fun xVelocity(): Int {
        val values = intArrayOf(-35, -30, -25, 25, 30, 35)
        val index = random.nextInt(6)
        return values[index]
    }
}

