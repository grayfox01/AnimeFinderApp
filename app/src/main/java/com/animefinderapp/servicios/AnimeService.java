package com.animefinderapp.servicios;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.animefinderapp.R;
import com.animefinderapp.actividades.MainActivity;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.entidades.Capitulo;
import com.animefinderapp.utilidad.CropCircleTransformation;
import com.animefinderapp.utilidad.ServidorUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class AnimeService extends Service {
	private IBinder mBinder = new LocalBinder();
	private SharedPreferences sharedPref;
	private String servidor;
	private String notificaciones;
	private Timer timer;
	private TimerTask doAsynchronousTask;

	public class LocalBinder extends Binder {
		public AnimeService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return AnimeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	private void displayNotification(ArrayList<Capitulo> lista) {
		try {
			ArrayList<AnimeFavorito> listafavoritos = AnimeDataSource.getAllFavoritos(servidor,this);
			if (notificaciones.equals("favoritos")) {

				for (Capitulo capitulo : lista) {
					AnimeFavorito favorito= new AnimeFavorito();
					favorito.setAnime(capitulo.getAnime());
					int index = listafavoritos.indexOf(favorito);
					if (index != -1) {
						favorito = listafavoritos.get(index);
						if (!favorito.getCapituloNotificado().equals(capitulo)) {
							mostrarNotification(capitulo);
							favorito.setCapituloNotificado(capitulo);
							AnimeDataSource.editarFavorito(favorito, servidor,this);
						}
					}
				}

			} else {
				if (AnimeDataSource.getAllNotificacion(servidor,this) == null) {
					AnimeDataSource.agregarNotificacion(lista.get(0), servidor,this);
				} else {
					Log.e("capitulo", AnimeDataSource.getAllNotificacion(servidor,this));
					Capitulo capitulo = new Capitulo();
					capitulo.setUrl(AnimeDataSource.getAllNotificacion(servidor,this));
					int index = lista.indexOf(capitulo);
					if (index != -1) {
						List<Capitulo> capitulos = lista.subList(0, index);
						for (Capitulo capitulo2 : capitulos) {
							mostrarNotification(capitulo2);
						}

					}
					AnimeDataSource.editarNotificacion(lista.get(0), servidor,this);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("error", e.getLocalizedMessage());
		}
	}

	@SuppressWarnings("unchecked")
	protected void mostrarNotification(final Capitulo capitulo) {
		try {
			final RemoteViews rv = new RemoteViews(AnimeService.this.getPackageName(), R.layout.notificacion_anime);

			rv.setImageViewResource(R.id.remoteview_notification_icon, R.mipmap.ic_launcher);
			rv.setTextViewText(R.id.notification_title, capitulo.getAnime().getTitulo());
			rv.setTextViewText(R.id.notification_description, capitulo.getTitulo());
			rv.setTextViewText(R.id.notification_time,
					DateFormat.getTimeInstance().format(new Date(System.currentTimeMillis())));
			// Invoking the default notification service //
			Uri alarmSound = Uri
					.parse("android.resource://" + AnimeService.this.getPackageName() + "/" + R.raw.notification);
			final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(AnimeService.this);
			mBuilder.setContentTitle(capitulo.getAnime().getTitulo()).setContentText(capitulo.getTitulo())
					.setTicker("Capitulos disponibles").setContent(rv).setAutoCancel(true)
					.setStyle(new NotificationCompat.BigTextStyle().bigText(capitulo.getTitulo()))
					.setSmallIcon(R.mipmap.ic_launcher).setVibrate(new long[] { 0, 1000, 1000, 0, 0 })
					.setLights(Color.WHITE, 3000, 3000).setSound(alarmSound);
			// Creates an explicit intent for an Activity in your app //

			Intent resultIntent = new Intent(AnimeService.this, MainActivity.class);

			TaskStackBuilder stackBuilder = TaskStackBuilder.create(AnimeService.this);
			stackBuilder.addParentStack(MainActivity.class);

			// Adds the Intent that starts the Activity to the top of the stack
			// //
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent);

			// notificationID allows you to update the notification later on. //
			final Notification notification = mBuilder.build();
			if (android.os.Build.VERSION.SDK_INT >= 16) {
				notification.bigContentView = rv;
			}
			NotificationTarget notificationTarget = new NotificationTarget(AnimeService.this, rv,
					R.id.remoteview_notification_icon, notification, capitulo.hashCode());

			Glide.with(AnimeService.this) // safer!
					.load(capitulo.getAnime().getImagen()).asBitmap()
					.transform(new CropCircleTransformation(AnimeService.this)).into(notificationTarget);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		callAsynchronousTask();
		return START_STICKY;
	}

	public void callAsynchronousTask() {
		final Handler handler = new Handler();
		timer = new Timer();
		doAsynchronousTask = new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						try {
							sharedPref = PreferenceManager.getDefaultSharedPreferences(AnimeService.this);
							servidor = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
							notificaciones = sharedPref.getString("pref_list_notificaciones", "Todos").toLowerCase();
							if (ServidorUtil.verificaConexion(AnimeService.this)) {
								new BuscarProgramacion(servidor).execute();
							} else {
								Log.e("error", "No hay conexion a internet");
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		};
		timer.schedule(doAsynchronousTask, 0, 60000); // execute in every 1 min

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (timer != null && doAsynchronousTask != null) {
			timer.cancel();
			doAsynchronousTask.cancel();
		}
	}

	public AnimeService() {
		// TODO Auto-generated constructor stub
	}

	private class BuscarProgramacion extends AsyncTask<String, Void, String> {
		private ArrayList<Capitulo> listaProgramacion;
		private String server;

		public BuscarProgramacion(String server) {
			this.listaProgramacion = new ArrayList<Capitulo>();
			this.server = server;
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(String... strings) {
			try {
				listaProgramacion = ServidorUtil.buscarProgramacion(server, AnimeService.this);
				return "ok";
			} catch (Exception e) {
				e.printStackTrace();
				return "error";
			}
		}

		@Override
		protected void onPostExecute(final String s) {
			super.onPostExecute(s);
			if (!listaProgramacion.isEmpty()) {
				displayNotification(listaProgramacion);
			}
		}

	}

}
