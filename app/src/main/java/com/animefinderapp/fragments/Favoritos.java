package com.animefinderapp.fragments;

import com.animefinderapp.R;
import com.animefinderapp.adaptadores.AnimeAdapter;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.utilidad.ServidorUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Favoritos extends Fragment {
    private SharedPreferences sharedPref;
    private String server;
    private String tipoLista;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AnimeAdapter favoritoAdapter;
    private RecyclerView recyclerView;
    private AnimeDataSource source;
    private boolean mostrarVistos;
    private Context context;

    public Favoritos() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favoritos, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            context= getActivity();
            source = (AnimeDataSource) getArguments().get("source");
            PreferenceManager.setDefaultValues(context, R.xml.settings, false);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
            mostrarVistos = sharedPref.getBoolean("mostrarVistos", true);
            tipoLista = sharedPref.getString("pref_list_view", "lista").toLowerCase();
            swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipetorefresh);
            favoritoAdapter = ServidorUtil.getAnimeAdapter(tipoLista,source, server, false, mostrarVistos, context);
            recyclerView = (RecyclerView) view.findViewById(R.id.favoritos);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(ServidorUtil.getlayout(tipoLista, context));
            recyclerView.setAdapter(favoritoAdapter);
            swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    favoritoAdapter.removeAll();
                    favoritoAdapter.addAll(source.getAllFavoritos(server));
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
            favoritoAdapter.removeAll();
            favoritoAdapter.addAll(source.getAllFavoritos(server));
        } catch (Exception e) {
            ServidorUtil.showMensageError(e, getView());
        }
        super.onViewCreated(view, savedInstanceState);
    }

}
