package giteri.meme.mecanisme;

import giteri.network.network.Node;
import giteri.run.configurator.Configurator;
import giteri.meme.entite.EntiteHandler;

/**
 * Attribut factory. Classe d'attibrut d'un noeud.
 *
 */
public class AttributFactory {
	private static AttributFactory instance = null;
	public AttributFactory(){
	}

	/** Renvoi l'attribut concerné
	 *
	 * @param attributName
	 * @return Un new de l'attribut en question
	 */
	public <T extends Comparable<T>> IAttribut<?> getAttribut(Configurator.AttributType attributName){
		switch (attributName) {
			case DEGREE:
				return new Degree();
			default:
				return null;
		}
	}

	/** Interface d'attribut, doit etre comparable
	 *
	 *
	 */
	public interface IAttribut<T extends Comparable<T>>
	{
		/** permet d'obtenir la valeur de l'attribut d'un node
		 *
		 * @param n le node dont on veut l'attribut
		 * @return un comparable, valeur de l'attribut
		 */
		public abstract T getAttributValue(Node n);

		/**
		 *
		 */
//		public EntiteHandler applier = EntiteHandler.getInstance();

		/**
		 *
		 * @return
		 */
		public String getFourCharName();

		/** Permet d'avoir une distance entre l'agregateur,
		 * le noeud en question et son obj
		 * @param ag
		 * @param n
		 * @return
		 */
//		public int getDistance(IFilter ag, Node n,ArrayList<Entite> cibles);
	}

	/** Attribut de nom degré, de type comparable Integer
	 *
	 * @param <> Integer
	 */
	public class Degree implements IAttribut<Integer>{

		/** Le degré d'un noeud en paramètre. 
		 *
		 */
		public Integer getAttributValue(Node n) {
			return new Integer(n.getConnectedNodes().size());
		}

		public String toString(){
			return "Degree";
		}

		/**
		 *
		 */
//		public int getDistance(IFilter ag, Node n, ArrayList<Entite> cibles) {
//			int degree = getAttributValue(n);
//			int malus = 0;
//			switch (ag.getEnumType()){
//			case THEMOST :
//				return Configurator.nbNode + 1 - degree;
//			case THELEAST:
//				return degree;
//			
//			case MINEEQUAL:
//				for (Entite entite : cibles) 
//					if(this.getAttributValue(n) != this.getAttributValue(entite.getNode()))
//						malus++;								
//				return malus;
//			
//			case MINEDIF:	
//				for (Entite entite : cibles) 
//					if(this.getAttributValue(n) == this.getAttributValue(entite.getNode()))
//						malus++;								
//				return malus;
//			
//			case MINESUP:
//				for (Entite entite : cibles) 
//					if(this.getAttributValue(n) <= this.getAttributValue(entite.getNode()))
//						malus++;								
//				return malus;
//			
//			case MINEINF:
//				for (Entite entite : cibles) 
//					if(this.getAttributValue(n) >= this.getAttributValue(entite.getNode()))
//						malus++;								
//				return malus;
//			// Ces cas la n'ont pas de sens pour une fonction objectif
//			case THEMOSTLINKED:
//			case THELEASTLINKED:
//			case LINKED:
//			case NOTLINKED:
//			case RANDOM:
//			}
//			return 0;
//		}

		@Override
		public String getFourCharName() {
			return "DG";
		}
	}


}

