/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.launchConfigurations;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.Target;
import org.eclipse.ant.core.TargetInfo;
import org.eclipse.ant.internal.ui.model.AntTargetNode;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class AntTargetContentProvider implements IStructuredContentProvider {

	protected List elements = new ArrayList();
	protected TableViewer viewer;
	private boolean fFilterInternalTargets= false;
	private int fNumFilteredTargets= 0;
	private int fNumTotalTargets= 0;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		fNumTotalTargets= elements.size();
		fNumFilteredTargets= 0;
		if (fNumTotalTargets == 0) {
			return new Object[0];
		}
		if (!fFilterInternalTargets) {
			return elements.toArray(new Object[fNumTotalTargets]);
		}
		Iterator iter= elements.iterator();
		while (iter.hasNext()) { 
			if (isInternal((AntTargetNode) iter.next())) {
				fNumFilteredTargets++;
			}
		}
		Object[] targets= new Object[getNumTargets()];
		iter= elements.iterator();
		int i= 0;
		while (iter.hasNext()) {
			AntTargetNode target= (AntTargetNode) iter.next(); 
			if (!isInternal(target)) {
				targets[i++]= target;  
			}
		}
		return targets;
	}
	
	/**
	 * Returns whether the given target is an internal target. Internal
	 * targets are targets which has no description. The default target
	 * is never considered internal.
	 * @param target the target to examine
	 * @return whether the given target is an internal target
	 */
	public boolean isInternal(AntTargetNode node) {
		Target target= node.getTarget();
		return target.getDescription() != null || node.isDefaultTarget();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer newViewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer) newViewer;
		elements.clear();
		if (newInput != null && ((Object[]) newInput).length != 0) {
			elements.addAll(Arrays.asList((Object[]) newInput));
		}
	}

	/**
	 * Moves the given target up in the list of active targets. Has no effect if
	 * the given target is already the first target in the list or the given
	 * index is invalid.
	 * 
	 * @param index the index of the target to move up
	 */
	public void moveUpTarget(int index) {
		Object target = elements.get(index);
		if (index == 0 || target == null) {
			return;
		}
		elements.set(index, elements.get(index - 1));
		elements.set(index - 1, target);
	}

	/**
	 * Moves the given target down in the list of active targets. Has no effect
	 * if the given target is already the last target in the list or the given
	 * index is invalid.
	 *
	 * @param index the index of the target to move down
	 */
	public void moveDownTarget(int index) {
		Object target = elements.get(index);
		if (index == elements.size() - 1 || target == null) {
			return;
		}
		elements.set(index, elements.get(index + 1));
		elements.set(index + 1, target);
	}
	
	/**
	 * Returns the number of targets filtered out of the list
	 * of targets last returned by this content provider
	 * @return the number of targets filtered out of the last request
	 *  for targets
	 */
	public int getNumFiltered() {
		return fNumFilteredTargets;
	}
	
	/**
	 * Returns the number of targets returned the last time this content
	 * provider was queried. If targets were filtered out, those targets are
	 * not included in this count.
	 * @return the number of targets returned the last time targets
	 *  were requested
	 */
	public int getNumTargets() {
		return fNumTotalTargets - fNumFilteredTargets;
	}
	
	/**
	 * Sets whether this content provider should filter out internal targets.
	 * Internal targets are targets which have no description. If set <code>true</code>,
	 * targets with no description will not be returned when getElements() is called.
	 * @param filter sets whether internal targets should be filtered out
	 */
	public void setFilterInternalTargets(boolean filter) {
		fFilterInternalTargets= filter;
	}
	
	/**
	 * Returns whether the given target is an internal target. Internal
	 * targets are targets which has no description. The default target
	 * is never considered internal.
	 * @param target the target to examine
	 * @return whether the given target is an internal target
	 * @deprecated to be deleted
	 */
	public boolean isInternal(TargetInfo target) {
		//TODO to be deleted
		return !target.isDefault() && target.getDescription() == null;
	}
}
