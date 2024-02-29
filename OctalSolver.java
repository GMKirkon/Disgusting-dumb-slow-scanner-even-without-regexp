import java.util.InputMismatchException;

public interface OctalSolver extends TypeSolver {
    
    @Override
    default boolean check(char c) {
        return Character.isDigit(c) || c == '-' || c == 'o' || c == 'O';
    }
    
    @Override
    default Long parse(String s) throws InputMismatchException {
        
        char lastChar = s.charAt(s.length() - 1);
        
        if (lastChar != 'o' && lastChar != 'O') {
            throw new InputMismatchException("Specified string doesn't match with LongOctal format, there is no 'o' or 'O' at the end");
        }
        
        try {
            return Long.parseLong(s, 8);
        } catch (NumberFormatException e) {
            throw new InputMismatchException("Specified string doesn't match with Long format" + e.getMessage());
        }
    }
}
