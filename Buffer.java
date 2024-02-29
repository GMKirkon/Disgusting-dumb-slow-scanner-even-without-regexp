import java.util.Arrays;


//Simplified version of Arraylist with some extra features for scanner
//Implemented to improve performance and to split code into smaller parts.
public class Buffer {
    
    //private instead of protected for potential IntBuffer,
    //don't want RandomBuffer to have some useless char[] buf.
    private char[] buf;
    
    protected int initialBufferSize = 128;
    protected int bufferSize;
    protected int bufferPosition;
    
    protected int bufferLastFilledElement = 0;
    
    protected int savedPosition = -1;


    //done this way for @Override and good usage of super constructors.
    protected void createBuf() {
        this.buf = new char[bufferSize];
    }
    
    public Buffer() {
        this.bufferSize = initialBufferSize;
        createBuf();
    }
    
    public Buffer(int bufferSize) {
        
        if (initialBufferSize < bufferSize) {
            initialBufferSize = bufferSize;
        }
        this.bufferSize = initialBufferSize;
        createBuf();
    }
    
    public Buffer(String s) {
        bufferSize = initialBufferSize;
        buf = new char[bufferSize];
        while (s.length() > bufferSize) {
            expand();
        }
        bufferPosition = 0;
        bufferLastFilledElement = s.length();
        for (int i = 0; i < s.length(); i++) {
            buf[i] = s.charAt(i);
        }
    }
    
    
    //it's a bit unsafe, for these methods to be public but so programmer should be well-aware of 
    //consequences for performance and potential data loss if the programmer really wants it.
    //the only solution that author does have for the moment is to rewrite functions from Scanner directly into Bufffer,
    //so there is no need in calling saveState() and revert() from the outside.
    public void saveState() {
        savedPosition = bufferPosition;
    }
    
    public void revert() {
        if(savedPosition == -1) {
            return;
        }
        bufferPosition = savedPosition;
        savedPosition = -1;
    }
    
    private void expand() {
        this.bufferSize *= 2;
        this.buf = Arrays.copyOf(buf, bufferSize);
        return;
    }
    
    
    public boolean atEnd() {
        return bufferLastFilledElement == bufferPosition;
    }
    
    public char current() throws IllegalStateException {
        if (!atEnd()) {
            return buf[bufferPosition];
        }
        throw new IllegalStateException("Buffer has no information about next symbol");
    }
    
    public void goFurther() {
        if (atEnd()) {
            throw new IllegalStateException("Buffer has no information about next symbol");
        }
        bufferPosition++;
        return;
    }
    
    public void clear() {
        buf = new char[initialBufferSize];
        bufferSize = initialBufferSize;
        bufferPosition = 0;
        bufferLastFilledElement = 0;
        return;
    }
    
    public void add(char[] block) {
        
        while (bufferLastFilledElement + block.length >= bufferSize) {
            expand();
        }
        
        for (int i = 0; i < block.length; i++) {
            buf[bufferLastFilledElement + i] = block[i];
        }
        bufferLastFilledElement += block.length;
        
        return;
    }
    
    public void del(int numberOfUselessElements) {
        int newBufferSize = Math.max(bufferLastFilledElement - numberOfUselessElements, initialBufferSize);
        
        char[] tmpBuf = Arrays.copyOfRange(buf, numberOfUselessElements, bufferLastFilledElement);
        
        buf = new char[newBufferSize];
        System.arraycopy(tmpBuf, 0, buf, 0, tmpBuf.length);
        
        bufferSize = newBufferSize;
        bufferPosition = 0;
        bufferLastFilledElement -= numberOfUselessElements;
        assert bufferLastFilledElement >= 0;
        
    }
    
    
    
    public boolean hasNextToken(TypeSolver checker, boolean noMoreData) {
        
        if(atEnd()) {
            revert();
            return false;
        }
        
        if(savedPosition == bufferPosition && !checker.check(buf[bufferPosition])) {
            revert();
            return false;
        } else {
            bufferPosition++;
        }
        
        while (bufferPosition < bufferLastFilledElement)  {
            if (!checker.check(buf[bufferPosition])) {
                revert();
                return true;
            }
            bufferPosition++;
        }
        
        if(noMoreData) {
            revert();
        }
        
        //probably the Token is not fully inside buffer
        return noMoreData;
    }
    
    
    public boolean safeHasNextToken(TypeSolver checker, boolean noMoreData) {
        saveState();
        return hasNextToken(checker, noMoreData);
    }
    
    public String getLastToken(TypeSolver checker) {
        
        int len = 0;
        
        while(bufferPosition + len < bufferLastFilledElement && checker.check(buf[bufferPosition + len])){
            len++;
        }
        
        String result = new String(buf, bufferPosition, len);
        bufferPosition += len;
        if(bufferPosition >= bufferSize / 2) {
            del(bufferPosition);
        }
        
        return result;
    }
}

