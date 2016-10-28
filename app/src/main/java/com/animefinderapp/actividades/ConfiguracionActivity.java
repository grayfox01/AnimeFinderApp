package com.animefinderapp.actividades;

import com.animefinderapp.R;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.fragments.Configuracion;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class ConfiguracionActivity extends AppCompatActivity {
    private AnimeDataSource source;
    private Toolbar toolbar;

    private Configuracion configuracion;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        source = new AnimeDataSource(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle args = new Bundle();
        args.putSerializable("source", source);
        configuracion = new Configuracion();
        configuracion.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.contenido, configuracion).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                restart();
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        restart();
    }

    public void restart() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
    @Override
    protected void onStart() {
        super.onStart();
        source.open();
    }

    @Override
    protected void onStop() {
        super.onStop();
        source.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        source.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        source.close();
    }

}
