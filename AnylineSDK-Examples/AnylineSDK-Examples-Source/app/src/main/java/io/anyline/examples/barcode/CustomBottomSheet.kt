package io.anyline.examples.barcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.anyline.examples.databinding.BottomSheetChooseBarcodeModeBinding
import io.anyline.examples.databinding.BottomSheetOptionBarcodeModeBinding

class CustomBottomSheet<T>(
    private val options: List<BottomSheetOption<T>>
) : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetChooseBarcodeModeBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetChooseBarcodeModeBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            header.text = "Select Mode"
            options.adapter = BottomSheetRecyclerViewAdapter(
                this@CustomBottomSheet.options,
                this@CustomBottomSheet::dismiss
            )
            options.layoutManager = LinearLayoutManager(context)
        }
    }
    companion object {
        fun createBarcodeModeBottomSheet(
            selectedOption: BarcodeMode,
            onOptionSelected: (BarcodeMode) -> Unit
        ): CustomBottomSheet<BarcodeMode> {
            val bottomSheet = CustomBottomSheet(
                BarcodeMode.values().map { barcodeMode ->
                    BottomSheetOption(
                        value = barcodeMode,
                        title = barcodeMode.text,
                        isSelected = selectedOption == barcodeMode,
                        onItemSelected = onOptionSelected
                    )
                }
            )
            return bottomSheet
        }
    }

    class BottomSheetRecyclerViewAdapter<T>(
        private val items: List<BottomSheetOption<T>>,
        private val dismiss: () -> Unit
    ) : RecyclerView.Adapter<BottomSheetRecyclerViewHolder<T>>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BottomSheetRecyclerViewHolder<T> {
            return BottomSheetRecyclerViewHolder(
                BottomSheetOptionBarcodeModeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                dismiss
            )
        }
        override fun onBindViewHolder(holder: BottomSheetRecyclerViewHolder<T>, position: Int) {
            holder.bind(items[position])
        }
        override fun getItemCount(): Int = items.size
    }
    class BottomSheetRecyclerViewHolder<T>(
        private val viewBinding: BottomSheetOptionBarcodeModeBinding,
        private val dismiss: () -> Unit
    ) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(bottomSheetOption: BottomSheetOption<T>) {
            viewBinding.title.setText(bottomSheetOption.title)
            viewBinding.icon.isVisible = bottomSheetOption.isSelected
            viewBinding.root.setOnClickListener {
                bottomSheetOption.onItemSelected.invoke(bottomSheetOption.value)
                dismiss()
            }
        }
    }
    class BottomSheetOption<T>(
        val value: T,
        val title: String,
        val isSelected: Boolean,
        val onItemSelected: (T) -> Unit
    )
}