package org.eclipse.ui.views.navigator;

import org.eclipse.jface.viewers.TreeViewer;

public interface IResourceTreeNavigatorPart extends IResourceNavigatorPart {

	/**
	 * Returns the tree viewer which shows the resource hierarchy.
	 */
	TreeViewer getTreeViewer();

}