package com.sapo.mock.clothing.customer.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiSuggestionResponseDto {
    private String callScript;
    private String smsTemplate;
    private String objectionHandling;
}
