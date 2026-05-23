package com.example.monitoringappslb.network;

import com.example.monitoringappslb.model.response.ApiModels.AbsensiListResponse;
import com.example.monitoringappslb.model.response.ApiModels.AbsensiSiswaRekapResponse;
import com.example.monitoringappslb.model.response.ApiModels.AdminUserListResponse;
import com.example.monitoringappslb.model.response.ApiModels.AspekListResponse;
import com.example.monitoringappslb.model.response.ApiModels.DashboardGuruResponse;
import com.example.monitoringappslb.model.response.ApiModels.DashboardKepsekResponse;
import com.example.monitoringappslb.model.response.ApiModels.DashboardWaliResponse;
import com.example.monitoringappslb.model.response.ApiModels.KelasListResponse;
import com.example.monitoringappslb.model.response.ApiModels.KegiatanBannerUploadResponse;
import com.example.monitoringappslb.model.response.ApiModels.KegiatanListResponse;
import com.example.monitoringappslb.model.response.ApiModels.LaporanKelasResponse;
import com.example.monitoringappslb.model.response.ApiModels.LaporanListResponse;
import com.example.monitoringappslb.model.response.ApiModels.LoginResponse;
import com.example.monitoringappslb.model.response.ApiModels.MessageResponse;
import com.example.monitoringappslb.model.response.ApiModels.PerkembanganListResponse;
import com.example.monitoringappslb.model.response.ApiModels.PerkembanganRingkasanResponse;
import com.example.monitoringappslb.model.response.ApiModels.PesanListResponse;
import com.example.monitoringappslb.model.response.ApiModels.PpiDetailResponse;
import com.example.monitoringappslb.model.response.ApiModels.PpiListResponse;
import com.example.monitoringappslb.model.response.ApiModels.SiswaDetailResponse;
import com.example.monitoringappslb.model.response.ApiModels.SiswaListResponse;
import com.example.monitoringappslb.model.response.ApiModels.SiswaRekapResponse;
import com.example.monitoringappslb.model.response.ApiModels.TingkatListResponse;
import com.example.monitoringappslb.model.response.MeResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.DELETE;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ─── AUTH ─────────────────────────────────────────────────

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body Map<String, String> body);

    // GET /api/auth/me → { success, data: { id, nama, email, role, ... } }
    @GET("api/auth/me")
    Call<MeResponse> getMe();

    @PUT("api/auth/profile")
    Call<MeResponse> updateProfile(@Body Map<String, Object> body);

    @PUT("api/auth/change-password")
    Call<MessageResponse> changePassword(@Body Map<String, String> body);

    @GET("api/users")
    Call<AdminUserListResponse> getUsers(
            @Query("role") String role,
            @Query("is_aktif") Integer isAktif,
            @Query("search") String search
    );

    @POST("api/users")
    Call<MessageResponse> createUser(@Body Map<String, Object> body);

    @PUT("api/users/{id}")
    Call<MessageResponse> updateUser(@Path("id") int id, @Body Map<String, Object> body);

    @PUT("api/users/{id}/reset-password")
    Call<MessageResponse> resetUserPassword(@Path("id") int id, @Body Map<String, String> body);

    // ─── DASHBOARD ────────────────────────────────────────────

    @GET("api/dashboard/guru")
    Call<DashboardGuruResponse> getDashboardGuru();

    @GET("api/dashboard/kepsek")
    Call<DashboardKepsekResponse> getDashboardKepsek();

    @GET("api/dashboard/wali")
    Call<DashboardWaliResponse> getDashboardWali();

    // ─── SISWA ────────────────────────────────────────────────

    // GET /api/siswa → { success, data: [ {...}, {...} ] }
    @GET("api/siswa")
    Call<SiswaListResponse> getSiswa(
            @Query("kelas_id") Integer kelasId,
            @Query("is_aktif") Integer isAktif,
            @Query("search") String search
    );

    // GET /api/siswa/:id → { success, data: { ... } }
    @GET("api/siswa/{id}")
    Call<SiswaDetailResponse> getSiswaById(@Path("id") int id);

    // GET /api/siswa/perlu-perhatian → { success, data: [ {...} ] }
    @GET("api/siswa/perlu-perhatian")
    Call<SiswaListResponse> getSiswaPerluPerhatian();

    @GET("api/siswa/rekap")
    Call<SiswaRekapResponse> getSiswaRekap(
            @Query("kelas_id") Integer kelasId,
            @Query("bulan") Integer bulan,
            @Query("tahun") Integer tahun
    );

    // ─── KELAS ────────────────────────────────────────────────

    // GET /api/kelas → { success, data: [ {...} ] }
    @GET("api/kelas")
    Call<KelasListResponse> getKelas(@Query("tahun_ajaran") String tahunAjaran);

    @POST("api/kelas")
    Call<MessageResponse> createKelas(@Body Map<String, Object> body);

    @PUT("api/kelas/{id}")
    Call<MessageResponse> updateKelas(@Path("id") int id, @Body Map<String, Object> body);

    // GET /api/kelas/guru/saya → { success, data: [ {...} ] }
    @GET("api/kelas/guru/saya")
    Call<KelasListResponse> getKelasSaya();

    // GET /api/kelas/:id → { success, data: { siswa:[...], guru:[...] } }
    @GET("api/kelas/{id}")
    Call<KelasListResponse> getKelasById(@Path("id") int id);

    // ─── PERKEMBANGAN ─────────────────────────────────────────

    // GET /api/perkembangan/siswa/:id → { success, data: [ {...} ] }
    @GET("api/perkembangan/siswa/{siswaId}")
    Call<PerkembanganListResponse> getPerkembanganSiswa(
            @Path("siswaId") int siswaId,
            @Query("aspek_id") Integer aspekId
    );

    // GET /api/perkembangan/kelas/:id/rekap → { success, data: [ {...} ] }
    @GET("api/perkembangan/siswa/{siswaId}/ringkasan")
    Call<PerkembanganRingkasanResponse> getRingkasanPerkembanganSiswa(
            @Path("siswaId") int siswaId,
            @Query("bulan_count") int bulanCount
    );

    @GET("api/perkembangan/kelas/{kelasId}/rekap")
    Call<PerkembanganListResponse> getRekapKelas(@Path("kelasId") int kelasId);

    // POST /api/perkembangan → { success, message }
    @POST("api/perkembangan")
    Call<MessageResponse> inputPerkembangan(@Body Map<String, Object> body);

    // POST /api/perkembangan/batch → { success, message }
    @POST("api/perkembangan/batch")
    Call<MessageResponse> inputPerkembanganBatch(@Body Map<String, Object> body);

    // ─── ABSENSI ──────────────────────────────────────────────

    // GET /api/absensi/kelas/:id → { success, data: [ {...} ] }
    @GET("api/absensi/kelas/{kelasId}")
    Call<AbsensiListResponse> getAbsensiKelas(
            @Path("kelasId") int kelasId,
            @Query("tanggal") String tanggal
    );

    // GET /api/absensi/rekap-bulanan → { success, data: [ {...} ] }
    @GET("api/absensi/rekap-bulanan")
    Call<AbsensiListResponse> getRekapAbsensiKelas(
            @Query("kelas_id") Integer kelasId,
            @Query("bulan") Integer bulan,
            @Query("tahun") Integer tahun
    );

    // GET /api/absensi/siswa/:id/rekap → { success, data: [ {...} ] }
    @GET("api/absensi/siswa/{siswaId}/rekap")
    Call<AbsensiSiswaRekapResponse> getRekapAbsensiSiswa(
            @Path("siswaId") int siswaId,
            @Query("bulan") Integer bulan,
            @Query("tahun") Integer tahun
    );

    // POST /api/absensi → { success, message }
    @POST("api/absensi")
    Call<MessageResponse> inputAbsensi(@Body Map<String, Object> body);

    // ─── PPI ──────────────────────────────────────────────────

    @GET("api/ppi/siswa/{siswaId}")
    Call<PpiListResponse> getPpiSiswa(@Path("siswaId") int siswaId);

    @GET("api/ppi/kelas/{kelasId}")
    Call<PpiListResponse> getPpiKelas(@Path("kelasId") int kelasId);

    @GET("api/ppi/{id}")
    Call<PpiDetailResponse> getPpiById(@Path("id") int id);

    @POST("api/ppi")
    Call<MessageResponse> buatPpi(@Body Map<String, Object> body);

    @PUT("api/ppi/{id}")
    Call<MessageResponse> updatePpi(@Path("id") int id, @Body Map<String, Object> body);

    // ─── PESAN ────────────────────────────────────────────────

    // GET /api/pesan/inbox → { success, data: [...], unread: N }
    @GET("api/pesan/inbox")
    Call<PesanListResponse> getInbox();

    // GET /api/pesan/kontak → { success, data: [...] }
    @GET("api/pesan/kontak")
    Call<PesanListResponse> getKontak();

    // GET /api/pesan/percakapan/:id → { success, data: [...] }
    @GET("api/pesan/percakapan/{userId}")
    Call<PesanListResponse> getPercakapan(@Path("userId") int userId);

    // POST /api/pesan → { success, message }
    @POST("api/pesan")
    Call<MessageResponse> kirimPesan(@Body Map<String, Object> body);

    // PUT /api/pesan/:id/baca → { success, message }
    @PUT("api/pesan/{id}/baca")
    Call<MessageResponse> bacaPesan(@Path("id") int id);

    // ─── PENGUMUMAN ───────────────────────────────────────────

    @GET("api/pengumuman")
    Call<PesanListResponse> getPengumuman();

    @POST("api/pengumuman")
    Call<MessageResponse> kirimPengumuman(@Body Map<String, Object> body);

    // ─── LAPORAN ──────────────────────────────────────────────

    @GET("api/laporan")
    Call<LaporanListResponse> getLaporan(@Query("tipe") String tipe);

    @POST("api/laporan/generate")
    Call<MessageResponse> generateLaporan(@Body Map<String, Object> body);

    @DELETE("api/laporan/{id}")
    Call<MessageResponse> deleteLaporan(@Path("id") int id);

    @Multipart
    @POST("api/laporan/upload")
    Call<MessageResponse> uploadLaporan(
            @Part MultipartBody.Part file,
            @Part("tipe") RequestBody tipe,
            @Part("periode") RequestBody periode,
            @Part("kelas_id") RequestBody kelasId,
            @Part("tahun_ajaran") RequestBody tahunAjaran
    );

    @GET("api/laporan/kelas/{kelasId}")
    Call<LaporanKelasResponse> getLaporanKelas(
            @Path("kelasId") int kelasId,
            @Query("bulan") Integer bulan,
            @Query("tahun") Integer tahun
    );

    // ─── ASPEK ────────────────────────────────────────────────

    // GET /api/aspek → { success, data: [...] }
    @GET("api/aspek")
    Call<AspekListResponse> getAspek();

    @GET("api/tingkat")
    Call<TingkatListResponse> getTingkat();

    // ─── KEGIATAN ─────────────────────────────────────────────

    @GET("api/kegiatan")
    Call<KegiatanListResponse> getKegiatan(
            @Query("bulan") Integer bulan,
            @Query("tahun") Integer tahun
    );

    @POST("api/kegiatan")
    Call<MessageResponse> createKegiatan(@Body Map<String, Object> body);

    @Multipart
    @POST("api/upload/kegiatan-banner")
    Call<KegiatanBannerUploadResponse> uploadKegiatanBanner(
            @Part MultipartBody.Part banner
    );
}
