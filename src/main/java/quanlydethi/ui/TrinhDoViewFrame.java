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
        trinhDoDAO = new TrinhDoDAO();

        setTitle("Danh Sách Trình Độ");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel();
        tableModel.addColumn("Mã Trình Độ");
        tableModel.addColumn("Tên Trình Độ");
        tableModel.addColumn("Mô Tả");

        trinhDoTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(trinhDoTable);

        JButton refreshButton = new JButton("Tải Lại Dữ Liệu");
        refreshButton.addActionListener(e -> loadDataToTable());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

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
