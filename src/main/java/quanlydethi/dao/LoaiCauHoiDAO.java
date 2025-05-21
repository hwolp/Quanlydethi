package quanlydethi.dao;

import quanlydethi.model.LoaiCauHoi;
import quanlydethi.dbconnector.SQLServerConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoaiCauHoiDAO {

    public List<LoaiCauHoi> getAllLoaiCauHoi() {
        List<LoaiCauHoi> danhSach = new ArrayList<>();
        String sql = "SELECT MaLoaiCauHoi, TenLoai, MoTa FROM dbo.LoaiCauHoi";

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LoaiCauHoi lch = new LoaiCauHoi();
                lch.setMaLoaiCauHoi(rs.getInt("MaLoaiCauHoi"));
                lch.setTenLoai(rs.getString("TenLoai"));
                lch.setMoTa(rs.getString("MoTa"));
                danhSach.add(lch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSach;
    }

    public LoaiCauHoi getLoaiCauHoiById(int maLoaiCauHoi) {
        String sql = "SELECT MaLoaiCauHoi, TenLoai, MoTa FROM dbo.LoaiCauHoi WHERE MaLoaiCauHoi = ?";
        LoaiCauHoi lch = null;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maLoaiCauHoi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lch = new LoaiCauHoi();
                    lch.setMaLoaiCauHoi(rs.getInt("MaLoaiCauHoi"));
                    lch.setTenLoai(rs.getString("TenLoai"));
                    lch.setMoTa(rs.getString("MoTa"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lch;
    }

    public boolean addLoaiCauHoi(LoaiCauHoi loaiCauHoi) {
        String sql = "INSERT INTO dbo.LoaiCauHoi (TenLoai, MoTa) VALUES (?, ?)";
        boolean rowInserted = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, loaiCauHoi.getTenLoai());
            ps.setString(2, loaiCauHoi.getMoTa());

            rowInserted = ps.executeUpdate() > 0;

            if (rowInserted) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        loaiCauHoi.setMaLoaiCauHoi(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // In chi tiết lỗi ra console (dev environment)
            // Kiểm tra lỗi ràng buộc UNIQUE KEY (tên loại câu hỏi đã tồn tại)
            if (e.getMessage().contains("UNIQUE KEY constraint") || e.getMessage().contains("Duplicate entry")) { // Điều chỉnh tùy theo CSDL
                System.err.println("Lỗi: Tên loại câu hỏi '" + loaiCauHoi.getTenLoai() + "' đã tồn tại.");
            }
        }
        return rowInserted;
    }

    public boolean updateLoaiCauHoi(LoaiCauHoi loaiCauHoi) {
        String sql = "UPDATE dbo.LoaiCauHoi SET TenLoai = ?, MoTa = ? WHERE MaLoaiCauHoi = ?";
        boolean rowUpdated = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, loaiCauHoi.getTenLoai());
            ps.setString(2, loaiCauHoi.getMoTa());
            ps.setInt(3, loaiCauHoi.getMaLoaiCauHoi());

            rowUpdated = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // In chi tiết lỗi ra console (dev environment)
            // Kiểm tra lỗi ràng buộc UNIQUE KEY (tên loại câu hỏi bị trùng khi cập nhật)
             if (e.getMessage().contains("UNIQUE KEY constraint") || e.getMessage().contains("Duplicate entry")) { // Điều chỉnh tùy theo CSDL
                System.err.println("Lỗi: Tên loại câu hỏi '" + loaiCauHoi.getTenLoai() + "' cập nhật bị trùng với một loại câu hỏi khác.");
            }
        }
        return rowUpdated;
    }

    public boolean deleteLoaiCauHoi(int maLoaiCauHoi) {
        String sql = "DELETE FROM dbo.LoaiCauHoi WHERE MaLoaiCauHoi = ?";
        boolean rowDeleted = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maLoaiCauHoi);
            rowDeleted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Kiểm tra lỗi khóa ngoại nếu LoaiCauHoi đang được tham chiếu
            if (e.getMessage().contains("REFERENCE constraint") || e.getMessage().contains("foreign key constraint fails")) { // Điều chỉnh tùy theo CSDL
                System.err.println("Lỗi: Không thể xóa loại câu hỏi này vì nó đang được sử dụng (tham chiếu) ở bảng khác.");
            } else {
                e.printStackTrace();
            }
        }
        return rowDeleted;
    }
}