/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.ui.navigator.internal.dnd.CommonNavigatorDropAdapter;
import org.eclipse.ui.part.IDropActionDelegate;

/**
 * <p>
 * Used by the <b>org.eclipse.wst.common.navigator.views.dropHandler</b> extension point to carry
 * out pluggable Drag and Drop actions.
 * </p>
 * <p>
 * This interface is experimental and is subject to change.
 * </p>
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