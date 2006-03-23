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
package org.eclipse.team.internal.ccvs.ui.mappings;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.core.mapping.CompoundResourceTraversal;
import org.eclipse.team.internal.core.subscribers.DiffChangeSet;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.mapping.ResourceModelTraversalCalculator;
import org.eclipse.team.internal.ui.mapping.SynchronizationResourceMappingContext;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ModelParticipantAction;
import org.eclipse.ui.ide.IDE;

public abstract class CVSModelProviderAction extends ModelParticipantAction {

	public CVSModelProviderAction(ISynchronizePageConfiguration configuration) {
		super(null, configuration);
		Utils.initAction(this, getBundleKeyPrefix(), Policy.getActionBundle());
	}
	
	/**
	 * Return the key to the action text in the resource bundle.
	 * The default is the fully qualified class name followed by a dot (.).
	 * @return the bundle key prefix
	 */
	protected String getBundleKeyPrefix() {
		return getClass().getName()  + "."; //$NON-NLS-1$
	}
	
	protected ResourceTraversal[] getResourceTraversals(IStructuredSelection selection, IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(null, selection.size() * 100);
			CompoundResourceTraversal traversal = new CompoundResourceTraversal();
			if (selection instanceof ITreeSelection) {
				ITreeSelection ts = (ITreeSelection) selection;
				TreePath[] paths = ts.getPaths();
				for (int i = 0; i < paths.length; i++) {
					TreePath path = paths[i];
					ResourceTraversal[] traversals = getTraversals(path, Policy.subMonitorFor(monitor, 100));
					traversal.addTraversals(traversals);
				}
			} else {
				for (Iterator iter = selection.iterator(); iter.hasNext();) {
					Object element = (Object) iter.next();
					ResourceTraversal[] traversals = getTraversals(element, Policy.subMonitorFor(monitor, 100));
					traversal.addTraversals(traversals);
				}
			}
			return traversal.asTraversals();
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Return a traversal that includes the resources that are visible 
	 * in the sync view.
	 * @param element the selected element
	 * @return a set of traversals that cover the visible resources.
	 */
	private ResourceTraversal[] getTraversals(Object element, IProgressMonitor monitor) throws CoreException {
		ResourceMapping mapping = Utils.getResourceMapping(element);
		if (mapping != null)
			return mapping.getTraversals(getResourceMappingContext(), monitor);
		return null;
	}

	protected ResourceMappingContext getResourceMappingContext() {
		return new SynchronizationResourceMappingContext(getSynchronizationContext());
	}
	
	protected ResourceModelTraversalCalculator getTraversalCalculator() {
		return (ResourceModelTraversalCalculator)getConfiguration().getProperty(ResourceModelTraversalCalculator.PROP_TRAVERSAL_CALCULATOR);
	}

	/**
	 * Return a traversal that includes the resources that are visible 
	 * in the sync view.
	 * @param element the selected element
	 * @return a set of traversals that cover the visible resources.
	 */
	private ResourceTraversal[] getTraversals(TreePath path, IProgressMonitor monitor) throws CoreException {
		if (path.getSegmentCount() > 0) {
			DiffChangeSet set = getChangeSet(path);
			Object o = path.getLastSegment();
			if (set != null) {
				if (path.getSegmentCount() == 1) {
					return new ResourceTraversal[] { new ResourceTraversal(set.getResources(), IResource.DEPTH_ZERO, IResource.NONE) };
				}
				if (o instanceof IResource) {
					IResource resource = (IResource) o;
					int depth = getTraversalCalculator().getLayoutDepth(resource, path);
					IDiff[] diffs = set.getDiffTree().getDiffs(resource, depth);
					Set resources = new HashSet();
					for (int i = 0; i < diffs.length; i++) {
						IDiff diff = diffs[i];
						IResource r = ResourceDiffTree.getResourceFor(diff);
						if (r != null)
							resources.add(r);
					}
					return new ResourceTraversal[] { new ResourceTraversal((IResource[]) resources.toArray(new IResource[resources.size()]), IResource.DEPTH_ZERO, IResource.NONE) };
				}
			} 
			if (getTraversalCalculator().isResourcePath(path)) {
				IResource resource = (IResource) o;
				return getTraversalCalculator().getTraversals(resource, path);
			}
			return getTraversals(o, monitor);
		}
		return null;
	}

	private DiffChangeSet getChangeSet(TreePath path) {
		Object o = path.getFirstSegment();
		if (o instanceof DiffChangeSet) {
			return (DiffChangeSet) o;
		}
		return null;
	}

	protected ResourceMapping[] getResourceMappings(IStructuredSelection selection) {
		List mappings = new ArrayList();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			ResourceMapping mapping = Utils.getResourceMapping(element);
			if (mapping != null)
				mappings.add(mapping);
		}
		return (ResourceMapping[]) mappings.toArray(new ResourceMapping[mappings.size()]);
	}
	
	/**
	 * Prompt to save all dirty editors and return whether to proceed
	 * or not.
	 * @return whether to proceed
	 * or not
	 */
	public final boolean saveDirtyEditors() {
		if(needsToSaveDirtyEditors()) {
			if(!saveAllEditors(getTargetResources(), confirmSaveOfDirtyEditor())) {
				return false;
			}
		}
		return true;
	}
	
	protected IResource[] getTargetResources() {
		IStructuredSelection selection = getStructuredSelection();
		Object[] objects = selection.toArray();
		Set roots = new HashSet();
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			ResourceMapping mapping = Utils.getResourceMapping(object);
			if (mapping != null) {
				try {
					ResourceTraversal[] traversals = mapping.getTraversals(ResourceMappingContext.LOCAL_CONTEXT, null);
					for (int j = 0; j < traversals.length; j++) {
						ResourceTraversal traversal = traversals[j];
						IResource[] resources = traversal.getResources();
						for (int k = 0; k < resources.length; k++) {
							IResource resource = resources[k];
							roots.add(resource);
						}
					}
				} catch (CoreException e) {
					TeamUIPlugin.log(e);
				}
			}
		}
		return (IResource[]) roots.toArray(new IResource[roots.size()]);
	}

	/**
	 * Save all dirty editors in the workbench that are open on files that may
	 * be affected by this operation. Opens a dialog to prompt the user if
	 * <code>confirm</code> is true. Return true if successful. Return false
	 * if the user has canceled the command. Must be called from the UI thread.
	 * @param resources the root resources being operated on
	 * @param confirm prompt the user if true
	 * @return boolean false if the operation was canceled.
	 */
	public final boolean saveAllEditors(IResource[] resources, boolean confirm) {
		return IDE.saveAllEditors(resources, confirm);
	}

	/**
	 * Return whether dirty editor should be saved before this action is run.
	 * Default is <code>true</code>.
	 * 
	 * @return whether dirty editor should be saved before this action is run
	 */
	protected boolean needsToSaveDirtyEditors() {
		return true;
	}
	
	/**
	 * Returns whether the user should be prompted to save dirty editors. The
	 * default is <code>true</code>.
	 * 
	 * @return whether the user should be prompted to save dirty editors
	 */
	protected boolean confirmSaveOfDirtyEditor() {
		return true;
	}
	
	public void run() {
		if (saveDirtyEditors())
			execute();
	}

	protected abstract void execute();
}
