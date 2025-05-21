package quanlydethi.service;

import quanlydethi.model.CauHoi;
import quanlydethi.model.DeThi;
import quanlydethi.model.LuaChon;
import quanlydethi.model.TrinhDo;
import quanlydethi.model.DoanVanDoc; // IMPORT DoanVanDoc model
import quanlydethi.model.TepAmThanh;
import quanlydethi.dao.CauHoiDAO;
import quanlydethi.dao.LuaChonDAO;
import quanlydethi.dao.CauHoiTrongDeThiDAO;
import quanlydethi.dao.TrinhDoDAO;
import quanlydethi.dao.TepAmThanhDAO;
import quanlydethi.dao.DoanVanDocDAO; // IMPORT DoanVanDocDAO

// Imports cho OpenHTMLToPDF
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;
import com.openhtmltopdf.extend.FSSupplier;

// Imports cho Apache POI (DOCX)
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

// Import Jsoup để parse HTML string thành W3C Document
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;


public class ExportDeThiService {
    private CauHoiDAO cauHoiDAO;
    private LuaChonDAO luaChonDAO;
    private CauHoiTrongDeThiDAO cauHoiTrongDeThiDAO;
    private TrinhDoDAO trinhDoDAO;
    private TepAmThanhDAO tepAmThanhDAO;
    private DoanVanDocDAO doanVanDocDAO; // Thêm DAO cho Đoạn Văn Đọc

    private final String FONT_RESOURCE_PATH = "/fonts/NotoSansJP.ttf";
    private final String FONT_FAMILY_CSS_NAME = "MyUnicodeFontFamily";

    public ExportDeThiService() {
        this.cauHoiDAO = new CauHoiDAO();
        this.luaChonDAO = new LuaChonDAO();
        this.cauHoiTrongDeThiDAO = new CauHoiTrongDeThiDAO();
        this.trinhDoDAO = new TrinhDoDAO();
        this.tepAmThanhDAO = new TepAmThanhDAO();
        this.doanVanDocDAO = new DoanVanDocDAO(); // Khởi tạo DAO
        XRLog.listRegisteredLoggers().forEach(logger -> XRLog.setLevel(logger, Level.WARNING));
    }

    private String getTenTrinhDo(Integer maTrinhDo) {
        // ... (Giữ nguyên)
        if (maTrinhDo == null) return "N/A";
        TrinhDo td = trinhDoDAO.getTrinhDoById(maTrinhDo);
        if (td != null && td.getTenTrinhDo() != null) {
            return Normalizer.normalize(td.getTenTrinhDo(), Normalizer.Form.NFC);
        }
        return "N/A (Mã: " + maTrinhDo + ")";
    }

    private String getBaseCss() {
        // Thêm style cho đoạn văn
        return "<style>\n" +
               "  body { font-family: '" + FONT_FAMILY_CSS_NAME + "', sans-serif; font-size: 11pt; line-height: 1.5; }\n" +
               "  h1 { text-align: center; font-size: 16pt; margin-bottom: 10px; }\n" +
               "  h2 { text-align: center; font-size: 14pt; margin-top: 10px; margin-bottom: 10px; }\n" +
               "  h3.section-title { font-size: 13pt; font-weight: bold; margin-top: 20px; margin-bottom: 10px; border-bottom: 1px solid #000; padding-bottom: 5px; }\n" +
               "  .exam-info { margin-bottom: 15px; padding: 10px; border: 1px solid #ccc; background-color: #f9f9f9; }\n" +
               "  .exam-info p { margin: 4px 0; }\n" +
               "  .reading-passage { margin-top: 10px; margin-bottom:10px; padding:10px; border: 1px solid #e0e0e0; background-color:#fdfdfd; text-align: justify; }\n" + // Style cho đoạn văn
               "  .question-block { margin-bottom: 12px; padding-left: 5px; page-break-inside: avoid; border-bottom: 1px dotted #eee; padding-bottom: 8px;}\n" +
               "  .question-content { font-weight: bold; margin-bottom: 5px; }\n" +
               "  .choices { margin-left: 20px; }\n" +
               "  .choice-item { margin-bottom: 3px; }\n" +
               "  .correct-answer-text { font-weight: bold; color: #006400; }\n" +
               "  .audio-info { font-style: italic; color: #555; font-size: 0.9em; margin-left: 20px; }\n" +
               "  hr { margin-top:15px; margin-bottom:15px; border: 0; border-top: 1px solid #ccc; }\n"+
               "</style>\n";
    }

    private String normalizeAndEscape(String text) {
        // ... (Giữ nguyên)
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFC);
        return escapeHtml(normalized);
    }
    
    private String escapeHtml(String text) {
        // ... (Giữ nguyên)
         if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private void appendHtmlHeader(StringBuilder html, DeThi deThi, String fileType) {
        // ... (Giữ nguyên)
        html.append("<!DOCTYPE html><html lang=\"vi\"><head><meta charset=\"UTF-8\">");
        html.append(getBaseCss());
        String titlePrefix = "Đề Thi";
        if ("DapAn".equals(fileType)) {
            titlePrefix = "Đáp Án - Đề Thi";
        }
        html.append("<title>").append(titlePrefix).append(": ").append(normalizeAndEscape(deThi.getTenDeThi())).append("</title></head><body>");
        if ("DapAn".equals(fileType)) {
            html.append("<h1>ĐÁP ÁN CHI TIẾT</h1>");
        } else {
            html.append("<h1>ĐỀ THI</h1>");
        }
        html.append("<h2>").append(normalizeAndEscape(deThi.getTenDeThi())).append("</h2>");
    }

    private void appendExamInfoToHtml(StringBuilder html, DeThi deThi) {
        // ... (Giữ nguyên)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        html.append("<div class='exam-info'>");
        html.append("<p><strong>Mã Đề:</strong> ").append(deThi.getMaDeThi()).append("</p>");
        html.append("<p><strong>Trình Độ:</strong> ").append(escapeHtml(getTenTrinhDo(deThi.getMaTrinhDo()))).append("</p>");
        if (deThi.getNgayTaoDe() != null) {
             html.append("<p><strong>Ngày Tạo:</strong> ").append(sdf.format(deThi.getNgayTaoDe())).append("</p>");
        }
        if (deThi.getThoiGianLamBaiPhut() != null && deThi.getThoiGianLamBaiPhut() > 0) {
            html.append("<p><strong>Thời gian làm bài:</strong> ").append(deThi.getThoiGianLamBaiPhut()).append(" phút</p>");
        } else {
            html.append("<p><strong>Thời gian làm bài:</strong> Không giới hạn</p>");
        }
        html.append("</div><hr>");
    }

    // CẬP NHẬT HÀM NÀY
    private int appendQuestionsToHtml(StringBuilder html, List<CauHoi> questions, boolean isForDapAnFile, int startingQuestionNumber) {
        int currentQuestionNumber = startingQuestionNumber;
        Integer lastPrintedMaDoanVan = null; // Để theo dõi đoạn văn đã in

        for (CauHoi ch : questions) {
            // In đoạn văn nếu có và chưa được in cho nhóm câu hỏi này
            if (!isForDapAnFile && ch.getMaDoanVan() != null && ch.getMaDoanVan() > 0) {
                if (lastPrintedMaDoanVan == null || !lastPrintedMaDoanVan.equals(ch.getMaDoanVan())) {
                    DoanVanDoc dv = doanVanDocDAO.getDoanVanDocById(ch.getMaDoanVan());
                    if (dv != null && dv.getNoiDungDoanVan() != null) {
                        html.append("<div class='reading-passage'>")
                            .append(normalizeAndEscape(dv.getNoiDungDoanVan()).replace("\n", "<br/>")) // Thay \n bằng <br/> để xuống dòng trong HTML
                            .append("</div>");
                    }
                    lastPrintedMaDoanVan = ch.getMaDoanVan();
                }
            } else {
                lastPrintedMaDoanVan = null; // Reset nếu câu hỏi không có đoạn văn
            }

            String normalizedNoiDungCauHoi = Normalizer.normalize(ch.getNoiDungCauHoi(), Normalizer.Form.NFC);
            html.append("<div class='question-block'>");
            
            if (isForDapAnFile) {
                 html.append("<p><strong>Câu ").append(currentQuestionNumber++).append(":</strong> ")
                    .append(escapeHtml(normalizedNoiDungCauHoi.substring(0, Math.min(normalizedNoiDungCauHoi.length(), 70)) + (normalizedNoiDungCauHoi.length() > 70 ? "..." : "")))
                    .append("</p>");
                if (ch.getMaAmThanh() != null && ch.getMaAmThanh() > 0) {
                    TepAmThanh audio = tepAmThanhDAO.getTepAmThanhById(ch.getMaAmThanh());
                    if (audio != null) {
                        html.append("<p class='audio-info'>Tệp âm thanh: ").append(normalizeAndEscape(audio.getTenTep()))
                            .append(" (Đường dẫn: ").append(normalizeAndEscape(audio.getDuongDanTep())).append(")</p>");
                    }
                }
                List<LuaChon> luaChons = luaChonDAO.getLuaChonByMaCauHoi(ch.getMaCauHoi());
                String dapAnDungText = "N/A";
                if (luaChons != null) {
                    for (LuaChon lc : luaChons) {
                        if (lc.isLaDapAnDung()) {
                            dapAnDungText = Normalizer.normalize(lc.getNoiDungLuaChon(), Normalizer.Form.NFC);
                            if (dapAnDungText.matches("^[A-Za-z0-9][.)][\\s\\S]*")){
                                 int dotIndex = dapAnDungText.indexOf("."); int bracketIndex = dapAnDungText.indexOf(")");
                                 int splitIndex = -1;
                                 if(dotIndex != -1 && bracketIndex != -1) splitIndex = Math.min(dotIndex, bracketIndex);
                                 else if (dotIndex != -1) splitIndex = dotIndex; else if (bracketIndex != -1) splitIndex = bracketIndex;
                                 if (splitIndex != -1 && splitIndex < 5) dapAnDungText = dapAnDungText.substring(0, splitIndex + 1).trim();
                            }
                            break;
                        }
                    }
                }
                html.append("<p><strong>Đáp án:</strong> <span class='correct-answer-text'>")
                    .append(escapeHtml(dapAnDungText)).append("</span></p>");
            } else { // For DeThi file
                html.append("<p class='question-content'><strong>Câu ").append(currentQuestionNumber++).append(":</strong> ")
                    .append(normalizeAndEscape(ch.getNoiDungCauHoi())).append(" (").append(ch.getDiem()).append(" điểm)</p>");
                if (ch.getMaAmThanh() != null && ch.getMaAmThanh() > 0) {
                     TepAmThanh audio = tepAmThanhDAO.getTepAmThanhById(ch.getMaAmThanh());
                     if (audio != null) {
                         html.append("<p class='audio-info'><em>(Tham khảo tệp âm thanh: ").append(normalizeAndEscape(audio.getTenTep())).append(")</em></p>");
                     }
                }
                List<LuaChon> luaChons = luaChonDAO.getLuaChonByMaCauHoi(ch.getMaCauHoi());
                if (luaChons != null && !luaChons.isEmpty()) {
                    html.append("<div class='choices'>");
                    for (LuaChon lc : luaChons) {
                        html.append("<p class='choice-item'>").append(normalizeAndEscape(lc.getNoiDungLuaChon())).append("</p>");
                    }
                    html.append("</div>");
                }
            }
            html.append("</div>");
        }
        return currentQuestionNumber;
    }

    // generateHtml (đã đổi tên từ generateHtmlForDeThi/DapAn cũ)
    private String generateHtml(DeThi deThi, boolean isForDapAnFile, List<CauHoi> docHieuQuestions, List<CauHoi> ngheHieuQuestions, List<CauHoi> khacQuestions) {
        // ... (Giữ nguyên logic gọi appendHtmlHeader, appendExamInfoToHtml, và appendQuestionsToHtml cho từng phần)
        StringBuilder html = new StringBuilder();
        appendHtmlHeader(html, deThi, isForDapAnFile ? "DapAn" : "DeThi");
        appendExamInfoToHtml(html, deThi);

        int questionCounter = 1;
        int sectionCounter = 1;

        if (!docHieuQuestions.isEmpty()) {
            html.append("<h3 class='section-title'>PHẦN ").append(sectionCounter++).append(": BÀI TẬP ĐỌC HIỂU</h3>");
            questionCounter = appendQuestionsToHtml(html, docHieuQuestions, isForDapAnFile, questionCounter);
        }
        if (!ngheHieuQuestions.isEmpty()) {
            html.append("<h3 class='section-title'>PHẦN ").append(sectionCounter++).append(": BÀI TẬP NGHE HIỂU</h3>");
            questionCounter = appendQuestionsToHtml(html, ngheHieuQuestions, isForDapAnFile, questionCounter);
        }
        if (!khacQuestions.isEmpty()) {
            html.append("<h3 class='section-title'>PHẦN ").append(sectionCounter++).append(": BÀI TẬP KHÁC</h3>");
            appendQuestionsToHtml(html, khacQuestions, isForDapAnFile, questionCounter);
        }
        html.append("</body></html>");
        return html.toString();
    }

    public void exportDeThiToPdfViaHtml(DeThi deThi, File fileDeThi, File fileDapAn) throws Exception {
        // ... (Phần phân loại câu hỏi giữ nguyên) ...
        List<Integer> maCauHoiList = cauHoiTrongDeThiDAO.getMaCauHoiByMaDeThi(deThi.getMaDeThi());
        List<CauHoi> tatCaCauHoiCuaDe = maCauHoiList.stream()
                                               .map(maCH -> cauHoiDAO.getCauHoiById(maCH))
                                               .filter(ch -> ch != null)
                                               .collect(Collectors.toList());

        List<CauHoi> docHieuQuestions = new ArrayList<>();
        List<CauHoi> ngheHieuQuestions = new ArrayList<>();
        List<CauHoi> khacQuestions = new ArrayList<>();

        for (CauHoi ch : tatCaCauHoiCuaDe) {
            if (ch.getMaDoanVan() != null && ch.getMaDoanVan() > 0) {
                docHieuQuestions.add(ch);
            } else if (ch.getMaAmThanh() != null && ch.getMaAmThanh() > 0) {
                ngheHieuQuestions.add(ch);
            } else {
                khacQuestions.add(ch);
            }
        }

        String htmlDeThiString = generateHtml(deThi, false, docHieuQuestions, ngheHieuQuestions, khacQuestions);
        String htmlDapAnString = generateHtml(deThi, true, docHieuQuestions, ngheHieuQuestions, khacQuestions);

        W3CDom w3cDom = new W3CDom();
        Document domDeThi = w3cDom.fromJsoup(Jsoup.parse(htmlDeThiString));
        Document domDapAn = w3cDom.fromJsoup(Jsoup.parse(htmlDapAnString));
        
        FSSupplier<InputStream> fontSupplier = () -> ExportDeThiService.class.getResourceAsStream(FONT_RESOURCE_PATH);
        
        try (InputStream testFontStream = ExportDeThiService.class.getResourceAsStream(FONT_RESOURCE_PATH)) {
            if (testFontStream == null) {
                String errorMsg = "LỖI CRITICAL: Không thể tìm thấy file font tại: " + FONT_RESOURCE_PATH + ". PDF sẽ không thể hiển thị đúng.";
                System.err.println(errorMsg);
                throw new IOException(errorMsg);
            }
        }
        
        // Xuất file Đề Thi
        try (OutputStream osDeThi = new FileOutputStream(fileDeThi)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(fontSupplier, FONT_FAMILY_CSS_NAME);
            builder.withW3cDocument(domDeThi, new File(".").toURI().toString()); 
            builder.toStream(osDeThi);
            builder.run();
            System.out.println("Đã xuất Đề Thi PDF tới: " + fileDeThi.getAbsolutePath());
        } // try-with-resources sẽ tự đóng osDeThi

        // Xuất file Đáp Án
        try (OutputStream osDapAn = new FileOutputStream(fileDapAn)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // Cần cung cấp lại fontSupplier cho mỗi instance của builder
            FSSupplier<InputStream> fontSupplierDapAn = () -> ExportDeThiService.class.getResourceAsStream(FONT_RESOURCE_PATH);
            builder.useFont(fontSupplierDapAn, FONT_FAMILY_CSS_NAME); 
            builder.withW3cDocument(domDapAn, new File(".").toURI().toString());
            builder.toStream(osDapAn);
            builder.run();
            System.out.println("Đã xuất Đáp Án PDF tới: " + fileDapAn.getAbsolutePath());
        } // try-with-resources sẽ tự đóng osDapAn
    }
    
    // --- DOCX Generation Helpers ---
    private void addSectionTitleToDocx(XWPFDocument doc, String title) {
        // ... (Giữ nguyên)
        XWPFParagraph para = doc.createParagraph();
        para.setAlignment(ParagraphAlignment.LEFT); // Hoặc CENTER
        XWPFRun run = para.createRun();
        run.setBold(true);
        run.setFontSize(14); // Có thể tăng giảm
        run.setText(Normalizer.normalize(title, Normalizer.Form.NFC));
        // run.addBreak(); // Thêm dòng trống sau tiêu đề nếu muốn
    }

    // CẬP NHẬT HÀM NÀY
    private int appendQuestionsToDocx(XWPFDocument doc, List<CauHoi> questions, boolean isForDapAnFile, int startingQuestionNumber) {
        int currentQuestionNumber = startingQuestionNumber;
        Integer lastPrintedMaDoanVan = null;

        for (CauHoi ch : questions) {
            // In đoạn văn nếu có và chưa được in cho nhóm câu hỏi này (chỉ cho file đề thi)
            if (!isForDapAnFile && ch.getMaDoanVan() != null && ch.getMaDoanVan() > 0) {
                if (lastPrintedMaDoanVan == null || !lastPrintedMaDoanVan.equals(ch.getMaDoanVan())) {
                    DoanVanDoc dv = doanVanDocDAO.getDoanVanDocById(ch.getMaDoanVan());
                    if (dv != null && dv.getNoiDungDoanVan() != null) {
                        // Thêm một paragraph cho đoạn văn, có thể định dạng khác
                        XWPFParagraph pDoanVan = doc.createParagraph();
                        pDoanVan.setBorderBottom(Borders.SINGLE); // Ví dụ thêm đường kẻ dưới
                        pDoanVan.setSpacingAfter(200); // Khoảng cách sau đoạn văn
                        XWPFRun runDoanVan = pDoanVan.createRun();
                        //runDoanVan.setFontStyle("italic"); // In nghiêng
                        // Xử lý xuống dòng cho DOCX
                        String[] lines = Normalizer.normalize(dv.getNoiDungDoanVan(), Normalizer.Form.NFC).split("\\r?\\n");
                        for(int i=0; i<lines.length; i++){
                            runDoanVan.setText(lines[i]);
                            if(i < lines.length -1) runDoanVan.addBreak();
                        }
                        runDoanVan.addBreak(); // Thêm dòng trống sau đoạn văn
                    }
                    lastPrintedMaDoanVan = ch.getMaDoanVan();
                }
            } else {
                lastPrintedMaDoanVan = null;
            }


            String questionTextHeader = "Câu " + currentQuestionNumber++ + ": ";
            String questionFullText = Normalizer.normalize(ch.getNoiDungCauHoi(), Normalizer.Form.NFC);
            
            if (isForDapAnFile) {
                 String noiDungRutGon = questionFullText.substring(0, Math.min(questionFullText.length(), 70)) + (questionFullText.length() > 70 ? "..." : "");
                 addParagraphToDocx(doc, questionTextHeader + noiDungRutGon);
            } else {
                 addParagraphToDocx(doc, questionTextHeader + questionFullText + " (" + ch.getDiem() + " điểm)");
            }


            // Thêm thông tin audio nếu là câu hỏi nghe (cho cả đề và đáp án)
            if (ch.getMaAmThanh() != null && ch.getMaAmThanh() > 0) {
                TepAmThanh audio = tepAmThanhDAO.getTepAmThanhById(ch.getMaAmThanh());
                if (audio != null) {
                    String audioInfo = "  (Tệp âm thanh: " + Normalizer.normalize(audio.getTenTep(), Normalizer.Form.NFC);
                    if (isForDapAnFile) { // Chỉ hiển thị đường dẫn trong file đáp án nếu cần
                        audioInfo += ", Đường dẫn: " + Normalizer.normalize(audio.getDuongDanTep(), Normalizer.Form.NFC);
                    }
                    audioInfo += ")";
                    XWPFRun audioRun = doc.createParagraph().createRun();
                   // audioRun.setFontStyle("italic");
                    audioRun.setText(audioInfo);
                }
            }

            List<LuaChon> luaChons = luaChonDAO.getLuaChonByMaCauHoi(ch.getMaCauHoi());
            if (isForDapAnFile) {
                String dapAnDungText = "N/A";
                if (luaChons != null) {
                    for (LuaChon lc : luaChons) {
                        if (lc.isLaDapAnDung()) {
                            dapAnDungText = Normalizer.normalize(lc.getNoiDungLuaChon(), Normalizer.Form.NFC);
                            if (dapAnDungText.matches("^[A-Za-z0-9][.)][\\s\\S]*")){
                                 int dotIndex = dapAnDungText.indexOf("."); int bracketIndex = dapAnDungText.indexOf(")");
                                 int splitIndex = -1;
                                 if(dotIndex != -1 && bracketIndex != -1) splitIndex = Math.min(dotIndex, bracketIndex);
                                 else if (dotIndex != -1) splitIndex = dotIndex; else if (bracketIndex != -1) splitIndex = bracketIndex;
                                 if (splitIndex != -1 && splitIndex < 5) dapAnDungText = dapAnDungText.substring(0, splitIndex + 1).trim();
                            }
                            break;
                        }
                    }
                }
                XWPFRun dapAnRun = doc.createParagraph().createRun();
                dapAnRun.setText("  Đáp án: ");
                XWPFRun dapAnValueRun = dapAnRun.getDocument().getParagraphs().get(dapAnRun.getDocument().getParagraphs().size()-1).createRun(); // Lấy run mới để style
                dapAnValueRun.setBold(true);
                dapAnValueRun.setColor("006400"); // DarkGreen
                dapAnValueRun.setText(dapAnDungText);

            } else { // For DeThi file
                if (luaChons != null && !luaChons.isEmpty()) {
                    for (LuaChon lc : luaChons) {
                        addParagraphToDocx(doc, "  " + Normalizer.normalize(lc.getNoiDungLuaChon(), Normalizer.Form.NFC));
                    }
                }
            }
            doc.createParagraph().createRun().addBreak(); // Dòng trống sau mỗi câu hỏi
        }
        return currentQuestionNumber;
    }

    public void exportDeThiToDocx(DeThi deThi, File fileDeThi, File fileDapAn) throws IOException {
        // ... (Phần phân loại câu hỏi giữ nguyên) ...
        List<Integer> maCauHoiList = cauHoiTrongDeThiDAO.getMaCauHoiByMaDeThi(deThi.getMaDeThi());
        List<CauHoi> tatCaCauHoiCuaDe = maCauHoiList.stream()
                                               .map(maCH -> cauHoiDAO.getCauHoiById(maCH))
                                               .filter(ch -> ch != null)
                                               .collect(Collectors.toList());

        List<CauHoi> docHieuQuestions = new ArrayList<>();
        List<CauHoi> ngheHieuQuestions = new ArrayList<>();
        List<CauHoi> khacQuestions = new ArrayList<>();

        for (CauHoi ch : tatCaCauHoiCuaDe) {
            if (ch.getMaDoanVan() != null && ch.getMaDoanVan() > 0) {
                docHieuQuestions.add(ch);
            } else if (ch.getMaAmThanh() != null && ch.getMaAmThanh() > 0) {
                ngheHieuQuestions.add(ch);
            } else {
                khacQuestions.add(ch);
            }
        }
        
        // Tạo file Đề Thi DOCX
        try (XWPFDocument docDeThi = new XWPFDocument()) {
            // ... (Tạo tiêu đề và thông tin chung như cũ) ...
            XWPFParagraph titleDeThiP = docDeThi.createParagraph();
            titleDeThiP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRunDeThi = titleDeThiP.createRun();
            titleRunDeThi.setBold(true); titleRunDeThi.setFontSize(16);
            titleRunDeThi.setText("ĐỀ THI: " + Normalizer.normalize(deThi.getTenDeThi().toUpperCase(), Normalizer.Form.NFC));
            titleRunDeThi.addBreak();

            addParagraphToDocx(docDeThi, "Trình độ: " + getTenTrinhDo(deThi.getMaTrinhDo()));
            if (deThi.getThoiGianLamBaiPhut() != null && deThi.getThoiGianLamBaiPhut() > 0) {
                addParagraphToDocx(docDeThi, "Thời gian làm bài: " + deThi.getThoiGianLamBaiPhut() + " phút");
            } else {
                 addParagraphToDocx(docDeThi, "Thời gian làm bài: Không giới hạn");
            }
            addParagraphToDocx(docDeThi, "------------------------------------");
            docDeThi.createParagraph().createRun().addBreak(); 

            int questionCounter = 1;
            int sectionCounter = 1;
            if (!docHieuQuestions.isEmpty()) {
                addSectionTitleToDocx(docDeThi, "PHẦN " + sectionCounter++ + ": BÀI TẬP ĐỌC HIỂU");
                questionCounter = appendQuestionsToDocx(docDeThi, docHieuQuestions, false, questionCounter);
            }
            if (!ngheHieuQuestions.isEmpty()) {
                addSectionTitleToDocx(docDeThi, "PHẦN " + sectionCounter++ + ": BÀI TẬP NGHE HIỂU");
                questionCounter = appendQuestionsToDocx(docDeThi, ngheHieuQuestions, false, questionCounter);
            }
            if (!khacQuestions.isEmpty()) {
                addSectionTitleToDocx(docDeThi, "PHẦN " + sectionCounter++ + ": BÀI TẬP KHÁC");
                appendQuestionsToDocx(docDeThi, khacQuestions, false, questionCounter);
            }

            try (FileOutputStream out = new FileOutputStream(fileDeThi)) {
                docDeThi.write(out);
            }
        }

        // Tạo file Đáp Án DOCX
        try (XWPFDocument docDapAn = new XWPFDocument()) {
            // ... (Tạo tiêu đề như cũ) ...
            XWPFParagraph titleDapAnP = docDapAn.createParagraph();
            titleDapAnP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRunDapAn = titleDapAnP.createRun();
            titleRunDapAn.setBold(true); titleRunDapAn.setFontSize(16);
            titleRunDapAn.setText("ĐÁP ÁN - ĐỀ THI: " + Normalizer.normalize(deThi.getTenDeThi().toUpperCase(), Normalizer.Form.NFC));
            titleRunDapAn.addBreak();
            addParagraphToDocx(docDapAn, "------------------------------------");
            docDapAn.createParagraph().createRun().addBreak();

            int questionCounter = 1;
            int sectionCounter = 1;
            if (!docHieuQuestions.isEmpty()) {
                addSectionTitleToDocx(docDapAn, "PHẦN " + sectionCounter++ + ": BÀI TẬP ĐỌC HIỂU");
                questionCounter = appendQuestionsToDocx(docDapAn, docHieuQuestions, true, questionCounter);
            }
            if (!ngheHieuQuestions.isEmpty()) {
                addSectionTitleToDocx(docDapAn, "PHẦN " + sectionCounter++ + ": BÀI TẬP NGHE HIỂU");
                questionCounter = appendQuestionsToDocx(docDapAn, ngheHieuQuestions, true, questionCounter);
            }
            if (!khacQuestions.isEmpty()) {
                addSectionTitleToDocx(docDapAn, "PHẦN " + sectionCounter++ + ": BÀI TẬP KHÁC");
                appendQuestionsToDocx(docDapAn, khacQuestions, true, questionCounter);
            }

            try (FileOutputStream out = new FileOutputStream(fileDapAn)) {
                docDapAn.write(out);
            }
        }
         System.out.println("Đã xuất Đề Thi và Đáp Án DOCX tới thư mục: " + fileDeThi.getParent());
    }
    
    private void addParagraphToDocx(XWPFDocument doc, String text) {
        doc.createParagraph().createRun().setText(text);
    } 
}
