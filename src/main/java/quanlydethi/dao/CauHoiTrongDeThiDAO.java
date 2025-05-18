package quanlydethi.dao;

import quanlydethi.model.CauHoiTrongDeThi; // Đảm bảo model này đã được tạo
import quanlydethi.dbconnector.SQLServerConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CauHoiTrongDeThiDAO {

    /**
     * Thêm một câu hỏi vào một đề thi, tự quản lý kết nối.
     * @param maDeThi Mã đề thi.
     * @param maCauHoi Mã câu hỏi.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean addCauHoiVaoDeThi(int maDeThi, int maCauHoi) {
        Connection conn = null;
        try {
            conn = SQLServerConnector.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction cục bộ
            
            // Gọi phiên bản addCauHoiVaoDeThi sử dụng Connection được truyền vào
            boolean result = addCauHoiVaoDeThi(maDeThi, maCauHoi, conn);
            
            if (result) {
                conn.commit(); // Commit nếu thành công
            } else {
                conn.rollback(); // Rollback nếu thất bại (ví dụ: do duplicate key đã được xử lý bên trong)
            }
            return result;
        } catch (SQLException e) {
            // Xử lý lỗi duplicate key đã được thực hiện trong phiên bản private
            // Các lỗi SQL khác sẽ được bắt ở đây
            System.err.println("Lỗi SQL khi thêm câu hỏi vào đề thi (tự quản lý kết nối): " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException exRollback) {
                    System.err.println("Lỗi khi rollback: " + exRollback.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Trả lại trạng thái auto-commit
                    conn.close();
                } catch (SQLException exClose) {
                    System.err.println("Lỗi khi đóng kết nối: " + exClose.getMessage());
                }
            }
        }
    }

    /**
     * Thêm một câu hỏi vào một đề thi, sử dụng Connection được cung cấp (hỗ trợ transaction ngoài).
     * @param maDeThi Mã đề thi.
     * @param maCauHoi Mã câu hỏi.
     * @param conn Đối tượng Connection đã được quản lý từ bên ngoài.
     * @return true nếu thêm thành công, false nếu thất bại (ví dụ: do duplicate key).
     * @throws SQLException Nếu có lỗi SQL nghiêm trọng xảy ra (ngoài lỗi duplicate key đã xử lý).
     */
    public boolean addCauHoiVaoDeThi(int maDeThi, int maCauHoi, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.CauHoiTrongDeThi (MaDeThi, MaCauHoi) VALUES (?, ?)";
        boolean rowInserted = false;
        // Đối tượng CauHoiTrongDeThi cục bộ để lấy ID tự tăng nếu cần,
        // nhưng phương thức này chỉ trả về boolean.
        // CauHoiTrongDeThi chtdt = new CauHoiTrongDeThi(); 
        // chtdt.setMaDeThi(maDeThi);
        // chtdt.setMaCauHoi(maCauHoi);

        PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, maDeThi);
            ps.setInt(2, maCauHoi);

            int affectedRows = ps.executeUpdate();
            rowInserted = affectedRows > 0;

            if (rowInserted) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    // int generatedId = generatedKeys.getInt(1);
                    // chtdt.setMaCauHoiDeThi(generatedId); // Gán ID nếu cần dùng
                    // System.out.println("Đã thêm câu hỏi vào đề thi với ID liên kết: " + generatedId);
                } else {
                    // Điều này có thể xảy ra nếu bảng không có cột IDENTITY hoặc có vấn đề với driver JDBC
                    // Tuy nhiên, với SQL Server và Statement.RETURN_GENERATED_KEYS, nó thường hoạt động.
                    System.err.println("Cảnh báo: Thêm liên kết câu hỏi-đề thi thành công nhưng không lấy được ID tự tăng.");
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UQ_DeThi_CauHoi") || 
                e.getMessage().toLowerCase().contains("violation of unique key constraint") ||
                e.getMessage().toLowerCase().contains("duplicate key")) {
                System.err.println("Lỗi: Câu hỏi ID " + maCauHoi + " đã tồn tại trong đề thi ID " + maDeThi + ".");
                return false; // Coi như thêm không thành công vì đã tồn tại
            } else {
                // Ném các lỗi SQL khác ra ngoài để transaction bên ngoài xử lý
                throw e;
            }
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            // Connection sẽ được đóng bởi lớp gọi (Service)
        }
        return rowInserted;
    }
    
    // Xóa một câu hỏi cụ thể khỏi một đề thi cụ thể (tự quản lý kết nối)
    public boolean removeCauHoiKhoiDeThi(int maDeThi, int maCauHoi) {
        Connection conn = null;
        try {
            conn = SQLServerConnector.getConnection();
            conn.setAutoCommit(false);
            boolean result = removeCauHoiKhoiDeThi(maDeThi, maCauHoi, conn);
            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi xóa câu hỏi khỏi đề thi: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // Xóa một câu hỏi cụ thể khỏi một đề thi cụ thể (hỗ trợ transaction ngoài)
    public boolean removeCauHoiKhoiDeThi(int maDeThi, int maCauHoi, Connection conn) throws SQLException {
        String sql = "DELETE FROM dbo.CauHoiTrongDeThi WHERE MaDeThi = ? AND MaCauHoi = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, maDeThi);
            ps.setInt(2, maCauHoi);
            return ps.executeUpdate() > 0;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Các phương thức đọc dữ liệu có thể giữ nguyên cách tự quản lý kết nối
    // vì chúng thường không tham gia vào transaction ghi dữ liệu.

    public List<Integer> getMaCauHoiByMaDeThi(int maDeThi) {
        List<Integer> danhSachMaCauHoi = new ArrayList<>();
        String sql = "SELECT MaCauHoi FROM dbo.CauHoiTrongDeThi WHERE MaDeThi = ?";

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maDeThi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSachMaCauHoi.add(rs.getInt("MaCauHoi"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy mã câu hỏi theo mã đề thi " + maDeThi + ": " + e.getMessage());
            e.printStackTrace();
        }
        return danhSachMaCauHoi;
    }
    
    public List<CauHoiTrongDeThi> getCauHoiTrongDeThiByMaDeThi(int maDeThi) {
        List<CauHoiTrongDeThi> danhSach = new ArrayList<>();
        String sql = "SELECT MaCauHoiDeThi, MaDeThi, MaCauHoi FROM dbo.CauHoiTrongDeThi WHERE MaDeThi = ?";

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, maDeThi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CauHoiTrongDeThi chtdt = new CauHoiTrongDeThi();
                    chtdt.setMaCauHoiDeThi(rs.getInt("MaCauHoiDeThi"));
                    chtdt.setMaDeThi(rs.getInt("MaDeThi"));
                    chtdt.setMaCauHoi(rs.getInt("MaCauHoi"));
                    danhSach.add(chtdt);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy CauHoiTrongDeThi theo mã đề thi " + maDeThi + ": " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }

    public boolean isCauHoiInDeThi(int maDeThi, int maCauHoi) {
        String sql = "SELECT COUNT(*) FROM dbo.CauHoiTrongDeThi WHERE MaDeThi = ? AND MaCauHoi = ?";
        boolean exists = false;
        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDeThi);
            ps.setInt(2, maCauHoi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    exists = rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra câu hỏi trong đề thi: " + e.getMessage());
            e.printStackTrace();
        }
        return exists;
    }
}