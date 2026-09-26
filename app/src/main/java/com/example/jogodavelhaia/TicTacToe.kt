package com.example.jogodavelhaia

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jogodavelhaia.ui.theme.CellBackground
import com.example.jogodavelhaia.ui.theme.CellBackgroundWin
import com.example.jogodavelhaia.ui.theme.CellBorder
import com.example.jogodavelhaia.ui.theme.JogoDaVelhaIATheme
import com.example.jogodavelhaia.ui.theme.PlayerOColor
import com.example.jogodavelhaia.ui.theme.PlayerXColor

@Composable
fun TicTacToeScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Jogo da Velha IA",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        ChipSelector(
            title = "Modo de jogo",
            options = GameMode.entries,
            selected = viewModel.mode,
            label = { it.label },
            onSelect = viewModel::selectMode
        )

        if (viewModel.mode == GameMode.VS_AI) {
            ChipSelector(
                title = "Dificuldade da IA",
                options = Difficulty.entries,
                selected = viewModel.difficulty,
                label = { it.label },
                onSelect = viewModel::selectDifficulty
            )
        }

        ScoreBoard(mode = viewModel.mode, score = viewModel.score)

        StatusPanel(
            mode = viewModel.mode,
            currentPlayer = viewModel.currentPlayer,
            winner = viewModel.winner,
            isDraw = viewModel.isDraw,
            isAiThinking = viewModel.isAiThinking
        )

        BoardGrid(
            board = viewModel.board,
            winningLine = viewModel.winningLine,
            enabled = viewModel.isBoardEnabled,
            onCellClick = viewModel::onCellClick
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = viewModel::newGame) {
                Text("Reiniciar partida")
            }
            OutlinedButton(onClick = viewModel::resetScore) {
                Text("Zerar placar")
            }
        }
    }
}

@Composable
private fun <T> ChipSelector(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(label(option)) }
                )
            }
        }
    }
}

@Composable
private fun ScoreBoard(mode: GameMode, score: Score) {
    val (xLabel, oLabel) = when (mode) {
        GameMode.VS_AI -> "Você (X)" to "IA (O)"
        GameMode.PVP -> "Jogador X" to "Jogador O"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreItem(label = xLabel, value = score.xWins, color = PlayerXColor)
            ScoreItem(
                label = "Empates",
                value = score.draws,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ScoreItem(label = oLabel, value = score.oWins, color = PlayerOColor)
        }
    }
}

@Composable
private fun ScoreItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontWeight = FontWeight.Bold, color = color, fontSize = 15.sp)
        Text(text = value.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatusPanel(
    mode: GameMode,
    currentPlayer: Player,
    winner: Player?,
    isDraw: Boolean,
    isAiThinking: Boolean
) {
    fun colorOf(player: Player) = if (player == Player.X) PlayerXColor else PlayerOColor

    val (text, color) = when {
        winner != null && mode == GameMode.VS_AI ->
            (if (winner == Player.X) "Você venceu!" else "A IA venceu!") to colorOf(winner)
        winner != null -> "Jogador $winner venceu!" to colorOf(winner)
        isDraw -> "Empate!" to MaterialTheme.colorScheme.onSurfaceVariant
        isAiThinking -> "IA pensando..." to PlayerOColor
        mode == GameMode.VS_AI -> "Sua vez (X)" to PlayerXColor
        else -> "Vez do jogador $currentPlayer" to colorOf(currentPlayer)
    }

    Row(
        modifier = Modifier.height(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAiThinking) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = PlayerOColor
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun BoardGrid(
    board: Board,
    winningLine: List<Int>?,
    enabled: Boolean,
    onCellClick: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until 3) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    Cell(
                        value = board[index],
                        isWinningCell = winningLine?.contains(index) == true,
                        enabled = enabled && board[index] == null,
                        onClick = { onCellClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Cell(
    value: Player?,
    isWinningCell: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "winPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isWinningCell) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val backgroundColor by infiniteTransition.animateColor(
        initialValue = CellBackground,
        targetValue = if (isWinningCell) CellBackgroundWin else CellBackground,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backgroundColor"
    )

    Box(
        modifier = Modifier
            .size(92.dp)
            .scale(if (isWinningCell) pulseScale else 1f)
            .background(
                color = if (isWinningCell) backgroundColor else CellBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .border(width = 2.dp, color = CellBorder, shape = RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value?.name ?: "",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = when (value) {
                Player.X -> PlayerXColor
                Player.O -> PlayerOColor
                null -> Color.Unspecified
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TicTacToeScreenPreview() {
    JogoDaVelhaIATheme {
        TicTacToeScreen(viewModel = GameViewModel())
    }
}
