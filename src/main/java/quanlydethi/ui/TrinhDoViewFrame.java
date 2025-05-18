package quanlydethi.ui;

import quanlydethi.dao.TrinhDoDAO;
import quanlydethi.model.TrinhDo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class TrinhDoViewFrame extends JFrame {

    private JTable trinhDoTable;
    private DefaultTableModel tableModel;
    private TrinhDoDAO trinhDoDAO;

    public TrinhDoViewFrame() {
        trinhDoDAO = new TrinhDoDAO(); // Khởi tạo DAO

        // Cấu hình cơ bản cho JFrame
        setTitle("Danh Sách Trình Độ");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Đóng cửa sổ này không thoát toàn bộ ứng dụng
        setLocationRelativeTo(null); // Hiển thị cửa sổ ở giữa màn hình

        // Khởi tạo TableModel với các cột
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Mã Trình Độ");
        tableModel.addColumn("Tên Trình Độ");
        tableModel.addColumn("Mô Tả");

        // Khởi tạo JTable với TableModel
        trinhDoTable = new JTable(tableModel);

        // Cho phép JTable cuộn được (nếu có nhiều dữ liệu)
        JScrollPane scrollPane = new JScrollPane(trinhDoTable);

        // Nút để tải lại dữ liệu
        JButton refreshButton = new JButton("Tải Lại Dữ Liệu");
        refreshButton.addActionListener(e -> loadDataToTable());

        // Panel cho nút
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);

        // Thêm JScrollPane (chứa JTable) và ButtonPanel vào JFrame
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Tải dữ liệu lần đầu khi cửa sổ được tạo
        loadDataToTable();
    }

    private void loadDataToTable() {
       
        tableModel.setRowCount(0);
      
        List<TrinhDo> danhSachTrinhDo = trinhDoDAO.getAllTrinhDo();
        if (danhSachTrinhDo == null || danhSachTrinhDo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có dữ liệu trình độ nào để hiển thị.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

      
        for (TrinhDo td : danhSachTrinhDo) {
            Vector<Object> row = new Vector<>();
            row.add(td.getMaTrinhDo());
            row.add(td.getTenTrinhDo());
            row.add(td.getMoTa());
            tableModel.addRow(row);
        }
    }

    public static void main(String[] args) {
      
        SwingUtilities.invokeLater(() -> {
            TrinhDoViewFrame frame = new TrinhDoViewFrame();
            frame.setVisible(true);
        });
    }
}