package quanlydethi.dao;

import quanlydethi.model.LuaChon;
import quanlydethi.dbconnector.SQLServerConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LuaChonDAO {

    public List<LuaChon> getAllLuaChon() {
        List<LuaChon> danhSach = new ArrayList<>();
        String sql = "SELECT MaLuaChon, MaCauHoi, NoiDungLuaChon, LaDapAnDung FROM dbo.LuaChon";

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LuaChon lc = mapResultSetToLuaChon(rs);
                danhSach.add(lc);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả lựa chọn: " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }

    public List<LuaChon> getLuaChonByMaCauHoi(int maCauHoi) {
        List<LuaChon> danhSach = new ArrayList<>();
        String sql = "SELECT MaLuaChon, MaCauHoi, NoiDungLuaChon, LaDapAnDung FROM dbo.LuaChon WHERE MaCauHoi = ?";

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, maCauHoi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LuaChon lc = mapResultSetToLuaChon(rs);
                    danhSach.add(lc);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lựa chọn theo Mã Câu Hỏi " + maCauHoi + ": " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }
    
    public LuaChon getLuaChonById(int maLuaChon) {
        String sql = "SELECT MaLuaChon, MaCauHoi, NoiDungLuaChon, LaDapAnDung FROM dbo.LuaChon WHERE MaLuaChon = ?";
        LuaChon lc = null;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maLuaChon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lc = mapResultSetToLuaChon(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lựa chọn theo ID " + maLuaChon + ": " + e.getMessage());
            e.printStackTrace();
        }
        return lc;
    }

    /**
     * Thêm một lựa chọn mới, tự quản lý kết nối và transaction.
     * @param luaChon Đối tượng LuaChon cần thêm.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean addLuaChon(LuaChon luaChon) {
        Connection conn = null;
        try {
            conn = SQLServerConnector.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction cục bộ
            boolean result = addLuaChon(luaChon, conn); // Gọi phiên bản hỗ trợ transaction
            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thêm lựa chọn (tự quản lý kết nối): " + e.getMessage());
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
     * Thêm một lựa chọn mới, sử dụng Connection được cung cấp (hỗ trợ transaction ngoài).
     * @param luaChon Đối tượng LuaChon cần thêm.
     * @param conn Đối tượng Connection đã được quản lý từ bên ngoài.
     * @return true nếu thêm thành công và lấy được ID, false nếu thất bại.
     * @throws SQLException Nếu có lỗi SQL xảy ra.
     */
    public boolean addLuaChon(LuaChon luaChon, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.LuaChon (MaCauHoi, NoiDungLuaChon, LaDapAnDung) VALUES (?, ?, ?)";
        boolean rowInserted = false;
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, luaChon.getMaCauHoi());
            ps.setString(2, luaChon.getNoiDungLuaChon());
            ps.setBoolean(3, luaChon.isLaDapAnDung());

            int affectedRows = ps.executeUpdate();
            rowInserted = affectedRows > 0;

            if (rowInserted) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    luaChon.setMaLuaChon(generatedKeys.getInt(1)); // Cập nhật ID vào đối tượng
                } else {
                    throw new SQLException("Thêm lựa chọn thành công nhưng không lấy được ID tự tăng.");
                }
            }
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            // Connection sẽ được đóng bởi lớp gọi (Service)
        }
        return rowInserted;
    }

    public boolean updateLuaChon(LuaChon luaChon) {
        // Tương tự, bạn có thể tạo phiên bản hỗ trợ transaction nếu cần
        String sql = "UPDATE dbo.LuaChon SET MaCauHoi = ?, NoiDungLuaChon = ?, LaDapAnDung = ? WHERE MaLuaChon = ?";
        boolean rowUpdated = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, luaChon.getMaCauHoi());
            ps.setString(2, luaChon.getNoiDungLuaChon());
            ps.setBoolean(3, luaChon.isLaDapAnDung());
            ps.setInt(4, luaChon.getMaLuaChon());

            rowUpdated = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật lựa chọn ID " + luaChon.getMaLuaChon() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return rowUpdated;
    }

    public boolean deleteLuaChon(int maLuaChon) {
        // Tương tự, bạn có thể tạo phiên bản hỗ trợ transaction nếu cần
        String sql = "DELETE FROM dbo.LuaChon WHERE MaLuaChon = ?";
        boolean rowDeleted = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maLuaChon);
            rowDeleted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Không cần kiểm tra REFERENCE constraint ở đây vì LuaChon thường không được tham chiếu bởi bảng khác
            System.err.println("Lỗi SQL khi xóa lựa chọn ID " + maLuaChon + ": " + e.getMessage());
            e.printStackTrace();
        }
        return rowDeleted;
    }

    /**
     * Xóa tất cả các lựa chọn của một câu hỏi cụ thể.
     * Phương thức này hữu ích khi xóa hoặc cập nhật câu hỏi.
     * Phiên bản này tự quản lý kết nối.
     * @param maCauHoi Mã câu hỏi.
     * @return true nếu xóa thành công (hoặc không có gì để xóa), false nếu có lỗi.
     */
    public boolean deleteLuaChonByMaCauHoi(int maCauHoi) {
        Connection conn = null;
        try {
            conn = SQLServerConnector.getConnection();
            conn.setAutoCommit(false);
            boolean result = deleteLuaChonByMaCauHoi(maCauHoi, conn);
            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi xóa lựa chọn theo Mã Câu Hỏi " + maCauHoi + ": " + e.getMessage());
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
    
    /**
     * Xóa tất cả các lựa chọn của một câu hỏi cụ thể, sử dụng Connection được cung cấp.
     * @param maCauHoi Mã câu hỏi.
     * @param conn Đối tượng Connection.
     * @return true nếu xóa thành công hoặc không có gì để xóa.
     * @throws SQLException Nếu có lỗi SQL.
     */
    public boolean deleteLuaChonByMaCauHoi(int maCauHoi, Connection conn) throws SQLException {
        String sql = "DELETE FROM dbo.LuaChon WHERE MaCauHoi = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, maCauHoi);
            ps.executeUpdate(); // Không cần kiểm tra số hàng bị ảnh hưởng, vì không có gì để xóa cũng là thành công
            return true;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private LuaChon mapResultSetToLuaChon(ResultSet rs) throws SQLException {
        LuaChon lc = new LuaChon();
        lc.setMaLuaChon(rs.getInt("MaLuaChon"));
        lc.setMaCauHoi(rs.getInt("MaCauHoi"));
        lc.setNoiDungLuaChon(rs.getString("NoiDungLuaChon"));
        lc.setLaDapAnDung(rs.getBoolean("LaDapAnDung"));
        return lc;
    }
}
