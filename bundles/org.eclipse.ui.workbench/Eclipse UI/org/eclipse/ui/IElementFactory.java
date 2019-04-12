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
package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;

/**
 * A factory for re-creating objects from a previously saved memento.
 * <p>
 * Clients should implement this interface and include the name of their class
 * in an extension to the platform extension point named
 * <code>"org.eclipse.ui.elementFactories"</code>. For example, the plug-in's
 * XML markup might contain:
 * </p>
 *
 * <pre>
 * &lt;extension point="org.eclipse.ui.elementFactories"&gt;
 *    &lt;factory id="com.example.myplugin.MyFactory" class="com.example.myplugin.MyFactory" /&gt;
 * &lt;/extension&gt;
 * </pre>
 *
 * @see IPersistableElement
 * @see IMemento
 * @see org.eclipse.ui.IWorkbench#getElementFactory
 */
public interface IElementFactory {
	/**
	 * Re-creates and returns an object from the state captured within the given
	 * memento.
	 * <p>
	 * If the result is not null, it should be persistable; that is,
	 * </p>
	 * 
	 * <pre>
	 * result.getAdapter(org.eclipse.ui.IPersistableElement.class)
	 * </pre>
	 * <p>
	 * should not return <code>null</code>.
	 * </p>
	 *
	 * @param memento a memento containing the state for the object
	 * @return an object, or <code>null</code> if the element could not be created
	 */
	IAdaptable createElement(IMemento memento);
}
