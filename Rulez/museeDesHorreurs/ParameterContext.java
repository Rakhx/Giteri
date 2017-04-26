package parameters;

import parameters.IModelParameter.ParameterLearningValue;
import parameters.IModelParameter.ValueParameterGeneric;
import configurator.Configurator.FittingParamType;


/** Contexte pour un param.
 *
 */
public class ParameterContext {
	public FittingParamType name; // les rendre obligatoires en fournissant un constructeur. 
	public double max = -1., min, step, defaultValue;

	/**
	 * 
	 */
	public ValueParameterGeneric getDefaultValue() {
		ValueParameterGeneric param;
		switch (name) {
//		case EVAP:
//			param = new ParameterEvapValue(this);
//			param.value = defaultValue;
//			return param;
//		case REMOVE:
//			param = new ParameterRemoveValue(this);
//			param.value = defaultValue;
//			return param;
		case LEARNINGPROBA:
			param = new ParameterLearningValue(this);
			param.value = defaultValue;
			return param;
		default:
			return null;
		}
	}
	
	/** renvoi le name.string()
	 * 
	 */
	public String toString(){
		return name.name();
	}
	
}
