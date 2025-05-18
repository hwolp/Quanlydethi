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
            e.printStackTrace();
        }
        return lc;
    }


    public boolean addLuaChon(LuaChon luaChon) {
        String sql = "INSERT INTO dbo.LuaChon (MaCauHoi, NoiDungLuaChon, LaDapAnDung) VALUES (?, ?, ?)";
        boolean rowInserted = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, luaChon.getMaCauHoi());
            ps.setString(2, luaChon.getNoiDungLuaChon());
            ps.setBoolean(3, luaChon.isLaDapAnDung());

            rowInserted = ps.executeUpdate() > 0;

            if (rowInserted) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        luaChon.setMaLuaChon(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowInserted;
    }

    public boolean updateLuaChon(LuaChon luaChon) {
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
            e.printStackTrace();
        }
        return rowUpdated;
    }

    public boolean deleteLuaChon(int maLuaChon) {
        String sql = "DELETE FROM dbo.LuaChon WHERE MaLuaChon = ?";
        boolean rowDeleted = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maLuaChon);
            rowDeleted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowDeleted;
    }

    // Phương thức này hữu ích khi xóa câu hỏi, để xóa các lựa chọn liên quan
    // (Mặc dù ON DELETE CASCADE đã được định nghĩa trong DB)
    public boolean deleteLuaChonByMaCauHoi(int maCauHoi) {
        String sql = "DELETE FROM dbo.LuaChon WHERE MaCauHoi = ?";
        boolean rowsDeleted = false;
        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maCauHoi);
            rowsDeleted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowsDeleted;
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