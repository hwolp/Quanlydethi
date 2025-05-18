package quanlydethi.model;

import java.sql.Timestamp; // Hoặc java.util.Date hoặc java.time.LocalDateTime

public class DeThi {
    private int maDeThi;
    private String tenDeThi;
    private Integer maTrinhDo; // Nullable
    private Timestamp ngayTaoDe;
    private Integer thoiGianLamBaiPhut; // Nullable
    private boolean laDeNgauNhien;

    // Constructors
    public DeThi() {
    }

    public DeThi(int maDeThi, String tenDeThi, Integer maTrinhDo, Timestamp ngayTaoDe, Integer thoiGianLamBaiPhut, boolean laDeNgauNhien) {
        this.maDeThi = maDeThi;
        this.tenDeThi = tenDeThi;
        this.maTrinhDo = maTrinhDo;
        this.ngayTaoDe = ngayTaoDe;
        this.thoiGianLamBaiPhut = thoiGianLamBaiPhut;
        this.laDeNgauNhien = laDeNgauNhien;
    }

    // Getters and Setters
    public int getMaDeThi() {
        return maDeThi;
    }

    public void setMaDeThi(int maDeThi) {
        this.maDeThi = maDeThi;
    }

    public String getTenDeThi() {
        return tenDeThi;
    }

    public void setTenDeThi(String tenDeThi) {
        this.tenDeThi = tenDeThi;
    }

    public Integer getMaTrinhDo() {
        return maTrinhDo;
    }

    public void setMaTrinhDo(Integer maTrinhDo) {
        this.maTrinhDo = maTrinhDo;
    }

    public Timestamp getNgayTaoDe() {
        return ngayTaoDe;
    }

    public void setNgayTaoDe(Timestamp ngayTaoDe) {
        this.ngayTaoDe = ngayTaoDe;
    }

    public Integer getThoiGianLamBaiPhut() {
        return thoiGianLamBaiPhut;
    }

    public void setThoiGianLamBaiPhut(Integer thoiGianLamBaiPhut) {
        this.thoiGianLamBaiPhut = thoiGianLamBaiPhut;
    }

    public boolean isLaDeNgauNhien() {
        return laDeNgauNhien;
    }

    public void setLaDeNgauNhien(boolean laDeNgauNhien) {
        this.laDeNgauNhien = laDeNgauNhien;
    }

    @Override
    public String toString() {
        return "DeThi{" +
               "maDeThi=" + maDeThi +
               ", tenDeThi='" + tenDeThi + '\'' +
               ", maTrinhDo=" + maTrinhDo +
               ", ngayTaoDe=" + ngayTaoDe +
               ", thoiGianLamBaiPhut=" + thoiGianLamBaiPhut +
               ", laDeNgauNhien=" + laDeNgauNhien +
               '}';
    }
}