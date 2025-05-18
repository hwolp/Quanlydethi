package quanlydethi.dto;

public class LuaChonDTO {
    private String kyHieu; 
    private String noiDung; 

    public LuaChonDTO() {
    }

    public LuaChonDTO(String kyHieu, String noiDung) {
        this.kyHieu = kyHieu;
        this.noiDung = noiDung;
    }

    // Getters
    public String getKyHieu() { return kyHieu; }
    public String getNoiDung() { return noiDung; }

    // Setters
    public void setKyHieu(String kyHieu) { this.kyHieu = kyHieu; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    @Override
    public String toString() {
        return "LuaChonDTO{" + "kyHieu='" + kyHieu + '\'' + ", noiDung='" + noiDung + '\'' + '}';
    }
}