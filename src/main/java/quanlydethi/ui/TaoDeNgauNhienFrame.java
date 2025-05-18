package quanlydethi.ui;

import quanlydethi.model.DeThi;
import quanlydethi.model.TrinhDo;
import quanlydethi.dao.TrinhDoDAO;
import quanlydethi.service.DeThiService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class TaoDeNgauNhienFrame extends JFrame {
    private JTextField txtTenDeThi;
    private JComboBox<TrinhDo> cmbTrinhDo;
    // private JSpinner spinnerSoLuongCauHoi; // BỎ ĐI
    private JSpinner spinnerSoLuongCauDocHieu;  // MỚI
    private JSpinner spinnerSoLuongCauNgheHieu; // MỚI
    private JSpinner spinnerSoLuongCauKhac;    // MỚI
    private JSpinner spinnerSoLuongDeTao;
    private JSpinner spinnerThoiGianLamBai;
    private JButton btnTaoDe;
    private JTextArea txtKetQua;

    private TrinhDoDAO trinhDoDAO;
    private DeThiService deThiService;
    // private List<TrinhDo> danhSachTrinhDoCache; // Không cần cache ở đây nữa nếu load trực tiếp

    public TaoDeNgauNhienFrame() {
        // ... (khởi tạo DAO, Service như cũ) ...
        this.trinhDoDAO = new TrinhDoDAO();
        this.deThiService = new DeThiService();

        setTitle("Tạo Đề Thi Ngẫu Nhiên Theo Cấu Trúc");
        setSize(600, 500); // Tăng chiều cao cho các trường mới
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        loadTrinhDoData();
        initActionListeners();
    }

    private void initComponents() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int yRow = 0;

        // Tên Đề Thi
        gbc.gridx = 0; gbc.gridy = yRow;
        inputPanel.add(new JLabel("Tên Đề Thi:"), gbc);
        gbc.gridx = 1; gbc.gridy = yRow++; gbc.weightx = 1.0;
        txtTenDeThi = new JTextField(30); // Tăng độ rộng một chút
        inputPanel.add(txtTenDeThi, gbc);
        gbc.weightx = 0.0;

        // Trình Độ
        gbc.gridx = 0; gbc.gridy = yRow;
        inputPanel.add(new JLabel("Trình Độ:"), gbc);
        gbc.gridx = 1; gbc.gridy = yRow++;
        cmbTrinhDo = new JComboBox<>();
        cmbTrinhDo.setRenderer(new TrinhDoComboBoxRenderer());
        inputPanel.add(cmbTrinhDo, gbc);

        // Số Lượng Câu Đọc Hiểu
        gbc.gridx = 0; gbc.gridy = yRow;
        inputPanel.add(new JLabel("Số câu Đọc Hiểu (có Đoạn Văn):"), gbc);
        gbc.gridx = 1; gbc.gridy = yRow++;
        spinnerSoLuongCauDocHieu = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1)); // Min 0
        inputPanel.add(spinnerSoLuongCauDocHieu, gbc);

        // Số Lượng Câu Nghe Hiểu
        gbc.gridx = 0; gbc.gridy = yRow;
        inputPanel.add(new JLabel("Số câu Nghe Hiểu (có Âm Thanh):"), gbc);
        gbc.gridx = 1; gbc.gridy = yRow++;
        spinnerSoLuongCauNgheHieu = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1)); // Min 0
        inputPanel.add(spinnerSoLuongCauNgheHieu, gbc);

        // Số Lượng Câu Khác (không đọc, không nghe)
        gbc.gridx = 0; gbc.gridy = yRow;
        inputPanel.add(new JLabel("Số câu Khác (không ĐV, không AT):"), gbc);
        gbc.gridx = 1; gbc.gridy = yRow++;
        spinnerSoLuongCauKhac = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1)); // Min 0
        inputPanel.add(spinnerSoLuongCauKhac, gbc);
        
        // Thời Gian Làm Bài
        gbc.gridx = 0; gbc.gridy = yRow;
        inputPanel.add(new JLabel("Thời Gian Làm Bài (phút, 0 nếu không giới hạn):"), gbc);
        gbc.gridx = 1; gbc.gridy = yRow++;
        spinnerThoiGianLamBai = new JSpinner(new SpinnerNumberModel(60, 0, 300, 15));
        inputPanel.add(spinnerThoiGianLamBai, gbc);

        // Số Lượng Đề Cần Tạo
        gbc.gridx = 0; gbc.gridy = yRow;
        inputPanel.add(new JLabel("Số Lượng Đề Cần Tạo:"), gbc);
        gbc.gridx = 1; gbc.gridy = yRow++;
        spinnerSoLuongDeTao = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        inputPanel.add(spinnerSoLuongDeTao, gbc);

        // Nút Tạo Đề
        btnTaoDe = new JButton("Tạo Đề Theo Cấu Trúc");
        btnTaoDe.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = yRow; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        inputPanel.add(btnTaoDe, gbc);

        txtKetQua = new JTextArea(7, 30); // Tăng số dòng cho kết quả
        txtKetQua.setEditable(false);
        txtKetQua.setLineWrap(true);
        txtKetQua.setWrapStyleWord(true);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(txtKetQua), BorderLayout.CENTER);
    }

    private void loadTrinhDoData() {
        // ... (giữ nguyên) ...
        List<TrinhDo> danhSachTrinhDo = trinhDoDAO.getAllTrinhDo();
        cmbTrinhDo.removeAllItems();
        if (danhSachTrinhDo != null) {
            for (TrinhDo td : danhSachTrinhDo) {
                cmbTrinhDo.addItem(td);
            }
        }
        if (cmbTrinhDo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu Trình Độ trong CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            btnTaoDe.setEnabled(false);
        }
    }

    private void initActionListeners() {
        btnTaoDe.addActionListener(e -> xuLyTaoDeNgauNhienTheoCauTruc()); // Gọi hàm xử lý mới
    }

    private void xuLyTaoDeNgauNhienTheoCauTruc() {
        String tenDeThiBase = txtTenDeThi.getText().trim(); // Tên cơ sở
        TrinhDo selectedTrinhDo = (TrinhDo) cmbTrinhDo.getSelectedItem();
        int slCauDoc = (Integer) spinnerSoLuongCauDocHieu.getValue();
        int slCauNghe = (Integer) spinnerSoLuongCauNgheHieu.getValue();
        int slCauKhac = (Integer) spinnerSoLuongCauKhac.getValue();
        int thoiGianValue = (Integer) spinnerThoiGianLamBai.getValue();
        Integer thoiGianLamBaiPhut = (thoiGianValue == 0) ? null : thoiGianValue;
        int soLuongDeCanTao = (Integer) spinnerSoLuongDeTao.getValue();

        if (tenDeThiBase.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên cơ sở cho đề thi.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            txtTenDeThi.requestFocus();
            return;
        }
        if (selectedTrinhDo == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn trình độ.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            cmbTrinhDo.requestFocus();
            return;
        }
        if (slCauDoc < 0 || slCauNghe < 0 || slCauKhac < 0) {
             JOptionPane.showMessageDialog(this, "Số lượng câu hỏi cho mỗi phần không được âm.", "Thông tin không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (slCauDoc + slCauNghe + slCauKhac == 0) {
            JOptionPane.showMessageDialog(this, "Tổng số câu hỏi phải lớn hơn 0.", "Thông tin không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }
         if (soLuongDeCanTao <= 0) {
            JOptionPane.showMessageDialog(this, "Số lượng đề cần tạo phải lớn hơn 0.", "Thông tin không hợp lệ", JOptionPane.WARNING_MESSAGE);
            spinnerSoLuongDeTao.requestFocus();
            return;
        }


        txtKetQua.setText("Đang tạo " + soLuongDeCanTao + " đề thi theo cấu trúc, vui lòng chờ...");
        btnTaoDe.setEnabled(false);

        // Tạo cấu trúc các phần
        List<DeThiService.CauTrucPhanDeChiTiet> cauTrucChiTietList = new ArrayList<>();
        if (slCauDoc > 0) cauTrucChiTietList.add(new DeThiService.CauTrucPhanDeChiTiet(DeThiService.LoaiPhanCauHoi.DOC_HIEU, slCauDoc));
        if (slCauNghe > 0) cauTrucChiTietList.add(new DeThiService.CauTrucPhanDeChiTiet(DeThiService.LoaiPhanCauHoi.NGHE_HIEU, slCauNghe));
        if (slCauKhac > 0) cauTrucChiTietList.add(new DeThiService.CauTrucPhanDeChiTiet(DeThiService.LoaiPhanCauHoi.KHAC, slCauKhac));

        SwingWorker<List<DeThi>, String> worker = new SwingWorker<List<DeThi>, String>() {
            @Override
            protected List<DeThi> doInBackground() throws Exception {
                List<DeThi> danhSachDeThiDaTao = new ArrayList<>();
                for (int i = 0; i < soLuongDeCanTao; i++) {
                    String tenDeThiChoLanLap = tenDeThiBase + (soLuongDeCanTao > 1 ? " - Đề " + String.format("%02d", i + 1) : "");
                    publish("Đang tạo '" + tenDeThiChoLanLap + "'...");
                    
                    DeThi motDeThi = deThiService.taoDeThiNgauNhienTheoCauTrucChiTiet(
                        tenDeThiChoLanLap,
                        selectedTrinhDo.getMaTrinhDo(),
                        cauTrucChiTietList,
                        thoiGianLamBaiPhut
                    );
                    danhSachDeThiDaTao.add(motDeThi);
                }
                return danhSachDeThiDaTao;
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    txtKetQua.append("\n" + chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    List<DeThi> cacDeThiMoi = get();
                    StringBuilder ketQuaText = new StringBuilder("Hoàn tất tạo " + cacDeThiMoi.size() + " đề thi:\n");
                    int countSuccess = 0;
                    for (DeThi dt : cacDeThiMoi) {
                        if (dt != null && dt.getMaDeThi() > 0) {
                            ketQuaText.append("- ID Đề: ").append(dt.getMaDeThi())
                                      .append(", Tên: ").append(dt.getTenDeThi()).append(" (Đã lưu)\n");
                            countSuccess++;
                        } else {
                            ketQuaText.append("- Một đề thi tạo thất bại (chi tiết xem console/log của service).\n");
                        }
                    }
                     if (countSuccess > 0) {
                        JOptionPane.showMessageDialog(TaoDeNgauNhienFrame.this,
                                                  countSuccess + "/" + cacDeThiMoi.size() + " đề thi đã được tạo thành công!",
                                                  "Hoàn Tất", JOptionPane.INFORMATION_MESSAGE);
                    } else if (!cacDeThiMoi.isEmpty()){
                         JOptionPane.showMessageDialog(TaoDeNgauNhienFrame.this,
                                                  "Tất cả các đề thi đều tạo thất bại. Vui lòng kiểm tra log.",
                                                  "Thất Bại", JOptionPane.ERROR_MESSAGE);
                    }
                    txtKetQua.setText(ketQuaText.toString());

                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    txtKetQua.append("\nLỗi nghiêm trọng trong quá trình tạo các đề: " + cause.getMessage());
                    JOptionPane.showMessageDialog(TaoDeNgauNhienFrame.this,
                                                  "Lỗi: " + cause.getMessage(),
                                                  "Tạo Đề Thất Bại", JOptionPane.ERROR_MESSAGE);
                    cause.printStackTrace();
                } finally {
                    btnTaoDe.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
    
    // ... (TrinhDoComboBoxRenderer và main giữ nguyên) ...
    class TrinhDoComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof TrinhDo) {
                TrinhDo td = (TrinhDo) value;
                setText(td.getMaTrinhDo() + " - " + td.getTenTrinhDo());
            } else if (value == null && index == -1) { // Item đang hiển thị trên ComboBox
                setText("-- Chọn Trình Độ --");
            } else if (value == null) { // Item null trong danh sách thả xuống
                setText("-- Chọn Trình Độ --"); // Hoặc để trống nếu muốn
            }
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new TaoDeNgauNhienFrame().setVisible(true);
        });
    }
}