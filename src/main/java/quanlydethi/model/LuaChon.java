package quanlydethi.model;

public class LuaChon {
    private int maLuaChon;
    private int maCauHoi;
    private String noiDungLuaChon;
    private boolean laDapAnDung;

    // Constructors
    public LuaChon() {
    }

    public LuaChon(int maLuaChon, int maCauHoi, String noiDungLuaChon, boolean laDapAnDung) {
        this.maLuaChon = maLuaChon;
        this.maCauHoi = maCauHoi;
        this.noiDungLuaChon = noiDungLuaChon;
        this.laDapAnDung = laDapAnDung;
    }

    // Getters and Setters
    public int getMaLuaChon() {
        return maLuaChon;
    }

    public void setMaLuaChon(int maLuaChon) {
        this.maLuaChon = maLuaChon;
    }

    public int getMaCauHoi() {
        return maCauHoi;
    }

    public void setMaCauHoi(int maCauHoi) {
        this.maCauHoi = maCauHoi;
    }

    public String getNoiDungLuaChon() {
        return noiDungLuaChon;
    }

    public void setNoiDungLuaChon(String noiDungLuaChon) {
        this.noiDungLuaChon = noiDungLuaChon;
    }

    public boolean isLaDapAnDung() {
        return laDapAnDung;
    }

    public void setLaDapAnDung(boolean laDapAnDung) {
        this.laDapAnDung = laDapAnDung;
    }

    @Override
    public String toString() {
        return "LuaChon{" +
               "maLuaChon=" + maLuaChon +
               ", maCauHoi=" + maCauHoi +
               ", noiDungLuaChon='" + noiDungLuaChon + '\'' +
               ", laDapAnDung=" + laDapAnDung +
               '}';
    }
}