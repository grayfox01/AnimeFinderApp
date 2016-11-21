package com.animefinderapp.actividades;

import com.animefinderapp.R;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.baseDatos.AnimeDbHelper;
import com.animefinderapp.fragments.FavoritosFragment;
import com.animefinderapp.fragments.GenerosNombreFragment;
import com.animefinderapp.fragments.HistorialFragment;
import com.animefinderapp.fragments.LetrasNombreFragment;
import com.animefinderapp.fragments.ProgramacionFragment;
import com.animefinderapp.fragments.VistosFragment;
import com.animefinderapp.servicios.AnimeService;
import com.animefinderapp.utilidad.CropCircleTransformation;
import com.animefinderapp.utilidad.ServidorUtil;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URLConnection;

import static android.R.attr.fragment;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    private String server;
    private TextView name;
    private TextView email;
    private ImageView imagen;
    private ImageView servidor;
    private DrawerLayout drawer;
    private Intent i;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private Fragment fragment = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.side_nav_bar);
        try {

            AnimeDbHelper helper = new AnimeDbHelper(this);
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            mAuth = FirebaseAuth.getInstance();


            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open,
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
                    switch (id) {
                        case R.id.nav_programacion:
                            fragment = new ProgramacionFragment();
                            break;
                        case R.id.nav_historial:
                            fragment = new HistorialFragment();
                            break;
                        case R.id.nav_letras:
                            fragment = new LetrasNombreFragment();
                            break;
                        case R.id.nav_generos:
                            fragment = new GenerosNombreFragment();
                            break;
                        case R.id.nav_favoritos:
                            fragment = new FavoritosFragment();
                            break;
                        case R.id.nav_vistos:
                            fragment = new VistosFragment();
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
                    }
                    if (fragment != null) {
                        toolbar.setTitle(item.getTitle());
                        getSupportFragmentManager().beginTransaction().replace(R.id.contenido, fragment).commit();
                    }

                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
            });
            setFirstItemNavigationView();
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        setupDrawerHeader(navigationView, true);
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
                        setupDrawerHeader(navigationView, false);
                    }
                }
            };
            PreferenceManager.setDefaultValues(this, R.xml.settings, false);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            server = sharedPref.getString("pref_servidor", "AnimeFlv").toLowerCase();
            boolean servicio = sharedPref.getBoolean("notificarcheckbox", false);
            if (servicio && !ServidorUtil.isMyServiceRunning(AnimeService.class, this)) {
                startService(new Intent(getApplicationContext(), AnimeService.class));
                Toast.makeText(getBaseContext(), "Servicio Iniciado", Toast.LENGTH_SHORT).show();
            }
            selectCodec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupDrawerHeader(NavigationView navigationView, Boolean isLogued) {
        RelativeLayout layoutout;
        LinearLayout layoutin;
        if (isLogued) {
            View hView = navigationView.getHeaderView(0);
            layoutin = (LinearLayout) hView.findViewById(R.id.loguedLayout);
            layoutout = (RelativeLayout) hView.findViewById(R.id.nologuedLayout);
            layoutin.setVisibility(View.VISIBLE);
            layoutout.setVisibility(View.GONE);
            imagen = (ImageView) hView.findViewById(R.id.circle_image);
            servidor = (ImageView) hView.findViewById(R.id.server_image);
            name = (TextView) hView.findViewById(R.id.username);
            email = (TextView) hView.findViewById(R.id.email);
            email.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {
                    PopupMenu popup = new PopupMenu(MainActivity.this, v);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.salir:
                                    signOut();
                                    return true;
                            }
                            return false;
                        }
                    });
                    popup.inflate(R.menu.user);
                    popup.show();
                }
            });
        } else {
            View hView = navigationView.getHeaderView(0);
            layoutin = (LinearLayout) hView.findViewById(R.id.loguedLayout);
            layoutout = (RelativeLayout) hView.findViewById(R.id.nologuedLayout);
            layoutin.setVisibility(View.GONE);
            layoutout.setVisibility(View.VISIBLE);
            SignInButton signInButton = (SignInButton) hView.findViewById(R.id.sign_in_button);
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signIn();
                }
            });
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
          drawer.closeDrawer(GravityCompat.START);
        } else {
        }
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            super.onBackPressed();
        } else {
            close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
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
        drawer.closeDrawer(GravityCompat.START);
        AnimeDataSource.uploadDB(MainActivity.this, mAuth.getCurrentUser());
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Snackbar.make(MainActivity.this.getCurrentFocus(), "Sesion Cerrada", Snackbar.LENGTH_LONG).show();
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Snackbar.make(MainActivity.this.getCurrentFocus(), "Authentication failed.", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(MainActivity.this.getCurrentFocus(), "Sesion iniciada", Snackbar.LENGTH_LONG).show();
                            AnimeDataSource.downloadDB(MainActivity.this, mAuth.getCurrentUser());
                        }
                        // [START_EXCLUDE]
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]

    // [START signin]
    private void signIn() {
        drawer.closeDrawer(GravityCompat.START);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    public static void selectCodec() {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                Log.e("Type:", types[j] );
            }
        }
    }


    public static String getMimeType(String fileUrl) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
}