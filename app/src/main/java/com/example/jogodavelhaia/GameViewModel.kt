package com.example.jogodavelhaia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Placar de um modo de jogo. No modo contra a IA, X = jogador humano e O = IA. */
data class Score(val xWins: Int = 0, val oWins: Int = 0, val draws: Int = 0)

class GameViewModel(
    private val ai: TicTacToeAI = TicTacToeAI(),
    private val aiDelayMillis: Long = 600
) : ViewModel() {

    var board by mutableStateOf(emptyBoard)
        private set
    var currentPlayer by mutableStateOf(Player.X)
        private set
    var winner by mutableStateOf<Player?>(null)
        private set
    var winningLine by mutableStateOf<List<Int>?>(null)
        private set
    var isDraw by mutableStateOf(false)
        private set
    var isAiThinking by mutableStateOf(false)
        private set

    var mode by mutableStateOf(GameMode.VS_AI)
        private set
    var difficulty by mutableStateOf(Difficulty.HARD)
        private set

    // Placar da sessão: um por modo, preservado ao reiniciar partidas e ao trocar de modo.
    var vsAiScore by mutableStateOf(Score())
        private set
    var pvpScore by mutableStateOf(Score())
        private set

    val score: Score get() = if (mode == GameMode.VS_AI) vsAiScore else pvpScore
    val isGameOver: Boolean get() = winner != null || isDraw

    /** Tabuleiro só aceita toques se a partida está em andamento e não é a vez da IA. */
    val isBoardEnabled: Boolean get() = !isGameOver && !isAiThinking

    private var startingPlayer = Player.X
    private var aiJob: Job? = null

    private val aiPlayer = Player.O

    fun onCellClick(index: Int) {
        if (!isBoardEnabled || board[index] != null) return
        play(index)
        maybeScheduleAiMove()
    }

    /** Nova partida; quem começa alterna a cada rodada. */
    fun newGame() {
        startingPlayer = startingPlayer.opponent
        resetBoard()
    }

    fun selectMode(newMode: GameMode) {
        if (newMode == mode) return
        mode = newMode
        startingPlayer = Player.X
        resetBoard()
    }

    fun selectDifficulty(newDifficulty: Difficulty) {
        if (newDifficulty == difficulty) return
        difficulty = newDifficulty
        startingPlayer = Player.X
        resetBoard()
    }

    fun resetScore() {
        if (mode == GameMode.VS_AI) vsAiScore = Score() else pvpScore = Score()
    }

    private fun resetBoard() {
        aiJob?.cancel()
        isAiThinking = false
        board = emptyBoard
        currentPlayer = startingPlayer
        winner = null
        winningLine = null
        isDraw = false
        maybeScheduleAiMove()
    }

    private fun play(index: Int) {
        board = board.toMutableList().also { it[index] = currentPlayer }
        val result = findWinner(board)
        when {
            result != null -> {
                winner = result.first
                winningLine = result.second
                addToScore(winner = result.first)
            }
            board.isFull() -> {
                isDraw = true
                addToScore(winner = null)
            }
            else -> currentPlayer = currentPlayer.opponent
        }
    }

    private fun maybeScheduleAiMove() {
        if (mode != GameMode.VS_AI || isGameOver || currentPlayer != aiPlayer) return
        isAiThinking = true
        aiJob = viewModelScope.launch {
            delay(aiDelayMillis) // pequena pausa para a jogada da IA ser perceptível
            val move = ai.chooseMove(board, aiPlayer, difficulty)
            isAiThinking = false
            play(move)
        }
    }

    private fun addToScore(winner: Player?) {
        val update: (Score) -> Score = { s ->
            when (winner) {
                Player.X -> s.copy(xWins = s.xWins + 1)
                Player.O -> s.copy(oWins = s.oWins + 1)
                null -> s.copy(draws = s.draws + 1)
            }
        }
        if (mode == GameMode.VS_AI) vsAiScore = update(vsAiScore) else pvpScore = update(pvpScore)
    }
}
