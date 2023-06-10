package com.mobile.todo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.mobile.todo.database.AppDatabase
import com.mobile.todo.utils.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Login : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Constant.setTheme(this)

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        database = AppDatabase.getDatabase(this)

        // Redirect to Signup Activity
        findViewById<Button>(R.id.signup_button).setOnClickListener {
            startActivity(Intent(this, EditTodoHabit::class.java))
        }

        findViewById<Button>(R.id.login_button).setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val enteredUsername = username.text.toString().trim()
        val enteredPassword = password.text.toString().trim()

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            Toast.makeText(this, "Username and Password must not be empty", Toast.LENGTH_SHORT)
                .show()
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            val user = withContext(Dispatchers.IO) {
                database.userDao().getUser(enteredUsername, enteredPassword)
            }
            runOnUiThread {
                if (user != null) {
                    val intent = Intent(this@Login, HomePage::class.java)
                    HomePage.pageToShow = R.id.navbar_todo
                    HomePage.USER_ID = user.id
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@Login,
                        "Username or Password is incorrect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}