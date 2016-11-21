package com.animefinderapp.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.animefinderapp.R;
import com.animefinderapp.actividades.DownloadsActivity;
import com.animefinderapp.adaptadores.VideosAdapter;
import com.animefinderapp.entidades.Descarga;
import com.animefinderapp.entidades.Video;
import com.animefinderapp.utilidad.ServidorUtil;
import java.io.File;
import java.util.ArrayList;

public class DescargaListFragment extends Fragment {

    private VideosAdapter adapter;
    private Descarga descarga;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private File carpeta;
    private RecyclerView videos;

    public DescargaListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            context = getActivity();
            descarga = (Descarga) getArguments().getSerializable("descarga");
            ((DownloadsActivity) (context)).getSupportActionBar().setTitle(descarga.getFileName());
            videos = (RecyclerView) view.findViewById(R.id.lista);
            carpeta = new File(descarga.getFilepath());
            adapter = new VideosAdapter(context, new ArrayList<Video>(),
                    R.layout.lista_descargas_videos_row);
            swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipetorefresh);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new BuscarVideos().execute();
                }
            });
            linearLayoutManager = new LinearLayoutManager(context);
            videos.setHasFixedSize(true);
            videos.setLayoutManager(linearLayoutManager);
            videos.setAdapter(adapter);
            new BuscarVideos().execute();
        } catch (Exception e) {
            e.printStackTrace();
            ServidorUtil.showMensageError(e, getView());
        }
        super.onViewCreated(view, savedInstanceState);
    }

    private class BuscarVideos extends AsyncTask<String, Void, String> {
        private ArrayList<Video> lista;

        public BuscarVideos() {
            this.lista = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            ServidorUtil.refresh(swipeRefreshLayout, true);
            adapter.removeAll();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                lista = ServidorUtil.getVideosDescarga(context, carpeta);
            } catch (Exception e) {
                ServidorUtil.showMensageError(e, getView());
            }

            return "ok";
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            ServidorUtil.refresh(swipeRefreshLayout, false);
            if (s.equals("ok")) {
                adapter.addAll(lista);
            }
        }

        @Override
        protected void onCancelled() {
            Snackbar.make(videos, "Busqueda cancelada!", Snackbar.LENGTH_SHORT).show();
        }
    }
}