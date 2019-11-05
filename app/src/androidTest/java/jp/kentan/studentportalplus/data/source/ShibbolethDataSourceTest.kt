package jp.kentan.studentportalplus.data.source

import androidx.test.core.app.ApplicationProvider
import org.apache.commons.lang3.RandomStringUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ShibbolethDataSourceTest {

    private companion object {
        const val NAME = "name"
        const val USERNAME = "username"
        const val PASSWORD = "password"

        const val RANDOM_STRING_LENGTH = 10
    }

    private val shibbolethDataSource =
        ShibbolethDataSource(ApplicationProvider.getApplicationContext())

    @Before
    fun setUp() {
        shibbolethDataSource.save(NAME, USERNAME, PASSWORD)
    }

    @Test
    fun getName() {
        assertEquals(NAME, shibbolethDataSource.name)
    }

    @Test
    fun getUsername() {
        assertEquals(USERNAME, shibbolethDataSource.username)
    }

    @Test
    fun getPassword() {
        assertEquals(PASSWORD, shibbolethDataSource.password)
    }

    @Test
    fun save() {
        val name = RandomStringUtils.randomAlphanumeric(RANDOM_STRING_LENGTH)
        val username = RandomStringUtils.randomAlphanumeric(RANDOM_STRING_LENGTH)
        val password = RandomStringUtils.randomAlphanumeric(RANDOM_STRING_LENGTH)

        shibbolethDataSource.save(name, username, password)

        assertEquals(name, shibbolethDataSource.name)
        assertEquals(username, shibbolethDataSource.username)
        assertEquals(password, shibbolethDataSource.password)
    }
}
