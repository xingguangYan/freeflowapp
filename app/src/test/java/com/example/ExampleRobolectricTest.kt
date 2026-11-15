package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.PlatformCatalog
import com.example.ui.components.analyzePlatformFit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FreeFlow", appName)
  }

  @Test
  fun `platform fit detects title overflow on xiaohongshu`() {
    val warnings = analyzePlatformFit(
      title = "这是一个超过二十个汉字长度限制的小红书标题测试示例",
      body = "正文内容",
      selectedPlatformIds = listOf("xiaohongshu", "zhihu")
    )
    assertTrue(warnings.any { it.platformName == "小红书" && it.isBlocking })
    assertTrue(PlatformCatalog.allPlatforms.isNotEmpty())
  }
}
