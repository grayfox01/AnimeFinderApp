package com.animefinderapp.interfaces;

import java.util.ArrayList;

import org.jsoup.select.Elements;

import com.animefinderapp.entidades.Anime;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.entidades.Capitulo;
import com.animefinderapp.entidades.Relacionado;

import android.app.ProgressDialog;
import android.content.Context;

public interface IServidores {

	public Anime buscarInfoAnime(String urlAnime,
			final Context context);

	public ArrayList<Relacionado> buscarRelacionados(String url,
			final Context context);
	
	public ArrayList<Capitulo> buscarProgramacion(
			final Context context);

	public Elements getProgramacion(final Context context);

	public ArrayList<AnimeFavorito> buscarAnime(String cadenaBusqueda,
			int pagina, final Context context);

	public ArrayList<AnimeFavorito> buscarAnimeGenero(String cadenaBusqueda,
			int pagina, final Context context);

	public ArrayList<AnimeFavorito> buscarAnimeLetra(String cadenaBusqueda,
			int pagina, final Context context);

	public ArrayList<String[]> buscarGeneros(final Context context);
	
	public class a{};
}
