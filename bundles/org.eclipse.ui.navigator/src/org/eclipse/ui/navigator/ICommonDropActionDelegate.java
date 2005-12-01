/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.ui.navigator.internal.dnd.CommonNavigatorDropAdapter;
import org.eclipse.ui.part.IDropActionDelegate;

/**
 * <p>
 * Used by the <b>org.eclipse.ui.navigator.dropHandler</b> extension point to carry
 * out pluggable Drag and Drop actions.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 *<p>
 * Clients may implement this interface.
 *</p> 
 * @since 3.2
 *  
 */
public interface ICommonDropActionDelegate extends IDropActionDelegate {

	/**
	 * <p>
	 * Perform any necessary initialization using the {@link CommonViewer}.
	 * </p>
	 * 
	 * @param aViewer
	 *            The instance of {@link CommonViewer}that the current ICommonDropActionDelegate
	 *            will be associated with
	 */
	void init(CommonViewer aViewer);

	/**
	 * Carry out the DND operation
	 * 
	 * @param operation
	 *            one of DND.DROP_MOVE|DND.DROP_COPY|DND.DROP_LINK
	 * @param source
	 *            The object being dragged
	 * @param target
	 *            The object being dragged onto
	 * @return
	 */
	boolean run(CommonNavigatorDropAdapter dropAdapter, Object source, Object target);
}
