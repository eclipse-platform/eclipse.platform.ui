package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;


/**
 * Interface of a control presenting information. The information is given
 * in textual form. Thus, it can either be the content itself or a description
 * of the content.<p>
 * The information control may not grap focus when made visible 
 * using <code>setVisible(true)</code>.
 */
public interface IInformationControl {

	/**
	 * Sets the information to be presented in this information control.
	 * 
	 * @param information the information to be presented
	 */
	void setInformation(String information);
	
	/**
	 * Sets the information control's size constraints. A constraint value of -1 indicates 
	 * no constraint. This method is called before <code>computeSizeHint</code>
	 * is called.
	 * 
	 * @param maxWidth the maximal width of the control  to present the information, or -1 for not constraint
	 * @param maxHeight the maximal height of the control to present the information, or -1 for not constraint
	 */
	void setSizeConstraints(int maxWidth, int maxHeight);
	
	/**
	 * Computes and returns a proposal for its size depending on the
	 * information to present. The method tries to honor the previously
	 * set constraints but might returns a size that exceeds them. 
	 * 
	 * @return the computed size hint
	 */
	Point computeSizeHint();
	
	/**
	 * Controls the visibility of this information control.
	 * 
	 * @param visible <code>true</code> if the control should be visible
	 */
	void setVisible(boolean visible);
	
	/**
	 * Sets the size of this information control.
	 * 
	 * @param width the width of the control
	 * @param height the height of the control
	 */
	void setSize(int width, int height);
	
	/**
	 * Sets the location of this information control.
	 * 
	 * @param location the location
	 */
	void setLocation(Point location);
	
	/**
	 * Disposes this information control.
	 */
	void dispose();
	
	/**
	 * Adds the given listener to the list of dispose listeners. 
	 * If the listener is already registered it is not registered again.
	 * 
	 * @param listener the listener to be added
	 */
	void addDisposeListener(DisposeListener listener);
	
	/**
	 * Removes the given listeners from the list of dispose listeners.
	 * If the listener is not registered this call has no affect.
	 * 
	 * @param listener the listener to be removed 
	 */
	void removeDisposeListener(DisposeListener listener);
	
	/**
	 * Sets the foreground color of this information control.
	 * 
	 * @param foreground the foreground color of this information control
	 */
	void setForegroundColor(Color foreground);
	
	/**
	 * Sets the background color of this information control.
	 * 
	 * @param background the background color of this information control
	 */
	void setBackgroundColor(Color background);
	
	/**
	 * Returns whether this information control has the focus.
	 * 
	 * @return whether this information control has the focus
	 */
	boolean isFocusControl();	
}

