package org.display.generation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.time.Time;
import org.util.generators.parsers.TruckGenerationData;

public class TruckMissionSettingsGenerationPanel extends GenerationDataPanel<TruckGenerationData> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6835530551298959029L;
	private static final Double MIN_RATE_COME_EMPTY = 0.0;
	private static final Double MIN_RATE_LEAVE_EMPTY = 0.0;

	private JSpinner jsCount;
	private JSpinner jsComeEmpty;
	private JSpinner jsLeaveEmpty;
	private JSpinner jsMin;
	private JSpinner jsMax;
	private JSpinner jsAvgTimeBeforeLeaving;
	
	private Date avgTimeBeforeLeaving;
	private Date minTime;

	private SpinnerDateModel minTimeModel;
	private SpinnerDateModel maxTimeModel;
	private SpinnerDateModel avgTimeModel;
	private SpinnerNumberModel spinnerNumberModel;
	private SpinnerNumberModel rateComeEmptyModel;
	private SpinnerNumberModel rateLeaveEmptyModel;

	@Override
	protected void buildElementPanel () {
		pElementControl.add(new JLabel("Count : "), constraints);
		constraints.gridx++;
		spinnerNumberModel = new SpinnerNumberModel(MIN_COUNT_VALUE,MIN_COUNT_VALUE, 500, 10);
		jsCount = new JSpinner(spinnerNumberModel);
		pElementControl.add(jsCount, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Rate come empty: "), constraints);
		constraints.gridx++;
		rateComeEmptyModel = new SpinnerNumberModel(MIN_RATE_COME_EMPTY.doubleValue(),MIN_RATE_COME_EMPTY.doubleValue(), 1.0, 0.1);
		jsComeEmpty = new JSpinner(rateComeEmptyModel);
		pElementControl.add(jsComeEmpty, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Rate leave empty: "), constraints);
		constraints.gridx++;
		rateLeaveEmptyModel = new SpinnerNumberModel(MIN_RATE_LEAVE_EMPTY.doubleValue(),MIN_RATE_LEAVE_EMPTY.doubleValue(), 1.0, 0.1);
		jsLeaveEmpty = new JSpinner(rateLeaveEmptyModel);
		pElementControl.add(jsLeaveEmpty, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Min time : "),constraints);
		constraints.gridx++;

		minTime = new Date(GenerationDataPanel.minTime.getTime());

		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 1);
		c.set(Calendar.SECOND, 0);
		avgTimeBeforeLeaving = c.getTime();

		minTimeModel = new SpinnerDateModel();
		minTimeModel.setCalendarField(Calendar.MINUTE);
		minTimeModel.setValue(new Date(minTime.getTime()));	

		jsMin = new JSpinner();
		jsMin.setModel(minTimeModel);
		jsMin.setEditor(new JSpinner.DateEditor(jsMin, "HH:mm:ss"));
		pElementControl.add(jsMin, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Max time : "), constraints);
		constraints.gridx++;
		maxTimeModel = new SpinnerDateModel();
		maxTimeModel.setCalendarField(Calendar.MINUTE);
		maxTimeModel.setValue(minTime);
		jsMax = new JSpinner();
		jsMax.setModel(maxTimeModel);
		jsMax.setEditor(new JSpinner.DateEditor(jsMax, "HH:mm:ss"));
		pElementControl.add(jsMax, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Avg time before leaving : "), constraints);
		constraints.gridx++;
		avgTimeModel = new SpinnerDateModel();
		avgTimeModel.setCalendarField(Calendar.MINUTE);
		avgTimeModel.setValue(new Date(avgTimeBeforeLeaving.getTime()));
		jsAvgTimeBeforeLeaving = new JSpinner();
		jsAvgTimeBeforeLeaving.setModel(avgTimeModel);
		jsAvgTimeBeforeLeaving.setEditor(new JSpinner.DateEditor(jsAvgTimeBeforeLeaving, "HH:mm:ss"));
		pElementControl.add(jsAvgTimeBeforeLeaving, constraints);
		setStates(false);
	}

	protected void setStates(boolean state){
		jsCount.setEnabled(state);
		jsComeEmpty.setEnabled(state);
		jsLeaveEmpty.setEnabled(state);
		jsMin.setEnabled(state);
		jsMax.setEnabled(state);
		jsAvgTimeBeforeLeaving.setEnabled(state);
	}

	public TruckMissionSettingsGenerationPanel () {
		super();
	}

	@Override
	protected void buildListeners () {
		jsCount.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(selectedData != null){
					selectedData.setCount(spinnerNumberModel.getNumber().intValue());
				}
			}
		});

		jsComeEmpty.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(selectedData != null){
					selectedData.setRateComeEmpty(rateComeEmptyModel.getNumber().doubleValue());
				}
			}
		});

		jsLeaveEmpty.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(selectedData != null){
					selectedData.setRateComeEmpty(rateLeaveEmptyModel.getNumber().doubleValue());
				}
			}
		});

		jsMin.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(selectedData != null){
					selectedData.setMinTime(minTimeModel.getDate());
				}
			}
		});

		jsMax.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(selectedData != null){
					selectedData.setMaxTime(maxTimeModel.getDate());
				}
			}
		});

		jsAvgTimeBeforeLeaving.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(selectedData != null){
					selectedData.setAvgTimeBeforeLeaving(avgTimeModel.getDate());
				}
			}
		});

		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(list.getSelectedIndex()>=0){
					selectedData = list.getSelectedValue();
					setStates(true);
					tb.setTitle(selectedData.getGroupID()+" settings :");
					spinnerNumberModel.setValue(selectedData.getNb());
					rateComeEmptyModel.setValue(selectedData.getRateComeEmpty());
					rateLeaveEmptyModel.setValue(selectedData.getRateLeaveEmpty());
					minTimeModel.setValue(selectedData.getMinTime());
					maxTimeModel.setValue(selectedData.getMaxTime());
					avgTimeModel.setValue(selectedData.getAvgTruckTimeBeforeLeaving());
				} else {
					//Remove data from the view
					tb.setTitle("Settings :");
					spinnerNumberModel.setValue(MIN_COUNT_VALUE);
					rateComeEmptyModel.setValue(MIN_RATE_COME_EMPTY);
					rateLeaveEmptyModel.setValue(MIN_RATE_LEAVE_EMPTY);
					minTimeModel.setValue(new Date(minTime.getTime()));
					maxTimeModel.setValue(new Date(minTime.getTime()));
					avgTimeModel.setValue(new Date(avgTimeBeforeLeaving.getTime()));
					setStates(false);
				}
				pElementControl.updateUI();
			}
		});

		jbAddData.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				TruckGenerationData stockData = new TruckGenerationData(MIN_COUNT_VALUE, MIN_RATE_COME_EMPTY, MIN_RATE_LEAVE_EMPTY, new Time(0).getDate(), new Time(0).getDate(), new Time(23,59,59).getDate(), "roadGroup-"+(COUNTER++));
				data.add(stockData);
				list.updateUI();
			}
		});

		jbRemoveData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!list.isSelectionEmpty()){
					TruckGenerationData selected = list.getSelectedValue();
					if(selected.equals(selectedData)){
						list.setSelectedIndex(1);
					}
					data.remove(selected);
					list.clearSelection();
					list.updateUI();
				}

			}
		});
	}
}
