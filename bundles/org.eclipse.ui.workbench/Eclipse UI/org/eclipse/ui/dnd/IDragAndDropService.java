/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.dnd;

import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;

/**
 * This interface specifies the API for a service to be used by part authors to
 * access methods which provide support for Drag and Drop operations within the
 * workbench.
 * <p>
 * Authors should access this service using the PartSite's
 * <code>getService</code> method, passing this interface as the argument.
 * </p>
 * <p>
 * <b>NOTE:</b> This interface it not expected to be implemented by clients; it
 * is provided only to allow access to the service's methods.
 * </p>
 * <p>
 * 
 * @since 3.3
 * 
 */
public interface IDragAndDropService {
	/**
	 * Causes a drop target to be added to the given control that respects the
	 * existing site's drop behaviour in addition to the behaviour being
	 * specified for the given control.
	 * <p>
	 * If a transfer type specified for the control matches one used by the site
	 * then the control's listener is called (the client is overriding the
	 * existing site behaviour which will no longer work).
	 * </p>
	 * <p>
	 * NOTE: Site authors <b>must</b> use this method to add drop behaviour;
	 * directly adding drop targets using SWT onto a site will cause the
	 * standard site behaviour (i.e. dragging files / markers into the
	 * EditorSite...) to not work when that editor is active.
	 * </p>
	 * <p>
	 * Note that this method may be used more than once should the part author
	 * wish to register different drop targets for internal controls (i.e. to
	 * support internal DnD).
	 * </p>
	 * <p>
	 * 
	 * @param control
	 *            The control to add the drop behaviour to
	 * @param ops
	 *            The Drop operations used by this target
	 * @param transfers
	 *            The TransferTypes used by this target
	 * @param listener
	 *            The listener controlling the target's behaviour
	 */
	public void addMergedDropTarget(Control control, int ops, Transfer[] transfers, DropTargetListener listener);
	
	/**
	 * Remove any previously 'merged' drop target for this Control
	 * 
	 * @param control The control to remove the drop target for
	 */
	public void removeMergedDropTarget(Control control);
}
