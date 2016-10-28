package com.animefinderapp.adaptadores;

import java.util.ArrayList;

import com.animefinderapp.R;
import com.animefinderapp.actividades.AnimeActivity;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.Capitulo;
import com.animefinderapp.utilidad.CropSquareTransformationGlide;
import com.animefinderapp.utilidad.ServidorUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ProgramacionAdapter extends RecyclerView.Adapter<ProgramacionAdapter.ViewHolder> {

	private ArrayList<Capitulo> capitulos;
	private Context context;
	private int resourse;
	private AnimeDataSource source;
	private String server;

	public ProgramacionAdapter(Context context,AnimeDataSource source, int resource, ArrayList<Capitulo> programacion, String server) {
		this.context = context;
		this.resourse = resource;
		this.capitulos = programacion;
		this.source = source;
		this.server = server;
	}

	@Override
	public int getItemCount() {
		return capitulos.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(resourse, viewGroup, false);
		ViewHolder viewHolder = new ViewHolder(view);
		return viewHolder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, final int i) {
		final Capitulo capitulo = capitulos.get(i);
		viewHolder.titulo.setText(capitulo.getAnime().getTitulo());
		viewHolder.capitulos.setText(capitulo.getTitulo());
		Glide.with(context).load(capitulo.getAnime().getImagen()).diskCacheStrategy(DiskCacheStrategy.ALL)
				.bitmapTransform(new CropSquareTransformationGlide(context)).placeholder(R.drawable.loadimage)
				.into(viewHolder.imagen);
		viewHolder.ver.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ServidorUtil.verificaConexion(context)) {
					if (!source.getAllCapitulosVistos(server, capitulo.getAnime().getUrl())
							.contains(capitulo.getUrl())) {
						source.agregarCapitulo(server, capitulo.getAnime().getUrl(), capitulo.getUrl());
						source.agregarHistorial(capitulo, server);
					}
					ServidorUtil.buscarVideos(capitulo, "ver", server, context);
				} else {
					Snackbar.make(v, "Sin conexion", Snackbar.LENGTH_SHORT).show();
				}

			}
		});
		viewHolder.anime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ServidorUtil.verificaConexion(context)) {
					Intent i = new Intent(context, AnimeActivity.class);
					i.putExtra("url", capitulo.getAnime().getUrl());
					context.startActivity(i);
				} else {
					Snackbar.make(v, "Sin conexion", Snackbar.LENGTH_SHORT).show();
				}

			}
		});
		viewHolder.imagen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ServidorUtil.verificaConexion(context)) {
					Intent i = new Intent(context, AnimeActivity.class);
					i.putExtra("url", capitulo.getAnime().getUrl());
					context.startActivity(i);
				} else {
					Snackbar.make(v, "Sin conexion", Snackbar.LENGTH_SHORT).show();
				}

			}
		});
		viewHolder.descargar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (ServidorUtil.verificaConexion(context)) {
						if (!source.getAllCapitulosVistos(server, capitulo.getAnime().getUrl())
								.contains(capitulo.getUrl())) {
							source.agregarCapitulo(server, capitulo.getAnime().getUrl(), capitulo.getUrl());
							source.agregarHistorial(capitulo, server);
						}
						ServidorUtil.buscarVideos(capitulo, "descargar", server, context);
					} else {
						Snackbar.make(v, "Sin conexion", Snackbar.LENGTH_SHORT).show();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView titulo;
		TextView capitulos;
		ImageView imagen;
		Button ver;
		Button anime;
		Button descargar;

		ViewHolder(View itemView) {
			super(itemView);
			imagen = (ImageView) itemView.findViewById(R.id.imagenProgramacion);
			titulo = (TextView) itemView.findViewById(R.id.tituloProgramacion);
			capitulos = (TextView) itemView.findViewById(R.id.capitulosProgramacion);
			ver = (Button) itemView.findViewById(R.id.verProgramacion);
			anime = (Button) itemView.findViewById(R.id.animeProgramacion);
			descargar = (Button) itemView.findViewById(R.id.descargarProgramacion);
		}
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
	}

	public void addAll(ArrayList<Capitulo> lista) {
		int first = capitulos.size();
		capitulos.addAll(lista);
		notifyItemRangeInserted(first, lista.size());
	}

	public void removeAll() {
		int last = capitulos.size();
		capitulos.clear();
		notifyItemRangeRemoved(0, last);
	}

}
