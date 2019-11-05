package jp.kentan.studentportalplus.ui.attendcoursedetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import jp.kentan.studentportalplus.LiveDataTestUtil
import jp.kentan.studentportalplus.MainCoroutineRule
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.AttendCourseRepository
import jp.kentan.studentportalplus.data.entity.AttendCourse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AttendCourseDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val attendCourseRepository = mockk<AttendCourseRepository>()

    private lateinit var viewModel: AttendCourseDetailViewModel

    @Before
    fun setUp() {
        val id = TestData.attendCourse.id

        viewModel = AttendCourseDetailViewModel(attendCourseRepository)
        viewModel.onActivityCreate(id)

        every {
            attendCourseRepository.getObservable(id)
        } returns MutableLiveData<AttendCourse>(TestData.attendCourse)
    }

    @Test
    fun onActivityCreate_dataLoad() {
        every {
            attendCourseRepository.getObservable(TestData.attendCourse.id)
        } returns MutableLiveData(TestData.attendCourse)

        val vm = AttendCourseDetailViewModel(attendCourseRepository)
        vm.onActivityCreate(TestData.attendCourse.id)

        val attendCourse = LiveDataTestUtil.getValue(vm.attendCourse)

        assertThat(attendCourse).isEqualTo(TestData.attendCourse)

        val enabledDeleteOptionMenu = LiveDataTestUtil.getValue(vm.enabledDeleteOptionMenu)
        assertThat(enabledDeleteOptionMenu?.peekContent()).isEqualTo(Unit)
    }

    @Test
    fun onActivityCreate_dataNotFound() {
        every {
            attendCourseRepository.getObservable(TestData.attendCourse.id)
        } returns MutableLiveData(null)

        val vm = AttendCourseDetailViewModel(attendCourseRepository)
        vm.onActivityCreate(TestData.attendCourse.id)

        assertThat(LiveDataTestUtil.getValue(vm.attendCourse)).isNull()

        assertThat(LiveDataTestUtil.getValue(vm.error)?.peekContent())
            .isEqualTo(R.string.error_not_found)

        assertThat(LiveDataTestUtil.getValue(vm.finish)).isEqualTo(Unit)
    }

    @Test
    fun onEditClick_startEditActivity() {
        viewModel.onEditClick()

        val startEditActivity = LiveDataTestUtil.getValue(viewModel.startEditAttendCourseActivity)
        assertThat(startEditActivity?.peekContent()).isEqualTo(TestData.attendCourse.id)
    }

    @Test
    fun onDeleteClick_showDialog() {
        // TODO resume
        LiveDataTestUtil.getValue(viewModel.attendCourse)

        viewModel.onDeleteClick()

        val showDialog = LiveDataTestUtil.getValue(viewModel.showDeleteDialog)
        assertThat(showDialog?.peekContent()).isEqualTo(TestData.attendCourse.subject)
    }

    @Test
    fun onDeleteConfirmClick_deleteData() {
        coEvery {
            attendCourseRepository.remove(TestData.attendCourse.id)
        } returns true

        mainCoroutineRule.runBlockingTest {
            viewModel.onDeleteConfirmClick()
        }

        assertThat(LiveDataTestUtil.getValue(viewModel.finish)).isEqualTo(Unit)
    }

    @Test
    fun onDeleteConfirmClick_deleteData_failed() {
        coEvery {
            attendCourseRepository.remove(TestData.attendCourse.id)
        } returns false

        mainCoroutineRule.runBlockingTest {
            viewModel.onDeleteConfirmClick()
        }

        val error = LiveDataTestUtil.getValue(viewModel.error)
        assertThat(error?.peekContent()).isEqualTo(R.string.error_delete_failed)
    }
}