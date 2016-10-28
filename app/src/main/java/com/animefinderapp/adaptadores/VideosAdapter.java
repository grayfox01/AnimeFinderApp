package com.animefinderapp.adaptadores;

import java.io.File;
import java.util.ArrayList;

import com.animefinderapp.R;
import com.animefinderapp.entidades.Video;
import com.animefinderapp.utilidad.ServidorUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.ViewHolder> {

	private ArrayList<Video> videos;
	private int resource;
	private Context context;

	public VideosAdapter(Context context, ArrayList<Video> videos, int resource) {
		this.videos = videos;
		this.resource = resource;
		this.context = context;
	}

	@Override
	public int getItemCount() {
		return videos.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(resource, viewGroup, false);
		ViewHolder viewHolder = new ViewHolder(view);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
		Glide.with(context).load(videos.get(i).getFilepath()).placeholder(R.drawable.ic_movie_black_48dp).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
				.into(viewHolder.imagen);
		viewHolder.titulo.setText(videos.get(i).getFileName());
		viewHolder.peso.setText(videos.get(i).getFileSize());
		viewHolder.tipo.setText(videos.get(i).getFileType());
		viewHolder.duracion.setText(videos.get(i).getFileDuration());
		viewHolder.itemView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ver(i);
			}
		});
		viewHolder.itemView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				PopupMenu popup = new PopupMenu(context, v);

				// This activity implements OnMenuItemClickListener
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {

						switch (item.getItemId()) {
						case R.id.eliminarDescarga:

							File file = new File(videos.get(i).getFilepath());
							boolean delete;
							do {
								delete = file.delete();
								file = new File(videos.get(i).getFilepath());
							} while (file.exists());
							if (delete) {
								videos.remove(i);
								notifyItemRemoved(i);
								Snackbar.make(viewHolder.imagen, "Video Eliminado", Snackbar.LENGTH_SHORT);
							}
							return true;
						case R.id.verDescarga:
							ver(i);
							return true;
						}
						return false;
					}
				});
				popup.inflate(R.menu.acciones_descargas);
				popup.show();
				return false;
			}
		});
	}

	private void ver(int i) {
		ServidorUtil.invokeReproductorExterno(context, videos.get(i).getFilepath(), videos.get(i).getFileType());
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView titulo;
		TextView peso;
		TextView tipo;
		TextView duracion;
		ImageView imagen;

		ViewHolder(View itemView) {
			super(itemView);
			imagen = (ImageView) itemView.findViewById(R.id.imagenVideo);
			titulo = (TextView) itemView.findViewById(R.id.tituloVideo);
			peso = (TextView) itemView.findViewById(R.id.pesoVideo);
			tipo = (TextView) itemView.findViewById(R.id.formatoVideo);
			duracion = (TextView) itemView.findViewById(R.id.duracionVideo);

		}

	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
	}

	public void addAll(ArrayList<Video> lista) {
		int first = videos.size();
		videos.addAll(lista);
		notifyItemRangeInserted(first, lista.size());
	}

	public void removeAll() {
		int last = videos.size();
		videos.clear();
		notifyItemRangeRemoved(0, last);
	}

}
