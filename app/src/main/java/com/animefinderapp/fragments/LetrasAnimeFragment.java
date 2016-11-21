package com.animefinderapp.fragments;

import java.util.ArrayList;
import java.util.Arrays;

import com.animefinderapp.R;
import com.animefinderapp.adaptadores.AnimeAdapter;
import com.animefinderapp.adaptadores.LetrasAdapter;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.servicios.AnimeService;
import com.animefinderapp.utilidad.ServidorUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LetrasAnimeFragment extends Fragment {

    private String server;
    private String tipoLista;
    private SwipeRefreshLayout swipeRefreshLayoutLetras;
    private AnimeAdapter animeLetraAdapter;
    private RecyclerView recyclerViewLetras;
    private SharedPreferences sharedPref;
    private String letraSeleccionada;
    private int pagina;
    private boolean end;
    private boolean refreshing = false;
    private Context context;

    public LetrasAnimeFragment() {
        // Required empty public constructor
    }

   
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            context= getActivity();
            letraSeleccionada =  getArguments().getString("letra");
            ((AppCompatActivity) context).getSupportActionBar().setTitle("Letra:" + letraSeleccionada);
            PreferenceManager.setDefaultValues(context, R.xml.settings, false);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
            tipoLista = sharedPref.getString("pref_list_view", "lista").toLowerCase();
            swipeRefreshLayoutLetras = (SwipeRefreshLayout) view.findViewById(R.id.swipetorefresh);
            animeLetraAdapter = ServidorUtil.getAnimeAdapter(tipoLista, server, context);
            recyclerViewLetras = (RecyclerView) view.findViewById(R.id.lista);
            recyclerViewLetras.setHasFixedSize(true);
            recyclerViewLetras.setLayoutManager(ServidorUtil.getlayout(tipoLista, context));
            recyclerViewLetras.setAdapter(animeLetraAdapter);
            pagina = 1;
            end = false;
            swipeRefreshLayoutLetras.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    pagina = 1;
                    animeLetraAdapter.removeAll();
                    new BuscarAnimesLetra(server, letraSeleccionada).execute();
                }
            });
            recyclerViewLetras.addOnScrollListener(new OnScrollListener() {

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (!recyclerView.canScrollVertically(1) && !refreshing && !end) {
                        new BuscarAnimesLetra(server, letraSeleccionada).execute();
                    }
                }
            });
            new BuscarAnimesLetra(server, letraSeleccionada).execute();
        } catch (Exception e) {
            ServidorUtil.showMensageError(e, getView());
        }
    }

    private class BuscarAnimesLetra extends AsyncTask<String, Void, String> {

        private ArrayList<AnimeFavorito> lista = new ArrayList<AnimeFavorito>();
        private String server;
        private String cadena;

        public BuscarAnimesLetra(String server, String cadena) {
            this.lista = new ArrayList<AnimeFavorito>();
            this.server = server;
            this.cadena = cadena;
        }

        @Override
        protected void onPreExecute() {
            refreshing = true;
            ServidorUtil.refresh(swipeRefreshLayoutLetras, true);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                lista = ServidorUtil.buscarAnimeLetra(server, cadena, pagina, context);
            } catch (Exception e) {
                ServidorUtil.showMensageError(e, getView());
            }

            return "ok";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ServidorUtil.refresh(swipeRefreshLayoutLetras, false);
            refreshing = false;
            if (!lista.isEmpty()) {
                animeLetraAdapter.addAll(lista);
                pagina++;
            } else {
                end = true;
            }
        }
    }

}
