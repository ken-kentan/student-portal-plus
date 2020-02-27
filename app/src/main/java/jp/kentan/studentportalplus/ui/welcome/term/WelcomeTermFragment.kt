package jp.kentan.studentportalplus.ui.welcome.term

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.FragmentWelcomeTermBinding
import jp.kentan.studentportalplus.ui.observeEvent

class WelcomeTermFragment : Fragment(R.layout.fragment_welcome_term) {

    private val welcomeTermViewModel by viewModels<WelcomeTermViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FragmentWelcomeTermBinding.bind(view).apply {
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

        welcomeTermViewModel.navigate.observeEvent(viewLifecycleOwner) {
            findNavController().navigate(it)
        }
    }
}
