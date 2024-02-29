import java.util.InputMismatchException;

public interface NonLineSeparatorSolver extends StringsSolver {
    
    @Override
    default boolean check (char symbol) {
        return symbol != '\n' && symbol != '\r';
    }
    
    @Override
    default String parse (String s) throws InputMismatchException {
        for (int i = 0; i < s.length(); i++) {
            if (!check(s.charAt(i))) {
                throw new InputMismatchException("Specified string doesn't match with words format");
            }
        }
        return s;
    }
}
