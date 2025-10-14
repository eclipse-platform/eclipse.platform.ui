/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.commands;

import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Binding Table Container</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This type contains the list of binding 'tables', representing the various sets of bindings
 * based on the application's current running 'context'. Here the 'context' represents
 * the application's UI state (i.e. whether a Dialog is open...).
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MBindingTableContainer#getBindingTables <em>Binding Tables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MBindingTableContainer#getRootContext <em>Root Context</em>}</li>
 * </ul>
 *
 * @model interface="true" abstract="true"
 * @generated
 */
public interface MBindingTableContainer {
	/**
	 * Returns the value of the '<em><b>Binding Tables</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.commands.MBindingTable}.
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
	 * @return the value of the '<em>Binding Tables</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MBindingTable> getBindingTables();

	/**
	 * Returns the value of the '<em><b>Root Context</b></em>' containment reference list.
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
	 * @return the value of the '<em>Root Context</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MBindingContext> getRootContext();

} // MBindingTableContainer
