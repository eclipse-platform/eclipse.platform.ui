package org.eclipse.ui.views.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;

public interface IResourceNavigatorPart extends IViewPart {

	/**
	 * Returns the pattern filter for this navigator.
	 *
	 * @return the pattern filter
	 */
	ResourcePatternFilter getPatternFilter();

	/**
	 * Set the current sorter.
	 */
	void setResourceSorter(ResourceSorter sorter);

	/**
	 * Get the resource sorter used by the receiver.
	 */

	ResourceSorter getResourceSorter();

	/**
	 * Returns the shell to use for opening dialogs.
	 * Used in this class, and in the actions.
	 */
	Shell getShell();

	/**
	 * Set the values of the filter preference to be the 
	 * strings in preference values
	 */

	void setFiltersPreference(String[] patterns);
	
	/**
	 * Returns the viewer which shows the resource hierarchy.
	 */
	Viewer getResourceViewer();

}