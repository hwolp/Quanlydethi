package quanlydethi.ui;

import quanlydethi.dto.CauHoiTrichXuatDTO;
import quanlydethi.dto.LuaChonDTO;
import quanlydethi.model.CauHoi;
import quanlydethi.model.LoaiCauHoi;
import quanlydethi.model.TrinhDo;
import quanlydethi.model.LuaChon;
import quanlydethi.service.ImageProcessingService;
import quanlydethi.dao.CauHoiDAO;
import quanlydethi.dao.LuaChonDAO;
import quanlydethi.dao.LoaiCauHoiDAO;
import quanlydethi.dao.TrinhDoDAO;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ImageToTextFrame extends JFrame {

    // --- Phần UI cũ ---
    private JButton selectImageButton;
    private JLabel imageLabel;
    private JFileChooser fileChooser;
    private File selectedImageFile;
    private JTextField promptTextField;
    private JButton extractButton;

    // --- UI cho thiết lập chung ---
    private JComboBox<LoaiCauHoi> cmbGlobalLoaiCauHoi;
    private JComboBox<TrinhDo> cmbGlobalTrinhDo;

    // --- Phần UI mới cho hiển thị và chỉnh sửa câu hỏi ---
    private JList<CauHoiTrichXuatDTO> listExtractedQuestions;
    private DefaultListModel<CauHoiTrichXuatDTO> listModelExtractedQuestions;
    private JPanel questionEditPanel;
    private JTextArea txtDeBai;
    private JPanel pnlChoicesContainer;
    private JTextField txtDapAnDungKyHieu;
    private JTextArea txtGiaiThich;
    // Đã xóa txtLoaiCauHoiAISuggestion và txtTrinhDoAISuggestion

    private JButton btnSaveChangesToDB;
    private JButton btnSaveAllQuestionsToDB;
    private JLabel lblSelectedQuestionInfo;
    private JButton btnQuayLaiHome;

    private List<CauHoiTrichXuatDTO> currentExtractedDTOs;
    private CauHoiTrichXuatDTO currentEditingDTO;

    // --- Services và DAOs ---
    private ImageProcessingService imageService;
    private CauHoiDAO cauHoiDAO;
    private LuaChonDAO luaChonDAO;
    private LoaiCauHoiDAO loaiCauHoiDAO;
    private TrinhDoDAO trinhDoDAO;

    public ImageToTextFrame() {
        this.imageService = new ImageProcessingService();
        this.cauHoiDAO = new CauHoiDAO();
        this.luaChonDAO = new LuaChonDAO();
        this.loaiCauHoiDAO = new LoaiCauHoiDAO();
        this.trinhDoDAO = new TrinhDoDAO();
        this.currentExtractedDTOs = new ArrayList<>();

        setTitle("Trích Xuất & Chỉnh Sửa Câu Hỏi Từ Hình Ảnh");
        setSize(1200, 800); // Có thể điều chỉnh lại chiều cao nếu cần
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        initFileChooser();
        initActionListeners();
        loadGlobalComboBoxData();
    }

    private void initComponents() {
        JPanel imageInputPanel = new JPanel(new BorderLayout(5, 5));
        imageInputPanel.setBorder(BorderFactory.createTitledBorder("1. Đầu Vào"));
        imageInputPanel.setPreferredSize(new Dimension(400, 0));

        selectImageButton = new JButton("Chọn Tệp Ảnh...");
        imageLabel = new JLabel("Chưa có ảnh.", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(380, 250));
        imageLabel.setBorder(BorderFactory.createEtchedBorder());
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);

        JPanel promptInputPanel = new JPanel(new BorderLayout(5, 5));
        promptInputPanel.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
        JLabel promptLabel = new JLabel("Prompt cho AI (ngữ cảnh/yêu cầu):");
        promptTextField = new JTextField("Trích xuất các câu hỏi trắc nghiệm và đáp án từ hình ảnh này.");
        extractButton = new JButton("2. Trích Xuất Câu Hỏi Từ Ảnh");
        extractButton.setFont(new Font("Arial", Font.BOLD, 14));
        extractButton.setEnabled(false);

        imageInputPanel.add(selectImageButton, BorderLayout.NORTH);
        imageInputPanel.add(imageScrollPane, BorderLayout.CENTER);
        JPanel bottomInputPanel = new JPanel(new BorderLayout(5,5));
        bottomInputPanel.add(promptLabel, BorderLayout.NORTH);
        bottomInputPanel.add(promptTextField, BorderLayout.CENTER);
        bottomInputPanel.add(extractButton, BorderLayout.SOUTH);
        imageInputPanel.add(bottomInputPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout(10,10));

        JPanel globalSelectionPanel = new JPanel(new GridBagLayout());
        globalSelectionPanel.setBorder(BorderFactory.createTitledBorder("Thiết Lập Chung (Áp dụng khi lưu)"));
        GridBagConstraints gbcGlobal = new GridBagConstraints();
        gbcGlobal.insets = new Insets(5,5,5,5);
        gbcGlobal.fill = GridBagConstraints.HORIZONTAL;
        gbcGlobal.anchor = GridBagConstraints.WEST;

        gbcGlobal.gridx = 0; gbcGlobal.gridy = 0; gbcGlobal.weightx = 0.2;
        globalSelectionPanel.add(new JLabel("Loại Câu Hỏi Chung:"), gbcGlobal);
        gbcGlobal.gridx = 1; gbcGlobal.gridy = 0; gbcGlobal.weightx = 0.8;
        cmbGlobalLoaiCauHoi = new JComboBox<>();
        globalSelectionPanel.add(cmbGlobalLoaiCauHoi, gbcGlobal);

        gbcGlobal.gridx = 0; gbcGlobal.gridy = 1;
        globalSelectionPanel.add(new JLabel("Trình Độ Chung:"), gbcGlobal);
        gbcGlobal.gridx = 1; gbcGlobal.gridy = 1;
        cmbGlobalTrinhDo = new JComboBox<>();
        globalSelectionPanel.add(cmbGlobalTrinhDo, gbcGlobal);
        
        rightPanel.add(globalSelectionPanel, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new BorderLayout(5,5));
        listPanel.setBorder(BorderFactory.createTitledBorder("3. Danh Sách Câu Hỏi Đã Trích Xuất"));
        listModelExtractedQuestions = new DefaultListModel<>();
        listExtractedQuestions = new JList<>(listModelExtractedQuestions);
        listExtractedQuestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listExtractedQuestions.setCellRenderer(new CauHoiDtoListCellRenderer());
        JScrollPane listScrollPane = new JScrollPane(listExtractedQuestions);
        listPanel.add(listScrollPane, BorderLayout.CENTER);
        lblSelectedQuestionInfo = new JLabel("Chọn một câu hỏi từ danh sách để xem/sửa chi tiết.");
        listPanel.add(lblSelectedQuestionInfo, BorderLayout.SOUTH);

        questionEditPanel = new JPanel();
        questionEditPanel.setBorder(BorderFactory.createTitledBorder("4. Chi Tiết Câu Hỏi Được Chọn"));
        questionEditPanel.setLayout(new GridBagLayout()); 
        GridBagConstraints gbcEdit = new GridBagConstraints();
        gbcEdit.insets = new Insets(5, 5, 5, 5);
        gbcEdit.fill = GridBagConstraints.HORIZONTAL;
        gbcEdit.anchor = GridBagConstraints.WEST;

        int yRow = 0;
        gbcEdit.gridx = 0; gbcEdit.gridy = yRow; gbcEdit.weightx = 0.1;
        questionEditPanel.add(new JLabel("Đề Bài:"), gbcEdit);
        gbcEdit.gridx = 1; gbcEdit.gridy = yRow++; gbcEdit.weightx = 0.9; gbcEdit.gridwidth = 2;
        txtDeBai = new JTextArea(5, 30);
        txtDeBai.setLineWrap(true);
        txtDeBai.setWrapStyleWord(true);
        questionEditPanel.add(new JScrollPane(txtDeBai), gbcEdit);
        gbcEdit.gridwidth = 1;

        gbcEdit.gridx = 0; gbcEdit.gridy = yRow++; gbcEdit.gridwidth = 3; gbcEdit.fill = GridBagConstraints.BOTH; gbcEdit.weighty = 1.0;
        pnlChoicesContainer = new JPanel();
        pnlChoicesContainer.setLayout(new BoxLayout(pnlChoicesContainer, BoxLayout.Y_AXIS));
        pnlChoicesContainer.setBorder(BorderFactory.createTitledBorder("Các Lựa Chọn"));
        JScrollPane choicesScrollPane = new JScrollPane(pnlChoicesContainer);
        questionEditPanel.add(choicesScrollPane, gbcEdit);
        gbcEdit.gridwidth = 1; gbcEdit.fill = GridBagConstraints.HORIZONTAL; gbcEdit.weighty = 0.0;

        gbcEdit.gridx = 0; gbcEdit.gridy = yRow; gbcEdit.weightx = 0.1;
        questionEditPanel.add(new JLabel("Đáp Án Đúng (Ký Hiệu):"), gbcEdit);
        gbcEdit.gridx = 1; gbcEdit.gridy = yRow++; gbcEdit.weightx = 0.9; gbcEdit.gridwidth = 2;
        txtDapAnDungKyHieu = new JTextField(5);
        questionEditPanel.add(txtDapAnDungKyHieu, gbcEdit);
        gbcEdit.gridwidth = 1;

        gbcEdit.gridx = 0; gbcEdit.gridy = yRow;
        questionEditPanel.add(new JLabel("Giải Thích:"), gbcEdit);
        gbcEdit.gridx = 1; gbcEdit.gridy = yRow++; gbcEdit.gridwidth = 2;
        txtGiaiThich = new JTextArea(3, 30);
        txtGiaiThich.setLineWrap(true);
        txtGiaiThich.setWrapStyleWord(true);
        questionEditPanel.add(new JScrollPane(txtGiaiThich), gbcEdit);
        gbcEdit.gridwidth = 1;

        // Đã xóa phần hiển thị Loại/Trình độ AI gợi ý ở đây

        JPanel saveButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnSaveChangesToDB = new JButton("Lưu Câu Hỏi Hiện Tại");
        btnSaveChangesToDB.setFont(new Font("Arial", Font.PLAIN, 14));
        btnSaveChangesToDB.setEnabled(false);
        
        btnSaveAllQuestionsToDB = new JButton("Lưu Tất Cả Câu Hỏi (Với Thiết Lập Chung)");
        btnSaveAllQuestionsToDB.setFont(new Font("Arial", Font.BOLD, 14));
        btnSaveAllQuestionsToDB.setEnabled(false);

        saveButtonsPanel.add(btnSaveChangesToDB);
        saveButtonsPanel.add(btnSaveAllQuestionsToDB);

        gbcEdit.gridx = 0; gbcEdit.gridy = yRow++; gbcEdit.gridwidth = 3; gbcEdit.anchor = GridBagConstraints.CENTER;
        gbcEdit.fill = GridBagConstraints.NONE;
        questionEditPanel.add(saveButtonsPanel, gbcEdit);
        
        JSplitPane listAndEditSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPanel, new JScrollPane(questionEditPanel));
        listAndEditSplitPane.setResizeWeight(0.4);
        rightPanel.add(listAndEditSplitPane, BorderLayout.CENTER);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageInputPanel, rightPanel);
        mainSplitPane.setDividerLocation(410);
        add(mainSplitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnQuayLaiHome = new JButton("<< Quay lại Home");
        btnQuayLaiHome.setFont(new Font("Arial", Font.PLAIN, 14));
        bottomPanel.add(btnQuayLaiHome);
        add(bottomPanel, BorderLayout.SOUTH);

        disableEditingFields();
    }
    
    private void loadGlobalComboBoxData() {
        List<LoaiCauHoi> loaiCauHoiList = loaiCauHoiDAO.getAllLoaiCauHoi();
        cmbGlobalLoaiCauHoi.removeAllItems(); 
        if (loaiCauHoiList != null) {
            for (LoaiCauHoi lch : loaiCauHoiList) {
                cmbGlobalLoaiCauHoi.addItem(lch);
            }
        }
        cmbGlobalLoaiCauHoi.setSelectedIndex(-1);

        List<TrinhDo> trinhDoList = trinhDoDAO.getAllTrinhDo();
        cmbGlobalTrinhDo.removeAllItems();
        if (trinhDoList != null) {
            for (TrinhDo td : trinhDoList) {
                cmbGlobalTrinhDo.addItem(td);
            }
        }
        cmbGlobalTrinhDo.setSelectedIndex(-1);
    }

    private void disableEditingFields() { 
        txtDeBai.setEnabled(false);
        txtDapAnDungKyHieu.setEnabled(false);
        txtGiaiThich.setEnabled(false);
        for (Component comp : pnlChoicesContainer.getComponents()) {
            if (comp instanceof JPanel) {
                for(Component choiceComp : ((JPanel) comp).getComponents()){
                    if (choiceComp instanceof JTextField || choiceComp instanceof JCheckBox) {
                         choiceComp.setEnabled(false);
                    }
                }
            }
        }
         btnSaveChangesToDB.setEnabled(false);
    }

    private void enableEditingFields() { 
        txtDeBai.setEnabled(true);
        txtDapAnDungKyHieu.setEnabled(true);
        txtGiaiThich.setEnabled(true);
        for (Component comp : pnlChoicesContainer.getComponents()) {
           if (comp instanceof JPanel) {
                for(Component choiceComp : ((JPanel) comp).getComponents()){
                     if (choiceComp instanceof JTextField || choiceComp instanceof JCheckBox) {
                         choiceComp.setEnabled(true);
                    }
                }
            }
        }
        btnSaveChangesToDB.setEnabled(true);
    }

    private void initFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Tệp hình ảnh (JPG, PNG, GIF, WEBP, BMP)", "jpg", "jpeg", "png", "gif", "webp", "bmp"));
        fileChooser.setAcceptAllFileFilterUsed(false);
    }

    private void initActionListeners() {
        selectImageButton.addActionListener(e -> chooseImageFile());
        extractButton.addActionListener(e -> startImageProcessing());
        listExtractedQuestions.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onQuestionSelectedFromList(); 
            }
        });
        btnSaveChangesToDB.addActionListener(e -> saveCurrentQuestionToDB());
        btnSaveAllQuestionsToDB.addActionListener(e -> saveAllQuestionsToDB());
        btnQuayLaiHome.addActionListener(e -> quayLaiHomeUI());
    }
    
    private void quayLaiHomeUI() {
        this.dispose(); 
        SwingUtilities.invokeLater(() -> {
            HomeIU homeFrame = new HomeIU(); 
            homeFrame.setVisible(true);
        });
    }

    private void chooseImageFile() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(selectedImageFile);
                if (img != null) {
                    int lblWidth = Math.max(100, imageLabel.getWidth() > 0 ? imageLabel.getWidth() -10 : 370);
                    int lblHeight = Math.max(100, imageLabel.getHeight() > 0 ? imageLabel.getHeight() - 10 : 240);
                    Image scaledImage = img.getScaledInstance(lblWidth, lblHeight, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImage));
                    imageLabel.setText(null);

                    extractButton.setEnabled(true);
                    listModelExtractedQuestions.clear(); 
                    currentExtractedDTOs.clear(); 
                    clearEditForm();
                    btnSaveChangesToDB.setEnabled(false);
                    btnSaveAllQuestionsToDB.setEnabled(false);
                    disableEditingFields();
                } else {
                    displayImageError("Không thể đọc tệp ảnh đã chọn. Định dạng có thể không được hỗ trợ hoặc tệp bị lỗi.");
                }
            } catch (IOException ex) {
                displayImageError("Lỗi khi đọc tệp ảnh: " + ex.getMessage());
            }
        }
    }

    private void displayImageError(String errorMessage) {
        selectedImageFile = null;
        imageLabel.setIcon(null);
        imageLabel.setText("Lỗi tải ảnh.");
        extractButton.setEnabled(false);
        btnSaveAllQuestionsToDB.setEnabled(false);
        JOptionPane.showMessageDialog(this, errorMessage, "Lỗi Hình Ảnh", JOptionPane.ERROR_MESSAGE);
    }

    private void startImageProcessing() {
        if (selectedImageFile == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tệp hình ảnh trước.", "Chưa chọn ảnh", JOptionPane.WARNING_MESSAGE);
            return;
        }
        extractButton.setEnabled(false);
        selectImageButton.setEnabled(false);
        cmbGlobalLoaiCauHoi.setEnabled(false);
        cmbGlobalTrinhDo.setEnabled(false);
        btnSaveAllQuestionsToDB.setEnabled(false);
        btnSaveChangesToDB.setEnabled(false);
        if (btnQuayLaiHome != null) btnQuayLaiHome.setEnabled(false);

        listModelExtractedQuestions.clear();
        currentExtractedDTOs.clear();
        clearEditForm();
        disableEditingFields();
        lblSelectedQuestionInfo.setText("Đang trích xuất câu hỏi từ ảnh, vui lòng chờ...");

        String userPrompt = promptTextField.getText();

        SwingWorker<List<CauHoiTrichXuatDTO>, Void> worker = new SwingWorker<List<CauHoiTrichXuatDTO>, Void>() {
            @Override
            protected List<CauHoiTrichXuatDTO> doInBackground() throws Exception {
                return imageService.extractQuestionsFromImage(selectedImageFile, userPrompt);
            }

            @Override
            protected void done() {
                try {
                    currentExtractedDTOs = get(); 
                    if (currentExtractedDTOs != null && !currentExtractedDTOs.isEmpty()) {
                        listModelExtractedQuestions.addAll(currentExtractedDTOs);
                        if (!listModelExtractedQuestions.isEmpty()) {
                             listExtractedQuestions.setSelectedIndex(0); 
                        }
                        lblSelectedQuestionInfo.setText("Trích xuất hoàn tất. " + currentExtractedDTOs.size() + " câu hỏi. Chọn Loại/Trình độ chung và câu hỏi để sửa/lưu.");
                        btnSaveAllQuestionsToDB.setEnabled(true); 
                    } else {
                        lblSelectedQuestionInfo.setText("Không trích xuất được câu hỏi nào hoặc AI trả về danh sách rỗng.");
                        JOptionPane.showMessageDialog(ImageToTextFrame.this,
                                "Không trích xuất được câu hỏi nào từ ảnh.",
                                "Kết Quả Trích Xuất", JOptionPane.INFORMATION_MESSAGE);
                        btnSaveAllQuestionsToDB.setEnabled(false);
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    lblSelectedQuestionInfo.setText("Lỗi trích xuất: " + cause.getMessage());
                    JOptionPane.showMessageDialog(ImageToTextFrame.this,
                        "Lỗi khi trích xuất từ AI: " + cause.getMessage(), "Lỗi Trích Xuất", JOptionPane.ERROR_MESSAGE);
                    if (cause != null) cause.printStackTrace(); else e.printStackTrace();
                    btnSaveAllQuestionsToDB.setEnabled(false);
                } finally {
                    extractButton.setEnabled(true);
                    selectImageButton.setEnabled(true);
                    cmbGlobalLoaiCauHoi.setEnabled(true);
                    cmbGlobalTrinhDo.setEnabled(true);
                    if (btnQuayLaiHome != null) btnQuayLaiHome.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
    
    private void updateCurrentEditingDTOFromForm() {
        if (currentEditingDTO != null) {
            currentEditingDTO.setNoiDungCauHoi(txtDeBai.getText());
            currentEditingDTO.setDapAnDungKyHieu(txtDapAnDungKyHieu.getText().trim().toUpperCase());
            currentEditingDTO.setGiaiThich(txtGiaiThich.getText());

            // Không cập nhật ...GoiY của DTO từ global combo boxes
            // currentEditingDTO.setLoaiCauHoiGoiY(...);
            // currentEditingDTO.setTrinhDoGoiY(...);

            List<LuaChonDTO> updatedChoicesInForm = new ArrayList<>();
            Component[] choicePanels = pnlChoicesContainer.getComponents();
            for (int i = 0; i < choicePanels.length; i++) {
                if (choicePanels[i] instanceof JPanel) {
                    JPanel cp = (JPanel) choicePanels[i];
                    JTextField khField = null;
                    JTextField ndField = null;
                    for (Component field : cp.getComponents()) {
                        if (field instanceof JTextField) {
                            if (field.getName() != null && field.getName().startsWith("kyHieu_")) {
                                khField = (JTextField) field;
                            } else if (field.getName() != null && field.getName().startsWith("noiDung_")) {
                                ndField = (JTextField) field;
                            }
                        }
                    }
                    if (khField != null && ndField != null) {
                        updatedChoicesInForm.add(new LuaChonDTO(khField.getText().trim(), ndField.getText().trim()));
                    }
                }
            }
            currentEditingDTO.setCacLuaChon(updatedChoicesInForm);
            
            listExtractedQuestions.repaint(); 
        }
    }

    private void onQuestionSelectedFromList() {
        currentEditingDTO = listExtractedQuestions.getSelectedValue(); 

        if (currentEditingDTO != null) {
            populateEditForm(currentEditingDTO);
            enableEditingFields();
            lblSelectedQuestionInfo.setText("Đang xem/sửa câu: " + (listExtractedQuestions.getSelectedIndex() + 1) + "/" + listModelExtractedQuestions.getSize() + ". Thiết lập chung vẫn giữ nguyên.");
        } else {
            clearEditForm();
            disableEditingFields();
            lblSelectedQuestionInfo.setText("Chọn một câu hỏi từ danh sách để xem/sửa chi tiết.");
        }
    }
    
    private void populateEditForm(CauHoiTrichXuatDTO dto) {
        if (dto == null) {
            clearEditForm();
            disableEditingFields();
            return;
        }
        txtDeBai.setText(dto.getNoiDungCauHoi());
        txtDapAnDungKyHieu.setText(dto.getDapAnDungKyHieu());
        txtGiaiThich.setText(dto.getGiaiThich() != null ? dto.getGiaiThich() : "");

        // Không điền gợi ý AI vào các trường đã xóa
        
        pnlChoicesContainer.removeAll(); 
        if (dto.getCacLuaChon() != null) {
            for (int i = 0; i < dto.getCacLuaChon().size(); i++) {
                LuaChonDTO lcDto = dto.getCacLuaChon().get(i);
                pnlChoicesContainer.add(createChoiceEditPanel(lcDto, i));
            }
        }
        pnlChoicesContainer.revalidate();
        pnlChoicesContainer.repaint();
        txtDeBai.setCaretPosition(0);
        if(txtGiaiThich.getDocument().getLength() > 0) txtGiaiThich.setCaretPosition(0);
    }
    
    private JPanel createChoiceEditPanel(LuaChonDTO lcDto, int index) {
        JPanel choicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        String kyHieu = (lcDto.getKyHieu() != null && !lcDto.getKyHieu().isEmpty()) ? lcDto.getKyHieu() : Character.toString((char) ('A' + index));
        JTextField txtKyHieu = new JTextField(kyHieu, 4);
        JTextField txtNoiDung = new JTextField(lcDto.getNoiDung(), 35);
        
        txtKyHieu.setName("kyHieu_" + index);
        txtNoiDung.setName("noiDung_" + index);

        choicePanel.add(new JLabel("Ký hiệu:"));
        choicePanel.add(txtKyHieu);
        choicePanel.add(new JLabel("Nội dung:"));
        choicePanel.add(txtNoiDung);
        return choicePanel;
    }

    private void clearEditForm() {
        txtDeBai.setText("");
        pnlChoicesContainer.removeAll();
        pnlChoicesContainer.revalidate();
        pnlChoicesContainer.repaint();
        txtDapAnDungKyHieu.setText("");
        txtGiaiThich.setText("");
        // Không còn txtLoaiCauHoiAISuggestion và txtTrinhDoAISuggestion để xóa
        currentEditingDTO = null;
    }

    private boolean getGlobalSelections(final int[] ids) { 
        LoaiCauHoi selectedGlobalLoaiCauHoi = (LoaiCauHoi) cmbGlobalLoaiCauHoi.getSelectedItem();
        TrinhDo selectedGlobalTrinhDo = (TrinhDo) cmbGlobalTrinhDo.getSelectedItem();

        if (selectedGlobalLoaiCauHoi == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Loại Câu Hỏi Chung trong phần 'Thiết Lập Chung'.", "Thiếu Thông Tin", JOptionPane.WARNING_MESSAGE);
            cmbGlobalLoaiCauHoi.requestFocus();
            return false;
        }
        if (selectedGlobalTrinhDo == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Trình Độ Chung trong phần 'Thiết Lập Chung'.", "Thiếu Thông Tin", JOptionPane.WARNING_MESSAGE);
            cmbGlobalTrinhDo.requestFocus();
            return false;
        }
        ids[0] = selectedGlobalLoaiCauHoi.getMaLoaiCauHoi();
        ids[1] = selectedGlobalTrinhDo.getMaTrinhDo();
        return true;
    }

    private void saveCurrentQuestionToDB() {
        if (currentEditingDTO == null) {
            JOptionPane.showMessageDialog(this, "Không có câu hỏi nào được chọn để lưu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        final int[] globalIds = new int[2];
        if (!getGlobalSelections(globalIds)) {
            return;
        }
        final int globalMaLCH = globalIds[0];
        final int globalMaTD = globalIds[1];

        updateCurrentEditingDTOFromForm(); 
        
        saveDtoToDatabase(currentEditingDTO, listExtractedQuestions.getSelectedIndex(), globalMaLCH, globalMaTD);
    }
    
    private void saveDtoToDatabase(CauHoiTrichXuatDTO dtoToSave, int dtoIndexInList, int maLCH, int maTD) {
        btnSaveChangesToDB.setEnabled(false);
        btnSaveAllQuestionsToDB.setEnabled(false);
        cmbGlobalLoaiCauHoi.setEnabled(false);
        cmbGlobalTrinhDo.setEnabled(false);
        if (btnQuayLaiHome != null) btnQuayLaiHome.setEnabled(false);

        SwingWorker<Boolean, Void> saveWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return processAndSaveSingleDto(dtoToSave, true, maLCH, maTD); 
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(ImageToTextFrame.this,
                                "Đã lưu câu hỏi '" + dtoToSave.getNoiDungCauHoi().substring(0, Math.min(30, dtoToSave.getNoiDungCauHoi().length())) + "...' vào CSDL.",
                                "Lưu Thành Công", JOptionPane.INFORMATION_MESSAGE);
                        
                        boolean removedFromModel = listModelExtractedQuestions.removeElement(dtoToSave);
                        if(removedFromModel) currentExtractedDTOs.remove(dtoToSave);
                        else if (dtoIndexInList >= 0 && dtoIndexInList < listModelExtractedQuestions.getSize() && listModelExtractedQuestions.getElementAt(dtoIndexInList) == dtoToSave) {
                             listModelExtractedQuestions.remove(dtoIndexInList);
                             currentExtractedDTOs.remove(dtoToSave);
                        }

                        if (!listModelExtractedQuestions.isEmpty()) {
                             int newSelectionIndex = Math.max(0, dtoIndexInList -1 );
                             if(listModelExtractedQuestions.size() <= newSelectionIndex) newSelectionIndex = listModelExtractedQuestions.size() -1;
                             if(newSelectionIndex >=0) listExtractedQuestions.setSelectedIndex(newSelectionIndex);
                             else {
                                 clearEditForm();
                                 disableEditingFields();
                             }
                        } else {
                            clearEditForm();
                            disableEditingFields();
                            lblSelectedQuestionInfo.setText("Đã lưu câu hỏi. Không còn câu hỏi nào.");
                        }
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(ImageToTextFrame.this, "Lỗi khi lưu vào CSDL: " + cause.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                    if(cause != null) cause.printStackTrace(); else ex.printStackTrace();
                } finally {
                    btnSaveChangesToDB.setEnabled(!listModelExtractedQuestions.isEmpty() && listExtractedQuestions.getSelectedIndex() != -1);
                    btnSaveAllQuestionsToDB.setEnabled(!listModelExtractedQuestions.isEmpty());
                    cmbGlobalLoaiCauHoi.setEnabled(true);
                    cmbGlobalTrinhDo.setEnabled(true);
                    if (btnQuayLaiHome != null) btnQuayLaiHome.setEnabled(true);
                }
            }
        };
        saveWorker.execute();
    }

    private void saveAllQuestionsToDB() {
        if (currentExtractedDTOs == null || currentExtractedDTOs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có câu hỏi nào để lưu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        final int[] globalIds = new int[2];
        if (!getGlobalSelections(globalIds)) {
            return;
        }
        final int globalMaLCH = globalIds[0];
        final int globalMaTD = globalIds[1];

        if (currentEditingDTO != null && listExtractedQuestions.getSelectedValue() == currentEditingDTO) {
            updateCurrentEditingDTOFromForm();
        }

        btnSaveAllQuestionsToDB.setEnabled(false);
        btnSaveChangesToDB.setEnabled(false);
        cmbGlobalLoaiCauHoi.setEnabled(false);
        cmbGlobalTrinhDo.setEnabled(false);
        if (btnQuayLaiHome != null) btnQuayLaiHome.setEnabled(false);
        lblSelectedQuestionInfo.setText("Đang lưu tất cả câu hỏi vào CSDL với Loại và Trình độ đã chọn...");

        SwingWorker<String, Integer> worker = new SwingWorker<String, Integer>() {
            private List<CauHoiTrichXuatDTO> successfullySavedDTOs = new ArrayList<>();
            private List<String> errorMessagesList = new ArrayList<>();
            private int totalToSaveInWorker; 

            @Override
            protected String doInBackground() throws Exception {
                totalToSaveInWorker = currentExtractedDTOs.size();
                int savedCount = 0;
                List<CauHoiTrichXuatDTO> dtosToProcess = new ArrayList<>(currentExtractedDTOs);

                for (int i = 0; i < dtosToProcess.size(); i++) {
                    CauHoiTrichXuatDTO dto = dtosToProcess.get(i);
                    publish(i + 1); 
                    try {
                        boolean isDtoCurrentlyInForm = (dto == currentEditingDTO);
                        boolean success = processAndSaveSingleDto(dto, isDtoCurrentlyInForm, globalMaLCH, globalMaTD);
                        if (success) {
                            savedCount++;
                            successfullySavedDTOs.add(dto);
                        } else {
                            errorMessagesList.add("Không thể lưu: " + (dto.getNoiDungCauHoi() != null ? dto.getNoiDungCauHoi().substring(0, Math.min(30,dto.getNoiDungCauHoi().length())) : "N/A") + "...");
                        }
                    } catch (Exception e) {
                        errorMessagesList.add("Lỗi khi lưu '" + (dto.getNoiDungCauHoi() != null ? dto.getNoiDungCauHoi().substring(0, Math.min(30,dto.getNoiDungCauHoi().length())) : "N/A") + "...': " + e.getMessage());
                        e.printStackTrace();
                    }
                    if (isCancelled()) break;
                }
                return "Đã xử lý " + totalToSaveInWorker + " câu hỏi. Thành công: " + savedCount + ". Thất bại: " + errorMessagesList.size();
            }

            @Override
            protected void process(List<Integer> chunks) {
                for (Integer number : chunks) {
                    lblSelectedQuestionInfo.setText("Đang lưu câu hỏi " + number + "/" + totalToSaveInWorker + "...");
                }
            }

            @Override
            protected void done() {
                try {
                    String resultMessage = get();
                    StringBuilder finalMessage = new StringBuilder(resultMessage);
                    if (!errorMessagesList.isEmpty()) {
                        finalMessage.append("\n\nChi tiết các vấn đề (nếu có):\n");
                        for (String error : errorMessagesList) {
                            finalMessage.append("- ").append(error).append("\n");
                        }
                    }
                    JOptionPane.showMessageDialog(ImageToTextFrame.this, finalMessage.toString(), "Kết Quả Lưu Tất Cả", JOptionPane.INFORMATION_MESSAGE);

                    currentExtractedDTOs.removeAll(successfullySavedDTOs);
                    for(CauHoiTrichXuatDTO savedDto : successfullySavedDTOs){
                        listModelExtractedQuestions.removeElement(savedDto);
                    }
                    
                    if (listModelExtractedQuestions.isEmpty()) {
                        clearEditForm();
                        disableEditingFields();
                        lblSelectedQuestionInfo.setText("Đã lưu tất cả câu hỏi. Không còn câu hỏi trong danh sách.");
                    } else {
                        listExtractedQuestions.setSelectedIndex(0); 
                    }

                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(ImageToTextFrame.this, "Lỗi trong quá trình lưu tất cả: " + cause.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    if (cause != null) cause.printStackTrace(); else e.printStackTrace();
                } finally {
                    selectImageButton.setEnabled(true);
                    extractButton.setEnabled(true);
                    cmbGlobalLoaiCauHoi.setEnabled(true);
                    cmbGlobalTrinhDo.setEnabled(true);
                    if (btnQuayLaiHome != null) btnQuayLaiHome.setEnabled(true);
                    
                    boolean listNotEmpty = !listModelExtractedQuestions.isEmpty();
                    btnSaveAllQuestionsToDB.setEnabled(listNotEmpty);
                    btnSaveChangesToDB.setEnabled(listNotEmpty && listExtractedQuestions.getSelectedIndex() != -1);

                    if (listNotEmpty) {
                         lblSelectedQuestionInfo.setText("Đã lưu. Còn " + listModelExtractedQuestions.getSize() + " câu hỏi. Chọn câu hỏi để sửa.");
                    } else {
                        lblSelectedQuestionInfo.setText("Đã lưu tất cả. Không còn câu hỏi nào.");
                    }
                }
            }
        };
        worker.execute();
    }

    private boolean processAndSaveSingleDto(CauHoiTrichXuatDTO dto, 
                                        boolean isCurrentFormDto, 
                                        Integer overrideMaLCH,
                                        Integer overrideMaTD)
                                        throws Exception {
        CauHoi cauHoiToSave = new CauHoi();
        if (isCurrentFormDto && dto == currentEditingDTO) {
            cauHoiToSave.setNoiDungCauHoi(txtDeBai.getText());
        } else {
            cauHoiToSave.setNoiDungCauHoi(dto.getNoiDungCauHoi());
        }

        int maLoaiCauHoiToSave = -1;
        int maTrinhDoToSave = -1;

        if (overrideMaLCH != null) {
            maLoaiCauHoiToSave = overrideMaLCH;
        } else { 
            // Fallback này chỉ mang tính phòng ngừa, vì logic hiện tại luôn truyền override
             System.err.println("Cảnh báo: overrideMaLCH là null trong processAndSaveSingleDto. Sử dụng ID mặc định.");
            maLoaiCauHoiToSave = 1; // Default nếu không có override (không nên xảy ra)
        }

        if (overrideMaTD != null) {
            maTrinhDoToSave = overrideMaTD;
        } else { 
            System.err.println("Cảnh báo: overrideMaTD là null trong processAndSaveSingleDto. Sử dụng ID mặc định.");
            maTrinhDoToSave = 1; // Default nếu không có override (không nên xảy ra)
        }
        
        // Ghi log nếu ID vẫn là -1 hoặc default mặc dù có override (chỉ để debug, vì override đã là ID)
        if (maLoaiCauHoiToSave <= 0) { // Giả sử ID hợp lệ > 0
            System.err.println("Lỗi nghiêm trọng: MaLoaiCauHoi không hợp lệ ("+ maLoaiCauHoiToSave +") dù có override.");
            maLoaiCauHoiToSave = 1; // Fallback cuối cùng
        }
         if (maTrinhDoToSave <= 0) {
            System.err.println("Lỗi nghiêm trọng: MaTrinhDo không hợp lệ ("+ maTrinhDoToSave +") dù có override.");
            maTrinhDoToSave = 1; // Fallback cuối cùng
        }

        cauHoiToSave.setMaLoaiCauHoi(maLoaiCauHoiToSave);
        cauHoiToSave.setMaTrinhDo(maTrinhDoToSave);
        
        cauHoiToSave.setDiem(1); 
        cauHoiToSave.setMaAmThanh(null);
        cauHoiToSave.setMaDoanVan(null);

        boolean cauHoiSaved = cauHoiDAO.addCauHoi(cauHoiToSave);
        if (cauHoiSaved && cauHoiToSave.getMaCauHoi() > 0) {
            int savedMaCauHoi = cauHoiToSave.getMaCauHoi();
            
            List<LuaChonDTO> choicesToSave;
            String dapAnDungKyHieuEffective;

            if (isCurrentFormDto && dto == currentEditingDTO) {
                choicesToSave = new ArrayList<>();
                Component[] choicePanels = pnlChoicesContainer.getComponents();
                for (Component choicePanelComp : choicePanels) {
                    if (choicePanelComp instanceof JPanel) {
                        JPanel cp = (JPanel) choicePanelComp;
                        JTextField khField = null, ndField = null;
                        for (Component field : cp.getComponents()) {
                            if (field instanceof JTextField) {
                                if (field.getName() != null && field.getName().startsWith("kyHieu_")) khField = (JTextField) field;
                                else if (field.getName() != null && field.getName().startsWith("noiDung_")) ndField = (JTextField) field;
                            }
                        }
                        if (khField != null && ndField != null) choicesToSave.add(new LuaChonDTO(khField.getText().trim(), ndField.getText().trim()));
                    }
                }
                dapAnDungKyHieuEffective = txtDapAnDungKyHieu.getText().trim().toUpperCase();
            } else {
                choicesToSave = dto.getCacLuaChon();
                dapAnDungKyHieuEffective = dto.getDapAnDungKyHieu() != null ? dto.getDapAnDungKyHieu().trim().toUpperCase() : "";
            }

            if (choicesToSave != null) {
                for (LuaChonDTO lcDto : choicesToSave) {
                    LuaChon luaChonToSave = new LuaChon();
                    luaChonToSave.setMaCauHoi(savedMaCauHoi);
                    String noiDungLc = (lcDto.getKyHieu() != null && !lcDto.getKyHieu().isEmpty() ? lcDto.getKyHieu() + ". " : "") + lcDto.getNoiDung();
                    luaChonToSave.setNoiDungLuaChon(noiDungLc);
                    luaChonToSave.setLaDapAnDung(lcDto.getKyHieu() != null && !lcDto.getKyHieu().isEmpty() && lcDto.getKyHieu().equalsIgnoreCase(dapAnDungKyHieuEffective));
                    luaChonDAO.addLuaChon(luaChonToSave);
                }
            }
            return true;
        } else {
            String errorMessage = "Lưu câu hỏi vào CSDL thất bại.";
             JOptionPane.showMessageDialog( SwingUtilities.getWindowAncestor(btnSaveAllQuestionsToDB != null ? btnSaveAllQuestionsToDB : btnSaveChangesToDB),
                                           errorMessage, "Lỗi Lưu CSDL", JOptionPane.ERROR_MESSAGE);
            System.err.println(errorMessage + " cho câu: " + (dto.getNoiDungCauHoi() != null ? dto.getNoiDungCauHoi().substring(0, Math.min(50, dto.getNoiDungCauHoi().length())) + "..." : "[Nội dung null]"));
        }
        return false;
    }
    
    class CauHoiDtoListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof CauHoiTrichXuatDTO) {
                CauHoiTrichXuatDTO dto = (CauHoiTrichXuatDTO) value;
                String displayText = dto.getNoiDungCauHoi();
                if (displayText != null) {
                    if (displayText.length() > 60) {
                        displayText = displayText.substring(0, 57) + "...";
                    }
                    setText((index + 1) + ". " + displayText);
                } else {
                    setText((index + 1) + ". [Không có nội dung]");
                }
                setToolTipText(dto.getNoiDungCauHoi()); 
            } else if (value == null) {
                setText((index + 1) + ". [Dữ liệu null]");
            } else {
                 setText((index + 1) + ". " + value.toString());
            }
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Không thể áp dụng System Look and Feel: " + e.getMessage());
            }
            ImageToTextFrame frame = new ImageToTextFrame();
            frame.setVisible(true);
        });
    }
}