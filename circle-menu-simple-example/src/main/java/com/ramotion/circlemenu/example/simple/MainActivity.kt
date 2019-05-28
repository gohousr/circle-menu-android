package com.ramotion.circlemenu.example.simple

import android.os.Bundle
import android.util.Log

import com.ramotion.circlemenu.CircleMenuView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val menu = findViewById<CircleMenuView>(R.id.circle_menu)
    menu.eventListener = object : CircleMenuView.EventListener() {
      override fun onMenuOpenAnimationStart(view: CircleMenuView) {
        Log.d("D", "onMenuOpenAnimationStart")
      }

      override fun onMenuOpenAnimationEnd(view: CircleMenuView) {
        Log.d("D", "onMenuOpenAnimationEnd")
      }

      override fun onMenuCloseAnimationStart(view: CircleMenuView) {
        Log.d("D", "onMenuCloseAnimationStart")
      }

      override fun onMenuCloseAnimationEnd(view: CircleMenuView) {
        Log.d("D", "onMenuCloseAnimationEnd")
      }

      override fun onButtonClickAnimationStart(view: CircleMenuView, index: Int) {
        Log.d("D", "onButtonClickAnimationStart| index: $index")
      }

      override fun onButtonClickAnimationEnd(view: CircleMenuView, index: Int) {
        Log.d("D", "onButtonClickAnimationEnd| index: $index")
      }

      override fun onButtonLongClick(view: CircleMenuView, index: Int): Boolean {
        Log.d("D", "onButtonLongClick| index: $index")
        return true
      }

      override fun onButtonLongClickAnimationStart(view: CircleMenuView, index: Int) {
        Log.d("D", "onButtonLongClickAnimationStart| index: $index")
      }

      override fun onButtonLongClickAnimationEnd(view: CircleMenuView, index: Int) {
        Log.d("D", "onButtonLongClickAnimationEnd| index: $index")
      }
    }
  }
}
