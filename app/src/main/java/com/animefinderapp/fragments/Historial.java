package com.animefinderapp.fragments;

import com.animefinderapp.R;
import com.animefinderapp.adaptadores.ProgramacionAdapter;
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

public class Historial extends Fragment {

    private ProgramacionAdapter historialAdapter;
    private SharedPreferences sharedPref;
    private String server;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private String tipoLista;
    private Context context;

    public Historial() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_historial, container, false);
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
            historialAdapter = ServidorUtil.getProgramacionAdapter(tipoLista, server, context);
            recyclerView = (RecyclerView) view.findViewById(R.id.historial);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(ServidorUtil.getlayout(tipoLista, context));
            recyclerView.setAdapter(historialAdapter);
            swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    historialAdapter.removeAll();
                    historialAdapter.addAll(AnimeDataSource.getHistorial(server, context));
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
            historialAdapter.removeAll();
            historialAdapter.addAll(AnimeDataSource.getHistorial(server, context));
        } catch (Exception e) {
            ServidorUtil.showMensageError(e, getView());
        }
        super.onViewCreated(view, savedInstanceState);
    }

}
