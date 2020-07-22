package giteri.meme.entite;

import giteri.run.interfaces.Interfaces.IUnitOfTransfer;

/** Classe qui stand for une unité de transfert d'une entité a l'autre pour un CA
 *
 */
public class CoupleFlux {
    int entity;
    IUnitOfTransfer oldCouple;
    IUnitOfTransfer newCouple;
    int timestamp;

    public CoupleFlux(int enti, IUnitOfTransfer old, IUnitOfTransfer newOne, int time){
        entity = enti;
        oldCouple = old;
        newCouple = newOne;
        timestamp = time;
    }

    // public static CoupleFlux getStub(){
     //   return new CoupleFlux(-1, null, null);
   // }


}
