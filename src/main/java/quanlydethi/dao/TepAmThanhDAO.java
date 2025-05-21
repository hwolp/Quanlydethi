package quanlydethi.dao;

import quanlydethi.model.TepAmThanh;
import quanlydethi.dbconnector.SQLServerConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TepAmThanhDAO {

    public List<TepAmThanh> getAllTepAmThanh() {
        List<TepAmThanh> danhSach = new ArrayList<>();
        String sql = "SELECT MaAmThanh, TenTep, DuongDanTep, ThoiLuong, MaTrinhDo FROM dbo.TepAmThanh";

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TepAmThanh tat = new TepAmThanh();
                tat.setMaAmThanh(rs.getInt("MaAmThanh"));
                tat.setTenTep(rs.getString("TenTep"));
                tat.setDuongDanTep(rs.getString("DuongDanTep"));
                tat.setThoiLuong(rs.getTime("ThoiLuong")); // SQL TIME maps to java.sql.Time
                int maTrinhDo = rs.getInt("MaTrinhDo");
                if (rs.wasNull()) {
                    tat.setMaTrinhDo(null);
                } else {
                    tat.setMaTrinhDo(maTrinhDo);
                }
                danhSach.add(tat);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả tệp âm thanh: " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }

    public TepAmThanh getTepAmThanhById(int maAmThanh) {
        String sql = "SELECT MaAmThanh, TenTep, DuongDanTep, ThoiLuong, MaTrinhDo FROM dbo.TepAmThanh WHERE MaAmThanh = ?";
        TepAmThanh tat = null;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maAmThanh);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tat = new TepAmThanh();
                    tat.setMaAmThanh(rs.getInt("MaAmThanh"));
                    tat.setTenTep(rs.getString("TenTep"));
                    tat.setDuongDanTep(rs.getString("DuongDanTep"));
                    tat.setThoiLuong(rs.getTime("ThoiLuong"));
                    int maTrinhDo = rs.getInt("MaTrinhDo");
                    if (rs.wasNull()) {
                        tat.setMaTrinhDo(null);
                    } else {
                        tat.setMaTrinhDo(maTrinhDo);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tệp âm thanh theo ID " + maAmThanh + ": " + e.getMessage());
            e.printStackTrace();
        }
        return tat;
    }
    
    /**
     * Tìm TepAmThanh dựa trên đường dẫn tệp (DuongDanTep).
     * @param duongDanTep Đường dẫn của tệp âm thanh.
     * @return Đối tượng TepAmThanh nếu tìm thấy, null nếu không.
     */
    public TepAmThanh findByDuongDan(String duongDanTep) {
        String sql = "SELECT MaAmThanh, TenTep, DuongDanTep, ThoiLuong, MaTrinhDo FROM dbo.TepAmThanh WHERE DuongDanTep = ?";
        TepAmThanh tat = null;
        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, duongDanTep);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tat = new TepAmThanh();
                    tat.setMaAmThanh(rs.getInt("MaAmThanh"));
                    tat.setTenTep(rs.getString("TenTep"));
                    tat.setDuongDanTep(rs.getString("DuongDanTep"));
                    tat.setThoiLuong(rs.getTime("ThoiLuong"));
                    int maTrinhDo = rs.getInt("MaTrinhDo");
                    if (rs.wasNull()) {
                        tat.setMaTrinhDo(null);
                    } else {
                        tat.setMaTrinhDo(maTrinhDo);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm tệp âm thanh theo đường dẫn '" + duongDanTep + "': " + e.getMessage());
            e.printStackTrace();
        }
        return tat;
    }


    /**
     * Thêm một tệp âm thanh mới, tự quản lý kết nối và transaction.
     * @param tepAmThanh Đối tượng TepAmThanh cần thêm.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean addTepAmThanh(TepAmThanh tepAmThanh) {
        Connection conn = null;
        try {
            conn = SQLServerConnector.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction cục bộ
            boolean result = addTepAmThanh(tepAmThanh, conn); // Gọi phiên bản hỗ trợ transaction
            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thêm tệp âm thanh (tự quản lý kết nối): " + e.getMessage());
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
                    conn.setAutoCommit(true); // Luôn trả lại trạng thái auto-commit
                    conn.close();
                } catch (SQLException exClose) {
                    System.err.println("Lỗi khi đóng kết nối: " + exClose.getMessage());
                }
            }
        }
    }

    /**
     * Thêm một tệp âm thanh mới, sử dụng Connection được cung cấp (hỗ trợ transaction ngoài).
     * @param tepAmThanh Đối tượng TepAmThanh cần thêm.
     * @param conn Đối tượng Connection đã được quản lý từ bên ngoài.
     * @return true nếu thêm thành công và lấy được ID, false nếu thất bại.
     * @throws SQLException Nếu có lỗi SQL xảy ra.
     */
    public boolean addTepAmThanh(TepAmThanh tepAmThanh, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.TepAmThanh (TenTep, DuongDanTep, ThoiLuong, MaTrinhDo) VALUES (?, ?, ?, ?)";
        boolean rowInserted = false;
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, tepAmThanh.getTenTep());
            ps.setString(2, tepAmThanh.getDuongDanTep());
            ps.setTime(3, tepAmThanh.getThoiLuong()); // java.sql.Time
            if (tepAmThanh.getMaTrinhDo() != null) {
                ps.setInt(4, tepAmThanh.getMaTrinhDo());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            int affectedRows = ps.executeUpdate();
            rowInserted = affectedRows > 0;

            if (rowInserted) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    tepAmThanh.setMaAmThanh(generatedKeys.getInt(1)); // Cập nhật ID vào đối tượng
                } else {
                    throw new SQLException("Thêm tệp âm thanh thành công nhưng không lấy được ID tự tăng.");
                }
            }
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return rowInserted;
    }

    public boolean updateTepAmThanh(TepAmThanh tepAmThanh) {
        String sql = "UPDATE dbo.TepAmThanh SET TenTep = ?, DuongDanTep = ?, ThoiLuong = ?, MaTrinhDo = ? WHERE MaAmThanh = ?";
        boolean rowUpdated = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tepAmThanh.getTenTep());
            ps.setString(2, tepAmThanh.getDuongDanTep());
            ps.setTime(3, tepAmThanh.getThoiLuong());
            if (tepAmThanh.getMaTrinhDo() != null) {
                ps.setInt(4, tepAmThanh.getMaTrinhDo());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, tepAmThanh.getMaAmThanh());

            rowUpdated = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật tệp âm thanh ID " + tepAmThanh.getMaAmThanh() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return rowUpdated;
    }

    // (Tùy chọn) Phiên bản updateTepAmThanh hỗ trợ transaction ngoài
    public boolean updateTepAmThanh(TepAmThanh tepAmThanh, Connection conn) throws SQLException {
        String sql = "UPDATE dbo.TepAmThanh SET TenTep = ?, DuongDanTep = ?, ThoiLuong = ?, MaTrinhDo = ? WHERE MaAmThanh = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, tepAmThanh.getTenTep());
            ps.setString(2, tepAmThanh.getDuongDanTep());
            ps.setTime(3, tepAmThanh.getThoiLuong());
            if (tepAmThanh.getMaTrinhDo() != null) {
                ps.setInt(4, tepAmThanh.getMaTrinhDo());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, tepAmThanh.getMaAmThanh());
            return ps.executeUpdate() > 0;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean deleteTepAmThanh(int maAmThanh) {
        String sql = "DELETE FROM dbo.TepAmThanh WHERE MaAmThanh = ?";
        boolean rowDeleted = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maAmThanh);
            rowDeleted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
             if (e.getMessage().contains("REFERENCE constraint")) {
                System.err.println("Lỗi: Không thể xóa tệp âm thanh ID " + maAmThanh + " này vì đang được tham chiếu bởi câu hỏi.");
            } else {
                System.err.println("Lỗi SQL khi xóa tệp âm thanh ID " + maAmThanh + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return rowDeleted;
    }

    // (Tùy chọn) Phiên bản deleteTepAmThanh hỗ trợ transaction ngoài
    public boolean deleteTepAmThanh(int maAmThanh, Connection conn) throws SQLException {
        String sql = "DELETE FROM dbo.TepAmThanh WHERE MaAmThanh = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, maAmThanh);
            return ps.executeUpdate() > 0;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
