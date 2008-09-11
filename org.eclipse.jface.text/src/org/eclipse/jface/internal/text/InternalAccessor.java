/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text;

import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension3;
import org.eclipse.jface.text.ITextViewerExtension8;
import org.eclipse.jface.text.ITextViewerExtension8.EnrichMode;


/**
 * An internal class that gives access to internal methods of {@link
 * AbstractInformationControlManager} and subclasses.
 *
 * @since 3.4
 */
public abstract class InternalAccessor {

	/**
	 * Returns the current information control, or <code>null</code> if none.
	 *
	 * @return the current information control, or <code>null</code> if none
	 */
	public abstract IInformationControl getCurrentInformationControl();

	/**
	 * Sets the information control replacer for this manager and disposes the
	 * old one if set.
	 *
	 * @param replacer the information control replacer for this manager, or
	 *            <code>null</code> if no information control replacing should
	 *            take place
	 */
	public abstract void setInformationControlReplacer(InformationControlReplacer replacer);

	/**
	 * Returns the current information control replacer or <code>null</code> if none has been installed.
	 *
	 * @return the current information control replacer or <code>null</code> if none has been installed
	 */
	public abstract InformationControlReplacer getInformationControlReplacer();

	/**
	 * Tests whether the given information control is replaceable.
	 *
	 * @param iControl information control or <code>null</code> if none
	 * @return <code>true</code> if information control is replaceable, <code>false</code> otherwise
	 */
	public abstract boolean canReplace(IInformationControl iControl);

	/**
	 * Tells whether this manager's information control is currently being replaced.
	 *
	 * @return <code>true</code> if a replace is in progress
	 */
	public abstract boolean isReplaceInProgress();

	/**
	 * Crops the given bounds such that they lie completely on the closest monitor.
	 *
	 * @param bounds shell bounds to crop
	 */
	public abstract void cropToClosestMonitor(Rectangle bounds);

	/**
	 * Sets the hover enrich mode. Only applicable when an information
	 * control replacer has been set with
	 * {@link #setInformationControlReplacer(InformationControlReplacer)} .
	 *
	 * @param mode the enrich mode
	 * @see ITextViewerExtension8#setHoverEnrichMode(org.eclipse.jface.text.ITextViewerExtension8.EnrichMode)
	 */
	public abstract void setHoverEnrichMode(EnrichMode mode);

	/**
	 * Indicates whether the mouse cursor is allowed to leave the subject area without closing the hover.
	 *
	 * @return whether the mouse cursor is allowed to leave the subject area without closing the hover
	 */
	public abstract boolean getAllowMouseExit();

	/**
	 * Replaces this manager's information control as defined by
	 * the information control replacer.
	 * <strong>Must only be called when the information control is instanceof {@link IInformationControlExtension3}!</strong>
	 *
	 * @param takeFocus <code>true</code> iff the replacing information control should take focus
	 */
	public abstract void replaceInformationControl(boolean takeFocus);

}
