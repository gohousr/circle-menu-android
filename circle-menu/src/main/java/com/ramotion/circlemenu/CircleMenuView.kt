package com.ramotion.circlemenu


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout

import com.google.android.material.floatingactionbutton.FloatingActionButton

import java.util.ArrayList

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat


/**
 * CircleMenuView
 */
class CircleMenuView : FrameLayout {

  private val mButtons = ArrayList<View>()
  private val mButtonRect = Rect()

  private var mMenuButton: FloatingActionButton? = null
  private var mRingView: RingEffectView? = null

  private var mClosedState = true
  private var mIsAnimating = false

  @get:DrawableRes
  var iconMenu: Int = 0
  @get:DrawableRes
  var iconClose: Int = 0
  /**
   * See [R.styleable.CircleMenuView_duration_ring]
   * @return current ring animation duration.
   */
  /**
   * See [R.styleable.CircleMenuView_duration_ring]
   * @param duration ring animation duration in milliseconds.
   */
  var durationRing: Int = 0
  /**
   * See [R.styleable.CircleMenuView_long_click_duration_ring]
   * @return current long click ring animation duration.
   */
  /**
   * See [R.styleable.CircleMenuView_long_click_duration_ring]
   * @param duration long click ring animation duration in milliseconds.
   */
  var longClickDurationRing: Int = 0
  /**
   * See [R.styleable.CircleMenuView_duration_open]
   * @return current open animation duration.
   */
  /**
   * See [R.styleable.CircleMenuView_duration_open]
   * @param duration open animation duration in milliseconds.
   */
  var durationOpen: Int = 0
  /**
   * See [R.styleable.CircleMenuView_duration_close]
   * @return current close animation duration.
   */
  /**
   * See [R.styleable.CircleMenuView_duration_close]
   * @param duration close animation duration in milliseconds.
   */
  var durationClose: Int = 0
  private var mDesiredSize: Int = 0
  private var mRingRadius: Int = 0

  private var mDistance: Float = 0.toFloat()

  /**
   * See [CircleMenuView.EventListener]
   * @return current event listener or null.
   */
  /**
   * See [CircleMenuView.EventListener]
   * @param listener new event listener or null.
   */
  var eventListener: EventListener? = null

  private val openMenuAnimation: Animator
    get() {
      val alphaAnimation = ObjectAnimator.ofFloat(mMenuButton!!, "alpha", DEFAULT_CLOSE_ICON_ALPHA)

      val kf0 = Keyframe.ofFloat(0f, 0f)
      val kf1 = Keyframe.ofFloat(0.5f, 60f)
      val kf2 = Keyframe.ofFloat(1f, 0f)
      val pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2)
      val rotateAnimation = ObjectAnimator.ofPropertyValuesHolder(mMenuButton, pvhRotation)
      rotateAnimation.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
        private var iconChanged = false
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
          val fraction = valueAnimator.animatedFraction
          if (fraction >= 0.5f && !iconChanged) {
            iconChanged = true
            mMenuButton!!.setImageResource(iconClose)
          }
        }
      })

      val centerX = mMenuButton!!.x
      val centerY = mMenuButton!!.y

      val buttonsCount = mButtons.size
      val angleStep = 360f / buttonsCount

      val buttonsAppear = ValueAnimator.ofFloat(0f, mDistance)
      buttonsAppear.interpolator = OvershootInterpolator()
      buttonsAppear.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) {
          for (view in mButtons) {
            view.visibility = View.VISIBLE
          }
        }
      })
      buttonsAppear.addUpdateListener { valueAnimator ->
        val fraction = valueAnimator.animatedFraction
        val value = valueAnimator.animatedValue as Float
        offsetAndScaleButtons(centerX, centerY, angleStep, value, fraction)
      }

      val result = AnimatorSet()
      result.playTogether(alphaAnimation, rotateAnimation, buttonsAppear)
      result.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) {
          mIsAnimating = true
        }

        override fun onAnimationEnd(animation: Animator) {
          mIsAnimating = false
        }
      })

      return result
    }

  private val closeMenuAnimation: Animator
    get() {
      val scaleX1 = ObjectAnimator.ofFloat(mMenuButton!!, "scaleX", 0f)
      val scaleY1 = ObjectAnimator.ofFloat(mMenuButton!!, "scaleY", 0f)
      val alpha1 = ObjectAnimator.ofFloat(mMenuButton!!, "alpha", 0f)
      val set1 = AnimatorSet()
      set1.playTogether(scaleX1, scaleY1, alpha1)
      set1.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) {
          for (view in mButtons) {
            view.visibility = View.INVISIBLE
          }
        }

        override fun onAnimationEnd(animation: Animator) {
          mMenuButton!!.rotation = 60f
          mMenuButton!!.setImageResource(iconMenu)
        }
      })

      val angle = ObjectAnimator.ofFloat(mMenuButton!!, "rotation", 0f)
      val alpha2 = ObjectAnimator.ofFloat(mMenuButton!!, "alpha", 1f)
      val scaleX2 = ObjectAnimator.ofFloat(mMenuButton!!, "scaleX", 1f)
      val scaleY2 = ObjectAnimator.ofFloat(mMenuButton!!, "scaleY", 1f)
      val set2 = AnimatorSet()
      set2.interpolator = OvershootInterpolator()
      set2.playTogether(angle, alpha2, scaleX2, scaleY2)

      val result = AnimatorSet()
      result.play(set1).before(set2)
      result.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) {
          mIsAnimating = true
        }

        override fun onAnimationEnd(animation: Animator) {
          mIsAnimating = false
        }
      })
      return result
    }

  /**
   * See [R.styleable.CircleMenuView_distance]
   * @return current distance in pixels.
   */
  /**
   * See [R.styleable.CircleMenuView_distance]
   * @param distance in pixels.
   */
  var distance: Float
    get() = mDistance
    set(distance) {
      mDistance = distance
      invalidate()
    }

  /**
   * CircleMenu event listener.
   */
  open class EventListener {
    /**
     * Invoked on menu button click, before animation start.
     * @param view current CircleMenuView instance.
     */
    open fun onMenuOpenAnimationStart(view: CircleMenuView) {}

    /**
     * Invoked on menu button click, after animation end.
     * @param view - current CircleMenuView instance.
     */
    open fun onMenuOpenAnimationEnd(view: CircleMenuView) {}

    /**
     * Invoked on close menu button click, before animation start.
     * @param view - current CircleMenuView instance.
     */
    open fun onMenuCloseAnimationStart(view: CircleMenuView) {}

    /**
     * Invoked on close menu button click, after animation end.
     * @param view - current CircleMenuView instance.
     */
    open fun onMenuCloseAnimationEnd(view: CircleMenuView) {}

    /**
     * Invoked on button click, before animation start.
     * @param view - current CircleMenuView instance.
     * @param buttonIndex - clicked button zero-based index.
     */
    open fun onButtonClickAnimationStart(view: CircleMenuView, buttonIndex: Int) {}

    /**
     * Invoked on button click, after animation end.
     * @param view - current CircleMenuView instance.
     * @param buttonIndex - clicked button zero-based index.
     */
    open fun onButtonClickAnimationEnd(view: CircleMenuView, buttonIndex: Int) {}

    /**
     * Invoked on button long click. Invokes {@see onButtonLongClickAnimationStart} and {@see onButtonLongClickAnimationEnd}
     * if returns true.
     * @param view current CircleMenuView instance.
     * @param buttonIndex clicked button zero-based index.
     * @return  true if the callback consumed the long click, false otherwise.
     */
    open fun onButtonLongClick(view: CircleMenuView, buttonIndex: Int): Boolean {
      return false
    }

    /**
     * Invoked on button long click, before animation start.
     * @param view - current CircleMenuView instance.
     * @param buttonIndex - clicked button zero-based index.
     */
    open fun onButtonLongClickAnimationStart(view: CircleMenuView, buttonIndex: Int) {}

    /**
     * Invoked on button long click, after animation end.
     * @param view - current CircleMenuView instance.
     * @param buttonIndex - clicked button zero-based index.
     */
    open fun onButtonLongClickAnimationEnd(view: CircleMenuView, buttonIndex: Int) {}
  }

  private inner class OnButtonClickListener : View.OnClickListener {
    override fun onClick(view: View) {
      if (mIsAnimating) {
        return
      }

      val click = getButtonClickAnimation(view as FloatingActionButton)
      click.duration = durationRing.toLong()
      click.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) {
          if (eventListener != null) {
            eventListener!!.onButtonClickAnimationStart(this@CircleMenuView, mButtons.indexOf(view))
          }
        }

        override fun onAnimationEnd(animation: Animator) {
          mClosedState = true
          if (eventListener != null) {
            eventListener!!.onButtonClickAnimationEnd(this@CircleMenuView, mButtons.indexOf(view))
          }
        }
      })
      click.start()
    }
  }

  private inner class OnButtonLongClickListener : View.OnLongClickListener {
    override fun onLongClick(view: View): Boolean {
      if (eventListener == null) {
        return false
      }

      val result = eventListener!!.onButtonLongClick(this@CircleMenuView, mButtons.indexOf(view))
      if (result && !mIsAnimating) {
        val click = getButtonClickAnimation(view as FloatingActionButton)
        click.duration = longClickDurationRing.toLong()
        click.addListener(object : AnimatorListenerAdapter() {
          override fun onAnimationStart(animation: Animator) {
            eventListener!!.onButtonLongClickAnimationStart(this@CircleMenuView, mButtons.indexOf(view))
          }

          override fun onAnimationEnd(animation: Animator) {
            mClosedState = true
            eventListener!!.onButtonLongClickAnimationEnd(this@CircleMenuView, mButtons.indexOf(view))
          }
        })
        click.start()
      }

      return result
    }
  }

  @JvmOverloads
  constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {

    if (attrs == null) {
      throw IllegalArgumentException("No buttons icons or colors set")
    }

    val menuButtonColor: Int
    val icons: MutableList<Int>
    val colors: MutableList<Int>

    val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CircleMenuView, 0, 0)
    try {
      val iconArrayId = a.getResourceId(R.styleable.CircleMenuView_button_icons, 0)
      val colorArrayId = a.getResourceId(R.styleable.CircleMenuView_button_colors, 0)

      val iconsIds = resources.obtainTypedArray(iconArrayId)
      try {
        val colorsIds = resources.getIntArray(colorArrayId)
        val buttonsCount = Math.min(iconsIds.length(), colorsIds.size)

        icons = ArrayList(buttonsCount)
        colors = ArrayList(buttonsCount)

        for (i in 0 until buttonsCount) {
          icons.add(iconsIds.getResourceId(i, -1))
          colors.add(colorsIds[i])
        }
      } finally {
        iconsIds.recycle()
      }

      iconMenu = a.getResourceId(R.styleable.CircleMenuView_icon_menu, R.drawable.ic_menu_black_24dp)
      iconClose = a.getResourceId(R.styleable.CircleMenuView_icon_close, R.drawable.ic_close_black_24dp)

      durationRing = a.getInteger(R.styleable.CircleMenuView_duration_ring, resources.getInteger(android.R.integer.config_mediumAnimTime))
      longClickDurationRing = a.getInteger(R.styleable.CircleMenuView_long_click_duration_ring, resources.getInteger(android.R.integer.config_longAnimTime))
      durationOpen = a.getInteger(R.styleable.CircleMenuView_duration_open, resources.getInteger(android.R.integer.config_mediumAnimTime))
      durationClose = a.getInteger(R.styleable.CircleMenuView_duration_close, resources.getInteger(android.R.integer.config_mediumAnimTime))

      val density = context.resources.displayMetrics.density
      val defaultDistance = DEFAULT_DISTANCE * density
      mDistance = a.getDimension(R.styleable.CircleMenuView_distance, defaultDistance)

      menuButtonColor = a.getColor(R.styleable.CircleMenuView_icon_color, Color.WHITE)
    } finally {
      a.recycle()
    }

    initLayout(context)
    initMenu(menuButtonColor)
    initButtons(context, icons, colors)
  }

  /**
   * Constructor for creation CircleMenuView in code, not in xml-layout.
   * @param context current context, will be used to access resources.
   * @param icons buttons icons resource ids array. Items must be @DrawableRes.
   * @param colors buttons colors resource ids array. Items must be @DrawableRes.
   */
  constructor(context: Context, icons: List<Int>, colors: List<Int>) : super(context) {

    val density = context.resources.displayMetrics.density
    val defaultDistance = DEFAULT_DISTANCE * density

    iconMenu = R.drawable.ic_menu_black_24dp
    iconClose = R.drawable.ic_close_black_24dp

    durationRing = resources.getInteger(android.R.integer.config_mediumAnimTime)
    longClickDurationRing = resources.getInteger(android.R.integer.config_longAnimTime)
    durationOpen = resources.getInteger(android.R.integer.config_mediumAnimTime)
    durationClose = resources.getInteger(android.R.integer.config_mediumAnimTime)

    mDistance = defaultDistance

    initLayout(context)
    initMenu(Color.WHITE)
    initButtons(context, icons, colors)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    val w = View.resolveSizeAndState(mDesiredSize, widthMeasureSpec, 0)
    val h = View.resolveSizeAndState(mDesiredSize, heightMeasureSpec, 0)

    setMeasuredDimension(w, h)
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)

    if (!changed && mIsAnimating) {
      return
    }

    mMenuButton!!.getContentRect(mButtonRect)

    mRingView!!.setStrokeWidth(mButtonRect.width())
    mRingView!!.radius = mRingRadius

    val lp = mRingView!!.layoutParams as FrameLayout.LayoutParams
    lp.width = right - left
    lp.height = bottom - top
  }

  private fun initLayout(context: Context) {
    LayoutInflater.from(context).inflate(R.layout.circle_menu, this, true)

    setWillNotDraw(true)
    clipChildren = false
    clipToPadding = false

    val density = context.resources.displayMetrics.density
    val buttonSize = DEFAULT_BUTTON_SIZE * density

    mRingRadius = (buttonSize + (mDistance - buttonSize / 2)).toInt()
    mDesiredSize = (mRingRadius.toFloat() * 2f * DEFAULT_RING_SCALE_RATIO).toInt()

    mRingView = findViewById(R.id.ring_view)
  }

  private fun initMenu(menuButtonColor: Int) {
    val animListener = object : AnimatorListenerAdapter() {
      override fun onAnimationStart(animation: Animator) {
        if (eventListener != null) {
          if (mClosedState) {
            eventListener!!.onMenuOpenAnimationStart(this@CircleMenuView)
          } else {
            eventListener!!.onMenuCloseAnimationStart(this@CircleMenuView)
          }
        }
      }

      override fun onAnimationEnd(animation: Animator) {
        if (eventListener != null) {
          if (mClosedState) {
            eventListener!!.onMenuOpenAnimationEnd(this@CircleMenuView)
          } else {
            eventListener!!.onMenuCloseAnimationEnd(this@CircleMenuView)
          }
        }

        mClosedState = !mClosedState
      }
    }

    mMenuButton = findViewById(R.id.circle_menu_main_button)
    mMenuButton!!.setImageResource(iconMenu)
    mMenuButton!!.backgroundTintList = ColorStateList.valueOf(menuButtonColor)
    mMenuButton!!.setOnClickListener(OnClickListener {
      if (mIsAnimating) {
        return@OnClickListener
      }

      val animation = if (mClosedState) openMenuAnimation else closeMenuAnimation
      animation.duration = (if (mClosedState) durationClose else durationOpen).toLong()
      animation.addListener(animListener)
      animation.start()
    })
  }

  private fun initButtons(context: Context, icons: List<Int>, colors: List<Int>) {
    val buttonsCount = Math.min(icons.size, colors.size)
    for (i in 0 until buttonsCount) {
      val button = FloatingActionButton(context)
      button.setImageResource(icons[i])
      button.backgroundTintList = ColorStateList.valueOf(colors[i])
      button.isClickable = true
      button.setOnClickListener(OnButtonClickListener())
      button.setOnLongClickListener(OnButtonLongClickListener())
      button.scaleX = 0f
      button.scaleY = 0f
      button.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)

      addView(button)
      mButtons.add(button)
    }
  }

  private fun offsetAndScaleButtons(centerX: Float, centerY: Float, angleStep: Float, offset: Float, scale: Float) {
    var i = 0
    val cnt = mButtons.size
    while (i < cnt) {
      val angle = angleStep * i - 90
      val x = Math.cos(Math.toRadians(angle.toDouble())).toFloat() * offset
      val y = Math.sin(Math.toRadians(angle.toDouble())).toFloat() * offset

      val button = mButtons[i]
      button.x = centerX + x
      button.y = centerY + y
      button.scaleX = 1.0f * scale
      button.scaleY = 1.0f * scale
      i++
    }
  }

  private fun getButtonClickAnimation(button: FloatingActionButton): Animator {
    val buttonNumber = mButtons.indexOf(button) + 1
    val stepAngle = 360f / mButtons.size
    val rOStartAngle = 270 - stepAngle + stepAngle * buttonNumber
    val rStartAngle = if (rOStartAngle > 360) rOStartAngle % 360 else rOStartAngle

    val x = Math.cos(Math.toRadians(rStartAngle.toDouble())).toFloat() * mDistance
    val y = Math.sin(Math.toRadians(rStartAngle.toDouble())).toFloat() * mDistance

    val pivotX = button.pivotX
    val pivotY = button.pivotY
    button.pivotX = pivotX - x
    button.pivotY = pivotY - y

    val rotateButton = ObjectAnimator.ofFloat(button, "rotation", 0f, 360f)
    rotateButton.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator) {
        button.pivotX = pivotX
        button.pivotY = pivotY
      }
    })

    val elevation = mMenuButton!!.compatElevation

    mRingView!!.visibility = View.INVISIBLE
    mRingView!!.startAngle = rStartAngle

    val csl = button.backgroundTintList
    if (csl != null) {
      mRingView!!.setStrokeColor(csl.defaultColor)
    }

    val ring = ObjectAnimator.ofFloat(mRingView!!, "angle", 360f)
    val scaleX = ObjectAnimator.ofFloat(mRingView!!, "scaleX", 1f, DEFAULT_RING_SCALE_RATIO)
    val scaleY = ObjectAnimator.ofFloat(mRingView!!, "scaleY", 1f, DEFAULT_RING_SCALE_RATIO)
    val visible = ObjectAnimator.ofFloat(mRingView!!, "alpha", 1f, 0f)

    val lastSet = AnimatorSet()
    lastSet.playTogether(scaleX, scaleY, visible, closeMenuAnimation)

    val firstSet = AnimatorSet()
    firstSet.playTogether(rotateButton, ring)

    val result = AnimatorSet()
    result.play(firstSet).before(lastSet)
    result.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationStart(animation: Animator) {
        mIsAnimating = true

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
          bringChildToFront(mRingView)
          bringChildToFront(button)
        } else {
          button.compatElevation = elevation + 1
          ViewCompat.setZ(mRingView!!, elevation + 1)

          for (b in mButtons) {
            if (b !== button) {
              (b as FloatingActionButton).compatElevation = 0f
            }
          }
        }

        mRingView!!.scaleX = 1f
        mRingView!!.scaleY = 1f
        mRingView!!.visibility = View.VISIBLE
      }

      override fun onAnimationEnd(animation: Animator) {
        mIsAnimating = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          for (b in mButtons) {
            (b as FloatingActionButton).compatElevation = elevation
          }

          ViewCompat.setZ(mRingView!!, elevation)
        }
      }
    })

    return result
  }

  private fun openOrClose(open: Boolean, animate: Boolean) {
    if (mIsAnimating) {
      return
    }

    if (open && !mClosedState) {
      return
    }

    if (!open && mClosedState) {
      return
    }

    if (animate) {
      mMenuButton!!.performClick()
    } else {
      mClosedState = !open

      val centerX = mMenuButton!!.x
      val centerY = mMenuButton!!.y

      val buttonsCount = mButtons.size
      val angleStep = 360f / buttonsCount

      val offset = if (open) mDistance else 0f
      val scale = if (open) 1f else 0f

      mMenuButton!!.setImageResource(if (open) iconClose else iconMenu)
      mMenuButton!!.alpha = if (open) DEFAULT_CLOSE_ICON_ALPHA else 1f

      val visibility = if (open) View.VISIBLE else View.INVISIBLE
      for (view in mButtons) {
        view.visibility = visibility
      }

      offsetAndScaleButtons(centerX, centerY, angleStep, offset, scale)
    }
  }

  /**
   * Open menu programmatically
   * @param animate open with animation or not
   */
  fun open(animate: Boolean) {
    openOrClose(true, animate)
  }

  /**
   * Close menu programmatically
   * @param animate close with animation or not
   */
  fun close(animate: Boolean) {
    openOrClose(false, animate)
  }

  companion object {

    private val DEFAULT_BUTTON_SIZE = 56
    private val DEFAULT_DISTANCE = DEFAULT_BUTTON_SIZE * 1.5f
    private val DEFAULT_RING_SCALE_RATIO = 1.3f
    private val DEFAULT_CLOSE_ICON_ALPHA = 0.3f
  }

}
