/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;


import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;


/**
 * Interface of a control presenting information. The information is given
 * in textual form. It can  be either the content itself or a description
 * of the content. This specification is left to the implementers of this interface.<p>
 * The information control may not grap focus when made visible  using
 * <code>setVisible(true)</code>.
 * 
 * @since 2.0
 */
public interface IInformationControl {

	/**
	 * Sets the information to be presented by this information control.
	 * 
	 * @param information the information to be presented
	 */
	void setInformation(String information);
	
	/**
	 * Sets the information control's size constraints. A constraint value of
	 * <code>-1</code> indicates no constraint. This method must be called before 
	 * <code>computeSizeHint</code> is called.
	 * <p>
	 * Note: An information control which implements {@link IInformationControlExtension3}
	 * can ignore this method or use it as hint for its very first appearance.
	 * </p>
	 * @param maxWidth the maximal width of the control  to present the information, or <code>-1</code> for not constraint
	 * @param maxHeight the maximal height of the control to present the information, or <code>-1</code> for not constraint
	 */
	void setSizeConstraints(int maxWidth, int maxHeight);
	
	/**
	 * Computes and returns a proposal for the size of this information control depending
	 * on the information to present. The method tries to honor known size constraints but might 
	 * return a size that exceeds them. 
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
	 * If the listener is not registered this call has no effect.
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
	 * @return <code>true</code> when the information control has the focus otherwise <code>false</code>
	 */
	boolean isFocusControl();
	
	/**
	 * Sets the keyboard focus to this information control.
	 */
	void setFocus();
	
	/**
	 * Adds the given listener to the list of focus listeners. 
	 * If the listener is already registered it is not registered again.
	 * 
	 * @param listener the listener to be added
	 */
	void addFocusListener(FocusListener listener);
	
	/**
	 * Removes the given listeners from the list of focus listeners.
	 * If the listener is not registered this call has no affect.
	 * 
	 * @param listener the listener to be removed 
	 */
	void removeFocusListener(FocusListener listener);
}
