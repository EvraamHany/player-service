package playerService.exception;

public class PlayerInactiveException extends RuntimeException {
    public PlayerInactiveException(String message) {
        super(message);
    }
}
