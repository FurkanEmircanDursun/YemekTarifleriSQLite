package com.klu.inc.yemektariflerisqlite

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_tarif.*
import java.io.ByteArrayOutputStream
import java.lang.Exception


class TarifFragment : Fragment() {

    var secilenGorsel : Uri?=null
    var secilenBitmap: Bitmap?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageButton.setOnClickListener {

            gorselSec(it)
        }
        KaydetButton.setOnClickListener(){
            kaydet(it)

        }
        arguments?.let {

            var gelenBilgi = TarifFragmentArgs.fromBundle(it).bilgi

            if(gelenBilgi.equals("menudengeldim")){
                // yeni yemek eklicek
                    yemekMalzemeText.setText("")
                yemekMalzemeText.setText("")
                KaydetButton.visibility=View.VISIBLE
                val gorselSecmeArkaPlani=BitmapFactory.decodeResource(context?.resources,R.drawable.gorsel)
                imageButton.setImageBitmap(gorselSecmeArkaPlani)
            }
            else{
                // listeden yemek secti
                    // gorunmezlik
                KaydetButton.visibility=View.INVISIBLE

                val secilenId=TarifFragmentArgs.fromBundle(it).id

                context?.let {
                    try {

                        val db=it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                        val cursor = db.rawQuery("SELECT * FROM yemekler WHERE id=?", arrayOf(secilenId.toString()))

                        val yemekIsmiIndex=cursor.getColumnIndex("yemekismi")
                        val yemekMalzemeIndex=cursor.getColumnIndex("yemekmalzemesi")
                        val yemekGorseli=cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()){

                            yemekİsmiText.setText(cursor.getString(yemekIsmiIndex))
                            yemekMalzemeText.setText(cursor.getString(yemekMalzemeIndex))
                            val byteDizisi=cursor.getBlob(yemekGorseli)
                            val bitmap= BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                        imageButton.setImageBitmap(bitmap)
                        }
                        cursor.close()

                    }catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }

            }

        }
    }
    fun kaydet( view: View){
        //SQLİTE KAYDETME
        val yemekIsmi=yemekİsmiText.text.toString()
        val yemekMalzemeleri=yemekMalzemeText.text.toString()


        if(secilenBitmap!=null){
            val kucukbitmap=kucukBitmapOlustur(secilenBitmap!!,300)
            val outputStream= ByteArrayOutputStream()
            kucukbitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream )
            val byteDizisi=outputStream.toByteArray()
            try {

                context?.let {
                    val database =it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY, yemekismi VARCHAR, yemekmalzemesi VARCHAR, gorsel BLOB)")
                    // yaptığım hata 
                   // database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY),yemekismi VARCHAR ,yemekmalzemesi VARCHAR,gorsel BLOB")



                    val sqlString = "INSERT INTO yemekler (yemekismi, yemekmalzemesi, gorsel) VALUES (?, ?, ?)"
                    val statement= database.compileStatement(sqlString)
                    statement.bindString(1,yemekIsmi)
                    statement.bindString(2,yemekMalzemeleri)
                    statement.bindBlob(3,byteDizisi)
                    statement.execute()

                }

            }catch (e: Exception){
                e.printStackTrace()
            }
            val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)

        }

        // SQLite Kaydetme
    }
    fun gorselSec(view: View){
        activity?.let {
            if (ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                // izin verilmedi izin istememiz gerekiyor
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }
            else{
                // izin verilmiş, tekrar sormadan galeriye git
                    // galeri intenti
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)


            }


        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==1){
            if(grantResults.size> 0 &&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //izni aldık
                var  galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
        // activite çalışınca ne olcak
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==2 &&resultCode== Activity.RESULT_OK && data !=null){
            // request code 2 mi activitedece bi kaynak secildimi ve data var mı

        secilenGorsel= data.data
            // data uri aldık

            try {
                context?.let {  if(secilenGorsel!=null){
                    if(Build.VERSION.SDK_INT>=28){
                     val source=   ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                    secilenBitmap=ImageDecoder.decodeBitmap(source)
                        imageButton.setImageBitmap(secilenBitmap)
                    }
                    else{
                        secilenBitmap=MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                        imageButton.setImageBitmap(secilenBitmap)
                    }
                }


                }


            }catch (e: Exception)
            {
                e.printStackTrace()
            }



        }

        super.onActivityResult(requestCode, resultCode, data)
    }
    fun kucukBitmapOlustur(kullanicininSectigiBitmap :Bitmap,maxiumumBoyut: Int): Bitmap{

        var width=kullanicininSectigiBitmap.width
        var height=kullanicininSectigiBitmap.height

        val bitmapOrani: Double=width.toDouble()/height.toDouble()

        if(bitmapOrani>1){
            //gorselimiz yatay

            width= maxiumumBoyut
            val kisaltilmisHeight=width/bitmapOrani
            height=kisaltilmisHeight.toInt()
        }
        else{
            // görselimiz dikey
            height=maxiumumBoyut
            val kisaltilmisWidth= height*bitmapOrani
            width=kisaltilmisWidth.toInt()
        }
        return  Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }
}