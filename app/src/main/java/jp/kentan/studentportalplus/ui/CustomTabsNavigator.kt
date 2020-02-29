package jp.kentan.studentportalplus.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import androidx.core.net.toUri
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.util.CustomTabsHelper
import jp.kentan.studentportalplus.util.buildCustomTabsIntent
import jp.kentan.studentportalplus.util.isPdf

@Navigator.Name("customTabs")
class CustomTabsNavigator(
    private val context: Context
) : Navigator<CustomTabsNavigator.Destination>() {

    override fun navigate(
        destination: Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ): NavDestination? {
        var url = requireNotNull(destination.url)

        if (url.isPdf()) {
            url = context.getString(R.string.custom_tabs_gdocs_url, url)
        }

        context.buildCustomTabsIntent().run {
            intent.`package` = CustomTabsHelper.getPackageNameToUse(context)
            launchUrl(context, url.toUri())
        }

        return null // Do not add to the back stack, managed by Chrome Custom Tabs
    }

    override fun createDestination() = Destination(this)

    override fun popBackStack() = true // Managed by Chrome Custom Tabs

    @NavDestination.ClassType(Activity::class)
    class Destination(navigator: Navigator<out NavDestination>) : NavDestination(navigator) {

        var url: String? = null
            private set

        override fun onInflate(context: Context, attrs: AttributeSet) {
            super.onInflate(context, attrs)

            context.withStyledAttributes(attrs, R.styleable.CustomTabsNavigator, 0, 0) {
                url = getString(R.styleable.CustomTabsNavigator_url)
            }
        }
    }
}
