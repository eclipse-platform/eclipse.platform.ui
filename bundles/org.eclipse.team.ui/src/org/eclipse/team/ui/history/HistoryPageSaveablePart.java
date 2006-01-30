/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.history;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.PageSaveablePart;
import org.eclipse.ui.part.Page;

public class HistoryPageSaveablePart extends PageSaveablePart {

	IHistoryPage historyPage;
	
	public HistoryPageSaveablePart(Shell shell, CompareConfiguration cc, IHistoryPage pageSource) {
		super(shell,cc);
		this.historyPage = pageSource;
	}
	public String getTitle() {
		return historyPage.getName();
	}

	public Image getTitleImage() {
		return null;
	}

	public void contentChanged(IContentChangeNotifier source) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent2) {

		super.createPartControl(parent2);
		IHistoryPageSite pageSite = historyPage.getHistoryPageSite();
		pageSite.setToolBarManager(CompareViewerPane.getToolBarManager(getEditionPane()));

		setShowContentPanes(false);
		((Page) historyPage).createControl(getEditionPane());
		
		getEditionPane().setContent(((Page) historyPage).getControl());
		historyPage.refresh();
		
		historyPage.getHistoryPageSite().getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IHistoryCompareAdapter compareAdapter = (IHistoryCompareAdapter) Utils.getAdapter(historyPage, IHistoryCompareAdapter.class);
				if (compareAdapter != null){
					ICompareInput input = compareAdapter.getCompareInput(event.getSelection());
					prepareCompareInput(input);
					setInput(input);
				}
			}
		});
	
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageSaveablePart#prepareInput(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.CompareConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void prepareInput(ICompareInput input, CompareConfiguration configuration, IProgressMonitor monitor) throws InvocationTargetException {
		// Do nothing
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		//TODO
	}
	
}
