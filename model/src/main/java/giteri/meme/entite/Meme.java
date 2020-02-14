package giteri.meme.entite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import giteri.meme.mecanisme.ActionFactory.IAction;
import giteri.meme.mecanisme.FilterFactory;
import giteri.meme.mecanisme.FilterFactory.IFilter;
import giteri.meme.mecanisme.AttributFactory.IAttribut;
import giteri.run.configurator.Configurator;
import giteri.run.interfaces.Interfaces.IUnitOfTransfer;

/** Classe qui va définir le type de comportement que peuvent avoir les agents.
 *  
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class Meme implements Serializable, IUnitOfTransfer<Meme>, Comparable<Meme>{
	// Liste des paramètres pour le giteri.meme en question
	IAction action;
	ArrayList<IAttribut> attributs;
	private Hashtable<String, Hashtable<Integer, IFilter>> KVAttributLAgregator;
	private double probaOfPropagation;
	private String name;
	boolean fluide = false;

	/** Constructeur avec une bardée de paramètre.
	 *
	 * @param name
	 * @param probaOfTransmission
	 * @param action
	 * @param attributs
	 * @param KVAttributLAgregator
	 */
	public Meme(String name, double probaOfTransmission,boolean fluidite, IAction action, ArrayList<IAttribut> attributs,
				Hashtable<String, Hashtable<Integer, IFilter>> KVAttributLAgregator){
		this.name = name;
		this.action = action;
		this.attributs = attributs;
		this.KVAttributLAgregator = KVAttributLAgregator;
		probaOfPropagation = probaOfTransmission;
		fluide = fluidite;
	}

	//region implementation de l'interface.

	/**
	 *
	 * @return ADLKDGRDMMNIFNTLK
	 */
	public String toFourCharString(){
		return getMemeName(0);
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
	@Override
	public Configurator.TypeOfUOT getActionType() {
		return action.getActionType();
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
			int maxIndex = 1;
			@Override
			public boolean hasNext() {
				return currentIndex < maxIndex;
			}
			@Override
			public Meme next() {
				if(currentIndex == 0) {
					currentIndex++;
					return Meme.this;
				}
				else
					throw new NoSuchElementException();
			}
		};
	}
	@Override
	public void setProbaPropagation(double p) {
		setProbaOfPropagation(p);
	}
	@Override
	public double getProbaPropagation() {
		return getProbaOfPropagation();

	}
	@Override
	public boolean isFluide() {
		return fluide;
	}
	@Override
	public int compareTo(Meme o) {
		return this.toFourCharString().compareTo(o.toFourCharString());
	}

	//endregion

	//region Getter/Setter/toString

	/**
	 *
	 * @return
	 */
	public String toString(){
		return getMemeName(0);
	}

	/** link:add:deg:sup:mine
	 *
	 * @return
	 */
	public String toShortString(){
		return getMemeName(1);
	}

	/**Add+ etc
	 *
	 * @return
	 */
	public String getName(){
		return this.name;
	}

	public IAction getAction() {
		return action;
	}

	public void setAction(IAction action) {
		this.action = action;
	}

	public Hashtable<Integer, FilterFactory.IFilter> getFilter(String attribut){
		return KVAttributLAgregator.get(attribut);
	}
	public ArrayList<IAttribut> getAttributs(){
		return this.attributs;
	}
	/** mode 0 : the shortest
	 *  mode 1 :
	 *  mode 2: non complet. Action:
	 * @param mode
	 * @return
	 */
	private String getMemeName(int mode){
		String resultat = "";
		String strAttribut = "";
		String strAgregator ="";
		Hashtable<Integer, FilterFactory.IFilter> aggregators;

		if(mode == 1)
			resultat += ":";
		if(mode == 2)
			resultat += "Action: ";

		resultat += mode == 0 ? action.getFourCharName() : action.toString();

		for (IAttribut attribut : attributs) {
			strAttribut = mode == 0 ? attribut.getFourCharName() : attribut.toString();
			if(mode == 1)
				resultat += ":";
			if(mode == 2)
				resultat += " Attribut: ";
			resultat += strAttribut;
			aggregators =KVAttributLAgregator.get(attribut.toString());
			for (Integer ordreApplyAggregator : aggregators.keySet()) {
				if(mode == 1)
					resultat += ":";
				if(mode == 2)
					resultat += " agrege sur: "+ ordreApplyAggregator + "-";
				strAgregator = mode== 0 ?
					aggregators.get(ordreApplyAggregator).getFourCharName() :
					aggregators.get(ordreApplyAggregator).toString();
				resultat += strAgregator;
			}
		}

		return resultat;
	}
	/**
	 *
	 * @param target
	 */
	public void setMyValues(Meme target){
		this.action = target.getAction();
		this.attributs = target.attributs;
		this.KVAttributLAgregator = target.KVAttributLAgregator;
		this.probaOfPropagation = target.probaOfPropagation;
		this.name = target.name;
	}
	public double getProbaOfPropagation() {
		return probaOfPropagation;
	}
	public void setProbaOfPropagation(double probaOfPropagation) {
		if(probaOfPropagation >= 0 && probaOfPropagation <= 1 )
			this.probaOfPropagation = probaOfPropagation;
		else
			this.probaOfPropagation = 0;
	}

	//endregion
}
