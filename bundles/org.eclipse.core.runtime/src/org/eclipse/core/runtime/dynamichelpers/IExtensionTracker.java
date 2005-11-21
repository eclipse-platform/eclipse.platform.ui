/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.dynamichelpers;

import org.eclipse.core.internal.runtime.ReferenceHashSet;
import org.eclipse.core.runtime.IExtension;

/**
 * An extension tracker keeps associations between extensions and their derived objects on an extension basis.
 * All extensions being added in a tracker will automatically be removed when the extension is uninstalled from the registry.
 * Users interested in extension removal can register a handler that will let them know when an object is being removed.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.1
 */
public interface IExtensionTracker {

	/**
	 * Constant for strong (normal) reference holding.
	 * 
	 * Value <code>1</code>.
	 */
	public static final int REF_STRONG = ReferenceHashSet.HARD;

	/**
	 * Constant for soft reference holding.
	 * 
	 * Value <code>2</code>.
	 */
	public static final int REF_SOFT = ReferenceHashSet.SOFT;

	/**
	 * Constant for weak reference holding.
	 * 
	 * Value <code>3</code>.
	 */
	public static final int REF_WEAK = ReferenceHashSet.WEAK;

	/**
	 * Register an extension change handler with this tracker using the given filter
	 * @param handler the handler to be registered
	 * @param filter the filter to use to choose interesting changes
	 */
	public void registerHandler(IExtensionChangeHandler handler, IFilter filter);

	/**
	 * Unregister the given extension change handler previously registered with this tracker
	 * @param handler the handler to be unregistered 
	 */
	public void unregisterHandler(IExtensionChangeHandler handler);

	/**
	 * Create an association between the given extension and the given object.
	 * The referenceType indicates how strongly the object is being kept in memory.
	 * There is 3 possible values: strong, soft, weak. 
	 * @param extension : an extension 
	 * @param object : the object to associate with the extension
	 * @param referenceType : one of REF_STRONG, REF_SOFT, REF_WEAK 
	 */
	public void registerObject(IExtension extension, Object object, int referenceType);

	/**
	 * Remove an association between the given extension and the given object.
	 * @param extension : the extension under which the object has been registered
	 * @param object : the object to unregister
	 */
	public void unregisterObject(IExtension extension, Object object);

	/**
	 * Remove all the objects associated with the given extension and return them.
	 * @param extension : the extension for which the objects are removed
	 * @return the objects that were associated with the extension.
	 */
	public Object[] unregisterObject(IExtension extension);

	/**
	 * Get all the objects that have been associated with the given extension.
	 * All objects registered strongly will be return unless they have been unregistered.
	 * The objects registered softly or weakly may not be returned if they have been garbage collected. 
	 * @param extension the extension for which the object must be returned 
	 * @return an array of objects associated with this extension, or an empty array if no association exists 
	 */
	public Object[] getObjects(IExtension extension);

	/**
	 * Close the tracker. All registered objects are freed and all handlers are being automatically removed.
	 */
	public void close();
}
