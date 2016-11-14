package com.animefinderapp.fragments;

import com.animefinderapp.R;
import com.animefinderapp.adaptadores.AnimeVistoAdapter;
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

public class Vistos extends Fragment {
    private SharedPreferences sharedPref;
    private String server;
    private String tipoLista;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AnimeVistoAdapter vistosAdapter;
    private RecyclerView recyclerView;
    private Context context;

    public Vistos() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            context = getActivity();
            PreferenceManager.setDefaultValues(context, R.xml.settings, false);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
            tipoLista = sharedPref.getString("pref_list_view", "lista").toLowerCase();
            swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipetorefresh);
            vistosAdapter = ServidorUtil.getAnimeAdapter2(tipoLista, server, context);
            recyclerView = (RecyclerView) view.findViewById(R.id.lista);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(ServidorUtil.getlayout(tipoLista, context));
            recyclerView.setAdapter(vistosAdapter);
            swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    vistosAdapter.removeAll();
                    vistosAdapter.addAll(AnimeDataSource.getAllAnimeVisto(server,context));
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
            vistosAdapter.removeAll();
            vistosAdapter.addAll(AnimeDataSource.getAllAnimeVisto(server,context));
        } catch (Exception e) {
            ServidorUtil.showMensageError(e, getView());
        }
        super.onViewCreated(view, savedInstanceState);
    }
}
