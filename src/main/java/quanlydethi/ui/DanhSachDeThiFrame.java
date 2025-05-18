package quanlydethi.ui;

import quanlydethi.model.DeThi;
import quanlydethi.model.TrinhDo; // Cần để hiển thị tên trình độ
import quanlydethi.dao.DeThiDAO;
import quanlydethi.dao.TrinhDoDAO; // Cần để lấy tên trình độ
import quanlydethi.service.DeThiService; // Có thể dùng service nếu có logic phức tạp hơn

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DanhSachDeThiFrame extends JFrame {
    private JTable tableDeThi;
    private DefaultTableModel tableModelDeThi;
    private JButton btnXemChiTiet;
    private JButton btnLamMoi;

    private DeThiDAO deThiDAO;
    private TrinhDoDAO trinhDoDAO; // Để lấy thông tin Trình Độ
    private Map<Integer, String> mapTenTrinhDo; // Cache tên trình độ

    public DanhSachDeThiFrame() {
        this.deThiDAO = new DeThiDAO();
        this.trinhDoDAO = new TrinhDoDAO();
        this.mapTenTrinhDo = new HashMap<>();

        setTitle("Danh Sách Đề Thi Đã Tạo");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        loadTenTrinhDoCache(); // Load tên trình độ trước
        loadDanhSachDeThi();
        initActionListeners();
    }

    private void initComponents() {
        // Table Model
        tableModelDeThi = new DefaultTableModel(
            new Object[]{"Mã Đề", "Tên Đề Thi", "Trình Độ", "Ngày Tạo", "Thời Gian (phút)", "Ngẫu Nhiên?"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên bảng
            }
        };
        tableDeThi = new JTable(tableModelDeThi);
        tableDeThi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDeThi.setFillsViewportHeight(true);
        tableDeThi.setAutoCreateRowSorter(true); // Cho phép sắp xếp cột

        JScrollPane scrollPane = new JScrollPane(tableDeThi);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnXemChiTiet = new JButton("Xem Chi Tiết Đề Thi");
        btnXemChiTiet.setEnabled(false); // Chỉ enable khi có đề được chọn
        btnLamMoi = new JButton("Làm Mới Danh Sách");
        buttonPanel.add(btnLamMoi);
        buttonPanel.add(btnXemChiTiet);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadTenTrinhDoCache() {
        List<TrinhDo> listTrinhDo = trinhDoDAO.getAllTrinhDo();
        if (listTrinhDo != null) {
            for (TrinhDo td : listTrinhDo) {
                mapTenTrinhDo.put(td.getMaTrinhDo(), td.getTenTrinhDo());
            }
        }
    }

    private void loadDanhSachDeThi() {
        tableModelDeThi.setRowCount(0); // Xóa dữ liệu cũ
        List<DeThi> danhSach = deThiDAO.getAllDeThi();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        if (danhSach != null) {
            for (DeThi dt : danhSach) {
                String tenTrinhDo = dt.getMaTrinhDo() != null ? mapTenTrinhDo.getOrDefault(dt.getMaTrinhDo(), "N/A") : "N/A";
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
        if (tableDeThi.getRowCount() > 0) {
            tableDeThi.setRowSelectionInterval(0,0); // Tự động chọn dòng đầu tiên
             btnXemChiTiet.setEnabled(true);
        } else {
            btnXemChiTiet.setEnabled(false);
        }
    }

    private void initActionListeners() {
        tableDeThi.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableDeThi.getSelectedRow() != -1) {
                btnXemChiTiet.setEnabled(true);
            } else if (tableDeThi.getSelectedRow() == -1){
                 btnXemChiTiet.setEnabled(false);
            }
        });

        btnLamMoi.addActionListener(e -> loadDanhSachDeThi());

        btnXemChiTiet.addActionListener(e -> moFrameChiTietDeThi());
        
        // Xử lý double-click trên bảng để xem chi tiết
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
    }

    private void moFrameChiTietDeThi() {
        int selectedRow = tableDeThi.getSelectedRow();
        if (selectedRow != -1) {
            // Lấy MaDeThi từ dòng đã chọn (cần convert về model index nếu có sort)
            int modelRow = tableDeThi.convertRowIndexToModel(selectedRow);
            int maDeThi = (Integer) tableModelDeThi.getValueAt(modelRow, 0); // Cột 0 là MaDeThi
            
            // Mở Frame/Dialog chi tiết đề thi (sẽ tạo ở Bước 2)
            ChiTietDeThiFrame chiTietFrame = new ChiTietDeThiFrame(maDeThi);
            chiTietFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để xem chi tiết.", "Chưa chọn đề", JOptionPane.WARNING_MESSAGE);
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