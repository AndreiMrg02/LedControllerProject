package com.example.ledcontrollerproject.ui.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import java.util.*

class ScheduleFragment : Fragment() {

/*    private var _binding: FragmentSlideshowBinding? = null
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
    }*/

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TimePickerDialogWrapper(
        onTimeSet: (Int, Int) -> Unit
    ) {
        var selectedHour by remember { mutableStateOf(0) }
        var selectedMinute by remember { mutableStateOf(0) }

        // Utilizăm LaunchedEffect pentru a actualiza textul în TextView
        LaunchedEffect(selectedHour, selectedMinute) {
            onTimeSet(selectedHour, selectedMinute)
        }

        // Compose TimePicker
        TimePicker(
            modifier = Modifier.padding(16.dp),
            hour = selectedHour,
            minute = selectedMinute,
            onHourChange = { selectedHour = it },
            onMinuteChange = { selectedMinute = it },
        )
    }

    @Composable
    fun ScheduleScreen() {
        var amountInput by remember { mutableStateOf("") }
        var switchChecked by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = { /* Handle button click */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Creează Element")
            }

            Text(
                text = "Ora: ",
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clickable {
                        showTimePickerDialog()
                    }
            )

            TextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                placeholder = { Text("Comentarii") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Switch(
                checked = switchChecked,
                onCheckedChange = { switchChecked = it },
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Button(
                    onClick = { /* Handle button click */ }
                ) {
                    Text("Extinde")
                }

                // Alte elemente pentru zilele săptămânii
                val daysOfWeek = arrayOf("L", "M", "M", "J", "V", "S", "D")
                for (day in daysOfWeek) {
                    Button(
                        onClick = { /* Handle button click for each day */ },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(day)
                    }
                }
            }
        }
    }