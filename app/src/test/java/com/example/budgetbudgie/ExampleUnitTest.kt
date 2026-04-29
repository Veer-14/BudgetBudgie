package com.example.budgetbudgie

import com.example.budgetbudgie.data.Account
import com.example.budgetbudgie.data.Expense
import com.example.budgetbudgie.data.User
import org.junit.Test
import org.junit.Assert.*

class BudgetBudgieUnitTest {

    // login validation

    @Test
    fun login_emptyUsername_fails() {
        val username = ""
        val password = "password123"
        val isValid = username.isNotEmpty() && password.isNotEmpty()
        assertFalse("Empty username should fail validation", isValid)
    }

    @Test
    fun login_emptyPassword_fails() {
        val username = "narishka"
        val password = ""
        val isValid = username.isNotEmpty() && password.isNotEmpty()
        assertFalse("Empty password should fail validation", isValid)
    }

    @Test
    fun login_bothFieldsFilled_passes() {
        val username = "narishka"
        val password = "abc123"
        val isValid = username.isNotEmpty() && password.isNotEmpty()
        assertTrue("Both fields filled should pass validation", isValid)
    }

    // Register validation

    @Test
    fun register_passwordTooShort_fails() {
        val password = "abc"
        val isValid = password.length >= 6
        assertFalse("Password shorter than 6 chars should fail", isValid)
    }

    @Test
    fun register_passwordExactlyMinLength_passes() {
        val password = "abc123"
        val isValid = password.length >= 6
        assertTrue("Password of exactly 6  should pass", isValid)
    }

    @Test
    fun register_passwordLongerThanMin_passes() {
        val password = "securepassword99"
        val isValid = password.length >= 6
        assertTrue("Password longer than 6  should pass", isValid)
    }

    // User data class tests

    @Test
    fun user_dataClass_createsCorrectly() {
        val user = User(id = 1, username = "narishka", password = "abc123")
        assertEquals("narishka", user.username)
        assertEquals("abc123", user.password)
        assertEquals(1, user.id)
    }

    @Test
    fun user_defaultId_isZero() {
        val user = User(username = "testuser", password = "pass123")
        assertEquals(0, user.id)
    }

    //  Account tests

    @Test
    fun account_dataClass_createsCorrectly() {
        val account = Account(id = 1, name = "Savings", balance = 1500.0, userId = 1)
        assertEquals("Savings", account.name)
        assertEquals(1500.0, account.balance, 0.01)
        assertEquals(1, account.userId)
    }

    @Test
    fun account_emptyName_fails_validation() {
        val name = ""
        val balance = "500.0"
        val isValid = name.isNotEmpty() && balance.isNotEmpty()
        assertFalse("Empty account name should fail", isValid)
    }

    @Test
    fun account_invalidBalance_fails_parsing() {
        val balanceText = "not a number"
        val balance = balanceText.toDoubleOrNull()
        assertNull("Non numeric balance should return null", balance)
    }

    @Test
    fun account_validBalance_parses_correctly() {
        val balanceText = "2500.50"
        val balance = balanceText.toDoubleOrNull()
        assertNotNull("Valid balance string should parse", balance)
        assertEquals(2500.50, balance!!, 0.01)
    }

    //  Total balance calculation tests

    @Test
    fun totalBalance_multipleAccounts_sumsCorrectly() {
        val accounts = listOf(
            Account(name = "Savings", balance = 1000.0, userId = 1),
            Account(name = "Current", balance = 500.0, userId = 1),
            Account(name = "Emergency", balance = 250.50, userId = 1)
        )
        val total = accounts.sumOf { it.balance }
        assertEquals(1750.50, total, 0.01)
    }

    @Test
    fun totalBalance_emptyList_isZero() {
        val accounts = emptyList<Account>()
        val total = accounts.sumOf { it.balance }
        assertEquals(0.0, total, 0.01)
    }

    @Test
    fun totalBalance_singleAccount_returnsSameValue() {
        val accounts = listOf(Account(name = "Savings", balance = 999.99, userId = 1))
        val total = accounts.sumOf { it.balance }
        assertEquals(999.99, total, 0.01)
    }

    // Expense tests

    @Test
    fun expense_dataClass_createsCorrectly() {
        val expense = Expense(
            id = 1,
            category = "Food",
            amount = 25.50,
            date = "2026-04-29",
            description = "Lunch"
        )
        assertEquals("Food", expense.category)
        assertEquals(25.50, expense.amount, 0.01)
        assertEquals("2026-04-29", expense.date)
        assertNull(expense.imageUri)
    }

    @Test
    fun expense_totalSpent_calculatesCorrectly() {
        val expenses = listOf(
            Expense(category = "Food", amount = 25.50, date = "2026-04-01", description = "Lunch"),
            Expense(category = "Transport", amount = 45.00, date = "2026-04-02", description = "Uber"),
            Expense(category = "Shopping", amount = 200.00, date = "2026-04-03", description = "Shoes")
        )
        val total = expenses.sumOf { it.amount }
        assertEquals(270.50, total, 0.01)
    }

    @Test
    fun expense_emptyList_totalIsZero() {
        val expenses = emptyList<Expense>()
        val total = expenses.sumOf { it.amount }
        assertEquals(0.0, total, 0.01)
    }

    //  Forecast calculation tests

    @Test
    fun forecast_withSpending_reducesBalance() {
        val currentBalance = 2000.0
        val totalSpent = 300.0
        val days = 30
        val avgDailySpend = totalSpent / days
        val forecast = currentBalance - (avgDailySpend * 30)
        assertEquals(1700.0, forecast, 0.01)
    }

    @Test
    fun forecast_noSpending_equalsCurrentBalance() {
        val currentBalance = 1500.0
        val avgDailySpend = 0.0
        val forecast = currentBalance - (avgDailySpend * 30)
        assertEquals(1500.0, forecast, 0.01)
    }

    @Test
    fun forecast_highSpending_canGoNegative() {
        val currentBalance = 100.0
        val avgDailySpend = 50.0
        val forecast = currentBalance - (avgDailySpend * 30)
        assertTrue("High spending forecast should be negative", forecast < 0)
    }

    // Category filtering tests

    @Test
    fun expenses_filterByCategory_returnsOnlyMatchingExpenses() {
        val expenses = listOf(
            Expense(category = "Food", amount = 25.0, date = "2026-04-01", description = "Lunch"),
            Expense(category = "Transport", amount = 45.0, date = "2026-04-02", description = "Uber"),
            Expense(category = "Food", amount = 30.0, date = "2026-04-03", description = "Dinner")
        )
        val foodExpenses = expenses.filter { it.category == "Food" }
        assertEquals(2, foodExpenses.size)
    }

    @Test
    fun expenses_categoryTotal_calculatesCorrectly() {
        val expenses = listOf(
            Expense(category = "Food", amount = 25.0, date = "2026-04-01", description = "Lunch"),
            Expense(category = "Food", amount = 30.0, date = "2026-04-03", description = "Dinner")
        )
        val foodTotal = expenses.filter { it.category == "Food" }.sumOf { it.amount }
        assertEquals(55.0, foodTotal, 0.01)
    }
}