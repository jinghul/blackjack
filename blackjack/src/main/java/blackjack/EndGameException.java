package blackjack;

public class EndGameException extends Exception {
    private static final long serialVersionUID = 1L;

    public EndGameException() {super();}
    public EndGameException(String message) { super(message); }
    public EndGameException(String message, Throwable cause) { super(message, cause); }
    public EndGameException(Throwable cause) { super(cause); }
}