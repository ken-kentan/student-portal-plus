package jp.kentan.studentportalplus.ui.mycoursedetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import jp.kentan.studentportalplus.LiveDataTestUtil
import jp.kentan.studentportalplus.MainCoroutineRule
import jp.kentan.studentportalplus.R
import jp.kentan.studentportalplus.TestData
import jp.kentan.studentportalplus.data.MyCourseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MyCourseDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val attendCourseRepository = mockk<MyCourseRepository>()

    private lateinit var viewModel: MyCourseDetailViewModel

    @Before
    fun setUp() {
        val id = TestData.attendCourse.id

        viewModel = MyCourseDetailViewModel(attendCourseRepository)
        viewModel.onActivityCreate(id)

        every {
            attendCourseRepository.getAsFlow(id)
        } returns flowOf(TestData.attendCourse)
    }

    @Test
    fun onActivityCreate_dataLoad() {
        val vm = MyCourseDetailViewModel(attendCourseRepository)
        vm.onActivityCreate(TestData.attendCourse.id)

        val attendCourse = LiveDataTestUtil.getValue(vm.myCourse)

        assertThat(attendCourse).isEqualTo(TestData.attendCourse)

        val enabledDeleteOptionMenu = LiveDataTestUtil.getValue(vm.enabledDeleteOptionMenu)
        assertThat(enabledDeleteOptionMenu?.peekContent()).isEqualTo(Unit)
    }

    @Test
    fun onActivityCreate_dataNotFound() {
        every {
            attendCourseRepository.getAsFlow(TestData.attendCourse.id)
        } returns flowOf(null)

        val vm = MyCourseDetailViewModel(attendCourseRepository)
        vm.onActivityCreate(TestData.attendCourse.id)

        assertThat(LiveDataTestUtil.getValue(vm.myCourse)).isNull()

        assertThat(LiveDataTestUtil.getValue(vm.error)?.peekContent())
            .isEqualTo(R.string.all_not_found_error)

        assertThat(LiveDataTestUtil.getValue(vm.finish)).isEqualTo(Unit)
    }

    @Test
    fun onEditClick_startEditActivity() {
        viewModel.onEditClick()

        val startEditActivity = LiveDataTestUtil.getValue(viewModel.startEditMyCourseActivity)
        assertThat(startEditActivity?.peekContent()).isEqualTo(TestData.attendCourse.id)
    }

    @Test
    fun onDeleteClick_showDialog() {
        // TODO resume
        LiveDataTestUtil.getValue(viewModel.myCourse)

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
        assertThat(error?.peekContent()).isEqualTo(R.string.all_delete_failed)
    }
}
