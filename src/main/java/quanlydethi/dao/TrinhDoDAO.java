package quanlydethi.dao;

import quanlydethi.model.TrinhDo;
import quanlydethi.dbconnector.SQLServerConnector; 

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrinhDoDAO {

    public List<TrinhDo> getAllTrinhDo() {
        List<TrinhDo> danhSachTrinhDo = new ArrayList<>();
        String sql = "SELECT MaTrinhDo, TenTrinhDo, MoTa FROM dbo.TrinhDo";

        try (Connection conn = SQLServerConnector.getConnection(); // Lấy kết nối
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TrinhDo td = new TrinhDo();
                td.setMaTrinhDo(rs.getInt("MaTrinhDo"));
                td.setTenTrinhDo(rs.getString("TenTrinhDo"));
                td.setMoTa(rs.getString("MoTa"));
                danhSachTrinhDo.add(td);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            
        }
        return danhSachTrinhDo;
    }


    public TrinhDo getTrinhDoById(int maTrinhDo) {
        String sql = "SELECT MaTrinhDo, TenTrinhDo, MoTa FROM dbo.TrinhDo WHERE MaTrinhDo = ?";
        TrinhDo td = null;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maTrinhDo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    td = new TrinhDo();
                    td.setMaTrinhDo(rs.getInt("MaTrinhDo"));
                    td.setTenTrinhDo(rs.getString("TenTrinhDo"));
                    td.setMoTa(rs.getString("MoTa"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return td;
    }

    public boolean addTrinhDo(TrinhDo trinhDo) {
        String sql = "INSERT INTO dbo.TrinhDo (TenTrinhDo, MoTa) VALUES (?, ?)";
        boolean rowInserted = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, trinhDo.getTenTrinhDo());
            ps.setString(2, trinhDo.getMoTa());

            rowInserted = ps.executeUpdate() > 0;

            if (rowInserted) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        trinhDo.setMaTrinhDo(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("UNIQUE KEY constraint")) {
                 System.err.println("Lỗi: Tên trình độ đã tồn tại.");
                
            }
        }
        return rowInserted;
    }

    public boolean updateTrinhDo(TrinhDo trinhDo) {
        String sql = "UPDATE dbo.TrinhDo SET TenTrinhDo = ?, MoTa = ? WHERE MaTrinhDo = ?";
        boolean rowUpdated = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trinhDo.getTenTrinhDo());
            ps.setString(2, trinhDo.getMoTa());
            ps.setInt(3, trinhDo.getMaTrinhDo());

            rowUpdated = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("UNIQUE KEY constraint")) {
                 System.err.println("Lỗi: Tên trình độ cập nhật bị trùng.");
            }
        }
        return rowUpdated;
    }

    public boolean deleteTrinhDo(int maTrinhDo) {
        String sql = "DELETE FROM dbo.TrinhDo WHERE MaTrinhDo = ?";
        boolean rowDeleted = false;

        try (Connection conn = SQLServerConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maTrinhDo);
            rowDeleted = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Kiểm tra lỗi khóa ngoại nếu TrinhDo đang được tham chiếu
            if (e.getMessage().contains("The DELETE statement conflicted with the REFERENCE constraint")) {
                System.err.println("Lỗi: Không thể xóa trình độ này vì nó đang được sử dụng ở bảng khác.");
                // Hoặc throw một custom exception
            } else {
                e.printStackTrace();
            }
        }
        return rowDeleted;
    }

	
}