package com.google.firebase.quickstart.database

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.quickstart.database.models.User
import com.google.firebase.quickstart.database.models.Comment
import com.google.firebase.quickstart.database.models.Post

import java.util.ArrayList
import kotlinx.android.synthetic.main.activity_post_detail.*

class PostDetailActivity : BaseActivity(), View.OnClickListener {

    private lateinit var postKey: String
    private val postReference: DatabaseReference by lazy {
        // Initialize Database reference
        FirebaseDatabase.getInstance().reference
                .child("posts").child(postKey)
    }
    private val commentsReference: DatabaseReference by lazy {
        // Initialize Database reference
        FirebaseDatabase.getInstance().reference
                .child("post-comments").child(postKey)
    }
    private val adapter: CommentAdapter by lazy {
        CommentAdapter(this, commentsReference)
    }
    private val postListener: ValueEventListener by lazy {
        // [START post_value_event_listener]
        object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val post = dataSnapshot.getValue(Post::class.java)
                // [START_EXCLUDE]
                post?.run {
                    post_author.text = author
                    post_title.text = title
                    post_body.text = body
                }
                // [END_EXCLUDE]
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(this@PostDetailActivity, "Failed to load post.",
                        Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }
        // [END post_value_event_listener]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // Get post key from intent
        postKey = intent.getStringExtra(EXTRA_POST_KEY)
                ?: throw IllegalArgumentException("Must pass EXTRA_POST_KEY")

        // Initialize Views
        button_post_comment.setOnClickListener(this)
        recycler_comments.layoutManager = LinearLayoutManager(this)

    }

    public override fun onStart() {
        super.onStart()

        // Add value event listener to the post
        postReference.addValueEventListener(postListener)

        // Listen for comments
        recycler_comments.adapter = adapter
    }

    public override fun onStop() {
        super.onStop()

        // Remove post value event listener
        postReference.removeEventListener(postListener)

        // Clean up comments listener
        adapter.cleanupListener()
    }

    override fun onClick(v: View) { if (v.id == R.id.button_post_comment) postComment() }

    private fun postComment() {
        val uid = uid
        FirebaseDatabase.getInstance().reference.child("users").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Get user information
                        val user = dataSnapshot.getValue(User::class.java)
                        val authorName = user!!.username

                        // Create new comment object
                        val commentText = field_comment_text!!.text.toString()
                        val comment = Comment(uid, authorName, commentText)

                        // Push the comment, it will appear in the list
                        commentsReference!!.push().setValue(comment)

                        // Clear the field
                        field_comment_text!!.text = null
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
    }

    private class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var authorView: TextView
        var bodyView: TextView

        init {
            authorView = itemView.findViewById(R.id.comment_author) as TextView
            bodyView = itemView.findViewById(R.id.comment_body) as TextView
        }
    }

    private class CommentAdapter(private val mContext: Context, private val mDatabaseReference: DatabaseReference) : RecyclerView.Adapter<CommentViewHolder>() {
        private val mChildEventListener: ChildEventListener?

        private val mCommentIds = ArrayList<String>()
        private val mComments = ArrayList<Comment>()

        init {

            // Create child event listener
            // [START child_event_listener_recycler]
            val childEventListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.key)

                    // A new comment has been added, add it to the displayed list
                    val comment = dataSnapshot.getValue(Comment::class.java)

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mCommentIds.add(dataSnapshot.key)
                    mComments.add(comment)
                    notifyItemInserted(mComments.size - 1)
                    // [END_EXCLUDE]
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.key)

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    val newComment = dataSnapshot.getValue(Comment::class.java)
                    val commentKey = dataSnapshot.key

                    // [START_EXCLUDE]
                    val commentIndex = mCommentIds.indexOf(commentKey)
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mComments.set(commentIndex, newComment)

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex)
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey)
                    }
                    // [END_EXCLUDE]
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.key)

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    val commentKey = dataSnapshot.key

                    // [START_EXCLUDE]
                    val commentIndex = mCommentIds.indexOf(commentKey)
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.removeAt(commentIndex)
                        mComments.removeAt(commentIndex)

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex)
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey)
                    }
                    // [END_EXCLUDE]
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.key)

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    val movedComment = dataSnapshot.getValue(Comment::class.java)
                    val commentKey = dataSnapshot.key

                    // ...
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException())
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show()
                }
            }
            mDatabaseReference.addChildEventListener(childEventListener)
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            val inflater = LayoutInflater.from(mContext)
            val view = inflater.inflate(R.layout.item_comment, parent, false)
            return CommentViewHolder(view)
        }

        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            val comment = mComments[position]
            holder.authorView.text = comment.author
            holder.bodyView.text = comment.text
        }

        override fun getItemCount(): Int {
            return mComments.size
        }

        fun cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener)
            }
        }

    }

    companion object {

        private val TAG = "PostDetailActivity"

        val EXTRA_POST_KEY = "post_key"
    }
}
