package org.eclipse.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.tasklist.ITaskListResourceAdapter;

/**
 * The ResourceAdapterUtil is a class that implements default
 * adaptors for each of the resource adaptor types.*/

public class ResourceAdapterUtil {

	/**
	 * Define default implementation of the resource adaptors
	 * used within org.eclipse.ui.
	 */

	private static ITaskListResourceAdapter taskAdapter =
		new ITaskListResourceAdapter() {
		/*
		* @see ITaskListResourceAdapter#getAffectedResource(IAdaptable)
		*/
		public IResource getAffectedResource(IAdaptable adaptable) {
			IResource resource = getResource(adaptable);
			if (resource == null)
				return (IFile) adaptable.getAdapter(IFile.class);
			else
				return resource;
		}
	};

	private static IContributorResourceAdapter resourceAdapter =
		new IContributorResourceAdapter() {
		/*
		* @see IContributorResourceAdapter#getAdaptedResource(IAdaptable)
		*/
		public IResource getAdaptedResource(IAdaptable adaptable) {
			return getResource(adaptable);
		}
	};

	/**
	 * Return the resource the adapter adapts to.
	 */

	private static IResource getResource(IAdaptable adaptable) {

		return (IResource) adaptable.getAdapter(IResource.class);
	}

	/**
	 * Return the default task adapter.
	 */
	public static ITaskListResourceAdapter getTaskAdapter() {
		return taskAdapter;
	}


	/**
	 * Return the default resource contribution adapter.
	 */
	public static IContributorResourceAdapter getResourceAdapter() {
		return resourceAdapter;
	}

}