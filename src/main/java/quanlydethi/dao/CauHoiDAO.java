package quanlydethi.dao;

import quanlydethi.model.CauHoi;
import quanlydethi.dbconnector.SQLServerConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CauHoiDAO {

    public List<CauHoi> getAllCauHoi() {
        List<CauHoi> danhSach = new ArrayList<>();
        String sql = "SELECT MaCauHoi, NoiDungCauHoi, MaLoaiCauHoi, MaTrinhDo, MaDoanVan, MaAmThanh, Diem FROM dbo.CauHoi";

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CauHoi ch = mapResultSetToCauHoi(rs);
                danhSach.add(ch);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả câu hỏi: " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }

    public CauHoi getCauHoiById(int maCauHoi) {
        String sql = "SELECT MaCauHoi, NoiDungCauHoi, MaLoaiCauHoi, MaTrinhDo, MaDoanVan, MaAmThanh, Diem FROM dbo.CauHoi WHERE MaCauHoi = ?";
        CauHoi ch = null;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maCauHoi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ch = mapResultSetToCauHoi(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy câu hỏi theo ID " + maCauHoi + ": " + e.getMessage());
            e.printStackTrace();
        }
        return ch;
    }

    /**
     * Lấy danh sách các câu hỏi dựa vào Mã Trình Độ.
     * @param maTrinhDo Mã trình độ cần lọc.
     * @return Danh sách các câu hỏi thuộc trình độ đó.
     */
    public List<CauHoi> getCauHoiByTrinhDo(int maTrinhDo) {
        List<CauHoi> danhSach = new ArrayList<>();
        String sql = "SELECT MaCauHoi, NoiDungCauHoi, MaLoaiCauHoi, MaTrinhDo, MaDoanVan, MaAmThanh, Diem FROM dbo.CauHoi WHERE MaTrinhDo = ?";

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maTrinhDo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CauHoi ch = mapResultSetToCauHoi(rs);
                    danhSach.add(ch);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy câu hỏi theo Mã Trình Độ " + maTrinhDo + ": " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }


    /**
     * Thêm một câu hỏi mới vào CSDL, tự quản lý kết nối.
     * @param cauHoi Đối tượng CauHoi cần thêm.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean addCauHoi(CauHoi cauHoi) {
        // Để nhất quán với DeThiDAO và CauHoiTrongDeThiDAO đã sửa,
        // chúng ta cũng nên có một phiên bản addCauHoi(CauHoi cauHoi, Connection conn)
        // và gọi nó từ đây với transaction cục bộ.
        Connection conn = null;
        try {
            conn = SQLServerConnector.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction cục bộ
            boolean result = addCauHoi(cauHoi, conn); // Gọi phiên bản hỗ trợ transaction
            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thêm câu hỏi (tự quản lý kết nối): " + e.getMessage());
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
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException exClose) {
                    System.err.println("Lỗi khi đóng kết nối: " + exClose.getMessage());
                }
            }
        }
    }

    /**
     * Thêm một câu hỏi mới vào CSDL, sử dụng Connection được cung cấp (hỗ trợ transaction ngoài).
     * @param cauHoi Đối tượng CauHoi cần thêm.
     * @param conn Đối tượng Connection đã được quản lý từ bên ngoài.
     * @return true nếu thêm thành công, false nếu thất bại.
     * @throws SQLException Nếu có lỗi SQL xảy ra.
     */
    public boolean addCauHoi(CauHoi cauHoi, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.CauHoi (NoiDungCauHoi, MaLoaiCauHoi, MaTrinhDo, MaDoanVan, MaAmThanh, Diem) VALUES (?, ?, ?, ?, ?, ?)";
        boolean rowInserted = false;
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setPreparedStatementParameters(ps, cauHoi);
            int affectedRows = ps.executeUpdate();
            rowInserted = affectedRows > 0;

            if (rowInserted) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    cauHoi.setMaCauHoi(generatedKeys.getInt(1)); // Cập nhật ID cho đối tượng
                } else {
                    throw new SQLException("Thêm câu hỏi thành công nhưng không lấy được ID tự tăng.");
                }
            }
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return rowInserted;
    }


    public boolean updateCauHoi(CauHoi cauHoi) {
        String sql = "UPDATE dbo.CauHoi SET NoiDungCauHoi = ?, MaLoaiCauHoi = ?, MaTrinhDo = ?, MaDoanVan = ?, MaAmThanh = ?, Diem = ? WHERE MaCauHoi = ?";
        boolean rowUpdated = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setPreparedStatementParameters(ps, cauHoi);
            ps.setInt(7, cauHoi.getMaCauHoi()); // For WHERE clause
            rowUpdated = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật câu hỏi ID " + cauHoi.getMaCauHoi() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return rowUpdated;
    }

    // (Tùy chọn) Bạn cũng có thể thêm phiên bản updateCauHoi(CauHoi cauHoi, Connection conn) nếu cần

    public boolean deleteCauHoi(int maCauHoi) {
        // Lưu ý: Việc gọi LuaChonDAO từ CauHoiDAO tạo ra sự phụ thuộc giữa các DAO.
        // Trong một kiến trúc chặt chẽ hơn, logic xóa phụ thuộc này nên được xử lý ở tầng Service
        // và cả hai DAO sẽ nhận Connection từ Service để thực hiện trong cùng một transaction.
        // Tuy nhiên, nếu ON DELETE CASCADE đã được thiết lập đúng trong CSDL cho khóa ngoại
        // từ LuaChon đến CauHoi, thì việc xóa LuaChon thủ công ở đây có thể không cần thiết
        // (hoặc thậm chí gây lỗi nếu CSDL tự xử lý).
        // Giả sử bạn muốn giữ logic xóa lựa chọn thủ công:
        Connection conn = null;
        try {
            conn = SQLServerConnector.getConnection();
            conn.setAutoCommit(false);

            // Xóa các lựa chọn liên quan sử dụng cùng connection
            LuaChonDAO luaChonDAO = new LuaChonDAO();
            // Cần sửa LuaChonDAO.deleteLuaChonByMaCauHoi để nhận Connection
            // Hoặc, nếu không sửa được LuaChonDAO ngay, thì thao tác này sẽ chạy trên connection riêng của nó.
            // Để đúng transaction, LuaChonDAO.deleteLuaChonByMaCauHoi(maCauHoi, conn) là cần thiết.
            // Tạm thời giả sử LuaChonDAO tự quản lý connection của nó cho việc xóa này:
             boolean luaChonDeleted = luaChonDAO.deleteLuaChonByMaCauHoi(maCauHoi);
            // Nếu bạn sửa LuaChonDAO, sẽ là:
            // boolean luaChonDeleted = luaChonDAO.deleteLuaChonByMaCauHoi(maCauHoi, conn);


            // Chỉ xóa câu hỏi nếu các lựa chọn đã được xóa thành công (nếu không có ON DELETE CASCADE)
            // Hoặc nếu bạn chắc chắn về ON DELETE CASCADE, bạn có thể không cần kiểm tra luaChonDeleted.
            // if (luaChonDeleted) { // Hoặc bỏ qua điều kiện này nếu tin vào ON DELETE CASCADE
                String sql = "DELETE FROM dbo.CauHoi WHERE MaCauHoi = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, maCauHoi);
                    if (ps.executeUpdate() > 0) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback(); // Câu hỏi không được xóa
                        return false;
                    }
                }
            // } else {
            //     System.err.println("Không thể xóa các lựa chọn của câu hỏi ID " + maCauHoi + ", hủy xóa câu hỏi.");
            //     conn.rollback();
            //     return false;
            // }

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            if (e.getMessage().contains("REFERENCE constraint")) {
                System.err.println("Lỗi: Không thể xóa câu hỏi ID " + maCauHoi + " vì đang được tham chiếu (ví dụ trong CauHoiTrongDeThi).");
            } else {
                System.err.println("Lỗi SQL khi xóa câu hỏi ID " + maCauHoi + ": " + e.getMessage());
                e.printStackTrace();
            }
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
    
    // Helper method to map ResultSet to CauHoi object
    private CauHoi mapResultSetToCauHoi(ResultSet rs) throws SQLException {
        CauHoi ch = new CauHoi();
        ch.setMaCauHoi(rs.getInt("MaCauHoi"));
        ch.setNoiDungCauHoi(rs.getString("NoiDungCauHoi"));
        ch.setMaLoaiCauHoi(rs.getInt("MaLoaiCauHoi"));
        ch.setMaTrinhDo(rs.getInt("MaTrinhDo"));
        
        int maDoanVan = rs.getInt("MaDoanVan");
        ch.setMaDoanVan(rs.wasNull() ? null : maDoanVan);
        
        int maAmThanh = rs.getInt("MaAmThanh");
        ch.setMaAmThanh(rs.wasNull() ? null : maAmThanh);
        
        ch.setDiem(rs.getInt("Diem"));
        return ch;
    }

    // Helper method to set PreparedStatement parameters from CauHoi object
    private void setPreparedStatementParameters(PreparedStatement ps, CauHoi cauHoi) throws SQLException {
        ps.setString(1, cauHoi.getNoiDungCauHoi());
        ps.setInt(2, cauHoi.getMaLoaiCauHoi());
        ps.setInt(3, cauHoi.getMaTrinhDo());

        if (cauHoi.getMaDoanVan() != null) {
            ps.setInt(4, cauHoi.getMaDoanVan());
        } else {
            ps.setNull(4, Types.INTEGER);
        }

        if (cauHoi.getMaAmThanh() != null) {
            ps.setInt(5, cauHoi.getMaAmThanh());
        } else {
            ps.setNull(5, Types.INTEGER);
        }
        ps.setInt(6, cauHoi.getDiem());
    }
}