package com.animefinderapp.servidores;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.animefinderapp.entidades.Anime;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.entidades.Capitulo;
import com.animefinderapp.entidades.Relacionado;
import com.animefinderapp.interfaces.IServidores;
import com.animefinderapp.utilidad.ServidorUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class AnimeFlv implements IServidores {

	private static final AnimeFlv animeFlv = new AnimeFlv();

	private AnimeFlv() {
	}

	public static AnimeFlv getInstance() {

		return animeFlv;
	}

	public Anime buscarInfoAnime(String urlAnime, final Context context) {
		Anime anime = new Anime();
		Response response;
		Document doc;
		try {

			Log.e("buscarAnimeConexion", "Connecting to [" + urlAnime + "]");
			response = Jsoup.connect(urlAnime).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
					.userAgent("Mozilla/5.0").execute();
			doc = null;
			if (response.statusCode() >= 500 && response.statusCode() <= 511) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Error en el servidor");
						toast.show();
					}
				});
			} else if (response.statusCode() >= 400 && response.statusCode() <= 450) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Error en el cliente");
						toast.show();
					}
				});
			} else if (response.statusCode() == 200) {
				doc = Jsoup.parse(response.body());
				Log.e("buscarAnimeConexion", "Connected to [" + urlAnime + "]");
                anime.setUrl(urlAnime);
				Elements topicList;
				Elements imgElement = doc.select("div.anime_info");
				String imageUrl = imgElement.select("img").attr("src");
				anime.setImagen(imageUrl);
				Elements datosElement = doc.select("ul.ainfo li");
				ArrayList<String> datos = new ArrayList<>();
				for (Element element : datosElement) {
					datos.add(new String(element.text().getBytes(), "UTF-8").toString());
				}
				anime.setDatos(datos);
				topicList = doc.select("div.anime_cont h1");
				String titulo = topicList.text();
				anime.setTitulo(titulo);
				topicList = doc.select("div.sinopsis");
				String descripcion = topicList.text();
				anime.setDescripcion(descripcion);
				topicList = doc.select("ul#listado_epis.anime_episodios li");
				ArrayList<Capitulo> capitulos = new ArrayList<>();
				for (Element capitulo : topicList) {
					Capitulo a = new Capitulo(anime, capitulo.text(), "http://www.animeflv.net" + capitulo.select("a").attr("href"));
					capitulos.add(a);
				}
				anime.setCapitulos(capitulos);
				ArrayList<Relacionado> relacionados = new ArrayList<>();
				topicList = doc.select("div.relacionados li");
				for (Element element : topicList) {
					Anime relacionado = new Anime();
					relacionado.setUrl("http://www.animeflv.net" + element.select("a").attr("href"));
					relacionado.setTitulo(element.select("a").text());
					Relacionado r = new Relacionado(element.select("b").text() + ": ", relacionado);
					relacionados.add(r);
				}
				anime.setRelacionados(relacionados);
			}

		} catch (IOException t) {
			if (t.getClass().equals(SocketTimeoutException.class)) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Tiempo de conexion agotado");
						toast.show();
					}
				});
			} else {
				t.printStackTrace();
				Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
						Toast.LENGTH_SHORT).show();
				ServidorUtil.appendLog(t.getMessage());
			}
		}
		return anime;
	}

	
	public ArrayList<Capitulo> buscarProgramacion(final Context context) {
		ArrayList<Capitulo> lista = new ArrayList<Capitulo>();
		String url;
		url = "http://www.animeflv.net";
		try {

			Elements topicList = getProgramacion(context);
			
			for (Element topic : topicList) {
				String imagen = topic.select("img.imglstsr.lazy").attr("src").replace("mini", "portada");
				String titulo = topic.select("span.tit").text().substring(0,
						topic.select("span.tit").text().lastIndexOf(" "));
				String episodio = "Episodio " + topic.select("span.tit").text().substring(
						topic.select("span.tit").text().trim().lastIndexOf(" "),
						topic.select("span.tit").text().trim().length());
				String urlEpisodio = url + topic.select("a").attr("href");
				String urlAnime = urlEpisodio.replace("/ver", "/anime")
						.substring(0, urlEpisodio.replace("/ver", "/anime").lastIndexOf("-")).concat(".html");
				Anime anime = new Anime(urlAnime, imagen, null, titulo, null, null, null);
				Capitulo animeProgramacion = new Capitulo(anime, episodio, urlEpisodio);
				Log.e("programacion", urlAnime+"-"+urlEpisodio);
				if (topic.select("a").attr("href").contains("/ver/")) {
					lista.add(animeProgramacion);
				}
			}

		} catch (Exception t) {
			if (t.getClass().equals(SocketTimeoutException.class)) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Tiempo de conexion agotado");
						toast.show();
					}
				});
			} else {
				t.printStackTrace();
				Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
						Toast.LENGTH_SHORT).show();
				ServidorUtil.appendLog(t.getMessage());
			}
		}
		return lista;
	}

	public Elements getProgramacion(final Context context) {
		Elements programacion = new Elements();
		String url;
		url = "http://www.animeflv.net";
		try {

			Response response = Jsoup.connect(url).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
					.userAgent("Mozilla/5.0").execute();
			Log.e("response", response.statusMessage());
			Document doc = null;
			if (response.statusCode() >= 500 && response.statusCode() <= 511) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Error en el servidor");
						toast.show();
					}
				});
			} else if (response.statusCode() >= 400 && response.statusCode() <= 450) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Error en el cliente");
						toast.show();
					}
				});
			} else if (response.statusCode() == 200) {
				doc = Jsoup.parse(response.body());

				Log.e("JSwa", "Connected to [" + url + "]");
				if (doc.select("div.ultimos_epis").select("div.not").size() > 20) {
					programacion = new Elements();
					programacion.addAll(doc.select("div.ultimos_epis").select("div.not").subList(0, 20));
				} else {
					programacion = doc.select("div.ultimos_epis").select("div.not");
				}

			}

		} catch (Exception t) {
			if (t.getClass().equals(SocketTimeoutException.class)) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Tiempo de conexion agotado");
						toast.show();
					}
				});
			} else {
				t.printStackTrace();
				Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
						Toast.LENGTH_SHORT).show();
				ServidorUtil.appendLog(t.getMessage());
			}
		}
		return programacion;
	}

	@Override
	public ArrayList<Relacionado> buscarRelacionados(String url, Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<AnimeFavorito> buscarAnime(String cadenaBusqueda, int pagina, final Context context) {
		ArrayList<AnimeFavorito> listaBusqueda1 = new ArrayList<AnimeFavorito>();
		String url = "http://www.animeflv.net/animes/?buscar=";
		try {
			String url2 = url + cadenaBusqueda + "&p=" + pagina + "/";
			Log.e("buscarAnimesConexion", "Connecting to [" + url2 + "]");
			Response response = Jsoup.connect(url2).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
					.userAgent("Mozilla/5.0").execute();
			Document doc = null;
			if (response.statusCode() >= 500 && response.statusCode() <= 511) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Error en el servidor");
						toast.show();
					}
				});
			} else if (response.statusCode() >= 400 && response.statusCode() <= 450) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Error en el cliente");
						toast.show();
					}
				});
			} else if (response.statusCode() == 200) {
				doc = Jsoup.parse(response.body());

				Log.e("buscarAnimesConexion", "Connected to [" + url2 + "]");
				Elements listaBusqueda = doc.select("div[style=margin-right: -20px;]").select("div.aboxy_lista");
				for (Element element : listaBusqueda) {
					String imagen = element.select("img.lazy.portada").attr("data-original");
					String titulo = element.select("a.titulo").text();
					String descripcion = element.select("div.sinopsis").text();
					String urlAnime = "http://www.animeflv.net" + element.select("a.titulo").attr("href");
					Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
					AnimeFavorito animeBusqueda = new AnimeFavorito(anime, null);
					listaBusqueda1.add(animeBusqueda);
				}
			}
		} catch (IOException t) {
			if (t.getClass().equals(SocketTimeoutException.class)) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Tiempo de conexion agotado");
						toast.show();
					}
				});
			} else {
				t.printStackTrace();
				Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
						Toast.LENGTH_SHORT).show();
				ServidorUtil.appendLog(t.getMessage());
			}
		}
		return listaBusqueda1;
	}

	public ArrayList<AnimeFavorito> buscarAnimeLetra(String cadenaBusqueda, int pagina, final Context context) {
		ArrayList<AnimeFavorito> listaBusqueda1 = new ArrayList<AnimeFavorito>();
		String url;
		url = "http://www.animeflv.net/animes/letra/";
		try {
			String url2 = url + cadenaBusqueda + "/?orden=nombre&p=" + pagina + "/";
			Log.e("buscarAnimesConexion", "Connecting to [" + url2 + "]");
			Response response = Jsoup.connect(url2).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
					.userAgent("Mozilla/5.0").execute();
			Document doc = null;
			if (response.statusCode() >= 500 && response.statusCode() <= 511) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Error en el servidor");
						toast.show();
					}
				});
			} else if (response.statusCode() >= 400 && response.statusCode() <= 450) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Error en el cliente");
						toast.show();
					}
				});
			} else if (response.statusCode() == 200) {
				doc = Jsoup.parse(response.body());

				Log.e("buscarAnimesConexion", "Connected to [" + url2 + "]");
				Elements listaBusqueda = doc.select("div[style=margin-right: -20px;]").select("div.aboxy_lista");
				for (Element element : listaBusqueda) {
					String imagen = element.select("img.lazy.portada").attr("data-original");
					String titulo = element.select("a.titulo").text();
					String descripcion = element.select("div.sinopsis").text();
					String urlAnime = "http://www.animeflv.net" + element.select("a.titulo").attr("href");
					Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
					AnimeFavorito animeBusqueda = new AnimeFavorito(anime, null);
					listaBusqueda1.add(animeBusqueda);
				}
			}
		} catch (IOException t) {
			if (t.getClass().equals(SocketTimeoutException.class)) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Tiempo de conexion agotado");
						toast.show();
					}
				});
			} else {
				t.printStackTrace();
				Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
						Toast.LENGTH_SHORT).show();
				ServidorUtil.appendLog(t.getMessage());
			}
		}
		return listaBusqueda1;
	}

	public ArrayList<AnimeFavorito> buscarAnimeGenero(String cadenaBusqueda, int pagina, final Context context) {
		ArrayList<AnimeFavorito> listaBusqueda1 = new ArrayList<AnimeFavorito>();
		String url = null;
		url = "http://www.animeflv.net";
		try {
			String url2 = url + cadenaBusqueda + "?orden=nombre&p=" + pagina + "/";
			Log.e("buscarAnimesConexion", "Connecting to [" + url2 + "]");
			Response response = Jsoup.connect(url2).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
					.userAgent("Mozilla/5.0").execute();
			Document doc = null;
			if (response.statusCode() >= 500 && response.statusCode() <= 511) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Error en el servidor");
						toast.show();
					}
				});
			} else if (response.statusCode() >= 400 && response.statusCode() <= 450) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Error en el cliente");
						toast.show();
					}
				});
			} else if (response.statusCode() == 200) {
				doc = Jsoup.parse(response.body());
				Log.e("buscarAnimesConexion", "Connected to [" + url2 + "]");
				Elements listaBusqueda = doc.select("div[style=margin-right: -20px;]").select("div.aboxy_lista");
				for (Element element : listaBusqueda) {
					String imagen = element.select("img.lazy.portada").attr("data-original");
					String titulo = element.select("a.titulo").text();
					String descripcion = element.select("div.sinopsis").text();
					String urlAnime = "http://www.animeflv.net" + element.select("a.titulo").attr("href");
					Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
					AnimeFavorito animeBusqueda = new AnimeFavorito(anime, null);
					listaBusqueda1.add(animeBusqueda);
				}
			}

		} catch (IOException t) {
			t.printStackTrace();
			Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
					Toast.LENGTH_SHORT).show();
			ServidorUtil.appendLog(t.getMessage());
		}
		return listaBusqueda1;
	}

	public ArrayList<String[]> buscarGeneros(final Context context) {
		ArrayList<String[]> lista = new ArrayList<>();
		String url = "http://www.animeflv.net/animes/";
		try {
			Log.e("buscarAnimesConexion", "Connecting to [" + url + "]");
			Response response = Jsoup.connect(url).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
					.userAgent("Mozilla/5.0").execute();
			Document doc = null;
			if (response.statusCode() >= 500 && response.statusCode() <= 511) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Error en el servidor");
						toast.show();
					}
				});
			} else if (response.statusCode() == 200) {
				doc = Jsoup.parse(response.body());

				Log.e("buscarAnimesConexion", "Connected to [" + url + "]");
				Elements listaBusqueda = doc.select("div.generos_box a");
				for (Element element : listaBusqueda) {
					String[] genero = { element.attr("href"), element.text() };
					lista.add(genero);
				}
			}
		} catch (IOException t) {
			if (t.getClass().equals(SocketTimeoutException.class)) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Tiempo de conexion agotado");
						toast.show();
					}
				});
			} else {
				t.printStackTrace();
				Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
						Toast.LENGTH_SHORT).show();
				ServidorUtil.appendLog(t.getMessage());
			}
		}
		return lista;
	}

}
