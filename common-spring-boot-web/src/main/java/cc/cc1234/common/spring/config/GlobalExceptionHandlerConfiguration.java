package cc.cc1234.common.spring.config;

import cc.cc1234.common.spring.web.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerConfiguration {

    @Bean
    GlobalExceptionHandler ikeaCustomExceptionHandler() {
        return new GlobalExceptionHandler();
    }

}
