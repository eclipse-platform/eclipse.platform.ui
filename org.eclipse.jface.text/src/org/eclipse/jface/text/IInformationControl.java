/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Interface of a control presenting information. The information is given in
 * the form of an input object. It can be either the content itself or a
 * description of the content. The specification of what is required from an
 * input object is left to the implementers of this interface.
 * <p>
 * The information control may not grab focus when made visible using
 * <code>setVisible(true)</code>.
 *
 * In order to provide backward compatibility for clients of
 * <code>IInformationControl</code>, extension interfaces are used as a means
 * of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jface.text.IInformationControlExtension} since
 *     version 2.0 introducing the predicate of whether the control has anything to
 *     show or would be empty</li>
 * <li>{@link org.eclipse.jface.text.IInformationControlExtension2} since
 *     version 2.1 replacing the original concept of textual input by general input
 *     objects.</li>
 * <li>{@link org.eclipse.jface.text.IInformationControlExtension3}since
 *     version 3.0 providing access to the control's bounds and introducing
 *     the concept of persistent size and location.</li>
 * </ul>
 * <p>
 * Clients can implements that interface and its extension interfaces or use the
 * provided default implementation {@link org.eclipse.jface.text.DefaultInformationControl}.
 *
 * @see org.eclipse.jface.text.IInformationControlExtension
 * @see org.eclipse.jface.text.IInformationControlExtension2
 * @see org.eclipse.jface.text.IInformationControlExtension3
 * @since 2.0
 */
public interface IInformationControl {

	/**
	 * Sets the information to be presented by this information control.
	 * <p>
	 * Replaced by {@link IInformationControlExtension2#setInput(Object)}.
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
	 * may ignore this method or use it as hint for its very first appearance.
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
