package edu.cit.caones.splitshare.model

data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val role: String = "ROLE_USER"
) {
    val fullName: String get() = "$firstName $lastName"
    val initials: String get() = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
}

data class Group(
    val id: String = "",
    val name: String = "",
    val emoji: String = "👥",
    val memberCount: Int = 0,
    val totalAmount: Double = 0.0,
    val myBalance: Double = 0.0  // positive = owed to me, negative = I owe
)

data class Expense(
    val id: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val myShare: Double = 0.0,
    val paidByName: String = "",
    val paidByMe: Boolean = false,
    val category: String = "Food",
    val date: String = "",
    val hasReceipt: Boolean = false,
    val emoji: String = "🛒"
)

data class Member(
    val id: String = "",
    val name: String = "",
    val initials: String = "",
    val balance: Double = 0.0   // positive = they owe me, negative = I owe them
)

data class Settlement(
    val id: String = "",
    val groupId: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val toUserId: String = "",
    val toUserName: String = "",
    val amount: Double = 0.0,
    val date: String = ""
)

data class ActivityItem(
    val title: String,
    val subtitle: String,
    val amount: Double,
    val emoji: String,
    val isPositive: Boolean
)
