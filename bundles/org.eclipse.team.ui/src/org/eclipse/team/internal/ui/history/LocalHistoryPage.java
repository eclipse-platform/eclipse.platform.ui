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
package org.eclipse.team.internal.ui.history;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.history.HistoryPage;
import org.eclipse.team.ui.history.IHistoryPageSite;
import org.eclipse.ui.IWorkbenchActionConstants;

public class LocalHistoryPage extends HistoryPage {

	Composite pgComp;
	TableViewer tableViewer;
	LocalHistoryTableProvider historyTableProvider;
	
	/* private */boolean shutdown = false;
	// cached for efficiency
	/* private */IFileState[] entries;
	
	protected FetchLocalHistoryJob fetchLocalHistoryJob;
	
	private IHistoryPageSite parentSite;
	private OpenLocalFileAction openAction;


	
	public boolean showHistory(Object object, boolean refetch) {
		if (object instanceof IFile) {
			IFile newfile = (IFile) object;
			if (!refetch || object == null)
				return false;
		
			//historyTableProvider.setFile(fileHistory, newfile);
			tableViewer.setInput(newfile);
			return true;
		}
		return false;
	}

	public boolean canShowHistoryFor(Object object) {
		//don't volunteer to use this page unless forced to do so
		return false;
	}

	public void refresh() {
		// TODO Auto-generated method stub

	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSite(IHistoryPageSite viewSite) {
		this.parentSite = viewSite;
	}

	public void createControl(Composite parent) {
		pgComp = new Composite(parent, SWT.NULL);
		pgComp.setLayout(new FillLayout());
		
		tableViewer = createTable(pgComp);
		
		contributeActions();
		
	}

	private void contributeActions() {
		// Double click open action
		openAction = new OpenLocalFileAction(TeamUIMessages.LocalHistoryPage_openRevision);
		tableViewer.getTable().addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				openAction.selectionChanged((IStructuredSelection) tableViewer.getSelection());
				openAction.run();
			}
		});
		
		//Contribute actions to popup menu
		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(tableViewer.getTable());
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				fillTableMenu(menuMgr);
			}

		
		});
		menuMgr.setRemoveAllWhenShown(true);
		tableViewer.getTable().setMenu(menu);
		parentSite.getWorkbenchPartSite().registerContextMenu(menuMgr, tableViewer);
		
	}

	/* private */void fillTableMenu(IMenuManager menuMgr) {
			IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		    openAction.selectionChanged(selection);
		    menuMgr.add(openAction);
		    /*compareAction.setEnabled(selection.size() == 2);
		    compareAction.selectionChanged(selection);
		    menuMgr.add(compareAction);*/
		    menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private TableViewer createTable(Composite parent) {
		historyTableProvider = new LocalHistoryTableProvider();
		TableViewer viewer = historyTableProvider.createTable(parent);
		
		viewer.setContentProvider(new IStructuredContentProvider(){

			public Object[] getElements(Object inputElement) {
				//The entries of already been fetch so return them
				if (entries != null)
					return entries;

				// The entries need to be fetch (or are being fetched)
				if (!(inputElement instanceof IResource))
					return null;

				final IFile inputFile = (IFile) inputElement;
				if (fetchLocalHistoryJob == null) {
					fetchLocalHistoryJob = new FetchLocalHistoryJob();
				}

				IFile file = fetchLocalHistoryJob.getFile();

				if (file == null || !file.equals(inputFile)) { // The resource
					// has changed
					// so stop the
					// currently
					// running job
					if (fetchLocalHistoryJob.getState() != Job.NONE) {
						fetchLocalHistoryJob.cancel();
						try {
							fetchLocalHistoryJob.join();
						} catch (InterruptedException e) {
							TeamUIPlugin.log(new TeamException(NLS.bind(TeamUIMessages.GenericHistoryView_ErrorFetchingEntries, new String[] {""}), e)); //$NON-NLS-1$
						}
					}
					fetchLocalHistoryJob.setFile(inputFile);
				} // Schedule the job even if it is already running
				Utils.schedule(fetchLocalHistoryJob, /*getSite()*/parentSite.getWorkbenchPartSite());

				return new Object[0];
			}

			public void dispose() {
				// TODO Auto-generated method stub
				
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
			}
			
		});
		
		return viewer;
	}

	public Control getControl() {
		return pgComp;
	}

	public void setFocus() {
		pgComp.setFocus();
	}
	
	private class FetchLocalHistoryJob extends Job {
		public IFile localFile;

		public FetchLocalHistoryJob() {
			super(TeamUIMessages.LocalHistoryPage_fetchingLocalHistory);
		}

		public IFile getFile() {
			return localFile;
		}

		public void setFile(IFile file) {
			localFile = file;
		}

		public IStatus run(IProgressMonitor monitor) {
			try {
				if (localFile != null && !shutdown) {
					entries = localFile.getHistory(monitor);
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (entries != null && tableViewer != null && !tableViewer.getTable().isDisposed()) {
								tableViewer.refresh();
							}
						}
					});
				}
				return Status.OK_STATUS;
			} catch (CoreException e) {
				return e.getStatus();
			}
		}

	}

}
