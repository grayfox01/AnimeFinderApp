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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FavoritosFragment extends Fragment {
    private SharedPreferences sharedPref;
    private String server;
    private String tipoLista;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AnimeAdapter favoritoAdapter;
    private RecyclerView recyclerView;
    private Context context;
    private String titulo;

    public FavoritosFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            context= getActivity();
            PreferenceManager.setDefaultValues(context, R.xml.settings, false);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
            tipoLista = sharedPref.getString("pref_list_view", "lista").toLowerCase();
            swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipetorefresh);
            favoritoAdapter = ServidorUtil.getAnimeAdapter(tipoLista,server, context);
            recyclerView = (RecyclerView) view.findViewById(R.id.lista);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(ServidorUtil.getlayout(tipoLista, context));
            recyclerView.setAdapter(favoritoAdapter);
            swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    favoritoAdapter.removeAll();
                    favoritoAdapter.addAll(AnimeDataSource.getAllFavoritos(server,context));
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
            favoritoAdapter.removeAll();
            favoritoAdapter.addAll(AnimeDataSource.getAllFavoritos(server,context));
        } catch (Exception e) {
            ServidorUtil.showMensageError(e, getView());
        }
        super.onViewCreated(view, savedInstanceState);
    }

}
