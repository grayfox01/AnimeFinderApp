package com.animefinderapp.baseDatos;

import com.animefinderapp.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AnimeDbHelper extends SQLiteOpenHelper {

	public final static String DATABASE_NAME = "animeDb";
	private static int DATABASE_VERSION = 1;
	private Context context;

	public AnimeDbHelper(Context context) {
		super(context, DATABASE_NAME,// String name
				null,// factory
				DATABASE_VERSION);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String[] a = context.getResources().getStringArray(
				R.array.pref_server_valores);
		for (int i = 0; i < a.length; i++) {
			String server = a[i].toLowerCase();
			db.execSQL(AnimeDataSource.CREATE_FAVORITOS_SCRIPT(server));
			db.execSQL(AnimeDataSource.CREATE_CAPITULOS_VISTOS_SCRIPT(server));
			db.execSQL(AnimeDataSource.CREATE_NOTIFICACIONES_SCRIPT(server));
			db.execSQL(AnimeDataSource.CREATE_HISTORIAL_SCRIPT(server));
			db.execSQL(AnimeDataSource.CREATE_ANIMES_VISTOS_SCRIPT(server));
			
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	
	

}
