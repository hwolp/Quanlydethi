package quanlydethi.ui;

import quanlydethi.model.DeThi;
import quanlydethi.model.TrinhDo;
import quanlydethi.dao.DeThiDAO;
import quanlydethi.dao.TrinhDoDAO;
// import quanlydethi.service.DeThiService; // Not directly used in this frame's current logic

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
// import javax.swing.table.TableRowSorter; // Not explicitly used but autoCreateRowSorter is true
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
// import java.sql.Timestamp; // Not directly used
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DanhSachDeThiFrame extends JFrame {
    private JTable tableDeThi;
    private DefaultTableModel tableModelDeThi;
    private JButton btnXemChiTiet;
    private JButton btnLamMoi;
    private JButton btnQuayLaiHome;
    private JButton btnXoaDeThi; // NÚT MỚI ĐỂ XÓA

    private DeThiDAO deThiDAO;
    private TrinhDoDAO trinhDoDAO;
    private Map<Integer, String> mapTenTrinhDo;

    public DanhSachDeThiFrame() {
        this.deThiDAO = new DeThiDAO();
        this.trinhDoDAO = new TrinhDoDAO();
        this.mapTenTrinhDo = new HashMap<>();

        setTitle("Danh Sách Đề Thi Đã Tạo");
        setSize(850, 500); // Tăng chiều rộng một chút để chứa nút mới
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        loadTenTrinhDoCache();
        loadDanhSachDeThi();
        initActionListeners();
    }

    private void initComponents() {
        tableModelDeThi = new DefaultTableModel(
            new Object[]{"Mã Đề", "Tên Đề Thi", "Trình Độ", "Ngày Tạo", "Thời Gian (phút)", "Ngẫu Nhiên?"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableDeThi = new JTable(tableModelDeThi);
        tableDeThi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDeThi.setFillsViewportHeight(true);
        tableDeThi.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(tableDeThi);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnQuayLaiHome = new JButton("Quay lại Home");
        btnLamMoi = new JButton("Làm Mới");
        btnXemChiTiet = new JButton("Xem Chi Tiết");
        btnXemChiTiet.setEnabled(false);
        btnXoaDeThi = new JButton("Xóa Đề Thi"); // KHỞI TẠO NÚT MỚI
        btnXoaDeThi.setEnabled(false); // Ban đầu vô hiệu hóa

        buttonPanel.add(btnQuayLaiHome);
        buttonPanel.add(btnLamMoi);
        buttonPanel.add(btnXoaDeThi); // THÊM NÚT XÓA VÀO PANEL
        buttonPanel.add(btnXemChiTiet);


        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadTenTrinhDoCache() {
        List<TrinhDo> listTrinhDo = trinhDoDAO.getAllTrinhDo();
        mapTenTrinhDo.clear();
        if (listTrinhDo != null) {
            for (TrinhDo td : listTrinhDo) {
                mapTenTrinhDo.put(td.getMaTrinhDo(), td.getTenTrinhDo());
            }
        }
    }

    private void loadDanhSachDeThi() {
        tableModelDeThi.setRowCount(0);
        List<DeThi> danhSach = deThiDAO.getAllDeThi();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        if (danhSach != null) {
            for (DeThi dt : danhSach) {
                String tenTrinhDo = dt.getMaTrinhDo() != null ? mapTenTrinhDo.getOrDefault(dt.getMaTrinhDo(), "N/A (ID: " + dt.getMaTrinhDo() + ")") : "N/A";
                String ngayTao = dt.getNgayTaoDe() != null ? sdf.format(dt.getNgayTaoDe()) : "N/A";
                String thoiGianLamBai = dt.getThoiGianLamBaiPhut() != null ? dt.getThoiGianLamBaiPhut().toString() : "Không giới hạn";
                
                tableModelDeThi.addRow(new Object[]{
                        dt.getMaDeThi(),
                        dt.getTenDeThi(),
                        tenTrinhDo,
                        ngayTao,
                        thoiGianLamBai,
                        dt.isLaDeNgauNhien() ? "Có" : "Không"
                });
            }
        }
        // Cập nhật trạng thái các nút dựa trên việc có dòng nào được chọn không
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        int selectedRow = tableDeThi.getSelectedRow();
        boolean isRowSelected = (selectedRow != -1);
        btnXemChiTiet.setEnabled(isRowSelected);
        btnXoaDeThi.setEnabled(isRowSelected);

        // Tự động chọn dòng đầu tiên nếu bảng có dữ liệu và chưa có dòng nào được chọn
        if (tableDeThi.getRowCount() > 0 && selectedRow == -1) {
            tableDeThi.setRowSelectionInterval(0, 0);
            // btnXemChiTiet.setEnabled(true); // Không cần vì listener sẽ xử lý
            // btnXoaDeThi.setEnabled(true);
        } else if (tableDeThi.getRowCount() == 0) {
            btnXemChiTiet.setEnabled(false);
            btnXoaDeThi.setEnabled(false);
        }
    }


    private void initActionListeners() {
        tableDeThi.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates(); // Cập nhật trạng thái nút khi lựa chọn thay đổi
            }
        });

        btnLamMoi.addActionListener(e -> {
            loadTenTrinhDoCache();
            loadDanhSachDeThi();
        });

        btnXemChiTiet.addActionListener(e -> moFrameChiTietDeThi());
        
        tableDeThi.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table =(JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    moFrameChiTietDeThi();
                }
            }
        });

        btnQuayLaiHome.addActionListener(e -> quayLaiHomeUI());
        
        // SỰ KIỆN CHO NÚT XÓA ĐỀ THI
        btnXoaDeThi.addActionListener(e -> xuLyXoaDeThi());
    }

    private void moFrameChiTietDeThi() {
        int selectedRowView = tableDeThi.getSelectedRow();
        if (selectedRowView != -1) {
            int modelRow = tableDeThi.convertRowIndexToModel(selectedRowView);
            int maDeThiValue = (Integer) tableModelDeThi.getValueAt(modelRow, 0);
            
            ChiTietDeThiFrame chiTietFrame = new ChiTietDeThiFrame(maDeThiValue);
            chiTietFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để xem chi tiết.", "Chưa chọn đề", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void quayLaiHomeUI() {
        this.dispose(); 
        SwingUtilities.invokeLater(() -> {
            HomeIU homeFrame = new HomeIU(); // Thay HomeUI bằng tên lớp Frame chính của bạn
            homeFrame.setVisible(true);
        });
    }

    // PHƯƠNG THỨC MỚI ĐỂ XỬ LÝ XÓA ĐỀ THI
    private void xuLyXoaDeThi() {
        int selectedRowView = tableDeThi.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để xóa.", "Chưa chọn đề", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tableDeThi.convertRowIndexToModel(selectedRowView);
        int maDeThiValue = (Integer) tableModelDeThi.getValueAt(modelRow, 0);
        String tenDeThi = (String) tableModelDeThi.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa đề thi:\n" + tenDeThi + " (Mã: " + maDeThiValue + ")?\n" +
                "Tất cả các câu hỏi liên kết với đề thi này cũng sẽ bị xóa khỏi đề (do ON DELETE CASCADE).",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Thực hiện xóa trong SwingWorker để không làm đơ UI nếu DAO mất thời gian
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return deThiDAO.deleteDeThi(maDeThiValue);
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(DanhSachDeThiFrame.this,
                                    "Đã xóa thành công đề thi: " + tenDeThi,
                                    "Xóa Thành Công", JOptionPane.INFORMATION_MESSAGE);
                            loadDanhSachDeThi(); // Tải lại danh sách sau khi xóa
                        } else {
                            JOptionPane.showMessageDialog(DanhSachDeThiFrame.this,
                                    "Xóa đề thi thất bại. Có thể đề thi đang được tham chiếu hoặc có lỗi CSDL.\nXem console log để biết thêm chi tiết.",
                                    "Xóa Thất Bại", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        JOptionPane.showMessageDialog(DanhSachDeThiFrame.this,
                                "Lỗi trong quá trình xóa: " + ex.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new DanhSachDeThiFrame().setVisible(true);
        });
    }
}
