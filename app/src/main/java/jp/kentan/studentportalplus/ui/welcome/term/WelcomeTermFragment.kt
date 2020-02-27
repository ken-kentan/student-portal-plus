package jp.kentan.studentportalplus.ui.welcome.term

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.FragmentWelcomeTermBinding

class WelcomeTermFragment : Fragment(R.layout.fragment_welcome_term) {

    private val welcomeViewModel by viewModels<WelcomeTermViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FragmentWelcomeTermBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = welcomeViewModel

            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    welcomeViewModel.onWebViewReceivedError()
                }
            }
        }
    }
}
