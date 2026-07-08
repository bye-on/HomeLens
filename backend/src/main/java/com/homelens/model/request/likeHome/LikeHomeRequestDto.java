package com.homelens.model.request.likeHome;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeHomeRequestDto {
    private int userId;
    private int itemId;
}
