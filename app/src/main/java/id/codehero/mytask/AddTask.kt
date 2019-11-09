package id.codehero.mytask

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_addtask.*

class AddTask : AppCompatActivity() {
    companion object {
        val EXTRA_TASK = "task"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addtask)
    }

    fun addClick(view: View) {
        val taskDescription = descText.text.toString()
        if (!taskDescription.isEmpty()) {
            val result = Intent()
            result.putExtra(EXTRA_TASK, taskDescription)
            setResult(Activity.RESULT_OK, result)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }

        finish()
    }
}
