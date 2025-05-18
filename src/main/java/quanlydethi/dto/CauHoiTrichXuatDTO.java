package quanlydethi.dto; // Hoặc package model của bạn

import java.util.List;
import java.util.ArrayList;

public class CauHoiTrichXuatDTO {
    private String noiDungCauHoi;
    private List<LuaChonDTO> cacLuaChon;
    private String dapAnDungKyHieu;
    private String giaiThich;
    private String loaiCauHoiGoiY;
    private String trinhDoGoiY;
    private String audioLinkGoiY; // TRƯỜNG MỚI cho link audio

    public CauHoiTrichXuatDTO() {
        this.cacLuaChon = new ArrayList<>();
    }

    // Getters and Setters (bao gồm cả cho audioLinkGoiY)
    public String getNoiDungCauHoi() { return noiDungCauHoi; }
    public void setNoiDungCauHoi(String noiDungCauHoi) { this.noiDungCauHoi = noiDungCauHoi; }

    public List<LuaChonDTO> getCacLuaChon() { return cacLuaChon; }
    public void setCacLuaChon(List<LuaChonDTO> cacLuaChon) { this.cacLuaChon = cacLuaChon; }
    public void addLuaChon(LuaChonDTO luaChon) { this.cacLuaChon.add(luaChon); }

    public String getDapAnDungKyHieu() { return dapAnDungKyHieu; }
    public void setDapAnDungKyHieu(String dapAnDungKyHieu) { this.dapAnDungKyHieu = dapAnDungKyHieu; }

    public String getGiaiThich() { return giaiThich; }
    public void setGiaiThich(String giaiThich) { this.giaiThich = giaiThich; }

    public String getLoaiCauHoiGoiY() { return loaiCauHoiGoiY; }
    public void setLoaiCauHoiGoiY(String loaiCauHoiGoiY) { this.loaiCauHoiGoiY = loaiCauHoiGoiY; }

    public String getTrinhDoGoiY() { return trinhDoGoiY; }
    public void setTrinhDoGoiY(String trinhDoGoiY) { this.trinhDoGoiY = trinhDoGoiY; }

    public String getAudioLinkGoiY() { return audioLinkGoiY; } // GETTER MỚI
    public void setAudioLinkGoiY(String audioLinkGoiY) { this.audioLinkGoiY = audioLinkGoiY; } // SETTER MỚI

    @Override
    public String toString() {
        return "CauHoiTrichXuatDTO{" +
               "noiDungCauHoi='" + noiDungCauHoi + '\'' +
               ", cacLuaChon=" + cacLuaChon +
               ", dapAnDungKyHieu='" + dapAnDungKyHieu + '\'' +
               ", giaiThich='" + (giaiThich != null ? giaiThich : "") + '\'' +
               ", loaiCauHoiGoiY='" + (loaiCauHoiGoiY != null ? loaiCauHoiGoiY : "") + '\'' +
               ", trinhDoGoiY='" + (trinhDoGoiY != null ? trinhDoGoiY : "") + '\'' +
               ", audioLinkGoiY='" + (audioLinkGoiY != null ? audioLinkGoiY : "") + '\'' + // THÊM VÀO TOSTRING
               '}';
    }
}