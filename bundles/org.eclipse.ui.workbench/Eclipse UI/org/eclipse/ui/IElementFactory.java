/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * <code>"org.eclipse.ui.elementFactories"</code>.
 * For example, the plug-in's XML markup might contain:
 * <pre>
 * &LT;extension point="org.eclipse.ui.elementFactories"&GT;
 *    &LT;factory id="com.example.myplugin.MyFactory" class="com.example.myplugin.MyFactory" /&GT; 
 * &LT;/extension&GT;
 * </pre>
 * </p>
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
	 * <pre>
	 * result.getAdapter(org.eclipse.ui.IPersistableElement.class)
	 * </pre>
	 * should not return <code>null</code>.
	 * </p>
	 * 
	 * @param memento
	 *            a memento containing the state for the object
	 * @return an object, or <code>null</code> if the element could not be
	 *         created
	 */
    public IAdaptable createElement(IMemento memento);
}
