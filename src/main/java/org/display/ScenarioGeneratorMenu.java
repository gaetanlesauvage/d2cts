package org.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.com.dao.TerminalDAO;
import org.com.model.TerminalBean;
import org.util.generators.v2.ScenarioGenerator;


public class ScenarioGeneratorMenu {
	public static final Logger log = Logger.getLogger(ScenarioGeneratorMenu.class);

	private static final Dimension labelSize = new Dimension(100,30);
	private static final Dimension inputComponentSize = new Dimension(300,30);
	
	private JLabel jlScenarioName, jlTerminalName, jl20Feet, jl40Feet, jl45Feet, jlStraddleCarriers;
	private JTextField jtfScenarioName;
	private JSlider jsStraddleCarriers;
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
				jlTerminalName.setPreferredSize(labelSize);
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
				jcbTerminals.setPreferredSize(inputComponentSize);
				jcbTerminals.setFont(GraphicDisplay.font);

				//Scenario name
				jlScenarioName = new JLabel("Scenario name : ");
				jlScenarioName.setPreferredSize(labelSize);
				jlScenarioName.setFont(GraphicDisplay.fontBold);
				jtfScenarioName = new JTextField();
				jtfScenarioName.setPreferredSize(inputComponentSize);
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
				jl20Feet.setPreferredSize(labelSize);
				jl20Feet.setFont(GraphicDisplay.font);
				js20Feet = new JSpinner(new SpinnerNumberModel(0,0, 10000, 10));
				js20Feet.setPreferredSize(inputComponentSize);
				js20Feet.setFont(GraphicDisplay.font);
				jl40Feet = new JLabel("40 feet containers : ");
				jl40Feet.setPreferredSize(labelSize);
				jl40Feet.setFont(GraphicDisplay.font);
				js40Feet = new JSpinner(new SpinnerNumberModel(0,0, 10000, 10));
				js40Feet.setPreferredSize(inputComponentSize);
				js40Feet.setFont(GraphicDisplay.font);
				jl45Feet = new JLabel("45 feet containers : ");
				jl45Feet.setPreferredSize(labelSize);
				jl45Feet.setFont(GraphicDisplay.font);
				js45Feet = new JSpinner(new SpinnerNumberModel(0,0, 10000, 10));
				js45Feet.setPreferredSize(inputComponentSize);
				js45Feet.setFont(GraphicDisplay.font);
				jlStraddleCarriers = new JLabel("StraddleCarriers : ");
				jlStraddleCarriers.setPreferredSize(labelSize);
				jlStraddleCarriers.setFont(GraphicDisplay.font);
				jsStraddleCarriers = new JSlider(1, 10, 4);
				jsStraddleCarriers.setPreferredSize(inputComponentSize);
				jsStraddleCarriers.setFont(GraphicDisplay.font);
				jsStraddleCarriers.setPaintTicks(true);
				jsStraddleCarriers.setPaintLabels(true);
				jsStraddleCarriers.setMajorTickSpacing(1);

				jbOk = new JButton("OK");
				jbOk.setPreferredSize(labelSize);
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

						//FIXME
						ScenarioGenerator.getInstance().generate(jtfScenarioName.getText(), jcbTerminals.getItemAt(jcbTerminals.getSelectedIndex()), nb20, nb40, nb45, jsStraddleCarriers.getValue(), mainFrame);
						
						jd.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
				jbCancel.setPreferredSize(labelSize);
				jbCancel.setFont(GraphicDisplay.font);

				jbCancel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						jd.setVisible(false);
						jd.dispose();
					}
				});

				JPanel pConfig = new JPanel(new GridBagLayout());
				GridBagConstraints constraints = new GridBagConstraints();
				constraints.anchor = GridBagConstraints.CENTER;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				constraints.insets = new Insets(5, 5, 5, 5);
				
				constraints.gridx=0;
				constraints.gridy=0;
				pConfig.add(jlScenarioName,constraints);
				constraints.gridx++;
				pConfig.add(jtfScenarioName,constraints);
				constraints.gridx=0;
				constraints.gridy++;
				
				pConfig.add(jlTerminalName,constraints);
				constraints.gridx++;
				pConfig.add(jcbTerminals,constraints);
				constraints.gridx=0;
				constraints.gridy++;
				

				pConfig.add(jl20Feet,constraints);
				constraints.gridx++;
				pConfig.add(js20Feet,constraints);
				constraints.gridx=0;
				constraints.gridy++;
				pConfig.add(jl40Feet,constraints);
				constraints.gridx++;
				pConfig.add(js40Feet,constraints);
				constraints.gridx=0;
				constraints.gridy++;
				pConfig.add(jl45Feet,constraints);
				constraints.gridx++;
				pConfig.add(js45Feet,constraints);
				constraints.gridx=0;
				constraints.gridy++;
				pConfig.add(jlStraddleCarriers,constraints);
				constraints.gridx++;
				pConfig.add(jsStraddleCarriers,constraints);
				constraints.gridx=0;
				constraints.gridy++;

				pConfig.setBorder(BorderFactory.createRaisedBevelBorder());

				pConfig.add(jbCancel,constraints);
				constraints.gridx++;
				
				pConfig.add(jbOk,constraints);
				for(Component c : pConfig.getComponents()){
					c.addKeyListener(key);
				}
				jd.add(pConfig,BorderLayout.CENTER);
				jd.setPreferredSize(new Dimension(550,250));
				jd.pack();
				JFrame parent = mainFrame.getFrame();
				jd.setLocation(parent.getLocation().x+(parent.getSize().width/2 - jd.getSize().width/2), parent.getLocation().y+(parent.getSize().height/2 - jd.getSize().height/2));
				jd.addKeyListener(key);
				jd.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						jd.dispose();
					}
				});
				jd.setVisible(true);
			}
		});
	}

	KeyAdapter key = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e){
			if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
				jd.setVisible(false);
				jd.dispose();
			}
		}
	};
	
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
