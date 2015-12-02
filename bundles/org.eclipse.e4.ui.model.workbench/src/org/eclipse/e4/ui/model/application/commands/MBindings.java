/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.commands;

import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Bindings</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * Mixin interface that lists MBindingContexts that should be active when this
 * object is active.
 * </p>
 * <p>Example values: org.eclipse.ui.contexts.dialog, org.eclipse.ui.contexts.window
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MBindings#getBindingContexts <em>Binding Contexts</em>}</li>
 * </ul>
 *
 * @model interface="true" abstract="true"
 * @generated
 */
public interface MBindings {
	/**
	 * Returns the value of the '<em><b>Binding Contexts</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.commands.MBindingContext}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * <strong>Developers</strong>:
	 * Add more detailed documentation by editing this comment in 
	 * org.eclipse.ui.model.workbench/model/UIElements.ecore. 
	 * There is a GenModel/documentation node under each type and attribute.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Binding Contexts</em>' reference list.
	 * @model
	 * @generated
	 */
	List<MBindingContext> getBindingContexts();

} // MBindings
