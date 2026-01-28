package pepes.co.trofes

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CountryPickerBottomSheet(
    private val initial: Country,
    private val onSelected: (Country) -> Unit,
) : BottomSheetDialogFragment() {

    private val allCountries: List<Country> = CountryUtils.getCountries()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.bottomsheet_country_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val rv = view.findViewById<RecyclerView>(R.id.rvCountries)

        rv.layoutManager = LinearLayoutManager(requireContext())

        val adapter = CountryAdapter(allCountries) { picked ->
            onSelected(picked)
            dismissAllowingStateLoss()
        }
        rv.adapter = adapter

        // Small UX: scroll to initial if present
        val initialIndex = allCountries.indexOfFirst { it.iso2 == initial.iso2 }
        if (initialIndex >= 0) {
            rv.scrollToPosition(initialIndex)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim().orEmpty()
                if (q.isEmpty()) {
                    adapter.submitList(allCountries)
                } else {
                    val filtered = allCountries.filter {
                        it.name.contains(q, ignoreCase = true) || it.dialCode.contains(q)
                    }
                    adapter.submitList(filtered)
                }
            }
        })
    }
}

