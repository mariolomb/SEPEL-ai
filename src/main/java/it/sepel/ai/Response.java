package it.sepel.ai;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;

public class Response {

    private HttpStatus httpStatus;
    private Integer resultCode;
    private String resultMessage;
    private Object result;

    public Response(Object o) {
        resultCode = 0;
        resultMessage = "OK";
        httpStatus = HttpStatus.OK;
        
        if(o instanceof String) {
             result = o.toString();
        } else {
            result = o;
        }
    }
    
    public Response(Throwable t) {
        resultCode = -1;
        if(t instanceof AppException) {
            result = "";
        } else {
            String stacktrace = ExceptionUtils.getStackTrace(t);
            result = stacktrace;
        }
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        resultMessage = t.getLocalizedMessage();
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }
    
    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
