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
            e.printStackTrace();
        }
        return tat;
    }

    public boolean addTepAmThanh(TepAmThanh tepAmThanh) {
        String sql = "INSERT INTO dbo.TepAmThanh (TenTep, DuongDanTep, ThoiLuong, MaTrinhDo) VALUES (?, ?, ?, ?)";
        boolean rowInserted = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tepAmThanh.getTenTep());
            ps.setString(2, tepAmThanh.getDuongDanTep());
            ps.setTime(3, tepAmThanh.getThoiLuong());
            if (tepAmThanh.getMaTrinhDo() != null) {
                ps.setInt(4, tepAmThanh.getMaTrinhDo());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            rowInserted = ps.executeUpdate() > 0;

            if (rowInserted) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        tepAmThanh.setMaAmThanh(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return rowUpdated;
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
                System.err.println("Lỗi: Không thể xóa tệp âm thanh này vì đang được tham chiếu bởi câu hỏi.");
            } else {
                e.printStackTrace();
            }
        }
        return rowDeleted;
    }
}