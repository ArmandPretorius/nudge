package com.example.nudge

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintSet

import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.helper.ItemTouchHelper.Callback.makeMovementFlags
import android.view.*
import android.webkit.ConsoleMessage
import android.widget.*

import com.example.nudge.Model.ToDoItem
import java.util.*

import kotlinx.android.synthetic.main.activity_fullscreen.*
import java.io.Console


class FullscreenActivity : AppCompatActivity() {
    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreen_content.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

//handle todolist
    lateinit var dbHandler: DBHandler
    var todoId: Long = -1

    var list: MutableList<ToDoItem>? = null
    var adapter : ItemAdapter? = null
    var touchHelper : ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.
     //   fullscreen_content.setOnClickListener { toggle() }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //dummy_button.setOnTouchListener(mDelayHideTouchListener)

        todoId = intent.getLongExtra(INTENT_TODO_ID, -1)
        dbHandler = DBHandler(this)

        rv_item.layoutManager = LinearLayoutManager(this)

        fab_item.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Add Nudge")
            val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
            val toDoName = view.findViewById<EditText>(R.id.ev_todo)
            dialog.setView(view)
            dialog.setPositiveButton("Add") { _: DialogInterface, _: Int ->
                if (toDoName.text.isNotEmpty()) {
                    val item = ToDoItem()
                    item.itemName = toDoName.text.toString()
                    item.toDoId = todoId
                    item.isCompleted = false
                    dbHandler.addToDoItem(item)
                    refreshList()
                }
            }
            dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
            }
            dialog.show()
            mVisible = true
            toggle()
        }


        touchHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    p0: RecyclerView,
                    p1: RecyclerView.ViewHolder,
                    p2: RecyclerView.ViewHolder
                ): Boolean {
                    val sourcePosition = p1.adapterPosition
                    val targetPosition = p2.adapterPosition
                    Collections.swap(list,sourcePosition,targetPosition)
                    adapter?.notifyItemMoved(sourcePosition,targetPosition)
                    return true
                }

                override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
                    adapter?.notifyItemRemoved(p0.adapterPosition)

                }
            })

        touchHelper?.attachToRecyclerView(rv_item)
    }


//    fun updateItem(item: ToDoItem) {
//        val dialog = AlertDialog.Builder(this)
//        dialog.setTitle("Update ToDo Item")
//        val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
//        val toDoName = view.findViewById<EditText>(R.id.ev_todo)
//        toDoName.setText(item.itemName)
//        dialog.setView(view)
//        dialog.setPositiveButton("Update") { _: DialogInterface, _: Int ->
//            if (toDoName.text.isNotEmpty()) {
//                item.itemName = toDoName.text.toString()
//                item.toDoId = todoId
//                item.isCompleted = false
//                dbHandler.updateToDoItem(item)
//                refreshList()
//            }
//        }
//        dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
//
//        }
//        dialog.show()
//        mVisible = true
//        toggle()
//    }

    override fun onResume() {
        refreshList()
        super.onResume()
    }

    private fun refreshList() {
        list = dbHandler.getToDoItems(todoId)
        adapter = ItemAdapter(this, list!!)
        rv_item.adapter = adapter
    }

    class ItemAdapter(val activity: FullscreenActivity, val list: MutableList<ToDoItem>) :
        RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_card, p0, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, p1: Int) {

            //Sets text to database list
            holder.itemName.text = list[p1].itemName
            holder.itemNameUnchecked.text = list[p1].itemName
           // holder.itemName.isChecked = list[p1].isCompleted

            //checks checked to set styling
            if(list[p1].isCompleted === true){
                holder.checkMark.visibility = View.VISIBLE
                holder.itemNameUnchecked.visibility = View.GONE
                holder.checkedIndication.layoutParams.width = 60
            } else {
                holder.checkMark.visibility = View.INVISIBLE
                holder.checkedIndication.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                holder.itemNameUnchecked.visibility = View.VISIBLE
            }

            //MARK AS CHECkED
            holder.nudgeItem.setOnClickListener {
                list[p1].isCompleted = !list[p1].isCompleted
                activity.dbHandler.updateToDoItem(list[p1])

                //checks checked to set styling
                if(list[p1].isCompleted === true){
                    holder.checkMark.visibility = View.VISIBLE
                    holder.itemNameUnchecked.visibility = View.GONE
                    holder.checkedIndication.layoutParams.width = 60
                } else {
                    holder.checkMark.visibility = View.INVISIBLE
                    holder.itemNameUnchecked.visibility = View.VISIBLE
                    holder.checkedIndication.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }

            //DELETES ITEM
            holder.delete.setOnTouchListener {v, event ->

                if(event.actionMasked == MotionEvent.ACTION_MOVE){
                    activity.touchHelper?.startSwipe(holder)

                    println(list[p1].id)
                    println(list[p1].itemName)
                    val dialog = AlertDialog.Builder(activity)
                    dialog.setTitle("Are you sure")
                    dialog.setMessage("Do you want to delete this item ?")
                    dialog.setPositiveButton("Continue") { _: DialogInterface, _: Int ->
                        activity.dbHandler.deleteToDoItem(list[p1].id)
                        activity.refreshList()
                    }
                    dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->

                    }
                    dialog.show()

                }
                false


            }

////            holder.edit.setOnClickListener {
////                activity.updateItem(list[p1])
////            }

            //MOVES ITEM
            holder.move.setOnTouchListener { v, event ->
                if(event.actionMasked== MotionEvent.ACTION_DOWN){
                    activity.touchHelper?.startDrag(holder)
                    //holder.move.background.colorFilter = ColorFi
                }
                false
            }
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val itemName: TextView = v.findViewById(R.id.cb_item)
            val checkedIndication: CardView = v.findViewById(R.id.checked_indication_background)
            val checkMark: ImageView = v.findViewById(R.id.check_mark)
            val itemNameUnchecked: TextView = v.findViewById(R.id.cb_item_unchecked)
        //    val edit: ImageView = v.findViewById(R.id.iv_edit)
            val delete: CardView = v.findViewById(R.id.cb_background)
            val move: CardView = v.findViewById(R.id.cb_background)
            val nudgeItem: Button = v.findViewById(R.id.nudgeId)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else
            super.onOptionsItemSelected(item)
    }


    //PART OF FULLSCREEN
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreen_content.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }
}
