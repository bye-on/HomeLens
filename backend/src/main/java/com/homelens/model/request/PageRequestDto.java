package com.homelens.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PageRequestDto {
	private Integer page = 1;
	private Integer size = 10;
	
	public void setPage(Integer page) {
        if (page == null || page < 1) {
            this.page = 1;
        } else {
            this.page = page;
        }
    }

    public void setSize(Integer size) {
        if (size == null || size < 1) {
            this.size = 10;
        } else {
            this.size = size;
        }
    }
	
	public int getOffset() {
		return (page - 1) * size;
	}
}
