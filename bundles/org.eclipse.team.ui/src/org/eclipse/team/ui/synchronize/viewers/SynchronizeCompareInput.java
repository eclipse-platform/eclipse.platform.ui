/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize.viewers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.internal.INavigatable;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.LocalResourceTypedElement;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * A <code>CompareEditorInput</code> whose diff viewer shows the resources contained
 * in a <code>SyncInfoSet</code>. The configuration of the diff viewer is determined by the 
 * <code>SyncInfoSetCompareConfiguration</code> that is used to create the 
 * <code>SynchronizeCompareInput</code>.
 * 
 * uses the presentation model defined by the configuration.
 * 
 * @since 3.0
 */
public class SynchronizeCompareInput extends CompareEditorInput implements IContentChangeListener {

	private TreeViewerAdvisor diffViewerConfiguration;
	private Viewer diffViewer;
	private NavigationAction nextAction;
	private NavigationAction previousAction;
	
	private boolean buffered = false;

	/**
	 * Create a <code>SynchronizeCompareInput</code> whose diff viewer is configured
	 * using the provided <code>SyncInfoSetCompareConfiguration</code>.
	 * @param configuration the compare configuration 
	 * @param diffViewerConfiguration the diff viewer configuration 
	 */
	public SynchronizeCompareInput(CompareConfiguration configuration, TreeViewerAdvisor diffViewerConfiguration) {
		super(configuration);
		this.diffViewerConfiguration = diffViewerConfiguration;
	}

	public final Viewer createDiffViewer(Composite parent) {
		this.diffViewer = internalCreateDiffViewer(parent, getViewerConfiguration());
		diffViewer.getControl().setData(CompareUI.COMPARE_VIEWER_TITLE, getTitle());
		
		// buffered merge mode, don't ask for save when switching nodes since contents will be buffered in diff nodes
		// and saved when the input is saved.
		if(isBuffered()) {
			getCompareConfiguration().setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean(false));
		}
		
		/*
		 * This viewer can participate in navigation support in compare editor inputs. Note that
		 * it is currently accessing an internal compare interface that should be made public. See
		 * the following bug report https://bugs.eclipse.org/bugs/show_bug.cgi?id=48795.
		 */	
		INavigatable nav= new INavigatable() {
			public boolean gotoDifference(boolean next) {
				return diffViewerConfiguration.navigate(next);
			}
		};
		diffViewer.getControl().setData(INavigatable.NAVIGATOR_PROPERTY, nav);
		
		nextAction = new NavigationAction(true);
		previousAction = new NavigationAction(false);
		nextAction.setCompareEditorInput(this);
		previousAction.setCompareEditorInput(this);
		
		initializeToolBar(diffViewer.getControl().getParent());
		initializeDiffViewer(diffViewer);
		diffViewerConfiguration.navigate(true);
		return diffViewer;
	}

	/**
	 * Create the diff viewer for this compare input. This method simply creates the widget.
	 * Any initialization is performed in the <code>initializeDiffViewer(StructuredViewer)</code>
	 * method. The default diff viewer is a <code>SyncInfoDiffTreeViewer</code>. Subclass may override.
	 * @param parent the parent <code>Composite</code> of the diff viewer to be created
	 * @param diffViewerConfiguration the configuration for the diff viewer
	 * @return the created diff viewer
	 */
	protected StructuredViewer internalCreateDiffViewer(Composite parent, TreeViewerAdvisor diffViewerConfiguration) {
		TreeViewer viewer = new TreeViewerAdvisor.NavigableTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData data = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(data);
		diffViewerConfiguration.initializeViewer(viewer);
		return viewer;
	}

	protected TreeViewerAdvisor getViewerConfiguration() {
		return diffViewerConfiguration;
	}
	
	protected Viewer getViewer() {
		return diffViewer;
	}
	
	/**
	 * Initialize the diff viewer created for this compare input. If a subclass
	 * overrides the <code>createDiffViewer(Composite)</code> method, it should
	 * invoke this method on the created viewer in order to get the proper
	 * labelling in the compare input's contents viewers.
	 * @param viewer the diff viewer created by the compare input
	 */
	protected void initializeDiffViewer(Viewer viewer) {
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
								node.cacheContents(new NullProgressMonitor());
								hookContentChangeListener(node);
							} catch (TeamException e) {
								Utils.handle(e);
							} finally {
								// Update the labels even if the content wasn't fetched correctly. This is
								// required because the selection would still of changed.
								Utils.updateLabels(node.getSyncInfo(), getCompareConfiguration());
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
	
	public void contributeToToolBar(ToolBarManager tbm) {	
		if(nextAction != null && previousAction != null) { 
			tbm.appendToGroup("navigation", nextAction); //$NON-NLS-1$
			tbm.appendToGroup("navigation", previousAction); //$NON-NLS-1$
		}
	}
	
	private void initializeToolBar(Composite parent) {
		ToolBarManager tbm= CompareViewerPane.getToolBarManager(parent);
		if (tbm != null) {
			tbm.removeAll();
			tbm.add(new Separator("navigation")); //$NON-NLS-1$
			contributeToToolBar(tbm);
			IActionBars bars = getActionBars(tbm);
			getViewerConfiguration().setActionBars(bars);
			tbm.update(true);
		}
	}
	
	/* private */ SyncInfoModelElement getElement(ISelection selection) {
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

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected Object prepareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			return getViewerConfiguration().prepareInput(monitor);
		} catch (TeamException e) {
			throw new InvocationTargetException(e);
		}
	}	
	
	/*
	 * (non-Javadoc)
	 * @see CompareEditorInput#saveChanges(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void saveChanges(IProgressMonitor pm) throws CoreException {
		super.saveChanges(pm);
		ISynchronizeModelElement root = (ISynchronizeModelElement)diffViewerConfiguration.getViewer().getInput();
		if (root != null && root instanceof DiffNode) {
			try {
				commit(pm, (DiffNode)root);
			} finally {
				setDirty(false);
			}
		}
	}

	private static void commit(IProgressMonitor pm, DiffNode node) throws CoreException {
		ITypedElement left = node.getLeft();
		if (left instanceof LocalResourceTypedElement)
			 ((LocalResourceTypedElement) left).commit(pm);

		ITypedElement right = node.getRight();
		if (right instanceof LocalResourceTypedElement)
			 ((LocalResourceTypedElement) right).commit(pm);
		
		//node.getC
		IDiffElement[] children = (IDiffElement[])node.getChildren();
		for (int i = 0; i < children.length; i++) {
			commit(pm, (DiffNode)children[i]);			
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.IContentChangeListener#contentChanged(org.eclipse.compare.IContentChangeNotifier)
	 */
	public void contentChanged(IContentChangeNotifier source) {
		try {
			if (isBuffered()) {
				setDirty(true);
			} else if (source instanceof DiffNode) {
				commit(new NullProgressMonitor(), (DiffNode) source);
			} else if (source instanceof LocalResourceTypedElement) {
				 ((LocalResourceTypedElement) source).commit(new NullProgressMonitor());
			}
		} catch (CoreException e) {
			Utils.handle(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return TeamImages.getImageDescriptor(ISharedImages.IMG_COMPARE_VIEW);
	}
	
	/**
	 * Returns <code>true</code> if this compare input will buffer node content changes until the input is saved, 
	 * otherwise content changes are saved to disk immediatly when each node is saved in the content merge viewer.
	 */
	public boolean isBuffered() {
		return buffered;
	}
	
	private IActionBars getActionBars(final IToolBarManager toolbar) {
		return new IActionBars() {
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
			public void setGlobalActionHandler(String actionId, IAction handler) {
			}
			public void updateActionBars() {
			}
		};
	}
}