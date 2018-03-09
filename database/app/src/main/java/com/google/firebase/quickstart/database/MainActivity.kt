/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.database

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.quickstart.database.fragment.MyPostsFragment
import com.google.firebase.quickstart.database.fragment.MyTopPostsFragment
import com.google.firebase.quickstart.database.fragment.RecentPostsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private val viewPager: ViewPager by lazy {
        container.apply {
            // Set up the ViewPager with the sections adapter.
            viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
                // Create the adapter that will return a fragment for each section
                private val fragments = arrayOf<Fragment>(
                        RecentPostsFragment(),
                        MyPostsFragment(),
                        MyTopPostsFragment())

                private val fragmentNames = arrayOf(
                        getString(R.string.heading_recent),
                        getString(R.string.heading_my_posts),
                        getString(R.string.heading_my_top_posts))

                override fun getItem(position: Int): Fragment {
                    return fragments[position]
                }

                override fun getCount(): Int {
                    return fragments.size
                }

                override fun getPageTitle(position: Int): CharSequence {
                    return fragmentNames[position]
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabs.setupWithViewPager(viewPager)

        // Button launches NewPostActivity
        fab_new_post.setOnClickListener { startActivity(Intent(this@MainActivity, NewPostActivity::class.java)) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when(item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    companion object {
        private val TAG = "MainActivity"
    }

}
