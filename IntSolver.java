import java.util.InputMismatchException;

public interface IntSolver extends TypeSolver {
    
    @Override
    default boolean check (char c) {
        return Character.isDigit(c) || c == '-';
    }
    
    @Override
    default Integer parse (String s) throws InputMismatchException {
        try{
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new InputMismatchException("Specified string doesn't match with Integer format " + e.getMessage());
        }
    }
}
