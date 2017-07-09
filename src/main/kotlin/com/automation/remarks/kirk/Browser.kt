package com.automation.remarks.kirk

import com.automation.remarks.kirk.core.JsExecutor
import com.automation.remarks.kirk.core.Navigable
import com.automation.remarks.kirk.core.ScreenshotContainer
import com.automation.remarks.kirk.core.SearchContext
import com.automation.remarks.kirk.ex.WrongUrlException
import org.aeonbits.owner.ConfigFactory
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver


class Browser(val driver: WebDriver = ChromeDriver()) : SearchContext, Navigable {

    constructor(driver: WebDriver = ChromeDriver(), baseUrl: String? = null) : this(driver) {
        this.baseUrl = baseUrl
    }


    private val config: Configuration = ConfigFactory.create(Configuration::class.java, System.getProperties())

    private var baseUrl: String? = null
        get() {
            if (field == null) {
                field = config.baseUrl()
            }
            return field?.removeSuffix("/")
        }

    val currentUrl: String by lazy {
        driver.currentUrl
    }

    val js: JsExecutor = JsExecutor(driver)

    override fun open(url: String) {
        if (isAbsoluteUrl(url)) {
            driver.navigate().to(url)
        } else {
            if (baseUrl == null) {
                throw WrongUrlException("Can't navigate to url [$url]. " +
                        "Please use absolute or set the base url !!!")
            }
            driver.navigate().to(baseUrl + url)
        }
    }

    private fun isAbsoluteUrl(url: String): Boolean {
        return (url.startsWith("http://") || url.startsWith("https://"))
    }

    override fun <T : Page> to(pageClass: (Browser) -> T): T {
        val page = pageClass(this)
        page.url?.let { open(it) }
        return page
    }

    override fun <T : Page> at(pageClass: (Browser) -> T, closure: T.() -> Unit) {
        val page = pageClass(this)
        page.closure()
    }

    override fun element(by: By): KElement {
        return KElement(by, driver)
    }

    override fun all(by: By): KElementCollection {
        return KElementCollection(by, driver)
    }

    fun takeScreenshot(saveTo: String = "${System.getProperty("user.dir")}/build/screen_${System.currentTimeMillis()}.png") {
        ScreenshotContainer(driver, saveTo).takeScreenshotAsFile()
    }

    override fun back(): Browser {
        driver.navigate().back()
        return this
    }

    override fun forward(): Browser {
        driver.navigate().forward()
        return this
    }

    override fun refresh(): Browser {
        driver.navigate().refresh()
        return this
    }

    override fun quit() {
        driver.quit()
    }
}