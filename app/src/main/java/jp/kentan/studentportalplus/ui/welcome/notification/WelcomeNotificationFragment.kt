package jp.kentan.studentportalplus.ui.welcome.notification

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.FragmentWelcomeNotificationBinding
import jp.kentan.studentportalplus.ui.MainActivity
import jp.kentan.studentportalplus.ui.ViewModelFactory
import jp.kentan.studentportalplus.ui.observeEvent
import javax.inject.Inject

class WelcomeNotificationFragment : Fragment(R.layout.fragment_welcome_notification) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val welcomeNotificationViewModel: WelcomeNotificationViewModel by viewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FragmentWelcomeNotificationBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = welcomeNotificationViewModel
        }

        welcomeNotificationViewModel.startMainActivity.observeEvent(viewLifecycleOwner) {
            startActivity(MainActivity.createIntent(requireContext(), shouldRefresh = true))
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}
