/*******************************************************************************
 * Copyright (c) 2010 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * 
 * A pipelined content provider allows an extension to reshape the contributions
 * of an upstream content extension.
 * 
 * An "upstream" extension is either:
 * <ul>
 * <li>the extension overridden by this extension using the
 * <b>org.eclipse.ui.navigatorContent/navigatorContent/override</b> element, or</li>
 * <li>another extension that overrides the same extension this extension
 * overrides, but with higher priority than this extension.</li>
 * </ul>
 * 
 * Overridden extensions form a tree where the nodes of the tree represent the
 * content extensions, children represent overriding extensions, and the
 * children are sorted by priority. Pipeline contributions traverse the tree,
 * allowing children to override the contributions of their parent, giving
 * precedence to the children of highest priority.
 * 
 * {@link ITreeContentProvider} is respected by the Common Navigator.
 * 
 * Note: this should be used instead of {@link IPipelinedTreeContentProvider} so
 * that the hasChildren indication reflects the actual pipelined children that
 * will be presented.
 * 
 * @see INavigatorPipelineService
 * @see INavigatorContentService#getPipelineService()
 * @since 3.5
 * 
 */
public interface IPipelinedTreeContentProvider2 extends IPipelinedTreeContentProvider {

	/**
	 * Intercept the fact of having children and optionally modify this. This
	 * calculation should match whether children will be actually provided.
	 * 
	 * @param anInput
	 *            An input from the viewer
	 * @param currentHasChildren
	 *            The current proposed setting of hasChildren thus far from the
	 *            upstream content providers.
	 * @return The new value for hasChildren
	 */
	boolean hasPipelinedChildren(Object anInput, boolean currentHasChildren);

}
