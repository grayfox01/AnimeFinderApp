package com.animefinderapp.fragments;

import com.animefinderapp.R;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.utilidad.ServidorUtil;
import com.squareup.picasso.Picasso;

import android.content.Context;
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
    private LinearLayout LinearLayout1;
    private Context context;
    private AnimeFavorito animeFavorito;
    private String server;
    private AnimeDataSource source;
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
            source = (AnimeDataSource) getArguments().get("source");
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
            animeFavorito = new AnimeFavorito();
            imagen = (ImageView) view.findViewById(R.id.imagenAnime);
            titulo = (TextView) view.findViewById(R.id.tituloAnime);
            descripcion = (TextView) view.findViewById(R.id.descripcionAnime);
            favorito = (CheckBox) view.findViewById(R.id.favoritoAnime);
            visto = (CheckBox) view.findViewById(R.id.isVisto);
            datos = (LinearLayout) view.findViewById(R.id.datosAnime);
            relacionados = (LinearLayout) view.findViewById(R.id.relacionadosAnime);
            LinearLayout1 = (LinearLayout) view.findViewById(R.id.animeInfo);
            favorito.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (favorito.isChecked()) {
                        source.agregarFavorito(animeFavorito, server);
                        Snackbar.make(v, "Favorito Agregado", Snackbar.LENGTH_SHORT).show();
                    } else {
                        source.eliminarFavorito(animeFavorito, server);
                        Snackbar.make(v, "Favorito Eliminado", Snackbar.LENGTH_SHORT).show();
                    }

                }
            });
            visto.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (visto.isChecked()) {
                        source.agregarAnimeVisto(animeFavorito, server);
                        Snackbar.make(v, "Anime Agregado", Snackbar.LENGTH_SHORT).show();
                    } else {
                        source.eliminarAnimeVisto(animeFavorito, server);
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

    public void setImagen(String url) {
        Picasso.with(context).load(url).placeholder(R.drawable.loadimage).into(imagen);
    }

    public void setTitulo(String titulo) {
        this.titulo.setText(titulo);
    }

    public void setDescripcion(String descripcion) {
        this.descripcion.setText(descripcion);
    }

    public void setFavorito(boolean favorito) {
        this.favorito.setChecked(favorito);
    }

    public void setVisto(boolean visto) {
        this.visto.setChecked(visto);
    }

    public void addDatos(LinearLayout dato) {
        datos.addView(dato);
    }

    public void addRelacionados(LinearLayout relacionado) {
        if (relacionado.getChildCount() == 0) {
            TextView t = new TextView(context);
            t.setText("No hay informacion");
            relacionado.addView(t);

        }
        relacionados.addView(relacionado);
    }

    public void show() {
        LinearLayout1.setVisibility(LinearLayout.VISIBLE);
    }

}
