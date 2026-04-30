package com.example.monitoringappslb.model;

public class Ppi {
    private String studentName;
    private String semester;
    private String mainTarget;
    private int progress;
    private String status;
    private String targetAkademik;
    private String targetPerilaku;
    private String targetSosial;
    private String targetMotorik;

    public Ppi(String studentName, String semester, String mainTarget, int progress, String status,
               String targetAkademik, String targetPerilaku, String targetSosial, String targetMotorik) {
        this.studentName = studentName;
        this.semester = semester;
        this.mainTarget = mainTarget;
        this.progress = progress;
        this.status = status;
        this.targetAkademik = targetAkademik;
        this.targetPerilaku = targetPerilaku;
        this.targetSosial = targetSosial;
        this.targetMotorik = targetMotorik;
    }

    public String getStudentName() { return studentName; }
    public String getSemester() { return semester; }
    public String getMainTarget() { return mainTarget; }
    public int getProgress() { return progress; }
    public String getStatus() { return status; }
    public String getTargetAkademik() { return targetAkademik; }
    public String getTargetPerilaku() { return targetPerilaku; }
    public String getTargetSosial() { return targetSosial; }
    public String getTargetMotorik() { return targetMotorik; }
}