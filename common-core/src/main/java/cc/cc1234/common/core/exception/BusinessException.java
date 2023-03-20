package cc.cc1234.common.core.exception;

public class BusinessException extends RuntimeException {

    private BusinessError businessError;

    private String errorCode;

    private String errorMessage;

    public BusinessException(BusinessError businessError) {
        super(businessError.getErrorMessage());
        this.businessError = businessError;
        this.errorCode = businessError.getErrorCode();
        this.errorMessage = businessError.getErrorMessage();
    }

    public BusinessException(BusinessError businessError, String overrideMessage) {
        super(overrideMessage);
        this.businessError = businessError;
        this.errorCode = businessError.getErrorCode();
        this.errorMessage = overrideMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
