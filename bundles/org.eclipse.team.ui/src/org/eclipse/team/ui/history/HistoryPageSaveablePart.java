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
import java.text.DateFormat;
import java.util.Date;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.core.LocalFileRevision;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.team.internal.ui.history.TypedBufferedContent;
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
		pageSite.setToolBarManager(CompareViewerPane.getToolBarManager(getPagePane()));

		setShowContentPanes(false);
		((Page) historyPage).createControl(getPagePane());
		
		getPagePane().setContent(((Page) historyPage).getControl());

		
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
	
		historyPage.refresh();	
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageSaveablePart#prepareInput(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.CompareConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void prepareInput(ICompareInput input, CompareConfiguration configuration, IProgressMonitor monitor) throws InvocationTargetException {
		initLabels(input,configuration);
	}

	private void initLabels(ICompareInput input, CompareConfiguration cc) {
		cc.setLeftEditable(false);
		cc.setRightEditable(false);
		String leftLabel = getFileRevisionLabel(input.getLeft(), cc);
		cc.setLeftLabel(leftLabel);
		String rightLabel = getFileRevisionLabel(input.getRight(), cc);
		cc.setRightLabel(rightLabel);
	}

	
	private String getFileRevisionLabel(ITypedElement element, CompareConfiguration cc) {
		String label = null;

		if (element instanceof TypedBufferedContent) {
			//current revision
			Date dateFromLong = new Date(((TypedBufferedContent) element).getModificationDate());
			label = NLS.bind(TeamUIMessages.CompareFileRevisionEditorInput_workspace, new Object[]{ element.getName(), DateFormat.getDateTimeInstance().format(dateFromLong)});
			cc.setLeftEditable(true);
			return label;

		} else if (element instanceof FileRevisionTypedElement) {
			Object fileObject = ((FileRevisionTypedElement) element).getFileRevision();

			if (fileObject instanceof LocalFileRevision) {
				try {
					IStorage storage = ((LocalFileRevision) fileObject).getStorage(new NullProgressMonitor());
					if (Utils.getAdapter(storage, IFileState.class) != null) {
						//local revision
						label = NLS.bind(TeamUIMessages.CompareFileRevisionEditorInput_localRevision, new Object[]{element.getName(), ((FileRevisionTypedElement) element).getTimestamp()});
					}
				} catch (CoreException e) {
				}
			} else {
				label = NLS.bind(TeamUIMessages.CompareFileRevisionEditorInput_repository, new Object[]{ element.getName(), ((FileRevisionTypedElement) element).getContentIdentifier()});
			}
		}
		return label;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		flushViewers(monitor);
	}
	
}
