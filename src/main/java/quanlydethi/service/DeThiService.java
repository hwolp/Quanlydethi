package quanlydethi.service;

import quanlydethi.model.CauHoi;
import quanlydethi.model.DeThi;
import quanlydethi.dao.CauHoiDAO;
import quanlydethi.dao.DeThiDAO;
import quanlydethi.dao.CauHoiTrongDeThiDAO;
import quanlydethi.dbconnector.SQLServerConnector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DeThiService {
    private DeThiDAO deThiDAO;
    private CauHoiDAO cauHoiDAO;
    private CauHoiTrongDeThiDAO cauHoiTrongDeThiDAO;

    public enum LoaiPhanCauHoi {
        DOC_HIEU,
        NGHE_HIEU,
        KHAC
    }

    public static class CauTrucPhanDeChiTiet {
        private LoaiPhanCauHoi loaiPhan;
        private int soLuong;

        public CauTrucPhanDeChiTiet(LoaiPhanCauHoi loaiPhan, int soLuong) {
            this.loaiPhan = loaiPhan;
            this.soLuong = soLuong;
        }

        public LoaiPhanCauHoi getLoaiPhan() { return loaiPhan; }
        public int getSoLuong() { return soLuong; }
    }

    public DeThiService() {
        this.deThiDAO = new DeThiDAO();
        this.cauHoiDAO = new CauHoiDAO();
        this.cauHoiTrongDeThiDAO = new CauHoiTrongDeThiDAO();
    }

    public DeThi taoDeThiNgauNhienTheoCauTrucChiTiet(String tenDeThi, int maTrinhDo,
                                                     List<CauTrucPhanDeChiTiet> cauTrucChiTietList,
                                                     Integer thoiGianLamBaiPhut) throws Exception {
        List<CauHoi> tatCaCauHoiTheoTrinhDo = cauHoiDAO.getCauHoiByTrinhDo(maTrinhDo);

        if (tatCaCauHoiTheoTrinhDo == null || tatCaCauHoiTheoTrinhDo.isEmpty()) {
            System.err.println("Không có câu hỏi nào thuộc trình độ ID: " + maTrinhDo);
            throw new Exception("Không có câu hỏi nào thuộc trình độ ID: " + maTrinhDo);
        }

        Collections.shuffle(tatCaCauHoiTheoTrinhDo, new Random(System.nanoTime()));

        List<CauHoi> cauHoiDaChonChoDeThi = new ArrayList<>();
        List<Integer> idCauHoiDaSuDung = new ArrayList<>();

        for (CauTrucPhanDeChiTiet phan : cauTrucChiTietList) {
            if (phan.getSoLuong() <= 0) continue;

            List<CauHoi> cauHoiPhuHopChoPhan = new ArrayList<>();

            for (CauHoi ch : tatCaCauHoiTheoTrinhDo) {
                if (idCauHoiDaSuDung.contains(ch.getMaCauHoi())) continue;

                boolean phuHop = false;
                switch (phan.getLoaiPhan()) {
                    case DOC_HIEU:
                        if (ch.getMaDoanVan() != null && ch.getMaDoanVan() > 0) phuHop = true;
                        break;
                    case NGHE_HIEU:
                        if (ch.getMaAmThanh() != null && ch.getMaAmThanh() > 0) phuHop = true;
                        break;
                    case KHAC:
                        if ((ch.getMaDoanVan() == null || ch.getMaDoanVan() == 0) &&
                            (ch.getMaAmThanh() == null || ch.getMaAmThanh() == 0)) phuHop = true;
                        break;
                }
                if (phuHop) {
                    cauHoiPhuHopChoPhan.add(ch);
                }
            }

            if (cauHoiPhuHopChoPhan.size() < phan.getSoLuong()) {
                System.err.println("Không đủ câu hỏi cho phần " + phan.getLoaiPhan() + " (Trình độ ID: " + maTrinhDo + "). Cần: " + phan.getSoLuong() + ", Có sẵn: " + cauHoiPhuHopChoPhan.size());
                throw new Exception("Không đủ câu hỏi cho phần " + phan.getLoaiPhan() + ". Cần: " + phan.getSoLuong() + ", Có: " + cauHoiPhuHopChoPhan.size());
            }

            int count = 0;
            for (CauHoi ch : cauHoiPhuHopChoPhan) {
                if (count < phan.getSoLuong()) {
                    cauHoiDaChonChoDeThi.add(ch);
                    idCauHoiDaSuDung.add(ch.getMaCauHoi());
                    count++;
                } else {
                    break;
                }
            }
        }

        DeThi deThiMoi = new DeThi();
        deThiMoi.setTenDeThi(tenDeThi);
        deThiMoi.setMaTrinhDo(maTrinhDo);
        deThiMoi.setNgayTaoDe(new Timestamp(System.currentTimeMillis()));
        deThiMoi.setThoiGianLamBaiPhut(thoiGianLamBaiPhut);
        deThiMoi.setLaDeNgauNhien(true);

        Connection conn = null;
        try {
            conn = SQLServerConnector.getConnection();
            conn.setAutoCommit(false);

            boolean deThiSaved = deThiDAO.addDeThi(deThiMoi, conn);
            if (deThiSaved && deThiMoi.getMaDeThi() > 0) {
                for (CauHoi ch : cauHoiDaChonChoDeThi) {
                    boolean added = cauHoiTrongDeThiDAO.addCauHoiVaoDeThi(deThiMoi.getMaDeThi(), ch.getMaCauHoi(), conn);
                    if (!added) {
                        System.err.println("Không thể thêm câu hỏi ID " + ch.getMaCauHoi() + " vào đề thi ID " + deThiMoi.getMaDeThi() + ". Có thể đã tồn tại hoặc lỗi khác.");
                    }
                }
                conn.commit();
                return deThiMoi;
            } else {
                if (conn != null) conn.rollback();
                throw new SQLException("Không thể lưu đề thi mới vào cơ sở dữ liệu.");
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("Lỗi SQL trong khi tạo đề: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Lỗi CSDL khi tạo đề theo cấu trúc: " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    public DeThi taoDeThiNgauNhien(String tenDeThi, int maTrinhDo, int soLuongCauHoi, Integer thoiGianLamBaiPhut) throws Exception {
        List<CauHoi> danhSachCauHoiTheoTrinhDo = cauHoiDAO.getCauHoiByTrinhDo(maTrinhDo);

        if (danhSachCauHoiTheoTrinhDo == null || danhSachCauHoiTheoTrinhDo.isEmpty()) {
            throw new Exception("Không có câu hỏi nào thuộc trình độ đã chọn.");
        }
        if (danhSachCauHoiTheoTrinhDo.size() < soLuongCauHoi) {
            throw new Exception("Không đủ số lượng câu hỏi ("+soLuongCauHoi+") yêu cầu. Chỉ có " +
                                danhSachCauHoiTheoTrinhDo.size() + " câu cho trình độ này.");
        }
        Collections.shuffle(danhSachCauHoiTheoTrinhDo, new Random(System.nanoTime()));
        List<CauHoi> cauHoiChoDeThi = danhSachCauHoiTheoTrinhDo.subList(0, soLuongCauHoi);

        DeThi deThiMoi = new DeThi();
        deThiMoi.setTenDeThi(tenDeThi);
        deThiMoi.setMaTrinhDo(maTrinhDo);
        deThiMoi.setNgayTaoDe(new Timestamp(System.currentTimeMillis()));
        deThiMoi.setThoiGianLamBaiPhut(thoiGianLamBaiPhut);
        deThiMoi.setLaDeNgauNhien(true);

        Connection conn = null;
        try {
            conn = SQLServerConnector.getConnection();
            conn.setAutoCommit(false);
            boolean deThiSaved = deThiDAO.addDeThi(deThiMoi, conn);
            if (deThiSaved && deThiMoi.getMaDeThi() > 0) {
                for (CauHoi ch : cauHoiChoDeThi) {
                    boolean added = cauHoiTrongDeThiDAO.addCauHoiVaoDeThi(deThiMoi.getMaDeThi(), ch.getMaCauHoi(), conn);
                    if (!added) {
                        throw new SQLException("Không thể thêm câu hỏi ID " + ch.getMaCauHoi() + " vào đề thi ID " + deThiMoi.getMaDeThi());
                    }
                }
                conn.commit();
                return deThiMoi;
            } else {
                if(conn!=null) conn.rollback();
                throw new SQLException("Không thể lưu đề thi mới vào cơ sở dữ liệu.");
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException exRollback) { exRollback.printStackTrace(); }
            throw new Exception("Lỗi khi tạo đề thi ngẫu nhiên (đơn giản): " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException exClose) { exClose.printStackTrace(); }
        }
    }
}
