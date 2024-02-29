import java.util.InputMismatchException;

public interface LongSolver extends TypeSolver {
    
    @Override
    default boolean check (char c) {
        return Character.isDigit(c) || c == '-';
    }
    
    @Override
    default Long parse (String s) throws InputMismatchException {
        try{
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new InputMismatchException("Specified string doesn't match with Long format" + e.getMessage());
        }
    }
}
