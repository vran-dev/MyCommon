package cc.cc1234.common.spring.web.exception;

import cc.cc1234.common.core.BusinessErrorResponse;
import cc.cc1234.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolationException(
        ConstraintViolationException constraintViolationException, WebRequest request) {

        String errorMsg = "";
        String path = getPath(request);

        Set<ConstraintViolation<?>> violations = constraintViolationException.getConstraintViolations();
        for (ConstraintViolation<?> item : violations) {
            errorMsg = item.getMessage();
            log.warn("ConstraintViolationException, request: {}, exception: {}, invalid value: {}",
                path, errorMsg, item.getInvalidValue());
            break;
        }
        return failInfo(errorMsg, HttpStatus.BAD_REQUEST, path);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    protected ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        String path = getPath(request);
        log.warn("IllegalArgumentException, request: {}, exception: {}", path, ex.getMessage());
        return failInfo(ex.getMessage(), HttpStatus.BAD_REQUEST, path);
    }

    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<Object> handleBusinessException(
        BusinessException businessException, WebRequest request) {
        String path = getPath(request);
        log.warn("BusinessException, request: {}, exception: {}", path, businessException.getMessage());
        // 自定义 499 业务异常
        return ResponseEntity
            .status(499)
            .body(new BusinessErrorResponse(businessException));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleUnspecificException(Exception ex, WebRequest request) {

        String path = getPath(request);
        String errorMsg = ex.getMessage();
        log.error("Unspecific exception, request: " + path + ", exception: " + errorMsg + ":", ex);
        return failInfo(errorMsg, HttpStatus.INTERNAL_SERVER_ERROR, path);
    }

    @Override
    public ResponseEntity<Object> handleBindException(
        BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String errorMsg = buildMessages(ex.getBindingResult());
        log.warn("BindException, request: {}, exception: {}", getPath(request), errorMsg);
        return handleOverriddenException(ex, headers, status, request, errorMsg);
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String errorMsg = buildMessages(ex.getBindingResult());
        log.warn("MethodArgumentNotValidException, request: {}, exception: {}", getPath(request), errorMsg);
        return handleOverriddenException(ex, headers, status, request, errorMsg);
    }

    @Override
    public ResponseEntity<Object> handleTypeMismatch(
        TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        log.warn("TypeMismatchException, request: {}, exception: {}", getPath(request), ex.getMessage());
        return handleOverriddenException(ex, headers, status, request, ex.getMessage());
    }

    @Override
    public ResponseEntity<Object> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        log.warn("MissingServletRequestParameterException, request: {}, exception: {}",
            getPath(request), ex.getMessage());
        return handleOverriddenException(ex, headers, status, request, ex.getMessage());
    }

    @Override
    public ResponseEntity<Object> handleMissingServletRequestPart(
        MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        log.warn("MissingServletRequestPartException, request: {}, exception: {}", getPath(request), ex.getMessage());
        return handleOverriddenException(ex, headers, status, request, ex.getMessage());
    }

    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String errorMsg = ex.getMostSpecificCause().getMessage();

        log.warn("HttpMessageNotReadableException, request: {}, exception: {}", getPath(request), errorMsg);
        return handleOverriddenException(ex, headers, status, request, errorMsg);
    }

    @Override
    public ResponseEntity<Object> handleServletRequestBindingException(
        ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        log.warn("ServletRequestBindingException, request: {}, exception: {}", getPath(request), ex.getMessage());
        return handleOverriddenException(ex, headers, status, request, ex.getMessage());
    }

    @Override
    public ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String errorMsg = ex.getMessage();
        log.warn("HttpRequestMethodNotSupportedException, request: {}, exception: {}", getPath(request), errorMsg);
        return handleOverriddenException(ex, headers, status, request, ex.getMessage());
    }

    private String buildMessages(BindingResult result) {

        StringBuilder resultBuilder = new StringBuilder();
        List<ObjectError> errors = result.getAllErrors();
        for (ObjectError error : errors) {
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                String fieldName = fieldError.getField();
                String fieldErrMsg = fieldError.getDefaultMessage();
                resultBuilder.append(fieldName).append(" ").append(fieldErrMsg);
            }
        }
        return resultBuilder.toString();
    }

    private ResponseEntity<Object> failInfo(String errorMsg, HttpStatus status, String path) {
        BusinessErrorResponse body = new BusinessErrorResponse(status.getReasonPhrase(), errorMsg);
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<Object> handleOverriddenException(
        Exception ex, HttpHeaders headers, HttpStatus status, WebRequest request, String errorMsg) {
        BusinessErrorResponse body = new BusinessErrorResponse(status.getReasonPhrase(), errorMsg);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    private String getPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.startsWith("uri=") ? description.substring(4) : description;
    }

}
