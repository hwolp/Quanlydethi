package quanlydethi.model;

public class CauHoi {
    private int maCauHoi;
    private String noiDungCauHoi;
    private int maLoaiCauHoi;
    private int maTrinhDo;
    private Integer maDoanVan; // Nullable
    private Integer maAmThanh; // Nullable
    private int diem;

    // Constructors
    public CauHoi() {
    }

    public CauHoi(int maCauHoi, String noiDungCauHoi, int maLoaiCauHoi, int maTrinhDo, Integer maDoanVan, Integer maAmThanh, int diem) {
        this.maCauHoi = maCauHoi;
        this.noiDungCauHoi = noiDungCauHoi;
        this.maLoaiCauHoi = maLoaiCauHoi;
        this.maTrinhDo = maTrinhDo;
        this.maDoanVan = maDoanVan;
        this.maAmThanh = maAmThanh;
        this.diem = diem;
    }

    // Getters and Setters
    public int getMaCauHoi() {
        return maCauHoi;
    }

    public void setMaCauHoi(int maCauHoi) {
        this.maCauHoi = maCauHoi;
    }

    public String getNoiDungCauHoi() {
        return noiDungCauHoi;
    }

    public void setNoiDungCauHoi(String noiDungCauHoi) {
        this.noiDungCauHoi = noiDungCauHoi;
    }

    public int getMaLoaiCauHoi() {
        return maLoaiCauHoi;
    }

    public void setMaLoaiCauHoi(int maLoaiCauHoi) {
        this.maLoaiCauHoi = maLoaiCauHoi;
    }

    public int getMaTrinhDo() {
        return maTrinhDo;
    }

    public void setMaTrinhDo(int maTrinhDo) {
        this.maTrinhDo = maTrinhDo;
    }

    public Integer getMaDoanVan() {
        return maDoanVan;
    }

    public void setMaDoanVan(Integer maDoanVan) {
        this.maDoanVan = maDoanVan;
    }

    public Integer getMaAmThanh() {
        return maAmThanh;
    }

    public void setMaAmThanh(Integer maAmThanh) {
        this.maAmThanh = maAmThanh;
    }

    public int getDiem() {
        return diem;
    }

    public void setDiem(int diem) {
        this.diem = diem;
    }

    @Override
    public String toString() {
        return "CauHoi{" +
               "maCauHoi=" + maCauHoi +
               ", noiDungCauHoi='" + noiDungCauHoi + '\'' +
               ", maLoaiCauHoi=" + maLoaiCauHoi +
               ", maTrinhDo=" + maTrinhDo +
               ", maDoanVan=" + maDoanVan +
               ", maAmThanh=" + maAmThanh +
               ", diem=" + diem +
               '}';
    }
}