package com.animefinderapp.factory;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.animefinderapp.entidades.Capitulo;
import com.animefinderapp.servidores.JkanimeCo;
import com.animefinderapp.utilidad.ServidorUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class GetVideosFactory {
	private static final GetVideosFactory getVideosFactory = new GetVideosFactory();

	private GetVideosFactory() {
	}

	public static GetVideosFactory getInstance() {
		return getVideosFactory;
	}

	public AsyncTask<String, Void, String> getVideo(Capitulo capitulo, String accion, String servidor,
			Context context) {
		switch (servidor) {
		case "animeflv":
			return new GetVideosAnimeFlv(capitulo, accion, servidor, context);
		case "animeid":
			return new GetVideosAnimeID(capitulo, accion, servidor, context);

		case "jkanime":
			return new GetVideosJkanime(capitulo, accion, servidor, context);

		case "reyanime":
			return new GetVideosReyanime(capitulo, accion, servidor, context);

		case "jkanimeco":
			return new GetVideosJkanimeCo(capitulo, accion, servidor, context);

		default:
			return null;
		}
	}

	private class GetVideosAnimeFlv extends AsyncTask<String, Void, String> {
		private ProgressDialog pDialog;
		private TreeMap<String, String> listavideos = new TreeMap<String, String>();
		private Capitulo capitulo;
		private List<String> listaServidoresAnimeflv = Arrays.asList("hyperion", "aflv", "novamov", "nowvideo",
				"mp4upload", "izanagi");
		private String accion;
		private String server;
		private Context context;

		public GetVideosAnimeFlv(Capitulo animeProgramacion, String accion, String server, Context context) {
			this.capitulo = animeProgramacion;
			this.accion = accion;
			this.server = server;
			this.context = context;
		}

		@Override
		protected void onPreExecute() {

			pDialog = new ProgressDialog(context);
			pDialog.setIndeterminate(false);
			pDialog.setMessage("Obteniendo enlaces...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected String doInBackground(String... strings) {
			try {
				Document doc = null;
				String url = capitulo.getUrl();
				Log.e("buscarVideosConexion", "Connecting to [" + url + "]");
				Response response = Jsoup.connect(url).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
						.userAgent("Mozilla/5.0").ignoreContentType(true).execute();
				doc = Jsoup.parse(response.body());
				Elements servidores = doc.select("ul.opvid li");
				Elements scriptTags = doc.getElementsByTag("script");
				for (Element tag : scriptTags) {
					for (DataNode node : tag.dataNodes()) {
						if (node.getWholeData().contains("var videos")) {
							Hashtable<String, String> videos = new Hashtable<>();
							videos.putAll(ServidorUtil.getEmbedTableAnimeFlv(node.getWholeData()));

							Log.e("servidoresSize", Integer.toString(servidores.size()));
							for (int i = 0; i < servidores.size(); i++) {
								try {
									if (listaServidoresAnimeflv
											.contains(servidores.get(i).attr("title").toLowerCase())) {
										Log.e("nombre" + i, servidores.get(i).attr("title"));
										Log.e("video" + i, videos.get(servidores.get(i).attr("title").toLowerCase()));
										String titulo = servidores.get(i).attr("title").toLowerCase();
										String video = ServidorUtil.getVideoAnimeFlv(titulo,
												videos.get(servidores.get(i).attr("title").toLowerCase()), context);
										if (video != null) {
											listavideos.put(titulo, video);
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
									Toast.makeText(context,
											"Error general:\n Revise el log de errores para mas informacion.",
											Toast.LENGTH_SHORT).show();
									ServidorUtil.appendLog(e.getMessage());
								}
							}
						}
					}
				}

				return "ok";
			} catch (Exception e) {
				if (e.getClass().equals(SocketTimeoutException.class)) {
					Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
					toast.setText("Tiempo de conexion agotado");
					toast.show();
				} else {
					e.printStackTrace();
					Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
							Toast.LENGTH_SHORT).show();
					ServidorUtil.appendLog(e.getMessage());
				}
				return "error";
			}
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			if (s.equals("ok")) {
				try {
					Log.e("listavideos", listavideos.toString());
					ServidorUtil.onCreateDialogVideo(listavideos, capitulo, accion, context, server).show();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
							Toast.LENGTH_SHORT).show();
					ServidorUtil.appendLog(e.getMessage());
				}
			}
			pDialog.dismiss();
		}
	}

	private class GetVideosAnimeID extends AsyncTask<String, Void, String> {
		private ProgressDialog pDialog;
		private TreeMap<String, String> listavideos = new TreeMap<String, String>();
		private Capitulo capitulo;
		private String accion;
		private String server;
		private Context context;

		public GetVideosAnimeID(Capitulo animeProgramacion, String accion, String server, Context context) {
			this.capitulo = animeProgramacion;
			this.accion = accion;
			this.server = server;
			this.context = context;
		}

		@Override
		protected void onPreExecute() {

			pDialog = new ProgressDialog(context);
			pDialog.setIndeterminate(false);
			pDialog.setMessage("Obteniendo enlaces...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected String doInBackground(String... strings) {

			try {
				Document doc = null;
				String url = capitulo.getUrl();
				Log.e("buscarVideosConexion", "Connecting to [" + url + "]");
				Response response = Jsoup.connect(url).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
						.userAgent("Mozilla/5.0").execute();

				doc = Jsoup.parse(response.body());
				Log.e("response", response.statusMessage());
				Log.e("buscarVideosConexion", "Connected to [" + url + "]");
				Elements topicList = doc.select("ul#mirrors").select("li");
				Elements video = doc.select("ul#partes").select("div.container").select("li.subtab");
				for (int i = 0; i < topicList.size(); i++) {

					try {
						String nombre = topicList.get(i).text();
						String urlVideo = StringEscapeUtils.unescapeJava(video.get(i).select("div.parte").attr("data"))
								.replace("{'v':'", "").replace("}", "");
						doc = Jsoup.parse(urlVideo);
						Elements videoElement = null;
						String urlvideo2 = null;
						if (!doc.select("iframe").isEmpty()) {
							videoElement = doc.select("iframe");
							urlvideo2 = videoElement.attr("src");
						}
						if (!doc.select("embed").isEmpty()) {
							videoElement = doc.select("embed");
							urlvideo2 = videoElement.attr("flashvars");
						}
						if (!doc.select("object").isEmpty()) {
							videoElement = doc.select("object param").select("[name=Flashvars]");
							String cadena = "proxy.link=";
							urlvideo2 = videoElement.attr("value")
									.substring(videoElement.attr("value").lastIndexOf(cadena) + cadena.length());
						}
						if (!urlvideo2.contains("http:")) {
							urlvideo2 = "http:" + urlvideo2;
						}
						Log.e("buscarVideosConexion", "Connecting to [" + urlvideo2 + "]");
						response = Jsoup.connect(urlvideo2).timeout(3000).ignoreHttpErrors(true).followRedirects(true)
								.userAgent("Mozilla/5.0").execute();
						doc = Jsoup.parse(response.body());
						urlvideo2 = doc.select("video").select("source").attr("src");
						if (!urlvideo2.contains("http:")) {
							urlvideo2 = "http:" + urlvideo2;
						}
						if (urlvideo2.contains(".mp4")) {
							listavideos.put(nombre, urlvideo2);
						}
					} catch (Throwable e) {
						if (e.getClass().equals(SocketTimeoutException.class)) {
							Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
							toast.setText("Tiempo de conexion agotado");
							toast.show();
						} else {
							e.printStackTrace();
							Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
									Toast.LENGTH_SHORT).show();
							ServidorUtil.appendLog(e.getMessage());
						}
					}
				}
				return "ok";
			} catch (Throwable e) {
				if (e.getClass().equals(SocketTimeoutException.class)) {
					Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
					toast.setText("Tiempo de conexion agotado");
					toast.show();
				} else {
					e.printStackTrace();
					Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
							Toast.LENGTH_SHORT).show();
					ServidorUtil.appendLog(e.getMessage());
				}
				return "error";
			}

		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			if (s.equals("ok")) {
				try {
					Log.e("listavideos", listavideos.toString());
					ServidorUtil
							.onCreateDialogVideo(listavideos, capitulo, accion, context, server)
							.show();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
							Toast.LENGTH_SHORT).show();
					ServidorUtil.appendLog(e.getMessage());
				}
			}
			pDialog.dismiss();
		}
	}

	private class GetVideosJkanime extends AsyncTask<String, Void, String> {
		private ProgressDialog pDialog;
		private TreeMap<String, String> listavideos = new TreeMap<String, String>();
		private Capitulo capitulo;
		private String accion;
		private String server;
		private Context context;

		public GetVideosJkanime(Capitulo animeProgramacion, String accion, String server, Context context) {
			this.capitulo = animeProgramacion;
			this.accion = accion;
			this.server = server;
			this.context = context;
		}

		@Override
		protected void onPreExecute() {

			pDialog = new ProgressDialog(context);
			pDialog.setIndeterminate(false);
			pDialog.setMessage("Obteniendo enlaces...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected String doInBackground(String... strings) {
			try {
				Document doc = null;
				String url = capitulo.getUrl();
				Log.e("buscarVideosConexion", "Connecting to [" + url + "]");
				Response response = Jsoup.connect(url).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
						.userAgent("Mozilla/5.0").execute();
				doc = Jsoup.parse(response.body());
				Log.d("buscarVideosConexion", "Connected to [" + url + "]");
				Elements topicList = doc.select("div.video_option").select("a");
				if (!topicList.isEmpty()) {
					Elements video = doc.select("div.the_video");
					for (int i = 0; i < topicList.size(); i++) {
						Element element1 = topicList.get(i);
						Element element2 = video.get(i);
						String nombre = element1.text();
						String urlVideo = element2.select("iframe").attr("src").replace("jk.php?u=", "");
						listavideos.put(nombre, urlVideo);
					}
				} else {
					topicList = doc.select("div.video_option_act").select("a");
					Elements video = doc.select("div.the_video-1");
					for (int i = 0; i < topicList.size(); i++) {
						Element element1 = topicList.get(i);
						Element element2 = video.get(i);
						String nombre = element1.text();
						String urlVideo = element2.select("iframe").attr("src").replace("jk.php?u=", "");
						listavideos.put(nombre, urlVideo);
					}
				}

				return "ok";
			} catch (Throwable t) {
				t.printStackTrace();
				Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
						Toast.LENGTH_SHORT).show();
				ServidorUtil.appendLog(t.getMessage());
				return "error";
			}

		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			if (s.equals("ok")) {
				try {
					Log.e("listavideos", listavideos.toString());
					ServidorUtil
							.onCreateDialogVideo(listavideos, capitulo, accion, context, server)
							.show();
				} catch (Exception e) {
					if (e.getClass().equals(SocketTimeoutException.class)) {
						Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
						toast.setText("Tiempo de conexion agotado");
						toast.show();
					} else {
						e.printStackTrace();
						Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
								Toast.LENGTH_SHORT).show();
						ServidorUtil.appendLog(e.getMessage());
					}
				}
			}
			pDialog.dismiss();
		}
	}

	private class GetVideosJkanimeCo extends AsyncTask<String, Void, String> {
		private ProgressDialog pDialog;
		private TreeMap<String, String> listavideos = new TreeMap<String, String>();
		private Capitulo capitulo;
		private String accion;
		private String server;
		private Context context;

		public GetVideosJkanimeCo(Capitulo animeProgramacion, String accion, String server, Context context) {
			this.capitulo = animeProgramacion;
			this.accion = accion;
			this.server = server;
			this.context = context;
		}

		@Override
		protected void onPreExecute() {

			pDialog = new ProgressDialog(context);
			pDialog.setIndeterminate(false);
			pDialog.setMessage("Obteniendo enlaces...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected String doInBackground(String... strings) {

			try {
				Document doc = null;
				String url = capitulo.getUrl();
				Log.e("buscarVideosConexion", "Connecting to [" + url + "]");
				Response response = Jsoup.connect(url).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
						.userAgent("Mozilla/5.0").execute();
				doc = Jsoup.parse(response.body());
				Log.d("buscarVideosConexion", "Connected to [" + url + "]");
				Elements topicList = doc.select("div.anime_video_body_watch script");
				Pattern p = Pattern.compile("data: '(.+?)'");
				Matcher m = p.matcher(topicList.html().toString());
				ArrayList<NameValuePair> values = new ArrayList<>();
				if (m.find()) {
					String[] id = (m.group().replace("'", "").replace("data:", "").trim()).split("&");
					for (int i = 0; i < id.length; i++) {
						String[] value = id[i].split("=");
						values.add(new BasicNameValuePair(value[0], value[1]));
					}
				}
				String php = ServidorUtil.getNombrePhpJkanimeco(doc.html());
				String urlVideo = JkanimeCo.videoJkanimeCo(php, values);
				Log.e("urlVideo", urlVideo);
				p = Pattern.compile("var part = (\\[(.*?)\\])");
				String a = urlVideo.replaceAll("[\n\r]", "");
				m = p.matcher(a);
				if (m.find()) {
					urlVideo = m.group(2).trim().replace("\"", "");
				}
				Log.e("urlVideo1", urlVideo);
				for (int i = 0; i < urlVideo.split("http").length; i++) {
					listavideos.put("Opcion " + (i + 1), "http" + urlVideo.split("http")[i]);
				}

				return "ok";
			} catch (Throwable e) {
				if (e.getClass().equals(SocketTimeoutException.class)) {
					Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
					toast.setText("Tiempo de conexion agotado");
					toast.show();
				} else {
					e.printStackTrace();
					Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
							Toast.LENGTH_SHORT).show();
					ServidorUtil.appendLog(e.getMessage());
				}
				return "error";
			}
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			if (s.equals("ok")) {
				try {
					Log.e("listavideos", listavideos.toString());
					ServidorUtil
							.onCreateDialogVideo(listavideos, capitulo, accion, context, server)
							.show();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
							Toast.LENGTH_SHORT).show();
					ServidorUtil.appendLog(e.getMessage());
				}
			}
			pDialog.dismiss();
		}
	}

	private class GetVideosReyanime extends AsyncTask<String, Void, String> {
		private ProgressDialog pDialog;
		private TreeMap<String, String> listavideos = new TreeMap<String, String>();
		private Capitulo capitulo;
		private List<String> listaServidoresReyanime = Arrays.asList("M", "Roku", "N", "Zero");
		private String accion;
		private String server;
		private Context context;

		public GetVideosReyanime(Capitulo animeProgramacion, String accion, String server, Context context) {
			this.capitulo = animeProgramacion;
			this.accion = accion;
			this.server = server;
			this.context = context;
		}

		@Override
		protected void onPreExecute() {

			pDialog = new ProgressDialog(context);
			pDialog.setIndeterminate(false);
			pDialog.setMessage("Obteniendo enlaces...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected String doInBackground(String... strings) {

			try {
				Document doc = null;
				String url = capitulo.getUrl();
				Log.e("buscarVideosConexion", "Connecting to [" + url + "]");
				Response response = Jsoup.connect(url).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
						.userAgent("Mozilla/5.0").ignoreContentType(true).execute();
				doc = Jsoup.parse(response.body());
				Elements servidores = doc.select("div.embed a");
				Elements scriptTags = doc.getElementsByTag("script");
				for (Element tag : scriptTags) {
					for (DataNode node : tag.dataNodes()) {
						if (node.getWholeData().contains("iframe")) {
							ArrayList<String> videos = ServidorUtil.getEmbed(node.getWholeData());

							for (int i = 0; i < servidores.size(); i++) {
								try {
									if (listaServidoresReyanime.contains(servidores.get(i).text())) {
										doc = Jsoup.parse(videos.get(i));
										listavideos
												.putAll(ServidorUtil.getVideos(doc, servidores.get(i).text(), context));
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				return "ok";
			} catch (Exception e) {
				if (e.getClass().equals(SocketTimeoutException.class)) {
					Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
					toast.setText("Tiempo de conexion agotado");
					toast.show();
				} else {
					e.printStackTrace();
					Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
							Toast.LENGTH_SHORT).show();
					ServidorUtil.appendLog(e.getMessage());
				}
				return "error";
			}
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			if (s.equals("ok")) {
				try {
					Log.e("listavideos", listavideos.toString());
					ServidorUtil
							.onCreateDialogVideo(listavideos, capitulo, accion, context, server)
							.show();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
							Toast.LENGTH_SHORT).show();
					ServidorUtil.appendLog(e.getMessage());
				}
			}
			pDialog.dismiss();
		}
	}

}
