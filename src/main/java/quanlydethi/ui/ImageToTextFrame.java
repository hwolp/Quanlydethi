package quanlydethi.ui;

import quanlydethi.dto.CauHoiTrichXuatDTO;
import quanlydethi.dto.LuaChonDTO;
import quanlydethi.model.CauHoi;
import quanlydethi.model.LuaChon;
import quanlydethi.service.ImageProcessingService;
import quanlydethi.dao.CauHoiDAO;
import quanlydethi.dao.LuaChonDAO;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException; // Thêm import này

public class ImageToTextFrame extends JFrame {

    // --- Phần UI cũ ---
    private JButton selectImageButton;
    private JLabel imageLabel;
    private JFileChooser fileChooser;
    private File selectedImageFile;
    private JTextField promptTextField;
    private JButton extractButton;

    // --- Phần UI mới cho hiển thị và chỉnh sửa câu hỏi ---
    private JList<CauHoiTrichXuatDTO> listExtractedQuestions;
    private DefaultListModel<CauHoiTrichXuatDTO> listModelExtractedQuestions;
    private JPanel questionEditPanel;
    private JTextArea txtDeBai;
    private JPanel pnlChoicesContainer;
    private JTextField txtDapAnDungKyHieu;
    private JTextArea txtGiaiThich;
    private JTextField txtLoaiCauHoiGoiY;
    private JTextField txtTrinhDoGoiY;
    private JButton btnSaveChangesToDB;
    private JButton btnSaveAllQuestionsToDB; // NÚT MỚI
    private JLabel lblSelectedQuestionInfo;

    private List<CauHoiTrichXuatDTO> currentExtractedDTOs;
    private CauHoiTrichXuatDTO currentEditingDTO;

    // --- Services và DAOs ---
    private ImageProcessingService imageService;
    private CauHoiDAO cauHoiDAO;
    private LuaChonDAO luaChonDAO;

    public ImageToTextFrame() {
        this.imageService = new ImageProcessingService();
        this.cauHoiDAO = new CauHoiDAO();
        this.luaChonDAO = new LuaChonDAO();
        this.currentExtractedDTOs = new ArrayList<>();

        setTitle("Trích Xuất & Chỉnh Sửa Câu Hỏi Từ Hình Ảnh");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        initFileChooser();
        initActionListeners();
    }

    private void initComponents() {
        // ... (Phần imageInputPanel giữ nguyên như trước) ...
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

        // === Panel Hiển thị Danh sách Câu hỏi Trích Xuất (Giữa Trên) ===
        // ... (Giữ nguyên như trước) ...
        JPanel listPanel = new JPanel(new BorderLayout(5,5));
        listPanel.setBorder(BorderFactory.createTitledBorder("3. Danh Sách Câu Hỏi Đã Trích Xuất"));
        listModelExtractedQuestions = new DefaultListModel<>();
        listExtractedQuestions = new JList<>(listModelExtractedQuestions);
        listExtractedQuestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listExtractedQuestions.setCellRenderer(new CauHoiDtoListCellRenderer());
        JScrollPane listScrollPane = new JScrollPane(listExtractedQuestions);
        listScrollPane.setPreferredSize(new Dimension(0, 200));
        listPanel.add(listScrollPane, BorderLayout.CENTER);
        lblSelectedQuestionInfo = new JLabel("Chọn một câu hỏi từ danh sách để chỉnh sửa.");
        listPanel.add(lblSelectedQuestionInfo, BorderLayout.SOUTH);


        // === Panel Chỉnh Sửa Chi Tiết Câu Hỏi (Giữa Dưới) ===
        // ... (Phần khai báo các trường txtDeBai, pnlChoicesContainer, ... giữ nguyên) ...
        questionEditPanel = new JPanel();
        questionEditPanel.setBorder(BorderFactory.createTitledBorder("4. Chỉnh Sửa Câu Hỏi Được Chọn"));
        questionEditPanel.setLayout(new GridBagLayout()); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        questionEditPanel.add(new JLabel("Đề Bài:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.9; gbc.gridwidth = 2;
        txtDeBai = new JTextArea(5, 30);
        txtDeBai.setLineWrap(true);
        txtDeBai.setWrapStyleWord(true);
        questionEditPanel.add(new JScrollPane(txtDeBai), gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        pnlChoicesContainer = new JPanel();
        pnlChoicesContainer.setLayout(new BoxLayout(pnlChoicesContainer, BoxLayout.Y_AXIS));
        pnlChoicesContainer.setBorder(BorderFactory.createTitledBorder("Các Lựa Chọn"));
        JScrollPane choicesScrollPane = new JScrollPane(pnlChoicesContainer);
        choicesScrollPane.setPreferredSize(new Dimension(0, 150)); 
        questionEditPanel.add(choicesScrollPane, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0.0;

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.1;
        questionEditPanel.add(new JLabel("Đáp Án Đúng (Ký Hiệu):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.9; gbc.gridwidth = 2;
        txtDapAnDungKyHieu = new JTextField(5);
        questionEditPanel.add(txtDapAnDungKyHieu, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 3;
        questionEditPanel.add(new JLabel("Giải Thích:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        txtGiaiThich = new JTextArea(3, 30);
        txtGiaiThich.setLineWrap(true);
        txtGiaiThich.setWrapStyleWord(true);
        questionEditPanel.add(new JScrollPane(txtGiaiThich), gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 4;
        questionEditPanel.add(new JLabel("Loại Câu Hỏi (AI):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        txtLoaiCauHoiGoiY = new JTextField(20);
        questionEditPanel.add(txtLoaiCauHoiGoiY, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        questionEditPanel.add(new JLabel("Trình Độ (AI):"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        txtTrinhDoGoiY = new JTextField(20);
        questionEditPanel.add(txtTrinhDoGoiY, gbc);

        // Panel chứa các nút Lưu
        JPanel saveButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnSaveChangesToDB = new JButton("Lưu Câu Hỏi Hiện Tại");
        btnSaveChangesToDB.setFont(new Font("Arial", Font.PLAIN, 14));
        btnSaveChangesToDB.setEnabled(false);
        
        btnSaveAllQuestionsToDB = new JButton("Lưu Tất Cả Câu Hỏi Đã Trích Xuất"); // NÚT MỚI
        btnSaveAllQuestionsToDB.setFont(new Font("Arial", Font.BOLD, 14));
        btnSaveAllQuestionsToDB.setEnabled(false); // Chỉ enable khi có danh sách câu hỏi

        saveButtonsPanel.add(btnSaveChangesToDB);
        saveButtonsPanel.add(btnSaveAllQuestionsToDB); // THÊM NÚT MỚI VÀO PANEL

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        questionEditPanel.add(saveButtonsPanel, gbc); // Thêm panel chứa các nút lưu
        
        disableEditingFields();

        // === Panel Chính ở giữa (gom danh sách và form chỉnh sửa) ===
        // ... (Giữ nguyên như trước) ...
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(listPanel, BorderLayout.NORTH);
        centerPanel.add(questionEditPanel, BorderLayout.CENTER);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imageInputPanel, centerPanel);
        mainSplitPane.setDividerLocation(410);
        add(mainSplitPane, BorderLayout.CENTER);
    }
    
    // ... (disableEditingFields, enableEditingFields, initFileChooser giữ nguyên) ...
    private void disableEditingFields() {
        txtDeBai.setEnabled(false);
        txtDapAnDungKyHieu.setEnabled(false);
        txtGiaiThich.setEnabled(false);
        txtLoaiCauHoiGoiY.setEnabled(false);
        txtTrinhDoGoiY.setEnabled(false);
        for (Component comp : pnlChoicesContainer.getComponents()) {
            if (comp instanceof JPanel) { 
                for(Component choiceComp : ((JPanel) comp).getComponents()){
                    choiceComp.setEnabled(false);
                }
            }
        }
         btnSaveChangesToDB.setEnabled(false);
    }

    private void enableEditingFields() {
        txtDeBai.setEnabled(true);
        txtDapAnDungKyHieu.setEnabled(true);
        txtGiaiThich.setEnabled(true);
        txtLoaiCauHoiGoiY.setEnabled(true);
        txtTrinhDoGoiY.setEnabled(true);
         for (Component comp : pnlChoicesContainer.getComponents()) {
            if (comp instanceof JPanel) {
                for(Component choiceComp : ((JPanel) comp).getComponents()){
                    choiceComp.setEnabled(true);
                }
            }
        }
        btnSaveChangesToDB.setEnabled(true);
    }

    private void initFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Tệp hình ảnh (JPG, PNG, GIF, WEBP)", "jpg", "jpeg", "png", "gif", "webp"));
        fileChooser.setAcceptAllFileFilterUsed(false);
    }


    private void initActionListeners() {
        selectImageButton.addActionListener(e -> chooseImageFile());
        extractButton.addActionListener(e -> startImageProcessing());
        listExtractedQuestions.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                // Trước khi load form mới, lưu lại thay đổi của DTO đang edit (nếu có)
                updateCurrentEditingDTOFromForm();
                onQuestionSelectedFromList();
            }
        });
        btnSaveChangesToDB.addActionListener(e -> saveCurrentQuestionToDB());
        btnSaveAllQuestionsToDB.addActionListener(e -> saveAllQuestionsToDB()); // SỰ KIỆN CHO NÚT MỚI
    }

    // ... (chooseImageFile, displayImageError giữ nguyên) ...
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
                    currentExtractedDTOs.clear(); // Xóa DTOs cũ
                    clearEditForm();
                    btnSaveChangesToDB.setEnabled(false);
                    btnSaveAllQuestionsToDB.setEnabled(false); // Vô hiệu hóa nút lưu tất cả
                    disableEditingFields();
                } else {
                    displayImageError("Không thể đọc tệp ảnh đã chọn.");
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
        // ... (Giữ nguyên như trước, nhưng cập nhật trạng thái nút "Lưu Tất Cả") ...
        if (selectedImageFile == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ảnh.", "Chưa chọn ảnh", JOptionPane.WARNING_MESSAGE);
            return;
        }
        extractButton.setEnabled(false);
        selectImageButton.setEnabled(false);
        btnSaveAllQuestionsToDB.setEnabled(false); // Vô hiệu hóa trong khi xử lý
        listModelExtractedQuestions.clear();
        currentExtractedDTOs.clear();
        clearEditForm();
        disableEditingFields();
        lblSelectedQuestionInfo.setText("Đang trích xuất câu hỏi từ ảnh...");

        String userPrompt = promptTextField.getText();

        SwingWorker<List<CauHoiTrichXuatDTO>, Void> worker = new SwingWorker<List<CauHoiTrichXuatDTO>, Void>() { // SỬA KIỂU GENERIC
            @Override
            protected List<CauHoiTrichXuatDTO> doInBackground() throws Exception {
                return imageService.extractQuestionsFromImage(selectedImageFile, userPrompt);
            }

            @Override
            protected void done() {
                try {
                    currentExtractedDTOs = get(); // get() trả về List<CauHoiTrichXuatDTO>
                    if (currentExtractedDTOs != null && !currentExtractedDTOs.isEmpty()) {
                        listModelExtractedQuestions.addAll(currentExtractedDTOs);
                        listExtractedQuestions.setSelectedIndex(0);
                        lblSelectedQuestionInfo.setText("Trích xuất hoàn tất. " + currentExtractedDTOs.size() + " câu hỏi. Chọn câu hỏi để sửa.");
                        btnSaveAllQuestionsToDB.setEnabled(true); // Kích hoạt nút "Lưu Tất Cả"
                    } else {
                        lblSelectedQuestionInfo.setText("Không trích xuất được câu hỏi nào hoặc AI trả về rỗng.");
                        JOptionPane.showMessageDialog(ImageToTextFrame.this,
                                "Không trích xuất được câu hỏi nào từ ảnh.",
                                "Kết Quả Trích Xuất", JOptionPane.INFORMATION_MESSAGE);
                        btnSaveAllQuestionsToDB.setEnabled(false);
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    lblSelectedQuestionInfo.setText("Lỗi trích xuất: " + cause.getMessage());
                    JOptionPane.showMessageDialog(ImageToTextFrame.this,
                        "Lỗi khi trích xuất: " + cause.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    cause.printStackTrace();
                    btnSaveAllQuestionsToDB.setEnabled(false);
                } finally {
                    extractButton.setEnabled(true);
                    selectImageButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
    
    // Hàm mới để cập nhật DTO hiện tại từ form trước khi chọn câu hỏi khác hoặc lưu
    private void updateCurrentEditingDTOFromForm() {
        if (currentEditingDTO != null) {
            currentEditingDTO.setNoiDungCauHoi(txtDeBai.getText());
            currentEditingDTO.setDapAnDungKyHieu(txtDapAnDungKyHieu.getText().trim().toUpperCase());
            currentEditingDTO.setGiaiThich(txtGiaiThich.getText());
            currentEditingDTO.setLoaiCauHoiGoiY(txtLoaiCauHoiGoiY.getText());
            currentEditingDTO.setTrinhDoGoiY(txtTrinhDoGoiY.getText());

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
                         // Cập nhật trực tiếp vào DTO trong list nếu có thể,
                         // hoặc tạo DTO mới nếu cấu trúc của bạn phức tạp hơn
                         // Ở đây ta cập nhật vào list mới updatedChoicesInForm
                        updatedChoicesInForm.add(new LuaChonDTO(khField.getText().trim(), ndField.getText().trim()));
                    }
                }
            }
            currentEditingDTO.setCacLuaChon(updatedChoicesInForm);
            // Cập nhật lại hiển thị trong JList nếu cần (nếu cell renderer phụ thuộc vào DTO đã thay đổi)
            listExtractedQuestions.repaint();
        }
    }


    private void onQuestionSelectedFromList() {
        // Lưu thay đổi của câu hỏi đang edit (nếu có) trước khi chuyển
        // Chức năng này cần được xem xét kỹ để tránh mất dữ liệu nếu người dùng không nhấn "Lưu Câu Hỏi Hiện Tại"
        // updateCurrentEditingDTOFromForm(); // Cân nhắc khi nào gọi hàm này

        currentEditingDTO = listExtractedQuestions.getSelectedValue();
        if (currentEditingDTO != null) {
            populateEditForm(currentEditingDTO);
            btnSaveChangesToDB.setEnabled(true);
            enableEditingFields();
            lblSelectedQuestionInfo.setText("Đang chỉnh sửa câu: " + (listExtractedQuestions.getSelectedIndex() + 1) + "/" + listModelExtractedQuestions.getSize());
        } else {
            clearEditForm();
            btnSaveChangesToDB.setEnabled(false);
            disableEditingFields();
            lblSelectedQuestionInfo.setText("Chọn một câu hỏi từ danh sách để chỉnh sửa.");
        }
    }

    // ... (populateEditForm, createChoiceEditPanel, clearEditForm giữ nguyên như trước) ...
    private void populateEditForm(CauHoiTrichXuatDTO dto) {
        txtDeBai.setText(dto.getNoiDungCauHoi());
        txtDapAnDungKyHieu.setText(dto.getDapAnDungKyHieu());
        txtGiaiThich.setText(dto.getGiaiThich() != null ? dto.getGiaiThich() : "");
        txtLoaiCauHoiGoiY.setText(dto.getLoaiCauHoiGoiY() != null ? dto.getLoaiCauHoiGoiY() : "");
        txtTrinhDoGoiY.setText(dto.getTrinhDoGoiY() != null ? dto.getTrinhDoGoiY() : "");

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
        JPanel choicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtKyHieu = new JTextField(lcDto.getKyHieu() != null ? lcDto.getKyHieu() : Character.toString((char) ('A' + index)), 3);
        JTextField txtNoiDung = new JTextField(lcDto.getNoiDung(), 30);
        
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
        txtLoaiCauHoiGoiY.setText("");
        txtTrinhDoGoiY.setText("");
        currentEditingDTO = null;
    }

    // --- Phương thức xử lý lưu một câu hỏi (giữ nguyên và cải thiện) ---
    private void saveCurrentQuestionToDB() {
        if (currentEditingDTO == null) {
            JOptionPane.showMessageDialog(this, "Không có câu hỏi nào được chọn để lưu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Cập nhật DTO từ form trước khi lưu
        updateCurrentEditingDTOFromForm();

        saveDtoToDatabase(currentEditingDTO, listExtractedQuestions.getSelectedIndex());
    }

    // --- PHƯƠNG THỨC MỚI ĐỂ LƯU TẤT CẢ ---
    private void saveAllQuestionsToDB() {
        if (currentExtractedDTOs == null || currentExtractedDTOs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có câu hỏi nào để lưu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Cập nhật DTO đang hiển thị trên form (nếu có) vào danh sách trước khi lưu tất cả
        if (currentEditingDTO != null && listExtractedQuestions.getSelectedValue() == currentEditingDTO) {
            updateCurrentEditingDTOFromForm();
        }


        btnSaveAllQuestionsToDB.setEnabled(false);
        btnSaveChangesToDB.setEnabled(false); // Vô hiệu hóa cả nút lưu đơn lẻ
        lblSelectedQuestionInfo.setText("Đang lưu tất cả câu hỏi vào CSDL...");

        SwingWorker<String, Integer> worker = new SwingWorker<String, Integer>() {
            private List<CauHoiTrichXuatDTO> successfullySavedDTOs = new ArrayList<>();
            private List<String> errorMessages = new ArrayList<>();
            private int totalToSave;

            @Override
            protected String doInBackground() throws Exception {
                totalToSave = currentExtractedDTOs.size();
                int savedCount = 0;
                // Tạo một bản sao của danh sách để tránh ConcurrentModificationException nếu ta xóa phần tử
                List<CauHoiTrichXuatDTO> dtosToProcess = new ArrayList<>(currentExtractedDTOs);

                for (int i = 0; i < dtosToProcess.size(); i++) {
                    CauHoiTrichXuatDTO dto = dtosToProcess.get(i);
                    publish(i + 1); // Gửi tiến trình (số câu hỏi đang xử lý)
                    try {
                        boolean success = processAndSaveSingleDto(dto); // Hàm helper để xử lý lưu 1 DTO
                        if (success) {
                            savedCount++;
                            successfullySavedDTOs.add(dto); // Thêm vào danh sách đã lưu thành công
                        } else {
                            errorMessages.add("Không thể lưu câu hỏi: " + dto.getNoiDungCauHoi().substring(0, Math.min(dto.getNoiDungCauHoi().length(), 30)) + "...");
                        }
                    } catch (Exception e) {
                        errorMessages.add("Lỗi khi lưu câu hỏi '" + dto.getNoiDungCauHoi().substring(0, Math.min(dto.getNoiDungCauHoi().length(), 30)) + "...': " + e.getMessage());
                        e.printStackTrace();
                    }
                    if (isCancelled()) break;
                }
                return "Đã xử lý " + totalToSave + " câu hỏi. Thành công: " + savedCount + ". Thất bại: " + errorMessages.size();
            }

            @Override
            protected void process(List<Integer> chunks) {
                // Cập nhật UI với tiến trình, ví dụ: câu hỏi thứ X / Y
                for (Integer number : chunks) {
                    lblSelectedQuestionInfo.setText("Đang lưu câu hỏi " + number + "/" + totalToSave + "...");
                }
            }

            @Override
            protected void done() {
                try {
                    String resultMessage = get();
                    StringBuilder finalMessage = new StringBuilder(resultMessage);
                    if (!errorMessages.isEmpty()) {
                        finalMessage.append("\n\nChi tiết lỗi:\n");
                        for (String error : errorMessages) {
                            finalMessage.append("- ").append(error).append("\n");
                        }
                    }
                    JOptionPane.showMessageDialog(ImageToTextFrame.this, finalMessage.toString(), "Kết Quả Lưu Tất Cả", JOptionPane.INFORMATION_MESSAGE);

                    // Xóa các DTO đã lưu thành công khỏi danh sách chính và UI
                    currentExtractedDTOs.removeAll(successfullySavedDTOs);
                    for(CauHoiTrichXuatDTO savedDto : successfullySavedDTOs){
                        listModelExtractedQuestions.removeElement(savedDto);
                    }
                    
                    if (listModelExtractedQuestions.isEmpty()) {
                        clearEditForm();
                        disableEditingFields();
                        btnSaveChangesToDB.setEnabled(false);
                        btnSaveAllQuestionsToDB.setEnabled(false);
                        lblSelectedQuestionInfo.setText("Đã lưu tất cả câu hỏi. Không còn câu hỏi nào trong danh sách.");
                    } else {
                        listExtractedQuestions.setSelectedIndex(0); // Chọn câu đầu tiên còn lại (nếu có)
                        btnSaveAllQuestionsToDB.setEnabled(true); // Kích hoạt lại nếu còn câu hỏi
                    }

                } catch (InterruptedException | ExecutionException e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(ImageToTextFrame.this, "Lỗi trong quá trình lưu tất cả: " + cause.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    cause.printStackTrace();
                } finally {
                    // Kích hoạt lại các nút tùy theo trạng thái
                    selectImageButton.setEnabled(true);
                    extractButton.setEnabled(true);
                    if(!listModelExtractedQuestions.isEmpty()){
                        btnSaveAllQuestionsToDB.setEnabled(true);
                        if(listExtractedQuestions.getSelectedIndex() != -1){
                            btnSaveChangesToDB.setEnabled(true);
                        }
                    }
                }
            }
        };
        worker.execute();
    }

    // Hàm helper để xử lý việc lưu một DTO (tương tự saveCurrentQuestionToDB nhưng trả về boolean)
    private boolean processAndSaveSingleDto(CauHoiTrichXuatDTO dto) throws Exception {
        CauHoi cauHoiToSave = new CauHoi();
        cauHoiToSave.setNoiDungCauHoi(dto.getNoiDungCauHoi());

        // TODO: Cần logic chuẩn để map gợi ý từ AI (String) sang ID (int) từ CSDL
        // Ví dụ: int maLoai = getMaLoaiCauHoiFromDB(dto.getLoaiCauHoiGoiY());
        // Tạm thời dùng giá trị cố định cho demo
        cauHoiToSave.setMaLoaiCauHoi(1); // GIẢ SỬ ID=1
        cauHoiToSave.setMaTrinhDo(1);   // GIẢ SỬ ID=1
        cauHoiToSave.setDiem(1);        // Điểm mặc định

        boolean cauHoiSaved = cauHoiDAO.addCauHoi(cauHoiToSave);
        if (cauHoiSaved && cauHoiToSave.getMaCauHoi() > 0) {
            int savedMaCauHoi = cauHoiToSave.getMaCauHoi();
            for (LuaChonDTO lcDto : dto.getCacLuaChon()) {
                LuaChon luaChonToSave = new LuaChon();
                luaChonToSave.setMaCauHoi(savedMaCauHoi);
                String noiDungLc = (lcDto.getKyHieu() != null && !lcDto.getKyHieu().isEmpty() ? lcDto.getKyHieu() + ". " : "") + lcDto.getNoiDung();
                luaChonToSave.setNoiDungLuaChon(noiDungLc);
                luaChonToSave.setLaDapAnDung(lcDto.getKyHieu() != null && lcDto.getKyHieu().equalsIgnoreCase(dto.getDapAnDungKyHieu()));
                luaChonDAO.addLuaChon(luaChonToSave); // Giả sử không cần kiểm tra kết quả lưu từng lựa chọn
            }
            return true;
        }
        return false;
    }
    
    // Hàm saveDtoToDatabase dùng cho nút "Lưu Câu Hỏi Hiện Tại"
    private void saveDtoToDatabase(CauHoiTrichXuatDTO dtoToSave, int dtoIndexInList) {
        // Tạm thời vô hiệu hóa các nút lưu để tránh double click
        btnSaveChangesToDB.setEnabled(false);
        btnSaveAllQuestionsToDB.setEnabled(false);

        SwingWorker<Boolean, Void> saveWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return processAndSaveSingleDto(dtoToSave);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(ImageToTextFrame.this,
                                "Đã lưu câu hỏi '" + dtoToSave.getNoiDungCauHoi().substring(0, Math.min(30, dtoToSave.getNoiDungCauHoi().length())) + "...' vào CSDL.",
                                "Lưu Thành Công", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Xóa DTO đã lưu khỏi danh sách và JList
                        if (dtoIndexInList >= 0 && dtoIndexInList < currentExtractedDTOs.size() && currentExtractedDTOs.get(dtoIndexInList) == dtoToSave) {
                            currentExtractedDTOs.remove(dtoIndexInList);
                            listModelExtractedQuestions.remove(dtoIndexInList);
                        } else { // Nếu index không hợp lệ hoặc DTO không khớp (ít xảy ra)
                            currentExtractedDTOs.remove(dtoToSave);
                            listModelExtractedQuestions.removeElement(dtoToSave);
                        }

                        if (!listModelExtractedQuestions.isEmpty()) {
                            listExtractedQuestions.setSelectedIndex(0); // Chọn câu đầu tiên
                        } else {
                            clearEditForm();
                            disableEditingFields();
                            lblSelectedQuestionInfo.setText("Đã lưu câu hỏi. Không còn câu hỏi nào.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(ImageToTextFrame.this, "Lưu câu hỏi vào CSDL thất bại.", "Lỗi Lưu", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ImageToTextFrame.this, "Lỗi khi lưu vào CSDL: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } finally {
                    // Kích hoạt lại các nút tùy theo trạng thái
                    btnSaveChangesToDB.setEnabled(!listModelExtractedQuestions.isEmpty() && listExtractedQuestions.getSelectedIndex() != -1);
                    btnSaveAllQuestionsToDB.setEnabled(!listModelExtractedQuestions.isEmpty());
                }
            }
        };
        saveWorker.execute();
    }


    // Custom ListCellRenderer giữ nguyên
    class CauHoiDtoListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof CauHoiTrichXuatDTO) {
                CauHoiTrichXuatDTO dto = (CauHoiTrichXuatDTO) value;
                String displayText = dto.getNoiDungCauHoi();
                if (displayText != null && displayText.length() > 80) {
                    displayText = displayText.substring(0, 77) + "...";
                }
                setText((index + 1) + ". " + displayText);
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