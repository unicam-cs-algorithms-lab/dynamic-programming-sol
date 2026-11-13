package it.unicam.cs.asdl.esdpsol;
 /** Esempi d'uso senza dipendenze esterne: lanciare dopo avere completato i TODO. */
 public final class Demo {
     private Demo() {}
     /** Avvia gli esempi.
      * @param args argomenti da linea di comando, ignorati
      */
     public static void main(String[] args) {
         LCSSolver lcs=new LCSSolver("ABCD","ACBD");
         lcs.solve();
         System.out.println("LCS: "+lcs.getOneSolution());
         EditDistanceSolver ed=new EditDistanceSolver("ALBERO","LABBRO");
         ed.solve();
         System.out.println("EDIT DISTANCE: "+ed.getOneOptimalAlignment());
         GlobalAlignmentSolver g=new GlobalAlignmentSolver("ALBERO","LABBRO");
         g.solve();
         System.out.println("SIMILARITÀ GLOBALE: "+g.getOneOptimalAlignment());
         LocalAlignmentSolver loc=new LocalAlignmentSolver("TTACGTAA","GGACGTCC");
         loc.solve();
         System.out.println("SIMILARITÀ LOCALE: "+loc.getOneOptimalAlignment());
     }
 }