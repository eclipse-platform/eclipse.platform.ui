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
package org.eclipse.ui.internal.part.components.services;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.ServiceFactory;
import org.eclipse.ui.internal.part.Part;

/**
 * Used to create instances of editors and views. Not intended to be implemented by clients.
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public interface IWorkbenchPartFactory {
    /**
     * Creates an instance of a view. Returns an <code>ISite</code> for the newly created view.
     * When the caller is done with the part it should dispose the part's main control. This can
     * be accomplished by calling <code>ISite.getControl().dispose()</code>, or by disposing the 
     * parent composite.
     * @param viewId ID of the view, as registered with the org.eclipse.ui.views extension point
     * @param parentComposite parent composite for the view. If the view is successfully created, it
     *        will create exactly one new child control in this composite. 
     * @param savedState previously saved state of the part, or null if none
     * @param context local context for the view. This object can override any or all of the view's dependencies.
     *        If the view has a dependency that isn't found in the local context, a default implementation will
     *        be supplied by the org.eclipse.core.component.types extension point.
     *
     * @return an ISite for the newly created view
     * @throws ComponentException if unable to create the part
     */
	public Part createView(String viewId, Composite parentComposite, IMemento savedState, ServiceFactory context) throws ComponentException;
    
    /**
     * Creates an instance of an editor. Returns an <code>ISite</code> for the newly created editor.
     * When the caller is done with the part it should dispose the part's main control. This can
     * be accomplished by calling <code>ISite.getControl().dispose()</code>, or by disposing the 
     * parent composite.
     * @param editorId ID of the editor, as registered with the org.eclipse.ui.editors extension point
     * @param parentComposite parent composite for the editor. If the editor is successfully created,
     *        it will create exactly one new child control in this composite.
     * @param input IEditorInput for this editor
     * @param savedState previously saved state for the part, or null if none
     * @param context local context for the editor. This object can override any or all of the part's dependencies.
     *        If the part has a dependency that isn't found in the local context, a default implementation will
     *        be supplied by the org.eclipse.core.component.types extension point.
     * 
     * @return an ISite for the newly created editor
     * @throws ComponentException if unable to create the part
     */
	public Part createEditor(String editorId, Composite parentComposite, IEditorInput input, IMemento savedState, ServiceFactory context) throws ComponentException;
}
