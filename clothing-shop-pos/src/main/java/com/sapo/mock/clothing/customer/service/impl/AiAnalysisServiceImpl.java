package com.sapo.mock.clothing.customer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.sapo.mock.clothing.customer.dto.response.AiSuggestionResponseDto;
import com.sapo.mock.clothing.customer.repository.CareCampaignRepository;
import com.sapo.mock.clothing.customer.repository.CustomerRepository;
import com.sapo.mock.clothing.customer.service.AiAnalysisService;
import com.sapo.mock.clothing.entity.CareCampaign;
import com.sapo.mock.clothing.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    @Value("${ai.gemini.api-url}")
    private String apiUrl;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CareCampaignRepository careCampaignRepository;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(30))
            .build();

    // ĐÃ SỬA: Cho phép chứa ký tự xuống dòng mộc trong giá trị JSON của AI trả về
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature());

    @Override
    public AiSuggestionResponseDto suggestScriptAndSms(Integer customerId, Integer campaignId) {
        AiSuggestionResponseDto fallback = new AiSuggestionResponseDto();
        fallback.setCallScript("Chào anh/chị, em gọi từ Sapo Shop ạ. Rất mong anh/chị dành chút thời gian nghe máy.");
        fallback.setSmsTemplate("Chào anh/chị, Sapo Shop đang có ưu đãi hấp dẫn. Mời anh/chị ghé qua cửa hàng nhé!");
        fallback.setObjectionHandling("Dạ không sao ạ, em xin phép gửi thông tin ưu đãi qua SMS/Zalo để anh/chị tham khảo khi rảnh nhé.");

        try {
            Customer customer = customerRepository.findById(customerId).orElse(null);
            CareCampaign campaign = careCampaignRepository.findById(campaignId).orElse(null);

            if (customer == null || campaign == null) {
                return fallback;
            }

            int age = 0;
            if (customer.getDateOfBirth() != null) {
                age = Period.between(customer.getDateOfBirth(), LocalDate.now()).getYears();
            }

            String genderStr = customer.getGender() != null ? customer.getGender().name() : "Không rõ";

            String birthdayRule = (campaign.getType() != null && campaign.getType().contains("BIRTHDAY")) || (campaign.getName() != null && campaign.getName().toLowerCase().contains("sinh nhật"))
                    ? "LƯU Ý RIÊNG: Đây là chiến dịch chúc mừng Sinh nhật, TUYỆT ĐỐI KHÔNG giải thích chi tiết về điểm thưởng hay chi tiết voucher. Chỉ cần báo cửa hàng có một phần quà/voucher sinh nhật đặc biệt tặng khách.\n"
                    : "";

            String systemInstruction = "Bạn là một nhân viên chăm sóc khách hàng xuất sắc của cửa hàng quần áo Sapo Clothing.\n"
                    + "Nhiệm vụ của bạn là TỰ ĐỘNG sáng tạo ra kịch bản gọi điện, tin nhắn SMS và cẩm nang xử lý từ chối HAY NHẤT, CHUYÊN NGHIỆP NHẤT để liên hệ với khách trong chiến dịch: '" + campaign.getName() + "'.\n"
                    + birthdayRule
                    + "Thông tin khách hàng: Tên: " + customer.getFullName() + " (" + genderStr + ", " + (age > 0 ? age + " tuổi" : "Chưa rõ") + "). Tổng chi tiêu: " + (customer.getTotalSpent() != null ? customer.getTotalSpent() : "0") + " VNĐ. Điểm: " + customer.getRewardPoints() + ".\n"
                    + "YÊU CẦU QUAN TRỌNG:\n"
                    + "1. Kịch bản gọi điện (call_script) phải chia thành các phần rõ ràng như: Lời mở đầu, Giới thiệu & Thăm dò nhu cầu, Kết thúc cuộc gọi.\n"
                    + "2. Kịch bản xử lý từ chối (objection_handling) cần gợi ý vài trường hợp thực tế (Ví dụ: Khách bận, Khách chưa có nhu cầu, Khách chê giá cao) kèm cách đáp lời lịch sự.\n"
                    + "3. TUYỆT ĐỐI KHÔNG được tự bịa ra các voucher, mã giảm giá hay chương trình khuyến mãi ảo (không tự bịa voucher 100k, 20%...). Chỉ được nhắc đến số Điểm hiện có của khách hàng nếu khách có điểm.\n"
                    + "4. TUYỆT ĐỐI không dùng ký tự ngoặc kép (\") bên trong nội dung văn bản để tránh làm lỗi cấu trúc chuỗi JSON, thay vào đó hãy dùng dấu nháy đơn (').";

            // 1. Tạo phần contents
            Map<String, Object> textPart = Map.of("text", systemInstruction);
            Map<String, Object> parts = Map.of("parts", List.of(textPart));

            // ĐÃ SỬA: Định nghĩa cấu trúc Schema để ép kiểu JSON trả về tuyệt đối chính xác
            Map<String, Object> properties = Map.of(
                    "call_script", Map.of("type", "STRING"),
                    "sms_template", Map.of("type", "STRING"),
                    "objection_handling", Map.of("type", "STRING")
            );
            Map<String, Object> responseSchema = Map.of(
                    "type", "OBJECT",
                    "properties", properties,
                    "required", List.of("call_script", "sms_template", "objection_handling")
            );

            // 2. Tạo generationConfig
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("maxOutputTokens", 8192); // Tăng token lên tối đa để tránh bị ngắt đuôi JSON
            generationConfig.put("responseMimeType", "application/json");
            generationConfig.put("responseSchema", responseSchema); // Gắn kèm schema vào cấu hình

            // 3. Gộp chung vào payload lớn để gửi đi
            Map<String, Object> rootPayload = Map.of(
                    "contents", List.of(parts),
                    "generationConfig", generationConfig
            );

            String jsonPayload = objectMapper.writeValueAsString(rootPayload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", apiKey)
                    .timeout(java.time.Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 || response.body() == null) {
                System.err.println("Gemini API Error Body: " + response.body());
                return fallback;
            }

            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> resParts = (List<Map<String, Object>>) content.get("parts");
                if (resParts != null && !resParts.isEmpty()) {
                    String aiJsonText = (String) resParts.get(0).get("text");

                    if (aiJsonText != null && !aiJsonText.isBlank()) {
                        // ĐÃ SỬA: Do bật ALLOW_UNESCAPED_CONTROL_CHARS nên dòng này sẽ parse cực kỳ mượt mà
                        Map<String, Object> aiData = objectMapper.readValue(aiJsonText.trim(), Map.class);

                        AiSuggestionResponseDto resultDto = new AiSuggestionResponseDto();
                        resultDto.setCallScript((String) aiData.get("call_script"));
                        resultDto.setSmsTemplate((String) aiData.get("sms_template"));
                        resultDto.setObjectionHandling((String) aiData.get("objection_handling"));

                        return resultDto;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi xử lý gọi AI bằng HttpClient: " + e.getMessage());
            e.printStackTrace();
        }
        return fallback;
    }
}