package quanlydethi.model;

public class CauHoiTrongDeThi {
    private int maCauHoiDeThi;
    private int maDeThi;
    private int maCauHoi;

    // Constructors
    public CauHoiTrongDeThi() {
    }

    public CauHoiTrongDeThi(int maCauHoiDeThi, int maDeThi, int maCauHoi) {
        this.maCauHoiDeThi = maCauHoiDeThi;
        this.maDeThi = maDeThi;
        this.maCauHoi = maCauHoi;
    }

    // Getters and Setters
    public int getMaCauHoiDeThi() {
        return maCauHoiDeThi;
    }

    public void setMaCauHoiDeThi(int maCauHoiDeThi) {
        this.maCauHoiDeThi = maCauHoiDeThi;
    }

    public int getMaDeThi() {
        return maDeThi;
    }

    public void setMaDeThi(int maDeThi) {
        this.maDeThi = maDeThi;
    }

    public int getMaCauHoi() {
        return maCauHoi;
    }

    public void setMaCauHoi(int maCauHoi) {
        this.maCauHoi = maCauHoi;
    }

    @Override
    public String toString() {
        return "CauHoiTrongDeThi{" +
               "maCauHoiDeThi=" + maCauHoiDeThi +
               ", maDeThi=" + maDeThi +
               ", maCauHoi=" + maCauHoi +
               '}';
    }
}