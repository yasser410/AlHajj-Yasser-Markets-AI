package com.alhajjyasser.markets

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MarketsUiJvmTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mainTabsAndLiveDataActionsAreReachable() {
        composeRule.setContent { MarketsApp() }

        composeRule.onNodeWithText("الحاج ياسر للأسواق").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("تحديث مباشر").assertIsDisplayed().performClick()

        composeRule.onNodeWithText("المصادر").performClick()
        composeRule.onNodeWithText("مصادر البيانات").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("فتح Gold API").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("فتح ExchangeRate-API").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("فتح البورصة المصرية").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("فتح البنك المركزي المصري").assertIsDisplayed()

        composeRule.onNodeWithText("الإعدادات").performClick()
        composeRule.onNodeWithText("شفافية البيانات").assertIsDisplayed()

        composeRule.onNodeWithText("الرئيسية").performClick()
        composeRule.onNodeWithText("لقطة السوق الحية").assertIsDisplayed()
    }
}
