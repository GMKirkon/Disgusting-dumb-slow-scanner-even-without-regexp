import java.util.InputMismatchException;

public interface StringsSolver extends TypeSolver {
    
    
    //checks whether symbol matches type or not
    //if matches return 1 otherwise returns 0
    @Override
    default boolean check(char symbol) {
        return !Character.isWhitespace(symbol) && symbol != '\r' && symbol != '\n';
    }
    
    
    //String differs a lot from any other class. It's the only class with overloaded +...
    //So let's solve strings format with another very special abstract class
    
    //Parses string from string, throws exception if it can't match.
    //sounds crazy, right? But done this way in order to solve some crazy modifications with mind-blowing input format
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
