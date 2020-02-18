package giteri.meme.entite;

import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.TypeOfUOT;
import giteri.run.interfaces.Interfaces.IUnitOfTransfer;
import giteri.tool.math.Toolz;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Classe qui est composé de deux memes
 *
 */
public class CoupleMeme implements Iterable<Meme>, IUnitOfTransfer<CoupleMeme> {
    private Meme addAction,removeAction;
    private double probaPropagation;
    private int index;
    private boolean fluidite;
    private TypeOfUOT myType = Configurator.TypeOfUOT.COUPLE;

    /** Constructeur
     *
     * @param add
     * @param rmv
     * @param probaPropagation
     */
    public CoupleMeme(int index,Meme add, Meme rmv, Double probaPropagation){
        addAction = add;
        removeAction = rmv;
        this.index = index;
        this.setProbaPropagation(probaPropagation);

    }

    public Meme getOneMemeAtRandom(){
        return Toolz.rollDice(.5) ? addAction : removeAction;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Meme> iterator() {
        return new Iterator<Meme>() {
            int currentIndex = 0;
            int maxIndex = 2;
            @Override
            public boolean hasNext() {
                return currentIndex < maxIndex;
            }
            @Override
            public Meme next() {
                if(currentIndex == 0) {
                    currentIndex++;
                    return addAction;
                }
                if(currentIndex == 1){
                    currentIndex++;
                    return removeAction;
                }
                else
                    throw new NoSuchElementException();
            }
        };
    }

    public String getName(){
        String name = "";
        name+= addAction.getName() ;
        // TODO [CV] - n'aime pas trop le & pour les classes graphstream?
        //name += "&";
        name += ".";
        name+= removeAction.getName();
        return name;
    }

    public String toString(){
        return getName();
    }

    /**
     *
     * @return
     */
    public String getColorClass(){
        String classe = "";
        classe += addAction.toFourCharString();
        classe += /*"1" +*/ removeAction.toFourCharString();
        return classe;
    }

    //region implementation d'interface

    /** Si on est en single transmission on renvoie que la proba sinon on
     * lisse par le nombre de cible c a d tte les entités
     * @return
     */
    public double getProbaPropagation() {
        return Configurator.coupleSingleTransmission ? probaPropagation :
                probaPropagation / Configurator.coupleDividerTransmission;
    }

    @Override
    public String toFourCharString() {
        return this.getColorClass();
    }

    /**
     * add+ pour les singles
     * add+.rmv- pour les couples.
     *
     * @return
     */
    @Override
    public String toNameString() {
        return getName();
    }

    public void setProbaPropagation(double probaPropagation) {
        this.probaPropagation = probaPropagation;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     *
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(CoupleMeme o) {
        return this.getName().compareTo(o.getName()) ;

    }

    public TypeOfUOT getActionType() {
        return TypeOfUOT.COUPLE;
    }

    public boolean isFluide(){
        throw new NotImplementedException();
    }
    //endregion

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
