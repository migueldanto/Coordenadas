package com.geotec.coordenadas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import java.util.Date;
import java.text.SimpleDateFormat;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final static int REQUEST_CODE_PERMISSION_LOCATION = 1240;
    final static String[] PERMISSIONS = new String[]{ACCESS_FINE_LOCATION,WRITE_EXTERNAL_STORAGE};

    private Button btnGetLocation;
    public TextView lblTextLocation;
    private TextView txtSalidaJson;

    private Button btnBeginEnd;
    private Button btnAddVert;
    private TextView txtConsola1;

    private WebView vistaweb0;


    private Boolean canDraw=false;
    private Boolean drawing=false;
    public RegistroBD currentRegistroBD;
    public Location currentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //requestPermissions();

        this.btnGetLocation = (Button) findViewById(R.id.btn_get_location);
        this.lblTextLocation = (TextView) findViewById(R.id.lbl_text_location);

        this.btnBeginEnd = (Button) findViewById(R.id.btn_begin_end);
        this.btnAddVert = (Button) findViewById(R.id.btn_add_vert);
        this.btnBeginEnd.setEnabled(false);
        this.btnBeginEnd.setVisibility(View.INVISIBLE);
        this.btnAddVert.setEnabled(false);
        this.btnAddVert.setVisibility(View.INVISIBLE);

        this.txtConsola1=(TextView) findViewById(R.id.txt_consola1);
        this.txtSalidaJson=(TextView)findViewById(R.id.txt_salidajson);


        this.btnGetLocation.setOnClickListener(this);
        this.btnBeginEnd.setOnClickListener(this);
        this.btnAddVert.setOnClickListener(this);
        this.vistaweb0=(WebView) findViewById(R.id.vistaweb0);
        this.setPolygonWebView0();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_get_location:
                getLocation();
                break;
            case R.id.btn_begin_end:
                beginEndpolygon();
                break;
            case R.id.btn_add_vert:
                add1Vertice();
                break;
        }
    }

    private void setPolygonWebView0(){
        //this.vistaweb0.getSettings().setAllowContentAccess(true);
        //this.vistaweb0.getSettings().setBlockNetworkImage(false);
        this.vistaweb0.getSettings().setDomStorageEnabled(true);
        this.vistaweb0.getSettings().setAppCacheEnabled(true);
        this.vistaweb0.getSettings().setLoadsImagesAutomatically(true);
        this.vistaweb0.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        this.vistaweb0.loadUrl("file:///android_asset/vpoligno_v1.html");
        this.vistaweb0.getSettings().setJavaScriptEnabled(true);

        //this.vistaweb0.loadData("<h1>hello web 1</h1>","text/html","UTF-8");

    }

    private void showLocation(Location location) {
        String textLocation = getString(R.string.lbl_text_location);
        textLocation += "\n\t\tLongitude: "   + location.getLongitude();
        textLocation += ", Latitude: "    + location.getLatitude();
        //textLocation += "\n\t\tAltitude: "    + location.getAltitude();
        textLocation += "\n\t\tAccuracy: "    + location.getAccuracy();
        textLocation += ", Speed: "       + location.getSpeed();
        //textLocation += "\n\t\tTime: "        + location.getTime();
        lblTextLocation.setText(textLocation);
        this.currentLocation=location;
        Date nd=new Date(location.getTime());
        SimpleDateFormat dtf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        printConsole("\uD83D\uDD04 "+dtf.format(nd));
    }

    private void showError(String msg) {
        btnGetLocation.setEnabled(true);
        lblTextLocation.setText(msg);
    }

    private void getLocation() {
        btnGetLocation.setEnabled(false);
        lblTextLocation.setText(getString(R.string.getting_location));
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localization localization = new Localization();

        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, (LocationListener) localization);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, (LocationListener) localization);


    }

    private void requestPermissions() {
        //ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE_PERMISSION_LOCATION);
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE_PERMISSION_LOCATION);

    }



    private void beginEndpolygon(){
        if(this.drawing){

            //se terminara el dibujo
            //DECIDIR SI SE CIERRA O SE AGREGA EL PRIMER VERTICE AL FINAL OTRA VEZ O ASI SE DEJA ??
            this.btnAddVert.setEnabled(false);
            this.btnBeginEnd.setText("abrir poligono");
            //LANZAR EL RESULTADO EN UN FORMATO RECONOCIBLE
            String salida1=this.currentRegistroBD.GeoJSON();
            this.txtSalidaJson.setText(salida1);
            this.drawing=false;
            terminar_registro();
        }else{
            this.drawing=true;
            //se comenzara el dibujo
            this.btnBeginEnd.setText("cerrar poligono");
            this.btnBeginEnd.setEnabled(false);
            this.btnAddVert.setEnabled(true);
            this.btnAddVert.setText("Agregar 2° vertice");
            this.currentRegistroBD=new RegistroBD("indefinido","indefinido");
            //agregar el vertice inicial
            double lax=this.currentLocation.getLongitude();
            double lay=this.currentLocation.getLatitude();
            Coordenada coord1=new Coordenada(lax,lay);
            this.currentRegistroBD.addVertice(coord1);
            this.vistaweb0.evaluateJavascript("fn_reset("+coord1.getX()+","+coord1.getY()+");",null);
            this.vistaweb0.evaluateJavascript("fn_addVertice("+coord1.getX()+","+coord1.getY()+");",null);
            this.printConsole("\uD83D\uDC4D Iniciando registro");
            this.printConsole("\uD83D\uDCCD agregando 1er vertice...");
            this.txtSalidaJson.setText("..");
        }
    }

    private void add1Vertice(){
        double lax=this.currentLocation.getLongitude();
        double lay=this.currentLocation.getLatitude();
        Coordenada coord2=new Coordenada(lax,lay);
        this.currentRegistroBD.addVertice(coord2);
        this.vistaweb0.evaluateJavascript("fn_addVertice("+coord2.getX()+","+coord2.getY()+");",null);
        int numbVert=this.currentRegistroBD.getVerticesSize();
        this.printConsole("\uD83D\uDCCD agregando vertice "+numbVert+" ...");
        if(this.currentRegistroBD.getVerticesSize()>2){
            if(! this.btnBeginEnd.isEnabled()){
                this.btnBeginEnd.setEnabled(true);
            }
        }

        this.btnAddVert.setText("Agregar "+(numbVert+1)+"° vertice");
        this.btnBeginEnd.setText("Cerrar poligono con "+numbVert+" vertices");
    }

    public void printConsole(String texto1){
        String texto=this.txtConsola1.getText().toString();
        //si la  consola muestra mas de 15 lineas por 60 chars.. la truncamos
        if(texto.length()>(15*60)){
            texto=texto.substring(0,(15*60));
        }
        texto=texto1+"\n"+texto;
        this.txtConsola1.setText(texto);
    }

    private void terminar_registro(){


        Intent intent=new Intent(this,GetForm.class);
        intent.putExtra("key_param1","Registro guardado");
        intent.putExtra("feature",this.currentRegistroBD.GeoJSONFeatureGeometry());
        
        startActivity(intent);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("PermissionsResult", "requestCode: " + requestCode);
        boolean noPermissions = true;
        System.out.println(grantResults.length);
        for (int g: grantResults) {
            if (g < 0) {
                noPermissions = false;
            }
        }
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (noPermissions) getLocation();
                else showError(getString(R.string.permissions_denied));
                break;
        }
    }

    public void showCanDraw(){
        if(!canDraw){
            btnBeginEnd.setVisibility(View.VISIBLE);
            btnAddVert.setVisibility(View.VISIBLE);
            if(!drawing){ //si no se ha dibujado aun
                btnBeginEnd.setEnabled(true);
            }else{ //en estado de agregar solo vertices
                btnAddVert.setEnabled(true);
            }

            canDraw=true;
        }
    }

    public void hideCanDraw(){
        if(canDraw){

            btnBeginEnd.setEnabled(false);
            //btnBeginEnd.setVisibility(View.INVISIBLE);
            btnAddVert.setEnabled(false);
            //btnAddVert.setVisibility(View.INVISIBLE);
            canDraw=false;
        }
    }

    private class Localization implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if(!canDraw){
                showCanDraw();
            }
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            System.out.println("NOSEEEE");
            switch (i) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    printConsole("⚠️ LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    printConsole("⚠️ LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    printConsole("⚠️ LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String s) {
            showError(getString(R.string.on_provider_enabled));
        }

        @Override
        public void onProviderDisabled(String s) {
            showError(getString(R.string.on_provider_disabled));
        }

    }
}