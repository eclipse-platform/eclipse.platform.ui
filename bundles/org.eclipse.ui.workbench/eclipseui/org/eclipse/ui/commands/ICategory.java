/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui.commands;

/**
 * <p>
 * A category is a grouping of commands by functional area. For example, in the
 * Eclipse workbench, "Text Editing" is a category containing various commands
 * related to text editing. A category's primary functionality is to control the
 * display of commands to the user. When appropriate, commands displayed to the
 * user (e.g., keys preference page) will be grouped by category.
 * </p>
 * <p>
 * An instance of <code>ICategory</code> is a handle representing a category as
 * defined by the extension point <code>org.eclipse.ui.commands</code>. The
 * identifier of the handle is identifier of the category being represented.
 * </p>
 * <p>
 * An instance of <code>ICategory</code> can be obtained from an instance of
 * <code>ICommandManager</code> for any identifier, whether or not a category
 * with that identifier defined in the plugin registry.
 * </p>
 * <p>
 * The handle-based nature of this API allows it to work well with runtime
 * plugin activation and deactivation, which causes dynamic changes to the
 * plugin registry, and therefore, potentially, dynamic changes to the set of
 * category definitions.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 *
 * @since 3.0
 * @see ICategoryListener
 * @see ICommandManager
 * @see org.eclipse.core.commands.Category
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead. This
 *             API is scheduled for deletion, see Bug 431177 for details
 * @noimplement This interface is not intended to be implemented by clients.
 * @noreference This interface is scheduled for deletion.
 * @noextend This interface is not intended to be extended by clients.
 */
@Deprecated
@SuppressWarnings("all")
public interface ICategory extends Comparable {

	/**
	 * Registers an instance of <code>ICategoryListener</code> to listen for changes
	 * to attributes of this instance.
	 *
	 * @param categoryListener the instance of <code>ICategoryListener</code> to
	 *                         register. Must not be <code>null</code>. If an
	 *                         attempt is made to register an instance of
	 *                         <code>ICategoryListener</code> which is already
	 *                         registered with this instance, no operation is
	 *                         performed.
	 */
	@Deprecated
	void addCategoryListener(ICategoryListener categoryListener);

	/**
	 * <p>
	 * Returns the description of the category represented by this handle, suitable
	 * for display to the user.
	 * </p>
	 * <p>
	 * Notification is sent to all registered listeners if this attribute changes.
	 * </p>
	 *
	 * @return the description of the category represented by this handle.
	 *         Guaranteed not to be <code>null</code>.
	 * @throws NotDefinedException if the category represented by this handle is not
	 *                             defined.
	 */
	@Deprecated
	String getDescription() throws NotDefinedException;

	/**
	 * Returns the identifier of this handle.
	 *
	 * @return the identifier of this handle. Guaranteed not to be
	 *         <code>null</code>.
	 */
	@Deprecated
	String getId();

	/**
	 * <p>
	 * Returns the name of the category represented by this handle, suitable for
	 * display to the user.
	 * </p>
	 * <p>
	 * Notification is sent to all registered listeners if this attribute changes.
	 * </p>
	 *
	 * @return the name of the category represented by this handle. Guaranteed not
	 *         to be <code>null</code>.
	 * @throws NotDefinedException if the category represented by this handle is not
	 *                             defined.
	 */
	@Deprecated
	String getName() throws NotDefinedException;

	/**
	 * <p>
	 * Returns whether or not the category represented by this handle is defined.
	 * </p>
	 * <p>
	 * Notification is sent to all registered listeners if this attribute changes.
	 * </p>
	 *
	 * @return <code>true</code>, iff the category represented by this handle is
	 *         defined.
	 */
	@Deprecated
	boolean isDefined();

	/**
	 * Unregisters an instance of <code>ICategoryListener</code> listening for
	 * changes to attributes of this instance.
	 *
	 * @param categoryListener the instance of <code>ICategoryListener</code> to
	 *                         unregister. Must not be <code>null</code>. If an
	 *                         attempt is made to unregister an instance of
	 *                         <code>ICategoryListener</code> which is not already
	 *                         registered with this instance, no operation is
	 *                         performed.
	 */
	@Deprecated
	void removeCategoryListener(ICategoryListener categoryListener);
}
