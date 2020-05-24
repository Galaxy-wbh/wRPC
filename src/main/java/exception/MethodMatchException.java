package exception;

public class MethodMatchException extends RuntimeException {

    public MethodMatchException(String message){
        super(message);
    }

    public MethodMatchException(String message, Throwable cause){
        super(message, cause);
    }

    public MethodMatchException(Throwable cause){
        super(cause);
    }
}
