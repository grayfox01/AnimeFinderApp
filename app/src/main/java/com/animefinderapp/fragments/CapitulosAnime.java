package com.animefinderapp.fragments;

import java.util.ArrayList;

import com.animefinderapp.R;
import com.animefinderapp.adaptadores.CapitulosAdapter;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.entidades.Capitulo;
import com.animefinderapp.utilidad.ServidorUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class CapitulosAnime extends Fragment {
    private Context context;
    private String server;
    private SharedPreferences sharedPref;
    private RecyclerView capitulos;
    private CapitulosAdapter adapter;
    private RelativeLayout capitulosl;
    private FloatingActionButton buttonSubirBajar;
    private LinearLayoutManager layoutManager;
    private AnimeFavorito animeFavorito;

    public CapitulosAnime() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_capitulos_anime, container, false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            context = getActivity();
            animeFavorito= (AnimeFavorito) getArguments().getSerializable("animeFavorito");
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
            adapter = new CapitulosAdapter(context, R.layout.lista_capitulos_row, new ArrayList<Capitulo>(), server);
            layoutManager = new LinearLayoutManager(context);
            buttonSubirBajar = (FloatingActionButton) view.findViewById(R.id.buttonFloating);
            capitulosl = (RelativeLayout) view.findViewById(R.id.capitulosAnime);
            capitulos = new RecyclerView(context);
            buttonSubirBajar.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back_black));
            capitulos.setHasFixedSize(true);
            capitulos.setLayoutManager(layoutManager);
            capitulos.setAdapter(adapter);
            buttonSubirBajar.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (v.getRotation() < 0) {
                        layoutManager.scrollToPositionWithOffset(layoutManager.getItemCount() - 1, 0);
                        buttonSubirBajar.setRotation(-90);
                    } else {
                        layoutManager.scrollToPositionWithOffset(0, 0);
                        buttonSubirBajar.setRotation(90);
                    }

                }
            });
            capitulos.addOnScrollListener(new OnScrollListener() {

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (!capitulos.canScrollVertically(1)) {
                        buttonSubirBajar.setRotation(90);
                    } else if (!capitulos.canScrollVertically(-1)) {
                        buttonSubirBajar.setRotation(-90);
                    }
                    super.onScrolled(recyclerView, dx, dy);
                }

            });
            capitulosl.addView(capitulos);
            adapter.addAll(animeFavorito.getAnime().getCapitulos());
        } catch (Exception e) {
            e.printStackTrace();
            ServidorUtil.showMensageError(e, getView());
        }
        super.onViewCreated(view, savedInstanceState);
    }


}