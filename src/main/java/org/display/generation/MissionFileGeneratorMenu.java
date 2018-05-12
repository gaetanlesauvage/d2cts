package org.display.generation;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.com.dao.ScenarioDAO;
import org.com.model.ScenarioBean;
import org.display.GraphicDisplay;
import org.display.MainFrame;
import org.display.StoredSimulationChooser;
import org.util.generators.MissionsFileGenerator;



public class MissionFileGeneratorMenu {
	private static final Logger log = Logger.getLogger(MissionFileGeneratorMenu.class);
	private static final Dimension okCancelSize = new Dimension(200,25);
	
	private MainFrame mf;
	
	private StockMissionSettingsGenerationPanel stockPanel;
	private TruckMissionSettingsGenerationPanel truckPanel;
	private ShipMissionSettingsGenerationPanel shipPanel;
	private TrainMissionSettingsGenerationPanel trainPanel;
	
	private JComboBox<ScenarioBean> jcbScenarios;
	private JLabel jlScenarios;

	private JTabbedPane jtpParameters;

	public MissionFileGeneratorMenu (MainFrame mainFrame){
		this.mf = mainFrame;

		final JDialog jd = new JDialog(mainFrame.getFrame(), "Mission file generator", ModalityType.APPLICATION_MODAL);

		jlScenarios = new JLabel("Scenario : ");
		jlScenarios.setFont(GraphicDisplay.fontBold);
		ScenarioBean[] beans = new ScenarioBean[ScenarioDAO.getInstance().size()];
		//HERE FOR LOOP
		Iterator<ScenarioBean> iterator = ScenarioDAO.getInstance().iterator();
		for(int i=0; iterator.hasNext(); i++){
			beans[i] = iterator.next();
		}
		//TODO => Add into arry dao values then into combobox
		jcbScenarios = new JComboBox<>(beans);
		jcbScenarios.setFont(GraphicDisplay.font);

		final JLabel jlSeed = new JLabel("Seed : ");
		jlSeed.setFont(GraphicDisplay.fontBold);

		final SpinnerNumberModel seedModel = new SpinnerNumberModel(1, Long.MIN_VALUE, Long.MAX_VALUE, 1);
		final JSpinner jsSeed = new JSpinner(seedModel);
		jsSeed.setMinimumSize(new Dimension(100, 25));
		jsSeed.setPreferredSize(new Dimension(100, 25));
		jsSeed.setFont(GraphicDisplay.font);

		jtpParameters = new JTabbedPane();
		stockPanel = new StockMissionSettingsGenerationPanel();
		truckPanel = new TruckMissionSettingsGenerationPanel();
		shipPanel = new ShipMissionSettingsGenerationPanel();
		trainPanel = new TrainMissionSettingsGenerationPanel();
		
		jtpParameters.addTab("Stock", stockPanel);
		jtpParameters.addTab("Road", truckPanel);
		jtpParameters.addTab("Sea", shipPanel);
		jtpParameters.addTab("Train", trainPanel);

		final JButton jbOk = new JButton("OK");
		jbOk.setFont(GraphicDisplay.font);
		jbOk.setEnabled(true);
		jbOk.setMinimumSize(okCancelSize);
		jbOk.setPreferredSize(okCancelSize);

		final JButton jbCancel = new JButton("Cancel");
		jbCancel.setFont(GraphicDisplay.font);
		jbCancel.setMinimumSize(okCancelSize);
		jbCancel.setPreferredSize(okCancelSize);

		jbCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jd.setVisible(false);
				jd.dispose();
			}
		});

		jbOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t2 = new Thread(){
					public void run(){
						if(jcbScenarios.getSelectedIndex()>=0){
							mf.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							mf.getFrame().setEnabled(false);
							jd.dispose();

							try{
							new MissionsFileGenerator(jcbScenarios.getItemAt(jcbScenarios.getSelectedIndex()),
									seedModel.getNumber().intValue(), 
									trainPanel.getData(), 
									truckPanel.getData(),
									shipPanel.getData(),
									stockPanel.getData(),
									MissionFileGeneratorMenu.this.mf);
							} catch (Exception e){
								log.error(e.getMessage(), e);
								e.printStackTrace();
							}
							
							mf.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							mf.getFrame().setEnabled(true);
							
						}
					}
				};

				SwingUtilities.invokeLater(t2);
				try {
					t2.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}


			}
		});

		JPanel pConfig = new JPanel(new SpringLayout());
		pConfig.add(jlScenarios);
		pConfig.add(jcbScenarios);
		pConfig.add(jlSeed);
		pConfig.add(jsSeed);
		pConfig.setMaximumSize(new Dimension(900, 200));

		StoredSimulationChooser.makeCompactGrid(pConfig, 2, 2, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad

		JPanel pOkCancel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.insets = new Insets(5, 100, 5, 100);
		pOkCancel.add(jbCancel, constraints);
		constraints.gridx = 1;
		pOkCancel.add(jbOk, constraints);
		

		jd.add(pConfig,BorderLayout.NORTH);
		jd.add(jtpParameters, BorderLayout.CENTER);
		jd.add(pOkCancel,BorderLayout.SOUTH);

		jd.setPreferredSize(new Dimension(900,550));
		jd.pack();
		JFrame parent = mainFrame.getFrame();
		jd.setLocation(parent.getLocation().x+(parent.getSize().width/2 - jd.getSize().width/2), parent.getLocation().y+(parent.getSize().height/2 - jd.getSize().height/2));

		jd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		jd.setVisible(true);
	}
}
