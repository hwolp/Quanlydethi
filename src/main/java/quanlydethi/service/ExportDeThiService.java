package quanlydethi.service;

import quanlydethi.model.CauHoi;
import quanlydethi.model.DeThi;
import quanlydethi.model.LuaChon;
import quanlydethi.model.TrinhDo;
import quanlydethi.dao.CauHoiDAO;
import quanlydethi.dao.LuaChonDAO;
import quanlydethi.dao.CauHoiTrongDeThiDAO;
import quanlydethi.dao.TrinhDoDAO;

// Imports cho OpenHTMLToPDF
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog; // Để quản lý logging (tùy chọn)
import com.openhtmltopdf.extend.FSSupplier; // Để cung cấp InputStream cho font

// Imports cho Apache POI (DOCX) - Giữ lại nếu bạn vẫn muốn chức năng này
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
// import java.nio.file.Files; // Không cần nữa nếu HTML sinh động
// import java.nio.charset.StandardCharsets; // Không cần nữa nếu HTML sinh động
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level; // Cho XRLog
import java.util.stream.Collectors;

public class ExportDeThiService {
    private CauHoiDAO cauHoiDAO;
    private LuaChonDAO luaChonDAO;
    private CauHoiTrongDeThiDAO cauHoiTrongDeThiDAO;
    private TrinhDoDAO trinhDoDAO;

    // Đường dẫn tới font trong resources, để PdfRendererBuilder sử dụng
    private final String FONT_RESOURCE_PATH = "/fonts/Arial.ttf"; // HOẶC "/fonts/Arial.ttf"
    private final String FONT_FAMILY_CSS_NAME = "MyUnicodeFont"; // Tên bạn sẽ dùng trong CSS

    public ExportDeThiService() {
        this.cauHoiDAO = new CauHoiDAO();
        this.luaChonDAO = new LuaChonDAO();
        this.cauHoiTrongDeThiDAO = new CauHoiTrongDeThiDAO();
        this.trinhDoDAO = new TrinhDoDAO();

        // Tắt bớt log mặc định của OpenHTMLToPDF nếu quá nhiều, chỉ hiển thị WARNING trở lên
        XRLog.listRegisteredLoggers().forEach(logger -> XRLog.setLevel(logger, Level.WARNING));
    }

    private String getTenTrinhDo(Integer maTrinhDo) {
        if (maTrinhDo == null) return "N/A";
        TrinhDo td = trinhDoDAO.getTrinhDoById(maTrinhDo);
        return td != null ? td.getTenTrinhDo() : "N/A (Mã: " + maTrinhDo + ")";
    }

    // Hàm tạo CSS cơ bản để nhúng font và định dạng
    private String getBaseCss() {
        return "<style>\n" +
               "  @font-face {\n" +
               "    font-family: '" + FONT_FAMILY_CSS_NAME + "';\n" +
               // "    src: url('classpath:/fonts/NotoSans-Regular.ttf');" + // Cách này có thể không hoạt động, dùng useFont() của builder tốt hơn
               "  }\n" +
               "  body { font-family: '" + FONT_FAMILY_CSS_NAME + "', Arial, sans-serif; font-size: 11pt; line-height: 1.6; }\n" +
               "  h1 { text-align: center; font-size: 16pt; margin-bottom: 15px; }\n" +
               "  h2 { text-align: center; font-size: 14pt; margin-top: 15px; margin-bottom: 10px; }\n" +
               "  .exam-info { margin-bottom: 15px; padding: 10px; border: 1px solid #ccc; }\n" +
               "  .exam-info p { margin: 3px 0; }\n" +
               "  .question-block { margin-bottom: 12px; padding-left: 5px; page-break-inside: avoid; }\n" +
               "  .question-content { font-weight: bold; margin-bottom: 4px; }\n" +
               "  .choices { margin-left: 15px; }\n" +
               "  .choice-item { margin-bottom: 2px; }\n" +
               "  .correct-answer-text { font-weight: bold; color: #006400; /* DarkGreen */ }\n" +
               "  hr { margin-top:10px; margin-bottom:10px; }\n"+
               "</style>\n";
    }

    // Hàm tạo nội dung HTML cho Đề Thi
    private String generateHtmlForDeThi(DeThi deThi, List<CauHoi> danhSachCauHoi) {
        StringBuilder html = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        html.append(getBaseCss());
        html.append("<title>Đề Thi: ").append(escapeHtml(deThi.getTenDeThi())).append("</title></head><body>");

        html.append("<h1>ĐỀ THI</h1>");
        html.append("<h2>").append(escapeHtml(deThi.getTenDeThi())).append("</h2>");

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
        html.append("</div>");
        html.append("<hr>");

        int stt = 1;
        for (CauHoi ch : danhSachCauHoi) {
            String normalizedNoiDungCauHoi = Normalizer.normalize(ch.getNoiDungCauHoi(), Normalizer.Form.NFC);
            html.append("<div class='question-block'>");
            html.append("<p class='question-content'><strong>Câu ").append(stt++).append(":</strong> ")
                .append(escapeHtml(normalizedNoiDungCauHoi)).append(" (").append(ch.getDiem()).append(" điểm)</p>");

            List<LuaChon> luaChons = luaChonDAO.getLuaChonByMaCauHoi(ch.getMaCauHoi());
            if (luaChons != null && !luaChons.isEmpty()) {
                html.append("<div class='choices'>");
                for (LuaChon lc : luaChons) {
                    String normalizedNoiDungLuaChon = Normalizer.normalize(lc.getNoiDungLuaChon(), Normalizer.Form.NFC);
                    html.append("<p class='choice-item'>").append(escapeHtml(normalizedNoiDungLuaChon)).append("</p>");
                }
                html.append("</div>");
            }
            html.append("</div>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    // Hàm tạo nội dung HTML cho Đáp Án
    private String generateHtmlForDapAn(DeThi deThi, List<CauHoi> danhSachCauHoi) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        html.append(getBaseCss());
        html.append("<title>Đáp Án - ").append(escapeHtml(deThi.getTenDeThi())).append("</title></head><body>");

        html.append("<h1>ĐÁP ÁN CHI TIẾT</h1>");
        html.append("<h2>Đề Thi: ").append(escapeHtml(deThi.getTenDeThi())).append("</h2>");
        html.append("<hr>");

        int stt = 1;
        for (CauHoi ch : danhSachCauHoi) {
            String normalizedNoiDungCauHoi = Normalizer.normalize(ch.getNoiDungCauHoi(), Normalizer.Form.NFC);
            html.append("<div class='question-block'>");
            html.append("<p class='question-content'><strong>Câu ").append(stt++).append(":</strong> ")
                .append(escapeHtml(normalizedNoiDungCauHoi.substring(0, Math.min(normalizedNoiDungCauHoi.length(), 70)) + (normalizedNoiDungCauHoi.length() > 70 ? "..." : "")))
                .append("</p>");

            List<LuaChon> luaChons = luaChonDAO.getLuaChonByMaCauHoi(ch.getMaCauHoi());
            String dapAnDungText = "N/A";
            if (luaChons != null) {
                for (LuaChon lc : luaChons) {
                    if (lc.isLaDapAnDung()) {
                        // Lấy ký hiệu đáp án từ nội dung lựa chọn (cần logic parse phù hợp)
                        String normalizedDapAn = Normalizer.normalize(lc.getNoiDungLuaChon(), Normalizer.Form.NFC);
                        if (normalizedDapAn.matches("^[A-Za-z0-9][.)][\\s\\S]*")){ // Tìm ký hiệu như A. B. 1)
                             int dotIndex = normalizedDapAn.indexOf(".");
                             int bracketIndex = normalizedDapAn.indexOf(")");
                             int splitIndex = -1;
                             if(dotIndex != -1 && bracketIndex != -1) splitIndex = Math.min(dotIndex, bracketIndex);
                             else if (dotIndex != -1) splitIndex = dotIndex;
                             else if (bracketIndex != -1) splitIndex = bracketIndex;

                             if (splitIndex != -1 && splitIndex < 5) { // Giả sử ký hiệu không quá dài
                                 dapAnDungText = normalizedDapAn.substring(0, splitIndex + 1).trim();
                             } else {
                                 dapAnDungText = normalizedDapAn; // Nếu không có dạng chuẩn, lấy cả
                             }
                        } else {
                           dapAnDungText = normalizedDapAn;
                        }
                        break;
                    }
                }
            }
            html.append("<p><strong>Đáp án:</strong> <span class='correct-answer-text'>")
                .append(escapeHtml(dapAnDungText)).append("</span></p>");

            // Giả sử CauHoi có trường giaiThich (nếu không, bạn cần lấy từ DTO hoặc bỏ qua)
            // if (ch.getGiaiThich() != null && !ch.getGiaiThich().isEmpty()) {
            //    String normalizedGiaiThich = Normalizer.normalize(ch.getGiaiThich(), Normalizer.Form.NFC);
            //    html.append("<p><em>Giải thích:</em> ").append(escapeHtml(normalizedGiaiThich)).append("</p>");
            // }
            html.append("</div>");
        }
        html.append("</body></html>");
        return html.toString();
    }

    // Phương thức chính để xuất PDF sử dụng HTML
    public void exportDeThiToPdfViaHtml(DeThi deThi, File fileDeThi, File fileDapAn) throws Exception {
        List<Integer> maCauHoiList = cauHoiTrongDeThiDAO.getMaCauHoiByMaDeThi(deThi.getMaDeThi());
        List<CauHoi> danhSachCauHoi = maCauHoiList.stream()
                                               .map(maCH -> cauHoiDAO.getCauHoiById(maCH))
                                               .filter(ch -> ch != null)
                                               .collect(Collectors.toList());

        String htmlDeThi = generateHtmlForDeThi(deThi, danhSachCauHoi);
        String htmlDapAn = generateHtmlForDapAn(deThi, danhSachCauHoi);
        
        // Xuất file Đề Thi
        try (OutputStream osDeThi = new FileOutputStream(fileDeThi)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // Cách tốt nhất để load font từ resources là dùng FSSupplier
            builder.useFont(new FSSupplier<InputStream>() {
                @Override
                public InputStream supply() {
                    return ExportDeThiService.class.getResourceAsStream(FONT_RESOURCE_PATH);
                }
            }, FONT_FAMILY_CSS_NAME); // Tên font family dùng trong CSS

            builder.withHtmlContent(htmlDeThi, null); // baseUri có thể là null
            builder.toStream(osDeThi);
            builder.run();
            System.out.println("Đã xuất Đề Thi PDF (qua HTML) tới: " + fileDeThi.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo PDF cho Đề Thi: " + e.getMessage());
            throw e;
        }


        // Xuất file Đáp Án
        try (OutputStream osDapAn = new FileOutputStream(fileDapAn)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(new FSSupplier<InputStream>() {
                @Override
                public InputStream supply() {
                    return ExportDeThiService.class.getResourceAsStream(FONT_RESOURCE_PATH);
                }
            }, FONT_FAMILY_CSS_NAME);

            builder.withHtmlContent(htmlDapAn, null);
            builder.toStream(osDapAn);
            builder.run();
            System.out.println("Đã xuất Đáp Án PDF (qua HTML) tới: " + fileDapAn.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo PDF cho Đáp Án: " + e.getMessage());
            throw e;
        }
    }
    
    // Hàm helper để escape ký tự HTML cơ bản
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    // Giữ lại hàm exportDeThiToDocx nếu bạn vẫn muốn dùng cách cũ cho DOCX
    public void exportDeThiToDocx(DeThi deThi, File fileDeThi, File fileDapAn) throws IOException {
        // ... (code export DOCX sử dụng Apache POI như trước) ...
        // Bạn cần đảm bảo code này vẫn hoạt động hoặc cập nhật nó nếu cần.
        // Ví dụ:
        List<Integer> maCauHoiList = cauHoiTrongDeThiDAO.getMaCauHoiByMaDeThi(deThi.getMaDeThi());
        List<CauHoi> danhSachCauHoi = maCauHoiList.stream()
                                               .map(maCH -> cauHoiDAO.getCauHoiById(maCH))
                                               .filter(ch -> ch != null)
                                               .collect(Collectors.toList());
        // Tạo file Đề Thi
        try (XWPFDocument docDeThi = new XWPFDocument()) {
            XWPFParagraph titleDeThi = docDeThi.createParagraph();
            titleDeThi.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRunDeThi = titleDeThi.createRun();
            titleRunDeThi.setBold(true);
            titleRunDeThi.setFontSize(16);
            titleRunDeThi.setText("ĐỀ THI: " + deThi.getTenDeThi().toUpperCase());
            titleRunDeThi.addBreak();

            addParagraphToDocx(docDeThi, "Trình độ: " + getTenTrinhDo(deThi.getMaTrinhDo()));
            if (deThi.getThoiGianLamBaiPhut() != null && deThi.getThoiGianLamBaiPhut() > 0) {
                addParagraphToDocx(docDeThi, "Thời gian làm bài: " + deThi.getThoiGianLamBaiPhut() + " phút");
            }
            addParagraphToDocx(docDeThi, "------------------------------------");
            docDeThi.createParagraph().createRun().addBreak(); 

            int stt = 1;
            for (CauHoi ch : danhSachCauHoi) {
                addParagraphToDocx(docDeThi, "Câu " + stt++ + ": " + Normalizer.normalize(ch.getNoiDungCauHoi(), Normalizer.Form.NFC));
                List<LuaChon> luaChons = luaChonDAO.getLuaChonByMaCauHoi(ch.getMaCauHoi());
                if (luaChons != null && !luaChons.isEmpty()) {
                    for (LuaChon lc : luaChons) {
                        addParagraphToDocx(docDeThi, "  " + Normalizer.normalize(lc.getNoiDungLuaChon(), Normalizer.Form.NFC));
                    }
                }
                docDeThi.createParagraph().createRun().addBreak(); 
            }

            try (FileOutputStream out = new FileOutputStream(fileDeThi)) {
                docDeThi.write(out);
            }
        }

        // Tạo file Đáp Án
        try (XWPFDocument docDapAn = new XWPFDocument()) {
            XWPFParagraph titleDapAn = docDapAn.createParagraph();
            titleDapAn.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRunDapAn = titleDapAn.createRun();
            titleRunDapAn.setBold(true);
            titleRunDapAn.setFontSize(16);
            titleRunDapAn.setText("ĐÁP ÁN - ĐỀ THI: " + deThi.getTenDeThi().toUpperCase());
            titleRunDapAn.addBreak();
            addParagraphToDocx(docDapAn, "------------------------------------");
            docDapAn.createParagraph().createRun().addBreak();

            int stt = 1;
            for (CauHoi ch : danhSachCauHoi) {
                // addParagraphToDocx(docDapAn, "Câu " + stt++ + ":"); // Chỉ cần đáp án là đủ
                String dapAnDung = "N/A";
                List<LuaChon> luaChons = luaChonDAO.getLuaChonByMaCauHoi(ch.getMaCauHoi());
                if (luaChons != null) {
                    for (LuaChon lc : luaChons) {
                        if (lc.isLaDapAnDung()) {
                            String noiDungLC = lc.getNoiDungLuaChon();
                             if (noiDungLC != null && noiDungLC.matches("^[A-ZDa-z0-9][.)][\\s\\S]*")){
                                dapAnDung = noiDungLC.substring(0, noiDungLC.indexOf(".")+1).trim();
                             } else if (noiDungLC != null && noiDungLC.length() > 0){
                                 dapAnDung = noiDungLC.substring(0, Math.min(noiDungLC.length(), 10)) + (noiDungLC.length()>10?"...":""); // Tránh quá dài
                             } else {
                                dapAnDung = "Không rõ";
                             }
                            break;
                        }
                    }
                }
                addParagraphToDocx(docDapAn, "Câu " + stt++ + ": " + dapAnDung);
                docDapAn.createParagraph().createRun().addBreak();
            }
            try (FileOutputStream out = new FileOutputStream(fileDapAn)) {
                docDapAn.write(out);
            }
        }
         System.out.println("Đã xuất Đề Thi và Đáp Án DOCX tới thư mục: " + fileDeThi.getParent());
    }
    
    // Helper cho DOCX
    private void addParagraphToDocx(XWPFDocument doc, String text) {
        doc.createParagraph().createRun().setText(text);
    }
}