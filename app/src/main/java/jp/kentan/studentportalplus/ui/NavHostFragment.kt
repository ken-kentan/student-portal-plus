package jp.kentan.studentportalplus.ui

import androidx.navigation.NavController
import androidx.navigation.plusAssign

@Suppress("UNUSED")
class NavHostFragment : androidx.navigation.fragment.NavHostFragment() {
    override fun onCreateNavController(navController: NavController) {
        super.onCreateNavController(navController)
        navController.navigatorProvider += CustomTabsNavigator(requireContext())
    }
}
