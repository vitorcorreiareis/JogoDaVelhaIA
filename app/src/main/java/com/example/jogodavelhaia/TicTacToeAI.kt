package com.example.jogodavelhaia

import kotlin.random.Random

/**
 * Oponente controlado por IA.
 *
 * - Fácil: jogadas totalmente aleatórias.
 * - Médio: vence se puder, bloqueia se precisar; caso contrário joga aleatoriamente.
 * - Difícil: Minimax com poda alfa-beta — joga perfeitamente e nunca perde.
 */
class TicTacToeAI(private val random: Random = Random.Default) {

    fun chooseMove(board: Board, aiPlayer: Player, difficulty: Difficulty): Int {
        val empty = board.emptyCells()
        require(empty.isNotEmpty()) { "Não há jogadas disponíveis" }

        return when (difficulty) {
            Difficulty.EASY -> empty.random(random)
            Difficulty.MEDIUM -> findWinningMove(board, aiPlayer)
                ?: findWinningMove(board, aiPlayer.opponent)
                ?: empty.random(random)
            Difficulty.HARD -> bestMinimaxMove(board, aiPlayer)
        }
    }

    /** Posição que completa uma linha para [player], se existir. */
    private fun findWinningMove(board: Board, player: Player): Int? =
        board.emptyCells().firstOrNull { index ->
            findWinner(board.with(index, player))?.first == player
        }

    private fun bestMinimaxMove(board: Board, aiPlayer: Player): Int {
        var bestScore = Int.MIN_VALUE
        val bestMoves = mutableListOf<Int>()
        for (index in board.emptyCells()) {
            val score = minimax(
                board = board.with(index, aiPlayer),
                aiPlayer = aiPlayer,
                isAiTurn = false,
                depth = 1,
                alpha = Int.MIN_VALUE,
                beta = Int.MAX_VALUE
            )
            if (score > bestScore) {
                bestScore = score
                bestMoves.clear()
                bestMoves.add(index)
            } else if (score == bestScore) {
                bestMoves.add(index)
            }
        }
        // Sorteia entre jogadas igualmente ótimas para a IA não ficar previsível.
        return bestMoves.random(random)
    }

    /**
     * Pontuação do ponto de vista da IA: vitória = 10 - profundidade (prefere vencer rápido),
     * derrota = profundidade - 10 (prefere perder o mais tarde possível), empate = 0.
     */
    private fun minimax(
        board: Board,
        aiPlayer: Player,
        isAiTurn: Boolean,
        depth: Int,
        alpha: Int,
        beta: Int
    ): Int {
        findWinner(board)?.let { (winner, _) ->
            return if (winner == aiPlayer) 10 - depth else depth - 10
        }
        if (board.isFull()) return 0

        var a = alpha
        var b = beta
        val current = if (isAiTurn) aiPlayer else aiPlayer.opponent
        var best = if (isAiTurn) Int.MIN_VALUE else Int.MAX_VALUE

        for (index in board.emptyCells()) {
            val score = minimax(board.with(index, current), aiPlayer, !isAiTurn, depth + 1, a, b)
            if (isAiTurn) {
                best = maxOf(best, score)
                a = maxOf(a, best)
            } else {
                best = minOf(best, score)
                b = minOf(b, best)
            }
            if (b <= a) break
        }
        return best
    }

    private fun Board.with(index: Int, player: Player): Board =
        toMutableList().also { it[index] = player }
}
