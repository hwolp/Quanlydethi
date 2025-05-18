package quanlydethi.dao;

import quanlydethi.model.DeThi;
import quanlydethi.dbconnector.SQLServerConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeThiDAO {

    // Phương thức này giữ nguyên, dùng khi không cần transaction ngoài
    public List<DeThi> getAllDeThi() {
        List<DeThi> danhSach = new ArrayList<>();
        String sql = "SELECT MaDeThi, TenDeThi, MaTrinhDo, NgayTaoDe, ThoiGianLamBaiPhut, LaDeNgauNhien FROM dbo.DeThi";

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DeThi dt = mapResultSetToDeThi(rs);
                danhSach.add(dt);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả đề thi: " + e.getMessage());
            e.printStackTrace(); // Trong ứng dụng thực tế, nên throw exception hoặc log chi tiết hơn
        }
        return danhSach;
    }

    // Phương thức này giữ nguyên
    public DeThi getDeThiById(int maDeThi) {
        String sql = "SELECT MaDeThi, TenDeThi, MaTrinhDo, NgayTaoDe, ThoiGianLamBaiPhut, LaDeNgauNhien FROM dbo.DeThi WHERE MaDeThi = ?";
        DeThi dt = null;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maDeThi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dt = mapResultSetToDeThi(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy đề thi theo ID " + maDeThi + ": " + e.getMessage());
            e.printStackTrace();
        }
        return dt;
    }

    /**
     * Thêm một đề thi mới vào CSDL, tự quản lý kết nối.
     * @param deThi Đối tượng DeThi cần thêm.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean addDeThi(DeThi deThi) {
        Connection conn = null;
        try {
            conn = SQLServerConnector.getConnection();
            // Gọi phiên bản addDeThi sử dụng Connection được truyền vào
            // và tự quản lý commit/rollback cho thao tác đơn lẻ này.
            conn.setAutoCommit(false); // Bắt đầu transaction cục bộ
            boolean result = addDeThi(deThi, conn);
            if (result) {
                conn.commit(); // Commit nếu thành công
            } else {
                conn.rollback(); // Rollback nếu thất bại
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thêm đề thi (tự quản lý kết nối): " + e.getMessage());
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
     * Thêm một đề thi mới vào CSDL, sử dụng Connection được cung cấp (hỗ trợ transaction ngoài).
     * @param deThi Đối tượng DeThi cần thêm.
     * @param conn Đối tượng Connection đã được quản lý từ bên ngoài.
     * @return true nếu thêm thành công, false nếu thất bại.
     * @throws SQLException Nếu có lỗi SQL xảy ra.
     */
    public boolean addDeThi(DeThi deThi, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.DeThi (TenDeThi, MaTrinhDo, NgayTaoDe, ThoiGianLamBaiPhut, LaDeNgauNhien) VALUES (?, ?, ?, ?, ?)";
        boolean rowInserted = false;

        // Không dùng try-with-resources cho PreparedStatement nếu Connection được quản lý bên ngoài
        // và chúng ta muốn ném SQLException ra ngoài để service xử lý transaction.
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setPreparedStatementParameters(ps, deThi);
            int affectedRows = ps.executeUpdate();
            rowInserted = affectedRows > 0;

            if (rowInserted) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    deThi.setMaDeThi(generatedKeys.getInt(1)); // Quan trọng: cập nhật ID cho đối tượng deThi
                } else {
                     // Điều này không nên xảy ra nếu Statement.RETURN_GENERATED_KEYS hoạt động đúng
                     // và bảng có cột IDENTITY.
                    throw new SQLException("Thêm đề thi thành công nhưng không lấy được ID tự tăng.");
                }
            }
        } finally {
            // Đóng ResultSet và PreparedStatement, Connection sẽ được đóng bởi lớp gọi.
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return rowInserted;
    }


    // Phương thức này giữ nguyên, tự quản lý kết nối
    public boolean updateDeThi(DeThi deThi) {
        String sql = "UPDATE dbo.DeThi SET TenDeThi = ?, MaTrinhDo = ?, NgayTaoDe = ?, ThoiGianLamBaiPhut = ?, LaDeNgauNhien = ? WHERE MaDeThi = ?";
        boolean rowUpdated = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setPreparedStatementParameters(ps, deThi);
            ps.setInt(6, deThi.getMaDeThi()); // For WHERE clause
            rowUpdated = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật đề thi ID " + deThi.getMaDeThi() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return rowUpdated;
    }

    // (Tùy chọn) Phiên bản updateDeThi hỗ trợ transaction ngoài
    public boolean updateDeThi(DeThi deThi, Connection conn) throws SQLException {
        String sql = "UPDATE dbo.DeThi SET TenDeThi = ?, MaTrinhDo = ?, NgayTaoDe = ?, ThoiGianLamBaiPhut = ?, LaDeNgauNhien = ? WHERE MaDeThi = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            setPreparedStatementParameters(ps, deThi);
            ps.setInt(6, deThi.getMaDeThi());
            return ps.executeUpdate() > 0;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    // Phương thức này giữ nguyên, tự quản lý kết nối
    public boolean deleteDeThi(int maDeThi) {
        String sql = "DELETE FROM dbo.DeThi WHERE MaDeThi = ?";
        boolean rowDeleted = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maDeThi);
            rowDeleted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("REFERENCE constraint")) {
                System.err.println("Lỗi: Không thể xóa đề thi ID " + maDeThi + " vì đang được tham chiếu.");
            } else {
                System.err.println("Lỗi khi xóa đề thi ID " + maDeThi + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return rowDeleted;
    }

    // (Tùy chọn) Phiên bản deleteDeThi hỗ trợ transaction ngoài
    public boolean deleteDeThi(int maDeThi, Connection conn) throws SQLException {
        String sql = "DELETE FROM dbo.DeThi WHERE MaDeThi = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, maDeThi);
            return ps.executeUpdate() > 0;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Helper method: Giữ nguyên
    private DeThi mapResultSetToDeThi(ResultSet rs) throws SQLException {
        DeThi dt = new DeThi();
        dt.setMaDeThi(rs.getInt("MaDeThi"));
        dt.setTenDeThi(rs.getString("TenDeThi"));
        
        int maTrinhDo = rs.getInt("MaTrinhDo");
        dt.setMaTrinhDo(rs.wasNull() ? null : maTrinhDo); // Correctly handle NULL for MaTrinhDo
        
        dt.setNgayTaoDe(rs.getTimestamp("NgayTaoDe"));
        
        int thoiGian = rs.getInt("ThoiGianLamBaiPhut");
        dt.setThoiGianLamBaiPhut(rs.wasNull() ? null : thoiGian); // Correctly handle NULL
        
        dt.setLaDeNgauNhien(rs.getBoolean("LaDeNgauNhien"));
        return dt;
    }

    // Helper method: Giữ nguyên
    private void setPreparedStatementParameters(PreparedStatement ps, DeThi deThi) throws SQLException {
        ps.setString(1, deThi.getTenDeThi());
        if (deThi.getMaTrinhDo() != null) {
            ps.setInt(2, deThi.getMaTrinhDo());
        } else {
            ps.setNull(2, Types.INTEGER);
        }
        // NgayTaoDe thường được set DEFAULT GETDATE() trong DB,
        // nếu deThi.getNgayTaoDe() là null, bạn có thể không set nó để DB tự xử lý,
        // hoặc set giá trị hiện tại như đang làm.
        if (deThi.getNgayTaoDe() != null) {
            ps.setTimestamp(3, deThi.getNgayTaoDe());
        } else {
            // Nếu không có ngày tạo và cột này NOT NULL và không có default trong DB (mặc dù DDL có default)
            // thì việc set new Timestamp là hợp lý, hoặc để DB tự xử lý nếu cột có DEFAULT.
            // Trong DDL của bạn, NgayTaoDe DATETIME2 DEFAULT GETDATE() NOT NULL
            // Vậy nếu deThi.getNgayTaoDe() là null thì không cần set, DB sẽ tự lấy GETDATE().
            // Tuy nhiên, để an toàn và rõ ràng, nếu bạn muốn giá trị từ Java, thì set.
            // Nếu muốn DB tự quyết, thì phải bỏ qua việc set cột này khi giá trị là null.
            // Để giữ logic cũ của bạn:
             ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            // Hoặc nếu bạn muốn chỉ set khi có giá trị và để DB tự xử lý khi null (cần điều chỉnh SQL INSERT):
            // if (deThi.getNgayTaoDe() != null) ps.setTimestamp(3, deThi.getNgayTaoDe()); else ps.setNull(3, Types.TIMESTAMP_WITH_TIMEZONE); // Hoặc bỏ qua
        }
        
        if (deThi.getThoiGianLamBaiPhut() != null) {
            ps.setInt(4, deThi.getThoiGianLamBaiPhut());
        } else {
            ps.setNull(4, Types.INTEGER);
        }
        ps.setBoolean(5, deThi.isLaDeNgauNhien());
    }
}