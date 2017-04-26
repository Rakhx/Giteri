package entite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import mecanisme.ActionFactory.IAction;
import mecanisme.AgregatorFactory.IAgregator;
import mecanisme.AttributFactory.IAttribut;

/** Classe qui va définir le type de comportement que peuvent avoir les agents.
 *  
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class Meme implements Serializable, Comparable<Meme>{	
		
	// Liste des paramètres pour le meme en question
	IAction action;
	ArrayList<IAttribut> attributs;
	Hashtable<String, Hashtable<Integer, IAgregator>> KVAttributLAgregator;

	public double probaOfPropagation;
	String name;

	/** Constructeur avec une bardée de paramètre.
	 * 
	 * @param name
	 * @param probaOfTransmission
	 * @param action
	 * @param attributs
	 * @param KVAttributLAgregator
	 */
	public Meme(String name, double probaOfTransmission, IAction action, ArrayList<IAttribut> attributs, Hashtable<String, Hashtable<Integer, IAgregator>> KVAttributLAgregator){
		this.name = name;
		this.action = action;
		this.attributs = attributs;
		this.KVAttributLAgregator = KVAttributLAgregator;
		probaOfPropagation = probaOfTransmission;
	}
	
	// Region Getter/Setter/toString
	
	public String toString(){
		return getMemeName(0);
	}
	
	public String toShortString(){
		return getMemeName(1);
	}
	
	public String toFourCharString(){
		return getMemeName(0);
	}
	
	public String getName(){
		return this.name;
	}
	
	public IAction getAction() {
		return action;
	}

	public void setAction(IAction action) {
		this.action = action;
	}

	public Hashtable<Integer, IAgregator> getAgregators(String attribut){
		return KVAttributLAgregator.get(attribut);
	}
	
	public ArrayList<IAttribut> getAttributs(){
		return this.attributs;
	}

	// EndRegion
	
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
		Hashtable<Integer, IAgregator> aggregators;
		
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

	@Override
	public int compareTo(Meme o) {
		return this.toFourCharString().compareTo(o.toFourCharString());
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
}
