package org.eclipse.ui.views.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.views.framelist.FrameList;

/**
 * This interface defines the API for the resource navigator.
 * The action groups restrict themselves to using this API.
 * 
 * @since 2.0
 */
public interface IResourceNavigatorPart extends IViewPart {

	/**
	 * Returns the pattern filter for this navigator.
	 *
	 * @return the pattern filter
	 */
	ResourcePatternFilter getPatternFilter();

	/**
	 * Returns the working set for this view.
	 *
	 * @return the working set
	 * @since 2.0
	 */
	IWorkingSet getWorkingSet();

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
	
	/**
	 * Returns the frame list for this navigator.
	 */
	FrameList getFrameList();
	
	/**
	 * Sets the working set for this view.
	 * 
	 * @param workingSet the working set
	 * @since 2.0
	 */
	void setWorkingSet(IWorkingSet workingSet);
}