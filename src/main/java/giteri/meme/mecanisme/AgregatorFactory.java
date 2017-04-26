package giteri.meme.mecanisme;

import java.util.ArrayList;
import java.util.Collections;

import giteri.tool.math.Toolz;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.AgregatorType;
import giteri.meme.entite.Entite;
import giteri.meme.entite.EntiteHandler;

public class AgregatorFactory {
	
	// Region singleton Stuff
	private static AgregatorFactory instance = null;
	private AgregatorFactory(){
		
	}
	public static AgregatorFactory getInstance(){
		if(instance == null)
			instance = new AgregatorFactory();
		return instance;
	}
	// EndRegion
		
	/** FACTORY Renvoi un agregator depuis l'enum du configurator.
	 *  
	 * @param agregatorName
	 * @return une classe implémentant l'interface d'agrégator.
	 */
	public IAgregator getAgregator(Configurator.AgregatorType agregatorName){
		switch (agregatorName) {
		case THEMOST:
			return new TheMost();
		case THELEAST:
			return new TheLeast();
		case THEMOSTLINKED:
			return new TheMostLinked();
		case THELEASTLINKED:
			return new TheLeastLinked();
		case MINESUP:
			return new MineSup();
		case MINEINF:
			return new MineInf();
		case MINEDIF:
			return new MineDif();
		case MINEEQUAL:
			return new MineEgal();
		case LINKED:
			return new linked();
		case NOTLINKED:
			return new notLinked();
		case RANDOM:
			return new random();
		case HOPAWAY:
			return new HopWay();
			
		default:
			return null;
		}
	}
	
	/** INTERFACE d'attribut
	 *
	 */
	public interface IAgregator 
	{
		public abstract <T extends Comparable<T>> ArrayList<Entite> applyAggregator(Entite asker, ArrayList<Entite> entites, AttributFactory.IAttribut<T> attribut);
		
		public AgregatorType getEnumType();
		
		public String getFourCharName();
		
		public EntiteHandler handler = EntiteHandler.getInstance();
		
	}
	
	/** Classe fournissant des méthodes de bases
	 *
	 */
	public abstract class Agregator {
		
		/** Méthode qui renvoi le sous ensemble des entités linked
		 * a l'entité asker.
		 * 
		 * @param asker
		 * @param entites
		 * @return
		 */
		protected ArrayList<Entite> getLinked(Entite asker, ArrayList<Entite> entites){
			ArrayList<Entite> linked = new ArrayList<Entite>();
			for (Entite entite : entites) {
				if(asker.getConnectedNodesIndex().contains(entite.getIndex())){
					linked.add(entite);
				}
			}
			
			return linked;
		}
		
		/** Méthode qui renvoi l'ensemble des entitées non linked a l'entité asker 
		 * 
		 * @param asker
		 * @param entites
		 * @return
		 */
		@SuppressWarnings("unchecked")
		protected ArrayList<Entite> getNotLinked(Entite asker, ArrayList<Entite> entites){
			ArrayList<Entite> notLinked = (ArrayList<Entite>) entites.clone();
			notLinked.removeAll(this.getLinked(asker, entites));
			
			return notLinked;
		}
	}
		
	/** CLASSE the most, Renvoi l'entité possédant la plus grande valeur sur l'attribut
	 * Depuis la liste d'entité entites, sur l'attribut comparable attribut
	 * implémentant l'interface d'agregator.
	 */
	public class TheMost implements IAgregator{
	
		/** Renvoi, depuis la liste en entrée, et l'attribut, les éléments qui répondent aux critères.
		 * Ici : ceux qui ont la plus grande valeur sur l'attribut spécifié. 
		 */
		public <T extends Comparable<T>> ArrayList<Entite> applyAggregator(Entite asker, ArrayList<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			Entite bestEntitee = null ;
			
			if(entites.size() > 0){
				resultat.add(entites.get(0));
				bestEntitee = entites.get(0);
			}
			
			entites.remove(0);
			for (Entite entitee : entites) {
				// Si égalité, on rajoute le node a la liste
					if(attribut.getAttributValue(entitee.getNode()).compareTo( 
							   attribut.getAttributValue(bestEntitee.getNode())) == 0)
					{
							resultat.add(entitee);
					}
					if(attribut.getAttributValue(entitee.getNode()).compareTo( 
							   attribut.getAttributValue(bestEntitee.getNode())) == 1)
					{
							resultat.clear();
							resultat.add(entitee);
							bestEntitee = entitee;
					}			
				}
				
				return resultat;
			}
		
		/** ToString de la méthode.
		 * 
		 */
		public String toString(){
			return "TheMost";
		}

		@Override
		public AgregatorType getEnumType() {
			return AgregatorType.THEMOST;
		}

		@Override
		public String getFourCharName() {
			return "MT";
		}
	}

	/** CLASSE The least de la map.
	 * 
	 */
	public class TheLeast implements IAgregator{

		/** Renvoi, depuis la liste des entrées, et de l'attribut, 
		 * les éléments qui répondent au critére suivant : Min sur la 
		 * valeur de l'attribut spécifié
		 */
		public <T extends Comparable<T>> ArrayList<Entite> applyAggregator(Entite asker,ArrayList<Entite> entitees, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			Entite bestEntitee = null ;
			
			if(entitees.size() > 0){
				resultat.add(entitees.get(0));
				bestEntitee = entitees.get(0);
			}			
			 
			entitees.remove(0);
			for (Entite entitee : entitees) {
				// Si égalité, on rajoute le node a la liste
				if(attribut.getAttributValue(entitee.getNode()).compareTo( 
						   attribut.getAttributValue(bestEntitee.getNode())) == 0)
				{
						resultat.add(entitee);
				}
				if(attribut.getAttributValue(entitee.getNode()).compareTo( 
						   attribut.getAttributValue(bestEntitee.getNode())) == -1)
				{
						resultat.clear();
						resultat.add(entitee);
						bestEntitee = entitee;
				}			
			}
				
				return resultat;
			}
		
		/** ToString de la méthode.
		 * 
		 */
		public String toString(){
			return "TheLeast";
		}
		
		@Override
		public AgregatorType getEnumType() {
			return AgregatorType.THELEAST;
		}
		
		@Override
		public String getFourCharName() {
			return "LT";
		}
	}

	/** CLASSE Retourne le most depuis la liste d'entité déjé liée au demandeur
	 * 
	 */
	public class TheMostLinked implements IAgregator{

		/** Retourne le maximum deja linké a l'asker. Entites n'est pas utilisé.
		 * 
		 */
		public <T extends Comparable<T>> ArrayList<Entite> applyAggregator(Entite asker,ArrayList<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			Entite bestEntitee = null ;
			
			entites = handler.getLinkedEntite(asker);
			
			// on extrait de la liste 			
			if(entites.size() > 0){
				resultat.add(entites.get(0));
				bestEntitee = entites.get(0);
			}else
				return entites;
			
			entites.remove(0);
			for (Entite entite : entites) {
				// Si égalité, on rajoute le node a la liste
					if(attribut.getAttributValue(entite.getNode()).compareTo(attribut.getAttributValue(bestEntitee.getNode())) == 0)
					{
							resultat.add(entite);
					}
					if(attribut.getAttributValue(entite.getNode()).compareTo(attribut.getAttributValue(bestEntitee.getNode())) == 1)
					{
							resultat.clear();
							resultat.add(entite);
							bestEntitee = entite;
					}			
				}
				
				return resultat;
		}
		
		/** ToString de la méthode.
		 * 
		 */
		public String toString(){
			return "TheMostLinked";
		}
		

		public AgregatorType getEnumType() {
			return AgregatorType.THEMOSTLINKED;
		}
		@Override
		public String getFourCharName() {
			return "MTLK";
		}
	}

	/** CLASSE renvoi, depuis une liste, l'entité déjé lié au demandeur
	 * possédant la plus petit valeur de l'attribut. Liste des éléments
	 * ayant l'attribut au minimum en cas d'égalité.
	 */
	public class TheLeastLinked implements IAgregator{

		/** Application de l'aggrégateur.
		 * 
		 */
		public <T extends Comparable<T>> ArrayList<Entite> applyAggregator(	Entite asker, ArrayList<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			Entite bestEntite = null ;
			entites = handler.getLinkedEntite(asker);
			
			if(entites.size() > 0){
				resultat.add(entites.get(0));
				bestEntite = entites.get(0);
			}else 
				return entites;
			 
			entites.remove(0);
			for (Entite entite : entites) {
				// Si égalité, on rajoute le node a la liste
				if(attribut.getAttributValue(entite.getNode()).compareTo( 
						   attribut.getAttributValue(bestEntite.getNode())) == 0)
				{
						resultat.add(entite);
				}
				if(attribut.getAttributValue(entite.getNode()).compareTo( 
						   attribut.getAttributValue(bestEntite.getNode())) == -1)
				{
						resultat.clear();
						resultat.add(entite);
						bestEntite = entite;
				}			
			}
				
				return resultat;
		}

		@Override
		public AgregatorType getEnumType() {
			return AgregatorType.THELEASTLINKED;
		}
		
		public String toString(){
			return "TheLeastLinked";
		}		
		
		@Override
		public String getFourCharName() {
			return "LTLK";
		}
	}

	/** CLASSE valeur de l'attribut strictement supérieur é celle des entités sélectionnées
	 *
	 */
	public class MineSup implements IAgregator{

		@Override
		public <T extends Comparable<T>> ArrayList<Entite> applyAggregator(Entite asker, ArrayList<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			if(entites.size() > 0){
				for (Entite entite : entites) {
					if(attribut.getAttributValue(entite.getNode()).compareTo(attribut.getAttributValue(asker.getNode())) == -1){
						resultat.add(entite);
					}
				}				
			}
			
			return resultat;
		}

		@Override
		public AgregatorType getEnumType() {			
			return AgregatorType.MINESUP;
		}
		
		public String toString(){
			return "MineSup";
		}
		
		@Override
		public String getFourCharName() {
			return "MNSP";
		}
	}

	/** CLASSE Valeur de l'attribut strictement inférieur a celle des entités sélectionnées
	 * 
	 */
	public class MineInf implements IAgregator{
		@Override
		public <T extends Comparable<T>> ArrayList<Entite> applyAggregator(Entite asker, ArrayList<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			if(entites.size() > 0){
				for (Entite entite : entites) {
					if(attribut.getAttributValue(entite.getNode()).compareTo(attribut.getAttributValue(asker.getNode())) == 1){
						resultat.add(entite);
					}
				}				
			}
			
			return resultat;
		}

		@Override
		public AgregatorType getEnumType() {			
			return AgregatorType.MINEINF;
		}
		
		public String toString(){
			return "MineInf";
		}
		
		@Override
		public String getFourCharName() {
			return "MNIF";
		}
	}

	/** CLASSE Valeur de l'attribut différente a celle des entités sélectionnées
	 *
	 */
	public class MineDif implements IAgregator{
		@Override
		public <T extends Comparable<T>> ArrayList<Entite> applyAggregator(	Entite asker, ArrayList<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			if(entites.size() > 0){
				for (Entite entite : entites) {
					if(attribut.getAttributValue(entite.getNode()).compareTo(attribut.getAttributValue(asker.getNode())) != 0 ){
						resultat.add(entite);
					}
				}				
			}
			
			return resultat;
		}

		@Override
		public AgregatorType getEnumType() {			
			return AgregatorType.MINEDIF;
		}
		
		public String toString(){
			return "MineDif";
		}
	
		@Override
		public String getFourCharName() {
			return "MNDF";
		}
	}

	/** CLASSE Valeur de l'attribut égale a celle des entités sélectionnées
	 *
	 */
	public class MineEgal implements IAgregator{
		@Override
		public <T extends Comparable<T>> ArrayList<Entite> applyAggregator(	Entite asker, ArrayList<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			if(entites.size() > 0){
				for (Entite entite : entites) {
					if(attribut.getAttributValue(entite.getNode()).compareTo(attribut.getAttributValue(asker.getNode())) == 0){
						resultat.add(entite);
					}
				}				
			}
			
			return resultat;
		}

		@Override
		public AgregatorType getEnumType() {			
			return AgregatorType.MINEEQUAL;
		}
		
		public String toString(){
			return "MineEqual";
		}
	
		@Override
		public String getFourCharName() {
			return "MNEQ";
		}
	}
	
	/** Agregator de type not linked, depuis une liste renvoi les éléments qui
	 * ne sont pas linké a l'entité en paramètre.
	 */
	public class notLinked extends Agregator implements IAgregator{

		/** Renvoi la liste des éléments non connectés a l'asker
		 * 
		 */
		public <T extends Comparable<T>> ArrayList<Entite> applyAggregator(Entite asker, ArrayList<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			return this.getNotLinked(asker, entites);
		}

		/**
		 * 
		 */
		public AgregatorType getEnumType() {
			return AgregatorType.NOTLINKED;
		}
		
		public String toString(){
			return "NotLinked";
		}
		
		@Override
		public String getFourCharName() {
			return "NTLK";
		}
	}
	
	/** renvoi la liste des éléments link a l'élément asker
	 * 
	 */
	public class linked extends Agregator implements IAgregator{

		/** Renvoi un item au hasard depuis la liste fourni en parametre, qui n'est 
		 * pas connecté au noeud en question. 
		 * 
		 */
		public <T extends Comparable<T>> ArrayList<Entite> applyAggregator(Entite asker, ArrayList<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			return this.getLinked(asker, entites);
		}

		/**
		 * 
		 */
		public AgregatorType getEnumType() {
			return AgregatorType.LINKED;
		}
		
		public String toString(){
			return "Linked";
		}
	
		@Override
		public String getFourCharName() {
			return "LK";
		}
	}
	
	/** Renvoi un élément aléatoire de la liste en paramètre,
	 * de facon équiprobable pour tout ses éléments. 
	 *
	 */
	public class random extends Agregator implements IAgregator {

		/** Application de l'agrégateur.
		 * 
		 */
		public <T extends Comparable<T>> ArrayList<Entite> applyAggregator(
				Entite asker, ArrayList<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			if(entites.size() > 1){
			ArrayList<Entite> randomElement = new ArrayList<Entite>();
			randomElement.add(entites.get(Toolz.getRandomNumber(entites.size())));
			return randomElement;
			}else
				return entites;
		}

		/** Retourne le type d'agregator. 
		 * 
		 */
		public AgregatorType getEnumType() {
			return AgregatorType.RANDOM; 
		}
		
		public String toString(){
			return "Random";
		}
		
		@Override
		public String getFourCharName() {
			return "RDM";
		}
	}

	public class HopWay extends Agregator implements IAgregator {

		public int reach = 2;
		
		@Override
		public <T extends Comparable<T>> ArrayList<Entite> applyAggregator(Entite asker, ArrayList<Entite> entites, AttributFactory.IAttribut<T> attribut) {

			ArrayList<Entite> entiteResult = new ArrayList<Entite>();
			Collections.sort(entites);
		
			if(Configurator.debugHopAway){
				String result = "before Entites";
				for (Entite entite : entites) {
					result +=":" + entite.getIndex();
				}
				
				System.out.println(result);
				}
				
			getNeightboor(entiteResult, entites, asker, reach);
			Collections.sort(entiteResult);
			
			if(Configurator.debugHopAway){
				String result = "after Entites:";
				for (Entite entite : entiteResult) {
					result +=":" + entite.getIndex();
				}
				
				System.out.println(result);
			}
			
			return entiteResult;
		}

		public String toString(){
			return "Hopaway";
		}
		
		@Override
		public AgregatorType getEnumType() {
			return AgregatorType.HOPAWAY;
		}

		@Override
		public String getFourCharName() {
			return "HA";
		}
		
		/**
		 * 
		 * @param entiteToReturn
		 * @param entiteSearchSpace
		 * @param target
		 * @param deepToGo
		 */
		private void getNeightboor(ArrayList<Entite> entiteToReturn,ArrayList<Entite> entiteSearchSpace, Entite target, int deepToGo){
			deepToGo --;
			ArrayList<Entite> entiteAccepted = new ArrayList<Entite>();
			for (Entite neighboor : target.getConnectedEntite()) {
				// On regarde si le voisin en question est dans la liste des éléments a regarder
				if(entiteSearchSpace.contains(neighboor)){
					entiteAccepted.add(neighboor);
					// on le retire de la liste des éléments que l'on regarde pour éviter les doublons retour
					entiteSearchSpace.remove(neighboor);
				}
			}
			
			entiteToReturn.addAll(entiteAccepted);
			if(deepToGo > 0)
				for (Entite entite : entiteAccepted) 
					getNeightboor(entiteToReturn, entiteSearchSpace, entite, deepToGo);
		}
		
	}
}	
