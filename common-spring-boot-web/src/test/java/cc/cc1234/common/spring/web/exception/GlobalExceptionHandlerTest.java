package cc.cc1234.common.spring.web.exception;

import cc.cc1234.common.core.ErrorResponse;
import cc.cc1234.common.core.exception.BusinessError;
import cc.cc1234.common.core.exception.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Locale;

public class GlobalExceptionHandlerTest {

    private MessageSource messageSource;

    private GlobalExceptionHandler handler;

    @BeforeEach
    public void before() {
        ResourceBundleMessageSource resource = new ResourceBundleMessageSource();
        resource.setDefaultEncoding("UTF-8");
        resource.addBasenames("messages");
        this.messageSource = resource;
        handler = new GlobalExceptionHandler(messageSource);
    }

    @Test
    void testHandleBusinessException_withDefaultMessage() {
        BusinessError businessError = () -> "error.code";
        WebRequest webRequest = Mockito.mock(WebRequest.class);
        Mockito.when(webRequest.getDescription(Mockito.anyBoolean()))
            .thenReturn("/hello/test");
        BusinessException exception = new BusinessException(businessError);
        ResponseEntity<Object> response = handler.handleBusinessException(exception, webRequest, Locale.CHINA);
        ErrorResponse body = (ErrorResponse) response.getBody();
        Assertions.assertEquals("error message", body.getErrorMessage());
    }

    @Test
    void testHandleBusinessException_withI18Message() {
        BusinessError businessError = () -> "error.user.not_found";
        WebRequest webRequest = Mockito.mock(WebRequest.class);
        Mockito.when(webRequest.getDescription(Mockito.anyBoolean())).thenReturn("/hello/test");
        BusinessException exception = new BusinessException(businessError);
        ResponseEntity<Object> response = handler.handleBusinessException(exception, webRequest, Locale.CHINA);
        ErrorResponse body = (ErrorResponse) response.getBody();
        Assertions.assertEquals("用户不存在", body.getErrorMessage());
    }
}