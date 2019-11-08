package com.geotec.coordenadas;

import java.util.ArrayList;
import android.text.TextUtils;



public class RegistroBD {
    private String nombre;
    private String id;
    private ArrayList<Coordenada> vertices;

    public RegistroBD(String nombre, String id) {
        this.nombre = nombre;
        this.id = id;
        this.vertices=new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addVertice(Coordenada coord){
        this.vertices.add(coord);
    }
    public int getVerticesSize(){
        return this.vertices.size();
    }

    public String GeoJSON(){
        String[] losd=new String[this.vertices.size()+1];
        for (int i = 0; i < this.vertices.size(); i++) {
            losd[i]=this.vertices.get(i).toString();
        }
        losd[this.vertices.size()]=this.vertices.get(0).toString();

        String salida=TextUtils.join(",",losd);
        String feature="{\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {\"id\": \""+this.id+"\", \"nombre\": \""+this.nombre+"\" },\n" +
                "      \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[ "+salida+" ]]}\n" +
                "    }";
        String base="{\"type\": \"FeatureCollection\", \"features\": ["+feature+"]}";

        return base;
    }

    public String GeoJSONFeature(){
        String[] losd=new String[this.vertices.size()+1];
        for (int i = 0; i < this.vertices.size(); i++) {
            losd[i]=this.vertices.get(i).toString();
        }
        losd[this.vertices.size()]=this.vertices.get(0).toString();

        String salida=TextUtils.join(",",losd);
        String feature="{\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {\"id\": \""+this.id+"\", \"nombre\": \""+this.nombre+"\" },\n" +
                "      \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[ "+salida+" ]]}\n" +
                "    }";

        return feature;
    }

    public String   GeoJSONFeatureGeometry(){
        String[] losd=new String[this.vertices.size()+1];
        for (int i = 0; i < this.vertices.size(); i++) {
            losd[i]=this.vertices.get(i).toString();
        }
        losd[this.vertices.size()]=this.vertices.get(0).toString();

        String salida=TextUtils.join(",",losd);
        String geometry="{\"type\": \"Polygon\", \"coordinates\": [[ "+salida+" ]]}";

        return geometry;
    }
}
