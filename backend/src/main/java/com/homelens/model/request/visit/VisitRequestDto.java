package com.homelens.model.request.visit;
import lombok.*;

@Data
@NoArgsConstructor 
@AllArgsConstructor
public class VisitRequestDto {
	
	private String visitorKey; // "u:123" or "c:uuid"
	private Long userId;        // nullable


}
