package com.animefinderapp.fragments;

import com.animefinderapp.R;
import com.animefinderapp.actividades.AnimeActivity;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.Anime;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.entidades.Relacionado;
import com.animefinderapp.utilidad.ServidorUtil;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InfoAnime extends Fragment {
    private ImageView imagen;
    private TextView titulo;
    private TextView descripcion;
    private CheckBox favorito;
    private CheckBox visto;
    private LinearLayout datos;
    private LinearLayout relacionados;
    private Context context;
    private AnimeFavorito animeFavorito;
    private String server;
    private SharedPreferences sharedPref;

    public InfoAnime() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_anime, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            context = getContext();
            animeFavorito= (AnimeFavorito) getArguments().getSerializable("animeFavorito");
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
            imagen = (ImageView) view.findViewById(R.id.imagenAnime);
            titulo = (TextView) view.findViewById(R.id.tituloAnime);
            descripcion = (TextView) view.findViewById(R.id.descripcionAnime);
            favorito = (CheckBox) view.findViewById(R.id.favoritoAnime);
            visto = (CheckBox) view.findViewById(R.id.isVisto);
            datos = (LinearLayout) view.findViewById(R.id.datosAnime);
            relacionados = (LinearLayout) view.findViewById(R.id.relacionadosAnime);
            titulo.setText(animeFavorito.getAnime().getTitulo());
            Picasso.with(context).load(animeFavorito.getAnime().getImagen()).placeholder(R.drawable.loadimage).into(imagen);
            descripcion.setText(animeFavorito.getAnime().getDescripcion());
            LinearLayout datosl = new LinearLayout(context);
            datosl.setOrientation(LinearLayout.VERTICAL);
            for (String dato : animeFavorito.getAnime().getDatos()) {
                TextView datos = new TextView(context);
                datos.setText(dato);
                datosl.addView(datos);
            }
            datos.addView(datosl);
            LinearLayout relacionadosl = new LinearLayout(context);
            relacionadosl.setOrientation(LinearLayout.VERTICAL);
            for (final Relacionado relacionados : animeFavorito.getAnime().getRelacionados()) {
                LinearLayout relacionado = new LinearLayout(context);
                TextView tipo = new TextView(context);
                tipo.setText(relacionados.getTipo());
                TextView nombre = new TextView(context);
                nombre.setText(relacionados.getAnime().getTitulo());
                nombre.setTextColor(getResources().getColor(R.color.colorPrimary));
                nombre.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, AnimeActivity.class);
                        i.putExtra("url", relacionados.getAnime().getUrl());
                        context.startActivity(i);
                    }
                });
                relacionado.addView(tipo);
                relacionado.addView(nombre);
                relacionadosl.addView(relacionado);
            }
            if (relacionadosl.getChildCount() == 0) {
                TextView t = new TextView(context);
                t.setText("No hay informacion");
                relacionados.addView(t);
            }else {
                relacionados.addView(relacionadosl);
            }
            favorito.setChecked(AnimeDataSource.getAllFavoritos(server, context).contains(animeFavorito));
            visto.setChecked(AnimeDataSource.getAllAnimeVisto(server, context).contains(animeFavorito));
            favorito.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (favorito.isChecked()) {
                        AnimeDataSource.agregarFavorito(animeFavorito, server,context);
                        Snackbar.make(v, "Favorito Agregado", Snackbar.LENGTH_SHORT).show();
                    } else {
                        AnimeDataSource.eliminarFavorito(animeFavorito, server,context);
                        Snackbar.make(v, "Favorito Eliminado", Snackbar.LENGTH_SHORT).show();
                    }

                }
            });
            visto.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (visto.isChecked()) {
                        AnimeDataSource.agregarAnimeVisto(animeFavorito, server,context);
                        Snackbar.make(v, "Anime Agregado", Snackbar.LENGTH_SHORT).show();
                    } else {
                        AnimeDataSource.eliminarAnimeVisto(animeFavorito, server,context);
                        Snackbar.make(v, "Anime Eliminado", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            ServidorUtil.showMensageError(e, getView());
            e.printStackTrace();
        }
        super.onViewCreated(view, savedInstanceState);
    }
}
