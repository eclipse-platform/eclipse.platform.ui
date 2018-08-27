/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A default content provider to present a hierarchy of <code>IChange</code>
 * objects in a tree viewer.
 */
class ChangeElementContentProvider  implements ITreeContentProvider {

	/* non Java-doc
	 * @see ITreeContentProvider#inputChanged
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}

	/* non Java-doc
	 * @see ITreeContentProvider#getChildren
	 */
	@Override
	public Object[] getChildren(Object o) {
		PreviewNode element= (PreviewNode)o;
		return element.getChildren();
	}

	/* non Java-doc
	 * @see ITreeContentProvider#getParent
	 */
	@Override
	public Object getParent(Object element){
		return ((PreviewNode)element).getParent();
	}

	/* non Java-doc
	 * @see ITreeContentProvider#hasChildren
	 */
	@Override
	public boolean hasChildren(Object element){
		Object[] children= getChildren(element);
		return children != null && children.length > 0;
	}

	/* non Java-doc
	 * @see ITreeContentProvider#dispose
	 */
	@Override
	public void dispose(){
	}

	/* non Java-doc
	 * @see ITreeContentProvider#getElements
	 */
	@Override
	public Object[] getElements(Object element){
		return getChildren(element);
	}
}
