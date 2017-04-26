package baseElements;

import configurator.Configurator;

public class BaseParameterTemp {
	public String name;
	public double value;
	public double max, min, step;
	public interface base
	{
		public void applyValue();
	}
	
	public class ParamEvapStuff extends BaseParameterTemp implements base {
		public void applyValue(){
			Configurator.setLifeTimeBeforeLinkEvaporation(value);
		}
	}
}
