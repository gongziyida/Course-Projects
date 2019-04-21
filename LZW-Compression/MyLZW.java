/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/
public class MyLZW {
    private static final int R = 256;   // number of input chars
    private static final double THERESHOLD = 1.1; // thereshold for the ratio in m mode
    private static final int W_MAX = 16; // thereshold for the ratio in m mode
    private static final int L_MAX = 2 << 15; // thereshold for the ratio in m mode
    private static int L; // number of codewords = 2^W
    private static int W;   // range of codeword width

    /**
     * Reset L, W, as well as the TST which contains only ASCII afterward.
     * @return the new TST
     */
    private static TST resetTST() {
        L = 2 << 8;
        W = 9;
        TST st = new TST<Integer>();
        // initialize the TST with all 1-character strings
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        return st;
    }

    /**
     * Reset L, W, as well as the String array which contains only 
     * ASCII afterward.
     * @return the new String array
     */
    private static String[] resetStringArray() {
        String[] st = new String[L_MAX];
        L = 2 << 8;
        W = 9;
        int i;
        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = ""; // (unused) lookahead for EOF
        return st;
    }

    public static void compress(char flag) { 
        StringBuilder input = new StringBuilder(BinaryStdIn.readString());
        TST<Integer> st = resetTST();
        int code = R+1;  // R is codeword for EOF
        double oldRatio = 0.0, newRatio, compressed = 0.0, uncompressed = 0.0;

        BinaryStdOut.write(flag + "", 8); // mode notation

        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();

            // Calculate the uncompressed size of the processed part so far
            uncompressed += t * 8.0;
            // Calculate the compressed size of the processed part so far 
            compressed += (double)W;

            if (code == L) {
                if (W < W_MAX) L = 2 << (W++); // need increase L
                // otherwise, code > L_MAX
                else if (flag == 'r') {
                    st = resetTST();
                    code = R + 1;
                } else if (flag == 'm') {
                    newRatio = uncompressed / compressed;
                    if (oldRatio/newRatio > THERESHOLD) {
                        st = resetTST();
                        code = R + 1;
                    }
                }
            }

            if (t < input.length() && code < L) {// Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
                oldRatio = uncompressed / compressed;
            }
            input.delete(0, t); // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void expand() {
        String[] st = resetStringArray();
        int i = R + 1; // next available codeword value
        double oldRatio = 0.0, newRatio, uncompressed = 0.0, compressed = 0.0;

        char flag = (char)BinaryStdIn.readInt(8);
        if (flag != 'r' && flag != 'm' && flag != 'n')
            throw new IllegalArgumentException("Invalid compression mode: " + flag);

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return; // expanded message is empty string
        String val = st[codeword];

        while (true) {
            BinaryStdOut.write(val);
            
            // Calculate the uncompressed size of the processed part so far
            uncompressed += val.length() * 8.0;
            // Calculate the compressed size of the processed part so far 
            compressed += (double)W;

            if (i == L) {
                if (W < W_MAX) L = 2 << (W++); // need increase L
                // otherwise, i > L_MAX
                else if (flag == 'r') {
                    st = resetStringArray();
                    i = R + 1;
                } else if (flag == 'm') {
                    newRatio = uncompressed / compressed;
                    if (oldRatio/newRatio > THERESHOLD) {
                        st = resetStringArray();
                        i = R + 1;
                    }
                }
            }

            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break; // termination char
            String s = st[codeword];
            
            
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L) {
                st[i++] = val + s.charAt(0); 
                oldRatio = uncompressed / compressed;
            }
            val = s;
        }
        BinaryStdOut.close();
    }



    public static void main(String[] args) {
        if (args[0].equals("+")) expand(); 
        else if (args[0].equals("-")) {
            char flag = args[1].charAt(0);
            if ((flag != 'n' && flag != 'r' && flag != 'm') || args[1].length() > 1)
                throw new IllegalArgumentException("Illegal command line argument");
            compress(flag);
        } 
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
