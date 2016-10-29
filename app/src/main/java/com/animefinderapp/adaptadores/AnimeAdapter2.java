package com.animefinderapp.adaptadores;

import java.util.ArrayList;

import com.animefinderapp.R;
import com.animefinderapp.actividades.AnimeActivity;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.Anime;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.utilidad.CropSquareTransformationGlide;
import com.animefinderapp.utilidad.ServidorUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class AnimeAdapter2 extends RecyclerView.Adapter<AnimeAdapter2.ViewHolder> {

	private Context context;
	private ArrayList<AnimeFavorito> listaAnimes;
	private int resourse;
	private String server;

	public AnimeAdapter2(Context context, int resource, ArrayList<AnimeFavorito> listaAnimes, String server) {
		this.context = context;
		this.resourse = resource;
		this.listaAnimes = listaAnimes;
		this.server = server;
	}

	@Override
	public int getItemCount() {
		return listaAnimes.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(resourse, viewGroup, false);
		ViewHolder viewHolder = new ViewHolder(view);
		return viewHolder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, final int posicion) {

		Glide.with(context).load(listaAnimes.get(posicion).getAnime().getImagen())
				.diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.loadimage)
				.bitmapTransform(new CropSquareTransformationGlide(context)).into(viewHolder.imagen);
		viewHolder.visto.setVisibility(ImageView.GONE);
		viewHolder.titulo.setText(listaAnimes.get(posicion).getAnime().getTitulo().trim());
		viewHolder.descripcion.setText(listaAnimes.get(posicion).getAnime().getDescripcion().trim());
		viewHolder.itemView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ServidorUtil.verificaConexion(context)) {
					Intent i = new Intent(context, AnimeActivity.class);
					i.putExtra("url", listaAnimes.get(posicion).getAnime().getUrl());
					context.startActivity(i);
					Log.e("posicion", Integer.toString(posicion));
				} else {
					Snackbar.make(v, "Sin conexion", Snackbar.LENGTH_SHORT).show();
				}
			}
		});
		viewHolder.itemView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				AnimeDataSource.eliminarAnimeVisto(listaAnimes.get(posicion), server,context);
				listaAnimes.remove(posicion);
				Snackbar.make(v, "Anime Eliminado", Snackbar.LENGTH_SHORT).show();
				notifyItemRemoved(posicion);
				return false;
			}
		});

	}

	public void addAll(ArrayList<AnimeFavorito> lista) {
		int first= listaAnimes.size();
		listaAnimes.addAll(lista);
		notifyItemRangeInserted(first, lista.size());
	}

	public void removeAll() {
		int last= listaAnimes.size();
		listaAnimes.clear();
		notifyItemRangeRemoved(0, last);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView titulo;
		TextView descripcion;
		ImageView imagen;
		ImageView visto;

		ViewHolder(View itemView) {
			super(itemView);
			imagen = (ImageView) itemView.findViewById(R.id.listaAnimeImagen);
			visto = (ImageView) itemView.findViewById(R.id.isVisto);
			titulo = (TextView) itemView.findViewById(R.id.listaAnimeTitulo);
			descripcion = (TextView) itemView.findViewById(R.id.listaAnimeDescripcion);
		}
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
	}

}
