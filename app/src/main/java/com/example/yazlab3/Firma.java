package com.example.yazlab3;

class Firma {
    private String firmaAdı;
    private double enlem;
    private double boylam;
    private String kampanyaBaslik;
    private String kampanyaIcerik;
    private String katagori;
    private int kampanyaSuresi;

    @SuppressWarnings("unused")
    public Firma() {
    }

    Firma(String firmaAdı, double enlem, double boylam, String kampanyaBaslik, String kampanyaIcerik, String katagori, int kampanyaSuresi) {
        this.firmaAdı = firmaAdı;
        this.enlem = enlem;
        this.boylam = boylam;
        this.kampanyaBaslik = kampanyaBaslik;
        this.kampanyaIcerik = kampanyaIcerik;
        this.katagori = katagori;
        this.kampanyaSuresi = kampanyaSuresi;
    }

    String getFirmaAdı() { return firmaAdı; }

    double getEnlem() {
        return enlem;
    }

    double getBoylam() {
        return boylam;
    }

    String getKampanyaBaslik() {
        return kampanyaBaslik;
    }

    String getKampanyaIcerik() {
        return kampanyaIcerik;
    }

    String getKatagori() {
        return katagori;
    }

    int getKampanyaSuresi() {
        return kampanyaSuresi;
    }
}
