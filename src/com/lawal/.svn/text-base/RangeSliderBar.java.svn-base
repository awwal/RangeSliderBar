/*
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * 
 * This is a modified port of the Google gwt SliderBar into Vaadin obtained from
 * http://google-web-toolkit-incubator.googlecode.com/svn/trunk/demo/SliderBar/index.html
 * by Olufowobi Lawal talktolawal@gmail.com (Nov 2011)
 */
package com.lawal;

import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;

@SuppressWarnings("serial")
@com.vaadin.ui.ClientWidget(com.lawal.client.ui.VRangeSliderBar.class)
public class RangeSliderBar extends AbstractField {
	
	private static final Object MIN_VALUE_VARIABLE = "knobmin";
	private static final Object MAX_VALUE_VARIABLE = "knobmax";
	private double rangeMin = 0;
	private double rangeMax = 100;
	private double stepSize = 1;
	private int numTicks = 10;
	private int numLabels = 10;
	private boolean superImmediateMode = false;
	private double knobMinValue = rangeMin;
	private double knobMaxValue = rangeMax;
	private String appendString = "";

	/**
	 * Default Slider constructor. Sets all values to defaults and the slide
	 * handle at minimum value.
	 */
	public RangeSliderBar() {
		super();
		setSizeFull();
		setVal(knobMinValue, knobMaxValue, false);
	}

	/**
	 * Create a new slider with the caption given as parameter. All slider
	 * values set to defaults.
	 * 
	 * @param caption
	 */
	public RangeSliderBar(String caption) {
		this();
		setCaption(caption);
	}

	/**
	 * @param rangeMin
	 * @param rangeMax
	 * @param stepSize
	 */
	public RangeSliderBar(double min, double max, int stepsize) {
		this();
		setRangeMin(min);
		setRangeMax(max);
		setStepSize(stepsize);
	}

	/**
	 * Create a new slider with given range
	 * 
	 * @param rangeMin
	 * @param rangeMax
	 */
	public RangeSliderBar(int min, int max) {
		this();
		setRangeMin(min);
		setRangeMax(max);
		setStepSize(0);
	}

	/**
	 * Create a new slider with given caption and range
	 * 
	 * @param caption
	 * @param rangeMin
	 * @param rangeMax
	 */
	public RangeSliderBar(String caption, int min, int max) {
		this(min, max);
		setCaption(caption);
	}

	/**
	 * Gets the biggest possible value in Sliders range.
	 * 
	 * @return the biggest value slider can have
	 */
	public double getRangeMax() {
		return rangeMax;
	}

	/**
	 * Set the maximum value of the Slider. If the current max value of the Slider
	 * is out of new bounds, the value is set to new maximum val of the slider.
	 * 
	 * @param rangeMax
	 *            New maximum value of the Slider.
	 */
	public void setRangeMax(double rangeMax) {
		this.rangeMax = rangeMax;

		double presentMax = ((DoublePair) getValue()).max;

		if (rangeMax < presentMax) {
			setVal(((DoublePair) getValue()).min, rangeMax, false);
		}

		requestRepaint();
	}

	/**
	 * Gets the minimum value in Sliders range.
	 * 
	 * @return the smallest value slider can have
	 */
	public double getRangeMin() {
		return rangeMin;
	}

	/**
	 * Set the minimum range of the Slider. If the current min value of the
	 * Slider is out of new bounds, the value is set to new minimum.
	 * 
	 * @param rangeMin
	 *            New minimum value of the Slider.
	 */
	public void setRangeMin(double minRangeValue) {
		this.rangeMin = minRangeValue;

		double presentMin = ((DoublePair) getValue()).min;

		if (Double.compare(rangeMin, presentMin) > 0) {
			setVal(rangeMin, ((DoublePair) getValue()).max, false);
		}

		requestRepaint();
	}

	/**
	 * Get the current stepSize of the Slider.
	 * 
	 * @return stepSize
	 */
	public double getStepSize() {
		return stepSize;
	}

	/**
	 * Set a new stepSize for the Slider.
	 * 
	 * @param stepSize
	 */
	public void setStepSize(double stepSize) {
		if (stepSize < 0) {
			return;
		}
		this.stepSize = stepSize;
		requestRepaint();
	}

	private double toMaxDescrete(double newMax) {
		if (newMax > rangeMax) {
			return rangeMax;
		}
		return newMax;
	}

	private double toMinDescrete(double newMin) {
		if (newMin < rangeMin) {
			return rangeMin;
		}
		return newMin;
	}

	@Override
	// send value to client
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		target.addAttribute("rangeMin", rangeMin);
		if (rangeMax > rangeMin) {
			target.addAttribute("rangeMax", rangeMax);
		} else {
			target.addAttribute("rangeMax", rangeMin);
		}
		target.addAttribute("stepsize", stepSize);
		target.addAttribute("numticks", numTicks);
		target.addAttribute("numlabels", numLabels);
		target.addAttribute("superimmediate", superImmediateMode);

		target.addAttribute("knobmin", knobMinValue);
		target.addAttribute("knobmax", knobMaxValue);

		target.addAttribute("append", appendString);
	}

	/**
	 * Invoked when the value of a variable has changed. RangeSliderBar
	 * listeners are notified if the slider value has changed.
	 * 
	 * @param source
	 * @param variables
	 */

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		if (variables.containsKey(MIN_VALUE_VARIABLE) && variables.containsKey(MAX_VALUE_VARIABLE)) {
			final Double min = new Double(variables.get(MIN_VALUE_VARIABLE).toString());
			final Double max = new Double(variables.get(MAX_VALUE_VARIABLE).toString());

			setVal(min, max, true);

		}
	}

	@Override
	public Class<?> getType() {
		return DoublePair.class;
	}

	/**
	 * The number of tick marks to show.
	 */
	public void setNumberOfTicks(int numTicks) {
		this.numTicks = numTicks;
	}

	public int getNumberTicks() {
		return numTicks;
	}

	/**
	 * The number of labels to show.
	 */
	public void setNumberOfLabels(int numLabels) {
		this.numLabels = numLabels;
	}

	public int getNumberLabels() {
		return numLabels;
	}

	/**
	 * If super immediate mode is true, values are immediately received from
	 * client when dragging with the mouse and when the Arrow keys are pressed
	 * down
	 * 
	 * @param superImmediateMode
	 */
	public void setSuperImmediateMode(boolean superImmediateMode) {
		if (superImmediateMode)
			this.setImmediate(true);
		this.superImmediateMode = superImmediateMode;
		requestRepaint();
	}

	public boolean isSuperImmediateMode() {
		return superImmediateMode;
	}

	public DoublePair getKnobValues() {
		return (DoublePair) super.getValue();
	}

	/**
	 * Set the value of this Slider.
	 * 
	 * @param value
	 *            New value of Slider. Must be within Sliders range (rangeMin -
	 *            rangeMax), otherwise throws an exception.
	 * @param repaintIsNotNeeded
	 *            If true, client-side is not requested to repaint itself.
	 * @throws ValueOutOfBoundsException
	 */
	protected void setVal(double min, double max, boolean repaintIsNotNeeded) {

		knobMinValue = toMinDescrete(min);
		knobMaxValue = toMaxDescrete(max);

		if (knobMaxValue < knobMinValue) {
			knobMaxValue = knobMinValue;
		}
		DoublePair dv = new DoublePair(knobMinValue, knobMaxValue);
	
		super.setValue(dv, repaintIsNotNeeded);
	}

	public void setKnobValues(Double minValue, Double maxValue) {

		setVal(minValue, maxValue, false);
	}

	@Override
	public void setValue(Object newValue){
		if (!(newValue instanceof DoublePair)) {
			throw new IllegalArgumentException("Value of Type " + DoublePair.class.getSimpleName() + " was excepted");

		}

		DoublePair val = (DoublePair) newValue;
		setVal(val.min, val.max, false);
	}

	public String getAppendString() {
		return appendString;
	}

	public void setAppendString(String appendString) {
		this.appendString = appendString;
	}
}
