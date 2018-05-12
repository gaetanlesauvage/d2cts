package org.display.generation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.display.StoredSimulationChooser;

public abstract class GenerationDataPanel<E> extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3346935953551560779L;
	private static final Dimension ELEMENT_CONTROL_SIZE = new Dimension(250, 300);
	private static final Dimension LIST_SIZE = new Dimension(100, 250);
	protected static final int MIN_COUNT_VALUE = 1;
	protected static Date minTime;


	protected static int COUNTER = 1;
	public static Date defaultMaxArrivalTime;
	public static Date defaultMinBerthTime;
	public static Date defaultMaxDepartureTime;
	public static Date defaultContainerTimeOperation;

	protected E selectedData;
	protected Vector<E> data;
	protected JList<E> list;
	private JScrollPane jspList;

	protected JButton jbAddData;
	protected JButton jbRemoveData;

	protected JPanel pElementControl;
	protected JPanel pTreeControl;
	protected TitledBorder tb;
	protected GridBagConstraints constraints;

	static {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		minTime = c.getTime();

		c.set(Calendar.MINUTE, 30);
		defaultMaxArrivalTime =  c.getTime();
		defaultMinBerthTime = c.getTime();
		c.set(Calendar.HOUR_OF_DAY, 1);
		defaultMaxDepartureTime = c.getTime();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 5);
		defaultContainerTimeOperation = c.getTime();
	}

	protected GenerationDataPanel(){
		super(new SpringLayout());
		data = new Vector<>();
		list = new JList<>(data);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jspList = new JScrollPane(list);
		jspList.setPreferredSize(LIST_SIZE);
		jspList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		// Panels
		buildTreePanel();
		buildCommonElementPanel();
		buildElementPanel();

		// Listeners
		buildListeners();

		this.add(pTreeControl);
		this.add(pElementControl);

		StoredSimulationChooser.makeCompactGrid(this, 1, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad


	}

	private void buildTreePanel() {
		pTreeControl = new JPanel(new GridBagLayout());

		GridBagConstraints constraintList = new GridBagConstraints();
		constraintList.fill = GridBagConstraints.BOTH;
		constraintList.weightx = 2.0;
		constraintList.weighty = 1.0;
		constraintList.gridwidth = GridBagConstraints.REMAINDER;

		Border bTree = BorderFactory.createLineBorder(Color.black, 1, true);
		TitledBorder tbTree = BorderFactory.createTitledBorder(bTree, "Add/Remove/Select a group of missions :");
		tbTree.setTitlePosition(TitledBorder.CENTER);
		tbTree.setTitleJustification(TitledBorder.TOP);
		pTreeControl.setBorder(tbTree);
		pTreeControl.setMinimumSize(new Dimension(100, 200));
		pTreeControl.setPreferredSize(new Dimension(100, 200));

		jbAddData = new JButton("Add");
		jbAddData.setMinimumSize(new Dimension(100, 25));
		jbAddData.setPreferredSize(new Dimension(100, 25));

		jbRemoveData = new JButton("Remove");
		jbRemoveData.setMinimumSize(new Dimension(100, 25));
		jbRemoveData.setPreferredSize(new Dimension(100, 25));


		pTreeControl.add(jspList, constraintList);
		constraintList.fill = GridBagConstraints.NONE;
		constraintList.gridwidth = GridBagConstraints.RELATIVE;
		constraintList.gridy = 9;
		constraintList.insets = new Insets(5, 5, 5, 5);
		pTreeControl.add(jbAddData, constraintList);
		constraintList.gridwidth = GridBagConstraints.REMAINDER;
		constraintList.gridx = 1;
		pTreeControl.add(jbRemoveData, constraintList);
	}

	protected abstract void buildElementPanel();

	protected abstract void setStates(boolean state);

	protected abstract void buildListeners ();

	protected void buildCommonElementPanel() {
		pElementControl = new JPanel(new GridBagLayout());

		Border b = BorderFactory.createLineBorder(Color.black, 1, true);
		tb = BorderFactory.createTitledBorder(b, "Settings :");
		tb.setTitlePosition(TitledBorder.CENTER);
		tb.setTitleJustification(TitledBorder.TOP);
		pElementControl.setBorder(tb);

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.fill = GridBagConstraints.HORIZONTAL;

		pElementControl.setMinimumSize(ELEMENT_CONTROL_SIZE);
		pElementControl.setPreferredSize(ELEMENT_CONTROL_SIZE);	
	}

	public Collection<E> getData() {
		return data;
	}	
}
