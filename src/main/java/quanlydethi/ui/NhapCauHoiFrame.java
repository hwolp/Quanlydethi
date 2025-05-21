package quanlydethi.ui;

import quanlydethi.model.*;
import quanlydethi.dao.*;
import quanlydethi.service.CauHoiNhapLieuService;
// import quanlydethi.service.ImageProcessingException; // Bỏ nếu không dùng trực tiếp ở đây

import javax.swing.*;
//import javax.swing.border.TitledBorder;
import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

public class NhapCauHoiFrame extends JFrame {

    private JComboBox<LoaiCauHoi> cmbLoaiCauHoi;
    private JComboBox<TrinhDo> cmbTrinhDo;
    private JTextArea txtNoiDungCauHoi;
    private JSpinner spinnerDiem;

    private JPanel pnlConditionalInputs;
    private CardLayout conditionalLayout;
    private final String PANEL_TRAC_NGHIEM_THUONG = "PANEL_TNT";
    private final String PANEL_BAI_DOC = "PANEL_BAIDOC";
    private final String PANEL_AM_THANH = "PANEL_AMTHANH";

    private JTextArea txtNoiDungDoanVan;
    private JComboBox<DoanVanDoc> cmbChonDoanVanCoSan;
    private JRadioButton radioTaoDoanVanMoi, radioChonDoanVanCoSan;
    private ButtonGroup groupDoanVanOption;

    private JTextField txtTenTepAmThanh;
    private JTextField txtDuongDanTepAmThanh;
    private JComboBox<TepAmThanh> cmbChonAmThanhCoSan;
    private JRadioButton radioTaoAmThanhMoi, radioChonAmThanhCoSan;
    private ButtonGroup groupAmThanhOption;

    private JPanel pnlLuaChonContainer;
    private JButton btnThemLuaChon;
    private List<JPanelLuaChonEntry> danhSachPanelLuaChon;
    private ButtonGroup dapAnDungGroup;

    private JButton btnLuuHoacCapNhat;
    private JButton btnXoaForm;
    private JButton btnTroLaiFormCu; // Đổi tên biến để phù hợp

    private LoaiCauHoiDAO loaiCauHoiDAO;
    private TrinhDoDAO trinhDoDAO;
    private CauHoiDAO cauHoiDAO;
    private LuaChonDAO luaChonDAO;
    private DoanVanDocDAO doanVanDocDAO;
    private TepAmThanhDAO tepAmThanhDAO;
    private CauHoiNhapLieuService cauHoiNhapLieuService;

    private boolean editMode = false;
    private Integer maCauHoiDangSua = null;
    private Integer currentMaDoanVan = null;
    private Integer currentMaAmThanh = null;

    public NhapCauHoiFrame() {
        this.loaiCauHoiDAO = new LoaiCauHoiDAO();
        this.trinhDoDAO = new TrinhDoDAO();
        this.cauHoiDAO = new CauHoiDAO();
        this.luaChonDAO = new LuaChonDAO();
        this.doanVanDocDAO = new DoanVanDocDAO();
        this.tepAmThanhDAO = new TepAmThanhDAO();
        this.cauHoiNhapLieuService = new CauHoiNhapLieuService();
        this.danhSachPanelLuaChon = new ArrayList<>();
        this.dapAnDungGroup = new ButtonGroup();

        setupFrame();
        initComponents();
        loadInitialData();
        initActionListeners();

        conditionalLayout.show(pnlConditionalInputs, PANEL_TRAC_NGHIEM_THUONG);
        addDefaultLuaChon();
        btnLuuHoacCapNhat.setText("Lưu Câu Hỏi Mới");
    }

    public NhapCauHoiFrame(int maCauHoi) {
        this();
        this.editMode = true;
        this.maCauHoiDangSua = maCauHoi;
        setTitle("Sửa Câu Hỏi - Mã: " + maCauHoi);
        btnLuuHoacCapNhat.setText("Cập Nhật Câu Hỏi");
        loadCauHoiDeSua(maCauHoi);
    }
    
    private void setupFrame(){
        setTitle("Nhập/Sửa Câu Hỏi");
        setSize(850, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void initComponents() {
        JPanel pnlThongTinChung = new JPanel(new GridBagLayout());
        pnlThongTinChung.setBorder(BorderFactory.createTitledBorder("Thông Tin Cơ Bản Câu Hỏi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        pnlThongTinChung.add(new JLabel("Loại Câu Hỏi:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        cmbLoaiCauHoi = new JComboBox<>();
        pnlThongTinChung.add(cmbLoaiCauHoi, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlThongTinChung.add(new JLabel("Trình Độ:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 1.0;
        cmbTrinhDo = new JComboBox<>();
        pnlThongTinChung.add(cmbTrinhDo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlThongTinChung.add(new JLabel("Nội Dung Câu Hỏi:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        txtNoiDungCauHoi = new JTextArea(5, 20);
        txtNoiDungCauHoi.setLineWrap(true);
        txtNoiDungCauHoi.setWrapStyleWord(true);
        pnlThongTinChung.add(new JScrollPane(txtNoiDungCauHoi), gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnlThongTinChung.add(new JLabel("Điểm:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        spinnerDiem = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        pnlThongTinChung.add(spinnerDiem, gbc);
        
        conditionalLayout = new CardLayout();
        pnlConditionalInputs = new JPanel(conditionalLayout);

        JPanel pnlTNT = new JPanel(new BorderLayout());
        pnlTNT.add(new JLabel("Không yêu cầu thông tin thêm cho loại này.", SwingConstants.CENTER), BorderLayout.CENTER);
        pnlConditionalInputs.add(pnlTNT, PANEL_TRAC_NGHIEM_THUONG);

        JPanel pnlBaiDoc = new JPanel(new BorderLayout(5, 5));
        pnlBaiDoc.setBorder(BorderFactory.createTitledBorder("Thông Tin Đoạn Văn Đọc"));
        JPanel pnlDoanVanOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioTaoDoanVanMoi = new JRadioButton("Tạo đoạn văn mới", true);
        radioChonDoanVanCoSan = new JRadioButton("Chọn đoạn văn đã có");
        groupDoanVanOption = new ButtonGroup();
        groupDoanVanOption.add(radioTaoDoanVanMoi);
        groupDoanVanOption.add(radioChonDoanVanCoSan);
        pnlDoanVanOptions.add(radioTaoDoanVanMoi);
        pnlDoanVanOptions.add(radioChonDoanVanCoSan);
        txtNoiDungDoanVan = new JTextArea(8, 20);
        txtNoiDungDoanVan.setLineWrap(true);
        txtNoiDungDoanVan.setWrapStyleWord(true);
        JScrollPane scrollDoanVan = new JScrollPane(txtNoiDungDoanVan);
        cmbChonDoanVanCoSan = new JComboBox<>();
        cmbChonDoanVanCoSan.setEnabled(false);
        pnlBaiDoc.add(pnlDoanVanOptions, BorderLayout.NORTH);
        pnlBaiDoc.add(scrollDoanVan, BorderLayout.CENTER);
        pnlBaiDoc.add(cmbChonDoanVanCoSan, BorderLayout.SOUTH);
        pnlConditionalInputs.add(pnlBaiDoc, PANEL_BAI_DOC);

        JPanel pnlAmThanh = new JPanel(new BorderLayout(5,5));
        pnlAmThanh.setBorder(BorderFactory.createTitledBorder("Thông Tin Tệp Âm Thanh"));
        JPanel pnlAmThanhOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioTaoAmThanhMoi = new JRadioButton("Tạo tệp âm thanh mới", true);
        radioChonAmThanhCoSan = new JRadioButton("Chọn tệp âm thanh đã có");
        groupAmThanhOption = new ButtonGroup();
        groupAmThanhOption.add(radioTaoAmThanhMoi);
        groupAmThanhOption.add(radioChonAmThanhCoSan);
        pnlAmThanhOptions.add(radioTaoAmThanhMoi);
        pnlAmThanhOptions.add(radioChonAmThanhCoSan);
        JPanel pnlNhapAmThanhMoi = new JPanel(new GridBagLayout());
        GridBagConstraints gbcAudio = new GridBagConstraints();
        gbcAudio.insets = new Insets(3,3,3,3); gbcAudio.fill = GridBagConstraints.HORIZONTAL; gbcAudio.anchor = GridBagConstraints.WEST;
        gbcAudio.gridx = 0; gbcAudio.gridy = 0; pnlNhapAmThanhMoi.add(new JLabel("Tên Tệp:"), gbcAudio);
        gbcAudio.gridx = 1; gbcAudio.gridy = 0; gbcAudio.weightx = 1.0; txtTenTepAmThanh = new JTextField(30); pnlNhapAmThanhMoi.add(txtTenTepAmThanh, gbcAudio);
        gbcAudio.gridx = 0; gbcAudio.gridy = 1; gbcAudio.weightx = 0.0; pnlNhapAmThanhMoi.add(new JLabel("Đường Dẫn/Link:"), gbcAudio);
        gbcAudio.gridx = 1; gbcAudio.gridy = 1; gbcAudio.weightx = 1.0; txtDuongDanTepAmThanh = new JTextField(30); pnlNhapAmThanhMoi.add(txtDuongDanTepAmThanh, gbcAudio);
        cmbChonAmThanhCoSan = new JComboBox<>();
        cmbChonAmThanhCoSan.setEnabled(false);
        pnlAmThanh.add(pnlAmThanhOptions, BorderLayout.NORTH);
        pnlAmThanh.add(pnlNhapAmThanhMoi, BorderLayout.CENTER);
        pnlAmThanh.add(cmbChonAmThanhCoSan, BorderLayout.SOUTH);
        pnlConditionalInputs.add(pnlAmThanh, PANEL_AM_THANH);

        JPanel pnlLuaChonOuter = new JPanel(new BorderLayout(5,5));
        pnlLuaChonOuter.setBorder(BorderFactory.createTitledBorder("Các Lựa Chọn Trắc Nghiệm"));
        pnlLuaChonContainer = new JPanel();
        pnlLuaChonContainer.setLayout(new BoxLayout(pnlLuaChonContainer, BoxLayout.Y_AXIS));
        JScrollPane scrollLuaChon = new JScrollPane(pnlLuaChonContainer);
        scrollLuaChon.setPreferredSize(new Dimension(780, 180));
        btnThemLuaChon = new JButton("Thêm Lựa Chọn");
        JPanel pnlThemBtn = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlThemBtn.add(btnThemLuaChon);
        pnlLuaChonOuter.add(scrollLuaChon, BorderLayout.CENTER);
        pnlLuaChonOuter.add(pnlThemBtn, BorderLayout.SOUTH);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnLuuHoacCapNhat = new JButton("Lưu Câu Hỏi");
        btnLuuHoacCapNhat.setFont(new Font("Arial", Font.BOLD, 14));
        btnXoaForm = new JButton("Xóa Form");
        // THAY ĐỔI TÊN NÚT VÀ CÓ THỂ LÀ BIẾN btnTroLaiFormCu
        btnTroLaiFormCu = new JButton("<< Trở lại form cũ"); 
        btnTroLaiFormCu.setFont(new Font("Arial", Font.PLAIN, 12));


        pnlActions.add(btnTroLaiFormCu); 
        pnlActions.add(btnXoaForm);
        pnlActions.add(btnLuuHoacCapNhat);

        JPanel centerPanel = new JPanel(new BorderLayout(10,10));
        centerPanel.add(pnlThongTinChung, BorderLayout.NORTH);
        centerPanel.add(pnlConditionalInputs, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.NORTH);
        add(pnlLuaChonOuter, BorderLayout.CENTER);
        add(pnlActions, BorderLayout.SOUTH);
    }

    private void loadInitialData() {
        List<LoaiCauHoi> loaiCauHois = loaiCauHoiDAO.getAllLoaiCauHoi();
        cmbLoaiCauHoi.addItem(null); 
        if (loaiCauHois != null) {
            for (LoaiCauHoi lch : loaiCauHois) {
                cmbLoaiCauHoi.addItem(lch);
            }
        }
        cmbLoaiCauHoi.setRenderer(new DefaultListCellRenderer() { 
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LoaiCauHoi) setText(((LoaiCauHoi) value).getTenLoai());
                else if (value == null && index == -1) setText("-- Chọn Loại Câu Hỏi --");
                else if (value == null) setText("-- Chọn Loại Câu Hỏi --");
                return this;
            }
        });

        List<TrinhDo> trinhDos = trinhDoDAO.getAllTrinhDo();
        cmbTrinhDo.addItem(null);
        if (trinhDos != null) {
            for (TrinhDo td : trinhDos) {
                cmbTrinhDo.addItem(td);
            }
        }
        cmbTrinhDo.setRenderer(new DefaultListCellRenderer() { 
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TrinhDo) setText(((TrinhDo) value).getTenTrinhDo());
                else if (value == null && index == -1) setText("-- Chọn Trình Độ --");
                else if (value == null) setText("-- Chọn Trình Độ --");
                return this;
            }
        });
        loadDoanVanCoSan();
        loadAmThanhCoSan();
    }

    private void loadDoanVanCoSan() {
        cmbChonDoanVanCoSan.removeAllItems();
        cmbChonDoanVanCoSan.addItem(null);
        List<DoanVanDoc> doanVans = doanVanDocDAO.getAllDoanVanDoc();
        if (doanVans != null) {
            for (DoanVanDoc dv : doanVans) {
                cmbChonDoanVanCoSan.addItem(dv);
            }
        }
        cmbChonDoanVanCoSan.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DoanVanDoc) {
                    String noiDung = ((DoanVanDoc) value).getNoiDungDoanVan();
                    setText("ID:" + ((DoanVanDoc) value).getMaDoanVan() + " - " + noiDung.substring(0, Math.min(noiDung.length(), 50)) + "...");
                } else if (value == null && index == -1) {
                    setText("-- Chọn đoạn văn đã có --");
                } else if (value == null) {
                    setText("-- Chọn đoạn văn đã có --");
                }
                return this;
            }
        });
    }
    
    private void loadAmThanhCoSan() {
        cmbChonAmThanhCoSan.removeAllItems();
        cmbChonAmThanhCoSan.addItem(null);
        List<TepAmThanh> amThanhs = tepAmThanhDAO.getAllTepAmThanh();
        if (amThanhs != null) {
            for (TepAmThanh at : amThanhs) {
                cmbChonAmThanhCoSan.addItem(at);
            }
        }
        cmbChonAmThanhCoSan.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TepAmThanh) {
                    setText("ID:" + ((TepAmThanh) value).getMaAmThanh() + " - " + ((TepAmThanh) value).getTenTep());
                } else if (value == null && index == -1) {
                    setText("-- Chọn tệp âm thanh đã có --");
                } else if (value == null) {
                    setText("-- Chọn tệp âm thanh đã có --");
                }
                return this;
            }
        });
    }
    
    private void addDefaultLuaChon() {
        for (int i = 0; i < 4; i++) {
            addLuaChonPanel();
        }
    }

    private void addLuaChonPanel() {
        int index = danhSachPanelLuaChon.size();
        String kyHieuMacDinh = Character.toString((char) ('A' + index));
        JPanelLuaChonEntry panelEntry = new JPanelLuaChonEntry(kyHieuMacDinh, "");
        
        dapAnDungGroup.add(panelEntry.getRadioButtonDapAnDung());
        danhSachPanelLuaChon.add(panelEntry);
        pnlLuaChonContainer.add(panelEntry);
        pnlLuaChonContainer.revalidate();
        pnlLuaChonContainer.repaint();
    }
    
    private void removeLuaChonPanel(JPanelLuaChonEntry panelEntry) {
        dapAnDungGroup.remove(panelEntry.getRadioButtonDapAnDung());
        danhSachPanelLuaChon.remove(panelEntry);
        pnlLuaChonContainer.remove(panelEntry);
        for(int i=0; i < danhSachPanelLuaChon.size(); i++){
            danhSachPanelLuaChon.get(i).updateKyHieuLabel(Character.toString((char) ('A' + i)));
        }
        pnlLuaChonContainer.revalidate();
        pnlLuaChonContainer.repaint();
    }

    private void initActionListeners() {
        cmbLoaiCauHoi.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                LoaiCauHoi selectedLoai = (LoaiCauHoi) cmbLoaiCauHoi.getSelectedItem();
                pnlLuaChonContainer.setVisible(true);
                btnThemLuaChon.setVisible(true);

                if (selectedLoai != null) {
                    String tenLoai = selectedLoai.getTenLoai();
                    if ("Trắc nghiệm bài đọc".equalsIgnoreCase(tenLoai)) {
                        conditionalLayout.show(pnlConditionalInputs, PANEL_BAI_DOC);
                        txtNoiDungDoanVan.setEnabled(radioTaoDoanVanMoi.isSelected());
                        cmbChonDoanVanCoSan.setEnabled(radioChonDoanVanCoSan.isSelected());
                    } else if ("Trắc nghiệm nghe".equalsIgnoreCase(tenLoai)) {
                        conditionalLayout.show(pnlConditionalInputs, PANEL_AM_THANH);
                        txtTenTepAmThanh.setEnabled(radioTaoAmThanhMoi.isSelected());
                        txtDuongDanTepAmThanh.setEnabled(radioTaoAmThanhMoi.isSelected());
                        cmbChonAmThanhCoSan.setEnabled(radioChonAmThanhCoSan.isSelected());
                    } else { 
                        conditionalLayout.show(pnlConditionalInputs, PANEL_TRAC_NGHIEM_THUONG);
                    }
                } else {
                    conditionalLayout.show(pnlConditionalInputs, PANEL_TRAC_NGHIEM_THUONG);
                }
            }
        });

        radioTaoDoanVanMoi.addActionListener(e -> {
            txtNoiDungDoanVan.setEnabled(true);
            cmbChonDoanVanCoSan.setEnabled(false);
            cmbChonDoanVanCoSan.setSelectedItem(null);
        });
        radioChonDoanVanCoSan.addActionListener(e -> {
            txtNoiDungDoanVan.setEnabled(false);
            cmbChonDoanVanCoSan.setEnabled(true);
        });

        radioTaoAmThanhMoi.addActionListener(e -> {
            txtTenTepAmThanh.setEnabled(true);
            txtDuongDanTepAmThanh.setEnabled(true);
            cmbChonAmThanhCoSan.setEnabled(false);
            cmbChonAmThanhCoSan.setSelectedItem(null);
        });
        radioChonAmThanhCoSan.addActionListener(e -> {
            txtTenTepAmThanh.setEnabled(false);
            txtDuongDanTepAmThanh.setEnabled(false);
            cmbChonAmThanhCoSan.setEnabled(true);
        });

        btnThemLuaChon.addActionListener(e -> addLuaChonPanel());
        btnLuuHoacCapNhat.addActionListener(e -> xuLyLuuHoacCapNhatCauHoi());
        btnXoaForm.addActionListener(e -> xoaForm());
        // THAY ĐỔI HÀNH ĐỘNG CỦA NÚT
        btnTroLaiFormCu.addActionListener(e -> this.dispose()); 
    }
    
    private void xoaForm() {
        cmbLoaiCauHoi.setSelectedItem(null);
        cmbTrinhDo.setSelectedItem(null);
        txtNoiDungCauHoi.setText("");
        spinnerDiem.setValue(1);

        txtNoiDungDoanVan.setText("");
        radioTaoDoanVanMoi.setSelected(true);
        cmbChonDoanVanCoSan.setSelectedItem(null);
        cmbChonDoanVanCoSan.setEnabled(false);
        txtNoiDungDoanVan.setEnabled(true);

        txtTenTepAmThanh.setText("");
        txtDuongDanTepAmThanh.setText("");
        radioTaoAmThanhMoi.setSelected(true);
        cmbChonAmThanhCoSan.setSelectedItem(null);
        cmbChonAmThanhCoSan.setEnabled(false);
        txtTenTepAmThanh.setEnabled(true);
        txtDuongDanTepAmThanh.setEnabled(true);
        
        for(JPanelLuaChonEntry panel : danhSachPanelLuaChon) {
            dapAnDungGroup.remove(panel.getRadioButtonDapAnDung());
        }
        danhSachPanelLuaChon.clear();
        pnlLuaChonContainer.removeAll();
        addDefaultLuaChon();
        
        pnlLuaChonContainer.revalidate();
        pnlLuaChonContainer.repaint();
        conditionalLayout.show(pnlConditionalInputs, PANEL_TRAC_NGHIEM_THUONG);
        btnLuuHoacCapNhat.setText(editMode ? "Cập Nhật Câu Hỏi" : "Lưu Câu Hỏi Mới");
        txtNoiDungCauHoi.requestFocus();
    }

    private void loadCauHoiDeSua(int maCH) {
        CauHoi ch = cauHoiDAO.getCauHoiById(maCH);
        if (ch == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy câu hỏi với mã: " + maCH, "Lỗi", JOptionPane.ERROR_MESSAGE);
            this.dispose();
            return;
        }

        txtNoiDungCauHoi.setText(ch.getNoiDungCauHoi());
        spinnerDiem.setValue(ch.getDiem());

        for (int i = 0; i < cmbTrinhDo.getItemCount(); i++) {
            TrinhDo td = cmbTrinhDo.getItemAt(i);
            if (td != null && td.getMaTrinhDo() == ch.getMaTrinhDo()) {
                cmbTrinhDo.setSelectedItem(td);
                break;
            }
        }

        for (int i = 0; i < cmbLoaiCauHoi.getItemCount(); i++) {
            LoaiCauHoi lch = cmbLoaiCauHoi.getItemAt(i);
            if (lch != null && lch.getMaLoaiCauHoi() == ch.getMaLoaiCauHoi()) {
                cmbLoaiCauHoi.setSelectedItem(lch);
                break;
            }
        }
        
        currentMaDoanVan = ch.getMaDoanVan();
        if (currentMaDoanVan != null && currentMaDoanVan > 0) {
            DoanVanDoc dv = doanVanDocDAO.getDoanVanDocById(currentMaDoanVan);
            if (dv != null) {
                radioChonDoanVanCoSan.setSelected(true);
                txtNoiDungDoanVan.setText(dv.getNoiDungDoanVan()); 
                txtNoiDungDoanVan.setEnabled(false); 
                cmbChonDoanVanCoSan.setEnabled(true);
                for(int i=0; i < cmbChonDoanVanCoSan.getItemCount(); i++){
                    Object item = cmbChonDoanVanCoSan.getItemAt(i);
                    if(item instanceof DoanVanDoc && ((DoanVanDoc)item).getMaDoanVan() == dv.getMaDoanVan()){
                        cmbChonDoanVanCoSan.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } else {
            radioTaoDoanVanMoi.setSelected(true);
            txtNoiDungDoanVan.setEnabled(true);
            cmbChonDoanVanCoSan.setEnabled(false);
        }

        currentMaAmThanh = ch.getMaAmThanh();
        if (currentMaAmThanh != null && currentMaAmThanh > 0) {
            TepAmThanh at = tepAmThanhDAO.getTepAmThanhById(currentMaAmThanh);
            if (at != null) {
                radioChonAmThanhCoSan.setSelected(true);
                txtTenTepAmThanh.setText(at.getTenTep()); 
                txtDuongDanTepAmThanh.setText(at.getDuongDanTep());
                txtTenTepAmThanh.setEnabled(false);
                txtDuongDanTepAmThanh.setEnabled(false);
                cmbChonAmThanhCoSan.setEnabled(true);
                for(int i=0; i < cmbChonAmThanhCoSan.getItemCount(); i++){
                    Object item = cmbChonAmThanhCoSan.getItemAt(i);
                    if(item instanceof TepAmThanh && ((TepAmThanh)item).getMaAmThanh() == at.getMaAmThanh()){
                        cmbChonAmThanhCoSan.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } else {
            radioTaoAmThanhMoi.setSelected(true);
            txtTenTepAmThanh.setEnabled(true);
            txtDuongDanTepAmThanh.setEnabled(true);
            cmbChonAmThanhCoSan.setEnabled(false);
        }

        List<LuaChon> luaChons = luaChonDAO.getLuaChonByMaCauHoi(maCH);
        for(JPanelLuaChonEntry panel : danhSachPanelLuaChon) {
            dapAnDungGroup.remove(panel.getRadioButtonDapAnDung());
        }
        danhSachPanelLuaChon.clear();
        pnlLuaChonContainer.removeAll();

        if (luaChons != null && !luaChons.isEmpty()) {
            for (LuaChon lc : luaChons) {
                String noiDungFull = lc.getNoiDungLuaChon();
                String kyHieu = "";
                String noiDung = noiDungFull;
                if (noiDungFull != null && noiDungFull.matches("^[A-Za-z0-9][.)]\\s.*")) {
                    int splitIndex = noiDungFull.indexOf(".");
                    if (splitIndex == -1) splitIndex = noiDungFull.indexOf(")");
                    if (splitIndex != -1 && splitIndex < 5) { 
                        kyHieu = noiDungFull.substring(0, splitIndex).trim();
                        noiDung = noiDungFull.substring(splitIndex + 1).trim();
                    }
                }
                JPanelLuaChonEntry panelEntry = new JPanelLuaChonEntry(kyHieu, noiDung);
                panelEntry.getRadioButtonDapAnDung().setSelected(lc.isLaDapAnDung());
                dapAnDungGroup.add(panelEntry.getRadioButtonDapAnDung());
                danhSachPanelLuaChon.add(panelEntry);
                pnlLuaChonContainer.add(panelEntry);
            }
        } else {
            addDefaultLuaChon();
        }
        pnlLuaChonContainer.revalidate();
        pnlLuaChonContainer.repaint();
    }

    private void xuLyLuuHoacCapNhatCauHoi() {
        LoaiCauHoi selectedLoaiCH = (LoaiCauHoi) cmbLoaiCauHoi.getSelectedItem();
        TrinhDo selectedTrinhDo = (TrinhDo) cmbTrinhDo.getSelectedItem();
        String noiDungCH = txtNoiDungCauHoi.getText().trim();
        int diem = (Integer) spinnerDiem.getValue();

        if (selectedLoaiCH == null || selectedTrinhDo == null || noiDungCH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin cơ bản (Loại CH, Trình Độ, Nội Dung).", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final CauHoi cauHoiToProcess = new CauHoi();
        if (editMode && maCauHoiDangSua != null) {
            cauHoiToProcess.setMaCauHoi(maCauHoiDangSua);
        }
        cauHoiToProcess.setNoiDungCauHoi(noiDungCH);
        cauHoiToProcess.setMaLoaiCauHoi(selectedLoaiCH.getMaLoaiCauHoi());
        cauHoiToProcess.setMaTrinhDo(selectedTrinhDo.getMaTrinhDo());
        cauHoiToProcess.setDiem(diem);

        DoanVanDoc doanVanFinal = null;
        TepAmThanh tepAmThanhFinal = null;

        String tenLoaiSelected = selectedLoaiCH.getTenLoai();
        if ("Trắc nghiệm bài đọc".equalsIgnoreCase(tenLoaiSelected)) {
            if (radioTaoDoanVanMoi.isSelected()) {
                String noiDungDV = txtNoiDungDoanVan.getText().trim();
                if (noiDungDV.isEmpty()) { JOptionPane.showMessageDialog(this, "Nội dung đoạn văn mới không được trống.", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
                doanVanFinal = new DoanVanDoc();
                doanVanFinal.setNoiDungDoanVan(noiDungDV);
                doanVanFinal.setMaTrinhDo(selectedTrinhDo.getMaTrinhDo());
            } else if (radioChonDoanVanCoSan.isSelected()) {
                DoanVanDoc selectedDV = (DoanVanDoc) cmbChonDoanVanCoSan.getSelectedItem();
                if (selectedDV == null) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một đoạn văn đã có.", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
                cauHoiToProcess.setMaDoanVan(selectedDV.getMaDoanVan());
            } else { 
                if(editMode && currentMaDoanVan != null) cauHoiToProcess.setMaDoanVan(currentMaDoanVan);
                else cauHoiToProcess.setMaDoanVan(null); 
            }
        } else {
            cauHoiToProcess.setMaDoanVan(null);
        }

        if ("Trắc nghiệm nghe".equalsIgnoreCase(tenLoaiSelected)) {
            if (radioTaoAmThanhMoi.isSelected()) {
                String tenTep = txtTenTepAmThanh.getText().trim();
                String duongDan = txtDuongDanTepAmThanh.getText().trim();
                if (tenTep.isEmpty() || duongDan.isEmpty()) { JOptionPane.showMessageDialog(this, "Tên tệp và đường dẫn âm thanh mới không được trống.", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
                tepAmThanhFinal = new TepAmThanh();
                tepAmThanhFinal.setTenTep(tenTep);
                tepAmThanhFinal.setDuongDanTep(duongDan);
                tepAmThanhFinal.setMaTrinhDo(selectedTrinhDo.getMaTrinhDo());
            } else if (radioChonAmThanhCoSan.isSelected()) {
                TepAmThanh selectedAT = (TepAmThanh) cmbChonAmThanhCoSan.getSelectedItem();
                if (selectedAT == null) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một tệp âm thanh đã có.", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
                cauHoiToProcess.setMaAmThanh(selectedAT.getMaAmThanh());
            } else {
                if(editMode && currentMaAmThanh != null) cauHoiToProcess.setMaAmThanh(currentMaAmThanh);
                else cauHoiToProcess.setMaAmThanh(null);
            }
        } else {
            cauHoiToProcess.setMaAmThanh(null);
        }

        final List<LuaChon> danhSachLuaChonFinal = new ArrayList<>();
        boolean coDapAnDung = false;
        int soLuaChonCoNoiDung = 0;
        for (JPanelLuaChonEntry panelLC : danhSachPanelLuaChon) {
            String noiDungLC = panelLC.getNoiDungLuaChon();
            if (!noiDungLC.isEmpty()) {
                soLuaChonCoNoiDung++;
                LuaChon lc = new LuaChon();
                lc.setNoiDungLuaChon(panelLC.getKyHieu() + ". " + noiDungLC);
                lc.setLaDapAnDung(panelLC.isDapAnDung());
                danhSachLuaChonFinal.add(lc);
                if (lc.isLaDapAnDung()) coDapAnDung = true;
            }
        }
        if (soLuaChonCoNoiDung < 2 && !"Tự luận".equalsIgnoreCase(selectedLoaiCH.getTenLoai())) { 
            JOptionPane.showMessageDialog(this, "Câu hỏi trắc nghiệm cần ít nhất 2 lựa chọn có nội dung.", "Thiếu lựa chọn", JOptionPane.WARNING_MESSAGE); return; 
        }
        if (!coDapAnDung && !"Tự luận".equalsIgnoreCase(selectedLoaiCH.getTenLoai())) { 
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đáp án đúng cho câu hỏi trắc nghiệm.", "Thiếu đáp án đúng", JOptionPane.WARNING_MESSAGE); return; 
        }

        final DoanVanDoc doanVanToSave = doanVanFinal;
        final TepAmThanh tepAmThanhToSave = tepAmThanhFinal;

        btnLuuHoacCapNhat.setEnabled(false);
        btnTroLaiFormCu.setEnabled(false); 

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (editMode) {
                    // Tạm thời: Cần cải thiện logic update trong Service
                    boolean updatedCH = cauHoiNhapLieuService.capNhatCauHoiDayDu(cauHoiToProcess, danhSachLuaChonFinal, doanVanToSave, tepAmThanhToSave, currentMaDoanVan, currentMaAmThanh);
                    return updatedCH;
                } else {
                    return cauHoiNhapLieuService.luuCauHoiDayDu(cauHoiToProcess, danhSachLuaChonFinal, doanVanToSave, tepAmThanhToSave);
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    String action = editMode ? "Cập nhật" : "Lưu";
                    if (success) {
                        JOptionPane.showMessageDialog(NhapCauHoiFrame.this, action + " câu hỏi thành công!", "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                        if (editMode) {
                            NhapCauHoiFrame.this.dispose();
                        } else {
                            xoaForm();
                        }
                    } else {
                        JOptionPane.showMessageDialog(NhapCauHoiFrame.this, action + " câu hỏi thất bại. Vui lòng kiểm tra console log.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(NhapCauHoiFrame.this, "Lỗi khi " + (editMode ? "cập nhật" : "lưu") + ": " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    btnLuuHoacCapNhat.setEnabled(true);
                    btnTroLaiFormCu.setEnabled(true); 
                }
            }
        };
        worker.execute();
    }

    private class JPanelLuaChonEntry extends JPanel {
        private JLabel lblKyHieu;
        private JTextField txtNoiDungLuaChon;
        private JRadioButton radioDapAnDung;
        private JButton btnXoaLuaChonNay;
        private String kyHieuHienTai;

        public JPanelLuaChonEntry(String kyHieu, String noiDung) {
            this.kyHieuHienTai = kyHieu;
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
            
            lblKyHieu = new JLabel(kyHieu + ".");
            txtNoiDungLuaChon = new JTextField(noiDung, 40);
            radioDapAnDung = new JRadioButton("Đáp án đúng");
            
            btnXoaLuaChonNay = new JButton("Xóa");
            btnXoaLuaChonNay.setMargin(new Insets(2, 5, 2, 5));
            btnXoaLuaChonNay.addActionListener(e -> removeLuaChonPanel(this));

            add(lblKyHieu);
            add(txtNoiDungLuaChon);
            add(radioDapAnDung);
            add(btnXoaLuaChonNay);
        }
        
        public void updateKyHieuLabel(String newKyHieu){
            this.kyHieuHienTai = newKyHieu;
            this.lblKyHieu.setText(newKyHieu + ".");
        }

        public String getKyHieu() { return kyHieuHienTai; }
        public String getNoiDungLuaChon() { return txtNoiDungLuaChon.getText().trim(); }
        public boolean isDapAnDung() { return radioDapAnDung.isSelected(); }
        public JRadioButton getRadioButtonDapAnDung() { return radioDapAnDung; }
    }
    
    // Phương thức quayLaiHomeUI không còn được dùng trực tiếp bởi nút btnTroLaiFormCu nữa
    // nhưng có thể giữ lại nếu cần cho mục đích khác.
    private void quayLaiHomeUI() {
        this.dispose();
        SwingUtilities.invokeLater(() -> {
            HomeIU homeFrame = new HomeIU(); 
            homeFrame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new NhapCauHoiFrame().setVisible(true); 
            // Hoặc test chế độ sửa:
            // new NhapCauHoiFrame(24).setVisible(true); 
        });
    }
}