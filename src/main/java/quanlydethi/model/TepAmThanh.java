package quanlydethi.model;

import java.sql.Time; // Hoặc java.time.LocalTime nếu dùng JDBC 4.2+ và cấu hình phù hợp

public class TepAmThanh {
    private int maAmThanh;
    private String tenTep;
    private String duongDanTep;
    private Time thoiLuong; // Hoặc LocalTime
    private Integer maTrinhDo; // Sử dụng Integer để cho phép giá trị null

    // Constructors
    public TepAmThanh() {
    }

    public TepAmThanh(int maAmThanh, String tenTep, String duongDanTep, Time thoiLuong, Integer maTrinhDo) {
        this.maAmThanh = maAmThanh;
        this.tenTep = tenTep;
        this.duongDanTep = duongDanTep;
        this.thoiLuong = thoiLuong;
        this.maTrinhDo = maTrinhDo;
    }

    // Getters and Setters
    public int getMaAmThanh() {
        return maAmThanh;
    }

    public void setMaAmThanh(int maAmThanh) {
        this.maAmThanh = maAmThanh;
    }

    public String getTenTep() {
        return tenTep;
    }

    public void setTenTep(String tenTep) {
        this.tenTep = tenTep;
    }

    public String getDuongDanTep() {
        return duongDanTep;
    }

    public void setDuongDanTep(String duongDanTep) {
        this.duongDanTep = duongDanTep;
    }

    public Time getThoiLuong() {
        return thoiLuong;
    }

    public void setThoiLuong(Time thoiLuong) {
        this.thoiLuong = thoiLuong;
    }

    public Integer getMaTrinhDo() {
        return maTrinhDo;
    }

    public void setMaTrinhDo(Integer maTrinhDo) {
        this.maTrinhDo = maTrinhDo;
    }

    @Override
    public String toString() {
        return "TepAmThanh{" +
               "maAmThanh=" + maAmThanh +
               ", tenTep='" + tenTep + '\'' +
               ", duongDanTep='" + duongDanTep + '\'' +
               ", thoiLuong=" + thoiLuong +
               ", maTrinhDo=" + maTrinhDo +
               '}';
    }
}