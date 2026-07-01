package com.sapo.mock.clothing.ai;

import com.sapo.mock.clothing.entity.ProductVariant;
import com.sapo.mock.clothing.product.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiAIService {

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    @Value("${ai.gemini.api-url}")
    private String apiUrl;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private com.sapo.mock.clothing.order.repository.OrderLineItemRepository orderLineItemRepository;

    public String analyzeInventory(String customPrompt, int days) {
        Page<ProductVariant> page = variantRepository.findAll(PageRequest.of(0, 100));
        List<ProductVariant> variants = page.getContent();
        
        // 1. Tính tổng lượng bán của từng sản phẩm trong N ngày qua
        java.time.Instant cutoffDate = java.time.Instant.now().minus(java.time.Duration.ofDays(days));
        List<Object[]> soldData = orderLineItemRepository.getVariantSoldQuantitySinceBySku(cutoffDate);
        Map<String, Integer> soldMap = new java.util.HashMap<>();
        for (Object[] row : soldData) {
            String sku = (String) row[0];
            Integer soldQty = ((Number) row[1]).intValue();
            soldMap.put(sku, soldQty);
        }

        // 2. Gộp cả Tồn kho và Số lượng đã bán gửi cho AI
        StringBuilder dataBuilder = new StringBuilder();
        for (ProductVariant v : variants) {
            String productName = v.getProduct() != null ? v.getProduct().getName() : "Unknown";
            String category = (v.getProduct() != null && v.getProduct().getCategory() != null) 
                                ? v.getProduct().getCategory().getName() : "Khác";
            
            // Lấy lượng đã bán từ Map, nếu không có thì mặc định là 0
            int soldQuantity = soldMap.getOrDefault(v.getSku(), 0);

            dataBuilder.append("- Danh mục: ").append(category)
                       .append(" | SP: ").append(productName)
                       .append(" | SKU: ").append(v.getSku())
                       .append(" | Tồn kho (còn lại): ").append(v.getQuantity())
                       .append(" | Đã bán (").append(days).append(" ngày qua): ").append(soldQuantity)
                       .append(" | Giá bán: ").append(v.getSalePrice())
                       .append("\n");
        }

        String data = dataBuilder.toString();
        
        String prompt = "Bạn là AI chuyên phân tích kho hàng.\n"
                + "YÊU CẦU QUAN TRỌNG: Trình bày kết quả bằng văn bản thuần túy (plain text) dễ đọc. TUYỆT ĐỐI KHÔNG sử dụng Markdown (không dùng ký tự **, *, #). Dùng dấu gạch ngang (-) để làm danh sách.\n"
                + "Yêu cầu của người dùng: [" + customPrompt + "].\n"
                + "Dữ liệu kho (tối đa 100 sản phẩm):\n" + data;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");

        String requestBody = "{\n" +
                "  \"contents\": [{\n" +
                "    \"parts\":[{\"text\": \"" + escapedPrompt + "\"}]\n" +
                "  }]\n" +
                "}";

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "?key=" + apiKey,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    return (String) parts.get(0).get("text");
                }
            }
            return "Không thể lấy kết quả phân tích từ AI.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi gọi AI: " + e.getMessage();
        }
    }
}
