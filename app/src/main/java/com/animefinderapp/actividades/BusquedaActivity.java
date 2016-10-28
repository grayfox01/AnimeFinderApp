package com.animefinderapp.actividades;

import java.util.ArrayList;

import com.animefinderapp.R;
import com.animefinderapp.adaptadores.AnimeAdapter;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.utilidad.ServidorUtil;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BusquedaActivity extends AppCompatActivity {

    private int pagina;
    private String server;
    private String cadenaBusqueda;
    private String tipoLista;
    private RelativeLayout relativeLayout;
    private RecyclerView rv;
    private AnimeAdapter animeAdapter;
    private AnimeDataSource source;
    private TextView emptyView;
    private Toolbar toolbar;
    private boolean end = false;
    private boolean refreshing = false;
    private boolean mostrarVistos;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busqueda);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
        mostrarVistos = sharedPref.getBoolean("mostrarVistos", true);
        source = new AnimeDataSource(this);
        tipoLista = sharedPref.getString("pref_list_view", "grid").toLowerCase();
        pagina = 1;
        relativeLayout = (RelativeLayout) findViewById(R.id.contenido);
        cadenaBusqueda = getIntent().getExtras().getString("cadenaBusqueda");
        getSupportActionBar().setTitle("Buscando:" + cadenaBusqueda);
        emptyView = new TextView(this);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setText("No hay resultados que mostrar");
        rv = new RecyclerView(this);
        relativeLayout.addView(rv);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (ServidorUtil.verificaConexion(BusquedaActivity.this)) {
            animeAdapter = ServidorUtil.getAnimeAdapter(tipoLista, source, server, false, mostrarVistos, this);
            rv.setLayoutManager(ServidorUtil.getlayout(tipoLista, this));
            rv.setAdapter(animeAdapter);
            rv.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!recyclerView.canScrollVertically(1) && !refreshing && !end) {
                        new BuscarAnime(server, cadenaBusqueda).execute();
                    }
                }
            });

            new BuscarAnime(server, cadenaBusqueda).execute();

        } else

        {
            Snackbar.make(toolbar, "No hay conexion a internet", Snackbar.LENGTH_SHORT).show();

            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.busqueda, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private class BuscarAnime extends AsyncTask<String, Void, String> {

        private ProgressDialog pDialog;
        ArrayList<AnimeFavorito> listaBusqueda;
        private String server;
        private String cadena;

        public BuscarAnime(String server, String cadena) {
            listaBusqueda = new ArrayList<AnimeFavorito>();
            this.server = server;
            this.cadena = cadena;
        }

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(BusquedaActivity.this);
            pDialog.setIndeterminate(false);
            pDialog.setMessage("Cargando...");
            pDialog.setCancelable(false);
            pDialog.show();
            refreshing = true;

        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                listaBusqueda = ServidorUtil.buscarAnime(server, cadena, pagina, BusquedaActivity.this);
            } catch (Exception e) {
                ServidorUtil.showMensageError(e, toolbar);
            }

            return "ok";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            refreshing = false;
            if (s.equals("ok")) {
                if (listaBusqueda.isEmpty() && animeAdapter.getItemCount() == 0) {
                    Toast.makeText(BusquedaActivity.this, "No se han encontrado animes", Toast.LENGTH_SHORT).show();
                    finish();
                }
                if (!listaBusqueda.isEmpty()) {
                    animeAdapter.addAll(listaBusqueda);
                    pagina++;
                }
                if (listaBusqueda.isEmpty()) {
                    end = true;
                }
            }
        }
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
