/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * A <code>Saveable</code> represents a unit of saveability, e.g. an editable
 * subset of the underlying domain model that may contain unsaved changes.
 * Different workbench parts (editors and views) may present the same saveables
 * in different ways. This interface allows the workbench to provide more
 * appropriate handling of operations such as saving and closing workbench
 * parts. For example, if two editors sharing the same saveable with unsaved
 * changes are closed simultaneously, the user is only prompted to save the
 * changes once for the shared saveable, rather than once for each editor.
 * <p>
 * Workbench parts that work in terms of saveables should implement
 * {@link ISaveablesSource}.
 * </p>
 * 
 * @see ISaveablesSource
 * @since 3.2
 */
public abstract class Saveable {
	
	/**
	 * Attempts to show this saveable in the given page and returns <code>true</code>
	 * on success. The default implementation does nothing and returns <code>false</code>.
	 * 
	 * @param page the workbench page in which to show this saveable  
	 * @return <code>true</code> if this saveable is now visible to the user
	 * @since 3.3
	 */
	public boolean show(IWorkbenchPage page) {
		return false;
	}

	/**
	 * Returns the name of this saveable for display purposes.
	 * 
	 * @return the model's name; never <code>null</code>.
	 */
	public abstract String getName();

	/**
	 * Returns the tool tip text for this saveable. This text is used to
	 * differentiate between two inputs with the same name. For instance,
	 * MyClass.java in folder X and MyClass.java in folder Y. The format of the
	 * text varies between input types.
	 * 
	 * @return the tool tip text; never <code>null</code>
	 */
	public abstract String getToolTipText();

	/**
	 * Returns the image descriptor for this saveable.
	 * 
	 * @return the image descriptor for this model; may be <code>null</code>
	 *         if there is no image
	 */
	public abstract ImageDescriptor getImageDescriptor();

	/**
	 * Saves the contents of this saveable.
	 * <p>
	 * If the save is cancelled through user action, or for any other reason,
	 * the part should invoke <code>setCancelled</code> on the
	 * <code>IProgressMonitor</code> to inform the caller.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided by
	 * the given progress monitor.
	 * </p>
	 * 
	 * @param monitor
	 *            the progress monitor
	 * @throws CoreException
	 *             if the save fails; it is the caller's responsibility to
	 *             report the failure to the user
	 */
	public abstract void doSave(IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns whether the contents of this saveable have changed since the last
	 * save operation.
	 * <p>
	 * <b>Note:</b> this method is called frequently, for example by actions to
	 * determine their enabled status.
	 * </p>
	 * 
	 * @return <code>true</code> if the contents have been modified and need
	 *         saving, and <code>false</code> if they have not changed since
	 *         the last save
	 */
	public abstract boolean isDirty();

	/**
	 * Clients must implement equals and hashCode as defined in
	 * {@link Object#equals(Object)} and {@link Object#hashCode()}. Two
	 * saveables should be equal if their dirty state is shared, and saving one
	 * will save the other. If two saveables are equal, their names, tooltips,
	 * and images should be the same because only one of them will be shown when
	 * prompting the user to save.
	 * 
	 * @param object
	 * @return true if this Saveable is equal to the given object
	 */
	public abstract boolean equals(Object object);

	/**
	 * Clients must implement equals and hashCode as defined in
	 * {@link Object#equals(Object)} and {@link Object#hashCode()}. Two
	 * saveables should be equal if their dirty state is shared, and saving one
	 * will save the other. If two saveables are equal, their hash codes MUST be
	 * the same, and their names, tooltips, and images should be the same
	 * because only one of them will be shown when prompting the user to save.
	 * <p>
	 * IMPORTANT: Implementers should ensure that the hashCode returned is
	 * sufficiently unique so as not to collide with hashCodes returned by other
	 * implementations. It is suggested that the defining plug-in's ID be used
	 * as part of the returned hashCode, as in the following example:
	 * </p>
	 * 
	 * <pre>
	 *   int PRIME = 31;
	 *   int hash = ...; // compute the &quot;normal&quot; hash code, e.g. based on some identifier unique within the defining plug-in
	 *   return hash * PRIME + MY_PLUGIN_ID.hashCode();
	 * </pre>
	 * 
	 * @return a hash code
	 */
	public abstract int hashCode();

}
