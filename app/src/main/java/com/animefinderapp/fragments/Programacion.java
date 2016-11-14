package com.animefinderapp.fragments;

import java.util.ArrayList;

import com.animefinderapp.R;
import com.animefinderapp.adaptadores.ProgramacionAdapter;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.Capitulo;
import com.animefinderapp.servicios.AnimeService;
import com.animefinderapp.utilidad.ServidorUtil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Programacion extends Fragment {

    private String server;
    private String tipoLista;
    private SwipeRefreshLayout swipeRefreshLayoutProgramacion;
    private ProgramacionAdapter programacionAdapter;
    private RecyclerView recyclerViewProgramacion;
    private SharedPreferences sharedPref;
    private Context context;

    public Programacion() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            context= getActivity();
            PreferenceManager.setDefaultValues(context, R.xml.settings, false);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
            boolean servicio = sharedPref.getBoolean("notificarcheckbox", false);
            if (servicio && !ServidorUtil.isMyServiceRunning(AnimeService.class, context)) {
                context.startService(new Intent(context, AnimeService.class));
                Snackbar.make(view, "Servicio Iniciado", Snackbar.LENGTH_SHORT).show();
            }
            tipoLista = sharedPref.getString("pref_list_view", "lista").toLowerCase();
            swipeRefreshLayoutProgramacion = (SwipeRefreshLayout) view.findViewById(R.id.swipetorefresh);
            programacionAdapter = ServidorUtil.getProgramacionAdapter(tipoLista, server, context);
            recyclerViewProgramacion = (RecyclerView) view.findViewById(R.id.lista);
            recyclerViewProgramacion.setHasFixedSize(true);
            recyclerViewProgramacion.setLayoutManager(ServidorUtil.getlayout(tipoLista, context));
            recyclerViewProgramacion.setAdapter(programacionAdapter);
            swipeRefreshLayoutProgramacion.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    programacionAdapter.removeAll();
                    new BuscarProgramacion(server).execute();
                }
            });
            new BuscarProgramacion(server).execute();

        } catch (Exception e) {
            ServidorUtil.showMensageError(e, getView());
        }
    }


    private class BuscarProgramacion extends AsyncTask<String, Void, String> {
        private ArrayList<Capitulo> lista;
        private String server;

        public BuscarProgramacion(String server) {
            this.lista = new ArrayList<Capitulo>();
            this.server = server;
        }

        @Override
        protected void onPreExecute() {
            ServidorUtil.refresh(swipeRefreshLayoutProgramacion, true);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                lista = ServidorUtil.buscarProgramacion(server, context);
            } catch (Exception e) {
                ServidorUtil.showMensageError(e, getView());
            }

            return "ok";
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            ServidorUtil.refresh(swipeRefreshLayoutProgramacion, false);
            if (s.equals("ok")) {
                programacionAdapter.addAll(lista);
            }
        }

        @Override
        protected void onCancelled() {
            Snackbar.make(recyclerViewProgramacion, "Busqueda cancelada!", Snackbar.LENGTH_SHORT).show();
        }
    }

}
