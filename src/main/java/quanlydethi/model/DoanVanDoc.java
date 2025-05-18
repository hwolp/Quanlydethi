package quanlydethi.model;

public class DoanVanDoc {
    private int maDoanVan;
    private String noiDungDoanVan;
    private Integer maTrinhDo; // Sử dụng Integer để cho phép giá trị null

    // Constructors
    public DoanVanDoc() {
    }

    public DoanVanDoc(int maDoanVan, String noiDungDoanVan, Integer maTrinhDo) {
        this.maDoanVan = maDoanVan;
        this.noiDungDoanVan = noiDungDoanVan;
        this.maTrinhDo = maTrinhDo;
    }

    // Getters and Setters
    public int getMaDoanVan() {
        return maDoanVan;
    }

    public void setMaDoanVan(int maDoanVan) {
        this.maDoanVan = maDoanVan;
    }

    public String getNoiDungDoanVan() {
        return noiDungDoanVan;
    }

    public void setNoiDungDoanVan(String noiDungDoanVan) {
        this.noiDungDoanVan = noiDungDoanVan;
    }

    public Integer getMaTrinhDo() {
        return maTrinhDo;
    }

    public void setMaTrinhDo(Integer maTrinhDo) {
        this.maTrinhDo = maTrinhDo;
    }

    @Override
    public String toString() {
        return "DoanVanDoc{" +
               "maDoanVan=" + maDoanVan +
               ", noiDungDoanVan='" + noiDungDoanVan + '\'' +
               ", maTrinhDo=" + maTrinhDo +
               '}';
    }
}