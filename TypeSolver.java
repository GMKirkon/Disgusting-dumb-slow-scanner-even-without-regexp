import java.util.InputMismatchException;

//interface is called a Solver because it neither a Checker nor a Parser.
//But it solves your problems with regex, so it's a Solver! (LifeSaver).
public interface TypeSolver {
    
    //checks whether symbol matches type or not
    //if matches return 1 otherwise returns 0
    
    boolean check(char symbol);
    
    //parses specified Type from string, throws exception if it can't match.
    Object parse(String s) throws InputMismatchException;
}
