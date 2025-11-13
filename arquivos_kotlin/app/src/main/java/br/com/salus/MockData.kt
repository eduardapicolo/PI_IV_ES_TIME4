package br.com.salus

import androidx.compose.runtime.mutableStateListOf


data class Competitor(
    val id: String,
    val name: String,
)

data class Participant(
    val name: String,
    val currentStreak: Int
)

data class Competition(
    val id: String,
    val name: String,
    val streak: Int,
    val competitors: List<Competitor>,
    val iconId: Int,
    val durationDays: Int,
    val participants: List<Participant>
)

val mockCompetitors = listOf(
    Competitor("1", "Ana"),
    Competitor("2", "Bruno"),
    Competitor("3", "Carla"),
    Competitor("4", "Daniel"),
    Competitor("5", "Elisa")
)

fun generateMockParticipants(competitors: List<Competitor>): List<Participant> {
    return competitors.mapIndexed { index, competitor ->
        Participant(
            name = competitor.name,
            currentStreak = 3
        )
    }
}


val mockCompetitionsList = mutableStateListOf(
    Competition(
        id = "c1",
        name = "21 Dias de Foco",
        streak = 5,
        competitors = mockCompetitors.shuffled().take(3),
        iconId = 1,
        durationDays = 21,
        participants = generateMockParticipants(mockCompetitors.shuffled().take(3))
    ),
    Competition(
        id = "c2",
        name = "Desafio da Meditação",
        streak = 12,
        competitors = mockCompetitors.shuffled().take(4),
        iconId = 2,
        durationDays = 30,
        participants = generateMockParticipants(mockCompetitors.shuffled().take(4))
    ),
    Competition(
        id = "c3",
        name = "Manhãs Milagrosas",
        streak = 2,
        competitors = mockCompetitors.shuffled().take(5),
        iconId = 3,
        durationDays = 14,
        participants = generateMockParticipants(mockCompetitors.shuffled().take(5))
    )
)