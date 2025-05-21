package quanlydethi.model;

public class LoaiCauHoi {
    private int maLoaiCauHoi;
    private String tenLoai;
    private String moTa;

    // Constructors
    public LoaiCauHoi() {
    }

    public LoaiCauHoi(int maLoaiCauHoi, String tenLoai, String moTa) {
        this.maLoaiCauHoi = maLoaiCauHoi;
        this.tenLoai = tenLoai;
        this.moTa = moTa;
    }

    // Getters and Setters
    public int getMaLoaiCauHoi() {
        return maLoaiCauHoi;
    }

    public void setMaLoaiCauHoi(int maLoaiCauHoi) {
        this.maLoaiCauHoi = maLoaiCauHoi;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

//    @Override
//    public String toString() {
//        return "LoaiCauHoi{" +
//               "maLoaiCauHoi=" + maLoaiCauHoi +
//               ", tenLoai='" + tenLoai + '\'' +
//               ", moTa='" + moTa + '\'' +
//               '}';
//    }

    @Override
    public String toString() {
        return tenLoai; // Giả sử thuộc tính tên là 'tenLoai'
    }
}