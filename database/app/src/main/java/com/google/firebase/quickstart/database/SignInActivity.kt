package com.google.firebase.quickstart.database

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.quickstart.database.models.User
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity(), View.OnClickListener {

    private val mDatabase: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }
    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Click listeners
        button_sign_in.setOnClickListener(this)
        button_sign_up.setOnClickListener(this)
    }

    public override fun onStart() {
        super.onStart()

        // Check auth on Activity start
        mAuth.currentUser?.let {
            onAuthSuccess(it)
        }
    }

    private fun signIn() {
        Log.d(TAG, "signIn")
        if (!validateForm()) {
            return
        }

        showProgressDialog()
        val email = field_email.text.toString()
        val password = field_password.text.toString()

        mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "signIn:onComplete:" + task.isSuccessful)
                    hideProgressDialog()

                    if (task.isSuccessful) {
                        onAuthSuccess(task.result.user)
                    } else {
                        Toast.makeText(this@SignInActivity, "Sign In Failed",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun signUp() {
        Log.d(TAG, "signUp")
        if (!validateForm()) {
            return
        }

        showProgressDialog()
        val email = field_email.text.toString()
        val password = field_password.text.toString()

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "createUser:onComplete:" + task.isSuccessful)
                    hideProgressDialog()

                    if (task.isSuccessful) {
                        onAuthSuccess(task.result.user)
                    } else {
                        Toast.makeText(this@SignInActivity, "Sign Up Failed",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun onAuthSuccess(user: FirebaseUser) {
        val username = usernameFromEmail(user.email)

        // Write new user
        writeNewUser(user.uid, username, user.email)

        // Go to MainActivity
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }

    private fun usernameFromEmail(email: String): String {
        if (email.contains("@")) {
            return email.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else {
            return email
        }
    }

    private fun validateForm(): Boolean {
        var result = true
        if (TextUtils.isEmpty(field_email.text.toString())) {
            field_email.error = "Required"
            result = false
        } else {
            field_email.error = null
        }

        if (TextUtils.isEmpty(field_password.text.toString())) {
            field_password.error = "Required"
            result = false
        } else {
            field_password.error = null
        }

        return result
    }

    // [START basic_write]
    private fun writeNewUser(userId: String, name: String, email: String) {
        val user = User(name, email)

        mDatabase.child("users").child(userId).setValue(user)
    }
    // [END basic_write]

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_sign_in -> signIn()
            R.id.button_sign_up -> signUp()
        }
    }

    companion object {

        private val TAG = "SignInActivity"
    }
}
