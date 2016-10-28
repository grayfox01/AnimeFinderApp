package com.animefinderapp.actividades;

import java.util.ArrayList;
import java.util.List;

import com.animefinderapp.R;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.Anime;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.entidades.Relacionado;
import com.animefinderapp.fragments.CapitulosAnime;
import com.animefinderapp.fragments.InfoAnime;
import com.animefinderapp.utilidad.ServidorUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AnimeActivity extends AppCompatActivity {
    private AnimeFavorito animeFavorito;
    private String server;
    private AnimeDataSource source;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private InfoAnime infoFragment;
    private CapitulosAnime capitulosFragment;
    private String url;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime);
        url = getIntent().getExtras().getString("url");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
        source = new AnimeDataSource(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().hide();
        animeFavorito = new AnimeFavorito();
        Bundle args = new Bundle();
        args.putSerializable("source", source);
        infoFragment = new InfoAnime();
        infoFragment.setArguments(args);
        capitulosFragment = new CapitulosAnime();
        capitulosFragment.setArguments(args);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager, new Anime());
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setVisibility(TabLayout.GONE);
        if (ServidorUtil.verificaConexion(this)) {
            new getAnime(url).execute();
        } else {
            Snackbar.make(toolbar, "Sin conexion", Snackbar.LENGTH_SHORT).show();
            finish();
        }

    }

    private void setupViewPager(ViewPager viewPager, Anime anime) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(infoFragment, "Informacion");
        adapter.addFragment(capitulosFragment, "Capitulos");
        ;
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
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

    private class getAnime extends AsyncTask<String, Void, String> {
        private ProgressDialog pDialog;
        private String url;
        private Anime anime;

        public getAnime(String url) {
            this.url = url;
            this.anime = new Anime();
        }

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(AnimeActivity.this);
            pDialog.setIndeterminate(false);
            pDialog.setMessage("Obteniendo Informacion...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                anime = ServidorUtil.buscarInfoAnime(server, url, AnimeActivity.this);
            } catch (Exception e) {
                ServidorUtil.showMensageError(e, toolbar);
            }
            return "ok";
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                infoFragment.setTitulo(anime.getTitulo());
                infoFragment.setImagen(anime.getImagen());
                infoFragment.setDescripcion(anime.getDescripcion());
                LinearLayout datosl = new LinearLayout(AnimeActivity.this);
                datosl.setOrientation(LinearLayout.VERTICAL);
                for (String dato : anime.getDatos()) {
                    TextView datos = new TextView(AnimeActivity.this);
                    datos.setText(dato);
                    datosl.addView(datos);
                }
                infoFragment.addDatos(datosl);
                LinearLayout relacionadosl = new LinearLayout(AnimeActivity.this);
                relacionadosl.setOrientation(LinearLayout.VERTICAL);
                for (final Relacionado relacionados : anime.getRelacionados()) {
                    LinearLayout relacionado = new LinearLayout(AnimeActivity.this);
                    TextView tipo = new TextView(AnimeActivity.this);
                    tipo.setText(relacionados.getTipo());
                    TextView nombre = new TextView(AnimeActivity.this);
                    nombre.setText(relacionados.getAnime().getTitulo());
                    nombre.setTextColor(getResources().getColor(R.color.colorPrimary));
                    nombre.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(AnimeActivity.this, AnimeActivity.class);
                            i.putExtra("url", relacionados.getAnime().getUrl());
                            AnimeActivity.this.startActivity(i);
                        }
                    });
                    relacionado.addView(tipo);
                    relacionado.addView(nombre);
                    relacionadosl.addView(relacionado);
                }
                infoFragment.addRelacionados(relacionadosl);
                AnimeFavorito a = new AnimeFavorito(anime, null);
                animeFavorito = a;
                infoFragment.setFavorito(source.getAllFavoritos(server).contains(a));
                infoFragment.setVisto(source.getAllAnimeVisto(server).contains(a));
                capitulosFragment.addCapitulos(anime.getCapitulos());
                infoFragment.show();
                getSupportActionBar().show();
                getSupportActionBar().setTitle(anime.getTitulo());
                tabLayout.setVisibility(TabLayout.VISIBLE);
            } catch (Exception e) {
                ServidorUtil.showMensageError(e, toolbar);
                finish();
            }
            pDialog.dismiss();
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