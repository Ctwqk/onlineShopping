package com.taiwei.reggie.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * handle exception globally
 *
 */
@ControllerAdvice(annotations = {Controller.class,RestController.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {


    /**
     * deal with exceptions, intercept and handle
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());

        if(ex.getMessage().contains("Duplicate entry")){
            String message = ex.getMessage().split(" ")[2]+ " Exists";
            return R.error(message);
        }
        return R.error("oops, something is wrong");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());


        return R.error(ex.getMessage());
    }
}
