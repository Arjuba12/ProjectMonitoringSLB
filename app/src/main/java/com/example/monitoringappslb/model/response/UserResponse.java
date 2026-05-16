// File: model/response/UserResponse.java
package com.example.monitoringappslb.model.response;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("id") private int id;
    @SerializedName("nama") private String nama;
    @SerializedName("email") private String email;
    @SerializedName("role") private String role;
    @SerializedName("foto") private String foto;
    @SerializedName("no_hp") private String noHp;
    @SerializedName("guru_id") private Integer guruId;
    @SerializedName("nip") private String nip;
    @SerializedName("spesialisasi") private String spesialisasi;
    @SerializedName("siswa_id") private Integer siswaId;
    @SerializedName("nama_siswa") private String namaSiswa;
    @SerializedName("kelas_id") private Integer kelasId;
    @SerializedName("nama_kelas") private String namaKelas;
    @SerializedName("hubungan") private String hubungan;

    public int getId() { return id; }
    public String getNama() { return nama; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getFoto() { return foto; }
    public String getNoHp() { return noHp; }
    public Integer getGuruId() { return guruId; }
    public String getNip() { return nip; }
    public String getSpesialisasi() { return spesialisasi; }
    public Integer getSiswaId() { return siswaId; }
    public String getNamaSiswa() { return namaSiswa; }
    public Integer getKelasId() { return kelasId; }
    public String getNamaKelas() { return namaKelas; }
    public String getHubungan() { return hubungan; }
}
