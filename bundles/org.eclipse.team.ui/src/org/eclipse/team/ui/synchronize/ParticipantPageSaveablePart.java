/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html Contributors:
 * IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.ui.synchronize;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.compare.*;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogSettings;
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
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.*;
import org.eclipse.team.ui.SaveablePartAdapter;
import org.eclipse.ui.*;
import org.eclipse.ui.commands.*;
import org.eclipse.ui.commands.ActionHandler;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.progress.IProgressService;

/**
 * Displays a synchronize participant page combined with the compare/merge infrastructured. This only works if the
 * synchronize page viewer provides selections that are of the following types: ITypedElement and ICompareInput. 
 * 
 * @since 3.0
 */
public class ParticipantPageSaveablePart extends SaveablePartAdapter implements IContentChangeListener {

	private CompareConfiguration cc;
	private ISynchronizeParticipant participant;
	private ISynchronizePageConfiguration pageConfiguration;
	private Image titleImage;
	private Shell shell;
	private Shell dialogShell;
	
	// Tracking of dirty state
	private boolean fDirty= false;
	private ArrayList fDirtyViewers= new ArrayList();
	private IPropertyChangeListener fDirtyStateListener;
	
	//	 SWT controls
	private CompareViewerSwitchingPane fContentPane;
	private CompareViewerPane fEditionPane;
	private CompareViewerSwitchingPane fStructuredComparePane;
	private Viewer viewer;
	private Control control;
	
	// Keybindings enabled in the dialog, these should be removed
	// when the dialog is closed.
	private IActionBars actionBars;
	private List actionHandlers = new ArrayList(2);

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
			return dialogShell;
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
			return ParticipantPageSaveablePart.this.getActionBars();
		}
		/* (non-Javadoc)
		 * @see org.eclipse.team.ui.synchronize.ISynchronizePageSite#isModal()
		 */
		public boolean isModal() {
			return true;
		}	
	}
	
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
		this.shell = shell;
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
		IWorkbenchCommandSupport cm = PlatformUI.getWorkbench().getCommandSupport();
		for (Iterator it = actionHandlers.iterator(); it.hasNext();) {
			HandlerSubmission handler = (HandlerSubmission) it.next();
			cm.removeHandlerSubmission(handler);
		}
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
	
	/*
	 * (non-Javadoc)
	 * @see CompareEditorInput#saveChanges(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor pm) {
		//super.saveChanges(pm);
		ISynchronizeModelElement root = (ISynchronizeModelElement)viewer.getInput();
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
					return CompareUI.findStructureViewer(oldViewer, (ICompareInput) input, this, cc);
				return null;
			}
		};
		fStructuredComparePane.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				feedInput2(e.getSelection());
			}
		});
		fEditionPane.setText(Policy.bind("ParticipantPageSaveablePart.0")); //$NON-NLS-1$
		
		IPageBookViewPage page = participant.createPage(pageConfiguration);
		((SynchronizePageConfiguration)pageConfiguration).setSite(new CompareViewerPaneSite());
		ToolBarManager tbm = CompareViewerPane.getToolBarManager(fEditionPane);
		createActionBars(tbm);
		try {
			((ISynchronizePage)page).init(pageConfiguration.getSite());
		} catch (PartInitException e1) {
		}

		page.createControl(fEditionPane);
		
		if(page instanceof ISynchronizePage) {
			((ISynchronizePage)page).getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					ISelection sel = event.getSelection();
					if (sel instanceof IStructuredSelection) {
						IStructuredSelection ss= (IStructuredSelection) sel;
						if (ss.size() == 1)
							setInput(ss.getFirstElement());
					}
				}
			});
			initializeDiffViewer(((ISynchronizePage)page).getViewer());
		}
		
		page.setActionBars(getActionBars());
		fEditionPane.setContent(page.getControl());
		tbm.update(true);
		if(page instanceof ISynchronizePage) {
			this.viewer = ((ISynchronizePage)page).getViewer();
		}
		
		fContentPane = new CompareViewerSwitchingPane(vsplitter, SWT.BORDER | SWT.FLAT) {
			protected Viewer getViewer(Viewer oldViewer, Object input) {
				Viewer newViewer= CompareUI.findContentViewer(oldViewer, input, this, cc);
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
				}	
				return newViewer;
			}
		};
		vsplitter.setWeights(new int[]{30, 70});
		setNavigator(pageConfiguration);
		control = parent;
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
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) sel;
			if (ss.size() == 1)
				fContentPane.setInput(ss.getFirstElement());
		}
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
	 * labelling in the compare input's contents viewers.
	 * @param viewer the diff viewer created by the compare input
	 */
	private void initializeDiffViewer(Viewer viewer) {
		if (viewer instanceof StructuredViewer) {
			((StructuredViewer) viewer).addOpenListener(new IOpenListener() {
				public void open(OpenEvent event) {
					ISelection s = event.getSelection();
					final SyncInfoModelElement node = getElement(s);
					if (node != null) {
						IResource resource = node.getResource();
						int kind = node.getKind();
						if (resource != null && resource.getType() == IResource.FILE) {
							// Cache the contents because compare doesn't show progress
							// when calling getContents on a diff node.
							IProgressService manager = PlatformUI.getWorkbench().getProgressService();
							try {
								manager.busyCursorWhile(new IRunnableWithProgress() {
									public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
										try {	
											node.cacheContents(monitor);
											hookContentChangeListener(node);
										} catch (TeamException e) {
											Utils.handle(e);
										} finally {
											// Update the labels even if the content wasn't fetched correctly. This is
											// required because the selection would still of changed.
											Utils.updateLabels(node.getSyncInfo(), cc);
										}
									}
								});
							} catch (InvocationTargetException e) {
								Utils.handle(e);
							} catch (InterruptedException e) {
								return;
							}
						}
					}
				}
			});
		}
	}
	
	private void hookContentChangeListener(DiffNode node) {
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
		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			if (ss.size() == 1) {
				Object o = ss.getFirstElement();
				if(o instanceof SyncInfoModelElement) {
					return (SyncInfoModelElement)o;
				}
			}
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
		boolean oldDirty= fDirtyViewers.size() > 0;
		if (dirty)
			fDirtyViewers.add(source);
		else
			fDirtyViewers.remove(source);
		boolean newDirty= fDirtyViewers.size() > 0;
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
					IHandler handler = new ActionHandler(action);
		            HandlerSubmission handlerSubmission = new HandlerSubmission(null,
		                    dialogShell, null, actionId, handler, Priority.MEDIUM);	
					PlatformUI.getWorkbench().getCommandSupport().addHandlerSubmission(handlerSubmission);
					actionHandlers.add(handlerSubmission);
				}

				public void updateActionBars() {
				}
			};
		}
	}
	
	private IActionBars getActionBars() {
		return actionBars;
	}
	
	/**
	 * Return the synchronize page configiration for this part
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