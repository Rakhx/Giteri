package giteri.network.network;

import java.lang.reflect.Field;
import java.util.*;

import giteri.tool.math.Toolz;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.graph.Graph;

import giteri.run.configurator.Configurator;

/** Classe qui contient les données analysées d'un réseau
 *
 */
public class NetworkProperties implements Cloneable{
	
	//region properties ! les propriétés publics sont utilisées génériquement !
	
	public String name;
	public double density;
	public double nbNodes, nbEdges;
	public double ddInterQrt;
	public double ddAvg;
	public double avgClust;
	public double APL;
	private int[] dd;
	private Hashtable<Integer, Double> furDurchschnitt;
	private int activator;
	private Integer networkInstance;

	//endregion
	
	//region Constructor

	public NetworkProperties(){
		name = "";
		networkInstance = -2;
		furDurchschnitt = new Hashtable<>();
	}
	
	public NetworkProperties(Integer id){
		name = "";
		networkInstance = id;
		furDurchschnitt = new Hashtable<>();
	}
	
	public NetworkProperties(String name){
		this();
		this.name = name;
	}

	/** Crée des propriétés avec des valeurs par défault
	 * Pour éviter les NaN ou null pointer
	 */
	public void createStub(){
		ddAvg = density = -1;
		ddInterQrt =nbNodes = nbEdges = -1;
		dd = new int[3];
		dd[0] = dd[1] = dd[2] = 0;
		activator = 0;
		networkInstance = -2;
		APL = -1;
	}

	//endregion

	//region getter / setter

	/** Défini les entêtes du csv pour les données.
	 * 
	 * @return
	 */
	public String getCsvHeader(int activationCode){
		String header = "";
		for (Configurator.NetworkAttribType attrib: getActivatedAttrib(activationCode)) {
			header += ";" + attrib.toString();
			header += ";" + "SD " + attrib.toString();
		}

		return header;
	}
	
	/** renvoi les valeurs dans le memes ordre que le header plus haut.
	 * 
	 * @return
	 */
//	public String getCsvFormat(String entete, int activationCode){
//		String values = "" + entete;
//		String toConcat = "";
//		for (Configurator.NetworkAttribType attrib: getActivatedAttrib(activationCode)) {
//			switch (attrib) {
//			case DENSITY:
//				toConcat = ";" + density;
//				break;
//			case DDAVG:
//				toConcat = ";" + ddAvg;
//				break;
//			case DDINTERQRT:
//				toConcat = ";" + ddInterQrt;
//				break;
//			case DDARRAY:
//				toConcat = ";dd array lol" ;
//				break;
//			case AVGCLUST:
//				toConcat = ";" + avgClust;
//				break;
//			case NBEDGES:
//				toConcat = ";" + nbEdges;
//				break;
//			case NBNODES:
//				toConcat = ";" + nbNodes;
//				break;
//			case APL:
//				toConcat = ";" + APL;
//				break;
//			case nbEdgesOnNbNodes:
//				toConcat = ";" + (double)nbEdges / nbNodes;
//				break;
//			default:
//				toConcat += ";nolose";
//				break;
//			}
//
//			values +=  toConcat;
//		}
//
//		return values;
//	}

	/** Prend un giteri.network qui représente le SD et renvoi une ligne de csv,
	 * avec les valeurs du networkProperties courant et celui en paramètre en 
	 * alternance.
	 * 
	 * @param sd
	 * @param activationCode
	 * @return
	 */
	public String getCSVFormatDoubleColonne(NetworkProperties sd, int activationCode){
		String values = "";
		String toConcat;

		if(sd == null){
			sd = new NetworkProperties();
			sd.createStub();
		}

		for (Configurator.NetworkAttribType attrib: getActivatedAttrib(activationCode)) {
			switch (attrib) {
				case DDARRAY:
					toConcat = ";ddArray" ;
					toConcat += ";ddArray" ;
					break;
				case nbEdgesOnNbNodes:
					toConcat = ";" + (double)nbEdges / nbNodes;
					toConcat += ";" + (double)sd.nbEdges / sd.nbNodes;
					break;
				default:
					toConcat = ";" + this.getValue(attrib);
					toConcat += ";" + sd.getValue(attrib);
					break;
			}

			values +=  toConcat;
		}

		return values;
	}

	//endregion

	// region calcul de propriétés + getter//Setter

	/** Methodes pour une utilisation générqieu des propriétés d'un réseau, en lecture ou écriture.
	 *
	 * @param type
	 * @return
	 */
	public Object getValue(Configurator.NetworkAttribType type){
		switch (type) {
			case DENSITY:
				return density;
			case AVGCLUST:
				return avgClust;
			case DDAVG:
				return ddAvg;
			case DDINTERQRT:
				return ddInterQrt;
			case NBEDGES:
				return nbEdges;
			case NBNODES:
				return nbNodes;
			case APL:
				return this.APL;
			case nbEdgesOnNbNodes:
				return (double)nbEdges / nbNodes;
			default:
				break;
		}

		return null;
	}

	/** Case issue, la flemme pour l'instant.
	 *
	 * @param type
	 * @return
	 */
	public Object getValueTest(Configurator.NetworkAttribType type) {
		Class cls = this.getClass();
		try {
			Field field = cls.getDeclaredField(type.toString().toLowerCase());
			return field.get(this);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}


	public void setValue(Configurator.NetworkAttribType type, Object value){
		switch (type) {
			case DENSITY:
				density = (double) value;
				break;
			case DDAVG:
				ddAvg = (double) value;
				break;
			case DDINTERQRT:
				ddInterQrt = (double) value;
				break;
			case DDARRAY:
				dd  = (int[]) value;
				for (int i = 0; i < dd.length; i++)
					furDurchschnitt.put(i,(double)dd[i]);
				break;
			case NBEDGES:
				nbEdges  = (double) value;
				break;
			case NBNODES:
				nbNodes  = (double) value;
				break;
			case AVGCLUST:
				avgClust = (double) value;
				break;
			case APL:
				this.APL = (double) value;
			default:
				break;
		}
	}

	/** Retourne la liste des attributs activé par le code.
	 *
	 * @param activationCode
	 * @return
		 */
	public List<Configurator.NetworkAttribType> getActivatedAttrib(int activationCode){
		List<Configurator.NetworkAttribType> res = new ArrayList<>();
		for (Configurator.NetworkAttribType attrib : Configurator.NetworkAttribType.values()) {
			if (Configurator.isAttribActived(activationCode, attrib)) {
				res.add(attrib);
			}
		}

		return res;
	}

	/**
 	 *
	 */
	public String toString(){
		String rez = "";
		//rez += "Densité du réseau: "+ density;
		//rez += "Nombre de noeud et d'edge "+ nbNodes + "|"+ nbEdges;
		rez += "◊id: "+ networkInstance.toString();
		rez += " ◊Densite: "+ Toolz.getNumberCutToPrecision(density,4) + " ◊ddAvg "+ ddAvg +
				" ◊DDinterQuart: "+ ddInterQrt + " ◊clust moyen: "+ Toolz.getNumberCutToPrecision(avgClust,4);
		return rez;
	}

	public Integer getNetworkInstance() {
		return networkInstance;
	}

	public void setNetworkUuidInstance(Integer networkInstance) {
		this.networkInstance = networkInstance;
	}

	public double getDensity() {
		return density;
	}

	public double getNbNodes() {
		return nbNodes;
	}

	public double getNbEdges() {
		return nbEdges;
	}

	public int[] getDd() {
		return dd;
	}

	public void setDd(int[] dd){
		this.dd = dd;
	}

	public double getAvgDd(){
		return Toolz.getAvg(furDurchschnitt);
	}

	public double getDdDeviation(){
		return Toolz.getDeviation(furDurchschnitt, Optional.ofNullable(null));
	}

	public double getAvgClust() {
		return avgClust;
	}

	public int getActivator() {
		return activator;
	}

	//endregion

	//region useless.

	/**
	 *
	 * @param graph
	 */
	public void computeAPL(Graph graph){
		APSP apsp = new APSP();
		apsp.init(graph);
		apsp.setDirected(false);
		apsp.compute();
		APSPInfo info = graph.getNode("10").getAttribute(APSPInfo.ATTRIBUTE_NAME);
		double total = 0;
		int nbValue = 0;
		for (int i = 0; i < graph.getNodeCount(); i++) {
			info =  graph.getNode(""+i).getAttribute(APSPInfo.ATTRIBUTE_NAME);
			for (String string : info.targets.keySet()) {
				total += info.targets.get(string).distance;
				nbValue++;
			}
		}
		this.APL = total / nbValue;
		System.out.println("Average path: "+ APL);
	}

	/** Clone.
	 *
	 */
	public NetworkProperties Clone(){
		NetworkProperties result = new NetworkProperties() ;
		result.name = this.name ;
		result.density = this.density ;
		result.nbNodes = this.nbNodes ;
		result.nbEdges = this.nbEdges ;
		result.ddInterQrt = this.ddInterQrt ;
		result.ddAvg = this.ddAvg ;
		result.avgClust = this.avgClust ;
		result.networkInstance = this.networkInstance ;
		result.activator = this.activator;
		result.dd = new int[this.dd.length];
		result.APL = this.APL;
		for (int i = 0; i < dd.length; i++) {
			result.dd[i] = this.dd[i];
			result.furDurchschnitt.put(i, (double)dd[i]);
		}

		return result;
	}

	//endregion
}
