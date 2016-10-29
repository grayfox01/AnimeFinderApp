package com.animefinderapp.utilidad;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.animefinderapp.R;
import com.animefinderapp.adaptadores.AnimeAdapter;
import com.animefinderapp.adaptadores.AnimeAdapter2;
import com.animefinderapp.adaptadores.GenerosAdapter;
import com.animefinderapp.adaptadores.ProgramacionAdapter;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.Anime;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.entidades.Capitulo;
import com.animefinderapp.entidades.Descarga;
import com.animefinderapp.entidades.Video;
import com.animefinderapp.factory.GetVideosFactory;
import com.animefinderapp.factory.ServidoresFactory;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class ServidorUtil {
    private static ServidoresFactory servidoresFactory = ServidoresFactory.getInstance();
    private static GetVideosFactory getVideosFactory = GetVideosFactory.getInstance();

    public static Anime buscarInfoAnime(String servidor, String urlAnime, final Context context) {

        return servidoresFactory.getServidor(servidor).buscarInfoAnime(urlAnime, context);
    }

    public static AnimeAdapter getAnimeAdapter(String tipo, String server,
                                               Context c) {
        switch (tipo) {
            case "lista":
                return new AnimeAdapter(c, R.layout.lista_anime_layout_list, new ArrayList<AnimeFavorito>(), server);

            case "grid":
                return new AnimeAdapter(c, R.layout.lista_anime_layout_grid, new ArrayList<AnimeFavorito>(), server);
            default:
                return new AnimeAdapter(c, R.layout.lista_anime_layout_list, new ArrayList<AnimeFavorito>(), server);
        }
    }

    public static AnimeAdapter2 getAnimeAdapter2(String tipo, String server, Context c) {
        switch (tipo) {
            case "lista":
                return new AnimeAdapter2(c, R.layout.lista_anime_layout_list, new ArrayList<AnimeFavorito>(), server);

            case "grid":
                return new AnimeAdapter2(c, R.layout.lista_anime_layout_grid, new ArrayList<AnimeFavorito>(), server);
            default:
                return new AnimeAdapter2(c, R.layout.lista_anime_layout_list, new ArrayList<AnimeFavorito>(), server);
        }
    }

    public static void showMensageError(Exception e, View v) {
        if (e.getClass().equals(SocketTimeoutException.class)) {
            Snackbar.make(v, "Tiempo de conexion agotado", Snackbar.LENGTH_SHORT).show();
        } else {
            e.printStackTrace();
            Snackbar.make(v, "Error general:\n Revise el log de errores para mas informacion.", Snackbar.LENGTH_SHORT)
                    .show();
            appendLog(e.getLocalizedMessage());
        }
    }

    public static ProgramacionAdapter getProgramacionAdapter(String tipo, String server, Context c) {
        switch (tipo) {
            case "lista":
                return new ProgramacionAdapter(c, R.layout.lista_programacion_row_list, new ArrayList<Capitulo>(), server);

            case "grid":
                return new ProgramacionAdapter(c, R.layout.lista_programacion_row_grid, new ArrayList<Capitulo>(), server);
            default:
                return new ProgramacionAdapter(c, R.layout.lista_programacion_row_list, new ArrayList<Capitulo>(), server);
        }
    }

    public static GenerosAdapter getGenerosAdapter() {
        return new GenerosAdapter(R.layout.lista_letras_generos_row_list, new ArrayList<String[]>());
    }

    public static LayoutManager getlayout(String tipo, Context c) {
        switch (tipo) {
            case "grid":
                return new GridLayoutManager(c, 2);
            case "lista":
                return new LinearLayoutManager(c);
            default:
                return new LinearLayoutManager(c);
        }
    }

    public static ArrayList<Capitulo> buscarProgramacion(String servidor, final Context context,
                                                         final ProgressDialog pDialog) {

        return servidoresFactory.getServidor(servidor).buscarProgramacion(context, pDialog);
    }

    public static ArrayList<Capitulo> buscarProgramacion(String servidor, final Context context) {

        return servidoresFactory.getServidor(servidor).buscarProgramacion(context);
    }

    public static ArrayList<AnimeFavorito> buscarAnime(String servidor, String cadenaBusqueda, int pagina,
                                                       final Context context) {

        return servidoresFactory.getServidor(servidor).buscarAnime(cadenaBusqueda, pagina, context);
    }

    public static void buscarVideos(Capitulo animeProgramacion, String accion, String servidor, Context context) {
        getVideosFactory.getVideo(animeProgramacion, accion, servidor, context).execute();
    }

    public static ArrayList<AnimeFavorito> buscarAnimeLetra(String servidor, String cadenaBusqueda, int pagina,
                                                            final Context context) {

        return servidoresFactory.getServidor(servidor).buscarAnimeLetra(cadenaBusqueda, pagina, context);
    }

    public static ArrayList<AnimeFavorito> buscarAnimeGenero(String servidor, String cadenaBusqueda, int pagina,
                                                             final Context context) {

        return servidoresFactory.getServidor(servidor).buscarAnimeGenero(cadenaBusqueda, pagina, context);
    }

    public static ArrayList<String[]> buscarGenero(String servidor, final Context context) {

        return servidoresFactory.getServidor(servidor).buscarGeneros(context);
    }

    @SuppressWarnings("deprecation")
    public static boolean verificaConexion(Context ctx) {
        boolean bConectado = false;
        ConnectivityManager connec = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] redes = connec.getAllNetworkInfo();
        for (int i = 0; i < 2; i++) {
            if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                bConectado = true;
            }
        }
        return bConectado;
    }

    public static Dialog onCreateDialogVideo(TreeMap<String, String> listavideos, final Capitulo capitulo,
                                             final String accion, final Context context, final String servidor) {
        Set<String> a = listavideos.keySet();
        Log.e("a", a.toString());
        final List<String> d = Arrays.asList(listavideos.values().toArray(new String[a.size()]));

        Log.e("c", a.toArray().toString());
        CharSequence[] b = a.toArray(new String[a.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (b.length == 0) {
            builder.setMessage("No se han encontrado servidores");
            builder.setTitle("Error");
        } else {
            builder.setTitle("Elija Servidor").setItems(b, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("oncreate", capitulo + "-" + accion + "-" + servidor);
                    try {
                        if (accion.equals("ver")) {
                            invokeReproductorExterno(context, d.get(which), "video/mp4");
                        }
                        if (accion.equals("descargar")) {
                            download(context, d.get(which), servidor, capitulo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
                                Toast.LENGTH_SHORT).show();
                        appendLog(e.getMessage());
                    }
                }
            });
        }

        return builder.create();
    }

    public static long download(Context context, String url, String servidor, Capitulo capitulo) {
        try {
            Log.e("Downloading", url + "-" + servidor + "-" + capitulo.getTitulo() + "-" + capitulo.getAnime().getTitulo());
            // TODO Auto-generated catch block
            long enqueue = 0;
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            String almacenamientoUrl = Environment.getExternalStorageDirectory().toString().trim() + "/Anime/Descargas/"
                    + servidor.trim() + "/" + capitulo.getAnime().getTitulo().replaceAll("\\W+", " ").trim()
                    + File.separator;
            File almacenamiento = new File(almacenamientoUrl);
            while (!almacenamiento.exists()) {
                almacenamiento.mkdirs();
            }
            File archivo = new File(almacenamiento,
                    capitulo.getTitulo().replaceAll("\\W+", " ").trim() + ".mp4");

            Request request = new Request(Uri.parse(url)).setAllowedOverRoaming(true)
                    .setDescription(capitulo.getTitulo()).setTitle(capitulo.getAnime().getTitulo())
                    .setDestinationUri(Uri.fromFile(archivo));
            enqueue = dm.enqueue(request);
            return enqueue;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            showMensageError(e, ((AppCompatActivity) (context)).getSupportActionBar().getCustomView());
            appendLog(e.getMessage());
            return 0;
        }
    }

    public static Dialog onCreateDialogCapitulo(final Capitulo animeProgramacion, final String servidor,
                                                final Context context) {
        Log.e("onCreateDialogCapitulo", "ok");
        final CharSequence[] b = {"Ver", "Descargar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Elija una opcion").setItems(b, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                buscarVideos(animeProgramacion, b[which].toString().toLowerCase(), servidor, context);
            }
        });
        return builder.create();
    }

    public static void invokeReproductorExterno(Context context, String url, String type) {
        Log.e("url", url);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        Uri data = Uri.parse(url);
        intent.setDataAndType(data, type);
        context.startActivity(intent);
    }

    public static void appendLog(String text) {
        File logFile = new File("sdcard/Anime/Errorlog.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        try {
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void lanzarToast(final Context context, final String mensaje) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, "message", Toast.LENGTH_SHORT);
                toast.setText(mensaje);
                toast.show();
            }
        });
    }

    public static ArrayList<String> getEmbed(String text) {
        ArrayList<String> a = new ArrayList<>();
        Pattern p = Pattern.compile("<(iframe|embed|object).+?></(iframe|embed|object)>");
        Matcher m = p.matcher(text.replace("\\", ""));
        while (m.find()) {
            a.add(m.group());
        }
        return a;
    }

    public static Hashtable<String, String> getEmbedTableAnimeFlv(String text) {
        Hashtable<String, String> a = new Hashtable<>();
        Pattern p;
        Matcher m;
        String embed;
        String titulo;
        p = Pattern.compile("\\[(.*?)\\]");
        m = p.matcher(text.replace("\\", "").replace("\"", ""));
        while (m.find()) {
            Pattern p1;
            Matcher m1;
            embed = m.group();
            titulo = embed.split(",")[9];
            p1 = Pattern.compile("<(iframe|embed|object).+?></(iframe|embed|object)>");
            m1 = p1.matcher(embed);
            if (m1.find()) {
                a.put(titulo.toLowerCase(), m1.group());
            }
        }
        return a;
    }

    public static String getUrlVideo(String text) {
        String a = "";
        Pattern p = Pattern.compile("https://.+?/.+?\\.mp4");
        Matcher m = p.matcher(text);
        if (m.find()) {
            a = m.group();
        } else {
            p = Pattern.compile("http://.+?/.+?\\.mp4");
            m = p.matcher(text);
            if (m.find()) {
                a = m.group();
            }
        }
        return a;
    }

    public static String getNombrePhpJkanimeco(String text) {
        String a = "";
        Pattern p = Pattern.compile("http://jkanime.co/lib/.+?\\.php");
        Matcher m = p.matcher(text);
        if (m.find()) {
            a = m.group();
        } else {
            p = Pattern.compile("https://jkanime.co/lib/.+?\\.php");
            m = p.matcher(text);
            if (m.find()) {
                a = m.group();
            }
        }
        return a;
    }

    public static Hashtable<String, String> getVideos(Document doc, String servidor, Context context) {
        Hashtable<String, String> listavideos = new Hashtable<>();
        Response response = null;
        try {
            while (!doc.select("iframe").isEmpty() || !doc.select("object.player").isEmpty()
                    || !doc.select("embed").isEmpty()) {
                if (!doc.select("iframe").isEmpty()) {
                    Log.e("urlIframe" + servidor, doc.select("iframe").attr("src"));
                    if (getUrlVideo(doc.select("iframe").attr("src")).contains("mp4")) {
                        if (!listavideos.contains(getUrlVideo(doc.select("iframe").attr("src")).trim())) {
                            listavideos.put(servidor, getUrlVideo(doc.select("iframe").attr("src")));
                            doc = Jsoup.parse("");
                        }
                    } else {
                        response = Jsoup.connect(doc.select("iframe").attr("src")).timeout(5000).ignoreHttpErrors(true)
                                .followRedirects(true).userAgent("Mozilla/5.0").ignoreContentType(true).execute();
                        doc = Jsoup.parse(response.body());
                    }
                }
                if (!doc.select("embed").isEmpty()) {
                    Log.e("urlIEmbed" + servidor, getUrlVideo(doc.select("embed").attr("flashvars")));
                    if (getUrlVideo(doc.select("embed").attr("flashvars")).contains("mp4")) {
                        if (!listavideos.contains(getUrlVideo(doc.select("embed").attr("flashvars")).trim())) {
                            listavideos.put(servidor, getUrlVideo(doc.select("embed").attr("flashvars")));
                            doc = Jsoup.parse("");
                        }

                    } else {
                        response = Jsoup.connect(getUrlVideo(doc.select("embed").attr("flashvars"))).timeout(5000)
                                .ignoreHttpErrors(true).followRedirects(true).userAgent("Mozilla/5.0")
                                .ignoreContentType(true).execute();
                        doc = Jsoup.parse(response.body());
                    }
                }
                if (!doc.select("object").isEmpty()) {
                    Log.e("urlIObjectF" + servidor,
                            getUrlVideo(doc.select("object param[name=Flashvars]").attr("value").trim()));
                    if (getUrlVideo(doc.select("object param[name=Flashvars]").attr("value")).contains("mp4")) {
                        if (!listavideos.contains(
                                getUrlVideo(doc.select("object param[name=Flashvars]").attr("value")).trim())) {
                            listavideos.put(servidor,
                                    getUrlVideo(doc.select("object param[name=Flashvars]").attr("value")));
                            doc = Jsoup.parse("");
                        }

                    } else {

                        response = Jsoup
                                .connect(getUrlVideo(doc.select("object.player param[name=flashvars]").attr("value")))
                                .timeout(5000).ignoreHttpErrors(true).followRedirects(true).userAgent("Mozilla/5.0")
                                .ignoreContentType(true).execute();
                        doc = Jsoup.parse(response.body());
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
                    Toast.LENGTH_SHORT).show();
            appendLog(e.getMessage());
        }
        return listavideos;

    }

    public static String getVideoAnimeFlv(String servidor, String embed, Context context) {
        try {

            Pattern p;
            Matcher m;
            Document doc;
            Response response;
            String video;
            switch (servidor) {
                case "nowvideo":

                    p = Pattern.compile("\\?v=(.+?)&");
                    m = p.matcher(embed);
                    if (m.find()) {
                        video = m.group();
                        Log.e("video", video);
                        video = "http://www.nowvideo.sx/mobile/video.php?id=" + video.replace("v=", "").replace("&", "");
                        response = Jsoup.connect(video).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
                                .userAgent("Mozilla/5.0").ignoreContentType(true).execute();
                        p = Pattern.compile("<source src=\"(.+?)\" type=\"video/(mp4|flv)\">");
                        m = p.matcher(response.body());
                        if (m.find()) {
                            Document document = Jsoup.parse(m.group());
                            return document.select("source").attr("src");
                        }
                    }

                    break;
                case "mp4upload":

                    doc = Jsoup.parse(embed);
                    response = Jsoup.connect(doc.select("iframe").attr("src")).timeout(5000).ignoreHttpErrors(true)
                            .followRedirects(true).userAgent("Mozilla/5.0").ignoreContentType(true).execute();
                    m = Pattern.compile("'file': '(.+?)'").matcher(response.body());
                    if (m.find()) {

                        return m.group().replace("'file':", "").replace("'", "").trim();
                    }

                    break;
                case "novamov":
                    p = Pattern.compile("&v=(.+?)&px=1");
                    m = p.matcher(embed);
                    if (m.find()) {
                        video = m.group();
                        Log.e("video", video);
                        video = "http://www.novamov.com/mobile/video.php?id=" + video.replace("&v=", "");
                        response = Jsoup.connect(video).timeout(5000).ignoreHttpErrors(true).followRedirects(true)
                                .userAgent("Mozilla/5.0").ignoreContentType(true).execute();
                        p = Pattern.compile("<source src=\"(.+?)\" type=\"video/(mp4|flv)\">");
                        m = p.matcher(response.body());
                        if (m.find()) {
                            Document document = Jsoup.parse(m.group());
                            return document.select("source").attr("src");
                        }
                    }
                    break;
                case "izanagi":

                    doc = Jsoup.parse(embed);
                    response = Jsoup.connect(doc.select("iframe").attr("src")).timeout(5000).ignoreHttpErrors(true)
                            .followRedirects(true).userAgent("Mozilla/5.0").ignoreContentType(true).execute();
                    m = Pattern.compile("file: '(.+?)'").matcher(response.body());
                    if (m.find()) {

                        return m.group().replace("file:", "").replace("'", "").trim();
                    }

                    break;
                case "aflv":
                    Log.e("embed", embed);
                    doc = Jsoup.parse(embed);
                    response = Jsoup.connect(doc.select("iframe").attr("src")).timeout(5000).ignoreHttpErrors(true)
                            .followRedirects(true).userAgent("Mozilla/5.0").ignoreContentType(true).execute();
                    m = Pattern.compile("file: '(.+?)'").matcher(response.body());
                    if (m.find()) {

                        return m.group().replace("file:", "").replace("'", "").trim();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error general:\n Revise el log de errores para mas informacion.",
                    Toast.LENGTH_SHORT).show();
            appendLog(e.getMessage());
        }
        return null;
    }

    public static ArrayList<Descarga> getDownloads(String servidor, Context context) {
        ArrayList<Descarga> listaDescargas = new ArrayList<>();
        try {
            String directorioUrl = Environment.getExternalStorageDirectory().toString() + "/Anime/Descargas/"
                    + servidor.trim() + "/";
            File directorio = new File(directorioUrl);

            if (directorio.exists()) {
                for (File carpeta : directorio.listFiles()) {
                    if (carpeta.isDirectory() && carpeta.listFiles(new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String filename) {
                            if (filename.endsWith(".mp4") || filename.endsWith(".mp4") || filename.endsWith(".flv")
                                    || filename.endsWith(".Flv")) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }).length > 0) {
                        Descarga descarga = new Descarga(carpeta.getName(), carpeta.getAbsolutePath(),
                                getVideosDescarga(context, carpeta));
                        listaDescargas.add(descarga);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        return listaDescargas;
    }

    private static ArrayList<Video> getVideosDescarga(Context context, File carpeta) {
        ArrayList<Video> lista = new ArrayList<>();
        for (File video : carpeta.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".mp4") || filename.endsWith(".mp4") || filename.endsWith(".flv")
                        || filename.endsWith(".Flv")) {
                    return true;
                } else {
                    return false;
                }
            }
        })) {
            Video video2 = getVideoInfo(context, video);
            lista.add(video2);
        }
        return lista;
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private static Video getVideoInfo(Context context, File video) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, Uri.fromFile(video));
            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            df.setTimeZone(tz);
            String duracion = df.format(
                    new Date(Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))));
            String titulo = video.getName();
            String size = android.text.format.Formatter.formatFileSize(context, video.length());
            String tipo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            String path = video.getAbsolutePath();
            Video video2 = new Video(titulo, path, tipo, duracion, size);
            appendLog(video2.toString());
            return video2;
        } catch (Exception e) {
            appendLog(e.getMessage());
            return null;
        }
    }

    public static void refresh(final SwipeRefreshLayout layout, final boolean refresh) {
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.setRefreshing(refresh);
            }
        });
    }
}
