package com.animefinderapp.servidores;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.animefinderapp.entidades.Anime;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.entidades.Capitulo;
import com.animefinderapp.entidades.Relacionado;
import com.animefinderapp.interfaces.IServidores;
import com.animefinderapp.utilidad.ServidorUtil;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.SocketTimeoutException;
import java.util.ArrayList;


public class Eianime implements IServidores {
    private static final Eianime eianime = new Eianime();

    private Eianime() {
    }

    public static Eianime getInstance() {
        return eianime;
    }


    @Override
    public Anime buscarInfoAnime(String urlAnime, Context context) {
        return null;
    }

    @Override
    public ArrayList<Relacionado> buscarRelacionados(String url, Context context) {
        return null;
    }


    @Override
    public ArrayList<Capitulo> buscarProgramacion(Context context) {
        ArrayList<Capitulo> lista = new ArrayList<>();
        Elements programacion = getProgramacion(context);
        try {
            for (Element element : programacion) {
                String titulo = element.select("div.animedt").text().substring(0, element.select("div.animedt").text().indexOf("-"));
                String urlEpisodio = element.select("a").attr("href");
                String imagen = element.select("img.home-cap").attr("src");
                Connection.Response response = Jsoup.connect(urlEpisodio).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
                        .userAgent("Mozilla/5.0").execute();
                Document doc = Jsoup.parse(response.body());
                String urlAnime = doc.select("div.cntnv a").get(1).attr("href");
                String tituloEpisodio = "Episodio "+ element.select("div.animedt").text().substring(element.select("div.animedt").text().indexOf("-"),element.select("div.animedt").text().length());
                Log.e("titulo: ", titulo);
                Log.e("imagen: ", imagen);
                Log.e("urlEpisodio: ", urlEpisodio);
                Log.e("urlAnime: ", urlAnime);
                Log.e("tituloEpisodio: ", tituloEpisodio);
                Capitulo capitulo = new Capitulo();
                Anime a = new Anime();
                a.setTitulo(titulo);
                a.setUrl(urlAnime);
                a.setImagen(imagen);
                capitulo.setAnime(a);
                capitulo.setUrl(urlAnime);
                capitulo.setTitulo(tituloEpisodio);
                lista.add(capitulo);
            }
        } catch (Exception t) {

        }
        return lista;
    }

    @Override
    public Elements getProgramacion(final Context context) {
        Elements programacion = null;
        String url = "http://elanimeonline.net/";
        try {
            Log.e("JSwa", "Connecting to [" + url + "]");
            Connection.Response response = Jsoup.connect(url).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
                    .userAgent("Mozilla/5.0").execute();
            Document doc = null;

            doc = Jsoup.parse(response.body());

            Log.e("JSwa", "Connected to [" + url + "]");
            if (doc.select("div.lstsradd").size() > 20) {
                programacion = new Elements();
                programacion.addAll(doc.select("div.lstsradd").subList(0, 20));
            } else {
                programacion = doc.select("div.lstsradd");
            }

        } catch (Exception t) {
            Log.e("Error", "getProgramacion: ", t);
        }
        return programacion;
    }

    @Override
    public ArrayList<AnimeFavorito> buscarAnime(String cadenaBusqueda, int pagina, Context context) {
        return null;
    }

    @Override
    public ArrayList<AnimeFavorito> buscarAnimeGenero(String cadenaBusqueda, int pagina, Context context) {
        return null;
    }

    @Override
    public ArrayList<AnimeFavorito> buscarAnimeLetra(String cadenaBusqueda, int pagina, Context context) {
        return null;
    }

    @Override
    public ArrayList<String[]> buscarGeneros(Context context) {
        return null;
    }
}
