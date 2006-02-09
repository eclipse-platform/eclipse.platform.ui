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

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IContentChangeNotifier;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.PageSaveablePart;
import org.eclipse.ui.part.Page;

/**
 * Displays a history page combined with the compare/merge infrastructure. This only works properly if the
 * history page adapts to an {@link IHistoryCompareAdapter}.
 * 
 * @since 3.2
 */
public class HistoryPageSaveablePart extends PageSaveablePart {

	IHistoryPage historyPage;
	
	/**
	 * Create a history page part for the given page.
	 * @param shell the parent shell
	 * @param configuration the compare configuration
	 * @param page the page
	 */
	public HistoryPageSaveablePart(Shell shell, CompareConfiguration configuration, IHistoryPage page) {
		super(shell,configuration);
		this.historyPage = page;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageSaveablePart#getTitle()
	 */
	public String getTitle() {
		return historyPage.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageSaveablePart#getTitleImage()
	 */
	public Image getTitleImage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.IContentChangeListener#contentChanged(org.eclipse.compare.IContentChangeNotifier)
	 */
	public void contentChanged(IContentChangeNotifier source) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageSaveablePart#createPage(org.eclipse.swt.widgets.Composite, org.eclipse.jface.action.ToolBarManager)
	 */
	protected Control createPage(Composite parent, ToolBarManager toolBarManager) {
		IHistoryPageSite pageSite = historyPage.getHistoryPageSite();
		pageSite.setToolBarManager(toolBarManager);
		((Page) historyPage).createControl(parent);
		historyPage.refresh();	
		setPageTitle(historyPage.getName());
		return ((Page) historyPage).getControl();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageSaveablePart#getPageSelectionProvider()
	 */
	protected ISelectionProvider getPageSelectionProvider() {
		return historyPage.getHistoryPageSite().getSelectionProvider();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageSaveablePart#getCompareInput(org.eclipse.jface.viewers.ISelection)
	 */
	protected ICompareInput getCompareInput(ISelection selection) {
		ICompareInput compareInput = super.getCompareInput(selection);
		if (compareInput != null)
			return compareInput;
		IHistoryCompareAdapter compareAdapter = (IHistoryCompareAdapter) Utils.getAdapter(historyPage, IHistoryCompareAdapter.class);
		if (compareAdapter != null){
			return compareAdapter.getCompareInput(selection);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageSaveablePart#prepareInput(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.CompareConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void prepareInput(ICompareInput input, CompareConfiguration configuration, IProgressMonitor monitor) throws InvocationTargetException {
		IHistoryCompareAdapter compareAdapter = (IHistoryCompareAdapter) Utils.getAdapter(historyPage, IHistoryCompareAdapter.class);
		if (compareAdapter != null){
			compareAdapter.prepareInput(input, configuration, monitor);
		}
	}
	
}
