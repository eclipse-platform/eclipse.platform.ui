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
package org.eclipse.jface.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.resource.JFaceResources;


/**
 * Extension interface for {@link org.eclipse.jface.text.IInformationControl}.
 * Adds API
 * <ul>
 * <li>to test the visibility of the control,</li>
 * <li>to test whether another control is a child of the information control,</li>
 * <li>to compute size constraints based on the information control's main font and</li>
 * <li>to return a control creator for an enriched version of this information control.</li>
 * </ul>
 *
 * <p>
 * <b>Important:</b> Enriching this information control only works properly if
 * {@link IInformationControl#isFocusControl()} is implemented like this (<code>fShell</code>
 * is the control's shell):
 *
 * <pre>
 * return fShell.getDisplay().getActiveShell() == fShell
 * </pre>
 * Likewise,
 * {@link IInformationControl#addFocusListener(org.eclipse.swt.events.FocusListener)}
 * should install listeners for {@link SWT#Activate} and {@link SWT#Deactivate}
 * on the shell and forward events to the focus listeners. Clients are
 * encouraged to subclass {@link AbstractInformationControl}, which does this
 * for free.
 * </p>
 *
 * @see org.eclipse.jface.text.IInformationControl
 * @since 3.4
 */
public interface IInformationControlExtension5 {

	/**
	 * Tests whether the given control is this information control
	 * or a child of this information control.
	 *
	 * @param control the control to test
	 * @return <code>true</code> iff the given control is this information control
	 * or a child of this information control
	 */
	public boolean containsControl(Control control);

	/**
	 * @return <code>true</code> iff the information control is currently visible
	 */
	public abstract boolean isVisible();

	/**
	 * Computes the width- and height constraints of the information control in
	 * pixels, based on the given width and height in characters. Implementors
	 * should use the main font of the information control to do the
	 * characters-to-pixels conversion. This is typically the
	 * {@link JFaceResources#getDialogFont() dialog font}.
	 *
	 * @param widthInChars the width constraint in number of characters
	 * @param heightInChars the height constraint in number of characters
	 * @return a point with width and height in pixels, or <code>null</code>
	 *         to use the subject control's font to calculate the size
	 */
	public Point computeSizeConstraints(int widthInChars, int heightInChars);

	/**
	 * Returns the rich information control creator for this information control.
	 * <p>
	 * The returned information control creator is used to create an enriched version of this
	 * information control, e.g. when the mouse is moved into this control and it needs to be
	 * {@link ITextViewerExtension8#setHoverEnrichMode(org.eclipse.jface.text.ITextViewerExtension8.EnrichMode) enriched}
	 * or when it needs to be made sticky for other reasons.
	 * </p>
	 * <p>
	 * The returned information control creator must create information controls
	 * that implement {@link IInformationControlExtension3} and {@link IInformationControlExtension2},
	 * and whose {@link IInformationControlExtension2#setInput(Object)} accepts all inputs that are
	 * also supported by this information control.
	 * </p>
	 * <p>
	 * Note that automatic enriching of this information control works only if it also implements
	 * {@link IInformationControlExtension3}.
	 * </p>
	 * <p>
	 * This method may be called frequently, so implementors should ensure it returns quickly,
	 * e.g. by caching the returned creator.
	 * </p>
	 *
	 * @return the information presenter control creator or <code>null</code> to disable enriching
	 */
	IInformationControlCreator getInformationPresenterControlCreator();

}
