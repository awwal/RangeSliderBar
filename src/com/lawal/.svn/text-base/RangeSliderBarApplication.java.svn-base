package com.lawal;

import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class RangeSliderBarApplication extends Application {

	private static final long serialVersionUID = 5484943553531596125L;

	@Override
	public void init() {
		Window mainWindow = new Window("RangeSliderBar Application");

		VerticalLayout vlay = setup();

		mainWindow.addComponent(vlay);

		setMainWindow(mainWindow);
	}

	private VerticalLayout setup() {

		final RangeSliderBar slider = new RangeSliderBar();
		final TextField minTf = new TextField();
		final TextField maxTf = new TextField();

		Button btn = new Button("Set value");

		btn.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					double minVal = convertVal(minTf.getValue());
					double maxVal = convertVal(maxTf.getValue());

					slider.setKnobValues(minVal, maxVal);

				} catch (NumberFormatException e) {

					e.printStackTrace();
				}
			}
		});

		VerticalLayout vlay = new VerticalLayout();
		vlay.setWidth(500, Sizeable.UNITS_PIXELS);
		vlay.addStyleName(Reindeer.LAYOUT_BLUE);

		slider.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				DoublePair val = (DoublePair) event.getProperty().getValue();
				Double max = val.max;
				Double min = val.min;
				minTf.setValue(min);
				maxTf.setValue(max);
			

			}
		});
		slider.setNumberOfLabels(10);
		slider.setNumberOfTicks(5);
		slider.setRangeMin(-15);
		slider.setRangeMax(+15);
		slider.setStepSize(.5);
		slider.setSuperImmediateMode(false);
		slider.setAppendString("ms");
		slider.setRequired(true);
		slider.setImmediate(true);
		slider.setCaption("Reaction time");
		
		DoublePair p = new DoublePair(-12.0d, 10.0d);
		slider.setValue(p);

		// Button incrRannge = new Button("+");
		//
		// incrRannge.addListener(new ClickListener() {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void buttonClick(ClickEvent event) {
		// bar.setRangeMax(bar.getRangeMax() - 10);
		// }
		// });

		HorizontalLayout hlay = new HorizontalLayout();
		hlay.addComponent(minTf);
		hlay.addComponent(maxTf);
		hlay.addComponent(btn);
		vlay.addComponent(hlay);
		vlay.addComponent(slider);
		return vlay;
	}

	protected double convertVal(Object mv) {
		return new Double(String.valueOf(mv));

	}
	


}
