package com.geotec.coordenadas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetForm extends AppCompatActivity implements View.OnClickListener{

    final static int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION=1;

    private Button btnSave1;
    private EditText txtId1;
    private EditText txtName1;
    private String feature;
    private WebView vistaweb1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_form);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra("key_param1");
        this.feature=intent.getStringExtra("feature");
        TextView textView = findViewById(R.id.txt_msg1);
        textView.setText(message);

        this.btnSave1=(Button) findViewById(R.id.btn_save1);
        this.txtId1=(EditText) findViewById(R.id.txt_id1);
        this.txtName1=(EditText) findViewById(R.id.txt_name1);
        this.vistaweb1=(WebView) findViewById(R.id.vistaweb1);
        //MainActivity actPa=(MainActivity)this.getParent();
        //this.registro=actPa.currentRegistroBD;
        this.btnSave1.setOnClickListener(this);

        //ponienodo la vista web
        this.setPolygonWebView();
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_save1:
                guardar();
                break;
        }
    }


    public void guardar(){
        String identificador=this.txtId1.getText().toString();
        String name=this.txtName1.getText().toString();
        if(identificador.length()>0 && name.length()>0){
            String feature="{\n" +
                    "      \"type\": \"Feature\",\n" +
                    "      \"properties\": {\"id\": \""+identificador+"\", \"nombre\": \""+name+"\" },\n" +
                    "      \"geometry\": "+this.feature +
                    "    }";
            String base="{\"type\": \"FeatureCollection\", \"features\": ["+feature+"]}";
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(GetForm.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

            this.writeToFile(base,GetForm.this,identificador);

        }else{
            AlertDialog dialog=new AlertDialog.Builder(GetForm.this).create();
            dialog.setTitle("Atención");
            dialog.setMessage("Proporciona los datos requeridos");
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }


    private void writeToFile(String data, Context context,String nameFile) {
        // Check whether this app has write external storage permission or not.


        File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dir = new File (sdCard.getAbsolutePath() + "/geojson_poligonos/");
        File file = new File(dir, "Captura_"+nameFile+".geojson");

        if (!dir.mkdirs()) {
            Log.e("Error1", "Directory not created");
        }
        System.out.println(file.getPath());
        System.out.println(isExternalStorageWritable());

        try {
            //file.createNewFile();
            FileOutputStream fileOutput = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutput);
            outputStreamWriter.write(data);
            outputStreamWriter.flush();
            fileOutput.getFD().sync();
            outputStreamWriter.close();
            Toast tt=Toast.makeText(getApplicationContext(),"guardado: "+file.getPath(),Toast.LENGTH_LONG);
            tt.setDuration(Toast.LENGTH_LONG);
            tt.show();
            //this.txtName1.setEnabled(false);
            //this.txtId1.setEnabled(false);
        }catch (IOException ex){
            ex.printStackTrace();
            Log.e("Error2", "Archivo no creado not created");
            Toast tt=Toast.makeText(getApplicationContext(),"Archivo no generado.",Toast.LENGTH_LONG);
            tt.show();
        }


    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void setPolygonWebView(){
        this.vistaweb1.getSettings().setAllowContentAccess(true);

        this.vistaweb1.loadUrl("file:///android_asset/vpoligno_v1.html");
        this.vistaweb1.getSettings().setJavaScriptEnabled(true);
        try {
            JSONObject geom=new JSONObject(this.feature);
            final JSONArray coords=geom.getJSONArray("coordinates");
            System.out.println(coords.toString());
            //this.vistaweb1.loadData("<h1>hello web 1</h1>","text/html","UTF-8");

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    vistaweb1.evaluateJavascript("fn_load_coords("+coords.toString()+");",null);
                }
            }, 1000);


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
