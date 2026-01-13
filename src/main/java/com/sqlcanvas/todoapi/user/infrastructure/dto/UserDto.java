package com.sqlcanvas.todoapi.user.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserDto(
        Integer id,

        @NotBlank(message = "名前は必須です！")
        @Size(max = 20, message = "名前は20文字以内で！")
        String name
) {
}
