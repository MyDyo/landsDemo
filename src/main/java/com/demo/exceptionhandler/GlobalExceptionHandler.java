package com.demo.exceptionhandler;

import com.demo.util.resultCode.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SciManagementException.class)
    @ResponseBody
    public R error(SciManagementException e){
         e.printStackTrace();
         return R.error().code(e.getCode()).message(e.getMsg());
    }
}
