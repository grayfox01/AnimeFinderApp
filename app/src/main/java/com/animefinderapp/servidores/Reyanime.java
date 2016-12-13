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

public class Reyanime implements IServidores {

	private static final Reyanime reyanime = new Reyanime();

	private Reyanime() {
	}

	public static Reyanime getInstance() {

		return reyanime;
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
			} else if (response.statusCode() == 200) {
				doc = Jsoup.parse(response.body());
				Log.e("buscarAnimeConexion", "Connected to [" + urlAnime + "]");
                anime.setUrl(urlAnime);
				Elements topicList = doc.select("div.cuerpo-dentro");
				String imageUrl = topicList.select("div.izq-gris img").attr("src");
				anime.setImagen(imageUrl);
				Elements datosElement = doc.select("div.sinopsis");
				ArrayList<String> datos = new ArrayList<>();
				Log.e("datosElement", datosElement.text());
				datos.add("Tipo: " + doc.select("div.conten-box h1 b").text());
				datos.add("Emitido: " + datosElement.select("span").text());
				datos.add("GenerosNombreFragment: " + datosElement.select("b").text());
				datos.add("Otros nombres: " + datosElement.select("h2").text());
				anime.setDatos(datos);
				topicList = doc.select("div.conten-box h1");
				String titulo = topicList.text();
				anime.setTitulo(titulo);
				topicList = doc.select("div.sinopsis");
				String descripcion = topicList.last().ownText();
				anime.setDescripcion(descripcion);
				topicList = doc.select("div#box-cap a");
				ArrayList<Capitulo> capitulos = new ArrayList<>();
				for (Element element : topicList) {
					Capitulo a = new Capitulo(anime, element.text(), "http://reyanime.com" + element.attr("href"));
					capitulos.add(a);
				}
				anime.setCapitulos(capitulos);
				ArrayList<Relacionado> relacionados = new ArrayList<>();
				topicList = doc.select("div.relacionado-box a");
				for (Element element : topicList) {
					Anime relacionado = new Anime();
					relacionado.setUrl("http://reyanime.com" + element.attr("href"));
					relacionado.setTitulo(element.select("h3").text().replace(element.select("h3 b").text(), ""));
					Relacionado r = new Relacionado(element.select("h3 b").text() + ": ", relacionado);
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

		try {

			Elements programacion = getProgramacion(context);
			for (Element topic : programacion) {
				String imagen = topic.select("img").attr("src");
				String titulo = topic.select("div.gominola name").text();
				String episodios = "Episodio " + topic.select("sombra").text();
				String urlEpisodio = "http://reyanime.com" + topic.select("a").attr("href");
				String urlAnime = urlEpisodio.replace("http://reyanime.com", "http://reyanime.com/anime").substring(0,
						urlEpisodio.replace("http://reyanime.com", "http://reyanime.com/anime").lastIndexOf("-"));
				Anime anime = new Anime(urlAnime, imagen, null, titulo, null, null, null);
				Capitulo animeProgramacion = new Capitulo(anime,episodios, urlEpisodio);
				Log.e("programacion", animeProgramacion.toString());
				lista.add(animeProgramacion);
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
		String url = "http://reyanime.com/anime/emision-diaria";
		Elements programacion = new Elements();
		try {
			Log.e("JSwa", "Connecting to [" + url + "]");
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

				Log.e("JSwa", "Connected to [" + url + "]");
				programacion = new Elements();
				programacion.addAll(doc.select("div.fechaemison a").subList(0, 20));

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

	public ArrayList<AnimeFavorito> buscarAnime(String cadenaBusqueda, int pagina, final Context context) {
		ArrayList<AnimeFavorito> listaBusqueda1 = new ArrayList<AnimeFavorito>();
		String url = "http://reyanime.com/anime/";
		try {
			String url2 = url + "?title=" + cadenaBusqueda + "&page=" + pagina;
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
			} else if (response.statusCode() == 200) {
				doc = Jsoup.parse(response.body());

				Log.e("buscarAnimesConexion", "Connected to [" + url2 + "]");
				Elements listaBusqueda = doc.select("div.resultado a");
				for (Element element : listaBusqueda) {
					String imagen = element.select("img").attr("src").replace("thumbnail", "image");
					String titulo = element.select("h3").text();
					String descripcion = element.select("div").text();
					String urlAnime = "http://reyanime.com" + element.attr("href");
					Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
					AnimeFavorito animeBusqueda = new AnimeFavorito(anime,null);
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
		String url = "http://reyanime.com/lista-";
		try {
			String cadenaBusqueda2;
			if (cadenaBusqueda.contains("0-9")) {
				cadenaBusqueda2 = cadenaBusqueda.replace("0-9", "numeros");
			} else {
				cadenaBusqueda2 = cadenaBusqueda.toUpperCase();
			}
			String url2 = url + cadenaBusqueda2 + "?page=" + pagina;
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
			} else if (response.statusCode() == 200) {
				doc = Jsoup.parse(response.body());

				Log.e("buscarAnimesConexion", "Connected to [" + url2 + "]");
				Elements listaBusqueda = doc.select("div.paginacion-alta a");
				for (Element element : listaBusqueda) {
					String imagen = element.select("img").attr("src");
					String titulo = element.select("span").text();
					String descripcion = element.select("div.sinopsis").text();
					String urlAnime = "http://reyanime.com" + element.attr("href");
					Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
					AnimeFavorito animeBusqueda = new AnimeFavorito(anime,null);
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
		try {
			String url2 = cadenaBusqueda + "?page=" + pagina;
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
			} else if (response.statusCode() == 200) {
				doc = Jsoup.parse(response.body());

				Log.e("buscarAnimesConexion", "Connected to [" + url2 + "]");
				Elements listaBusqueda = doc.select("div.paginacion-alta a");
				for (Element element : listaBusqueda) {
					String imagen = element.select("img").attr("src");
					String titulo = element.select("span").text();
					String descripcion = element.select("div.sinopsis").text();
					String urlAnime = "http://reyanime.com" + element.attr("href");
					Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
					AnimeFavorito animeBusqueda = new AnimeFavorito(anime,null);
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

	public ArrayList<String[]> buscarGeneros(final Context context) {
		ArrayList<String[]> lista = new ArrayList<>();
		String url = "http://reyanime.com/genero/accion";
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
				Elements listaBusqueda = doc.select("div.lista-hoja-genero-2 a");
				for (Element element : listaBusqueda) {
					String[] genero = { "http://reyanime.com" + element.attr("href"), element.text() };
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
			}
		}
		return lista;
	}

	@Override
	public ArrayList<Relacionado> buscarRelacionados(String url, Context context) {
		// TODO Auto-generated method stub
		return null;
	}

}
