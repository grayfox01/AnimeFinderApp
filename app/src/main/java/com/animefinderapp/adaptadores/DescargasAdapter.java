package com.animefinderapp.adaptadores;

import java.util.ArrayList;

import com.animefinderapp.R;
import com.animefinderapp.entidades.Descarga;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DescargasAdapter extends RecyclerView.Adapter<DescargasAdapter.ViewHolder> {

	private ArrayList<Descarga> descargas;
	private int resource;
	private static OnItemClickListener mOnItemClickListener;

	public DescargasAdapter(ArrayList<Descarga> descargas, int resource) {
		this.descargas = descargas;
		this.resource = resource;
	}

	@Override
	public int getItemCount() {
		return descargas.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(resource, viewGroup, false);
		ViewHolder viewHolder = new ViewHolder(view);
		return viewHolder;
	}

	public interface OnItemClickListener {
		public void onItemClick(Descarga descarga);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		mOnItemClickListener = onItemClickListener;
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int i) {
		viewHolder.mCurrentDescarga = descargas.get(i);
		viewHolder.titulo.setText(descargas.get(i).getFileName());
		viewHolder.descripcion.setText(descargas.get(i).getVideos().size() + " Videos");

	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView titulo;
		TextView descripcion;
		ImageView imagen;
		Descarga mCurrentDescarga;

		ViewHolder(View itemView) {
			super(itemView);
			imagen = (ImageView) itemView.findViewById(R.id.imagenDescargas1);
			titulo = (TextView) itemView.findViewById(R.id.tituloDescargas1);
			descripcion = (TextView) itemView.findViewById(R.id.descripcionDescargas1);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (mOnItemClickListener != null && mCurrentDescarga != null) {
				mOnItemClickListener.onItemClick(mCurrentDescarga);
			}
		}
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
	}

	public void addAll(ArrayList<Descarga> lista) {
		int first = descargas.size();
		descargas.addAll(lista);
		notifyItemRangeInserted(first, lista.size());
	}

	public void removeAll() {
		int last = descargas.size();
		descargas.clear();
		notifyItemRangeRemoved(0, last);
	}

}
