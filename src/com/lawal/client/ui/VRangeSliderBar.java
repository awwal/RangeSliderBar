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
 */
package com.lawal.client.ui;

import java.util.ArrayList;
import java.util.List;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FocusPanel;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.ContainerResizedListener;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.Field;

public class VRangeSliderBar extends FocusPanel implements Paintable, Field, ContainerResizedListener {

	private static final String SLIDER_BAR_LINE = "gwt-VRangeSliderBar-line";
	private static final String KNOB_MAX_CLASSNAME = "gwt-VRangeSliderBar-knobmax";
	private static final String KNOB_MIN_CLASSNAME = "gwt-VRangeSliderBar-knobmin";

	private static final String CLASSNAME = "gwt-VRangeSliderBar-shell";
	/**
	 * The timer used to continue to shift the knob if the user holds down a
	 * key.
	 */
	private KeyTimer keyTimer = new KeyTimer();
	/**
	 * The elements used to display labels above the ticks.
	 */
	private List<Element> labelElements = new ArrayList<Element>();
	/**
	 * The line that the knob moves over.
	 */
	private Element lineElement;
	/**
	 * The offset between the edge of the shell and the line.
	 */
	private int lineLeftOffset = 0;
	/**
	 * The maximum slider value.
	 */
	private double rangeMax;
	/**
	 * The minimum slider value.
	 */
	private double rangeMin;
	/**
	 * The number of labels to show.
	 */
	private int numLabels = 0;
	/**
	 * The number of tick marks to show.
	 */
	private int numTicks = 0;
	/**
	 * A bit indicating whether or not we are currently sliding the slider bar
	 * due to keyboard events.
	 */
	private boolean slidingKeyboard = false;
	/**
	 * A bit indicating whether or not we are currently sliding the slider bar
	 * due to mouse events.
	 */
	private boolean slidingMouse = false;
	/**
	 * A bit indicating whether or not the slider is enabled
	 */
	private boolean enabled = true;
	/**
	 * The size of the increments between knob positions.
	 */
	private double stepSize;
	/**
	 * The elements used to display tick marks, which are the vertical lines
	 * along the slider bar.
	 */
	private List<Element> tickElements = new ArrayList<Element>();
	private LabelFormatter labelFormatter;
	private Element knobElemMin;
	private Element knobElemMax;

	private ApplicationConnection client;
	private String id;
	private boolean immediate;
	private boolean readonly;
	private Element progressElementMin;
	private Element progressElementMax;

	private boolean superImmediate = false;
	private double minCurrValue;
	private double maxCurrValue;
	private Element _target;
	private String appendString="";

	public VRangeSliderBar() {
		super();
		this.rangeMin = 0;
		this.rangeMax = 100;
		this.setMinCurrValue(0);
		this.maxCurrValue = 100;
		this.stepSize = 10;
		setLabelFormatter(LabelFormatter.getDefault());
		// Create the outer shell
		DOM.setStyleAttribute(getElement(), "position", "relative");
		setStyleName(CLASSNAME);
		// Create the line
		lineElement = DOM.createDiv();
		DOM.appendChild(getElement(), lineElement);
		DOM.setStyleAttribute(lineElement, "position", "absolute");
		DOM.setElementProperty(lineElement, "className", SLIDER_BAR_LINE);
		// Create the knob

		// Element knobElemMin = knobImage.getElement();
		knobElemMin = DOM.createDiv();
		DOM.appendChild(getElement(), knobElemMin);
		DOM.setStyleAttribute(knobElemMin, "position", "absolute");
		DOM.setElementProperty(knobElemMin, "className", KNOB_MIN_CLASSNAME);

		knobElemMax = DOM.createDiv();
		DOM.appendChild(getElement(), knobElemMax);
		DOM.setStyleAttribute(knobElemMax, "position", "absolute");
		DOM.setElementProperty(knobElemMax, "className", KNOB_MAX_CLASSNAME);

		progressElementMin = DOM.createDiv();
		DOM.appendChild(getElement(), progressElementMin);
		DOM.setStyleAttribute(progressElementMin, "position", "absolute");
		DOM.setElementProperty(progressElementMin, "className", "gwt-VRangeSliderBar-progress");
		
		progressElementMax = DOM.createDiv();
		DOM.appendChild(getElement(), progressElementMax);
		DOM.setStyleAttribute(progressElementMax, "position", "absolute");
		DOM.setElementProperty(progressElementMax, "className", "gwt-VRangeSliderBar-progress");
		
		
		
		sinkEvents(Event.MOUSEEVENTS | Event.ONMOUSEWHEEL | Event.KEYEVENTS | Event.FOCUSEVENTS | Event.TOUCHEVENTS);
	}

	/**
	 * Return the total range between the minimum and maximum values.
	 * 
	 * @return the total range
	 */
	public double getTotalRange() {
		if (rangeMin > rangeMax) {
			return 0;
		}
		return rangeMax - rangeMin;
	}

	/**
	 * Listen for events that will move the knob.
	 * 
	 * @param event
	 *            the event that occurred
	 */
	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);
		// VConsole.log("browser event" + DOM.eventGetType(event));
		if (!enabled || readonly) {
			return;
		}

		switch (DOM.eventGetType(event)) {
		// Unhighlight and cancel keyboard events
		case Event.ONBLUR:
			keyTimer.cancel();
			if (slidingMouse) {
				DOM.releaseCapture(getElement());
				slidingMouse = false;
				slideKnob(event);
				stopSliding(true, true);
			} else if (slidingKeyboard) {
				slidingKeyboard = false;
				stopSliding(true, true);
			}
			unhighlight();
			break;
		// Highlight on focus
		case Event.ONFOCUS:
			highlight();
			break;
		// Mousewheel events
		case Event.ONMOUSEWHEEL:
			int velocityY = DOM.eventGetMouseWheelVelocityY(event);
			DOM.eventPreventDefault(event);
			if (velocityY < 0) {
//				 shiftRight(1,true);
			} else {
				// shiftLeft(1,true);
			}
			break;
		// Shift left or right on key press
		case Event.ONKEYDOWN:
			processKeyDown(event);
			break;
		// Stop shifting on key up
		case Event.ONKEYUP:
			keyTimer.cancel();
			if (slidingKeyboard) {
				slidingKeyboard = false;
				stopSliding(true, true);
			}
			break;
		// Mouse Events
		case Event.ONMOUSEDOWN:
		case Event.ONTOUCHSTART:
			setFocus(true);
			slidingMouse = true;
			DOM.setCapture(getElement());
			startSliding(true, true);
			DOM.eventPreventDefault(event);
			slideKnob(event);
			break;
		case Event.ONMOUSEUP:
			// case Event.ONTOUCHEND:
			if (slidingMouse) {
				DOM.releaseCapture(getElement());
				slidingMouse = false;
				slideKnob(event);
				stopSliding(true, true);
			}
			break;
		case Event.ONMOUSEMOVE:
		case Event.ONTOUCHMOVE:
			if (slidingMouse) {
				slideKnob(event);
			}
			break;
		}
		VConsole.log("sliding mouse " + slidingMouse + " slidKey" + slidingKeyboard + " step SZ" + stepSize);
	}

	private void processKeyDown(Event event) {
		if (slidingKeyboard) {
			return;
		}
		int multiplier = 1;
		if (DOM.eventGetCtrlKey(event)) {
			multiplier = (int) (getTotalRange() / stepSize / 10);
		}
		switch (DOM.eventGetKeyCode(event)) {
		case KeyCodes.KEY_HOME:
			DOM.eventPreventDefault(event);
			setCurrentValue(rangeMin, rangeMin, true);
			break;
		case KeyCodes.KEY_END:
			DOM.eventPreventDefault(event);
			setCurrentValue(rangeMax, rangeMax, true);
			break;
		case KeyCodes.KEY_LEFT:
			DOM.eventPreventDefault(event);
			slidingKeyboard = true;
			startSliding(false, true);
			// shiftLeft(multiplier,superImmediate);
			keyTimer.schedule(400, false, multiplier);
			break;
		case KeyCodes.KEY_RIGHT:
			DOM.eventPreventDefault(event);
			slidingKeyboard = true;
			startSliding(false, true);
			// shiftRight(multiplier,superImmediate);
			keyTimer.schedule(400, true, multiplier);
			break;
		case 32:
			DOM.eventPreventDefault(event);
			double half = rangeMin + getTotalRange() / 2;
			setCurrentValue(half, half, true);
			break;
		}
	}

	/**
	 * This method is called when the dimensions of the parent element change.
	 * Subclasses should override this method as needed.
	 * 
	 * @param width
	 *            the new client width of the element
	 * @param height
	 *            the new client height of the element
	 */
	public void onResize(int width, int height) {
		// Center the line in the shell
		int lineWidth = DOM.getElementPropertyInt(lineElement, "offsetWidth");
		lineLeftOffset = (width / 2) - (lineWidth / 2);
		DOM.setStyleAttribute(lineElement, "left", lineLeftOffset + "px");
		// Draw the other components
		drawLabels();
		drawTicks();
		drawKnobAndProgress();
	}

	/**
	 * Redraw the progress bar when something changes the layout.
	 */
	public void redraw() {
		if (isAttached()) {
			int width = DOM.getElementPropertyInt(getElement(), "clientWidth");
			int height = DOM.getElementPropertyInt(getElement(), "clientHeight");
			onResize(width, height);
		}
	}

	// /**
	// * Set the current value and fire the onValueChange event.
	// *
	// * @param curValue
	// * the current value
	// */
	// public void setCurrentValue(double curValue) {
	// setCurrentValue(curValue, true);
	// }

	/**
	 * Set the current value and optionally fire the onValueChange event.
	 * 
	 * @param curValue
	 *            the current value
	 * @param fireEvent
	 *            fire the onValue change event if true
	 */
	public void setCurrentValue(double minCurVal, double maxCurVal, boolean fireEvent) {
		this.setMinCurrValue(confineValue(minCurVal));
		this.maxCurrValue = confineValue(maxCurVal);
		// Redraw the knob and progress bar
		drawKnobAndProgress();
		if (fireEvent) {
			updateValueToServer();
		}
		VConsole.log(" setCurrentValue" + minCurVal + " " + maxCurVal + "" + superImmediate);
	}

	private double confineValue(double value) {
		// Confine the value to the range
		double val = Math.max(rangeMin, Math.min(rangeMax, value));
		double remainder = (val - rangeMin) % stepSize;
		val -= remainder;
		// Go to next step if more than halfway there
		if ((remainder > (stepSize / 2)) && ((val + stepSize) <= rangeMax)) {
			val += stepSize;
		}
		return val;
	}

	private void updateValueToServer() {
		client.updateVariable(id, "knobmin", getMinCurrValue(), false);
		client.updateVariable(id, "knobmax", getMaxCurrValue(), immediate);

	}

	/**
	 * Sets whether this widget is enabled.
	 * 
	 * @param enabled
	 *            true to enable the widget, false to disable it
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (enabled) {
			// images.slider().applyTo(knobImage);
			DOM.setElementProperty(lineElement, "className", SLIDER_BAR_LINE);
		} else {
			// images.sliderDisabled().applyTo(knobImage);
			DOM.setElementProperty(lineElement, "className",
					"gwt-VRangeSliderBar-line gwt-VRangeSliderBar-line-disabled");
		}
		redraw();
	}

	/**
	 * Set the label formatter.
	 * 
	 * @param labelFormatter
	 *            the label formatter
	 */
	public void setLabelFormatter(LabelFormatter labelFormatter) {
		this.labelFormatter = labelFormatter;
	}

	/**
	 * Set the max value.
	 * 
	 * @param rangeMax
	 *            the current value
	 */
	public void setMaxValue(double maxValue) {
		this.rangeMax = maxValue;
		drawLabels();
		resetCurrentValue();
	}

	/**
	 * Set the minimum value.
	 * 
	 * @param rangeMin
	 *            the current value
	 */
	public void setMinValue(double minValue) {
		this.rangeMin = minValue;
		drawLabels();
		resetCurrentValue();
	}

	/**
	 * Set the number of labels to show on the line. Labels indicate the value
	 * of the slider at that point. Use this method to enable labels. If you set
	 * the number of labels equal to the total range divided by the step size,
	 * you will get a properly aligned "jumping" effect where the knob jumps
	 * between labels. Note that the number of labels displayed will be one more
	 * than the number you specify, so specify 1 labels to show labels on either
	 * end of the line. In other words, numLabels is really the number of slots
	 * between the labels. setNumLabels(0) will disable labels.
	 * 
	 * @param numLabels
	 *            the number of labels to show
	 */
	public void setNumLabels(int numLabels) {
		this.numLabels = numLabels;
		drawLabels();
	}

	/**
	 * Set the number of ticks to show on the line. A tick is a vertical line
	 * that represents a division of the overall line. Use this method to enable
	 * ticks. If you set the number of ticks equal to the total range divided by
	 * the step size, you will get a properly aligned "jumping" effect where the
	 * knob jumps between ticks. Note that the number of ticks displayed will be
	 * one more than the number you specify, so specify 1 tick to show ticks on
	 * either end of the line. In other words, numTicks is really the number of
	 * slots between the ticks. setNumTicks(0) will disable ticks.
	 * 
	 * @param numTicks
	 *            the number of ticks to show
	 */
	public void setNumTicks(int numTicks) {
		this.numTicks = numTicks;
		drawTicks();
	}

	/**
	 * Set the step size.
	 * 
	 * @param stepSize
	 *            the current value
	 */
	public void setStepSize(double stepSize) {
		if (Double.compare(0.0, stepSize) >= 0) {
			this.stepSize = 0.5;
		} else {
			this.stepSize = stepSize;
		}
		resetCurrentValue();
	}

	/**
	 * Shift to the left (smaller value).
	 * 
	 * @param numSteps
	 *            the number of steps to shift
	 */
	// public void shiftLeft(int numSteps, boolean updateToserver) {
	// setCurrentValue(getCurrentValue() - numSteps * stepSize, updateToserver);
	// }
	//
	// /**
	// * Shift to the right (greater value).
	// *
	// * @param numSteps
	// * the number of steps to shift
	// */
	// public void shiftRight(int numSteps, boolean updateToserver) {
	// setCurrentValue(getCurrentValue() + numSteps * stepSize, updateToserver);
	// }

	/**
	 * Format the label to display above the ticks Override this method in a
	 * subclass to customize the format. By default, this method returns the
	 * integer portion of the value.
	 * 
	 * @param value
	 *            the value at the label
	 * @return the text to put in the label
	 */
	protected String formatLabel(double value) {
		if (labelFormatter != null) {
			return labelFormatter.formatLabel(this, value);
		}
		return (int) (10 * value) / 10.0 + "";
	}

	/**
	 * Get the percentage of the knob's position relative to the size of the
	 * line. The return value will be between 0.0 and 1.0.
	 * 
	 * @return the current percent complete
	 */
	protected double getKnobPercent(double val) {
		// If we have no range
		if (rangeMax <= rangeMin) {
			return 0;
		}
		// Calculate the relative progress
		double percent = (val - rangeMin) / (rangeMax - rangeMin);
		return Math.max(0.0, Math.min(1.0, percent));
	}

	/**
	 * This method is called immediately after a widget becomes attached to the
	 * browser's document.
	 */
	@Override
	protected void onLoad() {
		// Reset the position attribute of the parent element
		DOM.setStyleAttribute(getElement(), "position", "relative");
		// ResizableWidgetCollection.get().add(this);
		redraw();
	}

	/**
	 * Draw the knob where it is supposed to be relative to the line.
	 */
	private void drawKnobAndProgress() {
		// Abort if not attached
		if (!isAttached()) {
			return;
		}
		// // Move the knob to the correct position
		int lineWidth = DOM.getElementPropertyInt(lineElement, "offsetWidth");

		int knobWidth = DOM.getElementPropertyInt(knobElemMin, "offsetWidth");

		// draw min knob
		int minKnobLeft = (int) (lineLeftOffset + (getKnobPercent(getMinCurrValue()) * lineWidth) - (knobWidth / 2));
		minKnobLeft = Math.min(minKnobLeft, lineLeftOffset + lineWidth - (knobWidth / 2) - 1);
		DOM.setStyleAttribute(knobElemMin, "left", minKnobLeft + "px");

		knobWidth = DOM.getElementPropertyInt(knobElemMax, "offsetWidth");
		int maxknobLeft = (int) (lineLeftOffset + (getKnobPercent(maxCurrValue) * lineWidth) - (knobWidth / 2));
		maxknobLeft = Math.min(maxknobLeft, lineLeftOffset + lineWidth - (knobWidth / 2) - 1);
		DOM.setStyleAttribute(knobElemMax, "left", maxknobLeft + "px");

		//
		DOM.setStyleAttribute(progressElementMin, "left", lineLeftOffset + "px");
		DOM.setStyleAttribute(progressElementMin, "width", 95 * getKnobPercent(getMinCurrValue()) + "%");
		
		DOM.setStyleAttribute(progressElementMax, "right", lineLeftOffset + "px");
		 double rightwidth = 95*  (rangeMax-  getMaxCurrValue() )/ (rangeMax - rangeMin);
		DOM.setStyleAttribute(progressElementMax, "width", rightwidth + "%");
		
		
		
	}

	/**
	 * Draw the labels along the line.
	 */
	private void drawLabels() {
		// Abort if not attached
		if (!isAttached()) {
			return;
		}
		// Draw the labels
		int lineWidth = DOM.getElementPropertyInt(lineElement, "offsetWidth");
		if (numLabels > 0) {
			// Create the labels or make them visible
			for (int i = 0; i <= numLabels; i++) {
				Element label = null;
				if (i < labelElements.size()) {
					label = labelElements.get(i);
				} else { // Create the new label
					label = DOM.createDiv();
					DOM.setStyleAttribute(label, "position", "absolute");
					DOM.setStyleAttribute(label, "display", "none");
					if (enabled) {
						DOM.setElementProperty(label, "className", "gwt-VRangeSliderBar-label");
					} else {
						DOM.setElementProperty(label, "className", "gwt-VRangeSliderBar-label-disabled");
					}
					DOM.appendChild(getElement(), label);
					labelElements.add(label);
				}
				// Set the label text
				double value = rangeMin + (getTotalRange() * i / numLabels);
				DOM.setStyleAttribute(label, "visibility", "hidden");
				DOM.setStyleAttribute(label, "display", "");
				DOM.setElementProperty(label, "innerHTML", formatLabel(value));
				// Move to the left so the label width is not clipped by the
				// shell
				DOM.setStyleAttribute(label, "left", "0px");
				// Position the label and make it visible
				int labelWidth = DOM.getElementPropertyInt(label, "offsetWidth");
				int labelLeftOffset = lineLeftOffset + (lineWidth * i / numLabels) - (labelWidth / 2);
				labelLeftOffset = Math.min(labelLeftOffset, lineLeftOffset + lineWidth - labelWidth);
				labelLeftOffset = Math.max(labelLeftOffset, lineLeftOffset);
				DOM.setStyleAttribute(label, "left", labelLeftOffset + "px");
				DOM.setStyleAttribute(label, "visibility", "visible");
			}
			// Hide unused labels
			for (int i = (numLabels + 1); i < labelElements.size(); i++) {
				DOM.setStyleAttribute(labelElements.get(i), "display", "none");
			}
		} else { // Hide all labels
			for (Element elem : labelElements) {
				DOM.setStyleAttribute(elem, "display", "none");
			}
		}
	}

	/**
	 * Draw the tick along the line.
	 */
	private void drawTicks() {
		// Abort if not attached
		if (!isAttached()) {
			return;
		}
		// Draw the ticks
		int lineWidth = DOM.getElementPropertyInt(lineElement, "offsetWidth");
		if (numTicks > 0) {
			// Create the ticks or make them visible
			for (int i = 0; i <= numTicks; i++) {
				Element tick = null;
				if (i < tickElements.size()) {
					tick = tickElements.get(i);
				} else {
					// Create the new tick
					tick = DOM.createDiv();
					DOM.setStyleAttribute(tick, "position", "absolute");
					DOM.setStyleAttribute(tick, "display", "none");
					DOM.appendChild(getElement(), tick);
					tickElements.add(tick);
				}
				if (enabled) {
					DOM.setElementProperty(tick, "className", "gwt-VRangeSliderBar-tick");
				} else {
					DOM.setElementProperty(tick, "className",
							"gwt-VRangeSliderBar-tick gwt-VRangeSliderBar-tick-disabled");
				}
				// Position the tick and make it visible
				DOM.setStyleAttribute(tick, "visibility", "hidden");
				DOM.setStyleAttribute(tick, "display", "");
				int tickWidth = DOM.getElementPropertyInt(tick, "offsetWidth");
				int tickLeftOffset = lineLeftOffset + (lineWidth * i / numTicks) - (tickWidth / 2);
				tickLeftOffset = Math.min(tickLeftOffset, lineLeftOffset + lineWidth - tickWidth);
				DOM.setStyleAttribute(tick, "left", tickLeftOffset + "px");
				DOM.setStyleAttribute(tick, "visibility", "visible");
			}
			// Hide unused ticks
			for (int i = (numTicks + 1); i < tickElements.size(); i++) {
				DOM.setStyleAttribute(tickElements.get(i), "display", "none");
			}
		} else { // Hide all ticks
			for (Element elem : tickElements) {
				DOM.setStyleAttribute(elem, "display", "none");
			}
		}
	}

	/**
	 * Highlight this widget.
	 */
	private void highlight() {
		String styleName = getStylePrimaryName();
		DOM.setElementProperty(getElement(), "className", styleName + " " + styleName + "-focused");
	}

	/**
	 * Reset the progress to constrain the progress to the current range and
	 * redraw the knob as needed.
	 */
	private void resetCurrentValue() {
		setCurrentValue(getMinCurrValue(), getMaxCurrValue(), true);
	}

	/**
	 * Slide the knob to a new location.
	 * 
	 * @param event
	 *            the mouse event
	 */
	private void slideKnob(Event event) {
	
		
		int pointClicked = DOM.eventGetClientX(event);

		
		int minLeft = DOM.getAbsoluteLeft(knobElemMin);
		int maxLeft = DOM.getAbsoluteLeft(knobElemMax);

		VConsole.log("VRangeSliderBar.onBrowserEvent()" + " " + minLeft + " " + maxLeft);

		if (pointClicked <= minLeft) {
			_target = knobElemMin;
		}
		//
		else if (pointClicked >= maxLeft) {
			_target = knobElemMax;
		} else {
			
			
			int inbtw = minLeft +( (maxLeft- minLeft) / 2);
			if(pointClicked <= inbtw) {
				_target=knobElemMin;
			}
			else {
				
				_target = knobElemMax;
			}
			
		}

		VConsole.log("target to move is  " + _target.getClassName());
		if (pointClicked > 0) {
			int lineWidth = DOM.getElementPropertyInt(lineElement, "offsetWidth");
			int lineLeft = DOM.getAbsoluteLeft(lineElement);
			double percent = (double) (DOM.eventGetClientX(event) - lineLeft) / lineWidth * 1.0;

			if (_target == knobElemMin) {
				setCurrentValue(getTotalRange() * percent + rangeMin, this.maxCurrValue, superImmediate);
			}
			// max target
			else if (_target == knobElemMax) {
				setCurrentValue(this.minCurrValue, getTotalRange() * percent + rangeMin, superImmediate);

			}
		}
		
		if( minCurrValue > maxCurrValue) {
			VConsole.log(new Throwable("min cant be greater than max pos min="+ minLeft + " max is "+ maxLeft));
			
		}
		
		
		
	}

	/**
	 * Start sliding the knob.
	 * 
	 * @param highlight
	 *            true to change the style
	 * @param fireEvent
	 *            true to fire the event
	 */
	private void startSliding(boolean highlight, boolean fireEvent) {
		if (highlight) {

			if (_target == knobElemMin) {
				DOM.setElementProperty(knobElemMin, "className", KNOB_MIN_CLASSNAME);

			} else if (_target == knobElemMax) {
				DOM.setElementProperty(knobElemMax, "className", KNOB_MAX_CLASSNAME);

			}
			DOM.setElementProperty(lineElement, "className",
					"gwt-VRangeSliderBar-line gwt-VRangeSliderBar-line-sliding");
			// DOM.setElementProperty(knobElemMin, "className",
			// "gwt-VRangeSliderBar-knob gwt-VRangeSliderBar-knob-sliding");

		}
	}

	/**
	 * Stop sliding the knob.
	 * 
	 * @param unhighlight
	 *            true to change the style
	 * @param fireEvent
	 *            true to fire the event
	 */
	private void stopSliding(boolean unhighlight, boolean fireEvent) {
		if (unhighlight) {

			if (_target == knobElemMin) {
				DOM.setElementProperty(knobElemMin, "className", KNOB_MIN_CLASSNAME + "-inactive");

			} else if (_target == knobElemMax) {
				DOM.setElementProperty(knobElemMax, "className", KNOB_MAX_CLASSNAME + "-inactive");

			}

			DOM.setElementProperty(lineElement, "className", SLIDER_BAR_LINE);

		}

		if (fireEvent) {
			updateValueToServer();
		}

	}

	/**
	 * Unhighlight this widget.
	 */
	private void unhighlight() {
		DOM.setElementProperty(getElement(), "className", getStylePrimaryName());
	}

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection connClient) {
		this.client = connClient;
		id = uidl.getId();
		// Ensure correct implementation
		if (connClient.updateComponent(this, uidl, true)) {
			return;
		}
		superImmediate = uidl.getBooleanAttribute("superimmediate");
		immediate = uidl.getBooleanAttribute("immediate");
		readonly = uidl.getBooleanAttribute("readonly");
		boolean disables = uidl.getBooleanAttribute("disabled");
		double min = uidl.getDoubleAttribute("rangeMin");
		double max = uidl.getDoubleAttribute("rangeMax");
		int ticks = uidl.getIntAttribute("numticks");
		int numLabel = uidl.getIntAttribute("numlabels");
		double resolution = uidl.getDoubleAttribute("stepsize");
		this.stepSize = (resolution);
		this.rangeMin = (min);
		this.rangeMax = (max);

		this.minCurrValue = uidl.getDoubleAttribute("knobmin");
		this.maxCurrValue = uidl.getDoubleAttribute("knobmax");
		
		appendString = uidl.getStringAttribute("append");

		setEnabled(!disables);
		setNumLabels(numLabel);
		setNumTicks(ticks);
		
		setCurrentValue(minCurrValue, maxCurrValue, false);

//		VConsole.log("update from uidl range " + rangeMin + "    min =" + rangeMax);
//		VConsole.log("update from uidl currt " + minCurrValue + " max " + maxCurrValue);
		
		
	}

	@Override
	public void iLayout() {
		// Update handle position
		setCurrentValue(getMinCurrValue(), getMaxCurrValue(), false);
	}

	// GETTERS
	/**
	 * Return the current value.
	 * 
	 * @return the current value
	 */
	// public double getCurrentValue() {
	// return curValue;
	// }

	/**
	 * Return the label formatter.
	 * 
	 * @return the label formatter
	 */
	public LabelFormatter getLabelFormatter() {
		return labelFormatter;
	}

	/**
	 * Return the max value.
	 * 
	 * @return the max value
	 */
	public double getMaxValue() {
		return rangeMax;
	}

	/**
	 * Return the minimum value.
	 * 
	 * @return the minimum value
	 */
	public double getMinValue() {
		return rangeMin;
	}

	/**
	 * Return the number of labels.
	 * 
	 * @return the number of labels
	 */
	public int getNumLabels() {
		return numLabels;
	}

	/**
	 * Return the number of ticks.
	 * 
	 * @return the number of ticks
	 */
	public int getNumTicks() {
		return numTicks;
	}

	/**
	 * Return the step size.
	 * 
	 * @return the step size
	 */
	public double getStepSize() {
		return stepSize;
	}

	/**
	 * @return Gets whether this widget is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public void setMinCurrValue(double minCurrValue) {
		this.minCurrValue = minCurrValue;
	}

	public double getMinCurrValue() {
		return minCurrValue;
	}

	public void setMaxCurrValue(double maxCurrValue) {
		this.maxCurrValue = maxCurrValue;
	}

	public double getMaxCurrValue() {
		return maxCurrValue;
	}

	public String getAppendString() {
		return appendString;
	}

	public void setAppendString(String appendString) {
		this.appendString = appendString;
	}

	/**
	 * The timer used to continue to shift the knob as the user holds down one
	 * of the left/right arrow keys. Only IE auto-repeats, so we just keep
	 * catching the events.
	 */
	private  class KeyTimer extends Timer {
		/**
		 * A bit indicating that this is the run.
		 */
		private boolean firstRun = true;
		/**
		 * The delay between shifts, which shortens as the user holds down the
		 * button.
		 */
		private int repeatDelay = 30;
		/**
		 * A bit indicating whether we are shifting to a higher or lower value.
		 */
		private boolean shiftRight = false;
		/**
		 * The number of steps to shift with each press.
		 */
		private int multiplier = 1;

		/**
		 * This method will be called when a timer fires. Override it to
		 * implement the timer's logic.
		 */
		@Override
		public void run() {
			// Highlight the knob on _min run
			if (firstRun) {
				firstRun = false;
				startSliding(true, false);
			}
			// Slide the slider bar
			if (shiftRight) {
				// setCurrentValue(curValue + multiplier * stepSize,
				// superImmediate);
			} else {
				// setCurrentValue(curValue - multiplier *
				// stepSize,superImmediate);
			}
			// Repeat this timer until cancelled by keyup event
			schedule(repeatDelay);
		}

		/**
		 * Schedules a timer to elapse in the future.
		 * 
		 * @param delayMillis
		 *            how long to wait before the timer elapses, in milliseconds
		 * @param shiftRight
		 *            whether to shift up or not
		 * @param multiplier
		 *            the number of steps to shift
		 */
		public void schedule(int delayMillis, boolean shiftRight, int multiplier) {
			firstRun = true;
			this.shiftRight = shiftRight;
			this.multiplier = multiplier;
			super.schedule(delayMillis);
		}
	}
}