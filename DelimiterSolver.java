//It differs from TypeSolver a lot.
public interface DelimiterSolver {
    //noNextSymbol is only required for '\r\n' situation.
    
    
    //-1 -- for non Delimiter
    //0 -- for normal Delimiter, that does not create new line, or '\n' if it is following '\r'
    //1 -- for '\n' and '\r';
    
    default int check (char symbol) {
        if(symbol == '\n' || symbol == '\r') {
            return 1;
        }
        return Character.isWhitespace(symbol) ? 0 : -1;
    }
    
    default int check (char symbol, char previousSymbol) {
        if(previousSymbol == '\r' && symbol == '\n') {
            return 0;
        }
        return check(symbol);
    }
}
