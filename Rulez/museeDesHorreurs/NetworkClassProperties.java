package networkStuff;

import configurator.Configurator.NetworkAttribType;

public class NetworkClassProperties {
	
	/**
	 *
	 */
	public interface INetworkClassProperties {
		public NetworkAttribType getType();
	}
	
	/** Agent like?
	 *
	 */
	public class Density implements Comparable<Density>, INetworkClassProperties {
		double value;

		@Override
		public int compareTo(Density o) {
			// TODO Auto-generated method stub
			return 0;
		}
	
		public NetworkAttribType getType(){
			return NetworkAttribType.DENSITY;
		}
		public String toString(){
			return "DENSITY";
		}

	
		
	}
}
