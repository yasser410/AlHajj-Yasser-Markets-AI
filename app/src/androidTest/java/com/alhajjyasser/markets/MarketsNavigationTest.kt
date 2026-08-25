package com.alhajjyasser.markets

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarketsNavigationTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun primaryTabsAndRefreshActionAreReachable() {
        rule.onNodeWithText("الحاج ياسر للأسواق").assertIsDisplayed()
        rule.onNodeWithContentDescription("تحديث مباشر").assertIsDisplayed().performClick()

        rule.onNodeWithText("المصادر").performClick()
        rule.onNodeWithText("مصادر البيانات").assertIsDisplayed()
        rule.onNodeWithContentDescription("فتح Gold API").assertIsDisplayed()

        rule.onNodeWithText("الإعدادات").performClick()
        rule.onNodeWithText("شفافية البيانات").assertIsDisplayed()

        rule.onNodeWithText("الرئيسية").performClick()
        rule.onNodeWithText("لقطة السوق الحية").assertIsDisplayed()
    }
}
