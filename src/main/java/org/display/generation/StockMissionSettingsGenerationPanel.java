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
import org.util.generators.parsers.StockGenerationData;

public class StockMissionSettingsGenerationPanel extends GenerationDataPanel<StockGenerationData> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3905263561489878905L;
	private JSpinner jsCount;
	private JSpinner jsMin;
	private JSpinner jsMax;
	private JSpinner jsMarginTime;
	
	private Date marginTime;
	private Date minTime;

	private SpinnerDateModel minTimeModel;
	private SpinnerDateModel maxTimeModel;
	private SpinnerDateModel marginTimeModel;
	private SpinnerNumberModel spinnerNumberModel;

	@Override
	protected void buildElementPanel () {
		pElementControl.add(new JLabel("Count : "), constraints);
		constraints.gridx++;
		spinnerNumberModel = new SpinnerNumberModel(MIN_COUNT_VALUE,MIN_COUNT_VALUE, 500, 10);
		jsCount = new JSpinner(spinnerNumberModel);
		pElementControl.add(jsCount, constraints);
		constraints.gridy++;
		constraints.gridx = 0;
		pElementControl.add(new JLabel("Min time : "),constraints);
		constraints.gridx++;

		minTime = new Date(GenerationDataPanel.minTime.getTime());
		Calendar c = Calendar.getInstance();

		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 1);
		c.set(Calendar.SECOND, 0);
		marginTime = c.getTime();

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

		pElementControl.add(new JLabel("Margin time : "), constraints);
		constraints.gridx++;
		marginTimeModel = new SpinnerDateModel();
		marginTimeModel.setCalendarField(Calendar.MINUTE);
		marginTimeModel.setValue(new Date(marginTime.getTime()));
		jsMarginTime = new JSpinner();
		jsMarginTime.setModel(marginTimeModel);
		jsMarginTime.setEditor(new JSpinner.DateEditor(jsMarginTime, "HH:mm:ss"));
		pElementControl.add(jsMarginTime, constraints);
		
		setStates(false);
	}

	@Override
	public void setStates (boolean state) {
		jsCount.setEnabled(state);
		jsMin.setEnabled(state);
		jsMax.setEnabled(state);
		jsMarginTime.setEnabled(state);
	}

	public StockMissionSettingsGenerationPanel () {
		super();
	}

	protected void buildListeners () {
		jsCount.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(selectedData != null){
					selectedData.setCount(spinnerNumberModel.getNumber().intValue());
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

		jsMarginTime.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(selectedData != null){
					selectedData.setMarginTime(marginTimeModel.getDate());
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
					minTimeModel.setValue(selectedData.getMinTime());
					maxTimeModel.setValue(selectedData.getMaxTime());
					marginTimeModel.setValue(selectedData.getMarginTime());
				} else {
					//Remove data from the view
					tb.setTitle("Settings :");
					spinnerNumberModel.setValue(MIN_COUNT_VALUE);
					minTimeModel.setValue(new Date(minTime.getTime()));
					maxTimeModel.setValue(new Date(minTime.getTime()));
					marginTimeModel.setValue(new Date(marginTime.getTime()));
					setStates(false);
				}
				pElementControl.updateUI();
			}
		});

		jbAddData.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				StockGenerationData stockData = new StockGenerationData(MIN_COUNT_VALUE, new Time(0).getDate(), new Time(23,59, 59).getDate(), new Time(0).getDate(), "stockGroup-"+(COUNTER++));
				data.add(stockData);
				list.updateUI();
			}
		});

		jbRemoveData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!list.isSelectionEmpty()){
					StockGenerationData selected = list.getSelectedValue();
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
