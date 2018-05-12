package org.display;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.time.TimeScheduler;

public class StepSizeChooser {

	public StepSizeChooser(MainFrame mainFrame) {
		final JDialog jd = new JDialog(mainFrame.getFrame());

		JLabel jl = new JLabel("Seconds per step: ");
		jl.setFont(GraphicDisplay.font);
		final TimeScheduler ts = TimeScheduler.getInstance();
		final JSpinner jsSecByStep = new JSpinner(new SpinnerNumberModel(
				ts.getSecondsPerStep(), 1, 10, 1));
		jsSecByStep.setFont(GraphicDisplay.font);
		jd.add(jl, BorderLayout.CENTER);
		jd.add(jsSecByStep, BorderLayout.EAST);

		JButton jbOk = new JButton("Ok");
		jbOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ts.setSecondsPerStep(((Double) jsSecByStep.getValue())
						.floatValue());
				
				jd.dispose();
			}
		});
		JButton jbCancel = new JButton("Cancel");
		jbCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jd.dispose();
			}
		});
		JPanel pSouth = new JPanel(new GridLayout(1, 2));
		pSouth.add(jbCancel);
		pSouth.add(jbOk);
		jd.add(pSouth, BorderLayout.SOUTH);
		jd.setSize(150, 80);
		JFrame parent = mainFrame.getFrame();
		jd.setLocation(
				parent.getLocation().x
				+ (parent.getSize().width / 2 - jd.getSize().width / 2),
				parent.getLocation().y
				+ (parent.getSize().height / 2 - jd.getSize().height / 2));
		// Point p = new
		// Point(Toolkit.getDefaultToolkit().getScreenSize().width/2
		// -jd.getSize().width/2,
		// Toolkit.getDefaultToolkit().getScreenSize().height/2 -
		// jd.getSize().height/2);
		// jd.setLocation(p);
		jd.setModal(true);
		jd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		jd.setVisible(true);
	}
}
