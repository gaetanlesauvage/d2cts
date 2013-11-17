package org.scheduling.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.display.GraphicDisplay;
import org.display.panes.TableCellRendererCentered;
import org.display.panes.TableCellRendererWithIcon;
import org.display.panes.TableCellWithIcon;
import org.display.panes.ThreadSafeTableModel;
import org.scheduling.MissionScheduler;
import org.scheduling.UpdateInfo;
import org.time.Time;
import org.vehicles.StraddleCarrier;




public class IndicatorPane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3528006127310869714L;
	private JTable straddleCarriersTable, overallTable;
	private ThreadSafeTableModel straddleCarrierModel, overallModel;

	private ConcurrentHashMap<String, UpdateInfo> distances;
	private ConcurrentHashMap<String, Time> overspentTimes;
	private ConcurrentHashMap<String, Time> waitTimes;
	private ConcurrentHashMap<String, Integer> completedMissions;
	private ConcurrentHashMap<String, Double> fitnessScores;

	private int overrunTW;

	private ConcurrentHashMap<String, JLabel[]> datas;
	private JLabel[] overall;

	private Lock lock;

	private static final DecimalFormat df = new DecimalFormat("#.###");

	public IndicatorPane () {
		super(new BorderLayout());
		TableCellRenderer alignCenter = new TableCellRendererCentered();
		lock = new ReentrantLock();

		//OVERALL DATA
		overall = new JLabel[IndicatorPaneOverallColumns.values().length];
		overallModel = new ThreadSafeTableModel(IndicatorPaneOverallColumns.values());
		overallTable = new JTable(overallModel);


		overall[IndicatorPaneOverallColumns.DISTANCE.getIndex()] = new JLabel(df.format(0.0));
		overall[IndicatorPaneOverallColumns.OVERSPENT_TIME.getIndex()] = new JLabel(new Time(0).toString());
		overall[IndicatorPaneOverallColumns.WAIT_TIME.getIndex()] = new JLabel(new Time(0).toString());
		overall[IndicatorPaneOverallColumns.TW_OVERRUN.getIndex()] = new JLabel("0");
		overall[IndicatorPaneOverallColumns.MISSIONS_DONE.getIndex()] = new JLabel("0");
		overall[IndicatorPaneOverallColumns.SCORE.getIndex()] = new JLabel("0");
		for(int i=0; i<overall.length; i++) overall[i].setFont(GraphicDisplay.fontBold);

		overallModel.addRow(overall);


		overallTable.setDefaultRenderer(IndicatorPaneOverallColumns.class,alignCenter);

		overallTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		overallTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		overallTable.setFont(GraphicDisplay.fontBold);
		overallTable.getTableHeader().setFont(GraphicDisplay.fontBold);


		//STRADDLE CARRIER DATA
		IndicatorPaneColumn[] pcols = new IndicatorPaneColumn[IndicatorPaneColumns.values().length+IndicatorPaneColumnsWithIcon.values().length];
		for(IndicatorPaneColumn pc : IndicatorPaneColumns.values()) pcols[pc.getIndex()] = pc;
		for(IndicatorPaneColumn pc : IndicatorPaneColumnsWithIcon.values()) pcols[pc.getIndex()] = pc;

		straddleCarrierModel = new ThreadSafeTableModel(pcols);
		straddleCarriersTable = new JTable(straddleCarrierModel);
		datas = new ConcurrentHashMap<String, JLabel[]>();
		distances = new ConcurrentHashMap<String, UpdateInfo>();
		overspentTimes = new ConcurrentHashMap<String, Time>();
		waitTimes = new ConcurrentHashMap<String, Time>();
		completedMissions = new ConcurrentHashMap<String, Integer>();
		fitnessScores = new ConcurrentHashMap<String, Double>();

		overrunTW = 0;
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(straddleCarriersTable.getModel());
		sorter.setComparator(1, new Comparator<JLabel>() {
			@Override
			public int compare(JLabel jl1, JLabel jl2) {

				double d1 = Double.parseDouble(jl1.getText().replace(',', '.'));
				double d2 = Double.parseDouble(jl2.getText().replace(',', '.'));
				if(d1 < d2) return -1;
				else if(d1 > d2) return 1;
				else return 0;
			}
		});


		TableCellRendererWithIcon idRenderer = new TableCellRendererWithIcon();
		straddleCarriersTable.setDefaultRenderer( IndicatorPaneColumns.class, alignCenter );
		straddleCarriersTable.setDefaultRenderer( IndicatorPaneColumnsWithIcon.class, idRenderer);

		sorter.setSortsOnUpdates(true);
		straddleCarriersTable.setRowSorter(sorter);

		straddleCarriersTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		straddleCarriersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		straddleCarriersTable.setFillsViewportHeight(true);
		straddleCarriersTable.getTableHeader().setFont(GraphicDisplay.fontBold);
		straddleCarriersTable.setFont(GraphicDisplay.font);

		JScrollPane jsp = new JScrollPane(straddleCarriersTable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(jsp,BorderLayout.CENTER);
		JPanel pOverall = new JPanel(new BorderLayout());
		JLabel jlOverall = new JLabel("OVERALL",JLabel.CENTER);
		jlOverall.setFont(GraphicDisplay.fontBold);
		jlOverall.setBorder(BorderFactory.createLineBorder(Color.white, 2));
		pOverall.add(jlOverall,BorderLayout.PAGE_START);
		JPanel pOverallCenter = new JPanel(new BorderLayout());
		pOverallCenter.add(overallTable.getTableHeader(),BorderLayout.NORTH);
		pOverallCenter.add(overallTable,BorderLayout.CENTER);
		pOverall.add(pOverallCenter,BorderLayout.CENTER);
		add(pOverall,BorderLayout.SOUTH);
	}

	public void addResource(StraddleCarrier rsc) {
		String rscID = rsc.getId();
		TableCellWithIcon jlID = new TableCellWithIcon(rsc.getId(),rsc.getIcon());
		distances.put(rscID, new UpdateInfo(0, 0));
		Time t = new Time(0);

		overspentTimes.put(rscID, t);
		waitTimes.put(rscID, t);
		completedMissions.put(rscID,0);
		fitnessScores.put(rscID, 0.0);

		JLabel jlDistance = new JLabel("0");
		JLabel jlOverspentTime = new JLabel(t.toString());
		JLabel jlMissionsDone = new JLabel("0");
		JLabel jlOverrun = new JLabel("0");
		JLabel jlWaitTime = new JLabel(t.toString());
		JLabel jlScore = new JLabel("0");
		JLabel[] row = {jlID, jlDistance, jlOverspentTime, jlOverrun, jlMissionsDone, jlWaitTime, jlScore};
		for(int i=0; i<row.length; i++) row[i].setFont(GraphicDisplay.font);
		straddleCarrierModel.addRow(row);
		datas.put(rsc.getId(), row);
	}

	private int getIndex(String resourceID){
		int index = -1;
		for(int i=0; i< straddleCarriersTable.getRowCount()&&index==-1 ; i++){
			JLabel jl = (JLabel)straddleCarrierModel.getValueAt(i, IndicatorPaneColumnsWithIcon.ID.getIndex());
			if(jl.getText().equals(resourceID)){
				index = i;
			}
		}				
		return index;
	}

	private void computeOverall(){
		int iOverall = 0;
		double distanceOverall = 0;
		Time timeOverall = new Time(0);
		Time waitTimeOverall = new Time(0);
		double scoreOverall = 0.0;

		int missionsCount = 0;
		for(UpdateInfo ui : distances.values()){
			distanceOverall+=ui.distance;
		}
		for(Time t : overspentTimes.values()){
			timeOverall = new Time(timeOverall, t);
		}
		for(Time t : waitTimes.values()){
			waitTimeOverall = new Time(waitTimeOverall, t);
		}
		for(Integer i : completedMissions.values()){
			missionsCount+=i;
		}
		for(double score : fitnessScores.values()){
			scoreOverall += score;
		}

		overall[IndicatorPaneOverallColumns.DISTANCE.getIndex()].setText(df.format(distanceOverall));
		overall[IndicatorPaneOverallColumns.OVERSPENT_TIME.getIndex()].setText(timeOverall.toString());
		overall[IndicatorPaneOverallColumns.TW_OVERRUN.getIndex()].setText(overrunTW+"");
		overall[IndicatorPaneOverallColumns.MISSIONS_DONE.getIndex()].setText(missionsCount+"");
		overall[IndicatorPaneOverallColumns.WAIT_TIME.getIndex()].setText(waitTimeOverall.toString());
		overall[IndicatorPaneOverallColumns.SCORE.getIndex()].setText(df.format(scoreOverall));
		overallModel.setValueAt(overall[IndicatorPaneOverallColumns.DISTANCE.getIndex()],iOverall, IndicatorPaneOverallColumns.DISTANCE.getIndex());
		overallModel.setValueAt(overall[IndicatorPaneOverallColumns.OVERSPENT_TIME.getIndex()],iOverall, IndicatorPaneOverallColumns.OVERSPENT_TIME.getIndex());
		overallModel.setValueAt(overall[IndicatorPaneOverallColumns.WAIT_TIME.getIndex()],iOverall, IndicatorPaneOverallColumns.WAIT_TIME.getIndex());
		overallModel.setValueAt(overall[IndicatorPaneOverallColumns.MISSIONS_DONE.getIndex()],iOverall, IndicatorPaneOverallColumns.MISSIONS_DONE.getIndex());
		overallModel.setValueAt(overall[IndicatorPaneOverallColumns.SCORE.getIndex()], iOverall, IndicatorPaneOverallColumns.SCORE.getIndex());
	}

	public void addDistance(final String resourceID, double distance, double travelTime) {
		//		System.err.println("ADD DISTANCE "+resourceID+" "+distance+" "+new Time(travelTime+"s"));
		UpdateInfo oldInfo = distances.get(resourceID);
		if(oldInfo != null){
			Double oldDistance = oldInfo.distance;
			Double oldTravelTime = oldInfo.travelTime;

			Double newDistance = new Double(distance+oldDistance);
			Double newTravelTime = new Double(travelTime+oldTravelTime); 
			distances.put(resourceID, new UpdateInfo(newDistance, newTravelTime));

			double oldScore = fitnessScores.get(resourceID);
			oldScore += travelTime*MissionScheduler.getEvalParameters().getTravelTimeCoeff();
			fitnessScores.put(resourceID, oldScore);

			JLabel[] jls = datas.get(resourceID);
			JLabel jl = jls[IndicatorPaneColumns.DISTANCE.getIndex()];
			JLabel jlScore = jls[IndicatorPaneColumns.SCORE.getIndex()];
			jl.setText(df.format(distances.get(resourceID).distance));
			jlScore.setText(df.format(fitnessScores.get(resourceID)));
			jls[IndicatorPaneColumns.DISTANCE.getIndex()] = jl;
			jls[IndicatorPaneColumns.SCORE.getIndex()] = jlScore;
			straddleCarrierModel.setValueAt(jl, getIndex(resourceID), IndicatorPaneColumns.DISTANCE.getIndex());
			straddleCarrierModel.setValueAt(jlScore, getIndex(resourceID), IndicatorPaneColumns.SCORE.getIndex());

			computeOverall();
		}
	}

	public void incNbOfCompletedMissions(final String resourceID){
		lock.lock();
		int old = completedMissions.get(resourceID);
		int newCount = old +1;
		completedMissions.put(resourceID, newCount);
		JLabel[] jls = datas.get(resourceID);
		JLabel jl = jls[IndicatorPaneColumns.MISSIONS_DONE.getIndex()];
		jl.setText(completedMissions.get(resourceID)+"");
		jls[IndicatorPaneColumns.MISSIONS_DONE.getIndex()] = jl;

		straddleCarrierModel.setValueAt(jl, getIndex(resourceID), IndicatorPaneColumns.MISSIONS_DONE.getIndex());

		computeOverall();
		lock.unlock();
	}

	public void addOverspentTime(final String resourceID, Time overspentTime){
		if(overspentTime.getInSec()>0){
			lock.lock();
			overrunTW++;
			lock.unlock();
			Time oldTime = overspentTimes.get(resourceID);
			Time newTime = new Time(oldTime,overspentTime);
			overspentTimes.put(resourceID, newTime);

			double oldScore = fitnessScores.get(resourceID);
			oldScore += overspentTime.getInSec()*MissionScheduler.getEvalParameters().getLatenessCoeff();
			fitnessScores.put(resourceID, oldScore);

			JLabel[] jls = datas.get(resourceID);

			JLabel jl = jls[IndicatorPaneColumns.OVERSPENT_TIME.getIndex()];
			jl.setText(overspentTimes.get(resourceID).toString());
			jls[IndicatorPaneColumns.OVERSPENT_TIME.getIndex()] = jl;
			JLabel jlOverrun = jls[IndicatorPaneColumns.TW_OVERRUN.getIndex()];
			jlOverrun.setText(""+(Integer.parseInt(jlOverrun.getText())+1));
			jls[IndicatorPaneColumns.TW_OVERRUN.getIndex()] = jlOverrun;

			JLabel jlScore = jls[IndicatorPaneColumns.SCORE.getIndex()];
			jlScore.setText(df.format(fitnessScores.get(resourceID)));
			jls[IndicatorPaneColumns.SCORE.getIndex()] = jlScore;

			straddleCarrierModel.setValueAt(jl, getIndex(resourceID), IndicatorPaneColumns.OVERSPENT_TIME.getIndex());
			straddleCarrierModel.setValueAt(jlOverrun, getIndex(resourceID), IndicatorPaneColumns.TW_OVERRUN.getIndex());
			straddleCarrierModel.setValueAt(jlScore, getIndex(resourceID), IndicatorPaneColumns.SCORE.getIndex());

			computeOverall();
		}
	}

	public void addWaitTime(final String resourceID, Time waitTime){

		if(waitTime.getInSec()>0){
			Time oldTime = waitTimes.get(resourceID);
			Time newTime = new Time(oldTime,waitTime);
			waitTimes.put(resourceID, newTime);
			JLabel[] jls = datas.get(resourceID);
			JLabel jl = jls[IndicatorPaneColumns.WAIT_TIME.getIndex()];
			jl.setText(waitTimes.get(resourceID).toString());
			jls[IndicatorPaneColumns.WAIT_TIME.getIndex()] = jl;

			double oldScore = fitnessScores.get(resourceID);
			oldScore += waitTime.getInSec()*MissionScheduler.getEvalParameters().getEarlinessCoeff();
			fitnessScores.put(resourceID, oldScore);
			JLabel jlScore = jls[IndicatorPaneColumns.SCORE.getIndex()];
			jlScore.setText(df.format(fitnessScores.get(resourceID)));
			jls[IndicatorPaneColumns.SCORE.getIndex()] = jlScore;

			straddleCarrierModel.setValueAt(jl, getIndex(resourceID), IndicatorPaneColumns.WAIT_TIME.getIndex());
			straddleCarrierModel.setValueAt(jlScore, getIndex(resourceID), IndicatorPaneColumns.SCORE.getIndex());

			computeOverall();
		}
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("resource ID\t\tDISTANCE (METERS)\tOVERSPENT TIME (hh:mm:ss)\tTW OVERRUNS\tCOMPLETED MISSIONS\tWAIT TIME\n");
		sb.append("-------------------------------------------------------------------------------------\n");
		for(String scID : distances.keySet()){
			sb.append(scID+"\t"+distances.get(scID)+"\t\t"+overspentTimes.get(scID)+"\t\t"+datas.get(scID)[IndicatorPaneColumns.TW_OVERRUN.getIndex()].getText()+"\t\t"+completedMissions.get(scID)+"\t\t"+overspentTimes.get(scID)+"\n");
		}
		sb.append("-------------------------------------------------------------------------------------\nOVERALL:\t\t");
		for(JLabel jl : overall){
			sb.append(jl.getText().replaceAll(",", ".")+"\t");
		}
		return sb.toString();

	}
}
