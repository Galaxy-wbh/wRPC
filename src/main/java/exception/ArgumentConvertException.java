package exception;

public class ArgumentConvertException extends RuntimeException {

    public ArgumentConvertException(String message) {
        super(message);
    }

    public ArgumentConvertException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArgumentConvertException(Throwable cause) {
        super(cause);
    }
}
