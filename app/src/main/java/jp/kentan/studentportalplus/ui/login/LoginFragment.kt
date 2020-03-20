package jp.kentan.studentportalplus.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.android.support.DaggerFragment
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.databinding.FragmentLoginBinding
import jp.kentan.studentportalplus.ui.observeEvent
import jp.kentan.studentportalplus.util.hideSoftInput
import javax.inject.Inject

class LoginFragment : DaggerFragment(R.layout.fragment_login) {

    companion object {
        const val BUNDLE_NAVIGATE_RES_ID = "NAVIGATE_RES_ID"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val loginViewModel by viewModels<LoginViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FragmentLoginBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = loginViewModel
        }

        loginViewModel.navigate.observeEvent(viewLifecycleOwner) {
            findNavController().navigate(it)
        }
        loginViewModel.navigateUp.observeEvent(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
        loginViewModel.hideSoftInput.observeEvent(viewLifecycleOwner) {
            activity?.hideSoftInput()
        }

        loginViewModel.onViewCreated(
            arguments?.getInt(BUNDLE_NAVIGATE_RES_ID)
        )
    }
}
