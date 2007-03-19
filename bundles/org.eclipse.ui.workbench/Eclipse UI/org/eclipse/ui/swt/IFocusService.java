/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.swt;

import org.eclipse.swt.widgets.Control;

/**
 * Tracks focusGained and focusLost events for a Control registered with this
 * service, and provides the control and its registered ID as variables to the
 * application evaluation context for evaluation be the various services.
 * <p>
 * This service provides 2 variables, activeFocusControl (a Control) and
 * activeFocusControlId (the ID registered with the service).
 * </p>
 * <p>
 * You can use this service to provide default cut/copy/paste/selectAll for
 * specific text controls outside of the normal workbench part lifecycle, like a
 * control contributed to the trim. For example:
 * </p>
 * <p>
 * 
 * <pre>
 * &lt;handler
 *      class=&quot;org.eclipse.ui.internal.handlers.WidgetMethodHandler:paste&quot;
 *      commandId=&quot;org.eclipse.ui.edit.paste&quot;&gt;
 *   &lt;activeWhen&gt;
 *      &lt;with variable=&quot;activeFocusControlId&quot;&gt;
 *         &lt;equals value=&quot;org.eclipse.ui.tests.focusText&quot;/&gt;
 *      &lt;/with&gt;
 *   &lt;/activeWhen&gt;
 * &lt;/handler&gt;
 * </pre>
 * 
 * </p>
 * 
 * @see org.eclipse.ui.ISources
 * @since 3.3
 */
public interface IFocusService {
	/**
	 * Use the value to provide default copy behaviour in a handler element
	 * class attribute.
	 */
	public static final String COPY_HANDLER = "org.eclipse.ui.internal.handlers.WidgetMethodHandler:copy"; //$NON-NLS-1$

	/**
	 * Use the value to provide default paste behaviour in a handler element
	 * class attribute.
	 */
	public static final String PASTE_HANDLER = "org.eclipse.ui.internal.handlers.WidgetMethodHandler:paste"; //$NON-NLS-1$

	/**
	 * Use the value to provide default cut behaviour in a handler element class
	 * attribute.
	 */
	public static final String CUT_HANDLER = "org.eclipse.ui.internal.handlers.WidgetMethodHandler:cut"; //$NON-NLS-1$

	/**
	 * Use the value to provide default select all behaviour in a handler
	 * element class attribute.
	 */
	public static final String SELECT_ALL_HANDLER = "org.eclipse.ui.internal.handlers.SelectAllHandler"; //$NON-NLS-1$

	/**
	 * A Control for which the service will track focus. When in focus, this
	 * Control and its ID will be provided as variables to core expressions for
	 * the various services, as activeFocusControl and activeFocusControlId
	 * respectively.
	 * <p>
	 * A control must only be registered once, but different controls can be
	 * registered with the same ID. Expressions evaluated against the
	 * activeFocusControlId would then be true for all of the controls thus
	 * registered.
	 * </p>
	 * <p>
	 * We will remove ourselves as a listener when the Control is disposed.
	 * </p>
	 * 
	 * @param control
	 *            the control. Must not be <code>null</code>. If the control
	 *            is already registered with this service this call is a no-op.
	 * @param id
	 *            an ID for this control. Must not be <code>null</code>.
	 */
	public void addFocusTracker(Control control, String id);

	/**
	 * No longer track focus events for this control. Use this method when the
	 * control should no longer be tracked, but is not disposed.
	 * 
	 * @param control
	 *            the control registered with the service. Must not be
	 *            <code>null</code>.
	 */
	public void removeFocusTracker(Control control);
}
