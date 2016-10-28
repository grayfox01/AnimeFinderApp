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

public class AnimeID implements IServidores {
	private static final AnimeID animeID = new AnimeID();

	private AnimeID() {
	}

	public static AnimeID getInstance() {

		return animeID;
	}

	public Anime buscarInfoAnime(String urlAnime, final Context context) {
		Anime anime = new Anime();
		Response response;
		Document doc = null;
		try {
			Log.e("buscarAnimeConexion", "Connecting to [" + urlAnime + "]");
			response = Jsoup.connect(urlAnime).timeout(3000).ignoreHttpErrors(true).followRedirects(true)
					.userAgent("Mozilla/5.0").execute();

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
				Elements imgElement = doc.select("section.main").select("img");
				Log.e("imagenElement", imgElement.toString());
				String imageUrl = imgElement.attr("src");
				anime.setImagen(imageUrl);
				ArrayList<String> datos = new ArrayList<>();
				Elements datosElement = doc.select("div.status-left").select("div.cuerpo div");
				Log.e("datosElement", datosElement.text());
				for (Element element : datosElement) {
					datos.add(new String(element.text().getBytes(), "UTF-8").toString());
				}
				anime.setDatos(datos);
				topicList = doc.select("article#anime").select("hgroup").select("h1");
				String titulo = topicList.text();
				anime.setTitulo(titulo);
				topicList = doc.select("article#anime").select("p.sinopsis");
				String descripcion = topicList.text();
				anime.setDescripcion(descripcion);
				topicList = doc.select("ul#listado");
				int episodiosEmitidos = Integer
						.parseInt(topicList.select("li").get(0).text().split(" ")[1].replace(":", "").trim());
				ArrayList<Capitulo> capitulos = new ArrayList<>();
				for (int i = 1; i < episodiosEmitidos + 1; i++) {
					Capitulo a = new Capitulo(anime, titulo +" "+i,
							urlAnime.replace("http://www.animeid.tv", "http://www.animeid.tv/ver") + "-" + i);
					capitulos.add(a);
				}
				anime.setCapitulos(capitulos);
				ArrayList<Relacionado> relacionados = new ArrayList<>();
				topicList = doc.select("div.status-right").select("ul.cuerpo").select("li");
				for (Element element : topicList) {
					Anime relacionado = new Anime();
					relacionado.setTitulo(element.select("a").text());
					relacionado.setUrl(element.select("a").attr("href"));
					Relacionado r = new Relacionado(element.select("span").text() + ": ", relacionado);
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

	@Override
	public ArrayList<Relacionado> buscarRelacionados(String url, Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Capitulo> buscarProgramacion(final Context context, final ProgressDialog pDialog) {
		ArrayList<Capitulo> lista = new ArrayList<Capitulo>();
		String url = "http://www.animeid.tv";
		try {

			Elements programacion = getProgramacion(context);
			pDialog.setMax(programacion.size());
			for (Element topic : programacion) {
				String imagen = topic.select("figure").select("img").attr("src");
				String titulo = topic.select("header").text().split("#")[0];
				String episodios = "Episodio " + topic.select("header").text().split("#")[1];
				String urlEpisodio = url + topic.select("a").attr("href");
				String urlAnime = urlEpisodio.replace("ver/", "").substring(0,
						urlEpisodio.replace("ver/", "").lastIndexOf("-"));
				Anime anime = new Anime(urlAnime, imagen, null, titulo, null, null, null);
				Capitulo animeProgramacion = new Capitulo(anime,episodios, urlEpisodio);
				Log.e("programacion", animeProgramacion.toString());
				lista.add(animeProgramacion);

				pDialog.incrementProgressBy(1);

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
	
	public ArrayList<Capitulo> buscarProgramacion(final Context context) {
		ArrayList<Capitulo> lista = new ArrayList<Capitulo>();
		String url = "http://www.animeid.tv";
		try {

			Elements programacion = getProgramacion(context);
			for (Element topic : programacion) {
				String imagen = topic.select("figure").select("img").attr("src");
				String titulo = topic.select("header").text().split("#")[0];
				String episodios = "Episodio " + topic.select("header").text().split("#")[1];
				String urlEpisodio = url + topic.select("a").attr("href");
				String urlAnime = urlEpisodio.replace("ver/", "").substring(0,
						urlEpisodio.replace("ver/", "").lastIndexOf("-"));
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
		Elements programacion = new Elements();
		String url = "http://www.animeid.tv";
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

				Elements topicList = doc.select("section.lastcap").select("article");
				programacion = new Elements();
				int cantidad = 0;
				for (Element topic : topicList) {
					if (cantidad < 20) {
						if (!topic.select("a").attr("href").contains("peliculasid")) {
							programacion.add(topic);
							cantidad++;
						}
					}
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

	public ArrayList<AnimeFavorito> buscarAnime(String cadenaBusqueda, int pagina, final Context context) {
		ArrayList<AnimeFavorito> listaBusqueda1 = new ArrayList<AnimeFavorito>();
		String url = "http://www.animeid.tv/buscar?q=";
		try {
			String url2 = url + cadenaBusqueda + "&pag=" + pagina + "/";
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
				Elements listaBusqueda = doc.select("section#result.list").select("article.item");
				for (Element element : listaBusqueda) {
					String imagen = element.select("figure").select("img").attr("src");
					String titulo = element.select("header").text();
					String descripcion = element.select("p").text();
					String urlAnime = "http://www.animeid.tv" + element.select("a").attr("href");
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
		String url = "http://www.animeid.tv/letras/";
		try {
			String url2 = url + cadenaBusqueda + "?sort=asc&pag=" + pagina + "/";
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
				Elements listaBusqueda = doc.select("section#result.list").select("article.item");
				for (Element element : listaBusqueda) {
					String imagen = element.select("figure").select("img").attr("src");
					String titulo = element.select("header").text();
					String descripcion = element.select("p").text();
					String urlAnime = "http://www.animeid.tv" + element.select("a").attr("href");
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
		String url = "http://www.animeid.tv";
		try {
			String url2 = url + cadenaBusqueda + "?sort=asc&pag=" + pagina + "/";
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
				Elements listaBusqueda = doc.select("section#result.list").select("article.item");
				for (Element element : listaBusqueda) {
					String imagen = element.select("figure").select("img").attr("src");
					String titulo = element.select("header").text();
					String descripcion = element.select("p").text();
					String urlAnime = url + element.select("a").attr("href");
					Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
					AnimeFavorito animeBusqueda = new AnimeFavorito(anime,null);
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
		String url = "http://www.animeid.tv/";
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
				Elements listaBusqueda = doc.select("ul#generos a");
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