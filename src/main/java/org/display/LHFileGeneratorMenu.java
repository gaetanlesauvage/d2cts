package org.display;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.time.Time;
import org.util.generators.LaserHeadRangeEventsGenerator;
import org.xml.sax.SAXException;


public class LHFileGeneratorMenu {
	private MainFrame mf;

	private JLabel jlFile, jlTargetFile, jlMaxTime, jlIntervalTime, jlMaxVariation, jlP_change, jlR_avg, jl_inf_avg, jl_sup_avg;
	private JButton jbOk, jbCancel, jbChooseFile,jbChooseNewFile;
	private JTextField jtfFile,jtfNewFile;
	private JSpinner jsMaxTimeH, jsMaxTimeM, jsMaxTimeS, jsIntervalH, jsIntervalM, jsIntervalS, jsMaxVar;
	private JSlider jslider_p_change, jslider_r_avg, jslider_p_inf_avg, jslider_p_sup_avg;


	public LHFileGeneratorMenu (MainFrame mainFrame){
		this.mf = mainFrame;

		final JDialog jd = new JDialog(null, "Laser Head Event File Generator", ModalityType.APPLICATION_MODAL);

		//DEPLOYMENT FILE
		jlFile = new JLabel("Deployment file : ");
		jlFile.setFont(GraphicDisplay.font);
		jtfFile = new JTextField();
		jtfFile.setFont(GraphicDisplay.font);
		jbChooseFile = new JButton("...");
		jbChooseFile.setMaximumSize(new Dimension(30, 30));
		jbChooseFile.setFont(GraphicDisplay.fontBold);


		//DESTINATION FILE
		jlTargetFile = new JLabel("Destination file : ");
		jlTargetFile.setFont(GraphicDisplay.font);
		jtfNewFile = new JTextField();
		jtfNewFile.setFont(GraphicDisplay.font);
		jbChooseNewFile = new JButton("...");
		jbChooseNewFile.setFont(GraphicDisplay.fontBold);
		jbChooseNewFile.setMaximumSize(new Dimension(30, 30));


		//MAX TIME : 
		jlMaxTime = new JLabel("Max time: ");
		jlMaxTime.setFont(GraphicDisplay.font);
		jsMaxTimeH = new JSpinner(new SpinnerNumberModel(0,0, 23, 1));
		jsMaxTimeH.setFont(GraphicDisplay.font);
		
		jsMaxTimeM = new JSpinner(new SpinnerNumberModel(0,0, 59, 1));
		jsMaxTimeM.setFont(GraphicDisplay.font);
		jsMaxTimeS = new JSpinner(new SpinnerNumberModel(0,0, 59, 1));
		jsMaxTimeS.setFont(GraphicDisplay.font);

		//Interval Time
		jlIntervalTime = new JLabel("Interval time: ");
		jlIntervalTime.setFont(GraphicDisplay.font);
		jsIntervalH = new JSpinner(new SpinnerNumberModel(0,0, 23, 1));
		jsIntervalH.setFont(GraphicDisplay.font);
		jsIntervalM = new JSpinner(new SpinnerNumberModel(0,0, 59, 1));
		jsIntervalM.setFont(GraphicDisplay.font);
		jsIntervalS = new JSpinner(new SpinnerNumberModel(0,0, 59, 1));
		jsIntervalS.setFont(GraphicDisplay.font);

		//Sliders
		jlMaxVariation = new JLabel("Max variation (meters): ");
		jlMaxVariation.setFont(GraphicDisplay.font);
		jsMaxVar = new JSpinner(new SpinnerNumberModel(0,0,100,1));
		jsMaxVar.setFont(GraphicDisplay.font);
		
		jlP_change = new JLabel("Change probability: ");
		jlP_change.setFont(GraphicDisplay.font);
		jslider_p_change = new JSlider(0, 100, 15);
		makeJSlider(jslider_p_change);
		jslider_p_change.setFont(GraphicDisplay.font);
		jlR_avg = new JLabel("Average rate range: ");
		jlR_avg.setFont(GraphicDisplay.font);
		jslider_r_avg = new JSlider(0,100,75);
		makeJSlider(jslider_r_avg);
		jslider_r_avg.setFont(GraphicDisplay.font);
		jl_inf_avg = new JLabel("Decreasing range probability under average: ");
		jl_inf_avg.setFont(GraphicDisplay.font);
		jslider_p_inf_avg = new JSlider(0,100,25);
		makeJSlider(jslider_p_inf_avg);
		jslider_p_inf_avg.setFont(GraphicDisplay.font);
		jl_sup_avg = new JLabel("Decreasing range probability over average: ");
		jl_sup_avg.setFont(GraphicDisplay.font);
		jslider_p_sup_avg = new JSlider(0,100,75);
		makeJSlider(jslider_p_sup_avg);
		jslider_p_sup_avg.setFont(GraphicDisplay.font);



		jbOk = new JButton("OK");
		jbOk.setFont(GraphicDisplay.font);
		jbOk.setEnabled(false);

		jbCancel = new JButton("Cancel");
		jbCancel.setFont(GraphicDisplay.font);


		jbCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jd.setVisible(false);
				jd.dispose();
			}
		});

		CaretListener cl = new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				if(!jtfFile.getText().equals("")) jbOk.setEnabled(true);
				else jbOk.setEnabled(false);
			}
		}
		;
		jtfFile.addCaretListener(cl);

		jbOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t2 = new Thread(){
					public void run(){
						mf.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						mf.getFrame().setEnabled(false);
						jd.dispose();

						String dfile = jtfFile.getText();
						String newFile = jtfNewFile.getText();
						//TODO CALL GENERATOR
						Time maxTime = new Time(jsMaxTimeH.getValue()+":"+jsMaxTimeM.getValue()+":"+jsMaxTimeS.getValue());
						Time intervalTime = new Time(jsIntervalH.getValue()+":"+jsIntervalM.getValue()+":"+jsIntervalS.getValue());
						int maxVariation = Integer.parseInt(jsMaxVar.getValue()+"");
						try {
							new LaserHeadRangeEventsGenerator(
									mf.getLocalHostName(),
									dfile,
									newFile,
									maxTime,
									intervalTime,
									maxVariation,
									jslider_p_change.getValue()/100.0,
									jslider_r_avg.getValue()/100.0,
									jslider_p_inf_avg.getValue()/100.0,
									jslider_p_sup_avg.getValue()/100.0,
									mf);
						} catch (SAXException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

						mf.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						mf.getFrame().setEnabled(true);
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

		jbChooseFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(MainFrame.baseDirectory);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("D2CTS Deployment file", "d2cts");
				fc.setFileFilter(filter);
				int returnVal = fc.showOpenDialog(mf.getFrame());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File configFile = fc.getSelectedFile();
					File base = new File ("");
					String lastOpen = base.toURI().relativize(configFile.toURI()).getPath();
					jtfFile.setText(lastOpen);
					if(!jtfFile.getText().equals("")) jbOk.setEnabled(true);
					else jbOk.setEnabled(false);
				} else {
					System.out.println("Open command cancelled by user.");
				}
			}
		});

		jbChooseNewFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(MainFrame.baseDirectory);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("D2CTS event file", "xml");
				fc.setFileFilter(filter);
				int returnVal = fc.showSaveDialog(mf.getFrame());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File configFile = fc.getSelectedFile();
					File base = new File ("");
					String lastOpen = base.toURI().relativize(configFile.toURI()).getPath();
					jtfNewFile.setText(lastOpen);
					if(!jtfFile.getText().equals("")) jbOk.setEnabled(true);
					else jbOk.setEnabled(false);
				} else {
					System.out.println("Open command cancelled by user.");
				}
			}
		});

		JPanel pConfig = new JPanel(new SpringLayout());
		pConfig.add(jlFile);
		pConfig.add(jtfFile);
		pConfig.add(jbChooseFile);

		pConfig.add(jlTargetFile);
		pConfig.add(jtfNewFile);
		pConfig.add(jbChooseNewFile);
		pConfig.setMaximumSize(new Dimension(800, 100));
		StoredSimulationChooser.makeCompactGrid(pConfig, 2, 3, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad

		JPanel pConfig2 = new JPanel(new SpringLayout());
		pConfig2.add(jlMaxTime);
		pConfig2.add(jsMaxTimeH);
		pConfig2.add(new JLabel(":"));
		pConfig2.add(jsMaxTimeM);
		pConfig2.add(new JLabel(":"));
		pConfig2.add(jsMaxTimeS);
		pConfig2.setMaximumSize(new Dimension(800, 50));

		StoredSimulationChooser.makeCompactGrid(pConfig2, 1, 6, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad
		
		JPanel pConfig3 = new JPanel(new SpringLayout());
		pConfig3.add(jlIntervalTime);
		pConfig3.add(jsIntervalH);
		pConfig3.add(new JLabel(":"));
		pConfig3.add(jsIntervalM);
		pConfig3.add(new JLabel(":"));
		pConfig3.add(jsIntervalS);
		pConfig3.setMaximumSize(new Dimension(800, 50));

		StoredSimulationChooser.makeCompactGrid(pConfig3, 1, 6, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad
		
		JPanel pConfig4 = new JPanel(new SpringLayout());
		pConfig4.add(jlMaxVariation);
		pConfig4.add(jsMaxVar);
		pConfig4.add(jlP_change);
		pConfig4.add(jslider_p_change);
		pConfig4.add(jlR_avg);
		pConfig4.add(jslider_r_avg);
		pConfig4.add(jl_inf_avg);
		pConfig4.add(jslider_p_inf_avg);
		pConfig4.add(jl_sup_avg);
		pConfig4.add(jslider_p_sup_avg);
		pConfig4.setMaximumSize(new Dimension(800, 250));
		StoredSimulationChooser.makeCompactGrid(pConfig4, 5, 2, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad
		
		JPanel pOkCancel = new JPanel(new GridLayout(1,5,0,5));
		pOkCancel.add(new JLabel());
		pOkCancel.add(jbCancel);
		pOkCancel.add(new JLabel());
		pOkCancel.add(jbOk);
		pOkCancel.add(new JLabel());

		JPanel pCenter = new JPanel(new SpringLayout());
		pCenter.add(pConfig);
		pCenter.add(pConfig2);
		pCenter.add(pConfig3);
		pCenter.add(pConfig4);
		
		StoredSimulationChooser.makeCompactGrid(pCenter, 4, 1, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //x

		jd.add(pCenter,BorderLayout.CENTER);
		jd.add(pOkCancel,BorderLayout.SOUTH);
		jd.setPreferredSize(new Dimension(600,400));
		jd.pack();
		JFrame parent = mainFrame.getFrame();
		jd.setLocation(parent.getLocation().x+(parent.getSize().width/2 - jd.getSize().width/2), parent.getLocation().y+(parent.getSize().height/2 - jd.getSize().height/2));
		jd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		jd.setVisible(true);
	}
	
	public void makeJSlider(JSlider js){
		js.setMajorTickSpacing(10);
		js.setMinorTickSpacing(1);
		js.setPaintTicks(true);
		js.setPaintLabels(true);
	}
}
