package quanlydethi.dto;

public class LuaChonDTO {
    private String kyHieu; // Ví dụ: "A", "B", "1", "2"
    private String noiDung; // Nội dung của lựa chọn
    private boolean laDapAnDung; // TRƯỜNG MỚI: Đánh dấu nếu đây là đáp án đúng

    public LuaChonDTO() {
        this.laDapAnDung = false; // Mặc định không phải là đáp án đúng
    }

    public LuaChonDTO(String kyHieu, String noiDung) {
        this.kyHieu = kyHieu;
        this.noiDung = noiDung;
        this.laDapAnDung = false; // Mặc định
    }

    public LuaChonDTO(String kyHieu, String noiDung, boolean laDapAnDung) {
        this.kyHieu = kyHieu;
        this.noiDung = noiDung;
        this.laDapAnDung = laDapAnDung;
    }

    // Getters
    public String getKyHieu() {
        return kyHieu;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public boolean isLaDapAnDung() { // Getter cho boolean thường bắt đầu bằng "is"
        return laDapAnDung;
    }

    // Setters
    public void setKyHieu(String kyHieu) {
        this.kyHieu = kyHieu;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public void setLaDapAnDung(boolean laDapAnDung) {
        this.laDapAnDung = laDapAnDung;
    }

    @Override
    public String toString() {
        return "LuaChonDTO{" +
               "kyHieu='" + kyHieu + '\'' +
               ", noiDung='" + noiDung + '\'' +
               ", laDapAnDung=" + laDapAnDung + // Thêm vào toString
               '}';
    }
}
