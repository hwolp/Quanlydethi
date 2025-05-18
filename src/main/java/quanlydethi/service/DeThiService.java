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
import java.util.stream.Collectors;


public class DeThiService {
    private DeThiDAO deThiDAO;
    private CauHoiDAO cauHoiDAO;
    private CauHoiTrongDeThiDAO cauHoiTrongDeThiDAO;

    // Enum để định nghĩa các loại phần câu hỏi
    public enum LoaiPhanCauHoi {
        DOC_HIEU,    // MaDoanVan != NULL
        NGHE_HIEU,   // MaAmThanh != NULL
        KHAC         // MaDoanVan == NULL AND MaAmThanh == NULL
    }

    // Lớp nội để định nghĩa cấu trúc chi tiết cho mỗi phần
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

    /**
     * Tạo đề thi ngẫu nhiên với cấu trúc nhiều phần chi tiết.
     * @param tenDeThi Tên đề thi
     * @param maTrinhDo Mã trình độ
     * @param cauTrucChiTietList Danh sách cấu trúc chi tiết các phần của đề
     * @param thoiGianLamBaiPhut Thời gian làm bài
     * @return DeThi đã tạo, hoặc null nếu có lỗi không nghiêm trọng (ví dụ không đủ câu hỏi cho 1 phần)
     * @throws Exception Lỗi CSDL nghiêm trọng
     */
    public DeThi taoDeThiNgauNhienTheoCauTrucChiTiet(String tenDeThi, int maTrinhDo,
                                                     List<CauTrucPhanDeChiTiet> cauTrucChiTietList,
                                                     Integer thoiGianLamBaiPhut) throws Exception {
        // Bước 1: Lấy TẤT CẢ câu hỏi thuộc trình độ yêu cầu
        List<CauHoi> tatCaCauHoiTheoTrinhDo = cauHoiDAO.getCauHoiByTrinhDo(maTrinhDo);

        if (tatCaCauHoiTheoTrinhDo == null || tatCaCauHoiTheoTrinhDo.isEmpty()) {
            System.err.println("Không có câu hỏi nào thuộc trình độ ID: " + maTrinhDo);
            throw new Exception("Không có câu hỏi nào thuộc trình độ ID: " + maTrinhDo);
        }
        
        // Xáo trộn một lần danh sách nguồn để tăng tính ngẫu nhiên khi chọn
        Collections.shuffle(tatCaCauHoiTheoTrinhDo, new Random(System.nanoTime()));

        List<CauHoi> cauHoiDaChonChoDeThi = new ArrayList<>();
        List<Integer> idCauHoiDaSuDung = new ArrayList<>(); // Để tránh trùng lặp câu hỏi giữa các phần

        for (CauTrucPhanDeChiTiet phan : cauTrucChiTietList) {
            if (phan.getSoLuong() <= 0) continue; // Bỏ qua phần nếu không yêu cầu câu hỏi

            List<CauHoi> cauHoiPhuHopChoPhan = new ArrayList<>();
            
            // Lọc câu hỏi cho từng phần từ danh sách đã xáo trộn
            for (CauHoi ch : tatCaCauHoiTheoTrinhDo) {
                if (idCauHoiDaSuDung.contains(ch.getMaCauHoi())) continue; // Bỏ qua nếu đã dùng

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
                // Bạn có thể quyết định throw Exception ở đây hoặc tiếp tục với số câu hỏi ít hơn
                 throw new Exception("Không đủ câu hỏi cho phần " + phan.getLoaiPhan() + ". Cần: " + phan.getSoLuong() + ", Có: " + cauHoiPhuHopChoPhan.size());
                // continue; // Hoặc lấy hết số câu có thể
            }

            // Lấy đủ số lượng câu hỏi cho phần này (vì đã shuffle ở đầu nên chỉ cần lấy từ đầu)
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
        
        // Kiểm tra tổng số câu hỏi đã chọn có đáp ứng không (nếu có yêu cầu tổng số)
        // int tongSoCauYeuCau = cauTrucChiTietList.stream().mapToInt(CauTrucPhanDeChiTiet::getSoLuong).sum();
        // if (cauHoiDaChonChoDeThi.size() < tongSoCauYeuCau) {
        //     System.err.println("Không thể tạo đủ số lượng câu hỏi theo yêu cầu cấu trúc.");
        //     // return null; // Hoặc throw exception
        // }


        // ---- Phần lưu DeThi và CauHoiTrongDeThi vào CSDL (giữ nguyên logic transaction) ----
        DeThi deThiMoi = new DeThi();
        deThiMoi.setTenDeThi(tenDeThi);
        deThiMoi.setMaTrinhDo(maTrinhDo);
        deThiMoi.setNgayTaoDe(new Timestamp(System.currentTimeMillis()));
        deThiMoi.setThoiGianLamBaiPhut(thoiGianLamBaiPhut);
        deThiMoi.setLaDeNgauNhien(true); // Đánh dấu là đề được tạo theo cấu trúc ngẫu nhiên

        Connection conn = null;
        try {
            conn = SQLServerConnector.getConnection();
            conn.setAutoCommit(false);

            boolean deThiSaved = deThiDAO.addDeThi(deThiMoi, conn); // Giả sử DeThiDAO đã hỗ trợ Connection
            if (deThiSaved && deThiMoi.getMaDeThi() > 0) {
                for (CauHoi ch : cauHoiDaChonChoDeThi) {
                    // Nếu muốn lưu thứ tự câu hỏi hoặc thuộc phần nào, bạn cần thêm cột vào CauHoiTrongDeThi
                    boolean added = cauHoiTrongDeThiDAO.addCauHoiVaoDeThi(deThiMoi.getMaDeThi(), ch.getMaCauHoi(), conn); // Giả sử DAO này đã hỗ trợ Connection
                    if (!added) {
                        // Cố gắng thêm lại hoặc xử lý lỗi (ví dụ câu hỏi đã tồn tại - UQ_DeThi_CauHoi)
                        // Do đã shuffle và chọn từ list đã lọc, lỗi duplicate ở đây ít khả năng
                        // trừ khi có lỗi logic chọn câu hỏi hoặc contraint DB khác.
                        System.err.println("Không thể thêm câu hỏi ID " + ch.getMaCauHoi() + " vào đề thi ID " + deThiMoi.getMaDeThi() + ". Có thể đã tồn tại hoặc lỗi khác.");
                        // throw new SQLException("Không thể thêm câu hỏi ID " + ch.getMaCauHoi() + " vào đề thi.");
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
    
    // Hàm tạo đề cũ (nếu bạn vẫn muốn giữ)
    public DeThi taoDeThiNgauNhien(String tenDeThi, int maTrinhDo, int soLuongCauHoi, Integer thoiGianLamBaiPhut) throws Exception {
         // Lấy tất cả câu hỏi thuộc trình độ yêu cầu
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
        // ... (set các thuộc tính cho deThiMoi như trên) ...
        deThiMoi.setTenDeThi(tenDeThi);
        deThiMoi.setMaTrinhDo(maTrinhDo);
        deThiMoi.setNgayTaoDe(new Timestamp(System.currentTimeMillis())); 
        deThiMoi.setThoiGianLamBaiPhut(thoiGianLamBaiPhut);
        deThiMoi.setLaDeNgauNhien(true);

        Connection conn = null;
        // ... (logic transaction và lưu như hàm taoDeThiNgauNhienTheoCauTrucChiTiet) ...
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
            // ... (xử lý rollback và exception)
            if (conn != null) try { conn.rollback(); } catch (SQLException exRollback) { exRollback.printStackTrace(); }
            throw new Exception("Lỗi khi tạo đề thi ngẫu nhiên (đơn giản): " + e.getMessage(), e);
        } finally {
            // ... (đóng connection)
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException exClose) { exClose.printStackTrace(); }
        }
    }
}