package jp.kentan.studentportalplus.ui.welcome.terms

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.FragmentWelcomeTermsBinding
import jp.kentan.studentportalplus.ui.observeEvent

class WelcomeTermsFragment : Fragment(R.layout.fragment_welcome_terms) {

    private val welcomeTermViewModel by viewModels<WelcomeTermsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FragmentWelcomeTermsBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = welcomeTermViewModel

            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    welcomeTermViewModel.onWebViewReceivedError()
                }
            }
        }

        welcomeTermViewModel.navigateToLogin.observeEvent(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_welcome_terms_fragment_to_login_fragment)
        }
    }
}
