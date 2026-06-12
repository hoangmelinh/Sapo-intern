package com.sapo.mock.clothing.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestResponse<T> {
	private int statusCode;
	private String error;
	private Object message;
	private T data;
}
