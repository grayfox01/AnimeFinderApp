package com.animefinderapp.baseDatos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.animefinderapp.actividades.MainActivity;
import com.animefinderapp.entidades.Anime;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.entidades.Capitulo;
import com.animefinderapp.utilidad.ServidorUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

public class AnimeDataSource implements Serializable {

    private static final String FAVORITOS_TABLE_NAME = "Favoritos";
    private static final String NOTIFICACION_TABLE_NAME = "Notificaciones";
    private static final String CAPITULOS_VISTOS_TABLE_NAME = "CapitulosVistos";
    private static final String ANIMES_VISTOS_TABLE_NAME = "AnimeVistos";
    private static final String HISTORIAL_TABLE_NAME = "Historial";
    private static final String STRING_TYPE = "text";
    private static final String INT_TYPE = "integer";

    public AnimeDataSource() {
    }

    // Campos de la tabla Quotes
    private static class TABLA_FAVORITOS {
        static final String URL_ANIME = "url";
        static final String TITULO_ANIME = "titulo";
        static final String DESCRIPCION_ANIME = "descripcion";
        static final String IMAGEN_ANIME = "imagen";
        static final String EPISODIO_NOTIFICADO = "episodionotificado";
    }

    private static class TABLA_ANIMES_VISTOS {
        static final String URL_ANIME = "url";
        static final String TITULO_ANIME = "titulo";
        static final String DESCRIPCION_ANIME = "descripcion";
        static final String IMAGEN_ANIME = "imagen";
    }

    private static class TABLA_HISTORIAL {
        static final String URL_CAPITULO = "url_capitulo";
        static final String TITULO_CAPITULO = "titulo_capitulo";
        static final String URL_ANIME = "url_anime";
        static final String TITULO_ANIME = "titulo_anime";
        static final String IMAGEN_ANIME = "imagen_anime";
    }

    private static class TABLA_CAPITULOS_VISTOS {
        static final String URL_ANIME = "url";
        static final String CAPITULO = "capitulo";
    }

    private static class TABLA_NOTIFICACIONES {
        static final String INDEX = "key";
        static final String URL_ANIME = "url";
    }

    static String CREATE_FAVORITOS_SCRIPT(String server) {
        return "create table " + server.trim() + FAVORITOS_TABLE_NAME + "(" + TABLA_FAVORITOS.URL_ANIME + " "
                + STRING_TYPE + " primary key not null," + TABLA_FAVORITOS.TITULO_ANIME + " " + STRING_TYPE
                + " not null," + TABLA_FAVORITOS.DESCRIPCION_ANIME + " " + STRING_TYPE + " not null,"
                + TABLA_FAVORITOS.IMAGEN_ANIME + " " + STRING_TYPE + " not null," + TABLA_FAVORITOS.EPISODIO_NOTIFICADO
                + " " + STRING_TYPE + " not null)";
    }

    static String CREATE_HISTORIAL_SCRIPT(String server) {
        return "create table " + server.trim() + HISTORIAL_TABLE_NAME + "(" + TABLA_HISTORIAL.URL_CAPITULO
                + " " + STRING_TYPE + " primary key not null," + TABLA_HISTORIAL.TITULO_CAPITULO + " " + STRING_TYPE
                + " not null," + TABLA_HISTORIAL.URL_ANIME + " " + STRING_TYPE + " not null,"
                + TABLA_HISTORIAL.TITULO_ANIME + " " + STRING_TYPE + " not null," + TABLA_HISTORIAL.IMAGEN_ANIME + " "
                + STRING_TYPE + " not null)";
    }

    static String CREATE_ANIMES_VISTOS_SCRIPT(String server) {
        return "create table " + server.trim() + ANIMES_VISTOS_TABLE_NAME + "(" + TABLA_ANIMES_VISTOS.URL_ANIME
                + " " + STRING_TYPE + " primary key not null," + TABLA_ANIMES_VISTOS.TITULO_ANIME + " " + STRING_TYPE
                + " not null," + TABLA_ANIMES_VISTOS.DESCRIPCION_ANIME + " " + STRING_TYPE + " not null,"
                + TABLA_ANIMES_VISTOS.IMAGEN_ANIME + " " + STRING_TYPE + " not null)";
    }

    static String CREATE_NOTIFICACIONES_SCRIPT(String server) {
        return "create table " + server.trim() + NOTIFICACION_TABLE_NAME + "(" + TABLA_NOTIFICACIONES.INDEX
                + " " + INT_TYPE + " primary key not null," + TABLA_NOTIFICACIONES.URL_ANIME + " " + STRING_TYPE
                + " not null)";
    }

    static String CREATE_CAPITULOS_VISTOS_SCRIPT(String server) {
        return "create table " + server + CAPITULOS_VISTOS_TABLE_NAME + "(" + TABLA_CAPITULOS_VISTOS.URL_ANIME
                + " " + STRING_TYPE + " not null," + TABLA_CAPITULOS_VISTOS.CAPITULO + " " + STRING_TYPE + "not null,"
                + "PRIMARY KEY (" + TABLA_CAPITULOS_VISTOS.URL_ANIME + "," + TABLA_CAPITULOS_VISTOS.CAPITULO + "))";
    }

    public static void agregarFavorito(AnimeFavorito favorito, String servidor, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(AnimeDataSource.TABLA_FAVORITOS.URL_ANIME, favorito.getAnime().getUrl());
            values.put(AnimeDataSource.TABLA_FAVORITOS.TITULO_ANIME, favorito.getAnime().getTitulo());
            values.put(AnimeDataSource.TABLA_FAVORITOS.DESCRIPCION_ANIME, favorito.getAnime().getDescripcion());
            values.put(AnimeDataSource.TABLA_FAVORITOS.IMAGEN_ANIME, favorito.getAnime().getImagen());
            values.put(AnimeDataSource.TABLA_FAVORITOS.EPISODIO_NOTIFICADO, "null");
            // Insertando en la base de datos
            database.insert(servidor + FAVORITOS_TABLE_NAME, null, values);

            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }

    }

    public static void uploadDB(final Context context, final FirebaseUser user) {
        try {
            if (user == null) {
                Snackbar.make(((Activity) context).getCurrentFocus(), "Inicie sesion para usar esta caracteristica", Snackbar.LENGTH_LONG).show();
            } else {
                String id = user.getUid();
                final ProgressDialog[] p = {null, null};
                AnimeDbHelper openHelper = new AnimeDbHelper(context);
                SQLiteDatabase database = openHelper.getWritableDatabase();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://animefinderapp-3ab06.appspot.com");
                ContextWrapper cw = new ContextWrapper(context);
                Uri file = Uri.fromFile(cw.getDatabasePath(AnimeDbHelper.DATABASE_NAME));
                StorageReference dbRef = storageRef.child("users").child(id).child("database/" + file.getLastPathSegment());
                UploadTask uploadTask = dbRef.putFile(file);
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        try {

                            long subido = taskSnapshot.getBytesTransferred();
                            long total = taskSnapshot.getTotalByteCount();
                            long porcentaje = 100 * subido / total;
                            if (total != -1) {
                                if (p[0] == null) {
                                    p[0] = new ProgressDialog(context);
                                    p[0].setMessage("Subiendo Base de datos...");
                                    p[0].setCancelable(false);
                                    p[0].setCanceledOnTouchOutside(false);
                                    p[0].setIndeterminate(false);
                                    p[0].setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    p[0].setMax(100);
                                    p[0].setProgress((int) porcentaje);
                                    p[0].show();
                                } else {
                                    p[0].setProgress((int) porcentaje);
                                }
                            }
                            Log.i("onProgress: ", "total:" + total);
                            Log.i("onProgress: ", "subido:" + subido);
                            Log.i("onProgress: ", "porcentaje:" + porcentaje);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        p[0].setMessage("Subida Completada");
                        p[0].dismiss();
                        Snackbar.make(((Activity) context).getCurrentFocus(), "Subida Completada", Snackbar.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Snackbar.make(((Activity) context).getCurrentFocus(), "Descarga Fallida", Snackbar.LENGTH_LONG).show();
                        if (p[0] != null) {
                            p[0].dismiss();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void downloadDB(final Context context, final FirebaseUser user) {
        try {
            if (user == null) {
                Snackbar.make(((Activity) context).getCurrentFocus(), "Inicie sesion para usar esta caracteristica", Snackbar.LENGTH_LONG).show();
            } else {
                String id = user.getUid();
                final ProgressDialog[] p = {null, null};
                AnimeDbHelper openHelper = new AnimeDbHelper(context);
                SQLiteDatabase database = openHelper.getWritableDatabase();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://animefinderapp-3ab06.appspot.com");
                StorageReference userRef = storageRef.child("users").child(id).child("database").child(AnimeDbHelper.DATABASE_NAME);
                userRef.getPath().equals(userRef.getPath());
                final ContextWrapper cw = new ContextWrapper(context);
                final File localFile = File.createTempFile("datoTmp", "db");
                userRef.getFile(localFile).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        try {
                            long descargado = taskSnapshot.getBytesTransferred();
                            long total = taskSnapshot.getTotalByteCount();
                            long porcentaje = 100 * descargado / total;
                            if (total != -1) {
                                if (p[0] == null) {
                                    p[0] = new ProgressDialog(context);
                                    p[0].setMessage("Descargando Base de datos...");
                                    p[0].setCancelable(false);
                                    p[0].setCanceledOnTouchOutside(false);
                                    p[0].setIndeterminate(false);
                                    p[0].setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    p[0].setMax(100);
                                    p[0].setProgress((int) porcentaje);
                                    p[0].show();
                                } else {
                                    p[0].setProgress((int) porcentaje);
                                }
                            }
                            Log.i("onProgress: ", "total:" + total);
                            Log.i("onProgress: ", "descargado:" + descargado);
                            Log.i("onProgress: ", "porcentaje:" + porcentaje);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.e("onSuccess: ", "Descarga completa");
                        Log.e("onSuccess: ", "La nueva base de datos descargada tiene:" + taskSnapshot.getBytesTransferred() + " Bytes");
                        FileChannel source = null;
                        FileChannel destination = null;
                        File currentDB = cw.getDatabasePath(AnimeDbHelper.DATABASE_NAME);
                        Log.e("onSuccess: ", "Se copiara la nueva base de datos a:" + currentDB.getAbsolutePath());
                        try {
                            source = new FileInputStream(localFile).getChannel();
                            destination = new FileOutputStream(currentDB).getChannel();
                            Long transfered = destination.transferFrom(source, 0, source.size());
                            Log.e("onSuccess: ", "La nueva base de datos tiene" + transfered);
                            source.close();
                            destination.close();
                            p[0].setMessage("Descarga Completa");
                            p[0].dismiss();
                            Snackbar.make(((Activity) context).getCurrentFocus(), "Descarga Completa", Snackbar.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        if (exception.getClass().equals(StorageException.class)) {
                            if (((StorageException) exception).getHttpResultCode() != 404) {
                                Snackbar.make(((Activity) context).getCurrentFocus(), "Descarga Fallida", Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(((Activity) context).getCurrentFocus(), "Base de datos no encontrada en el servidor, se subira la base de datos local", Snackbar.LENGTH_LONG).show();
                                uploadDB(context, user);
                            }
                        } else {
                            Snackbar.make(((Activity) context).getCurrentFocus(), "Descarga Fallida", Snackbar.LENGTH_LONG).show();
                        }
                        if (p[0] != null) {
                            p[0].dismiss();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void agregarAnimeVisto(AnimeFavorito favorito, String servidor, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(AnimeDataSource.TABLA_ANIMES_VISTOS.URL_ANIME, favorito.getAnime().getUrl());
            values.put(AnimeDataSource.TABLA_ANIMES_VISTOS.TITULO_ANIME, favorito.getAnime().getTitulo());
            values.put(AnimeDataSource.TABLA_ANIMES_VISTOS.DESCRIPCION_ANIME, favorito.getAnime().getDescripcion());
            values.put(AnimeDataSource.TABLA_ANIMES_VISTOS.IMAGEN_ANIME, favorito.getAnime().getImagen());
            ;
            // Insertando en la base de datos
            database.insert(servidor + ANIMES_VISTOS_TABLE_NAME, null, values);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
            database.close();
        }

    }

    public static void agregarHistorial(Capitulo capitulo, String servidor, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(AnimeDataSource.TABLA_HISTORIAL.URL_CAPITULO, capitulo.getUrl());
            values.put(AnimeDataSource.TABLA_HISTORIAL.TITULO_CAPITULO,
                    capitulo.getTitulo());
            values.put(AnimeDataSource.TABLA_HISTORIAL.URL_ANIME, capitulo.getAnime().getUrl());
            values.put(AnimeDataSource.TABLA_HISTORIAL.TITULO_ANIME,
                    capitulo.getAnime().getTitulo());
            values.put(AnimeDataSource.TABLA_HISTORIAL.IMAGEN_ANIME, capitulo.getAnime().getImagen());
            // Insertando en la base de datos
            database.insert(servidor + HISTORIAL_TABLE_NAME, null, values);

            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }

    }

    public static void agregarNotificacion(Capitulo capitulo, String servidor, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(AnimeDataSource.TABLA_NOTIFICACIONES.INDEX, 1);
            values.put(AnimeDataSource.TABLA_NOTIFICACIONES.URL_ANIME, capitulo.getUrl());
            // Insertando en la base de datos
            database.insert(servidor + NOTIFICACION_TABLE_NAME, null, values);

            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    public static void editarNotificacion(Capitulo capitulo, String servidor, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(AnimeDataSource.TABLA_NOTIFICACIONES.URL_ANIME, capitulo.getUrl());
            // Insertando en la base de datos
            database.update(servidor + NOTIFICACION_TABLE_NAME, values, TABLA_NOTIFICACIONES.INDEX + " = 1", null);

            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    public static void editarFavorito(AnimeFavorito favorito, String servidor, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(TABLA_FAVORITOS.EPISODIO_NOTIFICADO, favorito.getCapituloNotificado().getUrl());
            // Insertando en la base de datos
            database.update(servidor + FAVORITOS_TABLE_NAME, values, TABLA_FAVORITOS.URL_ANIME + "=?",
                    new String[]{favorito.getAnime().getUrl()});

            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    public static void agregarCapitulo(String servidor, String url, String capitulo, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put(AnimeDataSource.TABLA_CAPITULOS_VISTOS.URL_ANIME, url);
            values.put(AnimeDataSource.TABLA_CAPITULOS_VISTOS.CAPITULO, capitulo);// Insertando
            database.insert(servidor + CAPITULOS_VISTOS_TABLE_NAME, null, values);

            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }

    }

    public static void eliminarFavorito(AnimeFavorito favorito, String servidor, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            String selection = TABLA_FAVORITOS.URL_ANIME + " = ?";
            String[] selectionArgs = {favorito.getAnime().getUrl()};
            database.delete(servidor + FAVORITOS_TABLE_NAME, selection, selectionArgs);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    public void eliminarCapitulosVistos(AnimeFavorito favorito, String servidor, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            String selection = TABLA_CAPITULOS_VISTOS.URL_ANIME + " = ?";
            String[] selectionArgs = {favorito.getAnime().getUrl()};
            database.delete(servidor + CAPITULOS_VISTOS_TABLE_NAME, selection, selectionArgs);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    public static void eliminarCapitulo(String servidor, String url, String capitulo, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {

            String selection = TABLA_CAPITULOS_VISTOS.URL_ANIME + " = ? and " + TABLA_CAPITULOS_VISTOS.CAPITULO
                    + " = ?";
            String[] selectionArgs = {url, capitulo};
            database.delete(servidor + CAPITULOS_VISTOS_TABLE_NAME, selection, selectionArgs);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    public static void eliminarAnimeVisto(AnimeFavorito favorito, String servidor, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            String selection = TABLA_ANIMES_VISTOS.URL_ANIME + " = ?";
            String[] selectionArgs = {favorito.getAnime().getUrl()};
            database.delete(servidor + ANIMES_VISTOS_TABLE_NAME, selection, selectionArgs);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
    }

    public static boolean vaciarHistorial(String servidor, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        try {
            boolean vaciar = database.delete(servidor + HISTORIAL_TABLE_NAME, "1", null) > 0;
            return vaciar;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getAllNotificacion(String server, Context context) {
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        String url = null;
        try {
            Cursor c = database.rawQuery("select * from " + server + NOTIFICACION_TABLE_NAME + " where "
                    + TABLA_NOTIFICACIONES.INDEX + " = '" + 1 + "'", null);

            while (c.moveToNext()) {
                url = c.getString(c.getColumnIndex(TABLA_NOTIFICACIONES.URL_ANIME));
            }
            c.close();
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }

        return url;

    }

    public static ArrayList<AnimeFavorito> getAllFavoritos(String server, Context context) {
        ArrayList<AnimeFavorito> arrayList = new ArrayList<>();
        AnimeFavorito favorito;
        Anime anime;
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            Cursor c = database.rawQuery("select * from " + server + FAVORITOS_TABLE_NAME, null);

            while (c.moveToNext()) {
                anime = new Anime(c.getString(c.getColumnIndex(TABLA_FAVORITOS.URL_ANIME)),
                        c.getString(c.getColumnIndex(TABLA_FAVORITOS.IMAGEN_ANIME)), null,
                        c.getString(c.getColumnIndex(TABLA_FAVORITOS.TITULO_ANIME)),
                        c.getString(c.getColumnIndex(TABLA_FAVORITOS.DESCRIPCION_ANIME)), null, null);
                favorito = new AnimeFavorito(anime,
                        new Capitulo(null, null, c.getString(c.getColumnIndex(TABLA_FAVORITOS.EPISODIO_NOTIFICADO))));
                arrayList.add(favorito);
            }
            c.close();
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }

        return arrayList;

    }

    public static ArrayList<AnimeFavorito> getAllAnimeVisto(String server, Context context) {
        ArrayList<AnimeFavorito> arrayList = new ArrayList<>();
        AnimeFavorito favorito;
        Anime anime;
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            Cursor c = database.rawQuery("select * from " + server + ANIMES_VISTOS_TABLE_NAME, null);

            while (c.moveToNext()) {
                anime = new Anime(c.getString(c.getColumnIndex(TABLA_FAVORITOS.URL_ANIME)),
                        c.getString(c.getColumnIndex(TABLA_FAVORITOS.IMAGEN_ANIME)), null,
                        c.getString(c.getColumnIndex(TABLA_FAVORITOS.TITULO_ANIME)),
                        c.getString(c.getColumnIndex(TABLA_FAVORITOS.DESCRIPCION_ANIME)), null, null);
                favorito = new AnimeFavorito(anime, null);
                arrayList.add(favorito);
            }
            c.close();
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
        Collections.sort(arrayList, new Comparator<AnimeFavorito>() {
            @Override
            public int compare(AnimeFavorito animeFavorito1, AnimeFavorito animeFavorito2) {

                return animeFavorito1.getAnime().getTitulo().compareTo(animeFavorito2.getAnime().getTitulo());
            }
        });
        return arrayList;

    }

    public static ArrayList<Capitulo> getHistorial(String server, Context context) {

        final ArrayList<Capitulo> arrayList = new ArrayList<>();
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            Cursor c = database.rawQuery("select * from " + server + HISTORIAL_TABLE_NAME, null);

            while (c.moveToNext()) {
                Anime anime = new Anime(c.getString(c.getColumnIndex(TABLA_HISTORIAL.URL_ANIME)),
                        c.getString(c.getColumnIndex(TABLA_HISTORIAL.IMAGEN_ANIME)), null,
                        c.getString(c.getColumnIndex(TABLA_HISTORIAL.TITULO_ANIME)), null, null, null);
                Capitulo capitulo = new Capitulo(anime, c.getString(c.getColumnIndex(TABLA_HISTORIAL.TITULO_CAPITULO)),
                        c.getString(c.getColumnIndex(TABLA_HISTORIAL.URL_CAPITULO)));
                arrayList.add(capitulo);
            }
            c.close();
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
        Collections.reverse(arrayList);
        return arrayList;

    }

    public static ArrayList<String> getAllCapitulosVistos(String server, String url, Context context) {

        ArrayList<String> arrayList = new ArrayList<>();
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            Cursor c = database.rawQuery("select * from " + server + CAPITULOS_VISTOS_TABLE_NAME + " where "
                    + TABLA_CAPITULOS_VISTOS.URL_ANIME + " = '" + url + "'", null);

            while (c.moveToNext()) {
                arrayList.add(c.getString(c.getColumnIndex(TABLA_CAPITULOS_VISTOS.CAPITULO)));
            }
            c.close();
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
        return arrayList;

    }

    public static boolean isFavorito(String url, String server, Context context) {
        boolean b = false;
        Cursor c = null;
        AnimeDbHelper openHelper = new AnimeDbHelper(context);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            String columns[] = new String[]{TABLA_FAVORITOS.URL_ANIME};
            String selection = TABLA_FAVORITOS.URL_ANIME + " = ? ";
            String selectionArgs[] = new String[]{url};
            c = database.query(server + FAVORITOS_TABLE_NAME, columns, selection, selectionArgs, null, null, null);
            b = c.moveToFirst();
            c.close();
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }
        return b;
    }

    public static boolean deleteDB(Context context) {
        try {
            ContextWrapper cw = new ContextWrapper(context);
            File currentDB = cw.getDatabasePath(AnimeDbHelper.DATABASE_NAME);
            return SQLiteDatabase.deleteDatabase(currentDB);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
