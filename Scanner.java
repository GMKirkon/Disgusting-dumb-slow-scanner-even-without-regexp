import java.io.*;
import java.util.*;

public class Scanner implements Closeable, Iterator<String> {
    
    private final int READING_BLOCKS_SIZE = 128;
    private Buffer scannerBuffer;
    private Reader source;
    
    //Yes, you actually need all those solvers instead of regex.
    private NonLineSeparatorSolver nonLineSeparatorSolver = new NonLineSeparatorSolver() {};
    private DelimiterSolver delimiter = new DelimiterSolver() {};
    private StringsSolver wordsSolver = new WordsSolver() {};
    private IntSolver intSolver = new IntSolver() {};
    private LongSolver longSolver = new LongSolver() {};
    private OctalSolver octalSolver = new OctalSolver() {};
    
    
    private boolean scannerClosed;
    private boolean sourceClosed;
    
    
    //It's implemented to skip delimiter but remember about empty lines
    private int skippedNextLines;
    
    
    //Needed not to crash programs that never tries to access next elements even if there is none.
    //also any Exception except IOException is thrown directly or handled properly,
    //so lastException can't be anything but IOException
    IOException lastException = null;
    
    //Only to save some information from IOExceptions, however,
    //I don't know any useful application (writing code managed by Exceptions in not a good option so...)
    //But it's better to have access to some information, than to just lose IOException which you can't handle
    //And you don't want to cause some crashes in has() methods.
    //So you could just ignore some IOExceptions in has methods. But loosing information is bad.
    public IOException ioException() {
        return lastException;
    }
    
    private void setSource(Reader source) {
        this.source = source;
    }
    
    
    private void setNonLineSeparatorSolver(NonLineSeparatorSolver nonLineSeparatorSolver) {
        this.nonLineSeparatorSolver = nonLineSeparatorSolver;
    }
    private void setDelimiter(DelimiterSolver delimiter) {
        this.delimiter = delimiter;
    }
    
    private void setWordsSolver(WordsSolver wordsSolver) {
        this.wordsSolver = wordsSolver;
    }
    
    private void setIntSolver(IntSolver intSolver) {
        this.intSolver = intSolver;
    }
    
    private void setLongSolver(LongSolver longSolver) {
        this.longSolver = longSolver;
    }
    
    private void setOctalSolver(OctalSolver octalSolver) {
        this.octalSolver = octalSolver;
    }
    
    private void setScannerBuffer(Buffer scannerBuffer) {
        this.scannerBuffer = scannerBuffer;
    }
    
    
    public Scanner () {
        this.scannerClosed = true;
        this.sourceClosed = true;
        setSource(null);
        setScannerBuffer(null);
    }
    
    public Scanner (Reader source) {
        setSource(Objects.requireNonNull(source, "Provided Reader was null"));
        setScannerBuffer(new Buffer(READING_BLOCKS_SIZE));
    }
    
    
    
    //You probably will never use it, but you have such option. 
    //It's for this one crazy modification where 'a', 'b', 'c', ... , 'z' are garbage and all those whitespaces are useful.
    public Scanner (Reader source, NonLineSeparatorSolver nonLineSeparatorSolver, DelimiterSolver delimiter, WordsSolver wordsSolver,
                    IntSolver intSolver, LongSolver longSolver, OctalSolver octalSolver) {
        
        setSource(Objects.requireNonNull(source, "Provided Reader was null"));
        setNonLineSeparatorSolver(Objects.requireNonNull(nonLineSeparatorSolver, "Provided nonLineSeparatorSolver was null"));
        setDelimiter(Objects.requireNonNull(delimiter, "Provided delimiter was null"));
        setWordsSolver(Objects.requireNonNull(wordsSolver, "Provided wordsSolver was null"));
        setIntSolver(Objects.requireNonNull(intSolver, "Provided intSolver was null"));
        setLongSolver(Objects.requireNonNull(longSolver, "Provided intSolver was null"));
        setOctalSolver(Objects.requireNonNull(octalSolver, "Provided intSolver was null"));
        
        //Will not check for null because if it's null then you have huge problems, probably some JVM error.
        setScannerBuffer(new Buffer(READING_BLOCKS_SIZE));
        
    }
    
    public Scanner (String s) {
        setSource(new StringReader(s));
        setScannerBuffer(new Buffer(READING_BLOCKS_SIZE));
    }
    
    public Scanner (InputStream source) {
        setSource(new InputStreamReader(source));
        setScannerBuffer(new Buffer(READING_BLOCKS_SIZE));
    }
    
    private boolean readInput() {
        //no ensureOpen() and Exceptions because of has() methods.
        if (sourceClosed || scannerClosed) {
            return false;
        }
        
        char[] block = new char[READING_BLOCKS_SIZE];
        
        //End of stream is not IOException, so it's alright, but you sometimes call readInput in has() methods
        //So you don't want to crush the program.
        int read = -1;
        try {
            read = source.read(block);
        } catch (IOException e) {
            lastException = new IOException("Could not get character from provided source" + e.getMessage());
        }
        
        if (read == -1) {
            try {
                source.close();
            } catch (IOException e) {
                lastException = new IOException("Was not able to neither get next character nor close provided Source"+ e.getMessage());
            } finally {
                //Even if it's not we will think it is, because we can't get information from source.
                //Yes it's not safe to use that implementation of Scanner with source that depends
                //on something being delivered through network or something like it where delivery can take it's time.
                //But let's hope that loops like: while(!hasNext()) { sleep(1000); }
                //will never occur in prog_intro homeworks
                sourceClosed = true;
            }
            return false;
        } else {
            block = Arrays.copyOf(block, read);
            scannerBuffer.add(block);
            return true;
        }
    }
    
    
    //it won't throw any Exception if scanner was not closed, but the EOF was reached.
    //that recreates the behaviour from original java.util.scanner
    private void ensureOpen() {
        if(scannerClosed) {
            throw new IllegalStateException("Scanner was closed before");
        }
    }
    
    private boolean hasSkippedLines() {
        return skippedNextLines > 0;
    }
    

    //may change to boolean but will lose @Override
    @Override
    public void close() {
        
        if (scannerClosed) {
            return;
        }
        
        scannerClosed = true;
        
        try {
            source.close();
        } catch (IOException e) {
            lastException = new IOException("Was not able to close the initial source" + e.getMessage());
        } finally {
            source = null;
            sourceClosed = true;
        }
        
    }


    private boolean getInputStart() {
        if (scannerBuffer.atEnd()) {
            if (!readInput()) {
                return false;
            }
        }
        return true;
    }
    
    //skipDelimiter relies on a fact that there is no collisions with delimiter and word and number.
    //'\n' and '\r', and (!!!) '\r\n' are the delimiters that we should take care about
    //So you have to (suffer) write some not very clear code.
    private boolean skipDelimiter() throws IOException {
        
        //can set lastChar to anything except '\r', it's only required to process '\r\n' correctly,
        //unfortunately there is no better solution than manually checking '\r\n', I mean... regex is not a solution.
        char lastChar = 'a';
        int lastCheckResult;
        
        if (!getInputStart()) {
            return false;
        }
        
        while(true) {
            
            lastCheckResult = delimiter.check(scannerBuffer.current(), lastChar);
            if (lastCheckResult == -1) {
                return true;
            }
            lastChar = scannerBuffer.current();
            
            //To handle properly empty line from string like '\n\n'
            skippedNextLines += lastCheckResult;
            
            if (scannerBuffer.atEnd()) {
                //clearing the buffer safe because we saved lastChar,
                //and also we never skipped anything useful for words or numbers
                scannerBuffer.clear();
                if (!readInput()) {
                    return false;
                }
            } else {
                scannerBuffer.goFurther();
                if (scannerBuffer.atEnd() && !readInput()) {
                    break;
                }
            }
            
        }
        
        return false;
    }

    private boolean trySkipping() {
        boolean res = false;
        try {
            res = skipDelimiter();
        } catch (IOException e) {
            lastException = e;
            return false;
        }
        return res;   
    }
    
    @Override
    public boolean hasNext() {
        return hasNext(wordsSolver);
    }
    
    @Override
    public String next() throws NoSuchElementException {
        return next(wordsSolver);
    }
    
    public boolean hasNext(TypeSolver checker) {
        ensureOpen();


        //you should be sure that there are no collisions between garbage
        //and symbols that checker thinks is useful
        //so this scanner is not that ultimate as it's java.util. friend.
        //but no regex.
        trySkipping();

        if (hasSkippedLines()) {
            if(checker.check('\n')) {
                return true;
            }
        }
        
        if (!getInputStart()) {
            return false;
        }

        scannerBuffer.saveState();

        while (true) {
            
            boolean lastCheckResult = checker.check(scannerBuffer.current());
            if (!lastCheckResult) {
                break;
            }
            
            if (scannerBuffer.atEnd() && !readInput()) {
                break;
            }
            
            scannerBuffer.goFurther();
            
            if(scannerBuffer.atEnd() && !readInput()) {
                break;
            }
        }

        scannerBuffer.revert();
        
        return hasNextInBuffer(checker);
    }
    
    
    //very simple wrapper but it might get complicated for some modifications
    //it's much better to do all the job inside checker, but for some small modifications
    //it might be easier to change the function inside scanner and not the class outside
    private boolean hasNextInBuffer (TypeSolver checker) {
        return scannerBuffer.safeHasNextToken(checker, sourceClosed);
    }
    
    public String next(TypeSolver checker) throws NoSuchElementException, InputMismatchException {
        ensureOpen();
        
        //done this way because of numbers, also we assume that '\n', '\r', '\r\n' are all the same to checker.
        if (skippedNextLines > 0 && checker.check('\n')) {
            skippedNextLines--;
            //done this way in case you want to process empty string in some special way
            return wordsSolver.parse("");
        }
        skippedNextLines = 0;

        //probably could be optimized, but done this way for safety.
        if (!hasNext(checker)) {
            if ((sourceClosed) && (scannerBuffer.atEnd()))
                throw new NoSuchElementException();
            else
                throw new InputMismatchException();
        }
        
        return wordsSolver.parse(scannerBuffer.getLastToken(checker));
    }
    
    public String nextLine() {
        ensureOpen();
        
        if (!hasNextLine(wordsSolver)) {
            throw new InputMismatchException("There is no nextLine");
        }
        
        if (skippedNextLines > 0) {
            skippedNextLines--;
            return "";
        }
        
        scannerBuffer.saveState();
        while (!sourceClosed && !scannerBuffer.hasNextToken(nonLineSeparatorSolver, false)) {
            readInput();
        }
        scannerBuffer.revert();
        String answer = scannerBuffer.getLastToken(nonLineSeparatorSolver);
        scannerBuffer.goFurther();
        return answer;
    }
    
    
    public boolean hasNextLine() {
        return hasNextLine(wordsSolver);
    }

    public boolean hasNextLine(TypeSolver checker) {
        ensureOpen();
        if (hasSkippedLines()) {
            return true;
        }

        boolean res = trySkipping();
        if (hasSkippedLines()) {
            return true;
        }
        return res;
    }
    
    public boolean hasNextInt() {
        return hasNext(intSolver);
    }
    
    public boolean hasNextLong() {
        return hasNext(longSolver);
    }
    
    public boolean hasNextOctal() {
        return hasNext(octalSolver);
    }
    
    public int nextInt() throws NoSuchElementException, InputMismatchException {
        String result = next(intSolver);    
        return intSolver.parse(result); 
    }
    
    public Long nextLong() throws NoSuchElementException, InputMismatchException{
        String result = next(longSolver);    
        return longSolver.parse(result); 
    }
    
    public Long nextOctal() throws NoSuchElementException, InputMismatchException {
        String result = next(octalSolver);    
        return octalSolver.parse(result); 
    }
    
}