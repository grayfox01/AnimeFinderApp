package com.animefinderapp.actividades;

import com.animefinderapp.R;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.fragments.Favoritos;
import com.animefinderapp.fragments.Generos;
import com.animefinderapp.fragments.Historial;
import com.animefinderapp.fragments.Letras;
import com.animefinderapp.fragments.Programacion;
import com.animefinderapp.fragments.Vistos;
import com.animefinderapp.servicios.AnimeService;
import com.animefinderapp.utilidad.CropCircleTransformation;
import com.animefinderapp.utilidad.ServidorUtil;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    private String server;
    private TextView name;
    private TextView email;
    private ImageView imagen;
    private ImageView servidor;
    private SearchView searchView;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Intent i;
    private Toolbar toolbar;
    private SharedPreferences sharedPref;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.side_nav_bar);

        try {
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close) {

                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }
            };
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    int id = item.getItemId();
                    Fragment fragment = null;
                    switch (id) {
                        case R.id.nav_programacion:
                            if (ServidorUtil.verificaConexion(MainActivity.this)) {
                                fragment = new Programacion();
                                getSupportFragmentManager().beginTransaction().replace(R.id.contenido, fragment).commit();
                                item.setChecked(true);
                                getSupportActionBar().setTitle(item.getTitle());
                            } else {
                                Snackbar.make(drawer, "Sin conexion", Snackbar.LENGTH_SHORT).show();
                            }
                            break;
                        case R.id.nav_historial:
                            fragment = new Historial();
                            getSupportFragmentManager().beginTransaction().replace(R.id.contenido, fragment).commit();
                            item.setChecked(true);
                            getSupportActionBar().setTitle(item.getTitle());

                            break;
                        case R.id.nav_letras:
                            if (ServidorUtil.verificaConexion(MainActivity.this)) {
                                fragment = new Letras();
                                getSupportFragmentManager().beginTransaction().replace(R.id.contenido, fragment).commit();
                                item.setChecked(true);
                                getSupportActionBar().setTitle(item.getTitle());
                            } else {
                                Snackbar.make(drawer, "Sin conexion", Snackbar.LENGTH_SHORT).show();
                            }
                            break;
                        case R.id.nav_generos:
                            if (ServidorUtil.verificaConexion(MainActivity.this)) {
                                fragment = new Generos();
                                getSupportFragmentManager().beginTransaction().replace(R.id.contenido, fragment).commit();
                                item.setChecked(true);
                                getSupportActionBar().setTitle(item.getTitle());
                            } else {
                                Snackbar.make(drawer, "Sin conexion", Snackbar.LENGTH_SHORT).show();
                            }
                            break;
                        case R.id.nav_favoritos:
                            fragment = new Favoritos();
                            getSupportFragmentManager().beginTransaction().replace(R.id.contenido, fragment).commit();
                            item.setChecked(true);
                            getSupportActionBar().setTitle(item.getTitle());
                            break;
                        case R.id.nav_vistos:
                            fragment = new Vistos();
                            getSupportFragmentManager().beginTransaction().replace(R.id.contenido, fragment).commit();
                            item.setChecked(true);
                            getSupportActionBar().setTitle(item.getTitle());
                            break;
                        case R.id.nav_admin_descargas:
                            Intent mView = new Intent();
                            mView.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                            startActivity(mView);
                            break;
                        case R.id.nav_descargas:
                            i = new Intent(MainActivity.this, DownloadsActivity.class);
                            startActivity(i);
                            break;
                        case R.id.nav_configuracion:
                            i = new Intent(MainActivity.this, ConfiguracionActivity.class);
                            startActivity(i);
                            break;
                        case R.id.nav_salir:
                            close();
                            break;
                        case R.id.nav_cerrar_sesion:
                            signOut();
                            break;
                    }
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
            });
            setFirstItemNavigationView();
            View hView = navigationView.getHeaderView(0);
            imagen = (ImageView) hView.findViewById(R.id.imagen);
            servidor = (ImageView) hView.findViewById(R.id.server);
            name = (TextView) hView.findViewById(R.id.nombre);
            email = (TextView) hView.findViewById(R.id.email);
            PreferenceManager.setDefaultValues(this, R.xml.settings, false);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
            boolean servicio = sharedPref.getBoolean("notificarcheckbox", false);
            if (servicio && !ServidorUtil.isMyServiceRunning(AnimeService.class, this)) {
                startService(new Intent(getApplicationContext(), AnimeService.class));
                Toast.makeText(getBaseContext(), "Servicio Iniciado", Toast.LENGTH_SHORT).show();
            }


            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
            mAuth = FirebaseAuth.getInstance();
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // User is signed in
                        String name1 = user.getDisplayName();
                        String email1 = user.getEmail();
                        String photoUrl = user.getPhotoUrl().toString();
                        Log.e("nombre", name1);
                        Log.e("email", email1);
                        Log.e("photoUrl", photoUrl);
                        email.setText(email1);
                        name.setText(name1);
                        Glide.with(MainActivity.this) // safer!
                                .load(photoUrl).asBitmap()
                                .transform(new CropCircleTransformation(MainActivity.this)).into(imagen);
                        fillServer();
                    } else {
                        Intent i = new Intent(MainActivity.this, GoogleSignInActivity.class);
                        MainActivity.this.startActivity(i);
                        finish();
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setQueryHint("Buscar...");
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() < 4) {
                    Toast.makeText(MainActivity.this, "El titulo tiene que tener almenos 4 caracteres",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Buscando: " + query, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, BusquedaActivity.class);
                    i.putExtra("cadenaBusqueda", query);
                    MainActivity.this.startActivity(i);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String arg0) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
                break;
            case R.id.action_settings:
                i = new Intent(this, ConfiguracionActivity.class);
                startActivity(i);
                break;
        }

        return false;
    }

    private void setFirstItemNavigationView() {
        navigationView.setCheckedItem(R.id.nav_programacion);
        navigationView.getMenu().performIdentifierAction(R.id.nav_programacion, 0);
    }

    private void close() {
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle("Importante");
        dialogo1.setMessage("¿Desea salir?");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogo1, int id) {
                finish();
            }
        });
        dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialogo1.show();
    }


    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public void fillServer() {
        try {
            switch (server.toLowerCase()) {
                case "animeflv":
                    Glide.with(this).load(R.drawable.animeflv).into(servidor);
                    break;
                case "animeid":
                    Glide.with(this).load(R.drawable.animeid).into(servidor);
                    break;
                case "jkanime":
                    Glide.with(this).load(R.drawable.jkanime).into(servidor);
                    break;
                case "reyanime":
                    Glide.with(this).load(R.drawable.reyanime).into(servidor);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Toast.makeText(MainActivity.this, "Sesion Cerrada", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}