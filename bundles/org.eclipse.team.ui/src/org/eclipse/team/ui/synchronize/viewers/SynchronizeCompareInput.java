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

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.NavigationAction;
import org.eclipse.compare.internal.INavigatable;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.Utils;
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
public class SynchronizeCompareInput extends CompareEditorInput {

	private TreeViewerAdvisor diffViewerConfiguration;
	private Viewer diffViewer;
	private NavigationAction nextAction;
	private NavigationAction previousAction;

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
}
