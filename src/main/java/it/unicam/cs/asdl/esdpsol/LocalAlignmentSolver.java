package it.unicam.cs.asdl.esdpsol;
 /**
  * ESTENSIONE FACOLTATIVA: allineamento locale di Smith-Waterman con punteggi
  * match +2, mismatch -1 e gap -2. S(i,j) rappresenta il miglior punteggio
  * di un allineamento che TERMINA nelle posizioni i,j. La ricorrenza è
  * max(0, diagonale+s, alto-2, sinistra-2). Il massimo può comparire in
  * qualsiasi cella, non necessariamente in (n,m). Il traceback si arresta
  * alla prima cella di valore 0 e restituisce segmenti contigui di x e y.
  * <p>Questa classe è indipendente dagli assignment obbligatori.
  */
 public class LocalAlignmentSolver {
     private final String x,y;
     private final int[][] memo;
     private final boolean[][] computed;
     private int bestI,bestJ,bestScore;
     private boolean solved;
     /**
      * Inizializza un nuovo risolutore per la coppia di stringhe.
      * @param x prima stringa
      * @param y seconda stringa */
     public LocalAlignmentSolver(String x,String y) {
         if(x==null||y==null)throw new NullPointerException("stringhe null");
         if(x.indexOf('-')>=0||y.indexOf('-')>=0)throw new IllegalArgumentException("'-' riservato ai gap");
         this.x=x;this.y=y;
         memo=new int[x.length()+1][y.length()+1];
         computed=new boolean[x.length()+1][y.length()+1];
     }
     /**
      * Calcola tutti gli stati: nel locale il massimo può essere in qualunque
      * cella. Si individuano quindi bestI e bestJ da cui iniziare il traceback.
      */
     public void solve() {
         if(solved)return;
         for(int i=0;i<=x.length();i++)for(int j=0;j<=y.length();j++){
             int v=score(i,j);
             // Il massimo locale puo trovarsi in qualunque cella.
             if(v>bestScore){bestScore=v;bestI=i;bestJ=j;}
         }
         solved=true;
     }
     /**
      * Restituisce un valore calcolato oppure uno stato del risolutore.
      * @return massimo punteggio di un allineamento locale */
     public int getOptimalScore(){requireSolved();return bestScore;}
     /**
      * Restituisce un valore calcolato oppure uno stato del risolutore.
      * @return un allineamento locale ottimo; righe vuote se punteggio 0 */
     public AlignmentResult getOneOptimalAlignment() {
         requireSolved();StringBuilder a=new StringBuilder(),b=new StringBuilder();
         traceBack(bestI,bestJ,a,b);
         return new AlignmentResult(a.reverse().toString(),b.reverse().toString(),bestScore);
     }
     private void requireSolved(){if(!solved)throw new IllegalStateException("chiamare solve prima");}
     /**
      * SOLUZIONE facoltativa 1: score(i,j) con cache booleana e casi base 0.
      * @param i prefisso x @param j prefisso y @return punteggio locale finale
      */
     private int score(int i,int j) {
         if(computed[i][j])return memo[i][j];
         int value=0;
         if(i>0&&j>0){
             int s=x.charAt(i-1)==y.charAt(j-1)?2:-1;
             value=Math.max(0,Math.max(score(i-1,j-1)+s,Math.max(score(i-1,j)-2,score(i,j-1)-2)));
         }
         computed[i][j]=true; memo[i][j]=value; return value;
     }
     /**
      * SOLUZIONE facoltativa 2: traceback da (bestI,bestJ) fino a valore 0.
      * @param i posizione finale in x @param j posizione finale in y
      * @param a prima riga rovesciata @param b seconda riga rovesciata
      */
     private void traceBack(int i,int j,StringBuilder a,StringBuilder b) {
         // Uno zero segnala il punto di INIZIO del segmento ottimo.
         if(i==0||j==0||memo[i][j]==0)return;
         int s=x.charAt(i-1)==y.charAt(j-1)?2:-1;
         if(memo[i][j]==score(i-1,j-1)+s){
             a.append(x.charAt(i-1));b.append(y.charAt(j-1));
             traceBack(i-1,j-1,a,b);return;
         }
         if(memo[i][j]==score(i-1,j)-2){
             a.append(x.charAt(i-1));b.append('-');
             traceBack(i-1,j,a,b);return;
         }
         if(memo[i][j]==score(i,j-1)-2){
             a.append('-');b.append(y.charAt(j-1));
             traceBack(i,j-1,a,b);return;
         }
         throw new IllegalStateException("nessun predecessore ottimo");
     }
 }