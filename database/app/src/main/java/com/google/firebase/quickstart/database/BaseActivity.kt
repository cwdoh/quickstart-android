package com.google.firebase.quickstart.database

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity

import com.google.firebase.auth.FirebaseAuth


open class BaseActivity : AppCompatActivity() {

    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            setCancelable(false)
            setMessage("Loading...")
        }
    }

    val uid: String
        get() = FirebaseAuth.getInstance().currentUser.run { uid }

    fun showProgressDialog() = progressDialog.show()

    fun hideProgressDialog() = progressDialog.run { if (isShowing) dismiss() }
}
