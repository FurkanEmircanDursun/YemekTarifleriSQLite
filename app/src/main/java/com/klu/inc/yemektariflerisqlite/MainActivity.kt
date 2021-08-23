package com.klu.inc.yemektariflerisqlite

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.Navigation

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    // menu yaratmak
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // menu baglamak

        val menuInflater=menuInflater
        menuInflater.inflate(R.menu.yemek_ekle,menu)
        return super.onCreateOptionsMenu(menu)
    }
        // menuden item seçilirse
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

      // item kontrol etmek
        if (item.itemId==R.id.yemek_ekleme_item){
            // nav aksiyonu

            val action=ListeFragmentDirections.actionListeFragmentToTarifFragment("menudengeldim",0)
            // nav aksiyonunu bulup bağlama
            Navigation.findNavController(this,R.id.fragmentContainerView).navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }


}