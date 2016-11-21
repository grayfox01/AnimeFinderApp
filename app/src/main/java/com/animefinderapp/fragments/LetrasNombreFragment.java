package com.animefinderapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.animefinderapp.R;
import com.animefinderapp.adaptadores.AnimeAdapter;
import com.animefinderapp.adaptadores.LetrasAdapter;
import com.animefinderapp.servicios.AnimeService;
import com.animefinderapp.utilidad.ServidorUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class LetrasNombreFragment extends Fragment {

    private String server;
    private String tipoLista;
    private SwipeRefreshLayout swipeRefreshLayoutLetras;
    private LetrasAdapter letrasAdapter;
    private AnimeAdapter animeLetraAdapter;
    private RecyclerView recyclerViewLetras;
    private SharedPreferences sharedPref;
    private String letraSeleccionada;
    private int pagina;
    private boolean end;
    private boolean refreshing = false;
    private Context context;
    private String titulo;

    public LetrasNombreFragment() {
        // Required empty public constructor
    }

   
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
           tipoLista = sharedPref.getString("pref_list_view", "lista").toLowerCase();
            swipeRefreshLayoutLetras = (SwipeRefreshLayout) view.findViewById(R.id.swipetorefresh);
            letrasAdapter = new LetrasAdapter(R.layout.lista_letras_generos_row_list, new ArrayList<String>());
            recyclerViewLetras = (RecyclerView) view.findViewById(R.id.lista);
            recyclerViewLetras.setHasFixedSize(true);
            recyclerViewLetras.setLayoutManager(ServidorUtil.getlayout(tipoLista, context));
            recyclerViewLetras.setAdapter(letrasAdapter);
            swipeRefreshLayoutLetras.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    letrasAdapter.removeAll();
                    letrasAdapter.addAll(Arrays.asList(getResources().getStringArray(R.array.items_letras)));
                    swipeRefreshLayoutLetras.setRefreshing(false);
                }
            });
            letrasAdapter.setOnItemClickListener(new LetrasAdapter.OnItemClickListener() {
                public void onItemClick(String cadena) {
                    if (ServidorUtil.verificaConexion(context)) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("letra", cadena);
                        LetrasAnimeFragment fragment = new LetrasAnimeFragment();
                        fragment.setArguments(bundle);
                        ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction()
                                .replace(R.id.contenido, fragment,"letra")
                                .addToBackStack(null)
                                .commit();

                    } else {
                        Snackbar.make(getView(), "No hay conexion a internet", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
            letrasAdapter.addAll(Arrays.asList(getResources().getStringArray(R.array.items_letras)));

        } catch (Exception e) {
            ServidorUtil.showMensageError(e, getView());
        }
    }

}
