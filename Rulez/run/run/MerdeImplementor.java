package run;

import interfaces.Interfaces.ISetValues;
import configurator.Configurator;
import entite.EntiteHandler;

public class MerdeImplementor {
	
	public class evapMerdeImplementator implements ISetValues{
		public void setValue(Double value) {
			Configurator.setProbaRetraitLien(value);
			EntiteHandler.getInstance().resetProba();
		}
	}
	
	public class elapsedTimeEvapMerdeImplementator implements ISetValues{
		public void setValue(Double value){
//			Configurator.setLifeTimeBeforeLinkEvaporation(value);
			// do nothing
		}
	}
}
