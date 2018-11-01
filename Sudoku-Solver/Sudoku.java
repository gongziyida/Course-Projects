package cs445.a3;

import java.util.List;
import java.util.Arrays;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Sudoku {
    // The index of cells that keep track of the most recently-filled cell and
    // the next empty cell in a row
    private static final int RECORDER_ROW = 9, 
                             RECENT_COL = 0, RECENT_ROW = 1, NUM_EMPTY = 2,
                             NEXT_EMPTY_COL = 3, NEXT_EMPTY_ROW = 4;

    /**
     * Checks if a partial solution is a full solution
     * @param board The partial solution
     * @return true if the solution is a full solution, false if not
     */
    static boolean isFullSolution(int[][] board) {
        return board[RECORDER_ROW][NUM_EMPTY] == 0;
    }
    
    /**
     * Checks if a partial solution should be rejected since it cannot become a 
     * full solution
     * @param board The partial solution
     * @return true if the partial solution should be rejected, false if not
     */
    static boolean reject(int[][] board) {
        int r = board[RECORDER_ROW][RECENT_ROW], 
            c = board[RECORDER_ROW][RECENT_COL];
        if (c == -1) return false;
        int filledNum = board[r][c];

        // Checks if cells alone the same row have no more choice
        for (int ci = 0; ci < 9; ci++) {
            if (board[r][ci] == filledNum && ci != c) return true;
        }
        // Checks if cells alone the same column have no more choice
        for (int ri = 0; ri < 9; ri++) {
            if (board[ri][c] == filledNum && ri != r) return true;
        }
        // Checks remaining cells in the region of the most recently-filled cell
        for (int rj = r - r % 3; rj < r + 3 - r % 3; rj++){
                if (rj == r) continue;  // steps over cells checked
            for (int cj = c - c % 3; cj < c + 3 - c % 3; cj++){
                if (cj == c) continue;  // steps over cells checked
                if (board[rj][cj] == filledNum) return true;
            }
        }
        return false;
    }

    /**
     * Extends a partial solution by filling the next cell with 
     * the least possible number
     * @param partial the partial solution
     * @return a partial solution with one more cell filled with a number, 
     * or null if no possible number can be filled into that cell
     */
    public static int[][] extend(int[][] partial) {
        if (partial[RECORDER_ROW][NUM_EMPTY] == 0) return null;
        int r = partial[RECORDER_ROW][NEXT_EMPTY_ROW], 
            c = partial[RECORDER_ROW][NEXT_EMPTY_COL];
        int[][] temp = new int[10][9];
        for (int i = 0; i < 10; i++) {
            temp[i] = partial[i].clone();
        }
        
        temp[r][c] = 1;

            // update the recorder row
        temp[RECORDER_ROW][RECENT_ROW] = r;
        temp[RECORDER_ROW][RECENT_COL] = c;
        temp[RECORDER_ROW][NUM_EMPTY] -= 1;
        boolean firstEmpty = true;
        for (int i = r; i < 9 && firstEmpty && 
                        temp[RECORDER_ROW][NUM_EMPTY] != 0; i++) {
            for (int j = 0; j < 9 && firstEmpty; j++) {
                if (temp[i][j] == 0) {
                    temp[RECORDER_ROW][NEXT_EMPTY_ROW] = i;
                    temp[RECORDER_ROW][NEXT_EMPTY_COL] = j;
                    firstEmpty = false;
                    break;
                }
            }
        }
        return temp;
    }

    /**
     * Fills the most recently-filled cell with another possible number
     * @param partial The partial solution
     * @return a partial solution with the most recently-filled cell containing
     * another possible number, or null if there is no more possible number.
     */
    public static int[][] next(int[][] partial) {
        int r = partial[RECORDER_ROW][RECENT_ROW], 
            c = partial[RECORDER_ROW][RECENT_COL];
        int[][] temp = new int[10][9];
        for (int i = 0; i < 10; i++) {
            temp[i] = partial[i].clone();
        }
        if (temp[r][c] < 9) {
                temp[r][c] += 1;
                return temp;
        }
        return null;
    }

    /**
     * Tests the next method using partial and full solutions
     */
    static void testIsFullSolution() {
        String fullSolFileName = "./test/FS.su";
        String nonFullSolFileName = "./test/NFS.su";
        int [][] fullSol = readBoard(fullSolFileName);
        int [][] notFullSol = readBoard(nonFullSolFileName);

        System.out.println("\n\nThis should be full:");
        if (isFullSolution(fullSol)) {
            System.out.println("\nFull solution:");
            printBoard(fullSol);
        } else {
            System.out.println("\nNot full solution:");
            printBoard(fullSol);
        }

        System.out.println("\n\nThis should NOT be full:");
        if (isFullSolution(notFullSol)) {
            System.out.println("\nFull solution:");
            printBoard(notFullSol);
        } else {
            System.out.println("\nNot full solution:");
            printBoard(notFullSol);
        }
    }

    /**
     * Tests the reject method using partial solutions
     */
    static void testReject() {
        String[] rejFileNames = {"./test/Rj1.su", "./test/Rj2.su", "./test/Rj3.su"};
        String notRejFileName = "./test/NRj.su";
        int [][][] rejected = readBoard(rejFileNames);
        int [][] notRejected = readBoard(notRejFileName);

        // As reject method requires that the program keeps track of the most
        // recently-filled cell during the real solving process, (and it's impossible 
        // to have cells of wrong num before the most recent one,) the recorder cells
        // are set to a value for convenience.
        notRejected[RECORDER_ROW][RECENT_ROW] = 1;
        notRejected[RECORDER_ROW][RECENT_COL] = 5;
        rejected[0][RECORDER_ROW][RECENT_ROW] = 1;
        rejected[0][RECORDER_ROW][RECENT_COL] = 5;
        rejected[1][RECORDER_ROW][RECENT_ROW] = 1;
        rejected[1][RECORDER_ROW][RECENT_COL] = 5;
        rejected[2][RECORDER_ROW][RECENT_ROW] = 1;
        rejected[2][RECORDER_ROW][RECENT_COL] = 5;

        System.out.println("\n\nThis should NOT be rejected:");
        if (reject(notRejected)) {
            System.out.println("\nRejected:");
            printBoard(notRejected);
        } else {
            System.out.println("\nNot rejected:");
            printBoard(notRejected);
        }

        System.out.println("\n\nThese should be rejected:");
        System.out.println("\n1) A conflict in column");
        System.out.println("\n2) A conflict in row");
        System.out.println("\n3) A conflict in region");
        for (int[][] test : rejected) {
            if (reject(test)) {
                System.out.println("\nRejected:");
                printBoard(test);
            } else {
                System.out.println("\nNot rejected:");
                printBoard(test);
            }
        }
    }

    /**
     * Tests the extend method using partial solutions
     */
    static void testExtend() {
        String[] extendFileNames = {"./test/E1.su", 
                                    "./test/E2.su", 
                                    "./test/E3.su"};
        String noExtendFileName = "./test/NE.su";
        int [][][] extendable = readBoard(extendFileNames);
        int [][] inExtendable = readBoard(noExtendFileName);

        System.out.println("\n\nThis can NOT be extended:");
        System.out.println("\nExtend");
        printBoard(inExtendable);
        System.out.println("\nto");
        int [][] after = extend(inExtendable);
        if (after != null) printBoard(after);
        else System.out.println("\nCannot be extended!");

        System.out.println("\n\nThese can be extended:");
        System.out.println("\n1) Extend somewhere in the middle");
        System.out.println("\n2) Extend in the beginning");
        System.out.println("\n3) Extend in the end");
        for (int[][] test : extendable) {
            System.out.println("\nExtend");
            printBoard(test);
            System.out.println("\nto");
            after = extend(test);
            if (after != null) printBoard(after);
            else System.out.println("\nCannot be extended!");
        }
    }

    /**
     * Tests the next method using partial solutions
     */
    static void testNext() {
        String nextFileName = "./test/Nx.su";
        String noNextFileName = "./test/NNx.su";
        int [][] nextable = readBoard(nextFileName);
        int [][] inNextable = readBoard(noNextFileName);

        // As reject method requires that the program keeps track of the most
        // recently-filled cell during the real solving process, (and it's impossible 
        // to have cells of wrong num before the most recent one), the recorder cells
        // are set to a value for convenience.
        nextable[RECORDER_ROW][RECENT_ROW] = 2;
        nextable[RECORDER_ROW][RECENT_COL] = 8;
        inNextable[RECORDER_ROW][RECENT_ROW] = 1;
        inNextable[RECORDER_ROW][RECENT_COL] = 6;

        System.out.println("\n\nThis can NOT be nexted:");
        System.out.println("\n(Last extend: (2nd,7th))");
        System.out.println("\nNext");
        printBoard(inNextable);
        System.out.println("\nto");
        int [][] after = next(inNextable);
        if (after != null) printBoard(after);
        else System.out.println("\nCannot be nexted!");

        System.out.println("\n\nThis can be nexted:");
        System.out.println("\n(Last extend: (3nd,9th))");
        System.out.println("\nNext");
        printBoard(nextable);
        System.out.println("\nto");
        after = next(nextable);
        if (after != null) printBoard(after);
        else System.out.println("\nCannot be nexted!");
    }
    /**
     * print the board to the console
     * @param board the sudoku board to print
     */
    static void printBoard(int[][] board) {
        if (board == null) {
            System.out.println("No assignment");
            return;
        }
        for (int i = 0; i < 9; i++) {
            if (i == 3 || i == 6) {
                System.out.println("----+-----+----");
            }
            for (int j = 0; j < 9; j++) {
                if (j == 2 || j == 5) {
                    System.out.print(board[i][j] + " | ");
                } else {
                    System.out.print(board[i][j]);
                }
            }
            System.out.print("\n");
        }
    }

    /**
     * Reveives an array of filenames and call readBoard for those files
     * @param filenames an array of the names of the files to read
     * @return an array of sudoku boards
     */
    static int[][][] readBoard(String[] filenames) {
        int[][][] boards = new int[filenames.length][9][11];
        for (int i = 0; i < filenames.length; i++) {
            boards[i] = readBoard(filenames[i]);
        }
        return boards;
    }

    /**
     * Reveives a filename, reads the boad from the file, and append two cells at
     * the end of each row which keep track of the most recently-filled cell  
     * and the next empty cell
     * @param filename the name of the file to read
     * @return a sudoku board
     */
    static int[][] readBoard(String filename) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(filename), Charset.defaultCharset());
        } catch (IOException e) {
            return null;
        }
        int[][] board = new int[10][9];
        int val = 0, numEmpty = 0;
        boolean firstEmpty = true;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                try {
                    val = Integer.parseInt(Character.toString(lines.get(i).charAt(j)));
                } catch (Exception e) {
                    val = 0;
                    numEmpty++;
                    if (firstEmpty) {
                    board[RECORDER_ROW][NEXT_EMPTY_ROW] = i;
                    board[RECORDER_ROW][NEXT_EMPTY_COL] = j;
                    firstEmpty = false;
                    }
                }
                board[i][j] = val;
            }
        }
        board[RECORDER_ROW][RECENT_ROW] = -1;
        board[RECORDER_ROW][RECENT_COL] = -1;
        board[RECORDER_ROW][NUM_EMPTY] = numEmpty;
        return board;
    }

    /**
     * Solves the provided sudoku and output one solution
     * @param board the sudoku board
     * @return the full solution
     */
    static int[][] solve(int[][] board) {
        if (reject(board)) return null;
        if (isFullSolution(board)) return board;
        int[][] attempt = extend(board);
        while (attempt != null) {
            int[][] solution = solve(attempt);
            if (solution != null) return solution;
            attempt = next(attempt);
        }
        return null;
    }

    public static void main(String[] args) {
        if (args[0].equals("-t")) {
            testIsFullSolution();
            testReject();
            testExtend();
            testNext();
        } else {
            //long end, elapse;
            //long start = System.nanoTime();
            int[][] board = readBoard(args[0]);
            printBoard(board);
            System.out.println("\nSolution:\n");
            int[][] solution = solve(board);
            if (solution != null)
                printBoard(solution);
            else System.out.println("No Solution");
            //end = System.nanoTime();
            //elapse = end - start;
            //System.out.println((double)elapse / 1000000000.0);
        }
    }
}

