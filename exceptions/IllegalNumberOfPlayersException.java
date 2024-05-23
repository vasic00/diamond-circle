package exceptions;

public class IllegalNumberOfPlayersException extends Exception {

    private static final String ILLEGAL_NUMBER_OF_PLAYERS_MESSAGE = "Invalid number of players.";

    public IllegalNumberOfPlayersException() {
        this(ILLEGAL_NUMBER_OF_PLAYERS_MESSAGE);
    }

    public IllegalNumberOfPlayersException(String message) {
        super(message);
    }
}