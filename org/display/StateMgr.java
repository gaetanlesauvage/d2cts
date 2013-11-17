package org.display;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;

public class StateMgr {
	Map<JComponent, Boolean> states;
	
	public StateMgr(JComponent ... components){
		states = new HashMap<JComponent, Boolean>();
		for(JComponent b : components){
			states.put(b, b.isEnabled());
		}
	}
	
	public void save(){
		Iterator<JComponent> it = states.keySet().iterator();
		while(it.hasNext()){
			JComponent jc= it.next();
			states.put(jc, jc.isEnabled());
		}
	}
	
	//Should be in EDT
	public void load(){
		for(Entry<JComponent, Boolean> e : states.entrySet()){
			e.getKey().setEnabled(e.getValue());
		}
	}
}