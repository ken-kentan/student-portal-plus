package jp.kentan.studentportalplus.ui.fragment

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelStoreOwner
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.data.component.LectureCancellation
import jp.kentan.studentportalplus.data.component.LectureInformation
import jp.kentan.studentportalplus.data.component.Notice
import jp.kentan.studentportalplus.ui.adapter.LectureCancellationAdapter
import jp.kentan.studentportalplus.ui.adapter.LectureInformationAdapter
import jp.kentan.studentportalplus.ui.adapter.NoticeAdapter
import jp.kentan.studentportalplus.ui.viewmodel.DashboardViewModel
import jp.kentan.studentportalplus.ui.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_dashboard.*
import javax.inject.Inject


class DashboardFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        AndroidSupportInjection.inject(this)

        val viewModel = ViewModelProvider(activity as ViewModelStoreOwner, viewModelFactory).get(DashboardViewModel::class.java)

        val noticeAdapter = NoticeAdapter(context, object : NoticeAdapter.Listener{
            override fun onUpdateFavorite(data: Notice, isFavorite: Boolean) {
                viewModel.updateNotice(data.copy(isFavorite = isFavorite))
            }

            override fun onClick(data: Notice) {
                viewModel.updateNotice(data.copy(hasRead = true))
                //TODO start activity
            }
        })

        val lectureInfoAdapter = LectureInformationAdapter(context, object : LectureInformationAdapter.Listener{
            override fun onClick(data: LectureInformation) {
                viewModel.updateLectureInformation(data.copy(hasRead = true))
                //TODO start activity
            }
        })

        val lectureCancelAdapter =  LectureCancellationAdapter(context, object : LectureCancellationAdapter.Listener{
            override fun onClick(data: LectureCancellation) {
                viewModel.updateLectureCancellation(data.copy(hasRead = true))
                //TODO start activity
            }
        })

        val owner = activity as LifecycleOwner
        viewModel.getNotices().observe(owner, noticeAdapter)
        viewModel.getLectureInformations().observe(owner, lectureInfoAdapter)
        viewModel.getLectureCancellation().observe(owner, lectureCancelAdapter)

        bindAdapter(notice_recycler_view, noticeAdapter)
        bindAdapter(lecture_info_recycler_view, lectureInfoAdapter)
        bindAdapter(lecture_cancel_recycler_view, lectureCancelAdapter)
    }

    private fun bindAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
        view.layoutManager = LinearLayoutManager(context)
        view.adapter = adapter
        view.isNestedScrollingEnabled = false
        view.setHasFixedSize(false)
    }

    companion object {
        var instance: DashboardFragment? = null
            private set
            get() {
                if (field == null) {
                    field = DashboardFragment()
                }

                return field
            }
    }
}
