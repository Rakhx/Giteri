package giteri.meme.entite;

import giteri.tool.math.Toolz;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Classe qui est composé de deux memes
 *
 */
public class CoupleMeme implements Iterable<Meme> {
    private Meme addAction,removeAction;
    private double probaPropagation;
    private int index;

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
        name += "&";
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
        classe += "1" + removeAction.toFourCharString();
        return classe;
    }

    public double getProbaPropagation() {
        return probaPropagation;
    }

    public void setProbaPropagation(double probaPropagation) {
        this.probaPropagation = probaPropagation;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
