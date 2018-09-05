package jp.kentan.studentportalplus.ui.myclass.edit

import com.android.colorpicker.ColorPickerSwatch

interface MyClassEditNavigator {

    fun onErrorValidation(isSubject: Boolean, isCredit: Boolean, isScheduleCode: Boolean)

    fun onMyClassSaved(success: Boolean)

    fun openColorPickerDialog(listener: ColorPickerSwatch.OnColorSelectedListener)
}