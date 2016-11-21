package com.animefinderapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.animefinderapp.R;
import com.animefinderapp.actividades.DownloadsActivity;
import com.animefinderapp.adaptadores.DescargasAdapter;
import com.animefinderapp.entidades.Descarga;
import com.animefinderapp.utilidad.ServidorUtil;

import java.util.ArrayList;

public class DescargaFragment extends Fragment {

    private Context context;
    private String server;
    private SharedPreferences sharedPref;
    private RecyclerView descarga;
    private DescargasAdapter adapter;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    public DescargaFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            context = getActivity();
            ((AppCompatActivity) (context)).getSupportActionBar().setTitle(R.string.title_activity_downloads);
            swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipetorefresh);
            descarga= (RecyclerView) view.findViewById(R.id.lista);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
            adapter = new DescargasAdapter(context,new ArrayList<Descarga>(), R.layout.lista_descargas_row);
            adapter.setOnItemClickListener(new DescargasAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(Descarga descarga) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("descarga", descarga);
                    DescargaListFragment descargaListFragment = new DescargaListFragment();
                    descargaListFragment.setArguments(bundle);
                    ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.contenido, descargaListFragment,"descarga")
                            .addToBackStack(null)
                            .commit();

                }
            });
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new BuscarDescargas().execute();
                }
            });
            layoutManager = new LinearLayoutManager(context);
            descarga.setHasFixedSize(true);
            descarga.setLayoutManager(layoutManager);
            descarga.setAdapter(adapter);
            new BuscarDescargas().execute();
        } catch (Exception e) {
            e.printStackTrace();
            ServidorUtil.showMensageError(e, getView());
        }
        super.onViewCreated(view, savedInstanceState);
    }


    private class BuscarDescargas extends AsyncTask<String, Void, String> {
        private ArrayList<Descarga> lista;

        public BuscarDescargas() {
            this.lista = new ArrayList<Descarga>();
        }

        @Override
        protected void onPreExecute() {
            ServidorUtil.refresh(swipeRefreshLayout, true);
            adapter.removeAll();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                lista = ServidorUtil.getDownloads(server, context);
            } catch (Exception e) {
                ServidorUtil.showMensageError(e, getView());
            }

            return "ok";
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            ServidorUtil.refresh(swipeRefreshLayout, false);
            if (s.equals("ok")) {
                adapter.addAll(lista);
            }
        }

        @Override
        protected void onCancelled() {
            Snackbar.make(descarga, "Busqueda cancelada!", Snackbar.LENGTH_SHORT).show();
        }
    }

}