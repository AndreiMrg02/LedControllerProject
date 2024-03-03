package com.example.ledcontrollerproject.ui.slideshow

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ledcontrollerproject.databinding.FragmentSlideshowBinding
import java.util.Calendar

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):  View {
        val homeViewModel = ViewModelProvider(this).get(SlideshowViewModel::class.java)
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val scrollView = ScrollView(requireContext())
        val scrollViewParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        scrollView.layoutParams = scrollViewParams

        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT  // Poate fi WRAP_CONTENT pentru înălțimea dorită
        )

        val buttonCreateElement = Button(requireContext())
        buttonCreateElement.text = "Creează Element"
        linearLayout.addView(buttonCreateElement)
        buttonCreateElement.setOnClickListener {
            createNewElement(linearLayout, root)
        }

        // Adaugă linearLayout în ScrollView
        scrollView.addView(linearLayout)

        // Adaugă ScrollView în root
        (root as ViewGroup).addView(scrollView)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun showTimePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                // Actualizează textul în TextView utilizând dataBinding
                textView.text = "Ora: $selectedHour:$selectedMinute"
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }

    private fun toggleDaysLayoutVisibility(layoutDays: LinearLayout) {
        if (layoutDays.visibility == View.VISIBLE) {
            layoutDays.visibility = View.GONE
        } else {
            layoutDays.visibility = View.VISIBLE
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun createNewElement(linearLayout: LinearLayout, root: View) {
        val textView = TextView(requireContext())
        textView.text = "Ora: "
        textView.setOnClickListener {
            showTimePickerDialog(textView)
        }
        linearLayout.addView(textView)

        val editTextComments = EditText(requireContext())
        editTextComments.hint = "Comentarii"
        linearLayout.addView(editTextComments)

        val switchToggle = Switch(requireContext())
        switchToggle.text = "Porneste/Opreste"
        linearLayout.addView(switchToggle)

        val layoutDays = LinearLayout(requireContext())
        layoutDays.orientation = LinearLayout.HORIZONTAL

        val buttonExpand = Button(requireContext())
        buttonExpand.text = "Extinde"
        layoutDays.visibility = View.GONE
        buttonExpand.setOnClickListener {
            toggleDaysLayoutVisibility(layoutDays)
        }
        linearLayout.addView(buttonExpand)
        linearLayout.addView(layoutDays)

        val daysOfWeek = arrayOf("L", "M", "M", "J", "V", "S", "D")
        for (day in daysOfWeek) {
            val dayButton = Button(requireContext())
            dayButton.text = day
            layoutDays.addView(dayButton)
        }
    }
}