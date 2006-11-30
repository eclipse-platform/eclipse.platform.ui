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
import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.history.DialogHistoryPageSite;
import org.eclipse.team.ui.PageCompareEditorInput;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;

/**
 * Displays a history page combined with the compare/merge infrastructure. This only works properly if the
 * history page adapts to an {@link IHistoryCompareAdapter}.
 * 
 * @since 3.3
 */
public class HistoryPageCompareEditorInput extends PageCompareEditorInput {

	private IHistoryPage historyPage;
	private DialogHistoryPageSite site;
	private final Object object;
	private final IHistoryPageSource pageSource;
	private final IPropertyChangeListener changeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			handlePropertyChange(event);
		}
	};
	
	/**
	 * Create a history page compare editor input for the given page and object.
	 * @param configuration the compare configuration
	 * @param pageSource the page source
	 * @param object the object whose history is to be displayed
	 */
	public HistoryPageCompareEditorInput(CompareConfiguration configuration, IHistoryPageSource pageSource, Object object) {
		super(configuration);
		this.pageSource = pageSource;
		this.object = object;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		return object;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#handleDispose()
	 */
	protected void handleDispose() {
		super.handleDispose();
		if (historyPage != null) {
			historyPage.removePropertyChangeListener(changeListener);
			historyPage.dispose();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageCompareEditorInput#createPage(org.eclipse.compare.CompareViewerPane, org.eclipse.jface.action.IToolBarManager)
	 */
	protected IPage createPage(CompareViewerPane parent, IToolBarManager toolBarManager) {
		site = new DialogHistoryPageSite(parent.getShell());
		historyPage = (IHistoryPage)pageSource.createPage(object);
		historyPage.setSite(site);
		site.setToolBarManager(toolBarManager);
		((Page) historyPage).createControl(parent);
		historyPage.setInput(object);
		String description = historyPage.getDescription();
		if (description == null)
			description = ""; //$NON-NLS-1$
		setPageDescription(description);
		setTitle(historyPage.getName());
		historyPage.addPropertyChangeListener(changeListener);
		return (IPage)historyPage;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageCompareEditorInput#asCompareInput(org.eclipse.jface.viewers.ISelection)
	 */
	protected ICompareInput asCompareInput(ISelection selection) {
		ICompareInput compareInput = super.asCompareInput(selection);
		if (compareInput != null)
			return compareInput;
		IHistoryCompareAdapter compareAdapter = (IHistoryCompareAdapter) Utils.getAdapter(historyPage, IHistoryCompareAdapter.class);
		if (compareAdapter != null){
			return compareAdapter.getCompareInput(selection);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageCompareEditorInput#getSelectionProvider()
	 */
	protected ISelectionProvider getSelectionProvider() {
		return site.getSelectionProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageCompareEditorInput#prepareInput(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.CompareConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void prepareInput(ICompareInput input,
			CompareConfiguration configuration, IProgressMonitor monitor)
			throws InvocationTargetException {
		IHistoryCompareAdapter compareAdapter = (IHistoryCompareAdapter) Utils.getAdapter(historyPage, IHistoryCompareAdapter.class);
		if (compareAdapter != null){
			compareAdapter.prepareInput(input, configuration, monitor);
		}
	}

	/**
	 * Return the history page for this input or <code>null</code> if the
	 * page hasn't been created yet.
	 * @return the history page for this input
	 */
	public final IHistoryPage getHistoryPage() {
		return historyPage;
	}

	/**
	 * Handle a property change event from the history page.
	 * @param event the change event
	 */
	protected void handlePropertyChange(PropertyChangeEvent event) {
		if (event.getSource() == historyPage) {
			if (event.getProperty().equals(IHistoryPage.P_NAME)) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setTitle(historyPage.getName());
					}
				});
			} else if (event.getProperty().equals(IHistoryPage.P_DESCRIPTION)) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						setPageDescription(historyPage.getDescription());
					}
				});
			}
		}
	}
	
}
