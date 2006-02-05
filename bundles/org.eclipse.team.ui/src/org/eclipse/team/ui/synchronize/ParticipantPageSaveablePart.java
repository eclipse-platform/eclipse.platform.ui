/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.PageSaveablePart;
import org.eclipse.team.ui.mapping.IModelCompareInput;
import org.eclipse.team.ui.mapping.ISaveableCompareModel;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * Displays a synchronize participant page combined with the compare/merge infrastructure. This only works if the
 * synchronize page viewer provides selections that are of the following types: ITypedElement and ICompareInput
 * or if the participant is a {@link ModelSynchronizeParticipant}.
 * 
 * @since 3.0
 */
public class ParticipantPageSaveablePart extends PageSaveablePart implements IContentChangeListener {

	private ISynchronizeParticipant participant;
	private ISynchronizePageConfiguration pageConfiguration;
	private Image titleImage;

	private IPageBookViewPage page;
	private DialogSynchronizePageSite site;
	
	private IPropertyChangeListener listener;
	private Viewer viewer;

	/**
	 * Creates a part for the provided participant. The page configuration is used when creating the participant page and the resulting
	 * compare/merge panes will be configured with the provided compare configuration.
	 * <p>
	 * For example, clients can decide if the user can edit the compare panes by calling {@link CompareConfiguration#setLeftEditable(boolean)}
	 * or {@link CompareConfiguration#setRightEditable(boolean)}. 
	 * </p>
	 * @param shell the parent shell for this part
	 * @param cc the compare configuration that will be used to create the compare panes
	 * @param pageConfiguration the configuration that will be provided to the participant prior to creating the page
	 * @param participant the participant whose page will be displayed in this part
	 */
	public ParticipantPageSaveablePart(Shell shell, CompareConfiguration cc, ISynchronizePageConfiguration pageConfiguration, ISynchronizeParticipant participant) {
		super(shell,cc);
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
		if (page != null) 
			page.dispose();
		if (site != null)
			site.dispose();
		pageConfiguration.removePropertyChangeListener(listener);
		super.dispose();
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
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		ISaveableCompareModel currentBuffer = ((ModelSynchronizeParticipant)pageConfiguration.getParticipant()).getActiveModel();
		if (currentBuffer != null) {
			return currentBuffer.isDirty();
		}
		return super.isDirty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.IContentChangeListener#contentChanged(org.eclipse.compare.IContentChangeNotifier)
	 */
	public void contentChanged(IContentChangeNotifier source) {
		try {
			if (source instanceof DiffNode) {
				commit(new NullProgressMonitor(), (DiffNode) source);
			} else if (source instanceof LocalResourceTypedElement) {
				 ((LocalResourceTypedElement) source).commit(new NullProgressMonitor());
			}
		} catch (CoreException e) {
			Utils.handle(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor pm) {
		// TODO needs to work for models
		//super.saveChanges(pm);
		Object input = viewer.getInput();
		if (input instanceof ISynchronizeModelElement) {
			ISynchronizeModelElement root = (ISynchronizeModelElement)input;
			if (root != null && root instanceof DiffNode) {
				try {
					commit(pm, (DiffNode)root);
				} catch (CoreException e) {
					Utils.handle(e);
				} finally {
					setDirty(false);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent2) {
		
		super.createPartControl(parent2);
		
		listener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(ISynchronizePageConfiguration.P_PAGE_DESCRIPTION)) {
					updateDescription();
				}
			}
		};
		pageConfiguration.addPropertyChangeListener(listener);
		updateDescription();

		page = participant.createPage(pageConfiguration);
		site = new DialogSynchronizePageSite(getShell(), true);
		((SynchronizePageConfiguration)pageConfiguration).setSite(site);
		ToolBarManager tbm = CompareViewerPane.getToolBarManager(getEditionPane());
		site.createActionBars(tbm);
		try {
			((ISynchronizePage)page).init(pageConfiguration.getSite());
		} catch (PartInitException e1) {
		}

		page.createControl(getEditionPane());
		
		if(page instanceof ISynchronizePage) {
			((ISynchronizePage)page).getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					ICompareInput input = getCompareInput(event.getSelection());
					prepareCompareInput(input);
					setInput(input);
				}
			});
			initializeDiffViewer(((ISynchronizePage)page).getViewer());
		}
		
		page.setActionBars(site.getActionBars());
		getEditionPane().setContent(page.getControl());
		tbm.update(true);
		if(page instanceof ISynchronizePage) {
			viewer = ((ISynchronizePage)page).getViewer();
		}
		
		setNavigator(pageConfiguration);
	}

	private void updateDescription() {
		String description = (String)pageConfiguration.getProperty(ISynchronizePageConfiguration.P_PAGE_DESCRIPTION);
		if (description != null) {
			getEditionPane().setText(description);
		}
	}
	
	/**
	 * This method should not be called from clients.
	 * TODO: using internal compare classes to support page navigation. This is required because
	 * we are building our own compare editor input that includes a participant page instead of a
	 * viewer.
	 */
	public void setNavigator(ISynchronizePageConfiguration configuration) {
			configuration.setProperty(SynchronizePageConfiguration.P_NAVIGATOR, new PartNavigator(
				new Object[] {
					configuration.getProperty(SynchronizePageConfiguration.P_ADVISOR),
					getStructuredComparePane(),
					getContentPane()
				}
			));
	}
	
	/**
	 * Initialize the diff viewer created for this compare input. If a subclass
	 * overrides the <code>createDiffViewer(Composite)</code> method, it should
	 * invoke this method on the created viewer in order to get the proper
	 * labeling in the compare input's contents viewers.
	 * @param viewer the diff viewer created by the compare input
	 */
	private void initializeDiffViewer(Viewer viewer) {
		if (viewer instanceof StructuredViewer) {
			((StructuredViewer) viewer).addOpenListener(new IOpenListener() {
				public void open(OpenEvent event) {
					ISelection s = event.getSelection();
					final SyncInfoModelElement node = getElement(s);
					if (node == null) {
						ICompareInput input = getCompareInput(s);
						if (input != null) {
							prepareCompareInput(input);
						}
					} else {
						prepareCompareInput(node);
					}
				}
			});
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.PageSaveablePart#prepareInput(org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.CompareConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void prepareInput(final ICompareInput input, CompareConfiguration configuration, IProgressMonitor monitor) throws InvocationTargetException {
		monitor.beginTask(TeamUIMessages.SyncInfoCompareInput_3, 100);
        monitor.setTaskName(TeamUIMessages.SyncInfoCompareInput_3);
		try {
			// First, see if the active buffer is changing
			checkForBufferChange(pageConfiguration.getSite().getShell(), input, false /* cancel not allowed */, monitor);
			if (input instanceof SyncInfoModelElement) {
				final SyncInfoModelElement node = (SyncInfoModelElement) input;
				IResource resource = node.getResource();
				if (resource != null && resource.getType() == IResource.FILE) {
					participant.prepareCompareInput(node, getCompareConfiguration(), monitor);
					hookContentChangeListener(input);
				}
			} else {
				IModelCompareInput adapter = asModelCompareInput(input);
				if (adapter != null) {
					adapter.prepareInput(getCompareConfiguration(), Policy.subMonitorFor(monitor, 90));
				}
				hookContentChangeListener(input);
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} finally {
            monitor.done();
        }
	}

	private void checkForBufferChange(Shell shell, final ICompareInput input, boolean cancelAllowed, IProgressMonitor monitor) throws CoreException {
		ISynchronizeParticipant participant = pageConfiguration.getParticipant();
		if (participant instanceof ModelSynchronizeParticipant) {
			ModelSynchronizeParticipant msp = (ModelSynchronizeParticipant) participant;
			if (input instanceof IModelCompareInput) {
				IModelCompareInput mci = (IModelCompareInput) input;
				msp.checkForBufferChange(shell, mci, cancelAllowed, monitor);
			}
		}
	}

	private IModelCompareInput asModelCompareInput(ICompareInput input) {
		return (IModelCompareInput)Utils.getAdapter(input, IModelCompareInput.class);
	}
	
	private SyncInfoModelElement getElement(ISelection selection) {
		ICompareInput input = getCompareInput(selection);
		if(input instanceof SyncInfoModelElement) {
			return (SyncInfoModelElement)input;
		}
		return null;
	}
	
	private static void commit(IProgressMonitor pm, DiffNode node) throws CoreException {
		ITypedElement left = node.getLeft();
		if (left instanceof LocalResourceTypedElement)
			 ((LocalResourceTypedElement) left).commit(pm);

		ITypedElement right = node.getRight();
		if (right instanceof LocalResourceTypedElement)
			 ((LocalResourceTypedElement) right).commit(pm);
		
		IDiffElement[] children = node.getChildren();
		for (int i = 0; i < children.length; i++) {
			commit(pm, (DiffNode)children[i]);			
		}
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

	/*
	 * Return a compare input that represents the selection.
	 * This input is used to feed the structure and content
	 * viewers. By default, a compare input is returned if the selection is
	 * of size 1 and the selected element implements <code>ICompareInput</code>
	 * @param selection the selection
	 * @return a compare input representing the selection
	 * TODO move to compare adapter
	 */
	protected ICompareInput getCompareInput(ISelection selection) {
		ICompareInput compareInput = super.getCompareInput(selection);
		if (compareInput != null)
			return compareInput;
		
		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			if (ss.size() == 1) {
				Object o = ss.getFirstElement();
				if (participant instanceof ModelSynchronizeParticipant) {
					ModelSynchronizeParticipant msp = (ModelSynchronizeParticipant) participant;
					return msp.asCompareInput(o);
				}
			}
		}
		return null;
	}
	
	

}
