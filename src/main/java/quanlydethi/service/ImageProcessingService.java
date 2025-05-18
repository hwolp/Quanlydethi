package quanlydethi.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Import các lớp DTO và Exception
import quanlydethi.dto.CauHoiTrichXuatDTO;
import quanlydethi.dto.LuaChonDTO;
// class ImageProcessingException extends Exception { ... } // Đảm bảo bạn đã định nghĩa lớp này

// Import thư viện JSON
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class ImageProcessingService {

    private String apiKey;
    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private String geminiModel = "gemini-1.5-flash-latest";
    private static final String CONFIG_FILE = "/config.properties";

    public ImageProcessingService() {
        loadApiKey();
    }

    // Constructor và loadApiKey giữ nguyên như code bạn đã cung cấp
    public ImageProcessingService(String apiKey, String modelName) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
             System.err.println("CẢNH BÁO: Gemini API Key được truyền vào không hợp lệ.");
             this.apiKey = "API_KEY_INVALID_PARAM";
        } else {
            this.apiKey = apiKey;
        }
        if (modelName != null && !modelName.trim().isEmpty()){
            this.geminiModel = modelName;
        }
    }

    private void loadApiKey() {
        try (InputStream input = ImageProcessingService.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("Lỗi: Không tìm thấy file cấu hình '" + CONFIG_FILE + "'.");
                this.apiKey = "API_KEY_CONFIG_FILE_NOT_FOUND";
                return;
            }
            Properties prop = new Properties();
            prop.load(input);
            this.apiKey = prop.getProperty("gemini.api.key");

            if (this.apiKey == null || this.apiKey.trim().isEmpty() || "YOUR_GEMINI_API_KEY_HERE".equals(this.apiKey) || this.apiKey.startsWith("API_KEY_")) {
                System.err.println("CẢNH BÁO: Gemini API Key chưa được cấu hình đúng trong " + CONFIG_FILE +
                                   " hoặc giá trị không hợp lệ. Giá trị hiện tại: '" + this.apiKey + "'");
                this.apiKey = "API_KEY_NOT_CONFIGURED_PROPERLY";
            }
        } catch (IOException ex) {
            System.err.println("Lỗi khi đọc file cấu hình API key: " + ex.getMessage());
            this.apiKey = "API_KEY_LOAD_ERROR";
        }
    }
    // ---- HẾT PHẦN GIỮ NGUYÊN ----

    /**
     * Gửi hình ảnh đến API Gemini để trích xuất danh sách câu hỏi.
     *
     * @param imageFile File hình ảnh cần xử lý.
     * @param userPromptForContext Một gợi ý chung về ngữ cảnh của hình ảnh.
     * @return Danh sách các đối tượng CauHoiTrichXuatDTO được trích xuất.
     * @throws ImageProcessingException Nếu có lỗi trong quá trình xử lý hoặc gọi API.
     */
    public List<CauHoiTrichXuatDTO> extractQuestionsFromImage(File imageFile, String userPromptForContext) throws ImageProcessingException {
        if (this.apiKey == null || this.apiKey.startsWith("API_KEY_") || this.apiKey.trim().isEmpty()){
            throw new ImageProcessingException("Gemini API Key không được cấu hình hoặc không hợp lệ. Vui lòng kiểm tra tệp " + CONFIG_FILE + " (giá trị hiện tại: '"+ this.apiKey +"') hoặc cách service được khởi tạo.");
        }
        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            throw new ImageProcessingException("Tệp hình ảnh không hợp lệ hoặc không tồn tại.");
        }

        String jsonPrompt = "Phân tích hình ảnh này. " +
                            (userPromptForContext != null && !userPromptForContext.trim().isEmpty() ? userPromptForContext : "Đây là một tài liệu chứa câu hỏi.") +
                            " Hãy trích xuất TẤT CẢ các câu hỏi bạn tìm thấy. Với mỗi câu hỏi, hãy cung cấp thông tin dưới dạng một đối tượng JSON. " +
                            "Toàn bộ kết quả phải là một MẢNG JSON (JSON array) chứa các đối tượng câu hỏi này. " +
                            "Mỗi đối tượng câu hỏi trong mảng phải có các trường sau:\n" +
                            "- \"noiDungCauHoi\": (string) Toàn bộ nội dung của câu hỏi, bao gồm cả phần dẫn và phần trống cần điền (nếu là câu hỏi đục lỗ như trong ảnh ví dụ của bạn), nếu là đoạn văn thì trong câu hỏi chứa luôn đoạn văn đó.\n" +
                            "- \"cacLuaChon\": (array of objects, có thể rỗng nếu không phải trắc nghiệm hoặc không có lựa chọn) Một mảng các lựa chọn, mỗi lựa chọn là một đối tượng có các trường:\n" +
                            "    - \"kyHieu\": (string) Ký hiệu của lựa chọn (ví dụ: \"A\", \"B\", \"C\", \"D\", \"1\", \"2\", ...).\n" +
                            "    - \"noiDung\": (string) Nội dung của lựa chọn đó.\n" +
                            "- \"dapAnDungKyHieu\": (string) Ký hiệu của đáp án đúng (ví dụ: \"A\"). Nếu không xác định được đáp án hoặc không phải câu hỏi trắc nghiệm có đáp án rõ ràng, hãy để giá trị là một chuỗi rỗng \"\".\n" +
                            "- \"giaiThich\": (string, tùy chọn) Phần giải thích chi tiết cho đáp án, nếu có và trích xuất được. Nếu không có, để trống.\n" +
                            "- \"loaiCauHoiGoiY\": (string, tùy chọn) Loại câu hỏi mà AI đoán được (ví dụ: 'Trắc nghiệm', 'Điền khuyết', 'Tự luận').\n" +
                            "- \"trinhDoGoiY\": (string, tùy chọn) Trình độ mà AI đoán được cho câu hỏi (ví dụ: 'Dễ', 'Trung bình', 'Khó', 'Lớp 10', 'TOEIC 500').\n" +
                            "Ví dụ một đối tượng câu hỏi trong mảng:\n" +
                            "{\n" +
                            "  \"noiDungCauHoi\": \"Thủ đô của Việt Nam là gì?\",\n" +
                            "  \"cacLuaChon\": [\n" +
                            "    {\"kyHieu\": \"A\", \"noiDung\": \"Hà Nội\"},\n" +
                            "    {\"kyHieu\": \"B\", \"noiDung\": \"Đà Nẵng\"}\n" +
                            "  ],\n" +
                            "  \"dapAnDungKyHieu\": \"A\",\n" +
                            "  \"giaiThich\": \"Hà Nội là thủ đô theo hiến pháp.\",\n" +
                            "  \"loaiCauHoiGoiY\": \"Trắc nghiệm\",\n" +
                            "  \"trinhDoGoiY\": \"Địa lý cơ bản\"\n" +
                            "}\n" +
                            "Nếu hình ảnh không chứa câu hỏi nào, hãy trả về một mảng JSON rỗng: []. " +
                            "Đảm bảo rằng TOÀN BỘ PHẢN HỒI của bạn chỉ là một MẢNG JSON HỢP LỆ, không có bất kỳ văn bản giới thiệu, giải thích hay ký tự nào khác trước hoặc sau mảng JSON đó.";

        try {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = Files.probeContentType(imageFile.toPath());

            if (mimeType == null) {
                String fileNameLower = imageFile.getName().toLowerCase();
                if (fileNameLower.endsWith(".png")) mimeType = "image/png";
                else if (fileNameLower.endsWith(".jpg") || fileNameLower.endsWith(".jpeg")) mimeType = "image/jpeg";
                else if (fileNameLower.endsWith(".gif")) mimeType = "image/gif";
                else if (fileNameLower.endsWith(".webp")) mimeType = "image/webp";
                else throw new ImageProcessingException("Không thể xác định MIME type của ảnh: " + imageFile.getName() + ". Chỉ hỗ trợ PNG, JPEG, GIF, WEBP.");
            }
            if (!mimeType.startsWith("image/")) {
                 throw new ImageProcessingException("Tệp không phải là định dạng ảnh được hỗ trợ: " + mimeType);
            }

            // Sử dụng org.json để tạo request body
            JSONObject inlineData = new JSONObject();
            inlineData.put("mime_type", mimeType);
            inlineData.put("data", base64Image);

            JSONObject imagePart = new JSONObject();
            imagePart.put("inline_data", inlineData);

            JSONObject textPart = new JSONObject();
            textPart.put("text", jsonPrompt);

            JSONArray partsArray = new JSONArray();
            partsArray.put(textPart);
            partsArray.put(imagePart);

            JSONObject content = new JSONObject();
            content.put("parts", partsArray);

            JSONArray contentsArray = new JSONArray();
            contentsArray.put(content);

            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.2);
            generationConfig.put("topK", 32);
            generationConfig.put("topP", 1.0);
            generationConfig.put("maxOutputTokens", 8192);
            generationConfig.put("response_mime_type", "application/json"); // Yêu cầu Gemini trả về JSON

            JSONObject payload = new JSONObject();
            payload.put("contents", contentsArray);
            payload.put("generationConfig", generationConfig);

            String requestBody = payload.toString();
            // System.out.println("---- Request Body to Gemini ----\n" + requestBody + "\n-----------------------------");


            String apiUrl = GEMINI_API_BASE_URL + this.geminiModel + ":generateContent?key=" + this.apiKey;

            HttpClient client = HttpClient.newBuilder()
                                      .connectTimeout(Duration.ofSeconds(30))
                                      .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(240))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                // System.out.println("---- RAW API Response Body ----\n" + responseBody + "\n-----------------------------");

                String extractedJsonText = extractTextFromGeminiJsonResponse(responseBody);
                // System.out.println("---- Extracted JSON String from Gemini Response ----\n" + extractedJsonText + "\n-----------------------------");
                
                return parseCauHoiDtoListFromJsonString(extractedJsonText);
            } else {
                String errorDetails = response.body();
                System.err.println("Lỗi từ API Gemini: " + response.statusCode() + "\nChi tiết: " + errorDetails);
                throw new ImageProcessingException("Lỗi từ API Gemini (Code: " + response.statusCode() + "): " + extractGeminiErrorMessage(errorDetails));
            }

        } catch (IOException e) {
            throw new ImageProcessingException("Lỗi I/O khi xử lý tệp ảnh hoặc gọi API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ImageProcessingException("Yêu cầu đến API Gemini bị gián đoạn.", e);
        } catch (JSONException e) { // Bắt lỗi từ thư viện org.json
            System.err.println("Lỗi tạo JSON request hoặc parse JSON response: " + e.getMessage());
            throw new ImageProcessingException("Lỗi JSON trong quá trình xử lý với API Gemini: " + e.getMessage() , e);
        } catch (Exception e) {
             System.err.println("Lỗi không xác định: " + e.getMessage());
             e.printStackTrace();
            throw new ImageProcessingException("Lỗi không xác định trong quá trình xử lý: " + e.getMessage(), e);
        }
    }
    
    private String extractTextFromGeminiJsonResponse(String geminiResponseBody) throws ImageProcessingException {
        try {
            JSONObject fullResponse = new JSONObject(geminiResponseBody);
            if (fullResponse.has("candidates") && fullResponse.getJSONArray("candidates").length() > 0) {
                JSONObject firstCandidate = fullResponse.getJSONArray("candidates").getJSONObject(0);
                if (firstCandidate.has("content") && firstCandidate.getJSONObject("content").has("parts") &&
                    firstCandidate.getJSONObject("content").getJSONArray("parts").length() > 0) {
                    JSONObject firstPart = firstCandidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0);
                    if (firstPart.has("text")) {
                        return firstPart.getString("text"); // Thư viện org.json tự unescape
                    }
                }
            }
            System.err.println("Không tìm thấy 'text' trong cấu trúc JSON response mong đợi từ Gemini. Full response:\n" + geminiResponseBody);
            throw new ImageProcessingException("Cấu trúc phản hồi API Gemini không như mong đợi hoặc không chứa JSON câu hỏi.");
        } catch (JSONException e) {
            System.err.println("Lỗi parse JSON từ Gemini API response (khi trích xuất text chính): " + e.getMessage() + "\nFull response:\n" + geminiResponseBody);
            // Kiểm tra xem có phải lỗi do "safetyRatings" block không
            if (geminiResponseBody.contains("blockReason")) {
                 throw new ImageProcessingException("Yêu cầu bị chặn bởi bộ lọc an toàn của API Gemini. Chi tiết: " + extractGeminiErrorMessage(geminiResponseBody), e);   
            }
            throw new ImageProcessingException("Lỗi phân tích JSON từ phản hồi API Gemini (khi trích xuất text chính): " + e.getMessage(), e);
        }
    }

    private List<CauHoiTrichXuatDTO> parseCauHoiDtoListFromJsonString(String jsonCauHoiArrayString) throws ImageProcessingException {
        List<CauHoiTrichXuatDTO> dtoList = new ArrayList<>();
        if (jsonCauHoiArrayString == null || jsonCauHoiArrayString.trim().isEmpty()) {
            return dtoList;
        }
        
        String cleanedJsonString = cleanJsonString(jsonCauHoiArrayString);
        // System.out.println("---- Cleaned JSON String to Parse into List<CauHoiDTO> ----\n" + cleanedJsonString + "\n-----------------------------");

        if (!cleanedJsonString.trim().startsWith("[")) {
            System.err.println("Chuỗi JSON sau khi làm sạch không phải là một mảng: " + cleanedJsonString);
            int startIndex = cleanedJsonString.indexOf("[");
            int endIndex = cleanedJsonString.lastIndexOf("]");
            if (startIndex != -1 && endIndex > startIndex) {
                cleanedJsonString = cleanedJsonString.substring(startIndex, endIndex + 1);
            } else {
                 throw new ImageProcessingException("Phản hồi từ AI không chứa một mảng JSON hợp lệ sau khi làm sạch. Nội dung nhận được: " + cleanedJsonString);
            }
        }

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(cleanedJsonString);
        } catch (JSONException e) {
            throw new ImageProcessingException("Phản hồi JSON từ AI không hợp lệ (không phải mảng JSON): " + e.getMessage() + ". JSON nhận được: " + cleanedJsonString, e);
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                if (!(jsonArray.get(i) instanceof JSONObject)) {
                    System.err.println("Cảnh báo: Phần tử trong mảng JSON không phải là JSONObject. Bỏ qua. Index: " + i + ", Value: " + jsonArray.get(i).toString());
                    continue;
                }
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                CauHoiTrichXuatDTO dto = new CauHoiTrichXuatDTO();

                dto.setNoiDungCauHoi(jsonObj.optString("noiDungCauHoi", "N/A"));

                if (jsonObj.has("cacLuaChon") && jsonObj.get("cacLuaChon") instanceof JSONArray) {
                    JSONArray luaChonArray = jsonObj.getJSONArray("cacLuaChon");
                    for (int j = 0; j < luaChonArray.length(); j++) {
                        if (luaChonArray.get(j) instanceof JSONObject) {
                            JSONObject luaChonObj = luaChonArray.getJSONObject(j);
                            LuaChonDTO luaChonDto = new LuaChonDTO(); // Sử dụng LuaChonDTO
                            luaChonDto.setKyHieu(luaChonObj.optString("kyHieu"));
                            luaChonDto.setNoiDung(luaChonObj.optString("noiDung"));
                            dto.addLuaChon(luaChonDto); // Thêm vào DTO câu hỏi
                        } else {
                             System.err.println("Cảnh báo: Phần tử trong 'cacLuaChon' không phải là JSONObject. Bỏ qua. Index: " + j + ", Value: " + luaChonArray.get(j).toString());
                        }
                    }
                }
                dto.setDapAnDungKyHieu(jsonObj.optString("dapAnDungKyHieu", ""));
                dto.setGiaiThich(jsonObj.optString("giaiThich", null));
                dto.setLoaiCauHoiGoiY(jsonObj.optString("loaiCauHoiGoiY", null));
                dto.setTrinhDoGoiY(jsonObj.optString("trinhDoGoiY", null));

                dtoList.add(dto);
            } catch (JSONException e) {
                System.err.println("Lỗi khi parse một đối tượng CauHoiTrichXuatDTO từ JSON: " + e.getMessage() +
                                   ". Bỏ qua. JSON object: " + (jsonArray.optJSONObject(i) != null ? jsonArray.optJSONObject(i).toString() : "null"));
            }
        }
        return dtoList;
    }
    
    private String cleanJsonString(String text) {
        if (text == null) return "[]";
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring("```json".length());
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - "```".length());
            }
        } else if (text.startsWith("```")) {
             text = text.substring("```".length());
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - "```".length());
            }
        }
        return text.trim();
    }
     
    private String extractGeminiErrorMessage(String errorBody) {
        try {
            JSONObject errorJson = new JSONObject(errorBody);
            if (errorJson.has("error") && errorJson.get("error") instanceof JSONObject) {
                JSONObject errorDetails = errorJson.getJSONObject("error");
                if (errorDetails.has("message")) {
                    return errorDetails.getString("message");
                }
            }
            if (errorJson.has("message")) { // Một số lỗi có message ở cấp cao nhất
                 return errorJson.getString("message");
            }
        } catch (JSONException e) {
            System.err.println("Không thể parse JSON error body: " + e.getMessage());
        }
        return errorBody;
    }

    // Các hàm escapeJsonString và unescapeJsonString không còn cần thiết
    // khi dùng thư viện org.json để tạo request và parse response,
    // vì thư viện này tự xử lý việc escape/unescape.
    // Tuy nhiên, nếu bạn vẫn muốn giữ chúng cho mục đích khác, thì có thể để lại.
}