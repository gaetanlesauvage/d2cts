package org.display;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.util.dbLoading.XMLToDataBase;

public class ImportXMLGUI extends Thread {
	public ImportXMLGUI(){
		start();
	}
	
	public void run(){
		JFileChooser fc = new JFileChooser(MainFrame.baseDirectory);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("D2CTS configuration files (terminal/scenario)", "terminal", "scenario");
		fc.setFileFilter(filter);
		fc.setMultiSelectionEnabled(true);
		
		fc.showOpenDialog(MainFrame.getInstance().getFrame());
		
		File[] xmlFiles = fc.getSelectedFiles();
		String stringFilesOverall = "";
		if(xmlFiles != null && xmlFiles.length > 0){
			try {
				SwingUtilities.invokeAndWait(new Runnable(){
					public void run(){
						MainFrame.getInstance().setWaitMode(true);
					}
				});
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			String[] xmlPathes = new String[xmlFiles.length];
			String stringFiles = "";
			int i=0;
			for(File f : xmlFiles){
				xmlPathes[i++] = f.getAbsolutePath();
				stringFiles += " "+f.getName();
				stringFilesOverall += " "+f.getName();
			}
			try{
				XMLToDataBase.parse(xmlPathes);
				JOptionPane.showMessageDialog(MainFrame.getInstance().getFrame(), "Files"+stringFilesOverall+" correctly imported.", "Import XML", JOptionPane.INFORMATION_MESSAGE);
			} catch(Exception e){
				JOptionPane.showMessageDialog(MainFrame.getInstance().getFrame(), "Error while importing files"+stringFiles+". See log file!", "Import XML", JOptionPane.ERROR_MESSAGE);
			}
			
			try {
				SwingUtilities.invokeAndWait(new Runnable(){
					public void run(){
						MainFrame.getInstance().setWaitMode(false);
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
