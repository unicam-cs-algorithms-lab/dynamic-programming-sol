package it.unicam.cs.asdl.esdpsol;
 /**
  * Risultato immutabile di un allineamento tra due stringhe.
  * <p>Le due righe hanno identica lunghezza e usano il carattere '-' per
  * indicare un gap. Nessuna colonna può contenere due gap. Rimuovendo i gap
  * si ottengono esattamente le due stringhe originali (global alignment);
  * per un allineamento locale si ottengono segmenti contigui delle originali.
  * Il campo {@code score} è un costo MINIMO nell'edit distance e un punteggio
  * MASSIMO negli allineamenti globali o locali: non confondere i due significati.
  * <p>Per semplicità didattica i simboli di input '-' sono vietati.
  */
 public final class AlignmentResult {
     private final String first, second;
     private final int score;
     /**
      * Crea un risultato di allineamento verificando la coerenza delle righe.
      * @param first prima riga allineata
      * @param second seconda riga allineata
      * @param score costo o punteggio ottimo
      * @throws IllegalArgumentException se lunghezze incompatibili o gap doppi */
     public AlignmentResult(String first, String second, int score) {
         if (first == null || second == null) throw new NullPointerException("righe null");
         if (first.length() != second.length()) throw new IllegalArgumentException("lunghezze differenti");
         for (int k=0;k<first.length();k++)
             if (first.charAt(k)=='-' && second.charAt(k)=='-')
                 throw new IllegalArgumentException("due gap nella stessa colonna");
         this.first=first; this.second=second; this.score=score;
     }
     /**
      * Restituisce un valore calcolato oppure uno stato del risolutore.
      * @return prima riga, con eventuali gap */ public String getFirst() {return first;}
     /**
      * Restituisce un valore calcolato oppure uno stato del risolutore.
      * @return seconda riga, con eventuali gap */ public String getSecond() {return second;}
     /**
      * Restituisce un valore calcolato oppure uno stato del risolutore.
      * @return valore ottimo (costo o punteggio) */ public int getScore() {return score;}
     /**
      * Restituisce un valore calcolato oppure uno stato del risolutore.
      * @return le due righe su righe distinte, seguite dal valore ottimo */
     @Override public String toString() {return first+"\n"+second+"\nvalore = "+score;}
 }