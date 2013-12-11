package org.display;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.SQLException;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.com.dao.TerminalDAO;
import org.com.model.TerminalBean;
import org.util.generators.v2.ScenarioGenerator;


public class ScenarioGeneratorMenu {
	public static final Logger log = Logger.getLogger(ScenarioGeneratorMenu.class);

	private JLabel jlScenarioName, jlTerminalName, jl20Feet, jl40Feet, jl45Feet;
	private JTextField jtfScenarioName;
	private JButton jbOk, jbCancel /*, jbChooseFile,jbChooseNewFile*/;
	//private JTextField jtfFile,jtfNewFile;

	private JComboBox<TerminalBean> jcbTerminals;

	private JSpinner js20Feet, js40Feet, js45Feet;
	private JDialog jd;

	public ScenarioGeneratorMenu (final MainFrame mainFrame){

		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				jd= new JDialog(mainFrame.getFrame(), "Container File Generator");

				jd.setModal(true);
				jd.setFont(GraphicDisplay.font);
				jd.getRootPane().setFont(GraphicDisplay.font);

				//				mainFrame.setEnableMenus(false);

				//Terminal file
				jlTerminalName = new JLabel("Terminal : ");
				jlTerminalName.setFont(GraphicDisplay.fontBold);
				try {
					TerminalDAO.getInstance().load();
				} catch (SQLException e1) {
					log.error(e1.getMessage(), e1);
				}

				TerminalBean[] beans = new TerminalBean[TerminalDAO.getInstance().size()];
				//HERE FOR LOOP
				Iterator<TerminalBean> iterator = TerminalDAO.getInstance().iterator();
				for(int i=0; iterator.hasNext(); i++){
					beans[i] = iterator.next();
				}
				//TODO => Add into arry dao values then into combobox
				jcbTerminals = new JComboBox<TerminalBean>(beans);
				jcbTerminals.setFont(GraphicDisplay.font);

				//Scenario name
				jlScenarioName = new JLabel("Scenario name : ");
				jlScenarioName.setFont(GraphicDisplay.fontBold);
				jtfScenarioName = new JTextField();
				jtfScenarioName.setFont(GraphicDisplay.font);
				//				jtfFile = new JTextField();
				//				jtfFile.setFont(GraphicDisplay.font);
				//				jbChooseFile = new JButton("...");
				//				jbChooseFile.setMaximumSize(new Dimension(30, 30));
				//				jbChooseFile.setFont(GraphicDisplay.fontBold);


				//DESTINATION FILE
				//				jlTargetFile = new JLabel("Destination file : ");
				//				jlTargetFile.setFont(GraphicDisplay.font);
				//				jtfNewFile = new JTextField();
				//				jtfNewFile.setFont(GraphicDisplay.font);
				//				jbChooseNewFile = new JButton("...");
				//				jbChooseNewFile.setFont(GraphicDisplay.fontBold);
				//				jbChooseNewFile.setMaximumSize(new Dimension(30, 30));


				//NB CONTAINERS : 
				jl20Feet = new JLabel("20 feet containers : ");
				jl20Feet.setFont(GraphicDisplay.font);
				js20Feet = new JSpinner(new SpinnerNumberModel(0,0, 10000, 10));
				js20Feet.setFont(GraphicDisplay.font);
				jl40Feet = new JLabel("40 feet containers : ");
				jl40Feet.setFont(GraphicDisplay.font);
				js40Feet = new JSpinner(new SpinnerNumberModel(0,0, 10000, 10));
				js40Feet.setFont(GraphicDisplay.font);
				jl45Feet = new JLabel("45 feet containers : ");
				jl45Feet.setFont(GraphicDisplay.font);
				js45Feet = new JSpinner(new SpinnerNumberModel(0,0, 10000, 10));
				js45Feet.setFont(GraphicDisplay.font);



				//				jbChooseFile.addActionListener(new ActionListener() {
				//					@Override
				//					public void actionPerformed(ActionEvent e) {
				//						JFileChooser fc = new JFileChooser(MainFrame.baseDirectory);
				//						FileNameExtensionFilter filter = new FileNameExtensionFilter("D2CTS Container Generator file", "cgen");
				//						fc.setFileFilter(filter);
				//						int returnVal = fc.showOpenDialog(mainFrame.getFrame());
				//						if (returnVal == JFileChooser.APPROVE_OPTION) {
				//							File configFile = fc.getSelectedFile();
				//							File base = new File ("");
				//							String lastOpen = base.toURI().relativize(configFile.toURI()).getPath();
				//							jtfFile.setText(lastOpen);
				//							if(!jtfNewFile.getText().equals("")) jbOk.setEnabled(true);
				//							else jbOk.setEnabled(false);
				//						} else {
				//							System.out.println("Open command cancelled by user.");
				//						}
				//					}
				//				});

				//				jbChooseNewFile.addActionListener(new ActionListener() {
				//					@Override
				//					public void actionPerformed(ActionEvent e) {
				//						JFileChooser fc = new JFileChooser(MainFrame.baseDirectory);
				//						FileNameExtensionFilter filter = new FileNameExtensionFilter("D2CTS containers files", "cont");
				//						fc.setFileFilter(filter);
				//						int returnVal = fc.showSaveDialog(mainFrame.getFrame());
				//						if (returnVal == JFileChooser.APPROVE_OPTION) {
				//							File configFile = fc.getSelectedFile();
				//							File base = new File ("");
				//							String lastOpen = base.toURI().relativize(configFile.toURI()).getPath();
				//							jtfNewFile.setText(lastOpen);
				//							if(!jtfFile.getText().equals("")) jbOk.setEnabled(true);
				//							else jbOk.setEnabled(false);
				//						} else {
				//							System.out.println("Open command cancelled by user.");
				//						}
				//					}
				//				});

				//				CaretListener cl = new CaretListener() {
				//					@Override
				//					public void caretUpdate(CaretEvent e) {
				//						if(!jtfFile.getText().equals("")&&!jtfNewFile.getText().equals("") ) jbOk.setEnabled(true);
				//						else jbOk.setEnabled(false);
				//					}
				//				}
				//				;
				//				jtfFile.addCaretListener(cl);
				//				jtfNewFile.addCaretListener(cl);


				jbOk = new JButton("OK");
				jbOk.setFont(GraphicDisplay.font);
				
				jbOk.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mainFrame.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						mainFrame.getFrame().setEnabled(false);
						
						jd.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						jd.setEnabled(false);

						setEnableEverything(false);
						if(jtfScenarioName.getText()!=null && jtfScenarioName.getText().length()>0 && jcbTerminals.getSelectedIndex()>=0 && 
								(!js20Feet.getValue().equals("0") || !js40Feet.getValue().equals("0") || !js45Feet.getValue().equals("0"))){
							
						
								
						final int nb20 = Integer.parseInt(js20Feet.getValue()+"");
						final int nb40 = Integer.parseInt(js40Feet.getValue()+"");
						final int nb45 = Integer.parseInt(js45Feet.getValue()+"");
						//jd.setVisible(false);

						//FIXME
						ScenarioGenerator.getInstance().generate(jtfScenarioName.getText(), jcbTerminals.getItemAt(jcbTerminals.getSelectedIndex()), nb20, nb40, nb45, mainFrame);


						
						jd.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						
						//mainFrame.setEnableMenus(true);
						mainFrame.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						mainFrame.getFrame().setEnabled(true);
						jd.setVisible(false);
						jd.dispose();
						} else {
							JOptionPane.showMessageDialog(mainFrame.getFrame(), "Something is missing! Please, check data.");
							jd.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							setEnableEverything(true);
							jd.setEnabled(true);
							
							
						}
						mainFrame.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						mainFrame.getFrame().setEnabled(true);
						
					}
				});


				jbCancel = new JButton("Cancel");
				jbCancel.setFont(GraphicDisplay.font);

				jbCancel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						jd.setVisible(false);
						//						mainFrame.setEnableMenus(true);
						jd.dispose();
					}
				});

				JPanel pConfig = new JPanel(new SpringLayout());
				pConfig.add(jlScenarioName);
				pConfig.add(jtfScenarioName);

				pConfig.add(jlTerminalName);
				pConfig.add(jcbTerminals);
				pConfig.setMaximumSize(new Dimension(800, 100));
				StoredSimulationChooser.makeCompactGrid(pConfig, 2, 2, //rows, cols
						6, 6,        //initX, initY
						6, 6);       //xPad, yPad

				JPanel pConfig2 = new JPanel(new SpringLayout());
				pConfig2.add(jl20Feet);
				pConfig2.add(js20Feet);
				pConfig2.add(jl40Feet);
				pConfig2.add(js40Feet);
				pConfig2.add(jl45Feet);
				pConfig2.add(js45Feet);

				pConfig2.setMaximumSize(new Dimension(800, 50));

				StoredSimulationChooser.makeCompactGrid(pConfig2, 1, 6, //rows, cols
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

				StoredSimulationChooser.makeCompactGrid(pCenter, 2, 1, //rows, cols
						6, 6,        //initX, initY
						6, 6);       //x

				jd.add(pCenter,BorderLayout.CENTER);
				jd.add(pOkCancel,BorderLayout.SOUTH);
				jd.setPreferredSize(new Dimension(600,175));
				jd.pack();
				JFrame parent = mainFrame.getFrame();
				jd.setLocation(parent.getLocation().x+(parent.getSize().width/2 - jd.getSize().width/2), parent.getLocation().y+(parent.getSize().height/2 - jd.getSize().height/2));
				jd.addWindowListener(new WindowListener() {

					@Override
					public void windowOpened(WindowEvent e) {
					}

					@Override
					public void windowIconified(WindowEvent e) {
					}

					@Override
					public void windowDeiconified(WindowEvent e) {
					}

					@Override
					public void windowDeactivated(WindowEvent e) {
					}

					@Override
					public void windowClosing(WindowEvent e) {
						//						mainFrame.setEnableMenus(true);
						jd.dispose();
					}

					@Override
					public void windowClosed(WindowEvent e) {
					}

					@Override
					public void windowActivated(WindowEvent e) {
					}
				});
				jd.setVisible(true);
			}
		});
	}

	private void setEnableEverything(boolean state){
		this.jbCancel.setEnabled(state);
		this.jlTerminalName.setEnabled(state);
		this.jbOk.setEnabled(state);
		this.jl20Feet.setEnabled(state);
		this.jl40Feet.setEnabled(state);
		this.jl45Feet.setEnabled(state);
		this.jlScenarioName.setEnabled(state);
		this.jtfScenarioName.setEnabled(state);
		this.js20Feet.setEnabled(state);
		this.js40Feet.setEnabled(state);
		this.js45Feet.setEnabled(state);
	}
}
