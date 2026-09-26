package com.example.jogodavelhaia

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TicTacToeAITest {

    private val ai = TicTacToeAI(Random(42))

    private fun board(vararg cells: Char): Board =
        cells.map { if (it == 'X') Player.X else if (it == 'O') Player.O else null }

    @Test
    fun findWinner_detectsRowsColumnsAndDiagonals() {
        assertEquals(Player.X, findWinner(board('X', 'X', 'X', '.', '.', '.', '.', '.', '.'))?.first)
        assertEquals(Player.O, findWinner(board('O', '.', '.', 'O', '.', '.', 'O', '.', '.'))?.first)
        assertEquals(listOf(2, 4, 6), findWinner(board('.', '.', 'X', '.', 'X', '.', 'X', '.', '.'))?.second)
        assertNull(findWinner(board('X', 'O', 'X', 'X', 'O', 'O', 'O', 'X', 'X')))
    }

    @Test
    fun easy_alwaysPicksEmptyCell() {
        val b = board('X', 'O', 'X', 'O', '.', 'X', 'O', 'X', '.')
        repeat(50) {
            val move = ai.chooseMove(b, Player.O, Difficulty.EASY)
            assertTrue(b[move] == null)
        }
    }

    @Test
    fun medium_winsWhenPossible_andBlocksOtherwise() {
        val canWin = board('O', 'O', '.', 'X', 'X', '.', '.', '.', '.')
        assertEquals(2, ai.chooseMove(canWin, Player.O, Difficulty.MEDIUM))

        val mustBlock = board('X', 'X', '.', '.', 'O', '.', '.', '.', '.')
        assertEquals(2, ai.chooseMove(mustBlock, Player.O, Difficulty.MEDIUM))
    }

    @Test
    fun hard_neverLoses_againstEveryPossibleOpponent() {
        // Explora todas as sequências de jogadas do humano, com a IA começando ou não.
        fun explore(b: Board, turn: Player) {
            val result = findWinner(b)
            if (result != null) {
                assertNotEquals("IA perdeu no tabuleiro $b", Player.X, result.first)
                return
            }
            if (b.isFull()) return
            if (turn == Player.O) {
                val move = ai.chooseMove(b, Player.O, Difficulty.HARD)
                explore(b.toMutableList().also { it[move] = Player.O }, Player.X)
            } else {
                for (i in b.emptyCells()) {
                    explore(b.toMutableList().also { it[i] = Player.X }, Player.O)
                }
            }
        }
        explore(emptyBoard, Player.X)
        explore(emptyBoard, Player.O)
    }
}
