package com.sapo.mock.clothing.customer.service;

import com.sapo.mock.clothing.customer.dto.response.AiSuggestionResponseDto;

public interface AiAnalysisService {
    AiSuggestionResponseDto suggestScriptAndSms(Integer customerId, Integer campaignId);
}
