package quanlydethi.ui;

import quanlydethi.model.CauHoi;
import quanlydethi.model.DeThi;
import quanlydethi.model.LuaChon;
import quanlydethi.model.TrinhDo;
import quanlydethi.dao.CauHoiDAO;
import quanlydethi.dao.DeThiDAO;
import quanlydethi.dao.LuaChonDAO;
import quanlydethi.dao.CauHoiTrongDeThiDAO;
import quanlydethi.dao.TrinhDoDAO;
import quanlydethi.service.ExportDeThiService; // Đảm bảo service này đã có hàm exportDeThiToPdfViaHtml

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
// import java.awt.event.ActionEvent; // Không cần nếu chỉ dùng lambda
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
// import java.util.Map; // Không dùng map cache trong phiên bản này
// import java.util.HashMap; // Không dùng map cache trong phiên bản này


public class ChiTietDeThiFrame extends JFrame {
    private int maDeThi;
    private DeThi deThiHienTai;

    private DeThiDAO deThiDAO;
    private CauHoiDAO cauHoiDAO;
    private LuaChonDAO luaChonDAO;
    private CauHoiTrongDeThiDAO cauHoiTrongDeThiDAO;
    private TrinhDoDAO trinhDoDAO;
    private ExportDeThiService exportService; // Service để xuất file

    // Components cho thông tin chung
    private JLabel lblMaDeThiValue;
    private JLabel lblTenDeThiValue;
    private JLabel lblTrinhDoValue;
    private JLabel lblNgayTaoValue;
    private JLabel lblThoiGianValue;
    private JLabel lblLoaiDeValue;

    // Panel để chứa danh sách câu hỏi
    private JPanel pnlDanhSachCauHoi;
    private JScrollPane scrollPaneCauHoi;

    // Nút
    private JButton btnExportPdf;
    private JButton btnExportDocx;


    public ChiTietDeThiFrame(int maDeThi) {
        this.maDeThi = maDeThi;
        this.deThiDAO = new DeThiDAO();
        this.cauHoiDAO = new CauHoiDAO();
        this.luaChonDAO = new LuaChonDAO();
        this.cauHoiTrongDeThiDAO = new CauHoiTrongDeThiDAO();
        this.trinhDoDAO = new TrinhDoDAO();
        this.exportService = new ExportDeThiService(); // Khởi tạo service

        setTitle("Chi Tiết Đề Thi - Mã: " + maDeThi);
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        loadChiTietDeThi();
    }

    private void initComponents() {
        // --- Panel Thông Tin Chung Của Đề Thi (NORTH) ---
        // ... (Giữ nguyên như phiên bản bạn đã cung cấp)
        JPanel pnlThongTinChung = new JPanel(new GridBagLayout());
        pnlThongTinChung.setBorder(BorderFactory.createTitledBorder("Thông Tin Chung Đề Thi"));
        GridBagConstraints gbcInfo = new GridBagConstraints();
        gbcInfo.insets = new Insets(5, 5, 5, 5);
        gbcInfo.anchor = GridBagConstraints.WEST;

        gbcInfo.gridx = 0; gbcInfo.gridy = 0;
        pnlThongTinChung.add(new JLabel("Mã Đề Thi:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.weightx = 1.0; gbcInfo.fill = GridBagConstraints.HORIZONTAL;
        lblMaDeThiValue = new JLabel("...");
        pnlThongTinChung.add(lblMaDeThiValue, gbcInfo);

        gbcInfo.gridx = 0; gbcInfo.gridy = 1; gbcInfo.weightx = 0.0; gbcInfo.fill = GridBagConstraints.NONE;
        pnlThongTinChung.add(new JLabel("Tên Đề Thi:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.weightx = 1.0; gbcInfo.fill = GridBagConstraints.HORIZONTAL;
        lblTenDeThiValue = new JLabel("...");
        pnlThongTinChung.add(lblTenDeThiValue, gbcInfo);

        gbcInfo.gridx = 0; gbcInfo.gridy = 2; gbcInfo.weightx = 0.0; gbcInfo.fill = GridBagConstraints.NONE;
        pnlThongTinChung.add(new JLabel("Trình Độ:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.weightx = 1.0; gbcInfo.fill = GridBagConstraints.HORIZONTAL;
        lblTrinhDoValue = new JLabel("...");
        pnlThongTinChung.add(lblTrinhDoValue, gbcInfo);
        
        gbcInfo.gridx = 0; gbcInfo.gridy = 3; gbcInfo.weightx = 0.0; gbcInfo.fill = GridBagConstraints.NONE;
        pnlThongTinChung.add(new JLabel("Ngày Tạo:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.weightx = 1.0; gbcInfo.fill = GridBagConstraints.HORIZONTAL;
        lblNgayTaoValue = new JLabel("...");
        pnlThongTinChung.add(lblNgayTaoValue, gbcInfo);

        gbcInfo.gridx = 0; gbcInfo.gridy = 4; gbcInfo.weightx = 0.0; gbcInfo.fill = GridBagConstraints.NONE;
        pnlThongTinChung.add(new JLabel("Thời Gian Làm Bài:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.weightx = 1.0; gbcInfo.fill = GridBagConstraints.HORIZONTAL;
        lblThoiGianValue = new JLabel("...");
        pnlThongTinChung.add(lblThoiGianValue, gbcInfo);

        gbcInfo.gridx = 0; gbcInfo.gridy = 5; gbcInfo.weightx = 0.0; gbcInfo.fill = GridBagConstraints.NONE;
        pnlThongTinChung.add(new JLabel("Đề Ngẫu Nhiên:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.weightx = 1.0; gbcInfo.fill = GridBagConstraints.HORIZONTAL;
        lblLoaiDeValue = new JLabel("...");
        pnlThongTinChung.add(lblLoaiDeValue, gbcInfo);
        add(pnlThongTinChung, BorderLayout.NORTH);

        // --- Panel Danh Sách Câu Hỏi (CENTER) ---
        // ... (Giữ nguyên như phiên bản bạn đã cung cấp)
        pnlDanhSachCauHoi = new JPanel();
        pnlDanhSachCauHoi.setLayout(new BoxLayout(pnlDanhSachCauHoi, BoxLayout.Y_AXIS)); 
        pnlDanhSachCauHoi.setBackground(Color.WHITE); 
        scrollPaneCauHoi = new JScrollPane(pnlDanhSachCauHoi);
        scrollPaneCauHoi.setBorder(BorderFactory.createTitledBorder("Danh Sách Câu Hỏi"));
        scrollPaneCauHoi.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPaneCauHoi.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPaneCauHoi, BorderLayout.CENTER);

        // --- Panel Nút Chức Năng (SOUTH) ---
        // ... (Giữ nguyên như phiên bản bạn đã cung cấp)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnExportPdf = new JButton("Xuất ra PDF");
        btnExportDocx = new JButton("Xuất ra DOCX");
        btnExportPdf.setEnabled(false); 
        btnExportDocx.setEnabled(false);
        bottomPanel.add(btnExportPdf);
        bottomPanel.add(btnExportDocx);
        add(bottomPanel, BorderLayout.SOUTH);

        btnExportPdf.addActionListener(e -> xuLyExport("pdf"));
        btnExportDocx.addActionListener(e -> xuLyExport("docx"));
    }

    private void loadChiTietDeThi() {
        // ... (Giữ nguyên như phiên bản bạn đã cung cấp, bao gồm cả createQuestionDisplayPanel)
        deThiHienTai = deThiDAO.getDeThiById(maDeThi);
        if (deThiHienTai == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy đề thi với mã: " + maDeThi, "Lỗi", JOptionPane.ERROR_MESSAGE);
            lblMaDeThiValue.setText("LỖI");
            lblTenDeThiValue.setText("Không tìm thấy đề thi " + maDeThi);
            btnExportPdf.setEnabled(false);
            btnExportDocx.setEnabled(false);
            return;
        }
        btnExportPdf.setEnabled(true);
        btnExportDocx.setEnabled(true);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        lblMaDeThiValue.setText(String.valueOf(deThiHienTai.getMaDeThi()));
        lblTenDeThiValue.setText(deThiHienTai.getTenDeThi());
        String tenTrinhDo = "N/A";
        if (deThiHienTai.getMaTrinhDo() != null) {
            TrinhDo td = trinhDoDAO.getTrinhDoById(deThiHienTai.getMaTrinhDo());
            if (td != null) {
                tenTrinhDo = td.getTenTrinhDo() + " (Mã: " + td.getMaTrinhDo() + ")";
            } else {
                 tenTrinhDo = "(Mã: " + deThiHienTai.getMaTrinhDo() + " - không tìm thấy tên)";
            }
        }
        lblTrinhDoValue.setText(tenTrinhDo);
        if (deThiHienTai.getNgayTaoDe() != null) {
            lblNgayTaoValue.setText(sdf.format(deThiHienTai.getNgayTaoDe()));
        } else {
            lblNgayTaoValue.setText("N/A");
        }
        lblThoiGianValue.setText(deThiHienTai.getThoiGianLamBaiPhut() != null ? deThiHienTai.getThoiGianLamBaiPhut() + " phút" : "Không giới hạn");
        lblLoaiDeValue.setText(deThiHienTai.isLaDeNgauNhien() ? "Có" : "Không");

        pnlDanhSachCauHoi.removeAll();
        List<Integer> danhSachMaCauHoi = cauHoiTrongDeThiDAO.getMaCauHoiByMaDeThi(maDeThi);

        if (danhSachMaCauHoi == null || danhSachMaCauHoi.isEmpty()) {
            JLabel lblKhongCoCauHoi = new JLabel("   Đề thi này không có câu hỏi nào.");
            lblKhongCoCauHoi.setFont(new Font("Arial", Font.ITALIC, 13));
            pnlDanhSachCauHoi.add(lblKhongCoCauHoi);
        } else {
            int stt = 1;
            for (Integer maCH : danhSachMaCauHoi) {
                if (maCH == null) continue;
                CauHoi ch = cauHoiDAO.getCauHoiById(maCH);
                if (ch != null) {
                    JPanel questionPanel = createQuestionDisplayPanel(ch, stt++);
                    pnlDanhSachCauHoi.add(questionPanel);
                    pnlDanhSachCauHoi.add(Box.createRigidArea(new Dimension(0, 10)));
                } else {
                     JLabel lblLoiCauHoi = new JLabel("   Lỗi: Không tải được chi tiết câu hỏi mã " + maCH);
                     lblLoiCauHoi.setForeground(Color.RED);
                     pnlDanhSachCauHoi.add(lblLoiCauHoi);
                     pnlDanhSachCauHoi.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }
        pnlDanhSachCauHoi.revalidate();
        pnlDanhSachCauHoi.repaint();
        SwingUtilities.invokeLater(() -> {
            if (scrollPaneCauHoi != null && scrollPaneCauHoi.getVerticalScrollBar() != null) {
                 scrollPaneCauHoi.getVerticalScrollBar().setValue(0);
            }
        });
    }

    private JPanel createQuestionDisplayPanel(CauHoi cauHoi, int stt) {
        // ... (Giữ nguyên như phiên bản bạn đã cung cấp)
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                new EmptyBorder(10, 15, 10, 15) 
        ));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT); 

        JTextArea txtNoiDung = new JTextArea("Câu " + stt + ": " + cauHoi.getNoiDungCauHoi() + " (" + cauHoi.getDiem() + " điểm)");
        txtNoiDung.setWrapStyleWord(true);
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setEditable(false);
        txtNoiDung.setOpaque(false);
        txtNoiDung.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13)); 
        txtNoiDung.setFocusable(false); 
        panel.add(txtNoiDung);

        List<LuaChon> cacLuaChon = luaChonDAO.getLuaChonByMaCauHoi(cauHoi.getMaCauHoi());
        if (cacLuaChon != null && !cacLuaChon.isEmpty()) {
            JPanel choicesOnlyPanel = new JPanel(); 
            choicesOnlyPanel.setLayout(new BoxLayout(choicesOnlyPanel, BoxLayout.Y_AXIS));
            choicesOnlyPanel.setOpaque(false);
            choicesOnlyPanel.setBorder(new EmptyBorder(5, 15, 0, 0));

            for (LuaChon lc : cacLuaChon) {
                JCheckBox chkLuaChon = new JCheckBox(lc.getNoiDungLuaChon());
                chkLuaChon.setSelected(lc.isLaDapAnDung());
                chkLuaChon.setEnabled(false);
                chkLuaChon.setOpaque(false);
                chkLuaChon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
                if (lc.isLaDapAnDung()) {
                    chkLuaChon.setForeground(new Color(0, 128, 0)); 
                    chkLuaChon.setFont(chkLuaChon.getFont().deriveFont(Font.BOLD));
                }
                choicesOnlyPanel.add(chkLuaChon);
            }
            panel.add(choicesOnlyPanel);
        } else {
            JLabel lblNoChoices = new JLabel("  (Câu hỏi tự luận hoặc không có lựa chọn)");
            lblNoChoices.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            lblNoChoices.setBorder(new EmptyBorder(5, 15, 0, 0));
            panel.add(lblNoChoices);
        }
        return panel;
    }
    
    // SỬA ĐỔI HÀM NÀY
    private void xuLyExport(String format) {
        if (deThiHienTai == null) {
            JOptionPane.showMessageDialog(this, "Không có thông tin đề thi để xuất.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        String sanitizedTenDeThi = deThiHienTai.getTenDeThi().replaceAll("[^a-zA-Z0-9.\\-]", "_"); // Loại bỏ ký tự không hợp lệ cho tên file
        String baseFileName = sanitizedTenDeThi + "_Ma" + deThiHienTai.getMaDeThi();
        
        fileChooser.setDialogTitle("Chọn vị trí lưu file Đề Thi");
        File suggestedFileDeThi = new File(baseFileName + "_DeThi." + format);
        fileChooser.setSelectedFile(suggestedFileDeThi);

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFileForDeThi = fileChooser.getSelectedFile();
            // Đảm bảo file có phần mở rộng đúng
            if (!selectedFileForDeThi.getName().toLowerCase().endsWith("." + format)) {
                selectedFileForDeThi = new File(selectedFileForDeThi.getParentFile(), selectedFileForDeThi.getName() + "." + format);
            }
            final File finalFileDeThi = selectedFileForDeThi; // Biến effectively final


            fileChooser.setDialogTitle("Chọn vị trí lưu file Đáp Án");
            File suggestedFileDapAn = new File(baseFileName + "_DapAn." + format);
            fileChooser.setSelectedFile(suggestedFileDapAn);

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFileForDapAn = fileChooser.getSelectedFile();
                 if (!selectedFileForDapAn.getName().toLowerCase().endsWith("." + format)) {
                    selectedFileForDapAn = new File(selectedFileForDapAn.getParentFile(), selectedFileForDapAn.getName() + "." + format);
                }
                final File finalFileDapAn = selectedFileForDapAn; // Biến effectively final

                btnExportPdf.setEnabled(false);
                btnExportDocx.setEnabled(false);
                JDialog progressDialog = createProgressDialog();


                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        if ("pdf".equalsIgnoreCase(format)) {
                            // GỌI PHƯƠNG THỨC MỚI TRONG SERVICE
                            exportService.exportDeThiToPdfViaHtml(deThiHienTai, finalFileDeThi, finalFileDapAn);
                        } else if ("docx".equalsIgnoreCase(format)) {
                            // Giữ nguyên nếu bạn muốn dùng cách cũ cho DOCX,
                            // hoặc cũng có thể tạo HTML rồi tìm cách chuyển sang DOCX (phức tạp hơn).
                            exportService.exportDeThiToDocx(deThiHienTai, finalFileDeThi, finalFileDapAn);
                        }
                        return true;
                    }

                    @Override
                    protected void done() {
                        progressDialog.dispose();
                        btnExportPdf.setEnabled(true);
                        btnExportDocx.setEnabled(true);
                        try {
                            get(); 
                            JOptionPane.showMessageDialog(ChiTietDeThiFrame.this,
                                    "Đã xuất thành công file Đề Thi và Đáp Án ra định dạng " + format.toUpperCase() + "!",
                                    "Xuất File Thành Công", JOptionPane.INFORMATION_MESSAGE);
                            File parentDir = finalFileDeThi.getParentFile();
                            if (parentDir != null && parentDir.exists() && Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().open(parentDir);
                            }

                        } catch (Exception ex) {
                            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                            JOptionPane.showMessageDialog(ChiTietDeThiFrame.this,
                                    "Lỗi khi xuất file " + format.toUpperCase() + ":\n" + cause.getMessage(),
                                    "Lỗi Xuất File", JOptionPane.ERROR_MESSAGE);
                            if (cause != null) cause.printStackTrace(); else ex.printStackTrace();
                        }
                    }
                };
                worker.execute();
                // Chỉ hiển thị dialog nếu worker thực sự bắt đầu
                // (người dùng không nhấn cancel ở JFileChooser cuối)
                if (worker.getState() != SwingWorker.StateValue.PENDING || !worker.isDone()) {
                     progressDialog.setVisible(true);
                } else { 
                    btnExportPdf.setEnabled(true);
                    btnExportDocx.setEnabled(true);
                }
            } else { 
                 btnExportPdf.setEnabled(true);
                 btnExportDocx.setEnabled(true);
            }
        } else { 
            btnExportPdf.setEnabled(true);
            btnExportDocx.setEnabled(true);
        }
    }
    
    private JDialog createProgressDialog(){
        JDialog progressDialog = new JDialog(this, "Đang Xuất File...", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressDialog.add(BorderLayout.CENTER, progressBar);
        progressDialog.add(BorderLayout.NORTH, new JLabel("Vui lòng chờ trong giây lát."));
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setSize(300, 100);
        progressDialog.setLocationRelativeTo(this);
        return progressDialog;
    }
    // (Main method có thể không cần thiết)
}