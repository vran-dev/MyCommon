package cc.cc1234.common.core.exception;

public interface BusinessError {

    String getErrorCode();

    String getErrorMessage();

    default BusinessException toException() {
        return new BusinessException(this);
    }

    default void throwException() {
        throw toException();
    }
}
