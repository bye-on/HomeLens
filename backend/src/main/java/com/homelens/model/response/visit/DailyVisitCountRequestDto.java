package com.homelens.model.response.visit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyVisitCountRequestDto {
	 private String day;
	 private long visitors;
}
