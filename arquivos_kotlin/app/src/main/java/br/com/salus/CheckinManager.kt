package br.com.salus

import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

data class UserCheckIn(
    var lastCheckInDate: LocalDate? = null,
    var consecutiveDays: Int = 0
)

class CheckInManager(private val zoneId: ZoneId = ZoneId.systemDefault()) {

    private val userCheckIns = ConcurrentHashMap<String, UserCheckIn>()

    fun canCheckIn(userId: String): Boolean {
        val today = LocalDate.now(zoneId)
        val userData = userCheckIns[userId] ?: return true
        return userData.lastCheckInDate != today
    }

    fun checkIn(userId: String) {
        val today = LocalDate.now(zoneId)
        val userData = userCheckIns.computeIfAbsent(userId) { UserCheckIn() }

        if (!canCheckIn(userId)) {
            println("User $userId already checked in today.")
            return
        }

        val yesterday = today.minusDays(1)
        if (userData.lastCheckInDate == yesterday) {
            userData.consecutiveDays += 1
        } else {
            userData.consecutiveDays = 1
        }

        userData.lastCheckInDate = today
        println("User $userId checked in on $today. Consecutive days: ${userData.consecutiveDays}")
    }

    fun getConsecutiveDays(userId: String): Int {
        return userCheckIns[userId]?.consecutiveDays ?: 0
    }
}
//TESTE
fun main() {
    val manager = CheckInManager()

    val userId = "user1"

    // Day 1
    manager.checkIn(userId)

    // Day 2
    simulateNextDay()
    manager.checkIn(userId)

    // Day 3 - skipped
    simulateNextDay()
    println("Skipping check-in for $userId")

    // Day 4
    simulateNextDay()
    manager.checkIn(userId)

    println("Final consecutive days for $userId: ${manager.getConsecutiveDays(userId)}")
}

// Função simulando passagem de dias (apenas para demo)
var simulatedDate: LocalDate? = null
fun simulateNextDay() {
    simulatedDate = (simulatedDate ?: LocalDate.now()).plusDays(1)
    println("Simulated next day: $simulatedDate")
}

fun getTodayDate(): LocalDate {
    return if (simulatedDate != null) simulatedDate!! else LocalDate.now()}