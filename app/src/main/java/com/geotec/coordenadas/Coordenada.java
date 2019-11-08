package com.geotec.coordenadas;

public class Coordenada {
    private double x;
    private double y;

    public Coordenada(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "["+x+","+y+"]";
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
