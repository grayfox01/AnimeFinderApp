package com.animefinderapp.adaptadores;

import java.util.List;

import com.animefinderapp.R;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LetrasAdapter extends RecyclerView.Adapter<LetrasAdapter.ViewHolder> {

	private List<String> lista;
	private int resourse;
	private static OnItemClickListener mOnItemClickListener;

	public LetrasAdapter(int resource, List<String> list) {
		this.resourse = resource;
		this.lista = list;
	}

	public interface OnItemClickListener {
		public void onItemClick(String titulo);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		mOnItemClickListener = onItemClickListener;
	}

	@Override
	public int getItemCount() {
		return lista.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(resourse, viewGroup, false);
		ViewHolder viewHolder = new ViewHolder(view);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, final int i) {
		String titulo = lista.get(i);
		viewHolder.mCurrentTitulo = titulo;
		viewHolder.titulo.setText(titulo);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView titulo;
		String mCurrentTitulo;

		ViewHolder(View itemView) {
			super(itemView);
			titulo = (TextView) itemView.findViewById(R.id.tituloLetraGenero);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (mOnItemClickListener != null && mCurrentTitulo != null) {
				mOnItemClickListener.onItemClick(mCurrentTitulo);
			}
		}
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
	}

	public void addAll(List<String> list) {
		int first = lista.size();
		this.lista.addAll(list);
		notifyItemRangeInserted(first, list.size());
	}

	public void removeAll() {
		int last = lista.size();
		lista.clear();
		notifyItemRangeRemoved(0, last);
	}
}
