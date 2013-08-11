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
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.exceptions.ContainerDimensionException;
import org.exceptions.EmptyLevelException;
import org.exceptions.NoPathFoundException;
import org.util.generators.MissionsFileGenerator;
import org.xml.sax.SAXException;



public class MissionFileGeneratorMenu {
	private MainFrame mf;
	
	public MissionFileGeneratorMenu (MainFrame mainFrame){
		this.mf = mainFrame;
		
		final JDialog jd = new JDialog(null/*mf.getFrame()*/, "Mission file generator", ModalityType.APPLICATION_MODAL);
		
		final JLabel jlDeployFile = new JLabel("Configuration file : ");
		jlDeployFile.setFont(GraphicDisplay.font);
		final JTextField jtfFile = new JTextField();
		jtfFile.setFont(GraphicDisplay.font);
		JButton jbChooseFile = new JButton("...");
		jbChooseFile.setMaximumSize(new Dimension(30, 30));
		jbChooseFile.setFont(GraphicDisplay.fontBold);

		
		final JButton jbOk = new JButton("OK");
		jbOk.setFont(GraphicDisplay.font);
		jbOk.setEnabled(false);
		
		final JButton jbCancel = new JButton("Cancel");
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
						//mf.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						//mf.getFrame().setEnabled(false);
						//jd.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						//jd.setEnabled(false);
						
						
						mf.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						mf.getFrame().setEnabled(false);
						jd.dispose();
						
						String file = jtfFile.getText();
						try{
							new MissionsFileGenerator(mf.getLocalHostName(), file, mf);
						} catch (SAXException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (NoPathFoundException e) {
							e.printStackTrace();
						} catch (ContainerDimensionException e) {
							e.printStackTrace();
						} catch (EmptyLevelException e) {
							e.printStackTrace();
						}


						mf.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						//jd.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						mf.getFrame().setEnabled(true);
						//jd.dispose();
						
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
				FileNameExtensionFilter filter = new FileNameExtensionFilter("D2CTS Mission Generator file", "mgen");
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

		JPanel pConfig = new JPanel(new SpringLayout());
		pConfig.add(jlDeployFile);
		pConfig.add(jtfFile);
		pConfig.add(jbChooseFile);

		pConfig.setMaximumSize(new Dimension(800, 75));
		StoredSimulationChooser.makeCompactGrid(pConfig, 1, 3, //rows, cols
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
		
		jd.setPreferredSize(new Dimension(600,100));
		jd.pack();
		JFrame parent = mainFrame.getFrame();
		jd.setLocation(parent.getLocation().x+(parent.getSize().width/2 - jd.getSize().width/2), parent.getLocation().y+(parent.getSize().height/2 - jd.getSize().height/2));

		jd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		jd.setVisible(true);
	}
}
