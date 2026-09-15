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

import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MExpression;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Handler</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * Handlers provide the execution logic that provides the implementation of a
 * particular command.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MHandler#getCommand <em>Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MHandler#getEnabledWhen <em>Enabled When</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 */
public interface MHandler extends MContribution {
	/**
	 * Returns the value of the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is a reference to the Command for which this is an execution candidate.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Command</em>' reference.
	 * @see #setCommand(MCommand)
	 * @model required="true"
	 * @generated
	 */
	MCommand getCommand();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.commands.MHandler#getCommand <em>Command</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Command</em>' reference.
	 * @see #getCommand()
	 * @generated
	 */
	void setCommand(MCommand value);

	/**
	 * Returns the value of the '<em><b>Enabled When</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * An optional core expression to control handler enablement. When specified, this takes precedence over the @CanExecute annotation.
	 * </p>
	 * @since 1.4
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Enabled When</em>' containment reference.
	 * @see #setEnabledWhen(MExpression)
	 * @model containment="true"
	 * @generated
	 */
	MExpression getEnabledWhen();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.commands.MHandler#getEnabledWhen <em>Enabled When</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Enabled When</em>' containment reference.
	 * @see #getEnabledWhen()
	 * @since 1.4
	 * @generated
	 */
	void setEnabledWhen(MExpression value);

} // MHandler
