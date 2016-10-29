package com.animefinderapp.fragments;

import com.animefinderapp.R;
import com.animefinderapp.adaptadores.AnimeAdapter2;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.Anime;
import com.animefinderapp.utilidad.ServidorUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Vistos extends Fragment {
    private SharedPreferences sharedPref;
    private String server;
    private String tipoLista;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AnimeAdapter2 vistosAdapter;
    private RecyclerView recyclerView;
    private TextView totalVistos;
    private Context context;

    public Vistos() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vistos, container, false);
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
            totalVistos = (TextView) view.findViewById(R.id.totalVistos);
            recyclerView = (RecyclerView) view.findViewById(R.id.vistos);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(ServidorUtil.getlayout(tipoLista, context));
            recyclerView.setAdapter(vistosAdapter);
            swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    vistosAdapter.removeAll();
                    vistosAdapter.addAll(AnimeDataSource.getAllAnimeVisto(server,context));
                    if (vistosAdapter.getItemCount() == 0) {
                        totalVistos.setVisibility(TextView.GONE);
                    } else {
                        totalVistos.setText("Total:" + vistosAdapter.getItemCount());
                        totalVistos.setVisibility(TextView.VISIBLE);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
            vistosAdapter.removeAll();
            vistosAdapter.addAll(AnimeDataSource.getAllAnimeVisto(server,context));
            if (vistosAdapter.getItemCount() == 0) {
                totalVistos.setVisibility(TextView.GONE);
            } else {
                totalVistos.setText("Total:" + vistosAdapter.getItemCount());
                totalVistos.setVisibility(TextView.VISIBLE);
            }
            AdapterDataObserver adapterDataObserver = new AdapterDataObserver() {

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    if (vistosAdapter.getItemCount() == 0) {
                        totalVistos.setVisibility(TextView.GONE);
                    } else {
                        totalVistos.setText("Total:" + vistosAdapter.getItemCount());
                        totalVistos.setVisibility(TextView.VISIBLE);
                    }
                    super.onItemRangeChanged(positionStart, itemCount);
                }
            };
            vistosAdapter.registerAdapterDataObserver(adapterDataObserver);
        } catch (Exception e) {
            ServidorUtil.showMensageError(e, getView());
        }
        super.onViewCreated(view, savedInstanceState);
    }
}
