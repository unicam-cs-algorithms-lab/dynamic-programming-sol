package it.unicam.cs.asdl.esdpsol;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test delle proprietà, non della particolare scelta di traceback.
 * Molti problemi ammettono allineamenti ottimi diversi: ciascuno va bene
 * purché ricostruisca le stringhe e produca il valore ottimo.
 */
class SequenceAlignmentTest {
    private static String ungap(String s) {
        return s.replace("-", "");
    }

    private static int editCost(AlignmentResult r) {
        int cost = 0;
        for (int i = 0; i < r.getFirst().length(); i++)
            if (r.getFirst().charAt(i) != r.getSecond().charAt(i)) cost++;
        return cost;
    }

    private static int globalScore(AlignmentResult r) {
        int score = 0;
        for (int i = 0; i < r.getFirst().length(); i++) {
            char a = r.getFirst().charAt(i), b = r.getSecond().charAt(i);
            score += a != '-' && b != '-' && a == b ? 1 : -1;
        }
        return score;
    }

    private static int localScore(AlignmentResult r) {
        int score = 0;
        for (int i = 0; i < r.getFirst().length(); i++) {
            char a = r.getFirst().charAt(i), b = r.getSecond().charAt(i);
            score += a == '-' || b == '-' ? -2 : (a == b ? 2 : -1);
        }
        return score;
    }

    private static void assertEdit(String x, String y, int expected) {
        EditDistanceSolver s = new EditDistanceSolver(x, y);
        assertFalse(s.isSolved());
        assertThrows(IllegalStateException.class, s::getDistance);
        assertThrows(IllegalStateException.class, s::getOneOptimalAlignment);
        s.solve();
        s.solve();
        assertTrue(s.isSolved());
        assertEquals(expected, s.getDistance());
        AlignmentResult r = s.getOneOptimalAlignment();
        assertEquals(x, ungap(r.getFirst()));
        assertEquals(y, ungap(r.getSecond()));
        assertEquals(expected, r.getScore());
        assertEquals(expected, editCost(r));
    }

    private static void assertGlobal(String x, String y, int expected) {
        GlobalAlignmentSolver s = new GlobalAlignmentSolver(x, y);
        assertThrows(IllegalStateException.class, s::getOptimalScore);
        assertThrows(IllegalStateException.class, s::getOneOptimalAlignment);
        s.solve();
        s.solve();
        assertEquals(expected, s.getOptimalScore());
        AlignmentResult r = s.getOneOptimalAlignment();
        assertEquals(x, ungap(r.getFirst()));
        assertEquals(y, ungap(r.getSecond()));
        assertEquals(expected, r.getScore());
        assertEquals(expected, globalScore(r));
    }

    private static void assertLocal(String x, String y, int expected) {
        LocalAlignmentSolver s = new LocalAlignmentSolver(x, y);
        s.solve();
        s.solve();
        AlignmentResult r = s.getOneOptimalAlignment();
        assertEquals(expected, s.getOptimalScore());
        assertEquals(expected, r.getScore());
        assertEquals(expected, localScore(r));
        assertTrue(x.contains(ungap(r.getFirst())));
        assertTrue(y.contains(ungap(r.getSecond())));
    }

    /**
     * Casi base: una o entrambe le stringhe vuote.
     */
    @Test
    void emptyStrings() {
        assertEdit("", "", 0);
        assertEdit("ABC", "", 3);
        assertEdit("", "ABC", 3);
        assertGlobal("", "", 0);
        assertGlobal("ABC", "", -3);
        assertGlobal("", "ABC", -3);
        assertLocal("", "ABC", 0);
        assertLocal("ABC", "", 0);
        assertLocal("", "", 0);
    }

    /**
     * Match, mismatch, inserimento e cancellazione isolati.
     */
    @Test
    void elementaryOperations() {
        assertEdit("A", "A", 0);
        assertEdit("A", "B", 1);
        assertEdit("AB", "A", 1);
        assertEdit("A", "AB", 1);
        assertEdit("AB", "BA", 2);
        assertEdit("kitten", "sitting", 3);
        assertGlobal("A", "A", 1);
        assertGlobal("A", "B", -1);
        assertGlobal("AB", "A", 0);
    }

    /**
     * Esempio discusso nelle dispense: ALBERO / LABBRO.
     */
    @Test
    void historicExample() {
        assertEdit("ALBERO", "LABBRO", 3);
        assertGlobal("ALBERO", "LABBRO", 1);
    }

    /**
     * Ripetizioni e casi con più soluzioni ottime: il test verifica il costo.
     */
    @Test
    void tiesAndDuplicates() {
        assertEdit("AAAA", "AA", 2);
        assertEdit("ABAB", "BABA", 2);
        assertGlobal("AAAA", "AA", 0);
        assertGlobal("AB", "BA", -1);
    }

    /**
     * Il traceback locale deve partire dal massimo interno e fermarsi a zero.
     */
    @Test
    void localInternalMatches() {
        assertLocal("TTACGTAA", "GGACGTCC", 8);
        assertLocal("ABC", "XYZ", 0);
        assertLocal("AAAA", "AA", 4);
    }

    /**
     * Un risultato non può contenere due gap nella stessa colonna.
     */
    @Test
    void resultValidation() {
        assertThrows(IllegalArgumentException.class, () -> new AlignmentResult("-", "-", 0));
        assertThrows(IllegalArgumentException.class, () -> new AlignmentResult("A", "AB", 0));
        assertThrows(NullPointerException.class, () -> new AlignmentResult(null, "", 0));
    }

    /**
     * I costruttori validano input null e simbolo riservato.
     */
    @Test
    void invalidInputs() {
        assertThrows(NullPointerException.class, () -> new EditDistanceSolver(null, "A"));
        assertThrows(IllegalArgumentException.class, () -> new EditDistanceSolver("A-", "B"));
        assertThrows(NullPointerException.class, () -> new GlobalAlignmentSolver("A", null));
        assertThrows(IllegalArgumentException.class, () -> new GlobalAlignmentSolver("-", "A"));
        assertThrows(NullPointerException.class, () -> new LocalAlignmentSolver(null, ""));
    }

    // Oracoli indipendenti bottom-up: non condividono il codice ricorsivo
    // delle soluzioni, perciò intercettano errori nella memoizzazione.
    private static int oracleED(String x, String y) {
        int[][] d = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) d[i][0] = i;
        for (int j = 0; j <= y.length(); j++) d[0][j] = j;
        for (int i = 1; i <= x.length(); i++)
            for (int j = 1; j <= y.length(); j++)
                d[i][j] = Math.min(d[i - 1][j] + 1, Math.min(d[i][j - 1] + 1,
                        d[i - 1][j - 1] +
                                (x.charAt(i - 1) == y.charAt(j - 1) ? 0 :
                                        1)));
        return d[x.length()][y.length()];
    }

    private static int oracleGlobal(String x, String y) {
        int[][] d = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) d[i][0] = -i;
        for (int j = 0; j <= y.length(); j++) d[0][j] = -j;
        for (int i = 1; i <= x.length(); i++)
            for (int j = 1; j <= y.length(); j++)
                d[i][j] = Math.max(d[i - 1][j] - 1, Math.max(d[i][j - 1] - 1,
                        d[i - 1][j - 1] +
                                (x.charAt(i - 1) == y.charAt(j - 1) ? 1 :
                                        -1)));
        return d[x.length()][y.length()];
    }

    private static int oracleLocal(String x, String y) {
        int[][] d = new int[x.length() + 1][y.length() + 1];
        int best = 0;
        for (int i = 1; i <= x.length(); i++)
            for (int j = 1; j <= y.length(); j++) {
                d[i][j] = Math.max(0, Math.max(
                        d[i - 1][j] - 2, Math.max(d[i][j - 1] - 2,
                                d[i - 1][j - 1] +
                                        (x.charAt(i - 1) == y.charAt(j - 1) ?
                                                2 : -1))));
                best = Math.max(best, d[i][j]);
            }
        return best;
    }

    /**
     * Centinaia di coppie piccole, generate con seme riproducibile.
     */
    @Test
    void randomizedCrossCheckWithIndependentOracles() {
        Random random = new Random(20261113L);
        for (int t = 0; t < 250; t++) {
            int n = random.nextInt(9), m = random.nextInt(9);
            StringBuilder x = new StringBuilder(), y = new StringBuilder();
            for (int i = 0; i < n; i++)
                x.append("ABC".charAt(random.nextInt(3)));
            for (int j = 0; j < m; j++)
                y.append("ABC".charAt(random.nextInt(3)));
            String a = x.toString(), b = y.toString();
            assertEdit(a, b, oracleED(a, b));
            assertGlobal(a, b, oracleGlobal(a, b));
            assertLocal(a, b, oracleLocal(a, b));
        }
    }

    /**
     * Invarianti di simmetria della distanza.
     */
    @Test
    void distanceSymmetry() {
        assertEdit("ABCDEF", "BADCE", oracleED("ABCDEF", "BADCE"));
        assertEquals(oracleED("ABCDEF", "BADCE"), oracleED("BADCE", "ABCDEF"));
    }

    /**
     * LCS è un esempio svolto: controllare anche le sue API storiche.
     */
    @Test
    void lcsExample() {
        LCSSolver s = new LCSSolver("ABCBDAB", "BDCABA");
        assertFalse(s.isSolved());
        assertThrows(IllegalStateException.class, s::getOneSolution);
        s.solve();
        s.solve();
        assertEquals(4, s.getLengthOfSolution());
        assertEquals(4, s.getOneSolution().length());
        assertTrue(s.isCommonSubsequence(s.getOneSolution()));
    }
}