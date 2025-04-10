package playerService.exception;

public class TimeLimitExceededException extends RuntimeException {
    public TimeLimitExceededException(String message) {
        super(message);
    }
}
