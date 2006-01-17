/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.ui.synchronize;

import org.eclipse.compare.CompareViewerPane;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.ui.*;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Standalone presentation of a participant page within a view pane. This
 * allows showing a participant page with it's toolbar in dialogs and embedded
 * in views and editors.
 * 
 * @since 3.1
 */
public final class ParticipantPagePane {

	private ISynchronizeParticipant participant;
	private ISynchronizePageConfiguration pageConfiguration;
	private Image titleImage;
	private Shell shell;
	private boolean isModal;
	
	//	 SWT controls
	private CompareViewerPane fEditionPane;
	private Viewer viewer;
	
	private IActionBars actionBars;
	private IPageBookViewPage fPage;

	/*
	 * Page site that allows hosting the participant page in a dialog.
	 */
	class CompareViewerPaneSite implements ISynchronizePageSite {
		ISelectionProvider selectionProvider;
		public IWorkbenchPage getPage() {
			return null;
		}
		public ISelectionProvider getSelectionProvider() {
			if (selectionProvider != null) 
				return selectionProvider;
			return viewer;
		}
		public Shell getShell() {
			return shell;
		}
		public IWorkbenchWindow getWorkbenchWindow() {
			return null;
		}
		public void setSelectionProvider(ISelectionProvider provider) {
			selectionProvider = provider;
		}
		public Object getAdapter(Class adapter) {
			return null;
		}
		public IWorkbenchSite getWorkbenchSite() {
			return null;
		}
		public IWorkbenchPart getPart() {
			return null;
		}
		public IKeyBindingService getKeyBindingService() {
			return null;
		}
		public void setFocus() {
		}
		public IDialogSettings getPageSettings() {
			return null;
		}
		public IActionBars getActionBars() {
			return ParticipantPagePane.this.getActionBars();
		}
		/* (non-Javadoc)
		 * @see org.eclipse.team.ui.synchronize.ISynchronizePageSite#isModal()
		 */
		public boolean isModal() {
			return isModal;
		}	
	}
	
	/**
	 * Creates a part for the provided participant. The page configuration is used when creating the participant page and the resulting
	 * compare/merge panes will be configured with the provided compare configuration.
	 * <p>
	 * For example, clients can decide if the user can edit the compare panes by calling {@link org.eclipse.compare.CompareConfiguration#setLeftEditable(boolean)}
	 * or {@link org.eclipse.compare.CompareConfiguration#setRightEditable(boolean)}. 
	 * </p>
	 * @param shell the parent shell for this part
	 * @param isModal to set the pane as modal or not
	 * @param pageConfiguration the configuration that will be provided to the participant prior to creating the page
	 * @param participant the participant whose page will be displayed in this part
	 */
	public ParticipantPagePane(Shell shell, boolean isModal, ISynchronizePageConfiguration pageConfiguration, ISynchronizeParticipant participant) {
		this.isModal = isModal;
		this.shell = shell;
		this.participant = participant;
		this.pageConfiguration = pageConfiguration;
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.SaveablePartAdapter#dispose()
	 */
	public void dispose() {
		if(titleImage != null) {
			titleImage.dispose();
		}
		if (fPage != null) {
			fPage.dispose();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#getTitleImage()
	 */
	public Image getTitleImage() {
		if(titleImage == null) {
			titleImage = participant.getImageDescriptor().createImage();
		}
		return titleImage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#getTitle()
	 */
	public String getTitle() {
		return participant.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createPartControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		top.setLayout(layout);
		
		shell = parent.getShell();
		
		fEditionPane = new CompareViewerPane(top, SWT.BORDER | SWT.FLAT);		
		fEditionPane.setText(TeamUIMessages.ParticipantPageSaveablePart_0); //		
		
		fEditionPane.setLayoutData(SWTUtils.createHVFillGridData());
		
		fPage = participant.createPage(pageConfiguration);
		((SynchronizePageConfiguration)pageConfiguration).setSite(new CompareViewerPaneSite());
		ToolBarManager tbm = CompareViewerPane.getToolBarManager(fEditionPane);
		createActionBars(tbm);
		try {
			((ISynchronizePage)fPage).init(pageConfiguration.getSite());
		} catch (PartInitException e1) {
		   TeamUIPlugin.log(IStatus.ERROR, TeamUIMessages.ParticipantPagePane_0, e1); 
		}

		fPage.createControl(fEditionPane);
		fPage.setActionBars(getActionBars());
		fEditionPane.setContent(fPage.getControl());
		tbm.update(true);
		
		return top;
	}
	
	private void createActionBars(final IToolBarManager toolbar) {
		if (actionBars == null) {
			actionBars = new IActionBars() {
				public void clearGlobalActionHandlers() {
				}
				public IAction getGlobalActionHandler(String actionId) {
					return null;
				}
				public IMenuManager getMenuManager() {
					return null;
				}
				public IStatusLineManager getStatusLineManager() {
					return null;
				}
				public IToolBarManager getToolBarManager() {
					return toolbar;
				}
				public void setGlobalActionHandler(String actionId, IAction action) {				
				}

				public void updateActionBars() {
				}
				public IServiceLocator getServiceLocator() {
					return null;
				}
			};
		}
	}
	
	private IActionBars getActionBars() {
		return actionBars;
	}
	
	/**
	 * Return the synchronize page configuration for this part
	 * 
	 * @return Returns the pageConfiguration.
	 */
	public ISynchronizePageConfiguration getPageConfiguration() {
		return pageConfiguration;
	}
	
	/**
	 * Return the Synchronize participant for this part
	 * 
	 * @return Returns the participant.
	 */
	public ISynchronizeParticipant getParticipant() {
		return participant;
	}
}
