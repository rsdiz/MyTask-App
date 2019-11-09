package id.codehero.mytask

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val ADD_TASK_REQUEST = 1
    private val PREFS_TASK = "prefs_tasks"
    private val KEY_TASKS_LIST = "tasks_list"
    // Initialize the activity properties include empty mutable list
    private val taskList: MutableList<String> = mutableListOf()
    // Initialize adapter using by lazy
    private val adapter by lazy { makeAdapter(taskList) }
    private val tickReceiver by lazy { makeBroadcastReceiver() }

    companion object {
        private const val LOG_TAG = "MainActivityLog"
        private fun getCurrentTimeStamp(): String {
            val simpleDateFormat = SimpleDateFormat("HH:mm [dd-MM-yyyy]", Locale.ENGLISH)
            val now = Date()
            return simpleDateFormat.format(now)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // setup the adapter for taskListView
        taskListView.adapter = adapter
        taskListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ -> taskSelected(position) }

        val savedList =
            getSharedPreferences(PREFS_TASK, Context.MODE_PRIVATE).getString(KEY_TASKS_LIST, null)
        if (savedList != null) {
            val items = savedList.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            taskList.addAll(items)
        }
    }

    override fun onResume() {
        super.onResume()
        dateTimeTextView.text = getCurrentTimeStamp()
        registerReceiver(tickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(tickReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e(LOG_TAG, "Time tick Receiver not registered", e)
        }
    }

    override fun onStop() {
        super.onStop()
        val savedList = StringBuilder()
        for (task in taskList) {
            savedList.append(task)
            savedList.append(",")
        }

        getSharedPreferences(PREFS_TASK, Context.MODE_PRIVATE).edit()
            .putString(KEY_TASKS_LIST, savedList.toString()).apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TASK_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val task = data?.getStringExtra(AddTask.EXTRA_TASK)
                task?.let {
                    taskList.add(task)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    // Method onClick for "Add Task"
    fun addTask(view: View) {
        val intent = Intent(this, AddTask::class.java)
        startActivityForResult(intent, ADD_TASK_REQUEST)
    }

    private fun taskSelected(position: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.alert_title)
            .setMessage(taskList[position])
            .setPositiveButton(R.string.delete) { _, _ ->
                taskList.removeAt(position)
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun makeBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_TIME_TICK) {
                    dateTimeTextView.text = getCurrentTimeStamp()
                }
            }
        }
    }

    // Private function that initialize the adapter for the list view
    private fun makeAdapter(list: List<String>): ArrayAdapter<String> =
        ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
}
