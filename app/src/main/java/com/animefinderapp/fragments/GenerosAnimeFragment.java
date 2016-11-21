package com.animefinderapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.animefinderapp.R;
import com.animefinderapp.adaptadores.AnimeAdapter;
import com.animefinderapp.adaptadores.GenerosAdapter;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.utilidad.ServidorUtil;

import java.util.ArrayList;

public class GenerosAnimeFragment extends Fragment {
    private GenerosAdapter generosAdapter;
    private RecyclerView recyclerViewGeneros;
    private SharedPreferences sharedPref;
    private String tipoLista;
    private String server;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private boolean refreshing = false;
    private int pagina;
    private boolean end;
    private String[] generoSeleccionado;
    private AnimeAdapter animeGeneroAdapter;
    private String titulo;


    public GenerosAnimeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            context = getActivity();
            titulo = getArguments().getString("titulo");
            ((AppCompatActivity) (context)).getSupportActionBar().setTitle("Genero:"+titulo);
            PreferenceManager.setDefaultValues(context, R.xml.settings, false);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
            tipoLista = sharedPref.getString("pref_list_view", "lista").toLowerCase();
            swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipetorefresh);
            generosAdapter = ServidorUtil.getGenerosAdapter();
            recyclerViewGeneros = (RecyclerView) view.findViewById(R.id.lista);
            recyclerViewGeneros.setHasFixedSize(true);
            recyclerViewGeneros.setLayoutManager(ServidorUtil.getlayout(tipoLista, context));
            recyclerViewGeneros.setAdapter(generosAdapter);
            swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    generosAdapter.removeAll();
                    new BuscarAnimesGenero(server,titulo).execute();
                }
            });
            generosAdapter.removeAll();
            new BuscarAnimesGenero(server,titulo).execute();
        } catch (Exception e) {
            ServidorUtil.showMensageError(e, getView());
        }
    }

    private class BuscarAnimesGenero extends AsyncTask<String, Void, String> {

        private ArrayList<AnimeFavorito> lista = new ArrayList<AnimeFavorito>();
        private String server;
        private String cadena;

        public BuscarAnimesGenero(String server, String cadena) {
            this.lista = new ArrayList<AnimeFavorito>();
            this.server = server;
            this.cadena = cadena;
        }

        @Override
        protected void onPreExecute() {
            refreshing = true;
            ServidorUtil.refresh(swipeRefreshLayout, true);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                lista = ServidorUtil.buscarAnimeGenero(server, cadena, pagina, context);
            } catch (Exception e) {
                ServidorUtil.showMensageError(e, getView());
            }

            return "ok";
        }

        @Override
        protected void onPostExecute(String s) {
            ServidorUtil.refresh(swipeRefreshLayout, false);
            refreshing = false;
            if (s.equals("ok")) {
                if (!lista.isEmpty()) {
                    animeGeneroAdapter.addAll(lista);
                    pagina++;
                } else {
                    end = true;
                }
            }
        }
    }
}
