package giteri.tool.objects;

public class ObjectRef<T> {
	private T myValue;
	
	public ObjectRef(T baseValue){
		myValue = baseValue;
	}
	
	public void setValue(T toSet){
		myValue = toSet;
	}
	
	public T getValue(){
		return myValue;
	}
	
	public String toString(){
		return myValue.toString();
	}
}
