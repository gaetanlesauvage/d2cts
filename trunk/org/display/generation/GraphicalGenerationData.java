package org.display.generation;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.TreeNode;

public class GraphicalGenerationData<E> implements TreeNode {
	private GraphicalGenerationData<E> parent;
	private Vector<GraphicalGenerationData<E>> children;
	private E element;

	public GraphicalGenerationData (GraphicalGenerationData<E> parent, E element){
		this.parent = parent;
		children = new Vector<>();
		this.element = element; 
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return children.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return children.size();
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		
		for(int i=0; i<children.size(); i++){
			GraphicalGenerationData<E> child = children.get(i);
			if(child.equals(node)){
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public boolean isLeaf() {
		return children.isEmpty();
	}

	@Override
	public Enumeration<GraphicalGenerationData<E>> children() {
		return children.elements(); 
	}

	public E getData(){
		return element;
	}

	public GraphicalGenerationData<E> addChild(E element){
		GraphicalGenerationData<E> n = new GraphicalGenerationData<>(this, element);
		children.add(n);
		return n;
	}

	public void removeChild(GraphicalGenerationData<E> child){
		children.remove(getIndex(child));
	}

	@Override
	public int hashCode() {
		return element != null ? element.hashCode() : "root".hashCode();
	}

	@Override
	public boolean equals(Object o){
		return o.hashCode() == hashCode();
	}

	public String toString(){
		return element != null ? element.toString() : "stockMissions :".toString();
	}

}