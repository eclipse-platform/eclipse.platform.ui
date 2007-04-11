/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.navigator;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.AbstractTreeViewer;

/**
 * 
 * A pipelined viewer update should map requests to refresh or update elements
 * in the viewer to their correct, modified structure. Clients use
 * {@link PipelinedViewerUpdate} as the input and return type from intercept
 * methods on {@link IPipelinedTreeContentProvider}.
 * 
 * <p>
 * Clients should use the viewer update to describe how the request from the
 * upstream extension (see {@link IPipelinedTreeContentProvider} for more
 * information on <i>upstream</i> extensions) should be reshaped when applied
 * to the tree. A request from an upstream extension to refresh a given element
 * could result in multiple refresh requests from downstream extensions.
 * Therefore, the refresh targets are modeled as a set.
 * </p>
 * <p>
 * Initially, this set will contain the original element that was passed to the
 * refresh requests. Clients may squash the refresh by clearing the set, change
 * the original target by removing the current element and adding a new target,
 * or expand the refresh by adding more elements to the set.
 * </p>
 * <p>
 * A pipelined extension may receive a {@link PipelinedViewerUpdate} as the
 * result of a call to {@link AbstractTreeViewer#refresh()}-methods or
 * {@link AbstractTreeViewer#update(Object, String[])}-methods. The
 * <code>properties</code> field is only applicable for <code>update()</code>
 * calls and the <code>updateLabels</code> field is only applicable for
 * <code>refresh()</code> calls.
 * </p>
 *  
 * @since 3.2
 * 
 */
public final class PipelinedViewerUpdate {

	private static final String[] NO_PROPERTIES = new String[0];

	private final Set refreshTargets = new LinkedHashSet();

	private boolean updateLabels = false;

	private Map properties;

	/**
	 * Properties allow optimization for <code>update</code> calls.
	 * 
	 * @param aTarget
	 *            The target which may have specific properties associated with
	 *            it for an optimized refresh.
	 * 
	 * @return Returns the properties for the given target. If no properties are
	 *         specified, then an empty array is returned. <b>null</b> will
	 *         never be returned.
	 */
	public final String[] getProperties(Object aTarget) {
		if (properties != null && properties.containsKey(aTarget)) {
			String[] props = (String[]) properties.get(aTarget);
			return props != null ? props : NO_PROPERTIES;
		}
		return NO_PROPERTIES;
	}

	/**
	 * 
	 * Properties allow optimization for <code>update</code> calls.
	 * 
	 * @param aTarget
	 *            The target of the properties.
	 * @param theProperties
	 *            The properties to pass along to the <code>update</code>
	 *            call.
	 * @see AbstractTreeViewer#update(Object, String[])
	 */
	public final void setProperties(Object aTarget, String[] theProperties) {
		if (theProperties != null && theProperties.length > 0) {
			if (properties == null) {
				properties = new HashMap();
			}
			properties.put(aTarget, theProperties);

		} else {
			properties.remove(aTarget);
		}

		if (properties.size() == 0) {
			properties = null;
		}

	}

	/**
	 * @return Returns the current set of refresh targets. Clients may add or
	 *         remove directly to or from this set.
	 */
	public final Set getRefreshTargets() {
		return refreshTargets;
	}

	/**
	 * @return Returns the true if the labels should also be updated during the
	 *         <code>refresh</code>.
	 */
	public final boolean isUpdateLabels() {
		return updateLabels;
	}

	/**
	 * @param toUpdateLabels
	 *            True indicates that calls to <code>refresh</code> should
	 *            force the update of the labels in addition to refreshing the
	 *            structure.
	 */
	public final void setUpdateLabels(boolean toUpdateLabels) {
		updateLabels = toUpdateLabels;
	}

}
