package quanlydethi.service;

import quanlydethi.model.*;
import quanlydethi.dao.*;
import java.util.List;

public class CauHoiNhapLieuService {
    private CauHoiDAO cauHoiDAO;
    private LuaChonDAO luaChonDAO;
    private DoanVanDocDAO doanVanDocDAO;
    private TepAmThanhDAO tepAmThanhDAO;

    public CauHoiNhapLieuService() {
        this.cauHoiDAO = new CauHoiDAO();
        this.luaChonDAO = new LuaChonDAO();
        this.doanVanDocDAO = new DoanVanDocDAO();
        this.tepAmThanhDAO = new TepAmThanhDAO();
    }

    public boolean luuCauHoiDayDu(CauHoi cauHoi, List<LuaChon> danhSachLuaChon, DoanVanDoc doanVanMoi, TepAmThanh tepAmThanhMoi) {
        try {
            if (doanVanMoi != null && doanVanMoi.getNoiDungDoanVan() != null && !doanVanMoi.getNoiDungDoanVan().isEmpty()) {
                boolean dvSaved = doanVanDocDAO.addDoanVanDoc(doanVanMoi);
                if (dvSaved && doanVanMoi.getMaDoanVan() > 0) {
                    cauHoi.setMaDoanVan(doanVanMoi.getMaDoanVan());
                } else {
                    System.err.println("Lỗi khi lưu đoạn văn mới.");
                }
            }

            if (tepAmThanhMoi != null && tepAmThanhMoi.getTenTep() != null && !tepAmThanhMoi.getTenTep().isEmpty()) {
                boolean atSaved = tepAmThanhDAO.addTepAmThanh(tepAmThanhMoi);
                if (atSaved && tepAmThanhMoi.getMaAmThanh() > 0) {
                    cauHoi.setMaAmThanh(tepAmThanhMoi.getMaAmThanh());
                } else {
                    System.err.println("Lỗi khi lưu tệp âm thanh mới.");
                }
            }

            boolean chSaved = cauHoiDAO.addCauHoi(cauHoi);
            if (chSaved && cauHoi.getMaCauHoi() > 0) {
                for (LuaChon lc : danhSachLuaChon) {
                    lc.setMaCauHoi(cauHoi.getMaCauHoi());
                    luaChonDAO.addLuaChon(lc);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean capNhatCauHoiDayDu(CauHoi cauHoi, List<LuaChon> danhSachLuaChon,
                                      DoanVanDoc doanVanMoiHoacCapNhat, TepAmThanh tepAmThanhMoiHoacCapNhat,
                                      Integer maDoanVanTruocKhiSua, Integer maAmThanhTruocKhiSua) {
        try {
            if (cauHoi.getMaLoaiCauHoi() == 3) {
                if (doanVanMoiHoacCapNhat != null && doanVanMoiHoacCapNhat.getNoiDungDoanVan() != null && !doanVanMoiHoacCapNhat.getNoiDungDoanVan().isEmpty() && doanVanMoiHoacCapNhat.getMaDoanVan() == 0) {
                    doanVanDocDAO.addDoanVanDoc(doanVanMoiHoacCapNhat);
                    cauHoi.setMaDoanVan(doanVanMoiHoacCapNhat.getMaDoanVan());
                } else if (cauHoi.getMaDoanVan() != null) {
                    // Đã chọn đoạn văn có sẵn
                } else {
                    cauHoi.setMaDoanVan(null);
                }
            } else {
                cauHoi.setMaDoanVan(null);
            }

            if (cauHoi.getMaLoaiCauHoi() == 4) {
                if (tepAmThanhMoiHoacCapNhat != null && tepAmThanhMoiHoacCapNhat.getTenTep() != null && !tepAmThanhMoiHoacCapNhat.getTenTep().isEmpty() && tepAmThanhMoiHoacCapNhat.getMaAmThanh() == 0) {
                    tepAmThanhDAO.addTepAmThanh(tepAmThanhMoiHoacCapNhat);
                    cauHoi.setMaAmThanh(tepAmThanhMoiHoacCapNhat.getMaAmThanh());
                } else if (cauHoi.getMaAmThanh() != null) {
                    // Đã chọn tệp âm thanh có sẵn
                } else {
                    cauHoi.setMaAmThanh(null);
                }
            } else {
                cauHoi.setMaAmThanh(null);
            }

            boolean cauHoiUpdated = cauHoiDAO.updateCauHoi(cauHoi);
            if (!cauHoiUpdated) {
                System.err.println("Không thể cập nhật câu hỏi chính.");
                return false;
            }

            luaChonDAO.deleteLuaChonByMaCauHoi(cauHoi.getMaCauHoi());
            if (danhSachLuaChon != null) {
                for (LuaChon lc : danhSachLuaChon) {
                    lc.setMaCauHoi(cauHoi.getMaCauHoi());
                    luaChonDAO.addLuaChon(lc);
                }
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
