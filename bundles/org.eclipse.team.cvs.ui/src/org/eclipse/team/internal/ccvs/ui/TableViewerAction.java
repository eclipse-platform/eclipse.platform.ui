/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.history.ITag;

/**
 * Copies the text form the selected element of the given {@link org.eclipse.jface.viewers.TableViewer}. 
 * @author Jakub Jurkiewicz
 *
 */
public class TableViewerAction extends Action {
	
	/**
	 * Viewer for which the action is to be performed
	 */
	private TableViewer viewer;

	public TableViewerAction(TableViewer viewer) {
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		if (viewer.getSelection() instanceof StructuredSelection) {
			StructuredSelection sSelection = (StructuredSelection) viewer
					.getSelection();
			if (sSelection.getFirstElement() instanceof ITag) {
				Clipboard cb = new Clipboard(Display.getCurrent());
				String textData = ((ITag) sSelection.getFirstElement())
						.getName();
				TextTransfer textTransfer = TextTransfer.getInstance();
				cb.setContents(new Object[] { textData },
						new Transfer[] { textTransfer });
			}
		}
	}

}
