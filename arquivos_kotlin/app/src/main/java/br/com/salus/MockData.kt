package br.com.salus

import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

// --- DATA CLASSES ---
// (Bom mantê-las aqui ou em um arquivo "models")

data class Competitor(
    val id: String,
    val name: String,
)

data class Competition(
    val id: String,
    val name: String,
    val streak: Int,
    val competitors: List<Competitor>,
    val iconId: Int
)

// --- MOCK DATA ---

// Lista de competidores "gerais"
val mockCompetitors = listOf(
    Competitor("1", "Ana"),
    Competitor("2", "Bruno"),
    Competitor("3", "Carla"),
    Competitor("4", "Daniel"),
    Competitor("5", "Elisa")
)

/**
 * Esta é a lista "viva" de competições.
 * TODO: BD - No futuro, isso será substituído por uma
 * consulta ao seu banco de dados (ex: um Flow do Room).
 */
val mockCompetitionsList = mutableStateListOf(
    Competition(
        id = "c1",
        name = "21 Dias de Foco",
        streak = 5,
        competitors = mockCompetitors.shuffled().take(3),
        iconId = 1
    ),
    Competition(
        id = "c2",
        name = "Desafio da Meditação",
        streak = 12,
        competitors = mockCompetitors.shuffled().take(4),
        iconId = 2
    ),
    Competition(
        id = "c3",
        name = "Manhãs Milagrosas",
        streak = 2,
        competitors = mockCompetitors.shuffled().take(5),
        iconId = 3
    )
)