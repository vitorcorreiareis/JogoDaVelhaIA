package com.example.jogodavelhaia

enum class Player {
    X, O;

    val opponent: Player get() = if (this == X) O else X
}

enum class GameMode(val label: String) {
    VS_AI("Contra a IA"),
    PVP("2 Jogadores")
}

enum class Difficulty(val label: String) {
    EASY("Fácil"),
    MEDIUM("Médio"),
    HARD("Difícil")
}

/** Tabuleiro 3x3 representado como uma lista de 9 posições (null = vazia). */
typealias Board = List<Player?>

val emptyBoard: Board = List(9) { null }

private val winningCombinations = listOf(
    listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // linhas
    listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // colunas
    listOf(0, 4, 8), listOf(2, 4, 6) // diagonais
)

/** Retorna o vencedor e a linha vencedora, ou null se ninguém venceu ainda. */
fun findWinner(board: Board): Pair<Player, List<Int>>? {
    for (combo in winningCombinations) {
        val (a, b, c) = combo
        val value = board[a]
        if (value != null && value == board[b] && value == board[c]) {
            return value to combo
        }
    }
    return null
}

fun Board.isFull(): Boolean = none { it == null }

fun Board.emptyCells(): List<Int> = indices.filter { this[it] == null }
