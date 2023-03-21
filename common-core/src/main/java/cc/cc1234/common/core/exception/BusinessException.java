package cc.cc1234.common.core.exception;

public class BusinessException extends RuntimeException {

    private final String errorCode;

    private Object[] args = new Object[0];

    public BusinessException(BusinessError businessError) {
        this.errorCode = businessError.getErrorCode();
    }

    public BusinessException(BusinessError businessError, Object[] args) {
        this.errorCode = businessError.getErrorCode();
        this.args = args;
    }

    public BusinessException(String errorCode) {
        this.errorCode = errorCode;
    }

    public BusinessException(String errorCode, Object[] args) {
        this.errorCode = errorCode;
        this.args = args;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getArgs() {
        return args;
    }

}
