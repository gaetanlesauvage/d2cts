package org.display;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.com.dao.ScenarioDAO;
import org.com.dao.SchedulingAlgorithmDAO;
import org.com.dao.scheduling.DefaultParametersDAO;
import org.com.model.ScenarioBean;
import org.com.model.SchedulingAlgorithmBean;
import org.com.model.scheduling.BBParametersBean;
import org.com.model.scheduling.BranchAndBoundParametersBean;
import org.com.model.scheduling.DefaultParametersBean;
import org.com.model.scheduling.GreedyParametersBean;
import org.com.model.scheduling.LinearParametersBean;
import org.com.model.scheduling.OfflineACO2ParametersBean;
import org.com.model.scheduling.OfflineACOParametersBean;
import org.com.model.scheduling.OnlineACOParametersBean;
import org.com.model.scheduling.ParameterBean;
import org.com.model.scheduling.ParameterType;
import org.com.model.scheduling.RandomParametersBean;
import org.com.model.scheduling.SchedulingParametersBeanInterface;
import org.display.panes.TableCellRendererCentered;
import org.scheduling.LinearMissionScheduler;
import org.scheduling.bb.BB;
import org.scheduling.branchAndBound.BranchAndBound;
import org.scheduling.greedy.GreedyMissionScheduler;
import org.scheduling.offlineACO.OfflineACOScheduler;
import org.scheduling.offlineACO2.OfflineACOScheduler2;
import org.scheduling.onlineACO.OnlineACOScheduler;
import org.scheduling.random.RandomMissionScheduler;
import org.system.Terminal;
import org.util.SimulationParameter;

public class NewSimulationDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(NewSimulationDialog.class);

	public static final int WIDTH = 750;
	public static final int HEIGHT = 300;

	private static final int SCENARIO_ID_COLUMN_INDEX = 0;
	private static final int SCENARIO_NAME_COLUMN_INDEX = 1;
	private static final int SCENARIO_DATE_REC_COLUMN_INDEX = 2;
	private static final int SCENARIO_CONTENT_FILE_COLUMN_INDEX = 3;
	public static final int SCHEDULING_ALGORITHM_COLUMN_INDEX = 4;

	private static final String SCHEDULING_ALGORITHM_COLUMN_NAME = "SCHEDULING ALGORITHM";

	private Integer scID;

	private SchedulingAlgorithmBean schedulingAlgorithmBean;

	private SimulationParameter parameters;

	private Thread waitThreadScID;

	private JScrollPane pParameters;
	private GridBagConstraints cJSPParameters;
	private JTable jtScenario;
	private JTable jtParameters;
	private JSpinner jtfSeed;

	private JButton jbLoad;

	private SchedulingAlgorithmBean lastSelected = null;

	public NewSimulationDialog(JFrame frame) {
		super(frame, "Create Simulation", true);
		build();
	}

	public void build() {
		final ScenarioDAO s = ScenarioDAO.getInstance();
		final SchedulingAlgorithmDAO a = SchedulingAlgorithmDAO.getInstance();

		String[] columnsName = new String[s.getColumnsName().length + 1];
		int i = 0;
		for (String sName : s.getColumnsName()) {
			columnsName[i++] = sName;
		}
		columnsName[i] = SCHEDULING_ALGORITHM_COLUMN_NAME;

		/*
		 * final TableModel tm = new NonEditableTableModel(columnsName,
		 * s.size());
		 */

		Vector<SchedulingAlgorithmBean> content = new Vector<>(a.size());
		for (Iterator<SchedulingAlgorithmBean> it = a.iterator(); it.hasNext();) {
			content.add(it.next());
		}
		final JComboBox<SchedulingAlgorithmBean> jcb = new JComboBox<>(content);
		jcb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jcb.getSelectedIndex() > -1) {
					SchedulingAlgorithmBean selected = (SchedulingAlgorithmBean) jcb.getSelectedItem();
					if (lastSelected == null || !selected.getName().equals(lastSelected.getName())) {
						updateParameters(selected);
						lastSelected = selected;
					}
				} else {
					jtParameters.setEnabled(false);
				}
				updateLoadButton();
			}
		});
		jcb.setFont(GraphicDisplay.font);
		// tm.setValueAt(jcb, rowIndex++, SCHEDULING_ALGORITHM_COLUMN_INDEX);

		DefaultTableCellRenderer alignCenter = new DefaultTableCellRenderer();

		TableColumn tcID = new TableColumn(SCENARIO_ID_COLUMN_INDEX, 50, alignCenter, null);
		tcID.setHeaderValue(columnsName[SCENARIO_ID_COLUMN_INDEX]);

		TableColumn tcName = new TableColumn(SCENARIO_NAME_COLUMN_INDEX, 75, alignCenter, null);
		tcName.setHeaderValue(columnsName[SCENARIO_NAME_COLUMN_INDEX]);

		TableColumn tcDate = new TableColumn(SCENARIO_DATE_REC_COLUMN_INDEX, 75, alignCenter, null);
		tcDate.setHeaderValue(columnsName[SCENARIO_DATE_REC_COLUMN_INDEX]);

		TableColumn tcContent = new TableColumn(SCENARIO_CONTENT_FILE_COLUMN_INDEX, 100, alignCenter, null);
		tcContent.setHeaderValue(columnsName[SCENARIO_CONTENT_FILE_COLUMN_INDEX]);

		TableCellEditor jcbce = new DefaultCellEditor(jcb);
		jcbce.getCellEditorValue();
		TableColumn tc = new TableColumn(SCHEDULING_ALGORITHM_COLUMN_INDEX, 150, alignCenter, jcbce);
		tc.setHeaderValue(SCHEDULING_ALGORITHM_COLUMN_NAME);

		TableModel tm = new DefaultTableModel(s.size(), columnsName.length);

		int rowIndex = 0;
		for (ScenarioBean scenario : s) {
			tm.setValueAt(scenario.getId(), rowIndex, SCENARIO_ID_COLUMN_INDEX);
			tm.setValueAt(scenario.getName(), rowIndex, SCENARIO_NAME_COLUMN_INDEX);
			tm.setValueAt(scenario.getDate_rec(), rowIndex, SCENARIO_DATE_REC_COLUMN_INDEX);
			tm.setValueAt(scenario.getFile(), rowIndex, SCENARIO_CONTENT_FILE_COLUMN_INDEX);

			rowIndex++;
		}

		TableColumnModel tcm = new DefaultTableColumnModel();
		tcm.addColumn(tcID);
		tcm.addColumn(tcName);
		tcm.addColumn(tcDate);
		tcm.addColumn(tcContent);
		tcm.addColumn(tc);

		ListSelectionModel sm = new DefaultListSelectionModel();
		sm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		jtScenario = new JTable(tm, tcm, sm);
		jtScenario.setFont(GraphicDisplay.font);
		jtScenario.getTableHeader().setFont(GraphicDisplay.fontBold);
		jtScenario.setRowHeight(20);

		alignCenter.setHorizontalAlignment(JLabel.CENTER);
		alignCenter.setFont(GraphicDisplay.font);

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jtScenario.getModel());
		sorter.setSortsOnUpdates(true);
		jtScenario.setRowSorter(sorter);

		jtScenario.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		jtScenario.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane jsp = new JScrollPane(jtScenario, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		jtfSeed = new JSpinner(new SpinnerNumberModel(Terminal.DEFAULT_SEED, Long.MIN_VALUE, Long.MAX_VALUE, 1));

		jbLoad = new JButton("OK");
		jtScenario.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				updateLoadButton();
			}
		});
		jbLoad.setEnabled(false);
		jbLoad.setFont(GraphicDisplay.font);
		jbLoad.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (jtScenario.getSelectedRow() != -1) {
					// Create Simu with scenario ID =
					scID = (Integer) jtScenario.getValueAt(jtScenario.getSelectedRow(), SCENARIO_ID_COLUMN_INDEX);
					// scID = (Integer) tm.getValueAt(jt.getSelectedRow(),
					// SCENARIO_ID_COLUMN_INDEX);
					schedulingAlgorithmBean = (SchedulingAlgorithmBean) jtScenario.getColumn(SCHEDULING_ALGORITHM_COLUMN_NAME).getCellEditor()
							.getCellEditorValue();
					SchedulingParametersBeanInterface parameter = null;
					// Parameters

					for (int i = 0; i < jtParameters.getColumnCount(); i++) {
						switch (schedulingAlgorithmBean.getName()) {
						case OnlineACOScheduler.rmiBindingName:
							parameter = OnlineACOParametersBean.get(jtParameters.getColumnModel().getColumn(i).getHeaderValue().toString());
							break;
						case LinearMissionScheduler.rmiBindingName:
							parameter = LinearParametersBean.get(jtParameters.getColumnModel().getColumn(i).getHeaderValue().toString());
							break;
						case RandomMissionScheduler.rmiBindingName:
							parameter = RandomParametersBean.get(jtParameters.getColumnModel().getColumn(i).getHeaderValue().toString());
							break;
						case GreedyMissionScheduler.rmiBindingName:
							parameter = GreedyParametersBean.get(jtParameters.getColumnModel().getColumn(i).getHeaderValue().toString());
							break;
						case BB.rmiBindingName:
							parameter = BBParametersBean.get(jtParameters.getColumnModel().getColumn(i).getHeaderValue().toString());
							break;
						case BranchAndBound.rmiBindingName:
							parameter = BranchAndBoundParametersBean.get(jtParameters.getColumnModel().getColumn(i).getHeaderValue().toString());
							break;
						case OfflineACOScheduler.rmiBindingName:
							parameter = OfflineACOParametersBean.get(jtParameters.getColumnModel().getColumn(i).getHeaderValue().toString());
							break;
						case OfflineACOScheduler2.rmiBindingName:
							parameter = OfflineACO2ParametersBean.get(jtParameters.getColumnModel().getColumn(i).getHeaderValue().toString());
							break;
						}
						if (parameter != null) {
							Object o = jtParameters.getValueAt(0, jtParameters.getColumnModel().getColumn(i).getModelIndex());
							if (o != null) {
								parameter.setValue(o);
							} else {
								log.error("Invalid parameter " + parameter.name() + " = " + o);
								return;
							}
						}
					}
					System.out.println("SCENARIO " + scID + " with scheduling algorithm " + schedulingAlgorithmBean + " chosen!");
					NewSimulationDialog.this.setVisible(false);
					synchronized (waitThreadScID) {
						waitThreadScID.notify();
					}
				}
			}
		});
		final JButton jbCancel = new JButton("CANCEL");
		jbCancel.setFont(GraphicDisplay.font);
		jbCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewSimulationDialog.this.setVisible(false);
				synchronized (waitThreadScID) {
					waitThreadScID.notify();
				}
			}
		});

		JPanel pSouth = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pSouth.add(new JLabel("Seed: "));
		pSouth.add(jtfSeed);
		pSouth.add(jbCancel);
		pSouth.add(jbLoad);

		TableColumnModel tcmp = new DefaultTableColumnModel();
		TableModel tmp = new DefaultTableModel(1, 5);
		ListSelectionModel smp = new DefaultListSelectionModel();
		smp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		jtParameters = new JTable(tmp, tcmp, smp);
		jtParameters.setFont(GraphicDisplay.font);
		jtParameters.getTableHeader().setFont(GraphicDisplay.fontBold);

		jtParameters.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		pParameters = new JScrollPane(jtParameters, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jtParameters.setRowHeight(20);
		jtParameters.setEnabled(false);

		Insets defaultInsets = new Insets(5, 5, 5, 5);
		Insets noInsets = new Insets(0, 5, 0, 5);

		GridBagConstraints cJSPScenario = new GridBagConstraints(0, 0, 1, 3, 1d, 8d, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				defaultInsets, 0, 0);
		cJSPParameters = new GridBagConstraints(0, 3, 1, 1, 1d, 1d, GridBagConstraints.BASELINE, GridBagConstraints.BOTH, noInsets, 0, -5);

		GridBagConstraints cButtons = new GridBagConstraints(0, 4, 1, 1, 1d, 0.5d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, noInsets, 0, 0);

		GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		gbl.setConstraints(jsp, cJSPScenario);
		gbl.setConstraints(pParameters, cJSPParameters);
		gbl.setConstraints(pSouth, cButtons);
		this.add(jsp);
		this.add(pParameters);
		this.add(pSouth);

		jtScenario.setFillsViewportHeight(true);
		jtParameters.setFillsViewportHeight(true);

		this.setLocationRelativeTo(super.getOwner());
		this.setLocation((int) ((super.getOwner().getWidth() / 2d) - (WIDTH / 2d)), (int) ((super.getOwner().getHeight() / 2d) - (HEIGHT / 2d)));
		this.setSize(new Dimension(WIDTH, HEIGHT));
	}

	public SimulationParameter getSelection() {
		waitThreadScID = new Thread("nsDialog") {
			public void run() {
				try {
					synchronized (waitThreadScID) {
						waitThreadScID.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Number n = (Number) jtfSeed.getValue();

				ParameterBean[] parameters2 = getParameters();
				if(parameters2 != null){
					schedulingAlgorithmBean.setParameters(parameters2);
					parameters = new SimulationParameter(scID, schedulingAlgorithmBean, n.longValue());
				}
			}
		};
		waitThreadScID.start();
		setVisible(true);
		try {
			waitThreadScID.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return parameters;
	}

	private void updateLoadButton() {
		int selectedRow = jtScenario.getSelectedRow();
		if (selectedRow > -1 && jtScenario.getModel().getValueAt(selectedRow, SCHEDULING_ALGORITHM_COLUMN_INDEX) != null) {
			jbLoad.setEnabled(jtScenario.getSelectedRowCount() != 0);
		}
	}

	private ParameterBean[] getParameters() {
		if(schedulingAlgorithmBean != null){
			switch (schedulingAlgorithmBean.getName()) {
			case OnlineACOScheduler.rmiBindingName:
				return OnlineACOParametersBean.getAll().toArray(new ParameterBean[OnlineACOParametersBean.values().length]);
			case OfflineACOScheduler.rmiBindingName:
				return OfflineACOParametersBean.getAll().toArray(new ParameterBean[OfflineACOParametersBean.values().length]);
			case OfflineACOScheduler2.rmiBindingName:
				return OfflineACO2ParametersBean.getAll().toArray(new ParameterBean[OfflineACO2ParametersBean.values().length]);
			case LinearMissionScheduler.rmiBindingName:
				return LinearParametersBean.getAll().toArray(new ParameterBean[LinearParametersBean.values().length]);
			case GreedyMissionScheduler.rmiBindingName:
				return GreedyParametersBean.getAll().toArray(new ParameterBean[GreedyParametersBean.values().length]);
			case RandomMissionScheduler.rmiBindingName:
				return RandomParametersBean.getAll().toArray(new ParameterBean[RandomParametersBean.values().length]);
			case BranchAndBound.rmiBindingName:
				return BranchAndBoundParametersBean.getAll().toArray(new ParameterBean[BranchAndBoundParametersBean.values().length]);
			case BB.rmiBindingName:
				return BBParametersBean.getAll().toArray(new ParameterBean[BBParametersBean.values().length]);
			}
		}
		return null;
	}

	private void updateParameters(SchedulingAlgorithmBean bean) {
		ListSelectionModel sm = new DefaultListSelectionModel();
		sm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TableColumnModel cm = new DefaultTableColumnModel();
		TableCellRenderer alignCenter = new TableCellRendererCentered();
		DefaultParametersDAO defaultParameters = DefaultParametersDAO.getInstance();
		String[] names = null;
		ParameterType[] types = null;

		switch (bean.getName()) {
		case OnlineACOScheduler.rmiBindingName:
			names = OnlineACOParametersBean.names();
			types = OnlineACOParametersBean.types();
			break;
		case OfflineACOScheduler.rmiBindingName:
			names = OfflineACOParametersBean.names();
			types = OfflineACOParametersBean.types();
			break;
		case OfflineACOScheduler2.rmiBindingName:
			names = OfflineACO2ParametersBean.names();
			types = OfflineACO2ParametersBean.types();
			break;
		case LinearMissionScheduler.rmiBindingName:
			names = LinearParametersBean.names();
			types = LinearParametersBean.types();
			break;
		case GreedyMissionScheduler.rmiBindingName:
			names = GreedyParametersBean.names();
			types = GreedyParametersBean.types();
			break;
		case RandomMissionScheduler.rmiBindingName:
			names = RandomParametersBean.names();
			types = RandomParametersBean.types();
			break;
		case BranchAndBound.rmiBindingName:
			names = BranchAndBoundParametersBean.names();
			types = BranchAndBoundParametersBean.types();
			break;
		case BB.rmiBindingName:
			names = BBParametersBean.names();
			types = BBParametersBean.types();
			break;
		}

		if (names != null && types != null) {
			// Build TableModel
			TableModel dm = new DefaultTableModel(1, names.length);
			// Create columns
			int i = 0;
			for (String name : names) {
				DefaultParametersBean dfBean = defaultParameters.get(name.toUpperCase());
				TableCellEditor editor = null;

				if (types[i] == ParameterType.DOUBLE) {
					editor = new DefaultCellEditor(new JTextField(5));
				} else if (types[i] == ParameterType.INTEGER) {
					editor = new DefaultCellEditor(new JTextField(3));
				} else {
					editor = new DefaultCellEditor(new JTextField(50));
				}

				if (dfBean != null) {
					dm.setValueAt(dfBean.getValue(), 0, i);
				} else {
					System.err.println("No parameter " + name);
					if (types[i] == ParameterType.DOUBLE)
						dm.setValueAt(new Double(1.0), 0, i);
					else if (types[i] == ParameterType.STRING)
						dm.setValueAt("", 0, i);
					else if (types[i] == ParameterType.INTEGER)
						dm.setValueAt(new Integer(1), 0, i);
				}
				TableColumn col = new TableColumn(i, 50, alignCenter, editor);
				col.setHeaderValue(name);
				cm.addColumn(col);

				i++;
			}

			jtParameters.setModel(dm);
			jtParameters.setColumnModel(cm);
			jtParameters.setSelectionModel(sm);
			jtParameters.setEnabled(true);

			jtParameters.updateUI();
		}
	}
}
