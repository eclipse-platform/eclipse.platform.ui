/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.swt.SWT;
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
 * <em>If this information control is used by a {@link AbstractHoverInformationControlManager}
 * then that manager will own this control and override any properties that
 * may have been set before by any other client.</em></p>
 * <p>
 * The information control must not grab focus when made visible using
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
 * <li>{@link org.eclipse.jface.text.IInformationControlExtension3} since
 *     version 3.0 providing access to the control's bounds and introducing
 *     the concept of persistent size and location.</li>
 * <li>{@link org.eclipse.jface.text.IInformationControlExtension4} since
 *     version 3.3, adding API which allows to set this information control's status field text.</li>
 * <li>{@link org.eclipse.jface.text.IInformationControlExtension5} since
 *     version 3.4, adding API to get the visibility of the control, to
 *     test whether another control is a child of the information control,
 *     to compute size constraints based on the information control's main font
 *     and to return a control creator for an enriched version of this information control.</li>
 * </ul>
 * <p>
 * Clients can implement this interface and its extension interfaces,
 * subclass {@link AbstractInformationControl}, or use the (text-based)
 * default implementation {@link DefaultInformationControl}.
 *
 * @see org.eclipse.jface.text.IInformationControlExtension
 * @see org.eclipse.jface.text.IInformationControlExtension2
 * @see org.eclipse.jface.text.IInformationControlExtension3
 * @see org.eclipse.jface.text.IInformationControlExtension4
 * @see org.eclipse.jface.text.IInformationControlExtension5
 * @see AbstractInformationControl
 * @see DefaultInformationControl
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
	 * {@link SWT#DEFAULT} indicates no constraint. This method must be called before
	 * {@link #computeSizeHint()} is called.
	 * <p>
	 * Note: An information control which implements {@link IInformationControlExtension3}
	 * may ignore this method or use it as hint for its very first appearance.
	 * </p>
	 * @param maxWidth the maximal width of the control  to present the information, or {@link SWT#DEFAULT} for not constraint
	 * @param maxHeight the maximal height of the control to present the information, or {@link SWT#DEFAULT} for not constraint
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
	 * <p>
	 * <strong>Note:</strong> The information control must not grab focus when
	 * made visible.
	 * </p>
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
	 * Returns whether this information control (or one of its children) has the focus.
	 * The suggested implementation is like this (<code>fShell</code> is this information control's shell):
	 * <pre>return fShell.getDisplay().getActiveShell() == fShell</pre>
	 *
	 * @return <code>true</code> when the information control has the focus, otherwise <code>false</code>
	 */
	boolean isFocusControl();

	/**
	 * Sets the keyboard focus to this information control.
	 */
	void setFocus();

	/**
	 * Adds the given listener to the list of focus listeners.
	 * If the listener is already registered it is not registered again.
	 * <p>
	 * The suggested implementation is to install listeners for {@link SWT#Activate} and {@link SWT#Deactivate}
	 * on the shell and forward events to the focus listeners. Clients are
	 * encouraged to subclass {@link AbstractInformationControl}, which does this
	 * for free.
	 * </p>
	 *
	 * @param listener the listener to be added
	 */
	void addFocusListener(FocusListener listener);

	/**
	 * Removes the given listeners from the list of focus listeners.
	 * If the listener is not registered this call has no effect.
	 *
	 * @param listener the listener to be removed
	 */
	void removeFocusListener(FocusListener listener);
}
