package org.eclipse.ui.views.tasklist;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * The ITaskListResourceContributor is a class that defines
 * the way that objects with markers for the task list
 * supply resources for use by the task list.
 */

public interface ITaskListResourceAdapter {

	/**
	 * Get the resource that is affected by changed
	 * to object.
	 */

	public IResource getAffectedResource(IAdaptable adaptable);



}