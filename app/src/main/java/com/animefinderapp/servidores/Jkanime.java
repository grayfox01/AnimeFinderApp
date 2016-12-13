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

public class Jkanime implements IServidores {
	private static final Jkanime jkanime = new Jkanime();

	private Jkanime() {
	}

	public static Jkanime getInstance() {

		return jkanime;
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
				Element imgElement = doc.select("div.separedescrip").get(0);
				String imageUrl = imgElement.select("img").attr("src");
				anime.setImagen(imageUrl);
				ArrayList<String> datos = new ArrayList<>();
				Elements datosElement = doc.select("div.separedescrip div");
				for (Element element : datosElement) {
					datos.add(new String(element.text().getBytes(), "UTF-8").toString());
				}
				anime.setDatos(datos);
				topicList = doc.select("div.sinopsis_title");
				String titulo = topicList.text();
				anime.setTitulo(titulo);
				topicList = doc.select("div.sinoptext").select("p");
				String descripcion = topicList.text();
				anime.setDescripcion(descripcion);
				topicList = doc.select("div.lista_title");
				String episodiosEmitidos1;
				int episodiosEmitidos;
				if (!topicList.isEmpty()) {
					topicList = doc.select("div.listnavi").select("a");
					episodiosEmitidos1 = topicList.get(topicList.size() - 1).text();
					episodiosEmitidos = Integer.parseInt(episodiosEmitidos1.split("-")[1].trim());
					Log.e("capitulosEmitidos", Integer.toString(episodiosEmitidos));
					ArrayList<Capitulo> capitulos = new ArrayList<>();
					for (int i = 1; i < episodiosEmitidos + 1; i++) {
						Capitulo a = new Capitulo(anime, titulo + " " + i, urlAnime + "/" + i + "/");
						capitulos.add(a);
					}
					anime.setCapitulos(capitulos);
				} else {
					topicList = doc.select("div.capitulos_right").select("div.listbox").select("a");
					ArrayList<Capitulo> capitulos = new ArrayList<>();
					Capitulo a = new Capitulo(anime, titulo + " 1", topicList.attr("href").toString());
					capitulos.add(a);
					anime.setCapitulos(capitulos);
				}
				ArrayList<Relacionado> relacionados = buscarRelacionados(urlAnime, context);
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

	public ArrayList<Relacionado> buscarRelacionados(String url, final Context context) {
		ArrayList<Relacionado> listaRelacionados = new ArrayList<>();
		try {
			Log.e("buscarRelacionadosConexion", "Connecting to [" + url + "]");
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

				Log.e("buscarRelacionadosConexion", "Connected to [" + url + "]");
				Elements topicList = doc.select("div.sinoptext").select("div");
				String tipoRelacion = "";
				for (Element element : topicList) {

					if (element.className().contains("rel_type")) {
						tipoRelacion = element.text();

					}
					if (element.className().contains("rel_content conte_rel")) {
						Anime relacionado = new Anime();
						relacionado.setUrl(element.select("a").attr("href"));
						relacionado.setTitulo(element.select("a").text());
						Relacionado r = new Relacionado(tipoRelacion, relacionado);
						listaRelacionados.add(r);
					}
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

		return listaRelacionados;
	}

	public ArrayList<Capitulo> buscarProgramacion(final Context context) {
		ArrayList<Capitulo> lista = new ArrayList<Capitulo>();
		try {

			Elements topicList = getProgramacion(context);
			for (Element topic : topicList) {
				String imagen = topic.select("a.rated_avatar.listpr").select("img").attr("src").replace("thumbnail",
						"image");
				String titulo = topic.select("a.rated_title").text();
				String episodios = topic.select("div.rated_stars").select("span").get(0).text();
				String urlEpisodio = topic.select("a.rated_title").attr("href");
				String urlAnime = urlEpisodio.substring(0, urlEpisodio.lastIndexOf("/")).substring(0,
						urlEpisodio.substring(0, urlEpisodio.lastIndexOf("/")).lastIndexOf("/"))+"/";
				Anime anime = new Anime(urlAnime, imagen, null, titulo, null, null, null);
				Capitulo animeProgramacion = new Capitulo(anime, episodios, urlEpisodio);
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
		Elements programacion = new Elements();
		String url = "http://www.jkanime.net/";
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
				if (doc.select("ul.ratedul").select("li").size() > 20) {
					programacion = new Elements();
					programacion.addAll(doc.select("ul.ratedul").select("li").subList(0, 20));
				} else {
					programacion = doc.select("ul.ratedul").select("li");
				}
			}
		} catch (Exception t) {
			if (t.getClass().equals(SocketTimeoutException.class)) {
				Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
				toast.setText("Tiempo de conexion agotado");
				toast.show();

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
		String url = "http://www.jkanime.net/buscar/";
		try {
			String url2 = url + cadenaBusqueda.replace(" ", "_") + "/" + pagina + "/";
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
				Elements listaBusqueda = doc.select("div.listpage").select("table.search");
				for (Element element : listaBusqueda) {
					if (!element.text().contains("No se encontraron resultados")) {
						String imagen = element.select("td").get(0).select("a").select("img").attr("src")
								.replace("thumbnail", "image");
						String titulo = element.select("td").get(1).text();
						String descripcion = element.select("td").get(4).text();
						String urlAnime = element.select("td").select("a.next").attr("href");
						Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
						AnimeFavorito animeBusqueda = new AnimeFavorito(anime, null);
						listaBusqueda1.add(animeBusqueda);

					}
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
		String url = "http://www.jkanime.net/letra/";
		try {
			String url2 = url + cadenaBusqueda.replace(" ", "_") + "/" + pagina + "/";
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
				Elements listaBusqueda = doc.select("div.listpage").select("table.search");
				for (Element element : listaBusqueda) {
					String urlAnime = element.select("td").select("a.next").attr("href");
					if (!urlAnime.trim().equals("http://jkanime.net//")
							&& !element.text().contains("No se encontraron resultados")) {
						String imagen = element.select("td").get(0).select("a").select("img").attr("src")
								.replace("thumbnail", "image");
						String titulo = element.select("td").get(1).text();
						String descripcion = element.select("td").get(4).text();
						Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
						AnimeFavorito animeBusqueda = new AnimeFavorito(anime, null);
						listaBusqueda1.add(animeBusqueda);
					}
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
			String url2 = cadenaBusqueda + pagina + "/";
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
				Elements listaBusqueda = doc.select("div.listpage").select("table.search");
				for (Element element : listaBusqueda) {
					String urlAnime = element.select("td").select("a.next").attr("href");
					if (!urlAnime.trim().equals("http://jkanime.net//")
							&& !element.text().contains("No se encontraron resultados")) {
						String imagen = element.select("td").get(0).select("a").select("img").attr("src")
								.replace("thumbnail", "image");
						String titulo = element.select("td").get(1).text();
						String descripcion = element.select("td").get(4).text();
						Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
						AnimeFavorito animeBusqueda = new AnimeFavorito(anime, null);
						listaBusqueda1.add(animeBusqueda);
					}
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
		String url = "http://jkanime.net/";
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
				Elements listaBusqueda = doc.select("div.genres a");
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
