package org.eclipse.ui.views.tasklist;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * The ITaskListResourceAdapter is a class that defines
 * the way that objects with markers for the task list
 * supply resources for use by the task list.
 */

public interface ITaskListResourceAdapter {

	/**
	 * Get the resource that is affected by changed
	 * to object.
	 * @return <code>IResource</codee> or <code>null</code> if there
	 * 	is no adapted resource for this type.
	 * @param IAdaptable the adaptable being queried.
	 */

	public IResource getAffectedResource(IAdaptable adaptable);



}