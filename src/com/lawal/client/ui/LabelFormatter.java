package com.lawal.client.ui;


/**
   * A formatter used to format the labels displayed in the widget.
   */
  public abstract class  LabelFormatter {
    /**
     * Generate the text to display in each label based on the label's value.
     * 
     * Override this method to change the text displayed within the RangeSliderBar.
     * 
     * @param slider the Slider bar
     * @param value the value the label displays
     * @return the text to display for the label
     */
   public abstract String formatLabel(VRangeSliderBar slider, double value);
   
   public static LabelFormatter getDefault() {
	   return new LabelFormatter() {
		@Override
		public String formatLabel(VRangeSliderBar slider, double value) {
			return String.valueOf(value)+ slider.getAppendString();
		}
	};
   }
    
  }