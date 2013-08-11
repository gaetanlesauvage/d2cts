package org.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//TODO wait refactoring relative to new database implementation model
public class StoredSimulationChooser {
	//private MainFrame mainFrame;
	private JTextField jtfServer, jtfUser,jtfDBName;
	private JPasswordField jpfPassword;
	private JList<String> jlSimulations;
	private JButton jbRetrieve, jbOk, jbCancel;
	//FIXME
	//private DatabaseManager dbm;
	private JDialog jd;
	
	public StoredSimulationChooser (final MainFrame mainFrame){
		//this.mainFrame = mainFrame;

		//FIXME
		//this.dbm = new DatabaseManager();
		
		jd = new JDialog(mainFrame.getFrame(), "Choose a simulation", true);

		JLabel jlServer = new JLabel("Server : ");
		jtfServer = new JTextField("obliviongate.univ-lehavre.fr");
		
		//JLabel jlPort = new JLabel("Port : ");
		//jtfPort = new JTextField("3306");
		
		JLabel jlDBName = new JLabel("Database name : ");
		jtfDBName = new JTextField("projet_eads");
		
		JLabel jlUser = new JLabel("User : ");
		jtfUser = new JTextField("d2cts");

		JLabel jlPassword = new JLabel("Password : ");
		jpfPassword = new JPasswordField(15);
		
		jlSimulations = new JList<String>();
		jlSimulations.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlSimulations.setBorder(BorderFactory.createLineBorder(Color.black));
		jlSimulations.setMinimumSize(new Dimension(50,150));
		jlSimulations.setMaximumSize(new Dimension(200,200));
		jlSimulations.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getFirstIndex()<0) jbOk.setEnabled(false);
				else 	if(!jbOk.isEnabled()) jbOk.setEnabled(true);
			}
		});
		
		jbRetrieve = new JButton("Retrieve list");
		jbRetrieve.setEnabled(false);

		jbOk = new JButton("Ok");
		jbOk.setEnabled(false);
		
		jbCancel = new JButton("Cancel");

		jpfPassword.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				if(!jbRetrieve.isEnabled()){
					if(jpfPassword.getPassword().length>0&&jtfServer.getText().length()>0&&jtfDBName.getText().length()>0&&jtfUser.getText().length()>0)	
									jbRetrieve.setEnabled(true);
				}
				else if(jpfPassword.getPassword().length==0||jtfServer.getText().length()==0||jtfDBName.getText().length()==0||jtfUser.getText().length()==0){
					jbRetrieve.setEnabled(false);
				}	
			}
		});			
		
		jbRetrieve.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//FIXME
				/*dbm.setServer(jtfServer.getText());
			
				dbm.setDbName(jtfDBName.getText());
				dbm.setUserLogin(jtfUser.getText());
				dbm.setPassword(new String(jpfPassword.getPassword()));
				int connectionOk = dbm.checkConnection();
				String msg = "";
				if(connectionOk == DatabaseManager.SERVER_ERROR){
					msg = "Server error. Please check the server url.";
				}
				else if(connectionOk == DatabaseManager.USER_OR_PASSWORD_OR_DBNAME_ERROR){
					msg = "Username or password or database name error! Please check the values.";
				}
				if(msg.equals("")){
					List<String> simIDS = dbm.getSimIDS();
					DefaultListModel<String> lm = new DefaultListModel<String>();
					for(String s : simIDS){
						lm.add(lm.getSize(), s);
					}
					
					jlSimulations.setModel(lm);
					
				}
				else{
					JOptionPane.showMessageDialog(StoredSimulationChooser.this.jd, msg, "Error", JOptionPane.ERROR_MESSAGE);
				}
				System.out.println("connection ok = "+connectionOk);*/
			}
		});
		
		jbOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//FIXME
				/*String f = dbm.getDistributionFile(jlSimulations.getSelectedValue());
				System.out.println("F="+f);
				StoredSimulationChooser.this.mainFrame.openReplaySimulation(f, ""+jlSimulations.getSelectedValue());
				jd.setVisible(false);
				jd.dispose();*/
			}
		});
		jbCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jd.setVisible(false);
				jd.dispose();
				mainFrame.enableMenus();
			}
		});
		
		JPanel pConfig = new JPanel(new SpringLayout());

		pConfig.add(jlServer);
		pConfig.add(jtfServer);
		pConfig.add(jlDBName);
		pConfig.add(jtfDBName);
		pConfig.add(jlUser);
		pConfig.add(jtfUser);
		pConfig.add(jlPassword);
		pConfig.add(jpfPassword);
		makeCompactGrid(pConfig, 4, 2, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad

		JPanel pList = new JPanel(new BorderLayout());
		pList.add(jbRetrieve, BorderLayout.NORTH);
		pList.add(new JScrollPane(jlSimulations),BorderLayout.CENTER);

		JPanel pOkCancel = new JPanel(new GridLayout(1,2));
		pOkCancel.add(jbCancel);
		pOkCancel.add(jbOk);

		jd.add(pConfig,BorderLayout.NORTH);
		jd.add(pList,BorderLayout.CENTER);
		jd.add(pOkCancel, BorderLayout.SOUTH);

		jd.setPreferredSize(new Dimension(400, 400));
		jd.pack();

		JFrame parent = mainFrame.getFrame();
		jd.setLocation(parent.getLocation().x+(parent.getSize().width/2 - jd.getSize().width/2), parent.getLocation().y+(parent.getSize().height/2 - jd.getSize().height/2));

		jd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		jd.setVisible(true);
	}
	public static void makeCompactGrid(Container parent,
			int rows, int cols,
			int initialX, int initialY,
			int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout)parent.getLayout();
		} catch (ClassCastException exc) {
			System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
			return;
		}

		//Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width,
						getConstraintsForCell(r, c, parent, cols).
						getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints =
					getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		//Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height,
						getConstraintsForCell(r, c, parent, cols).
						getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints =
					getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		//Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}
	/* Used by makeCompactGrid. */
	private static SpringLayout.Constraints getConstraintsForCell(
			int row, int col,
			Container parent,
			int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component c = parent.getComponent(row * cols + col);
		return layout.getConstraints(c);
	}
	
	public static JPanel createJPanelLabelTextField(JComponent left, JComponent center){
		JPanel p = new JPanel();
		SpringLayout layout = new SpringLayout();
		p.setLayout(layout);

		//Add the components.
		p.add(left);
		p.add(center);

		//Adjust constraints for the label so it's at (5,5).
		layout.putConstraint(SpringLayout.WEST, left,
				5,
				SpringLayout.WEST, p);
		layout.putConstraint(SpringLayout.NORTH, left,
				5,
				SpringLayout.NORTH, p);

		//Adjust constraints for the text field so it's at
		//(<label's right edge> + 5, 5).
		layout.putConstraint(SpringLayout.WEST, center,
				5,
				SpringLayout.EAST, left);
		layout.putConstraint(SpringLayout.NORTH, center,
				5,
				SpringLayout.NORTH, p);

		//Adjust constraints for the content pane: Its right
		//edge should be 5 pixels beyond the text field's right
		//edge, and its bottom edge should be 5 pixels beyond
		//the bottom edge of the tallest component (which we'll
		//assume is textField).
		layout.putConstraint(SpringLayout.EAST, p,
				5,
				SpringLayout.EAST, center);
		layout.putConstraint(SpringLayout.SOUTH, p,
				5,
				SpringLayout.SOUTH, center);


		p.add(left, BorderLayout.WEST);
		p.add(center, BorderLayout.CENTER);
		return p;
	}
	
}
