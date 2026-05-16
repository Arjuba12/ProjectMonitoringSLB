package com.example.monitoringappslb.model;

import java.io.Serializable;

public class Ppi implements Serializable {
    private int id;
    private int siswaId;
    private String studentName;
    private String semester;
    private String mainTarget;
    private int progress;
    private String status;
    private String potensi;
    private String hambatan;
    private String targetAkademik;
    private String targetPerilaku;
    private String targetSosial;
    private String targetMotorik;

    public Ppi(int id, String studentName, String semester, String mainTarget, int progress, String status,
               String potensi, String hambatan,
               String targetAkademik, String targetPerilaku, String targetSosial, String targetMotorik) {
        this.id = id;
        this.studentName = studentName;
        this.semester = semester;
        this.mainTarget = mainTarget;
        this.progress = progress;
        this.status = status;
        this.potensi = potensi;
        this.hambatan = hambatan;
        this.targetAkademik = targetAkademik;
        this.targetPerilaku = targetPerilaku;
        this.targetSosial = targetSosial;
        this.targetMotorik = targetMotorik;
    }

    public Ppi(int id, String studentName, String targetAkademik, String targetPerilaku, String targetSosial, String targetMotorik) {
        this.id = id;
        this.studentName = studentName;
        this.targetAkademik = targetAkademik;
        this.targetPerilaku = targetPerilaku;
        this.targetSosial = targetSosial;
        this.targetMotorik = targetMotorik;
        this.semester = "";
        this.mainTarget = "";
        this.progress = 0;
        this.status = "";
        this.potensi = "";
        this.hambatan = "";
    }

    public int getId() { return id; }
    public int getSiswaId() { return siswaId; }
    public void setSiswaId(int siswaId) { this.siswaId = siswaId; }
    public String getStudentName() { return studentName; }
    public String getSemester() { return semester; }
    public String getMainTarget() { return mainTarget; }
    public int getProgress() { return progress; }
    public String getStatus() { return status; }
    public String getPotensi() { return potensi; }
    public String getHambatan() { return hambatan; }
    public String getTargetAkademik() { return targetAkademik; }
    public String getTargetPerilaku() { return targetPerilaku; }
    public String getTargetSosial() { return targetSosial; }
    public String getTargetMotorik() { return targetMotorik; }
}
