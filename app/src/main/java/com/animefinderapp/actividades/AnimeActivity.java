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
    private String server;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private String url;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime);
        url = getIntent().getExtras().getString("url");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
        if (ServidorUtil.verificaConexion(this)) {
            new getAnime(url).execute();
        } else {
            Snackbar.make(getCurrentFocus(), "Sin conexion", Snackbar.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupViewPager(ViewPager viewPager, AnimeFavorito anime) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        bundle.putSerializable("animeFavorito", anime);
        InfoAnime infoAnime= new InfoAnime();
        infoAnime.setArguments(bundle);
        CapitulosAnime capitulosAnime= new CapitulosAnime();
        capitulosAnime.setArguments(bundle);
        adapter.addFragment(infoAnime, "Informacion");
        adapter.addFragment(capitulosAnime, "Capitulos");
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
    public boolean onOptionsItemSelected(MenuItem item) {

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
                toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(anime.getTitulo());
                viewPager = (ViewPager) findViewById(R.id.viewpager);
                setupViewPager(viewPager, new AnimeFavorito(anime));
                tabLayout = (TabLayout) findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(viewPager);
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
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}