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
import java.util.ArrayList;

import org.eclipse.compare.*;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.mapping.ModelProviderAction;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.SaveablePartAdapter;
import org.eclipse.team.ui.compare.*;
import org.eclipse.team.ui.mapping.ISynchronizationConstants;
import org.eclipse.team.ui.operations.ModelSynchronizeParticipant;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.progress.IProgressService;

/**
 * Displays a synchronize participant page combined with the compare/merge infrastructure. This only works if the
 * synchronize page viewer provides selections that are of the following types: ITypedElement and ICompareInput
 * or if the participant is a {@link ModelSynchronizeParticipant}.
 * 
 * @since 3.0
 */
public class ParticipantPageSaveablePart extends SaveablePartAdapter implements IContentChangeListener {

	private CompareConfiguration cc;
	private ISynchronizeParticipant participant;
	private ISynchronizePageConfiguration pageConfiguration;
	private Image titleImage;
	Shell dialogShell;
	
	// Tracking of dirty state
	private boolean fDirty= false;
	private ArrayList fDirtyViewers= new ArrayList();
	private IPropertyChangeListener fDirtyStateListener;
	
	//	 SWT controls
	private CompareViewerSwitchingPane fContentPane;
	private CompareViewerPane fEditionPane;
	private CompareViewerSwitchingPane fStructuredComparePane;
	Viewer viewer;
	private Control control;
	
	// Configuration options
	private boolean showContentPanes = true;

	private IPageBookViewPage page;
	private DialogSynchronizePageSite site;
	
	private Object currentEditingContext;
	private IPropertyChangeListener listener;

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
		this.cc = cc;
		this.participant = participant;
		this.pageConfiguration = pageConfiguration;
		
		fDirtyStateListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String propertyName= e.getProperty();
				if (CompareEditorInput.DIRTY_STATE.equals(propertyName)) {
					boolean changed= false;
					Object newValue= e.getNewValue();
					if (newValue instanceof Boolean)
						changed= ((Boolean)newValue).booleanValue();
					setDirty(e.getSource(), changed);
				}			
			}
		};
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
		return fDirty || fDirtyViewers.size() > 0;
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
		Composite parent = new Composite(parent2, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		parent.setLayout(layout);
		parent.setLayoutData(data);
		
		dialogShell = parent2.getShell();
		
		Splitter vsplitter = new Splitter(parent, SWT.VERTICAL);
		vsplitter.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
		// we need two panes: the left for the elements, the right one for the structured diff
		Splitter hsplitter = new Splitter(vsplitter, SWT.HORIZONTAL);
		fEditionPane = new CompareViewerPane(hsplitter, SWT.BORDER | SWT.FLAT);
		fStructuredComparePane = new CompareViewerSwitchingPane(hsplitter, SWT.BORDER | SWT.FLAT, false) {
			protected Viewer getViewer(Viewer oldViewer, Object input) {
				if (input instanceof ICompareInput)
					return findStructureViewer(this, oldViewer, (ICompareInput)input);
				return null;
			}
		};
		fStructuredComparePane.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				feedInput2(e.getSelection());
			}
		});
		
		fEditionPane.setText(TeamUIMessages.ParticipantPageSaveablePart_0);
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
		site = new DialogSynchronizePageSite(dialogShell, true);
		((SynchronizePageConfiguration)pageConfiguration).setSite(site);
		ToolBarManager tbm = CompareViewerPane.getToolBarManager(fEditionPane);
		site.createActionBars(tbm);
		try {
			((ISynchronizePage)page).init(pageConfiguration.getSite());
		} catch (PartInitException e1) {
		}

		page.createControl(fEditionPane);
		
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
		fEditionPane.setContent(page.getControl());
		tbm.update(true);
		if(page instanceof ISynchronizePage) {
			this.viewer = ((ISynchronizePage)page).getViewer();
		}
		
		fContentPane = new CompareViewerSwitchingPane(vsplitter, SWT.BORDER | SWT.FLAT) {
			protected Viewer getViewer(Viewer oldViewer, Object input) {
				if (!(input instanceof ICompareInput))
					return null;
				Viewer newViewer= findContentViewer(this, oldViewer, (ICompareInput)input);
				boolean isNewViewer= newViewer != oldViewer;
				if (isNewViewer && newViewer instanceof IPropertyChangeNotifier) {
					final IPropertyChangeNotifier dsp= (IPropertyChangeNotifier) newViewer;
					dsp.addPropertyChangeListener(fDirtyStateListener);
					Control c= newViewer.getControl();
					c.addDisposeListener(
						new DisposeListener() {
							public void widgetDisposed(DisposeEvent e) {
								dsp.removePropertyChangeListener(fDirtyStateListener);
							}
						}
					);
					hookContentChangeListener((ICompareInput)input);
				}	
				return newViewer;
			}
		};
		vsplitter.setWeights(new int[]{30, 70});
		setNavigator(pageConfiguration);
		control = parent;
		
		if(! showContentPanes) {
			hsplitter.setMaximizedControl(fEditionPane);
		}
	}

	private void updateDescription() {
		String description = (String)pageConfiguration.getProperty(ISynchronizePageConfiguration.P_PAGE_DESCRIPTION);
		if (description != null) {
			fEditionPane.setText(description);
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
					fStructuredComparePane,
					fContentPane
				}
			));
	}
	
    /**
     * Set whether the file contents panes should be shown. If they are not,
     * only the resource tree will be shown.
     * 
     * @param showContentPanes whether to show contents pane
     * @since 3.1
     */
	public void setShowContentPanes(boolean showContentPanes) {
		this.showContentPanes = showContentPanes;
	}
	
	/*
	 * Feeds input from the participant page into the content and structured viewers.
	 */
	private void setInput(Object input) {
		fContentPane.setInput(input);
		if (fStructuredComparePane != null)
			fStructuredComparePane.setInput(input);
	}
	
	/*
	 * Feeds selection from structure viewer to content viewer.
	 */
	private void feedInput2(ISelection sel) {
		ICompareInput input = getCompareInput(sel);
		prepareCompareInput(input);
		if (input != null)
			fContentPane.setInput(input);
	}
	
	/**
	 * Returns the primary control for this part.
	 * 
	 * @return the primary control for this part.
	 */
	public Control getControl() {
		return control;
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
	
	/* package */ void prepareCompareInput(final ICompareInput input) {
		// TODO Merge operations would need to do this check as well
		// TODO Where should this check live
		// TODO What about the left vs. right side changes (perhaps a buffer that wraps three buffers)
		if (input == null)
			return;
		IProgressService manager = PlatformUI.getWorkbench().getProgressService();
		try {
			// TODO: we need a better progress story here
			manager.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			        internalPrepareInput(input, monitor);
				}
			});
		} catch (InvocationTargetException e) {
			Utils.handle(e);
		} catch (InterruptedException e) {
			// Ignore
		}
	}
	
	/* private */ void internalPrepareInput(final ICompareInput input, IProgressMonitor monitor) throws InvocationTargetException {
		monitor.beginTask(TeamUIMessages.SyncInfoCompareInput_3, 100);
        monitor.setTaskName(TeamUIMessages.SyncInfoCompareInput_3);
		try {
			// First, see if the active buffer is changing
			IModelBuffer currentBuffer = (IModelBuffer)pageConfiguration.getProperty(ISynchronizationConstants.P_ACTIVE_BUFFER);
			IModelBuffer targetBuffer = getTargetBuffer(input);
			try {
				ModelProviderAction.handleBufferChange(pageConfiguration.getSite().getShell(), targetBuffer, currentBuffer, false /* cancel not allowed */, Policy.subMonitorFor(monitor, 10));
			} catch (InterruptedException e) {
				// Ignore since we indicated that cancel is not supported
			}
			pageConfiguration.setProperty(ISynchronizationConstants.P_ACTIVE_BUFFER, targetBuffer);
			if (input instanceof SyncInfoModelElement) {
				final SyncInfoModelElement node = (SyncInfoModelElement) input;
				IResource resource = node.getResource();
				if (resource != null && resource.getType() == IResource.FILE) {
					participant.prepareCompareInput(node, cc, monitor);
					hookContentChangeListener(input);
				}
			} else {
				IPrepareCompareInputAdapter adapter = getPrepareAdapter(input);
				if (adapter != null) {
					adapter.prepareInput(input, cc, Policy.subMonitorFor(monitor, 90));
				}
				hookContentChangeListener(input);
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} finally {
            monitor.done();
        }
	}
	
	private IModelBuffer getTargetBuffer(ICompareInput input) {
		IModelBufferAdapter adapter = (IModelBufferAdapter)Utils.getAdapter(input, IModelBufferAdapter.class);
		if (adapter != null)
			return adapter.getBuffer(input);
		return null;
	}

	private IPrepareCompareInputAdapter getPrepareAdapter(ICompareInput input) {
		return (IPrepareCompareInputAdapter)Utils.getAdapter(input, IPrepareCompareInputAdapter.class);
	}
	
	private void hookContentChangeListener(ICompareInput node) {
		// TODO: there is no unhook which may lead to a leak
		ITypedElement left = node.getLeft();
		if(left instanceof IContentChangeNotifier) {
			((IContentChangeNotifier)left).addContentChangeListener(this);
		}
		ITypedElement right = node.getRight();
		if(right instanceof IContentChangeNotifier) {
			((IContentChangeNotifier)right).addContentChangeListener(this);
		}
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

	private void setDirty(boolean dirty) {
		boolean confirmSave= true;
		Object o= cc.getProperty(CompareEditor.CONFIRM_SAVE_PROPERTY);
		if (o instanceof Boolean)
			confirmSave= ((Boolean)o).booleanValue();

		if (!confirmSave) {
			fDirty= dirty;
			if (!fDirty)
				fDirtyViewers.clear();
		}
	}
	
	private void setDirty(Object source, boolean dirty) {
		Assert.isNotNull(source);
		if (dirty)
			fDirtyViewers.add(source);
		else
			fDirtyViewers.remove(source);
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
	 */
	private ICompareInput getCompareInput(ISelection selection) {
		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			if (ss.size() == 1) {
				Object o = ss.getFirstElement();
				if(o instanceof ICompareInput) {
					return (ICompareInput)o;
				}
				if (participant instanceof ModelSynchronizeParticipant) {
					ModelSynchronizeParticipant msp = (ModelSynchronizeParticipant) participant;
					return msp.asCompareInput(o);
				}
			}
		}
		return null;
	}
	
	/*
	 * Find a viewer that can provide a structure view for the given compare input.
	 * Return <code>null</code> if a suitable viewer could not be found.
	 * @param parent the parent composite for the viewer
	 * @param oldViewer the viewer that is currently a child of the parent
	 * @param input the compare input to be viewed
	 * @return a viewer capable of displaying a structure view of the input or
	 * <code>null</code> if such a viewer is not available.
	 */
	private Viewer findStructureViewer(Composite parent, Viewer oldViewer, ICompareInput input) {
		return CompareUI.findStructureViewer(oldViewer, input, parent, cc);
	}
	
	/*
	 * Find a viewer that can provide a content compare view for the given compare input.
	 * Return <code>null</code> if a suitable viewer could not be found.
	 * @param parent the parent composite for the viewer
	 * @param oldViewer the viewer that is currently a child of the parent
	 * @param input the compare input to be viewed
	 * @return a viewer capable of displaying a content compare view of the input or
	 * <code>null</code> if such a viewer is not available.
	 */
	private Viewer findContentViewer(Composite parent, Viewer oldViewer, ICompareInput input) {
		return CompareUI.findContentViewer(oldViewer, input, parent, cc);
	}
}
