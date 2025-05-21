package quanlydethi.model;

public class TrinhDo {
    private int maTrinhDo;
    private String tenTrinhDo;
    private String moTa;

    // Constructors
    public TrinhDo() {
    }

    public TrinhDo(int maTrinhDo, String tenTrinhDo, String moTa) {
        this.maTrinhDo = maTrinhDo;
        this.tenTrinhDo = tenTrinhDo;
        this.moTa = moTa;
    }

    // Getters and Setters
    public int getMaTrinhDo() {
        return maTrinhDo;
    }

    public void setMaTrinhDo(int maTrinhDo) {
        this.maTrinhDo = maTrinhDo;
    }

    public String getTenTrinhDo() {
        return tenTrinhDo;
    }

    public void setTenTrinhDo(String tenTrinhDo) {
        this.tenTrinhDo = tenTrinhDo;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

//    @Override
//    public String toString() {
//        return "TrinhDo{" +
//               "maTrinhDo=" + maTrinhDo +
//               ", tenTrinhDo='" + tenTrinhDo + '\'' +
//               ", moTa='" + moTa + '\'' +
//               '}';
//    }

    @Override
    public String toString() {
        return tenTrinhDo; // Giả sử thuộc tính tên là 'tenLoai'
    }
}