package giteri.meme.mecanisme;

import java.util.*;

import giteri.tool.math.Toolz;
import giteri.run.configurator.Configurator;
import giteri.run.configurator.Configurator.AgregatorType;
import giteri.meme.entite.Entite;
import giteri.meme.entite.EntiteHandler;

public class FilterFactory {

	// EntiteHandler entiteHandler;
	public FilterFactory(){
	}

	public void setEntiteHandler(EntiteHandler eh){
	//	entiteHandler = eh;
	}

	/** FACTORY Renvoi un agregator depuis l'enum du configurator.
	 *
	 * @param agregatorName
	 * @return une classe implémentant l'interface d'agrégator.
	 */
	public IFilter getFilter(Configurator.AgregatorType agregatorName){
		switch (agregatorName) {
			case THEMOST:
				return new TheMost();
			case THELEAST:
				return new TheLeast();
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
			case HOPAWAY3:
				return new HopWay3();
			case TRIANGLE:
				return new Triangle();
			case THEIRSUP:
				return new TheirSupParam(2);
				case THEIRSUPSIX:
				return new TheirSupParam(6);
			case THEIREQUAL:
				return new TheirEqual();
			case SELFSUP:
				return new SelfSup();
			case BLANK:
				return new Blank();
			default:
				return null;
		}
	}

	/** INTERFACE d'attribut
	 *
	 */
	public interface IFilter {
		/** Modifie la liste @entites en renvoyant un sous ensemble, fonction de l'attribut sur lequel faire les comparaisons
		 * et l'aggregator sélectionné.
		 *
		 * @param asker
		 * @param entites
		 * @param attribut
		 * @param <T>
		 */
		<T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut);

		AgregatorType getEnumType();

		String getFourCharName();

	}

	/** Classe fournissant des méthodes de bases pour certaines des implémentations de l'interface. Non intégré a tte les classes
	 * de filtre.
	 *
	 */
	public abstract class Filter {

		/** Méthode qui renvoi le sous ensemble des entités linked
		 * a l'entité asker.
		 *
		 * @param asker l'entité qui recherche les liés a elle
		 * @param entites la liste d'entité d'ou il faut extraire les connectés
		 * @return
		 */
		protected void getLinked(Entite asker, Set<Entite> entites){
			entites.retainAll(asker.getConnectedEntite());
		}

		/** Méthode qui renvoi l'ensemble des entitées non linked a l'entité asker
		 *
		 * @param asker
		 * @param entites
		 * @return
		 */
		@SuppressWarnings("unchecked")
		protected void getNotLinked(Entite asker, Set<Entite> entites){
			Set<Entite> entitesLinked = new HashSet<>(entites);
			this.getLinked(asker, entitesLinked);
			entites.removeAll(entitesLinked);
		}
	}

	/** CLASSE the most, Renvoi l'entité possédant la plus grande valeur sur l'attribut
	 * Depuis la liste d'entité entites, sur l'attribut comparable attribut
	 * implémentant l'interface d'agregator.
	 */
	public class TheMost implements IFilter {

		/** Renvoi, depuis la liste en entrée, et l'attribut, les éléments qui répondent aux critères.
		 * Ici : ceux qui ont la plus grande valeur sur l'attribut spécifié.
		 */
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {

		    ArrayList<Entite> resultat = new ArrayList<>();
			Entite bestEntitee = null ;
			boolean firstStep = true;

			for (Entite entitee : entites)
			{
				if(firstStep){
					resultat.add(entitee);
					bestEntitee = entitee;
					firstStep = false;
					continue;
				}

				// Si égalité, on rajoute le node a la liste
				if(attribut.getAttributValue(entitee.getNode()).compareTo(
						attribut.getAttributValue(bestEntitee.getNode())) == 0)
				{
					resultat.add(entitee);
				}
				// dans le cas contraire on a un nouveau meilleur, mise a jour des listes etc.
				if(attribut.getAttributValue(entitee.getNode()).compareTo(
						attribut.getAttributValue(bestEntitee.getNode())) == 1)
				{
					resultat.clear();
					resultat.add(entitee);
					bestEntitee = entitee;
				}
			}

			entites.clear();
			entites.addAll(resultat);
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
	public class TheLeast implements IFilter {

		/** Renvoi, depuis la liste des entrées, et de l'attribut,
		 * les éléments qui répondent au critére suivant : Min sur la
		 * valeur de l'attribut spécifié
		 */
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			Entite bestEntitee = null ;
			boolean firstStep = true;

			for (Entite entitee : entites)
			{
				if(firstStep){
					resultat.add(entitee);
					bestEntitee = entitee;
					firstStep = false;
					continue;
				}
				// Si égalité, on rajoute le node a la liste
				if(attribut.getAttributValue(entitee.getNode()).compareTo(
						attribut.getAttributValue(bestEntitee.getNode())) == 0)
				{
					resultat.add(entitee);
				}
				// Si meilleur résultat en regard du critère
				if(attribut.getAttributValue(entitee.getNode()).compareTo(
						attribut.getAttributValue(bestEntitee.getNode())) == -1)
				{
					resultat.clear();
					resultat.add(entitee);
					bestEntitee = entitee;
				}
			}

			entites.clear();
			entites.addAll(resultat);
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

	/** CLASSE valeur de l'attribut strictement supérieur é celle des entités sélectionnées
	 *
	 */
	public class MineSup implements IFilter {

		@Override
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			for (Entite entite : entites) {
				if(attribut.getAttributValue(entite.getNode()).compareTo(attribut.getAttributValue(asker.getNode())) == -1 ||
						(Configurator.strictEqualityInComparaison ? false :
								attribut.getAttributValue(entite.getNode()).compareTo(attribut.getAttributValue(asker.getNode())) == 0)){
					resultat.add(entite);
				}
			}

			entites.clear();
			entites.addAll(resultat);
		}

		public AgregatorType getEnumType() {
			return AgregatorType.MINESUP;
		}

		public String toString(){
			return "MineSup";
		}

		public String getFourCharName() {
			return "MNSP";
		}
	}

	/** Prise en compte de la valeur d'attribut de la cible sans le comparer a l'attribut de l'acting.
	 *
	 */
	public class TheirSupTwo implements IFilter {
		int valueAttribut = 2;

		@Override
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			for (Entite entite : entites) {
					if(entite.getDegree() >= valueAttribut)
						resultat.add(entite);
				}


			entites.clear();
			entites.addAll(resultat);
		}

		public AgregatorType getEnumType() {
			return AgregatorType.THEIRSUP;
		}

		public String toString(){
			return "TheirSup2";
		}

		public String getFourCharName() {
			return "TRSP2";
		}
	}

	/**
	 *
	 */
	public class TheirSupParam implements IFilter {
		int valueAttribut = 6;

		public TheirSupParam(int param){
			valueAttribut = param;
		}

		@Override
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			for (Entite entite : entites) {
				if(entite.getDegree() >= valueAttribut)
					resultat.add(entite);
			}


			entites.clear();
			entites.addAll(resultat);
		}

		public AgregatorType getEnumType() {
			return AgregatorType.THEIRSUPSIX;
		}

		public String toString(){
			return "TheirSup6";
		}

		public String getFourCharName() {
			return "TRSP6";
		}
	}

	/** Prise en compte de la valeur d'attribut de la cible sans le comparer a l'attribut de l'acting.
	 *
	 */
	public class TheirEqual implements IFilter {

		@Override
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			for (Entite entite : entites) {
				if(entite.getDegree() == asker.getDegree())
					resultat.add(entite);
			}

			entites.clear();
			entites.addAll(resultat);
		}

		public AgregatorType getEnumType() {
			return AgregatorType.THEIREQUAL;
		}

		public String toString(){
			return "TheirEqual";
		}

		public String getFourCharName() {
			return "TREQ";
		}
	}

	/** CLASSE Valeur de l'attribut strictement inférieur a celle des entités sélectionnées
	 *
	 */
	public class MineInf implements IFilter {

		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<>();
			for (Entite entite : entites) {
				if(attribut.getAttributValue(entite.getNode()).compareTo(attribut.getAttributValue(asker.getNode())) == 1 ||
						(Configurator.strictEqualityInComparaison ? false :
						attribut.getAttributValue(entite.getNode()).compareTo(attribut.getAttributValue(asker.getNode())) == 0))
				{
					resultat.add(entite);
				}
			}

			entites.clear();
			entites.addAll(resultat);
		}

		public AgregatorType getEnumType() {
			return AgregatorType.MINEINF;
		}

		public String toString(){
			return "MineInf";
		}

		public String getFourCharName() {
			return "MNIF";
		}
	}

	/** CLASSE Valeur de l'attribut différente a celle des entités sélectionnées
	 *
	 */
	public class MineDif implements IFilter {
		@Override
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<Entite>();
			for (Entite entite : entites) {
				if(attribut.getAttributValue(entite.getNode()).compareTo(attribut.getAttributValue(asker.getNode())) != 0 ){
					resultat.add(entite);
				}
			}

			entites.clear();
			entites.addAll(resultat);
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
	public class MineEgal implements IFilter {
		@Override
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			ArrayList<Entite> resultat = new ArrayList<>();
			for (Entite entite : entites) {
				if(attribut.getAttributValue(entite.getNode()).compareTo(attribut.getAttributValue(asker.getNode())) == 0){
					resultat.add(entite);
				}
			}

			entites.clear();
			entites.addAll(resultat);
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

	/** Filter de type not linked, depuis une liste renvoi les éléments qui
	 * ne sont pas linké a l'entité en paramètre.
	 */
	public class notLinked extends Filter implements IFilter {

		/** Renvoi la liste des éléments non connectés a l'asker
		 *
		 */
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			this.getNotLinked(asker, entites);
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
	public class linked extends Filter implements IFilter {

		/** Renvoi un item au hasard depuis la liste fourni en parametre, qui n'est
		 * pas connecté au noeud en question.
		 *
		 */
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			 this.getLinked(asker, entites);
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
	public class random extends Filter implements IFilter {

		/** Application de l'agrégateur.
		 *
		 */
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			if(entites.size() > 1){
				Entite selected = null;
				int nbStep = Toolz.getRandomNumber(entites.size());
				for (Entite entite : entites) {
					if(nbStep ==  0) {
						selected = entite;
						break;
					}

					nbStep--;
				}

				entites.clear();
				entites.add(selected);
			}
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

	/** Renvoi les noeuds liés a une distance de @reach
	 *
	 */
	public class HopWay extends Filter implements IFilter {

		public int reach = 2;

		@Override
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {

			Set<Entite> entiteResult = new HashSet<>();

			if(Configurator.debugHopAway){
				String result = "before Entites";

				for (Entite entite : entites) {
					result +=":" + entite.getIndex();
				}
				System.out.println(result); // l'ensemble des entites pré filtrage
			}

			getNeightboor(entiteResult, entites, asker, reach);

			if(Configurator.debugHopAway){
				String result = "after Entites:";
				for (Entite entite : entiteResult) {
					result +=":" + entite.getIndex();
				}
				System.out.println(result);
			}

			entites.clear();
			entites.addAll(entiteResult);
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

		/** Fonction récursive qui va chercher les noeuds liés a la @target à une distance de @deepToGo initial.
		 * 1er appel : on regarde les voisins de @target et les rajoute dans une liste @entiteSearchSpace.
		 *  l @deepToGo est décrementé, si égal 0 on arrete l'appel récursif.
		 * @param entiteToReturn
		 * @param entiteSearchSpace
		 * @param target
		 * @param deepToGo
		 */
		private void getNeightboor(Set<Entite> entiteToReturn, Set<Entite> entiteSearchSpace, Entite target, int deepToGo){
			deepToGo --;
			Set<Entite> entiteAccepted = new HashSet<>();
			// pout chaque voisin connecté a la cible,
			for (Entite neighboor : target.getConnectedEntite()) {
				// On regarde si le voisin en question est dans la liste des éléments a regarder
				if(entiteSearchSpace.contains(neighboor)){
					entiteAccepted.add(neighboor);
					// on le retire de la liste des éléments que l'on regarde pour éviter les doublons retour
					entiteSearchSpace.remove(neighboor);
				}
			}

			if(deepToGo == 0)
			entiteToReturn.addAll(entiteAccepted);
			else if(deepToGo > 0)
				for (Entite entite : entiteAccepted)
					getNeightboor(entiteToReturn, entiteSearchSpace, entite, deepToGo);
		}

	}

	/** Renvoi les noeuds liés a une distance de @reach
	 *
	 */
	public class HopWay3 extends Filter implements IFilter {

		public int reach = 3;

		@Override
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {

			Set<Entite> entiteResult = new HashSet<>();

			if(Configurator.debugHopAway){
				String result = "before Entites";

				for (Entite entite : entites) {
					result +=":" + entite.getIndex();
				}
				System.out.println(result); // l'ensemble des entites pré filtrage
			}

			getNeightboor(entiteResult, entites, asker, reach);

			if(Configurator.debugHopAway){
				String result = "after Entites:";
				for (Entite entite : entiteResult) {
					result +=":" + entite.getIndex();
				}
				System.out.println(result);
			}

			entites.clear();
			entites.addAll(entiteResult);
		}

		public String toString(){
			return "Hopaway3";
		}

		@Override
		public AgregatorType getEnumType() {
			return AgregatorType.HOPAWAY3;
		}

		@Override
		public String getFourCharName() {
			return "HA3";
		}

		/** Fonction récursive qui va chercher les noeuds liés a la @target à une distance de @deepToGo initial.
		 * 1er appel : on regarde les voisins de @target et les rajoute dans une liste @entiteSearchSpace.
		 *  l @deepToGo est décrementé, si égal 0 on arrete l'appel récursif.
		 * @param entiteToReturn
		 * @param entiteSearchSpace
		 * @param target
		 * @param deepToGo
		 */
		private void getNeightboor(Set<Entite> entiteToReturn, Set<Entite> entiteSearchSpace, Entite target, int deepToGo){
			deepToGo --;
			Set<Entite> entiteAccepted = new HashSet<>();
			// pout chaque voisin connecté a la cible,
			for (Entite neighboor : target.getConnectedEntite()) {
				// On regarde si le voisin en question est dans la liste des éléments a regarder
				if(entiteSearchSpace.contains(neighboor)){
					entiteAccepted.add(neighboor);
					// on le retire de la liste des éléments que l'on regarde pour éviter les doublons retour
					entiteSearchSpace.remove(neighboor);
				}
			}

			if(deepToGo == 0)
				entiteToReturn.addAll(entiteAccepted);
			else if(deepToGo > 0)
				for (Entite entite : entiteAccepted)
					getNeightboor(entiteToReturn, entiteSearchSpace, entite, deepToGo);
		}

	}

	/** retourne les noeuds liés entre eux de ceux pris en param.
	 * Plus optimal si une réduction a déja été appelé avant
	 */
	public class Triangle extends Filter implements IFilter {

		@Override
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			Set<Entite> resultat = new HashSet<>();

			for (Entite entite : entites) {
				for (Entite entiteCo : entite.getConnectedEntite()) {
					if(asker.getConnectedEntite().contains(entiteCo))
						resultat.add(entiteCo);
				}
			}

			entites.retainAll(resultat);
		}

		@Override
		public AgregatorType getEnumType() {
			return AgregatorType.TRIANGLE;
		}

		@Override
		public String getFourCharName() {
			return "GL";
		}

		public String toString(){
			return "Triangle";
		}

	}

	/** ne dépend pas des autres
	 *
	 */
	public class SelfSup extends Filter implements IFilter {
		int attributValue = 1;


		@Override
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			Set<Entite> resultat = new HashSet<>();
			if(asker.getDegree() > attributValue){
			}else {
				entites.clear();
			}


		}

		@Override
		public AgregatorType getEnumType() {
			return AgregatorType.SELFSUP;
		}

		@Override
		public String getFourCharName() {
			return "SFSP";
		}

		public String toString(){
			return "SelfSup";
		}

	}

	/** Retourne tjrs vide.
	 *
	 */
	public class Blank extends Filter implements IFilter {

		@Override
		public <T extends Comparable<T>> void applyFilter(Entite asker, Set<Entite> entites, AttributFactory.IAttribut<T> attribut) {
			entites.clear();
		}

		@Override
		public AgregatorType getEnumType() {
			return AgregatorType.BLANK;
		}

		@Override
		public String getFourCharName() {
			return "BK";
		}

		public String toString(){
			return "Blank";
		}

	}

}