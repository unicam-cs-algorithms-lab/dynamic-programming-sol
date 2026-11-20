package it.unicam.cs.asdl.esdpsol;

/**
 * Allineamento GLOBALE di due stringhe: tutte le posizioni di entrambe le
 * stringhe devono comparire nell'allineamento (eventualmente contro gap).
 * <p>Il punteggio è +1 per match, -1 per mismatch e -1 per gap.
 * La funzione S(i,j) è il MASSIMO punteggio sui prefissi x[0..i) e
 * y[0..j). I casi base sono S(i,0)=-i e S(0,j)=-j.
 * Per i,j>0 si sceglie il massimo tra diagonale + match/mismatch,
 * alto -1, sinistra -1. A differenza di EditDistanceSolver, qui si
 * MASSIMIZZA un punteggio che può essere NEGATIVO: la sentinella -1
 * non sarebbe sicura. Si usa quindi un array booleano {@code computed}.
 * <p>Top-down con memoizzazione: O(nm) tempo e spazio, stack O(n+m).
 */
public class GlobalAlignmentSolver {
    private final String x, y;
    private final int[][] memo;
    private final boolean[][] computed;
    private boolean solved;

    /**
     * Inizializza un nuovo risolutore per la coppia di stringhe.
     *
     * @param x prima stringa
     * @param y seconda stringa
     * @throws NullPointerException     se una stringa è null
     * @throws IllegalArgumentException se contiene il simbolo riservato '-'
     */
    public GlobalAlignmentSolver(String x, String y) {
        if (x == null || y == null)
            throw new NullPointerException("stringhe null");
        if (x.indexOf('-') >= 0 || y.indexOf('-') >= 0)
            throw new IllegalArgumentException("'-' riservato ai gap");
        this.x = x;
        this.y = y;
        memo = new int[x.length() + 1][y.length() + 1];
        computed = new boolean[x.length() + 1][y.length() + 1];
    }

    /**
     * Risolve il problema; le chiamate successive non ricalcolano nulla.
     */
    public void solve() {
        if (!solved) {
            score(x.length(), y.length());
            solved = true;
        }
    }

    /**
     * Restituisce un valore calcolato oppure uno stato del risolutore.
     *
     * @return il massimo punteggio globale
     * @throws IllegalStateException se solve non è stato chiamato
     */
    public int getOptimalScore() {
        requireSolved();
        return memo[x.length()][y.length()];
    }

    /**
     * Restituisce un valore calcolato oppure uno stato del risolutore.
     *
     * @return uno dei possibili allineamenti globali ottimi
     * @throws IllegalStateException se solve non è stato chiamato
     */
    public AlignmentResult getOneOptimalAlignment() {
        requireSolved();
        StringBuilder a = new StringBuilder(), b = new StringBuilder();
        traceBack(x.length(), y.length(), a, b);
        return new AlignmentResult(a.reverse().toString(), b.reverse()
                .toString(), getOptimalScore());
    }

    private void requireSolved() {
        if (!solved) throw new IllegalStateException("chiamare solve prima");
    }

    /**
     * SOLUZIONE 3. Restituire S(i,j) usando ricorsione memoizzata.
     * Prima controllare computed[i][j]; scrivere memo e impostare computed
     * anche nei casi base. NON usare -1 per marcare valori assenti!
     *
     * @param i lunghezza prefisso di x @param j lunghezza prefisso di y
     * @return migliore punteggio sui prefissi
     */
    private int score(int i, int j) {
        // I punteggi possono essere negativi: un booleano distingue le celle note.
        if (computed[i][j]) return memo[i][j];
        int value;
        if (i == 0) value = -j;
        else if (j == 0) value = -i;
        else {
            int s = x.charAt(i - 1) == y.charAt(j - 1) ? 1 : -1;
            value = Math.max(score(i - 1, j - 1) + s, Math.max(
                    score(i - 1, j) - 1, score(i, j - 1) - 1));
        }
        // Prima si scrive il valore, poi si marca lo stato come calcolato.
        computed[i][j] = true;
        memo[i][j] = value;
        return value;
    }

    /**
     * SOLUZIONE 4. Risalire fino a (0,0) seguendo solo transizioni
     * che realizzano il punteggio ottimo. Preferenza a parità: diagonale,
     * poi alto, poi sinistra. Durante la risalita inserire simboli alla fine
     * dei builder; il metodo pubblico li invertirà.
     *
     * @param i prefisso residuo x @param j prefisso residuo y
     * @param a prima riga rovesciata @param b seconda riga rovesciata
     */
    private void traceBack(int i, int j, StringBuilder a, StringBuilder b) {
        if (i == 0 && j == 0) return;
        // Si ricostruisce un percorso di massimo, non uno qualsiasi.
        if (i > 0 && j > 0) {
            int s = x.charAt(i - 1) == y.charAt(j - 1) ? 1 : -1;
            if (memo[i][j] == score(i - 1, j - 1) + s) {
                a.append(x.charAt(i - 1));
                b.append(y.charAt(j - 1));
                traceBack(i - 1, j - 1, a, b);
                return;
            }
        }
        if (i > 0 && memo[i][j] == score(i - 1, j) - 1) {
            a.append(x.charAt(i - 1));
            b.append('-');
            traceBack(i - 1, j, a, b);
            return;
        }
        if (j > 0 && memo[i][j] == score(i, j - 1) - 1) {
            a.append('-');
            b.append(y.charAt(j - 1));
            traceBack(i, j - 1, a, b);
            return;
        }
        throw new IllegalStateException("nessun predecessore ottimo");
    }
}