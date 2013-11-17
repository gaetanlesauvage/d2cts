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
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.com.dao.ScenarioDAO;
import org.com.dao.SimulationDAO;
import org.com.dao.StraddleCarrierDAO;
import org.com.dao.TerminalDAO;
import org.com.dao.scheduling.AbstractSchedulingParameterDAO;
import org.com.model.ScenarioBean;
import org.com.model.SchedulingAlgorithmBean;
import org.com.model.SimulationBean;
import org.com.model.TerminalBean;
import org.com.model.scheduling.ParameterBean;

public class LoadSimulationDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*private static enum LoadColumns {
		private String name;
		private int size;

		private LoadColumns (String name, int size){
			this.name = name;
			this.size = size;
		}
	}*/

	private static final Logger log = Logger.getLogger(LoadSimulationDialog.class);

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

	public static final int WIDTH = 900;
	public static final int HEIGHT = 300;

	private static final int SIMULATION_ID_COLUMN_INDEX = 0;
	private static final int SIMULATION_DATE_REC_COLUMN_INDEX = 1;

	private static final int SCENARIO_ID_COLUMN_INDEX = 2;
	private static final int SCENARIO_NAME_COLUMN_INDEX = 3;

	private static final int TERMINAL_LABEL_COLUMN_INDEX = 4;
	private static final int STRADDLE_CARRIERS_COUNT_COLUMN_INDEX = 5;
	private static final int SCHEDULING_ALGORITHM_COLUMN_INDEX = 6;
	private static final int TERMINAL_SEED_COLUMN_INDEX = 7;


	private static final String SIMULATION_ID_COLUMN_NAME = "SIM ID";
	private static final String SIMULATION_DATE_REC_COLUMN_NAME = "DATE_REC";
	private static final String SCENARIO_ID_COLUMN_NAME = "SCENARIO ID";
	private static final String SCENARIO_NAME_COLUMN_NAME = "SCENARIO NAME";
	private static final String TERMINAL_LABEL_COLUMN_NAME = "TERMINAL";
	private static final String STRADDLE_CARRIERS_COUNT_COLUMN_NAME = "RESOURCES SIZE";
	private static final String TERMINAL_SEED_COLUMN_NAME = "SEED";
	private static final String SCHEDULING_ALGORITHM_COLUMN_NAME = "SCHEDULING ALGORITHM";

	private static final String[] COLUMNS_NAME = {
		SIMULATION_ID_COLUMN_NAME,
		SIMULATION_DATE_REC_COLUMN_NAME,
		SCENARIO_ID_COLUMN_NAME,
		SCENARIO_NAME_COLUMN_NAME,
		TERMINAL_LABEL_COLUMN_NAME,
		STRADDLE_CARRIERS_COUNT_COLUMN_NAME,
		SCHEDULING_ALGORITHM_COLUMN_NAME,
		TERMINAL_SEED_COLUMN_NAME
	};

	private static final int[] COLUMNS_SIZE = {
		30,
		75,
		30,
		75,
		75,
		30,
		75,
		30
	};

	private Integer scID;
	private Thread waitThreadScID;

	private JTable jtSimulation;

	private JButton jbLoad;


	public LoadSimulationDialog(JFrame frame) {
		super(frame, "Load Simulation", true);
		build();
	}

	public void build() {
		final SimulationDAO simulations = SimulationDAO.getInstance();
		try {
			simulations.load();
		} catch (SQLException e1) {
			log.error(e1.getMessage(), e1);
		}
		final ScenarioDAO scenarios = ScenarioDAO.getInstance();
		try {
			scenarios.load();
		} catch (SQLException e1) {
			log.error(e1.getMessage(), e1);
		}
		final TerminalDAO terminals = TerminalDAO.getInstance();

		// final StraddleCarrierDAO resources =
		// StraddleCarrierDAO.getInstance();

		//		String[] columnsName = new String[COLUMNS_COUNT];
		//		
		//		columnsName[SIMULATION_ID_COLUMN_INDEX] = SIMULATION_ID_COLUMN_NAME;
		//		columnsName[SIMULATION_DATE_REC_COLUMN_INDEX] = SIMULATION_DATE_REC_COLUMN_NAME;
		//		columnsName[SCENARIO_ID_COLUMN_INDEX] = SCENARIO_ID_COLUMN_NAME;
		//		columnsName[SCENARIO_NAME_COLUMN_INDEX] = SCENARIO_NAME_COLUMN_NAME;
		//		columnsName[TERMINAL_LABEL_COLUMN_INDEX] = TERMINAL_LABEL_COLUMN_NAME;
		//		columnsName[STRADDLE_CARRIERS_COUNT_COLUMN_INDEX] = STRADDLE_CARRIERS_COUNT_COLUMN_NAME;
		//		columnsName[SCHEDULING_ALGORITHM_COLUMN_INDEX] = SCHEDULING_ALGORITHM_COLUMN_NAME;
		//		columnsName[TERMINAL_SEED_COLUMN_INDEX] = TERMINAL_SEED_COLUMN_NAME;

		DefaultTableCellRenderer alignCenter = new DefaultTableCellRenderer();

		int index = 0;
		TableColumnModel tcm = new DefaultTableColumnModel();
		for(String colName : COLUMNS_NAME){
			TableColumn tc = new TableColumn(index,COLUMNS_SIZE[index], alignCenter, null);
			tc.setHeaderValue(colName);
			tcm.addColumn(tc);
			index++;
		}

		final TableModel tm = new DefaultTableModel(simulations.size(), COLUMNS_NAME.length);

		int rowIndex = 0;
		Iterator<SimulationBean> simIterator = simulations.iterator(); 
		while(simIterator.hasNext()){
			SimulationBean simulation = simIterator.next();
			ScenarioBean scenario = scenarios.getScenario(simulation.getContent());
			TerminalBean terminal = null;
			try {
				terminal = terminals.getTerminal(scenario.getTerminal());
			} catch (SQLException e1) {
				log.error(e1.getMessage(), e1);
			}
			SchedulingAlgorithmBean algo = simulation.getSchedulingAlgorithm();
			StraddleCarrierDAO resources = StraddleCarrierDAO.getInstance(scenario.getId());
			if(resources.size()==-1){
				try {
					resources.load();
				} catch (SQLException e1) {
					log.error(e1.getMessage(), e1);
				}
			}
			tm.setValueAt(simulation.getId(), rowIndex, SIMULATION_ID_COLUMN_INDEX);
			tm.setValueAt(DATE_FORMAT.format(simulation.getDate_rec()), rowIndex, SIMULATION_DATE_REC_COLUMN_INDEX);
			tm.setValueAt(scenario.getId(), rowIndex, SCENARIO_ID_COLUMN_INDEX);
			tm.setValueAt(scenario.getName(), rowIndex, SCENARIO_NAME_COLUMN_INDEX);
			//terminal
			tm.setValueAt(terminal.getLabel(), rowIndex, TERMINAL_LABEL_COLUMN_INDEX);
			//resources count
			tm.setValueAt(resources.size(), rowIndex, STRADDLE_CARRIERS_COUNT_COLUMN_INDEX);
			//scheduling algo
			tm.setValueAt(algo.getName(), rowIndex, SCHEDULING_ALGORITHM_COLUMN_INDEX);
			//seed
			tm.setValueAt(simulation.getSeed(), rowIndex, TERMINAL_SEED_COLUMN_INDEX);

			rowIndex++;
		}


		ListSelectionModel sm = new DefaultListSelectionModel();
		sm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		jtSimulation = new JTable(tm, tcm, sm){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public String getToolTipText(MouseEvent e){
				String tip = null;

				int rowIndex = rowAtPoint(e.getPoint());
				int columnIndex = columnAtPoint(e.getPoint());

				int realColumnIndex = convertColumnIndexToModel(columnIndex);

				if(realColumnIndex >= 0 && realColumnIndex == SCHEDULING_ALGORITHM_COLUMN_INDEX && rowIndex >=0){
					Integer simID = Integer.parseInt(tm.getValueAt(rowIndex, SIMULATION_ID_COLUMN_INDEX)+"");
					SimulationDAO instance = SimulationDAO.getInstance();
					tip = "";
					boolean first = true;

					SchedulingAlgorithmBean algo = instance.get(simID).getSchedulingAlgorithm();

					ParameterBean[] parameters = null;
					try {
						parameters = AbstractSchedulingParameterDAO.getInstance(algo.getName(), simID).get();
					} catch (SQLException e1) {
						log.error(e1);
					}
					if(parameters != null){
						for(ParameterBean parameter : parameters){
							if(first){
								first = false;
								tip = "Parameters: "+parameter.name()+"="+parameter.getValueAsString();
							} else {
								tip += ", "+parameter.name()+"="+parameter.getValueAsString();
							}
						}
					}
				}
				return tip;
			}
		};
		jtSimulation.setFont(GraphicDisplay.font);
		jtSimulation.getTableHeader().setFont(GraphicDisplay.fontBold);
		jtSimulation.setRowHeight(20);

		alignCenter.setHorizontalAlignment(JLabel.CENTER);
		alignCenter.setFont(GraphicDisplay.font);

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jtSimulation.getModel());
		sorter.setSortsOnUpdates(true);
		jtSimulation.setRowSorter(sorter);

		jtSimulation.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		jtSimulation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane jsp = new JScrollPane(jtSimulation, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		jbLoad = new JButton("OK");
		jtSimulation.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				updateLoadButton();
			}
		});

		/*jtSimulation.addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseMoved(MouseEvent e){
				if(e.get)
			}
		})*/;
		jbLoad.setEnabled(false);
		jbLoad.setFont(GraphicDisplay.font);
		jbLoad.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (jtSimulation.getSelectedRow() != -1) {
					// Load Simu with ID =
					scID = (Integer) jtSimulation.getValueAt(jtSimulation.getSelectedRow(), SIMULATION_ID_COLUMN_INDEX);
					log.info("User action: simulation " + scID + " chosen.");
					
					LoadSimulationDialog.this.setVisible(false);
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
				LoadSimulationDialog.this.setVisible(false);
				synchronized (waitThreadScID) {
					waitThreadScID.notify();
				}
			}
		});

		JPanel pSouth = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pSouth.add(jbCancel);
		pSouth.add(jbLoad);


		Insets defaultInsets = new Insets(5, 5, 5, 5);
		Insets noInsets = new Insets(0, 5, 0, 5);

		GridBagConstraints cJSPScenario = new GridBagConstraints(0, 0, 1, 3, 1d, 8d, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				defaultInsets, 0, 0);

		GridBagConstraints cButtons = new GridBagConstraints(0, 4, 1, 1, 1d, 0.5d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, noInsets, 0, 0);

		GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		gbl.setConstraints(jsp, cJSPScenario);
		gbl.setConstraints(pSouth, cButtons);
		this.add(jsp);
		this.add(pSouth);

		jtSimulation.setFillsViewportHeight(true);

		this.setLocationRelativeTo(super.getOwner());
		this.setLocation((int) ((super.getOwner().getWidth() / 2d) - (WIDTH / 2d)), (int) ((super.getOwner().getHeight() / 2d) - (HEIGHT / 2d)));
		this.setSize(new Dimension(WIDTH, HEIGHT));
	}

	public Integer getSelection() {
		waitThreadScID = new Thread("nsDialog") {
			public void run() {
				try {
					synchronized (waitThreadScID) {
						waitThreadScID.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
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

		return scID;
	}

	private void updateLoadButton() {
		int selectedRow = jtSimulation.getSelectedRow();
		if (selectedRow > -1) {
			jbLoad.setEnabled(jtSimulation.getSelectedRowCount() != 0);
		}
	}

}
