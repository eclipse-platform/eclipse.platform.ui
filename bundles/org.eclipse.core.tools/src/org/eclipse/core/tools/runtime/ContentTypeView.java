/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.tools.runtime;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.internal.content.ContentTypeManager;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.tools.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Content type Spy view. This view shows detailed information about the currently 
 * the content type registry . 
 * 
 */
public class ContentTypeView extends SpyView implements IAdaptable {

	private static class ContentTypeContentProvider extends AbstractTreeContentProvider {

		public ContentTypeContentProvider() {
			super(true);
		}

		protected boolean acceptInput(Object input) {
			return true;
		}

		private TreeContentProviderNode addContentType(IContentType type, Set visited) {
			ContentTypePropertySource wrapped = new ContentTypePropertySource(type);
			if (!visited.add(wrapped))
				return getNodeFor(wrapped);
			IContentType base = type.getBaseType();
			TreeContentProviderNode newNode = createNode(null, wrapped);
			if (base == null) {
				getRootNode().addChild(newNode);
				return newNode;
			}
			TreeContentProviderNode baseTypeNode = addContentType(base, visited);
			baseTypeNode.addChild(newNode);
			return newNode;
		}

		private TreeContentProviderNode getNodeFor(Object type) {
			return getRootNode().findNode(type);
		}

		protected void rebuild(Viewer viewer, Object input) {
			IContentType[] allTypes = ContentTypeManager.getInstance().getAllContentTypes();
			Set visited = new HashSet(allTypes.length);
			for (int i = 0; i < allTypes.length; i++)
				addContentType(allTypes[i], visited);
		}
	}

	/** JFace's tree component used to present resource details. */
	private AbstractTreeViewer viewer;

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ContentTypeContentProvider());
		viewer.setInput(""); //$NON-NLS-1$
		getSite().setSelectionProvider(viewer);
	}
}