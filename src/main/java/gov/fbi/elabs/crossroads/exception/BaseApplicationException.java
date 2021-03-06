package gov.fbi.elabs.crossroads.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;


public class BaseApplicationException extends Exception{
	
	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = LoggerFactory.getLogger(BaseApplicationException.class);
	
	private String errorMessage;
	private String errorCode;
	
	HttpStatus status;
	
	
	public BaseApplicationException(HttpStatus status,String errorCode, String errorMessage){
		this.errorMessage = errorMessage;
		this.status = status;
		this.errorCode = errorCode;
		logger.error(errorMessage);
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public BaseApplicationException (Throwable cause){
		logPrintStackTrace(cause);
	}
	
	public BaseApplicationException(){
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	public void setStatus(HttpStatus status) {
		this.status = status;
	}
	
	private void logPrintStackTrace(Throwable cause) {
		StringWriter errors = new StringWriter();
		cause.printStackTrace(new PrintWriter(errors));
		logger.error(cause.toString());
	}
	
}
