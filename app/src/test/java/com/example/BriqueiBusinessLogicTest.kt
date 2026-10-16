package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BriqueiBusinessLogicTest {

    @Test
    fun testFinancialCashflowBalanceCalculation() {
        val income1 = 2100.0 // Venda iPhone
        val income2 = 200.0  // Sinal reserva
        val expense1 = 2460.0 // Compra estoque
        val expense2 = 120.0  // Películas

        val totalIncome = income1 + income2
        val totalExpense = expense1 + expense2
        val netBalance = totalIncome - totalExpense

        assertEquals(2300.0, totalIncome, 0.001)
        assertEquals(2580.0, totalExpense, 0.001)
        assertEquals(-280.0, netBalance, 0.001)
    }

    @Test
    fun testStockProfitAndMarginCalculation() {
        val purchasePrice = 2400.0
        val additionalCosts = 60.0 // Capa e película
        val totalCost = purchasePrice + additionalCosts
        val announcedPrice = 2950.0

        val estimatedProfit = announcedPrice - totalCost
        val marginPercent = (estimatedProfit / totalCost) * 100.0

        assertEquals(2460.0, totalCost, 0.001)
        assertEquals(490.0, estimatedProfit, 0.001)
        assertEquals(19.918, marginPercent, 0.01)
    }

    @Test
    fun test12StepInspectionRecommendationLogic() {
        // If critical failure (e.g. iCloud or blacklist) -> ABORT
        val recommendationAbort = getInspectionRecommendation(approved = 11, attention = 0, failed = 1)
        assertEquals("ABORT", recommendationAbort)

        // If multiple attention items (e.g. battery 80% + replaced screen) -> CAUTION
        val recommendationCaution = getInspectionRecommendation(approved = 10, attention = 2, failed = 0)
        assertEquals("CAUTION", recommendationCaution)

        // If all approved -> PROCEED
        val recommendationProceed = getInspectionRecommendation(approved = 12, attention = 0, failed = 0)
        assertEquals("PROCEED", recommendationProceed)
    }

    @Test
    fun testPriceAlertMatching() {
        val alertTargetPrice = 2500.0
        val opportunityPrice = 2400.0
        val opportunityTitle = "iPhone 13 128GB Estelar"
        val alertKeyword = "iphone 13"

        val matches = opportunityPrice <= alertTargetPrice &&
                opportunityTitle.lowercase().contains(alertKeyword)

        assertTrue(matches)
    }

    private fun getInspectionRecommendation(approved: Int, attention: Int, failed: Int): String {
        return when {
            failed > 0 -> "ABORT"
            attention >= 2 -> "CAUTION"
            else -> "PROCEED"
        }
    }
}
