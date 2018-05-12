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
import org.util.generators.parsers.TrainGenerationData;

public class TrainMissionSettingsGenerationPanel extends GenerationDataPanel<TrainGenerationData> {
	private static final Date MAX_TIME = new Time(23,59,59).getDate();
	private static final double DEFAULT_MARGIN_RATE = 0.3d;
	private static final double DEFAULT_FULL_RATE = 0.75d;
	private static final double DEFAULT_AFTER_UNLOAD_RATE = 0.25d;
	private static final double DEFAULT_AFTER_RELOAD_RATE = 0.75d;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JSpinner jsMinTime;
	private JSpinner jsMaxTime;
	private JSpinner jsMarginRate;
	private JSpinner jsFullRate;
	private JSpinner jsAfterReload;
	private JSpinner jsAfterUnload;
	
	private SpinnerDateModel minTimeModel;
	private SpinnerDateModel maxTimeModel;
	private SpinnerNumberModel fullRateModel;
	private SpinnerNumberModel afterReloadModel;
	private SpinnerNumberModel afterUnloadModel;
	private SpinnerNumberModel marginRateModel;
	
	public TrainMissionSettingsGenerationPanel() {
		super();
	}

	@Override
	protected void buildElementPanel() {
		pElementControl.add(new JLabel("Min arrival time : "), constraints);
		constraints.gridx++;
		minTimeModel = new SpinnerDateModel();
		minTimeModel.setCalendarField(Calendar.MINUTE);
		minTimeModel.setValue(new Date(minTime.getTime()));

		jsMinTime = new JSpinner();
		jsMinTime.setModel(minTimeModel);
		jsMinTime.setEditor(new JSpinner.DateEditor(jsMinTime, "HH:mm:ss"));
		pElementControl.add(jsMinTime, constraints);
		constraints.gridy++;
		constraints.gridx = 0;
		
		pElementControl.add(new JLabel("Max departure time : "), constraints);
		constraints.gridx++;
		maxTimeModel = new SpinnerDateModel();
		maxTimeModel.setCalendarField(Calendar.MINUTE);
		maxTimeModel.setValue(new Date(MAX_TIME.getTime()));

		jsMaxTime = new JSpinner();
		jsMaxTime.setModel(maxTimeModel);
		jsMaxTime.setEditor(new JSpinner.DateEditor(jsMaxTime, "HH:mm:ss"));
		pElementControl.add(jsMaxTime, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Margin rate : "), constraints);
		constraints.gridx++;
		marginRateModel = new SpinnerNumberModel(DEFAULT_MARGIN_RATE, 0d, 1d, 0.1);
		jsMarginRate = new JSpinner(marginRateModel);
		pElementControl.add(jsMarginRate, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Full rate : "), constraints);
		constraints.gridx++;
		fullRateModel = new SpinnerNumberModel(DEFAULT_FULL_RATE, 0d, 1d, 0.1);
		jsFullRate = new JSpinner(fullRateModel);
		pElementControl.add(jsFullRate, constraints);
		constraints.gridy++;
		constraints.gridx = 0;
		
		pElementControl.add(new JLabel("After unload rate : "), constraints);
		constraints.gridx++;
		afterUnloadModel = new SpinnerNumberModel(DEFAULT_AFTER_UNLOAD_RATE, 0d, 1d, 0.1);
		
		jsAfterUnload = new JSpinner(afterUnloadModel);
		pElementControl.add(jsAfterUnload, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		
		pElementControl.add(new JLabel("After reload rate : "), constraints);
		constraints.gridx++;
		afterReloadModel = new SpinnerNumberModel(DEFAULT_AFTER_RELOAD_RATE, 0d, 1d, 0.1);
		
		jsAfterReload = new JSpinner(afterReloadModel);
		pElementControl.add(jsAfterReload, constraints);
		constraints.gridy++;
		constraints.gridx = 0;
				
		setStates(false);
	}

	protected void buildListeners() {
		
		
		jsMinTime.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setMinTime(minTimeModel.getDate());
				}
			}
		});

		jsMaxTime.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setMaxTime(maxTimeModel.getDate());
				}
			}
		});
		
		jsFullRate.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setFullRate(fullRateModel.getNumber().doubleValue());
				}
			}
		});

		jsMarginRate.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setMarginRate(marginRateModel.getNumber().doubleValue());
				}
			}
		});
		
		jsAfterReload.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setAfterReload(afterReloadModel.getNumber().doubleValue());
				}
			}
		});

		jsAfterUnload.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setAfterUnload(afterUnloadModel.getNumber().doubleValue());
				}
			}
		});
				
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (list.getSelectedIndex() >= 0) {
					selectedData = list.getSelectedValue();
					setStates(true);
					tb.setTitle(selectedData.getGroupID() + " settings :");

					minTimeModel.setValue(selectedData.getMinTime());
					maxTimeModel.setValue(selectedData.getMaxTime());
					marginRateModel.setValue(selectedData.getMarginRate());
					fullRateModel.setValue(selectedData.getFullRate());
					afterReloadModel.setValue(selectedData.getAfterReload());
					afterUnloadModel.setValue(selectedData.getAfterUnload());
				} else {
					// Remove data from the view
					tb.setTitle("Settings :");
					minTimeModel.setValue(minTime);
					maxTimeModel.setValue(new Date(MAX_TIME.getTime()));
					marginRateModel.setValue(DEFAULT_MARGIN_RATE);
					fullRateModel.setValue(DEFAULT_FULL_RATE);
					afterReloadModel.setValue(DEFAULT_AFTER_RELOAD_RATE);
					afterUnloadModel.setValue(DEFAULT_AFTER_UNLOAD_RATE);
					
					setStates(false);
				}
				pElementControl.updateUI();
			}
		});

		jbAddData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TrainGenerationData trainData = new TrainGenerationData(minTime, new Time(23,59,59).getDate(), 0.75, 0.25, 0.75, 0.3, "trainGroup-" + (COUNTER++));
				data.add(trainData);
				list.updateUI();
			}
		});

		jbRemoveData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!list.isSelectionEmpty()) {
					TrainGenerationData selected = list.getSelectedValue();
					if (selected.equals(selectedData)) {
						list.setSelectedIndex(1);
					}
					data.remove(selected);
					list.clearSelection();
					list.updateUI();
				}

			}
		});
	}

	@Override
	protected void setStates(boolean state) {
		jsMinTime.setEnabled(state);
		jsMaxTime.setEnabled(state);
		jsMarginRate.setEnabled(state);
		jsFullRate.setEnabled(state);
		jsAfterReload.setEnabled(state);
		jsAfterUnload.setEnabled(state);
	}
	
}
