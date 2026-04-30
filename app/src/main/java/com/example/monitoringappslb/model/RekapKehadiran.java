package com.example.monitoringappslb.model;

public class RekapKehadiran {
    private String namaSiswa;
    private int hadir;
    private int sakit;
    private int izin;
    private int alpha;

    public RekapKehadiran(String namaSiswa, int hadir, int sakit, int izin, int alpha) {
        this.namaSiswa = namaSiswa;
        this.hadir = hadir;
        this.sakit = sakit;
        this.izin = izin;
        this.alpha = alpha;
    }

    public String getNamaSiswa() { return namaSiswa; }
    public int getHadir() { return hadir; }
    public int getSakit() { return sakit; }
    public int getIzin() { return izin; }
    public int getAlpha() { return alpha; }
}