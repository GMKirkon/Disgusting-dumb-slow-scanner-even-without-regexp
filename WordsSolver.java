import java.util.InputMismatchException;

//Default implementation of StringsSolver
//For modifications you probably want ModificatedWordsSolver
public interface WordsSolver extends StringsSolver {
    
    @Override
    default boolean check (char symbol) {
        return !Character.isWhitespace(symbol);
    }
    
    @Override
    default String parse (String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!check(s.charAt(i))) {
                throw new InputMismatchException("Specified string doesn't match with words format");
            }
        }
        return s;
    }
}
