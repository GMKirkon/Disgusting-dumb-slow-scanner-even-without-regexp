import java.io.*;
import java.util.Arrays;

public final class Reverse {
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, Exception {
        Scanner in = new Scanner(System.in);
        
        int[][] matrix = new int[1][];
        int matrixSize = 0;
        
        while (in.hasNextLine()) {
            String currentString = in.nextLine();
            Scanner currentLine = new Scanner(currentString);
            int[] currentInts = new int[1];
            int currentIntsSize = 0;
            while (currentLine.hasNextInt()) {
                int x = currentLine.nextInt();
                if (currentIntsSize == currentInts.length) {
                    currentInts = Arrays.copyOf(currentInts, currentInts.length * 2);
                }
                currentInts[currentIntsSize++] = x;
            }
            if (matrixSize == matrix.length) {
                matrix = Arrays.copyOf(matrix, matrix.length * 2);
            }
            matrix[matrixSize++] = Arrays.copyOf(currentInts, currentIntsSize);
        }
        
        for (int i = matrixSize - 1; i >= 0; i--) {
            for (int j = matrix[i].length - 1; j > 0; j--) {
                System.out.print(matrix[i][j] + " ");
            }
            if (matrix[i].length >= 1) {
                System.out.println(matrix[i][0]);
            } else {
                System.out.println();
            }
        }
    }
}