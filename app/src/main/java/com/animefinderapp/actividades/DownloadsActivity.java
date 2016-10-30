package com.animefinderapp.actividades;

import java.util.ArrayList;

import com.animefinderapp.R;
import com.animefinderapp.adaptadores.DescargasAdapter;
import com.animefinderapp.adaptadores.VideosAdapter;
import com.animefinderapp.entidades.Descarga;
import com.animefinderapp.utilidad.ServidorUtil;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

public class DownloadsActivity extends AppCompatActivity {

	private ArrayList<Descarga> descargas;
	private String servidor;
	private DescargasAdapter adapterDescargas;
	private VideosAdapter videosAdapter;
	private RecyclerView rv;
	private LinearLayoutManager llm;
	private RelativeLayout contenidoLayout;
	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downloads);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		servidor = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		contenidoLayout = (RelativeLayout) findViewById(R.id.contenido);
		rv = new RecyclerView(this);
		contenidoLayout.addView(rv);
		rv.setHasFixedSize(true);
		llm = new LinearLayoutManager(this);
		rv.setLayoutManager(llm);

		init();
	}

	private void init() {
		try {
			descargas = ServidorUtil.getDownloads(servidor, this);
			toolbar.setTitle("Descargas");
			adapterDescargas = new DescargasAdapter(descargas, R.layout.lista_descargas_row);
			adapterDescargas.setOnItemClickListener(new DescargasAdapter.OnItemClickListener() {

				@Override
				public void onItemClick(Descarga descarga) {
					rv.setHasFixedSize(true);
					rv.setLayoutManager(llm);
					toolbar.setTitle(descarga.getFileName());
					videosAdapter = new VideosAdapter(DownloadsActivity.this, descarga.getVideos(),
							R.layout.lista_descargas_videos_row);
					rv.setAdapter(videosAdapter);
				}
			});
			rv.setAdapter(adapterDescargas);

		} catch (Exception e) {
			ServidorUtil.showMensageError(e, toolbar);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			if (getSupportActionBar().getTitle().equals("Descargas")) {
				finish();
			} else {
				init();
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

}
