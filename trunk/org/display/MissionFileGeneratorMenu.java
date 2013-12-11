package org.display;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridLayout;
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
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.com.dao.ScenarioDAO;
import org.com.model.ScenarioBean;
import org.util.generators.MissionsFileGenerator;
import org.util.generators.parsers.TrainGenerationData;



public class MissionFileGeneratorMenu {
	private MainFrame mf;

	private JComboBox<ScenarioBean> jcbScenarios;
	private JLabel jlScenarios;
	
	//GUI for TrainGenerationData
	//GUI for TruckGenerationData
	//GUI for ShipGenerationData
	//GUI for StockGenerationData
	
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

		final JLabel jlVehiclesCount = new JLabel("Number of straddle carriers : ");
		jlVehiclesCount.setFont(GraphicDisplay.font);

		final JSpinner jsVehicleCount = new JSpinner(new SpinnerNumberModel(4, 1, 20, 1));
		jsVehicleCount.setFont(GraphicDisplay.font);

		final JLabel jlSeed = new JLabel("Seed : ");
		jlVehiclesCount.setFont(GraphicDisplay.font);

		final JSpinner jsSeed = new JSpinner(new SpinnerNumberModel(1, Long.MIN_VALUE, Long.MAX_VALUE, 1));
		jsVehicleCount.setFont(GraphicDisplay.font);

		
		final JButton jbOk = new JButton("OK");
		jbOk.setFont(GraphicDisplay.font);
		jbOk.setEnabled(true);

		final JButton jbCancel = new JButton("Cancel");
		jbCancel.setFont(GraphicDisplay.font);


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

							new MissionsFileGenerator(jcbScenarios.getItemAt(jcbScenarios.getSelectedIndex()), (Integer)jsVehicleCount.getValue(), (Long)jsSeed.getValue(), mf);
							
							mf.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							//jd.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							mf.getFrame().setEnabled(true);
							//jd.dispose();

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
		pConfig.add(jlVehiclesCount);
		pConfig.add(jsVehicleCount);
		pConfig.add(jlSeed);
		pConfig.add(jsSeed);
		pConfig.setMaximumSize(new Dimension(800, 250));
		
		StoredSimulationChooser.makeCompactGrid(pConfig, 3, 2, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad

		JPanel pOkCancel = new JPanel(new GridLayout(1,5,0,5));
		pOkCancel.add(new JLabel());
		pOkCancel.add(jbCancel);
		pOkCancel.add(new JLabel());
		pOkCancel.add(jbOk);
		pOkCancel.add(new JLabel());

		jd.add(pConfig,BorderLayout.CENTER);
		jd.add(pOkCancel,BorderLayout.SOUTH);

		jd.setPreferredSize(new Dimension(300,175));
		jd.pack();
		JFrame parent = mainFrame.getFrame();
		jd.setLocation(parent.getLocation().x+(parent.getSize().width/2 - jd.getSize().width/2), parent.getLocation().y+(parent.getSize().height/2 - jd.getSize().height/2));

		jd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		jd.setVisible(true);
	}
}
