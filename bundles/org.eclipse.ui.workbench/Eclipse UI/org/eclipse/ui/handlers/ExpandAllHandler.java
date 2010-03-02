/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *     @author Prakash G. R. 
 ******************************************************************************/

package org.eclipse.ui.handlers;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.ui.IWorkbenchCommandConstants;

/**
 * Expand a tree viewer.
 * <p>
 * It can be used in a part's createPartControl(Composite) method:
 * 
 * <pre>
 * IHandlerService handlerService = (IHandlerService) getSite().getService(
 * 		IHandlerService.class);
 * expandHandler = new ExpandAllHandler(myViewer);
 * handlerService.activateHandler(ExpandAllHandler.COMMAND_ID, expandHandler);
 * </pre>
 * 
 * The part should dispose the handler in its own dispose() method. The part can
 * provide its own expand all handler if desired, or if it needs to delegate to
 * multiple tree viewers.
 * </p>
 * <p>
 * <b>Note</b>: This class can be instantiated. It should not be subclasses.
 * </p>
 * 
 * @since 3.6
 */
public class ExpandAllHandler extends AbstractHandler {
	/**
	 * The command id for collapse all.
	 */
	public static final String COMMAND_ID = IWorkbenchCommandConstants.NAVIGATE_EXPAND_ALL;

	private AbstractTreeViewer treeViewer;

	/**
	 * Create the handler for this tree viewer.
	 * 
	 * @param viewer
	 *            The viewer to expand. Must not be <code>null</code>.
	 */
	public ExpandAllHandler(AbstractTreeViewer viewer) {
		Assert.isNotNull(viewer);
		treeViewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) {
		treeViewer.expandAll();
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
