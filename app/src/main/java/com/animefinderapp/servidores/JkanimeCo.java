package com.animefinderapp.servidores;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.animefinderapp.actividades.AnimeActivity;
import com.animefinderapp.entidades.Anime;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.entidades.Capitulo;
import com.animefinderapp.entidades.Relacionado;
import com.animefinderapp.interfaces.IServidores;
import com.animefinderapp.utilidad.ServidorUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class JkanimeCo implements IServidores {
	private static final JkanimeCo jkanimeCo = new JkanimeCo();

	private JkanimeCo() {
	}

	public static JkanimeCo getInstance() {
		return jkanimeCo;
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
				topicList = doc.select("div.anime_info_body_bg");
				String imageUrl = topicList.select("img").attr("src");
				anime.setImagen(imageUrl);
				Elements datosElement = topicList.select("p.type");
				ArrayList<String> datos = new ArrayList<>();
				for (Element element : datosElement) {
					datos.add(new String(element.text().getBytes(), "UTF-8").toString());
				}
				anime.setDatos(datos);
				String titulo = topicList.select("h1").text();
				anime.setTitulo(titulo);
				String descripcion = topicList.select("p.type").get(0).ownText();
				anime.setDescripcion(descripcion);
				topicList = doc.select("ul#episode_related li");
				ArrayList<Capitulo> capitulos = new ArrayList<>();
				for (Element element : topicList) {
					Capitulo a = new Capitulo(anime, element.text(), element.select("a").attr("href"));
					capitulos.add(a);
				}
				anime.setCapitulos(capitulos);
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
				Elements topicList = doc.select("nav.menu_series ul").select("li");
				String tipoRelacion = "";
				for (Element element : topicList) {
					Anime anime = new Anime();
					anime.setUrl(element.select("a").attr("href"));
					anime.setTitulo(element.select("a").text());
					Relacionado r = new Relacionado(tipoRelacion, anime);
					listaRelacionados.add(r);
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
				String imagen = null;
				Pattern p = Pattern.compile("http://.+?\\.jpg");
				Matcher m = p.matcher(topic.select("div.thumbnail-recent_home").attr("style"));
				if (m.find()) {
					imagen = m.group();
				}
				String titulo = topic.select("div.bottom p.name").text();
				String episodios = topic.select("div.bottom p.time").text();
				String urlEpisodio = topic.select("div.bottom p.name a").attr("href");
				Response response = Jsoup.connect(urlEpisodio).timeout(3000).ignoreHttpErrors(true)
						.followRedirects(true).userAgent("Mozilla/5.0").execute();
				Document doc = Jsoup.parse(response.body());
				String urlAnime = doc.select("a.anime_title").attr("href");
				Anime anime = new Anime(urlAnime, imagen, null, titulo, null, null, null);
				Capitulo animeProgramacion = new Capitulo(anime,episodios, urlEpisodio);
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
		String url = "http://jkanime.co/";
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
				if (doc.select("div.last_episodes_items").size() > 20) {
					programacion = new Elements();
					programacion.addAll(doc.select("div.last_episodes_items").subList(0, 20));
				} else {
					programacion = doc.select("div.last_episodes_items");
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
		String url = "http://jkanime.co/Buscar?s=";
		try {
			String url1 = url + cadenaBusqueda.replace(" ", "+");
			Response response = Jsoup.connect(url1).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
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
				Log.e("url", response.url().toString());
				if (!response.url().toString().equals(url1)
						&& response.url().toString().contains("http://jkanime.co/")) {
					Intent i = new Intent(context, AnimeActivity.class);
					i.putExtra("url", response.url().toString());
					context.startActivity(i);
					((Activity) context).finish();
				} else {

					Document doc = Jsoup.parse(response.body());
					if (!doc.select("ul.pages").isEmpty()) {
						if (doc.select("ul.pages").text().contains(Integer.toString(pagina))) {
							String url2 = url + cadenaBusqueda.replace(" ", "+") + "&page=" + pagina;
							Log.e("buscarAnimesConexion", "Connecting to [" + url2 + "]");
							response = Jsoup.connect(url2).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
									.userAgent("Mozilla/5.0").execute();
							doc = Jsoup.parse(response.body());

							Log.e("buscarAnimesConexion", "Connected to [" + url2 + "]");
							Elements listaBusqueda = doc.select("div.last_episodes div.anime_movies_items");
							for (Element element : listaBusqueda) {

								String imagen = element.select("img").attr("src");
								String titulo = element.select("p.name").text();
								String descripcion = "";
								String urlAnime = element.select("p.name a").attr("href");
								Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
								AnimeFavorito animeBusqueda = new AnimeFavorito(anime,null);
								listaBusqueda1.add(animeBusqueda);
							}
						}
					} else {
						if (pagina == 1) {
							Log.e("buscarAnimesConexion", "Connected to [" + url1 + "]");
							Elements listaBusqueda = doc.select("div.last_episodes div.anime_movies_items");
							for (Element element : listaBusqueda) {

								String imagen = element.select("img").attr("src");
								String titulo = element.select("p.name").text();
								String descripcion = "";
								String urlAnime = element.select("p.name a").attr("href");
								Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
								AnimeFavorito animeBusqueda = new AnimeFavorito(anime,null);
								listaBusqueda1.add(animeBusqueda);
							}
						}
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
			Log.e("cadenabusqueda", cadenaBusqueda);
			Response response = Jsoup.connect(cadenaBusqueda).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
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
				Document doc = Jsoup.parse(response.body());
				if (!doc.select("ul.pages").isEmpty()) {
					if (doc.select("ul.pages").text().contains("First") && doc.select("ul.pages").contains("Last")) {
						Pattern p = Pattern.compile("page=(.+?)");
						Matcher m1 = p.matcher(doc.select("ul.pages li").first().html());
						Matcher m2 = p.matcher(doc.select("ul.pages li").last().html());
						if (pagina >= Integer.parseInt(m1.group()) && pagina <= Integer.parseInt(m2.group())) {
							String url2 = cadenaBusqueda + "&page=" + pagina;
							Log.e("buscarAnimesConexion", "Connecting to [" + url2 + "]");
							response = Jsoup.connect(url2).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
									.userAgent("Mozilla/5.0").execute();
							doc = Jsoup.parse(response.body());

							Log.e("buscarAnimesConexion", "Connected to [" + url2 + "]");
							Elements listaBusqueda = doc.select("div.last_episodes div.anime_movies_items");
							for (Element element : listaBusqueda) {

								String imagen = element.select("img").attr("src");
								String titulo = element.select("p.name").text();
								String descripcion = "";
								String urlAnime = element.select("p.name a").attr("href");
								Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
								AnimeFavorito animeBusqueda = new AnimeFavorito(anime,null);
								listaBusqueda1.add(animeBusqueda);
							}
						}
					} else if (doc.select("ul.pages").text().contains(Integer.toString(pagina))) {
						String url2 = cadenaBusqueda + "&page=" + pagina;
						Log.e("buscarAnimesConexion", "Connecting to [" + url2 + "]");
						response = Jsoup.connect(url2).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
								.userAgent("Mozilla/5.0").execute();
						doc = Jsoup.parse(response.body());

						Log.e("buscarAnimesConexion", "Connected to [" + url2 + "]");
						Elements listaBusqueda = doc.select("div.last_episodes div.anime_movies_items");
						for (Element element : listaBusqueda) {

							String imagen = element.select("img").attr("src");
							String titulo = element.select("p.name").text();
							String descripcion = "";
							String urlAnime = element.select("p.name a").attr("href");
							Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
							AnimeFavorito animeBusqueda = new AnimeFavorito(anime,null);
							listaBusqueda1.add(animeBusqueda);
						}
					}
				} else {
					if (pagina == 1) {
						Log.e("buscarAnimesConexion", "Connected to [" + cadenaBusqueda + "]");
						Elements listaBusqueda = doc.select("div.last_episodes div.anime_movies_items");
						for (Element element : listaBusqueda) {

							String imagen = element.select("img").attr("src");
							String titulo = element.select("p.name").text();
							String descripcion = "";
							String urlAnime = element.select("p.name a").attr("href");
							Anime anime = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
							AnimeFavorito animeBusqueda = new AnimeFavorito(anime,null);
							listaBusqueda1.add(animeBusqueda);
						}
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
		String url = "http://jkanime.co/ListadeAnime?c=";
		String cadenaBusqueda1;
		try {
			if (cadenaBusqueda.equals("0-9")) {
				cadenaBusqueda1 = "0";
			} else {
				cadenaBusqueda1 = cadenaBusqueda;
			}
			String url1 = url + cadenaBusqueda1;
			Response response = Jsoup.connect(url1).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
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
				Document doc = Jsoup.parse(response.body());
				if (!doc.select("ul.pages").isEmpty()) {
					if (doc.select("ul.pages").text().contains(Integer.toString(pagina))) {
						String url2 = url + cadenaBusqueda1 + "&page=" + pagina;
						Log.e("buscarAnimesConexion", "Connecting to [" + url2 + "]");
						response = Jsoup.connect(url2).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
								.userAgent("Mozilla/5.0").execute();
						doc = Jsoup.parse(response.body());

						Log.e("buscarAnimesConexion", "Connected to [" + url2 + "]");
						Elements listaBusqueda = doc.select("ul.listing li");
						for (Element element : listaBusqueda) {
							Element anime = Jsoup.parse(element.attr("title").replace("&quot;", "\""));
							String imagen = anime.select("img").attr("src");
							String titulo = anime.select("a.bigChar").text();
							String descripcion = anime.select("p.sumer").get(0).ownText();
							String urlAnime = anime.select("a.bigChar").attr("href");
							Anime anime1 = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
							AnimeFavorito animeBusqueda = new AnimeFavorito(anime1,null);
							listaBusqueda1.add(animeBusqueda);
						}
					}
				} else {
					if (pagina == 1) {
						Log.e("buscarAnimesConexion", "Connected to [" + url1 + "]");
						Elements listaBusqueda = doc.select("ul.listing li");
						for (Element element : listaBusqueda) {
							Element anime = Jsoup.parse(element.attr("title").replace("&quot;", "\""));
							String imagen = anime.select("img").attr("src");
							String titulo = anime.select("a.bigChar").text();
							String descripcion = anime.select("p.sumer").get(0).ownText();
							String urlAnime = anime.select("a.bigChar").attr("href");
							Anime anime1 = new Anime(urlAnime, imagen, null, titulo, descripcion, null, null);
							AnimeFavorito animeBusqueda = new AnimeFavorito(anime1,null);
							listaBusqueda1.add(animeBusqueda);
						}
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

	public ArrayList<String[]> buscarGeneros(final Context context) {
		ArrayList<String[]> lista = new ArrayList<>();
		String url = "http://jkanime.co/";
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
				Elements listaBusqueda = doc.select("nav.menu_series.genre.right a");
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

	public static String videoJkanimeCo(String posturl, ArrayList<NameValuePair> values) {

		try {

			HttpClient httpclient = new DefaultHttpClient();
			/*
			 * Creamos el objeto de HttpClient que nos permitira conectarnos
			 * mediante peticiones http
			 */
			HttpPost httppost = new HttpPost(posturl);
			/*
			 * El objeto HttpPost permite que enviemos una peticion de tipo POST
			 * a una URL especificada
			 */
			// AÑADIR PARAMETROS

			/*
			 * Una vez añadidos los parametros actualizamos la entidad de
			 * httppost, esto quiere decir en pocas palabras anexamos los
			 * parametros al objeto para que al enviarse al servidor envien los
			 * datos que hemos añadido
			 */
			httppost.setEntity(new UrlEncodedFormEntity(values));

			/* Finalmente ejecutamos enviando la info al server */
			HttpResponse resp = httpclient.execute(httppost);
			HttpEntity ent = resp.getEntity();/* y obtenemos una respuesta */

			return EntityUtils.toString(ent);

		} catch (Exception e) {
			return "error";
		}

	}
}
