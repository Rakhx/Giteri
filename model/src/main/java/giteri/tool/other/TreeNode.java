package giteri.tool.other;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> {
	public T data;
	public TreeNode<T> parent;
    public List<TreeNode<T>> children;
    public String leafName;
    
    public TreeNode(String name, T rootData) {
        leafName = name;
    	data = rootData;
        children = new ArrayList<>();
    }

    /** recherche d'un node dans les enfants.
     * 
     * @param name
     * @return
     */
    public TreeNode<T> findNode(String name){
    	TreeNode<T> resultat = null;
    	if(leafName.compareTo(name)== 0)
    		resultat = this;
    	else 
    		for (TreeNode<T> treeNode : children) {
    			resultat = treeNode.findNode(name);
    			if(resultat != null)
    				break;
			}
    	
    	return resultat;
    }
    
    /** ajout d'un enfant au noeud courant.
     * 
     * @param name
     * @param data
     */
    public TreeNode<T> addChild(String name, T data){
    	TreeNode<T> child = new TreeNode<T>(name, data);
    	this.children.add(child);
    	return child;
    }
    
    public void print() {
        print("", true);
    }

    /** Affichage de l'arbre horizontalement
     * 
     * @param prefix
     * @param isTail
     */
    private void print(String prefix, boolean isTail) {
    	
    	DecimalFormat df = new DecimalFormat ( ) ;
    	df.setMaximumFractionDigits ( 3 ) ; //arrondi à 2 chiffres apres la virgules
    	df.setMinimumFractionDigits ( 3 ) ; 
//    	Double.parseDouble(df.format ( (double)usageByName.get(keyz)/getTotalTime()))+"%")
    	double parentTime = parent == null? 1: Double.parseDouble(parent.data.toString());
    	if(parent != null){
    		
    	}
    	System.out.println(prefix + (isTail ? "└── " : "├── ") + leafName + " "+ 
    			df.format( 100 * (Double.parseDouble(data.toString()) / parentTime ))
    					+"%");
        for (int i = 0; i < children.size() - 1; i++) {
            children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (children.size() > 0) {
            children.get(children.size() - 1).print(prefix + (isTail ?"    " : "│   "), true);
        }
    }
    
}