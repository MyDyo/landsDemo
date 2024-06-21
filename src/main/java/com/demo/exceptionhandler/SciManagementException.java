package com.demo.exceptionhandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SciManagementException extends RuntimeException {
    private Integer code;
    private String msg;

}
