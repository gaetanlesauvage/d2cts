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

import org.util.generators.parsers.ShipGenerationData;

public class ShipMissionSettingsGenerationPanel extends GenerationDataPanel<ShipGenerationData> {
	private static final int MAX_TEU = 5000;

	private static final int DEFAULT_TEU = 100;

	private static final Integer MIN_COUNT_VALUE = 1;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JSpinner jsMinBerthTimeLength;
	private JSpinner jsMaxArrivalTime;
	private JSpinner jsMaxDepartureTime;
	private JSpinner jsTimePerContainerOperation;
	private JSpinner jsMinTeuCapacity;
	private JSpinner jsMaxTeuCapacity;
	private JSpinner jsCapacityFactor;
	private JSpinner jsFullRate;
	private JSpinner jsTwentyFeetRate;
	private JSpinner jsFortyFeetRate;
	private JSpinner jsAfterUnload;
	private JSpinner jsAfterReload;
	private JSpinner jsMarginRate;

	private SpinnerDateModel minBerthTimeLengthModel;
	private SpinnerDateModel maxArrivalTimeModel;
	private SpinnerDateModel maxDepartureTimeModel;
	private SpinnerDateModel timePerContainerOperationhModel;
	private SpinnerNumberModel minTeuCapacityModel;
	private SpinnerNumberModel maxTeuCapacityModel;
	private SpinnerNumberModel capacityFactorModel;
	private SpinnerNumberModel fullRateModel;
	private SpinnerNumberModel twentyFeetRateModel;
	private SpinnerNumberModel fortyFeetRateModel;
	private SpinnerNumberModel afterUnloadModel;
	private SpinnerNumberModel afterReloadModel;
	private SpinnerNumberModel marginRateModel;

	
	public ShipMissionSettingsGenerationPanel() {
		super();
	}

	@Override
	protected void buildElementPanel() {
		pElementControl.add(new JLabel("Min berth time before leaving : "), constraints);
		constraints.gridx++;
		minBerthTimeLengthModel = new SpinnerDateModel();
		minBerthTimeLengthModel.setCalendarField(Calendar.MINUTE);
		minBerthTimeLengthModel.setValue(new Date(minTime.getTime()));

		jsMinBerthTimeLength = new JSpinner();
		jsMinBerthTimeLength.setModel(minBerthTimeLengthModel);
		jsMinBerthTimeLength.setEditor(new JSpinner.DateEditor(jsMinBerthTimeLength, "HH:mm:ss"));
		pElementControl.add(jsMinBerthTimeLength, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Max arrival time : "), constraints);
		constraints.gridx++;
		maxArrivalTimeModel = new SpinnerDateModel();
		maxArrivalTimeModel.setCalendarField(Calendar.MINUTE);
		maxArrivalTimeModel.setValue(new Date(minTime.getTime()));

		jsMaxArrivalTime = new JSpinner();
		jsMaxArrivalTime.setModel(maxArrivalTimeModel);
		jsMaxArrivalTime.setEditor(new JSpinner.DateEditor(jsMaxArrivalTime, "HH:mm:ss"));
		pElementControl.add(jsMaxArrivalTime, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Max departure time : "), constraints);
		constraints.gridx++;
		maxDepartureTimeModel = new SpinnerDateModel();
		maxDepartureTimeModel.setCalendarField(Calendar.MINUTE);
		maxDepartureTimeModel.setValue(new Date(minTime.getTime()));

		jsMaxDepartureTime = new JSpinner();
		jsMaxDepartureTime.setModel(maxDepartureTimeModel);
		jsMaxDepartureTime.setEditor(new JSpinner.DateEditor(jsMaxDepartureTime, "HH:mm:ss"));
		pElementControl.add(jsMaxDepartureTime, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Time per container operation : "), constraints);
		constraints.gridx++;
		timePerContainerOperationhModel = new SpinnerDateModel();
		timePerContainerOperationhModel.setCalendarField(Calendar.MINUTE);
		timePerContainerOperationhModel.setValue(new Date(minTime.getTime()));

		jsTimePerContainerOperation = new JSpinner();
		jsTimePerContainerOperation.setModel(timePerContainerOperationhModel);
		jsTimePerContainerOperation.setEditor(new JSpinner.DateEditor(jsTimePerContainerOperation, "HH:mm:ss"));
		pElementControl.add(jsTimePerContainerOperation, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Min TEU capacity : "), constraints);
		constraints.gridx++;
		minTeuCapacityModel = new SpinnerNumberModel(DEFAULT_TEU, MIN_COUNT_VALUE.intValue(), MAX_TEU, 10);
		jsMinTeuCapacity = new JSpinner(minTeuCapacityModel);
		pElementControl.add(jsMinTeuCapacity, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Max TEU capacity : "), constraints);
		constraints.gridx++;
		maxTeuCapacityModel = new SpinnerNumberModel(DEFAULT_TEU, MIN_COUNT_VALUE.intValue(), MAX_TEU, 10); // TODO
																											// Ensure
																											// MAX
																											// >
																											// MIN
		jsMaxTeuCapacity = new JSpinner(maxTeuCapacityModel);
		pElementControl.add(jsMaxTeuCapacity, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Capacity factor : "), constraints);
		constraints.gridx++;
		capacityFactorModel = new SpinnerNumberModel(4, 1, 10, 1);
		jsCapacityFactor = new JSpinner(capacityFactorModel);
		pElementControl.add(jsCapacityFactor, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Full rate : "), constraints);
		constraints.gridx++;
		fullRateModel = new SpinnerNumberModel(0.0, 0.0, 1.0, 0.1);
		jsFullRate = new JSpinner(fullRateModel);
		pElementControl.add(jsFullRate, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Twenty feet rate : "), constraints);
		constraints.gridx++;
		twentyFeetRateModel = new SpinnerNumberModel(0.0, 0.0, 1.0, 0.1);
		jsTwentyFeetRate = new JSpinner(twentyFeetRateModel);
		pElementControl.add(jsTwentyFeetRate, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Forty feet rate : "), constraints);
		constraints.gridx++;
		fortyFeetRateModel = new SpinnerNumberModel(0.0, 0.0, 1.0, 0.1);
		jsFortyFeetRate = new JSpinner(fortyFeetRateModel);
		pElementControl.add(jsFortyFeetRate, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("After unload rate : "), constraints);
		constraints.gridx++;
		afterUnloadModel = new SpinnerNumberModel(0.0, 0.0, 1.0, 0.1);
		jsAfterUnload = new JSpinner(afterUnloadModel);
		pElementControl.add(jsAfterUnload, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("After reload rate : "), constraints);
		constraints.gridx++;
		afterReloadModel = new SpinnerNumberModel(0.0, 0.0, 1.0, 0.1);
		jsAfterReload = new JSpinner(afterReloadModel);
		pElementControl.add(jsAfterReload, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		pElementControl.add(new JLabel("Margin rate : "), constraints);
		constraints.gridx++;
		marginRateModel = new SpinnerNumberModel(0.0, 0.0, 1.0, 0.1);
		jsMarginRate = new JSpinner(marginRateModel);
		pElementControl.add(jsMarginRate, constraints);
		constraints.gridy++;
		constraints.gridx = 0;
		
		setStates(false);
	}

	protected void buildListeners() {
		jsMinBerthTimeLength.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setMinBerthTimeLength(minBerthTimeLengthModel.getDate());
				}
			}
		});

		jsMaxArrivalTime.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setMaxArrivalTime(maxArrivalTimeModel.getDate());
				}
			}
		});

		jsMaxDepartureTime.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setMaxDepartureTime(maxDepartureTimeModel.getDate());
				}
			}
		});

		jsTimePerContainerOperation.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setTimePerContainerOperation(timePerContainerOperationhModel.getDate());
				}
			}
		});

		jsMinTeuCapacity.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setMinTeuCapacity(minTeuCapacityModel.getNumber().intValue());
				}
			}
		});

		jsMaxTeuCapacity.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setMaxTeuCapacity(maxTeuCapacityModel.getNumber().intValue());
				}
			}
		});

		jsCapacityFactor.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setCapacityFactor(capacityFactorModel.getNumber().intValue());
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

		jsTwentyFeetRate.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setTwentyFeetRate(twentyFeetRateModel.getNumber().doubleValue());
				}
			}
		});

		jsFortyFeetRate.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setFortyFeetRate(fortyFeetRateModel.getNumber().doubleValue());
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

		jsAfterReload.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (selectedData != null) {
					selectedData.setAfterReload(afterReloadModel.getNumber().doubleValue());
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
		
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (list.getSelectedIndex() >= 0) {
					selectedData = list.getSelectedValue();
					setStates(true);
					tb.setTitle(selectedData.getGroupID() + " settings :");

					minBerthTimeLengthModel.setValue(selectedData.getMinBerthTimeLength());
					maxArrivalTimeModel.setValue(selectedData.getMaxArrivalTime());
					maxDepartureTimeModel.setValue(selectedData.getMaxDepartureTime());
					timePerContainerOperationhModel.setValue(selectedData.getTimePerContainerOperation());
					minTeuCapacityModel.setValue(selectedData.getMinTeuCapacity());
					maxTeuCapacityModel.setValue(selectedData.getMaxTeuCapacity());
					capacityFactorModel.setValue(selectedData.getCapacityFactor());
					fullRateModel.setValue(selectedData.getFullRate());
					twentyFeetRateModel.setValue(selectedData.getTwentyFeetRate());
					fortyFeetRateModel.setValue(selectedData.getFortyFeetRate());
					afterUnloadModel.setValue(selectedData.getAfterUnload());
					afterReloadModel.setValue(selectedData.getAfterReload());
					marginRateModel.setValue(selectedData.getMarginRate());
				} else {
					// Remove data from the view
					tb.setTitle("Settings :");
					minBerthTimeLengthModel.setValue(minTime);
					maxArrivalTimeModel.setValue(minTime);
					maxDepartureTimeModel.setValue(minTime);
					timePerContainerOperationhModel.setValue(minTime);
					minTeuCapacityModel.setValue(DEFAULT_TEU);
					maxTeuCapacityModel.setValue(DEFAULT_TEU);
					capacityFactorModel.setValue(4);
					fullRateModel.setValue(0.5);
					twentyFeetRateModel.setValue(0.1);
					fortyFeetRateModel.setValue(0.9);
					afterUnloadModel.setValue(0.5);
					afterReloadModel.setValue(0.5);
					marginRateModel.setValue(0.2);
					setStates(false);
				}
				pElementControl.updateUI();
			}
		});

		jbAddData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				ShipGenerationData shipData = new ShipGenerationData(defaultMinBerthTime, defaultMaxArrivalTime, defaultMaxDepartureTime, defaultContainerTimeOperation, DEFAULT_TEU, DEFAULT_TEU, 4, 0.5, 0.1, 0.9,
						0.5, 0.5, 0.2, "seaGroup-" + (COUNTER++));
				data.add(shipData);
				list.updateUI();
			}
		});

		jbRemoveData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!list.isSelectionEmpty()) {
					ShipGenerationData selected = list.getSelectedValue();
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
		jsMinBerthTimeLength.setEnabled(state);
		jsMaxArrivalTime.setEnabled(state);
		jsMaxDepartureTime.setEnabled(state);
		jsTimePerContainerOperation.setEnabled(state);
		jsMinTeuCapacity.setEnabled(state);
		jsMaxTeuCapacity.setEnabled(state);
		jsCapacityFactor.setEnabled(state);
		jsFullRate.setEnabled(state);
		jsTwentyFeetRate.setEnabled(state);
		jsFortyFeetRate.setEnabled(state);
		jsAfterUnload.setEnabled(state);
		jsAfterReload.setEnabled(state);
		jsMarginRate.setEnabled(state);
	}

	
}
