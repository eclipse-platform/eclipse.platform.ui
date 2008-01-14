/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.AbstractTreeViewer;

/**
 * Collapse a tree viewer.
 * <p>
 * It can be used in a part's createPartControl(Composite) method:
 * 
 * <pre>
 * IHandlerService handlerService = (IHandlerService) getSite().getService(
 * 		IHandlerService.class);
 * collapseHandler = new CollapseAllHandler(myViewer);
 * handlerService.activateHandle(CollapseAllHandler.COMMAND_ID, collapseHandler);
 * </pre>
 * 
 * The part should dispose the handler in its own dispose() method. The part
 * can provide its own collapse all handler if desired, or if it needs to
 * delegate to multiple tree viewers.
 * </p>
 * <p>
 * <b>Note</b>: This class can be instantiated. It should not be subclasses.
 * </p>
 * 
 * @since 3.4
 */
public class CollapseAllHandler extends AbstractHandler {
	/**
	 * The command id for collapse all.
	 */
	public static final String COMMAND_ID = "org.eclipse.ui.navigate.collapseAll"; //$NON-NLS-1$

	private AbstractTreeViewer treeViewer;

	/**
	 * Create the handler for this tree viewer.
	 * 
	 * @param viewer
	 *            The viewer to collapse. Must not be <code>null</code>.
	 */
	public CollapseAllHandler(AbstractTreeViewer viewer) {
		Assert.isNotNull(viewer);
		treeViewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) {
		treeViewer.collapseAll();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#dispose()
	 */
	public void dispose() {
		treeViewer = null;
	}
}
