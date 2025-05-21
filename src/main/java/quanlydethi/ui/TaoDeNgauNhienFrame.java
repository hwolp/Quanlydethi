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
    private JSpinner spinnerSoLuongCauDocHieu;
    private JSpinner spinnerSoLuongCauNgheHieu;
    private JSpinner spinnerSoLuongCauKhac;
    private JSpinner spinnerSoLuongDeTao;
    private JSpinner spinnerThoiGianLamBai;
    private JButton btnTaoDe;
    private JButton btnQuayLaiHome; // NÚT MỚI
    private JTextArea txtKetQua;

    private TrinhDoDAO trinhDoDAO;
    private DeThiService deThiService;

    public TaoDeNgauNhienFrame() {
        this.trinhDoDAO = new TrinhDoDAO();
        this.deThiService = new DeThiService();

        setTitle("Tạo Đề Thi Ngẫu Nhiên Theo Cấu Trúc");
        setSize(600, 550); // Tăng chiều cao một chút cho nút mới
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
        txtTenDeThi = new JTextField(30);
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
        spinnerSoLuongCauDocHieu = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
        inputPanel.add(spinnerSoLuongCauDocHieu, gbc);

        // Số Lượng Câu Nghe Hiểu
        gbc.gridx = 0; gbc.gridy = yRow;
        inputPanel.add(new JLabel("Số câu Nghe Hiểu (có Âm Thanh):"), gbc);
        gbc.gridx = 1; gbc.gridy = yRow++;
        spinnerSoLuongCauNgheHieu = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
        inputPanel.add(spinnerSoLuongCauNgheHieu, gbc);

        // Số Lượng Câu Khác
        gbc.gridx = 0; gbc.gridy = yRow;
        inputPanel.add(new JLabel("Số câu Khác (không ĐV, không AT):"), gbc);
        gbc.gridx = 1; gbc.gridy = yRow++;
        spinnerSoLuongCauKhac = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
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

        // Panel cho các nút hành động
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnTaoDe = new JButton("Tạo Đề Theo Cấu Trúc");
        btnTaoDe.setFont(new Font("Arial", Font.BOLD, 14));
        btnQuayLaiHome = new JButton("Quay lại Home"); // KHỞI TẠO NÚT MỚI
        btnQuayLaiHome.setFont(new Font("Arial", Font.PLAIN, 14));

        actionButtonsPanel.add(btnTaoDe);
        actionButtonsPanel.add(btnQuayLaiHome); // THÊM NÚT MỚI VÀO PANEL

        gbc.gridx = 0; gbc.gridy = yRow; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        inputPanel.add(actionButtonsPanel, gbc); // Thêm panel chứa các nút

        txtKetQua = new JTextArea(7, 30);
        txtKetQua.setEditable(false);
        txtKetQua.setLineWrap(true);
        txtKetQua.setWrapStyleWord(true);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(txtKetQua), BorderLayout.CENTER);
    }

    private void loadTrinhDoData() {
        List<TrinhDo> danhSachTrinhDo = trinhDoDAO.getAllTrinhDo();
        cmbTrinhDo.removeAllItems();
        cmbTrinhDo.addItem(null); // Cho phép không chọn ban đầu
        if (danhSachTrinhDo != null) {
            for (TrinhDo td : danhSachTrinhDo) {
                cmbTrinhDo.addItem(td);
            }
        }
        if (cmbTrinhDo.getItemCount() <= 1 && danhSachTrinhDo.isEmpty()) { // <=1 vì đã add item null
            JOptionPane.showMessageDialog(this, "Không có dữ liệu Trình Độ trong CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            btnTaoDe.setEnabled(false);
        } else {
            btnTaoDe.setEnabled(true);
        }
    }

    private void initActionListeners() {
        btnTaoDe.addActionListener(e -> xuLyTaoDeNgauNhienTheoCauTruc());
        btnQuayLaiHome.addActionListener(e -> quayLaiHomeUI()); // SỰ KIỆN CHO NÚT MỚI
    }

    private void xuLyTaoDeNgauNhienTheoCauTruc() {
        String tenDeThiBase = txtTenDeThi.getText().trim();
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
        btnQuayLaiHome.setEnabled(false); // Vô hiệu hóa nút quay lại trong khi xử lý

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
                    danhSachDeThiDaTao.add(motDeThi); // Thêm cả khi motDeThi là null để biết số lượng đã thử
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
                    StringBuilder ketQuaText = new StringBuilder("Hoàn tất xử lý " + cacDeThiMoi.size() + " đề thi:\n");
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
                    } else if (!cacDeThiMoi.isEmpty()){ // Nếu có thử tạo nhưng không thành công
                         JOptionPane.showMessageDialog(TaoDeNgauNhienFrame.this,
                                                  "Tất cả các đề thi đều tạo thất bại. Vui lòng kiểm tra log.",
                                                  "Thất Bại", JOptionPane.ERROR_MESSAGE);
                    } else { // Trường hợp không có đề nào được yêu cầu tạo (ít xảy ra do validation)
                         ketQuaText.append("Không có đề nào được yêu cầu tạo.");
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
                    btnQuayLaiHome.setEnabled(true); // Kích hoạt lại nút quay lại
                }
            }
        };
        worker.execute();
    }
    
    private void quayLaiHomeUI() {
        this.dispose(); // Đóng frame hiện tại
        // Mở lại HomeUI (giả sử HomeUI là một JFrame và có constructor không tham số)
        // Đảm bảo HomeUI.java tồn tại trong package quanlydethi.ui
        SwingUtilities.invokeLater(() -> {
            HomeIU homeFrame = new HomeIU(); // THAY THẾ HomeUI BẰNG TÊN LỚP FRAME CHÍNH CỦA BẠN
            homeFrame.setVisible(true);
        });
    }

    // Custom renderer cho JComboBox TrinhDo
    class TrinhDoComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof TrinhDo) {
                TrinhDo td = (TrinhDo) value;
                setText(td.getMaTrinhDo() + " - " + td.getTenTrinhDo());
            } else if (value == null && index == -1) {
                setText("-- Chọn Trình Độ --");
            } else if (value == null) {
                setText("-- Chọn Trình Độ --");
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
