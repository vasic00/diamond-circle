package exceptions;

public class IllegalMatrixDimensionException extends Exception {
	
    private static final String ILLEGAL_MATRIX_DIMENSION_MESSAGE = "Invalid matrix dimension.";

    public IllegalMatrixDimensionException() {
        this(ILLEGAL_MATRIX_DIMENSION_MESSAGE);
    }

    public IllegalMatrixDimensionException(String message) {
        super(message);
    }
}