package quanlydethi.ui;

import quanlydethi.model.CauHoi;
import quanlydethi.model.LoaiCauHoi;
import quanlydethi.model.TrinhDo;
import quanlydethi.dao.CauHoiDAO;
import quanlydethi.dao.LoaiCauHoiDAO;
import quanlydethi.dao.TrinhDoDAO;
// import quanlydethi.service.CauHoiService; // Có thể tạo service để xử lý logic phức tạp

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuanLyNganHangCauHoiFrame extends JFrame {

    private JTable tblCauHoi;
    private DefaultTableModel modelTblCauHoi;
    private JButton btnThemMoi, btnSua, btnXoa, btnLamMoi, btnTimKiem, btnXoaBoLoc, btnQuayLaiHome;
    private JComboBox<TrinhDo> cmbFilterTrinhDo;
    private JComboBox<LoaiCauHoi> cmbFilterLoaiCauHoi;
    private JTextField txtFilterKeyword;

    private CauHoiDAO cauHoiDAO;
    private LoaiCauHoiDAO loaiCauHoiDAO;
    private TrinhDoDAO trinhDoDAO;

    // Không cần cache listTrinhDoCache và listLoaiCauHoiCache ở mức class nữa nếu chỉ dùng trong loadFilterData
    // private List<TrinhDo> listTrinhDoCache;
    // private List<LoaiCauHoi> listLoaiCauHoiCache;
    private Map<Integer, String> mapTenTrinhDo;
    private Map<Integer, String> mapTenLoaiCauHoi;


    public QuanLyNganHangCauHoiFrame() {
        this.cauHoiDAO = new CauHoiDAO();
        this.loaiCauHoiDAO = new LoaiCauHoiDAO();
        this.trinhDoDAO = new TrinhDoDAO();
        this.mapTenTrinhDo = new HashMap<>();
        this.mapTenLoaiCauHoi = new HashMap<>();

        setTitle("Quản Lý Ngân Hàng Câu Hỏi");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        loadFilterData();
        loadCauHoiData(null, null, null); // Load tất cả ban đầu
        initActionListeners();
    }

    private void initComponents() {
        // --- Panel Lọc ---
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlFilter.setBorder(BorderFactory.createTitledBorder("Bộ Lọc Câu Hỏi"));

        pnlFilter.add(new JLabel("Trình Độ:"));
        cmbFilterTrinhDo = new JComboBox<>();
        cmbFilterTrinhDo.setPreferredSize(new Dimension(150, 25));
        pnlFilter.add(cmbFilterTrinhDo);

        pnlFilter.add(new JLabel("Loại Câu Hỏi:"));
        cmbFilterLoaiCauHoi = new JComboBox<>();
        cmbFilterLoaiCauHoi.setPreferredSize(new Dimension(180, 25));
        pnlFilter.add(cmbFilterLoaiCauHoi);

        pnlFilter.add(new JLabel("Từ Khóa:"));
        txtFilterKeyword = new JTextField(20);
        pnlFilter.add(txtFilterKeyword);

        btnTimKiem = new JButton("Tìm Kiếm");
        btnXoaBoLoc = new JButton("Xóa Bộ Lọc");
        pnlFilter.add(btnTimKiem);
        pnlFilter.add(btnXoaBoLoc);

        add(pnlFilter, BorderLayout.NORTH);

        // --- Bảng Hiển Thị Câu Hỏi ---
        String[] columnNames = {"Mã CH", "Nội Dung Câu Hỏi (Xem trước)", "Loại Câu Hỏi", "Trình Độ", "Điểm"};
        modelTblCauHoi = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên bảng
            }
        };
        tblCauHoi = new JTable(modelTblCauHoi);
        tblCauHoi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCauHoi.setFillsViewportHeight(true);
        tblCauHoi.setAutoCreateRowSorter(true); // Cho phép sắp xếp
        tblCauHoi.getColumnModel().getColumn(0).setPreferredWidth(50); // Mã CH
        tblCauHoi.getColumnModel().getColumn(1).setPreferredWidth(400); // Nội dung
        tblCauHoi.getColumnModel().getColumn(2).setPreferredWidth(150); // Loại
        tblCauHoi.getColumnModel().getColumn(3).setPreferredWidth(100); // Trình độ
        tblCauHoi.getColumnModel().getColumn(4).setPreferredWidth(50);  // Điểm

        JScrollPane scrollPaneTable = new JScrollPane(tblCauHoi);
        add(scrollPaneTable, BorderLayout.CENTER);

        // --- Panel Nút Chức Năng ---
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnThemMoi = new JButton("Thêm Mới Câu Hỏi");
        btnSua = new JButton("Sửa Câu Hỏi");
        btnXoa = new JButton("Xóa Câu Hỏi");
        btnLamMoi = new JButton("Làm Mới DS");
        btnQuayLaiHome = new JButton("Quay Lại Home");

        btnSua.setEnabled(false);
        btnXoa.setEnabled(false);

        pnlActions.add(btnThemMoi);
        pnlActions.add(btnSua);
        pnlActions.add(btnXoa);
        pnlActions.add(btnLamMoi);
        pnlActions.add(btnQuayLaiHome);
        add(pnlActions, BorderLayout.SOUTH);
    }

    
    private void loadFilterData() {
        // Load Trình Độ
        List<TrinhDo> listTrinhDo = trinhDoDAO.getAllTrinhDo(); // Lấy dữ liệu mới mỗi lần load
        cmbFilterTrinhDo.removeAllItems(); // <<<< SỬA Ở ĐÂY: Xóa các item cũ trước
        cmbFilterTrinhDo.addItem(null);    // Item "Tất cả"
        mapTenTrinhDo.clear();
        if (listTrinhDo != null) {
            for (TrinhDo td : listTrinhDo) {
                cmbFilterTrinhDo.addItem(td);
                mapTenTrinhDo.put(td.getMaTrinhDo(), td.getTenTrinhDo());
            }
        }
        // Chỉ set renderer một lần trong initComponents hoặc nếu cần thay đổi động
        if (cmbFilterTrinhDo.getRenderer() == null || !(cmbFilterTrinhDo.getRenderer() instanceof DefaultListCellRenderer)) { // Tránh set lại nhiều lần nếu không cần
            cmbFilterTrinhDo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof TrinhDo) setText(((TrinhDo) value).getTenTrinhDo());
                    else if (value == null && index == -1) setText("-- Tất cả Trình Độ --"); // Item đang hiển thị
                    else if (value == null) setText("-- Tất cả Trình Độ --"); // Item null trong danh sách
                    return this;
                }
            });
        }


        // Load Loại Câu Hỏi
        List<LoaiCauHoi> listLoaiCauHoi = loaiCauHoiDAO.getAllLoaiCauHoi(); // Lấy dữ liệu mới
        cmbFilterLoaiCauHoi.removeAllItems(); // <<<< SỬA Ở ĐÂY: Xóa các item cũ trước
        cmbFilterLoaiCauHoi.addItem(null);    // Item "Tất cả"
        mapTenLoaiCauHoi.clear();
        if (listLoaiCauHoi != null) {
            for (LoaiCauHoi lch : listLoaiCauHoi) {
                cmbFilterLoaiCauHoi.addItem(lch);
                mapTenLoaiCauHoi.put(lch.getMaLoaiCauHoi(), lch.getTenLoai());
            }
        }
        if (cmbFilterLoaiCauHoi.getRenderer() == null || !(cmbFilterLoaiCauHoi.getRenderer() instanceof DefaultListCellRenderer)) {
            cmbFilterLoaiCauHoi.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof LoaiCauHoi) setText(((LoaiCauHoi) value).getTenLoai());
                    else if (value == null && index == -1) setText("-- Tất cả Loại Câu Hỏi --");
                    else if (value == null) setText("-- Tất cả Loại Câu Hỏi --");
                    return this;
                }
            });
        }
    }

    private void loadCauHoiData(Integer maTrinhDoFilter, Integer maLoaiCHFilter, String keywordFilter) {
        modelTblCauHoi.setRowCount(0);
        // TODO: Triển khai CauHoiDAO.searchCauHoi(maTrinhDoFilter, maLoaiCHFilter, keywordFilter)
        // Hiện tại vẫn dùng getAllCauHoi() và lọc phía client
        List<CauHoi> danhSachCauHoi;
        if (maTrinhDoFilter != null || maLoaiCHFilter != null || (keywordFilter != null && !keywordFilter.isEmpty())) {
            // Nếu có bất kỳ bộ lọc nào, lý tưởng là gọi hàm search của DAO
            // Tạm thời vẫn lọc ở client cho ví dụ này
            List<CauHoi> allCauHoi = cauHoiDAO.getAllCauHoi();
             if (allCauHoi != null) {
                danhSachCauHoi = allCauHoi.stream()
                    .filter(ch -> (maTrinhDoFilter == null || ch.getMaTrinhDo() == maTrinhDoFilter))
                    .filter(ch -> (maLoaiCHFilter == null || ch.getMaLoaiCauHoi() == maLoaiCHFilter))
                    .filter(ch -> (keywordFilter == null || keywordFilter.isEmpty() ||
                                   (ch.getNoiDungCauHoi() != null && 
                                    ch.getNoiDungCauHoi().toLowerCase().contains(keywordFilter.toLowerCase()))))
                    .collect(Collectors.toList());
            } else {
                danhSachCauHoi = new ArrayList<>();
            }
        } else {
            danhSachCauHoi = cauHoiDAO.getAllCauHoi();
        }


        if (danhSachCauHoi != null) {
            for (CauHoi ch : danhSachCauHoi) {
                String noiDungXemTruoc = ch.getNoiDungCauHoi();
                if (noiDungXemTruoc != null && noiDungXemTruoc.length() > 100) {
                    noiDungXemTruoc = noiDungXemTruoc.substring(0, 97) + "...";
                }
                modelTblCauHoi.addRow(new Object[]{
                        ch.getMaCauHoi(),
                        noiDungXemTruoc,
                        mapTenLoaiCauHoi.getOrDefault(ch.getMaLoaiCauHoi(), "N/A ID: " + ch.getMaLoaiCauHoi()),
                        mapTenTrinhDo.getOrDefault(ch.getMaTrinhDo(), "N/A ID: " + ch.getMaTrinhDo()),
                        ch.getDiem()
                });
            }
        }
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        boolean rowSelected = tblCauHoi.getSelectedRow() != -1;
        btnSua.setEnabled(rowSelected);
        btnXoa.setEnabled(rowSelected);
    }

    private void initActionListeners() {
        tblCauHoi.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        btnTimKiem.addActionListener(e -> {
            TrinhDo selectedTD = (TrinhDo) cmbFilterTrinhDo.getSelectedItem();
            LoaiCauHoi selectedLCH = (LoaiCauHoi) cmbFilterLoaiCauHoi.getSelectedItem();
            Integer maTD = (selectedTD != null) ? selectedTD.getMaTrinhDo() : null;
            Integer maLCH = (selectedLCH != null) ? selectedLCH.getMaLoaiCauHoi() : null;
            String keyword = txtFilterKeyword.getText().trim();
            loadCauHoiData(maTD, maLCH, keyword);
        });

        btnXoaBoLoc.addActionListener(e -> {
            cmbFilterTrinhDo.setSelectedItem(null);
            cmbFilterLoaiCauHoi.setSelectedItem(null);
            txtFilterKeyword.setText("");
            loadCauHoiData(null, null, null);
        });

        btnLamMoi.addActionListener(e -> {
            // Load lại dữ liệu cho ComboBoxes phòng trường hợp có thay đổi trong DB
            loadFilterData(); 
            // Sau đó load lại dữ liệu bảng với filter hiện tại (hoặc xóa filter)
            TrinhDo selectedTD = (TrinhDo) cmbFilterTrinhDo.getSelectedItem();
            LoaiCauHoi selectedLCH = (LoaiCauHoi) cmbFilterLoaiCauHoi.getSelectedItem();
            Integer maTD = (selectedTD != null) ? selectedTD.getMaTrinhDo() : null;
            Integer maLCH = (selectedLCH != null) ? selectedLCH.getMaLoaiCauHoi() : null;
            String keyword = txtFilterKeyword.getText().trim();
            loadCauHoiData(maTD, maLCH, keyword);
        });

        btnThemMoi.addActionListener(e -> {
            NhapCauHoiFrame nhapFrame = new NhapCauHoiFrame();
            nhapFrame.setVisible(true);
            // Cân nhắc thêm WindowListener để tự động làm mới khi frame này đóng
            nhapFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    System.out.println("NhapCauHoiFrame closed, refreshing question list.");
                    loadCauHoiData(null, null, null); // Hoặc giữ filter hiện tại
                }
            });
        });

        btnSua.addActionListener(e -> xuLySuaCauHoi());
        btnXoa.addActionListener(e -> xuLyXoaCauHoi());
        
        tblCauHoi.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblCauHoi.getSelectedRow() != -1) {
                    xuLySuaCauHoi();
                }
            }
        });

        btnQuayLaiHome.addActionListener(e -> {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                HomeIU homeUI = new HomeIU(); 
                homeUI.setVisible(true);
            });
        });
    }

    private void xuLySuaCauHoi() {
        int selectedRowView = tblCauHoi.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để sửa.", "Chưa chọn câu hỏi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tblCauHoi.convertRowIndexToModel(selectedRowView);
        int maCauHoi = (Integer) modelTblCauHoi.getValueAt(modelRow, 0);

        // Mở NhapCauHoiFrame ở chế độ sửa
        NhapCauHoiFrame editFrame = new NhapCauHoiFrame(maCauHoi); // Cần tạo constructor này cho NhapCauHoiFrame
        editFrame.setVisible(true);
        editFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                 System.out.println("Edit NhapCauHoiFrame closed, refreshing question list.");
                loadCauHoiData(null, null, null); // Hoặc giữ filter hiện tại
            }
        });
    }

    private void xuLyXoaCauHoi() {
        int selectedRowView = tblCauHoi.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để xóa.", "Chưa chọn câu hỏi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tblCauHoi.convertRowIndexToModel(selectedRowView);
        int maCauHoi = (Integer) modelTblCauHoi.getValueAt(modelRow, 0);
        String noiDungXemTruoc = (String) modelTblCauHoi.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa câu hỏi sau?\n" +
                "Mã: " + maCauHoi + "\n" +
                "Nội dung: " + noiDungXemTruoc + "\n" +
                "(Các lựa chọn và liên kết trong đề thi cũng sẽ bị xóa nếu CSDL có ON DELETE CASCADE)",
                "Xác nhận xóa câu hỏi",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return cauHoiDAO.deleteCauHoi(maCauHoi);
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(QuanLyNganHangCauHoiFrame.this,
                                    "Đã xóa thành công câu hỏi mã " + maCauHoi,
                                    "Xóa Thành Công", JOptionPane.INFORMATION_MESSAGE);
                            loadCauHoiData(null, null, null);
                        } else {
                            JOptionPane.showMessageDialog(QuanLyNganHangCauHoiFrame.this,
                                    "Xóa câu hỏi thất bại. Câu hỏi có thể đang được sử dụng hoặc có lỗi CSDL.",
                                    "Xóa Thất Bại", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                         Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(QuanLyNganHangCauHoiFrame.this,
                                "Lỗi trong quá trình xóa: " + cause.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        cause.printStackTrace();
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
            new QuanLyNganHangCauHoiFrame().setVisible(true);
        });
    }
}
