package com.example.mehmetsabir.memorygamesforsmartalarm

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import java.util.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import android.view.ViewGroup.LayoutParams.FILL_PARENT

class MainActivity : AppCompatActivity() {

    private var ROW_COUNT = -1
    private var COL_COUNT = -1
    private var context: Context? = null
    private var backImage: Drawable? = null
    private var cards: Array<IntArray>? = null
    private var size: Int = 0
    private var images: MutableList<Drawable>? = null
    private var firstCard: Cards? = null
    private var seconedCard: Cards? = null
    private var buttonListener: ButtonListener? = null
    private var list: ArrayList<Int>? = null
    private var counter = 0
    private var radioGroup : RadioGroup? = null
    private var btnOk :  Button? = null
    private var btnCancel :  Button? = null
    private val lock = Any()
    private var scale : Float? = null
    private var p : android.widget.TableRow.LayoutParams? = null
    private var turns: Int = 0
    private var imgBtn : Button? = null
    private var mainTable: TableLayout? = null
    private var handler: UpdateCardsHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        openDialogScreen()

    }

    private fun openDialogScreen() {


        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.alertlayout, null)

        radioGroup = view.findViewById(R.id.rbtnGrpLevel)
        btnOk = view.findViewById(R.id.btnOkay)
        btnCancel = view.findViewById(R.id.btnCancel)

        val alert = AlertDialog.Builder(this)
        alert.setView(view)
        alert.setCancelable(false)
        val dialog = alert.create()

        btnOk?.setOnClickListener {

            changeLevel(radioGroup?.checkedRadioButtonId!!)

            dialog.cancel()
        }
        btnCancel?.setOnClickListener { dialog.cancel() }


        dialog.show()

    }

    private fun init() {

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        handler  = UpdateCardsHandler()

        loadImages()

        buttonListener = ButtonListener()

        mainTable = this.findViewById(R.id.TableLayout03) as TableLayout

        context = mainTable?.context

        backImage = resources.getDrawable(R.drawable.card_empty)

    }

    private fun changeLevel(position : Int) {

            var x: Int = 0
            var y: Int = 0

            counter = 0

        when (position) {

            R.id.rbEasy -> {
                x = 2
                y = 2
            }
            R.id.rbMedium ->{
                x = 2
                y = 4
            }
            R.id.rbHard ->{
                x = 3
                y = 4
            }
            else ->  Toast.makeText(this@MainActivity,"Seçilmedi",Toast.LENGTH_LONG).show()
        }
            newGame(x, y)
    }

    private fun newGame(c: Int, r: Int) {

        ROW_COUNT = r
        COL_COUNT = c

        cards = Array(COL_COUNT) { IntArray(ROW_COUNT) }


        val tr = findViewById<TableRow>(R.id.TableRow03)
        tr.removeAllViews()

        mainTable = TableLayout(context)
        tr.addView(mainTable)

        for (y in 0 until ROW_COUNT) {
            mainTable?.addView(createRow(y))
        }

        firstCard = null
        loadCards()

        turns = 0

        //
       // (findViewById<TextView>(R.id.tv1)).text = "Tries:  $turns"


    }

    inner class ButtonListener : View.OnClickListener {

        override fun onClick(v: View) {

            synchronized(lock) {
                if (firstCard != null && seconedCard != null) {
                    return
                }
                val id = v.id
                val x = id / 100
                val y = id % 100
                turnCard(v as Button, x, y)
            }

        }

        private fun turnCard(button: Button, x: Int, y: Int) {
            button.setBackgroundDrawable(images?.get(cards!![x][y]))

            if (firstCard == null) {
                firstCard = Cards(button, x, y)
            } else {

                if (firstCard?.x == x && firstCard?.y == y) {

                    return

                }

                seconedCard = Cards(button, x, y)

                turns++
              //  (findViewById<TextView>(R.id.tv1)).text = "$turns"


                val tt = object : TimerTask() {

                    override fun run() {
                        try {
                            synchronized(lock) {
                                handler?.sendEmptyMessage(0)
                            }
                        } catch (e: Exception) {
                            Log.e("E1", e.message)
                        }

                    }
                }

                val t = Timer(false)
                t.schedule(tt, 1300)
            }


        }

    }

    private fun loadCards() {
        try {
            size = ROW_COUNT * COL_COUNT

            Log.i("loadCards()", "size=$size")

            list = ArrayList()

            for (i in 0 until size) {
                list?.add(i)
            }


            val r = Random()

            for (i in size - 1 downTo 0) {
                var t = 0

                if (i > 0) {
                    t = r.nextInt(i)
                }

                t = list!!.removeAt(t)
                cards!![i % COL_COUNT][i / COL_COUNT] = t % (size / 2)

                Log.i("loadCards()", "card[" + i % COL_COUNT +
                        "][" + i / COL_COUNT + "]=" + cards!![i % COL_COUNT][i / COL_COUNT])
            }
        } catch (e: Exception) {
            Log.e("loadCards()", e.toString() + "")
        }

    }

    private fun createRow(y: Int): TableRow {
        val row = TableRow(context)
        row.setHorizontalGravity(Gravity.CENTER)

        for (x in 0 until COL_COUNT) {
            row.addView(createImageButton(x, y))


        }
        return row
    }

    private fun createImageButton(x: Int, y: Int): View {
        p = android.widget.TableRow.LayoutParams();
        imgBtn= Button(context)
        imgBtn?.setBackgroundDrawable(backImage)
        imgBtn?.id = 100 * x + y
        imgBtn?.setOnClickListener(buttonListener)
        p?.rightMargin = dpToPixel(10, context!!)
        p?.bottomMargin = dpToPixel(10, context!!) // right-margin = 10dp
        imgBtn?.layoutParams = p;
        return imgBtn!!


    }

    private fun dpToPixel(dp : Int, context :Context ) : Int {
        if (scale == null)
            scale = context.getResources().displayMetrics.density;
        return (dp*scale!!).toInt()
    }

    private fun loadImages() {

        images = ArrayList()

        images?.add(resources.getDrawable(R.drawable.card1))
        images?.add(resources.getDrawable(R.drawable.card2))
        images?.add(resources.getDrawable(R.drawable.card3))
        images?.add(resources.getDrawable(R.drawable.card4))
        images?.add(resources.getDrawable(R.drawable.card5))
        images?.add(resources.getDrawable(R.drawable.card6))

    }

    inner class UpdateCardsHandler : Handler() {

        override fun handleMessage(msg: Message) {
            synchronized(lock) {
                checkCards()
            }
        }

        fun checkCards() {
            if (cards!![seconedCard!!.x][seconedCard!!.y] == cards!![firstCard!!.x][firstCard!!.y]) {
                counter++
                firstCard!!.button.visibility = View.INVISIBLE
                seconedCard!!.button.visibility = View.INVISIBLE

                if (size / 2 == counter) {

                    Toast.makeText(this@MainActivity, "Bitti", Toast.LENGTH_LONG).show()
                    init()
                    openDialogScreen()


                }


            } else {
                seconedCard?.button?.setBackgroundDrawable(backImage)
                firstCard?.button?.setBackgroundDrawable(backImage)
            }

            firstCard = null
            seconedCard = null
        }
    }


}
