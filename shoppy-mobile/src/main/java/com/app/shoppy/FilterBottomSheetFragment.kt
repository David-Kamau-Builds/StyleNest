package com.app.shoppy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.RangeSlider
import com.app.shoppy.ui.viewmodel.SharedProductViewModel

class FilterBottomSheetFragment : BottomSheetDialogFragment() {

    private val productViewModel: SharedProductViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cgCategory = view.findViewById<ChipGroup>(R.id.cgCategory)
        val rsPrice = view.findViewById<RangeSlider>(R.id.rsPrice)
        val tvMinPrice = view.findViewById<TextView>(R.id.tvMinPrice)
        val tvMaxPrice = view.findViewById<TextView>(R.id.tvMaxPrice)
        val btnClear = view.findViewById<MaterialButton>(R.id.btnClear)
        val btnApply = view.findViewById<MaterialButton>(R.id.btnApply)

        rsPrice.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            tvMinPrice.text = "$${values[0].toInt()}"
            tvMaxPrice.text = "$${values[1].toInt()}"
        }

        btnClear.setOnClickListener {
            cgCategory.check(R.id.chipAll)
            rsPrice.values = listOf(0.0f, 1000.0f)
            productViewModel.clearSearchAndFilter()
            dismiss()
        }

        btnApply.setOnClickListener {
            val checkedChipId = cgCategory.checkedChipId
            val category = if (checkedChipId != -1) {
                view.findViewById<Chip>(checkedChipId).text.toString()
            } else {
                "All"
            }
            
            val minPrice = rsPrice.values[0].toDouble()
            val maxPrice = rsPrice.values[1].toDouble()

            productViewModel.filterProducts(
                if (category == "All") "" else category, 
                minPrice, 
                maxPrice
            )
            dismiss()
        }
    }
}
