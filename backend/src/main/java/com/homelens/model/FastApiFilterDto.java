package com.homelens.model;

import lombok.Data;

@Data
public class FastApiFilterDto {
	private String sales_type;
    private String service_type;

    private String local1;
    private String local2;
    private String local3;

    private Integer deposit_min;
    private Integer deposit_max;
    private Integer rent_max;
    private Integer area_m2_min;
    private Integer area_m2_max;
    private Integer manage_cost_max;
}
