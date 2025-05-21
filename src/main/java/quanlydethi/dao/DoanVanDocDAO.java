package quanlydethi.dao;

import quanlydethi.model.DoanVanDoc;
import quanlydethi.dbconnector.SQLServerConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoanVanDocDAO {

    public List<DoanVanDoc> getAllDoanVanDoc() {
        List<DoanVanDoc> danhSach = new ArrayList<>();
        String sql = "SELECT MaDoanVan, NoiDungDoanVan, MaTrinhDo FROM dbo.DoanVanDoc";

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DoanVanDoc dvd = new DoanVanDoc();
                dvd.setMaDoanVan(rs.getInt("MaDoanVan"));
                dvd.setNoiDungDoanVan(rs.getString("NoiDungDoanVan"));
                // Xử lý MaTrinhDo có thể NULL
                int maTrinhDo = rs.getInt("MaTrinhDo");
                if (rs.wasNull()) {
                    dvd.setMaTrinhDo(null);
                } else {
                    dvd.setMaTrinhDo(maTrinhDo);
                }
                danhSach.add(dvd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSach;
    }

    public DoanVanDoc getDoanVanDocById(int maDoanVan) {
        String sql = "SELECT MaDoanVan, NoiDungDoanVan, MaTrinhDo FROM dbo.DoanVanDoc WHERE MaDoanVan = ?";
        DoanVanDoc dvd = null;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maDoanVan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dvd = new DoanVanDoc();
                    dvd.setMaDoanVan(rs.getInt("MaDoanVan"));
                    dvd.setNoiDungDoanVan(rs.getString("NoiDungDoanVan"));
                    int maTrinhDo = rs.getInt("MaTrinhDo");
                    if (rs.wasNull()) {
                        dvd.setMaTrinhDo(null);
                    } else {
                        dvd.setMaTrinhDo(maTrinhDo);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dvd;
    }

    public boolean addDoanVanDoc(DoanVanDoc doanVanDoc) {
        String sql = "INSERT INTO dbo.DoanVanDoc (NoiDungDoanVan, MaTrinhDo) VALUES (?, ?)";
        boolean rowInserted = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, doanVanDoc.getNoiDungDoanVan());
            if (doanVanDoc.getMaTrinhDo() != null) {
                ps.setInt(2, doanVanDoc.getMaTrinhDo());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            rowInserted = ps.executeUpdate() > 0;

            if (rowInserted) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        doanVanDoc.setMaDoanVan(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowInserted;
    }
    public boolean addDoanVanDoc(DoanVanDoc doanVanDoc, Connection conn) throws SQLException {
        String sql = "INSERT INTO dbo.DoanVanDoc (NoiDungDoanVan, MaTrinhDo) VALUES (?, ?)";
        boolean rowInserted = false;
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, doanVanDoc.getNoiDungDoanVan());
            if (doanVanDoc.getMaTrinhDo() != null) {
                ps.setInt(2, doanVanDoc.getMaTrinhDo());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            int affectedRows = ps.executeUpdate();
            rowInserted = affectedRows > 0;

            if (rowInserted) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    doanVanDoc.setMaDoanVan(generatedKeys.getInt(1)); // Cập nhật ID vào đối tượng
                } else {
                    // Nếu không có key được trả về dù insert thành công, có thể là vấn đề cấu hình DB hoặc driver
                    throw new SQLException("Thêm đoạn văn thành công nhưng không lấy được ID tự tăng.");
                }
            }
        } finally {
            // Đóng ResultSet và PreparedStatement, Connection sẽ được đóng bởi lớp gọi (Service).
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return rowInserted;
    }


    public boolean updateDoanVanDoc(DoanVanDoc doanVanDoc) {
        String sql = "UPDATE dbo.DoanVanDoc SET NoiDungDoanVan = ?, MaTrinhDo = ? WHERE MaDoanVan = ?";
        boolean rowUpdated = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doanVanDoc.getNoiDungDoanVan());
            if (doanVanDoc.getMaTrinhDo() != null) {
                ps.setInt(2, doanVanDoc.getMaTrinhDo());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, doanVanDoc.getMaDoanVan());

            rowUpdated = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowUpdated;
    }

    public boolean deleteDoanVanDoc(int maDoanVan) {
        String sql = "DELETE FROM dbo.DoanVanDoc WHERE MaDoanVan = ?";
        boolean rowDeleted = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maDoanVan);
            rowDeleted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("REFERENCE constraint")) {
                System.err.println("Lỗi: Không thể xóa đoạn văn này vì đang được tham chiếu bởi câu hỏi.");
            } else {
                e.printStackTrace();
            }
        }
        return rowDeleted;
    }
}