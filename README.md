# Programmazione dinamica: memoizzazione e confronto tra stringhe

## Obiettivo formativo

Sperimentare due realizzazioni della programmazione dinamica: **bottom-up** (LCS, già svolto)
e **top-down con memoizzazione** (edit distance e similarità globale). In entrambi i casi
si usa una matrice di risultati sui **prefissi**; cambia chi decide l'ordine di riempimento:
nel bottom-up lo decidono i cicli, nel top-down le chiamate ricorsive.
Il **traceback** è un passaggio distinto: ricostruisce un allineamento ottimo a partire dai
valori della matrice. È possibile che esistano più allineamenti ottimi.

## Struttura dei sorgenti

- `LCSSolver`: **esempio completo**. Lunghezza della più lunga
  **sottosequenza** comune e ricostruzione. Studiare `solve()` e `traceBack()`.
- `AlignmentResult`: classe immutabile già implementata, con due righe allineate e valore ottimo.
- `EditDistanceSolver`: **2 TODO obbligatori**: `distance(i,j)` e `traceBack(...)`.
- `GlobalAlignmentSolver`: **2 TODO obbligatori**: `score(i,j)` e `traceBack(...)`.
- `LocalAlignmentSolver`: **2 TODO facoltativi**: `score(i,j)` e `traceBack(...)`.
- `Demo`: esempi di chiamata; la parte che usa i TODO può essere eseguita dopo averli completati.
- `SequenceAlignmentTest`: test JUnit per casi base, correttezza, traceback e input casuali.

## Ordine consigliato per lo svolgimento

1. Leggere la Javadoc di `LCSSolver`; eseguire `solve()` su stringhe piccole, visualizzare
   la differenza fra una **sottosequenza** e una **sottostringa**.
2. Leggere la Javadoc di `EditDistanceSolver`; disegnare su carta D(i,j) per due prefissi.
3. Implementare `distance(i,j)` **ricorsivamente**: se `memo[i][j] != -1`, riusare il risultato;
   altrimenti valutare i casi base e le tre scelte, salvare la cella, restituirla.
4. Implementare `traceBack` per edit distance: partire da (n,m), avanzare verso (0,0)
   con **diagonale** (match/sostituzione), **alto** (cancellazione) o **sinistra**
   (inserimento). Ricostruire le due righe con gap `-`.
5. Passare a `GlobalAlignmentSolver`: cambiano MIN in MAX, i punteggi e i casi base.
   Usare la cache booleana `computed`: **-1 può essere un valore ottimo valido**.
6. Completare il relativo traceback e verificare il punteggio della soluzione.
7. Eseguire `mvn test` e verificare anche input vuoti, ripetuti e ambigui.
8. **Solo facoltativo**: ripetere con `LocalAlignmentSolver`, includendo l'alternativa 0;
   cercare il massimo in tutta la matrice e fermarsi a uno zero nel traceback.

## Ricorrenze e significato delle celle

Per le stringhe `x` e `y`, `i` e `j` sono **lunghezze di prefisso**, non indici di caratteri:
`x.charAt(i-1)` è il carattere finale del prefisso di lunghezza `i`.

### Edit distance (Levenshtein)

`D(0,j)=j`, `D(i,0)=i`; per `i,j>0`:

```
D(i,j) = min(D(i-1,j)+1, D(i,j-1)+1,
             D(i-1,j-1) + (x[i-1]==y[j-1] ? 0 : 1))
```

L'output è un **costo minimo**. Match 0, mismatch 1, gap 1.
Esempio: `D("ALBERO", "LABBRO") = 3`.

### Similarità globale (Needleman–Wunsch con gap lineare)

`S(0,j)=-j`, `S(i,0)=-i`; per `i,j>0`:

```
S(i,j) = max(S(i-1,j)-1, S(i,j-1)-1,
             S(i-1,j-1) + (x[i-1]==y[j-1] ? +1 : -1))
```

L'output è un **punteggio massimo**, anche negativo; match +1, mismatch -1, gap -1.
Esempio: `S("ALBERO", "LABBRO") = +1`.

### Similarità locale (facoltativo, Smith–Waterman)

Riga e colonna 0 valgono zero. Si massimizza tra `0`, diagonale +2/-1,
verticale -2 e orizzontale -2. Il risultato è il **massimo di tutta la matrice**;
il traceback termina raggiungendo una cella a 0. Un allineamento locale confronta
**segmenti contigui** delle stringhe, eventualmente con gap al loro interno.

## Convenzioni del traceback

- Due righe di pari lunghezza; il simbolo `-` rappresenta un gap, mai un carattere dell'input.
- Sono vietate colonne con due gap.
- Nel globale e nell'edit distance, rimuovere tutti i gap deve restituire esattamente
  le due stringhe di partenza.
- Nel locale, rimuovendo i gap si devono ottenere **sottostringhe contigue** degli input.
- Se più predecessori hanno lo stesso valore ottimo, scegliere preferibilmente diagonale,
  alto, sinistra. **I test ammettono qualunque allineamento ottimo valido**.
- Tempo per calcolo e tabella: O(nm); traceback O(n+m) passi. La ricorsione richiede
  uno stack aggiuntivo O(n+m). Una matrice può non essere interamente calcolata top-down
  in problemi diversi, ma in queste ricorrenze gran parte delle celle è raggiunta.

## Compilazione, test, dimostrazione

Dalla cartella contenente `pom.xml`:

```sh
mvn test
mvn -DskipTests compile
java -cp target/classes it.unicam.cs.asdl.esdpsol.Demo
```

Nella **versione studenti**, prima di completare i TODO, i test dei solver assegnati
falliranno: è il comportamento previsto. `LCSSolver` e `AlignmentResult` sono funzionanti.
Usare le Javadoc come specifica dei contratti ed esaminare gli esempi già commentati.

## Consegna su Moodle

Consegnare le **due classi obbligatorie completate**: `EditDistanceSolver.java` e
`GlobalAlignmentSolver.java`. Aggiungere `LocalAlignmentSolver.java` **solo se svolta**.
Non è richiesto consegnare le classi già implementate. Rispettare la scadenza indicata
nell'attività Moodle. I test servono per autovalutazione: correttezza del valore
non basta, è necessario anche un traceback valido.

## Limiti dichiarati

L'implementazione è pensata per Java 8 e usa `String.charAt`, quindi lavora su unità
UTF-16, non su caratteri Unicode astratti (code point). Gli input non possono contenere
`-`, riservato ai gap. Per input molto lunghi la ricorsione può esaurire lo stack;
per questo è utile conoscere anche la tabulazione bottom-up. L'algoritmo locale usa
punteggi diversi da quello globale per favorire regioni ben conservate.

Materiali di riferimento: dispense fornite a lezione su programmazione dinamica,
LCS ed edit distance, e commenti dei sorgenti del progetto.
