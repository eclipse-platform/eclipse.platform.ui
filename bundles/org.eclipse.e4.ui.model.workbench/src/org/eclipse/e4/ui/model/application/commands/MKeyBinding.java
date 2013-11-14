/**
 * Copyright (c) 2008, 2013 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.MApplicationElement;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Key Binding</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * Keybindings map a particular keyboard sequence (i.e. Ctrl + C for Copy...) onto
 * some command.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MKeyBinding#getCommand <em>Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.MKeyBinding#getParameters <em>Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 */
public interface MKeyBinding extends MApplicationElement, MKeySequence {
	/**
	 * Returns the value of the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * A reference to the Command to (attempt to) execute if the given key sequence is
	 * detected.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Command</em>' reference.
	 * @see #setCommand(MCommand)
	 * @model required="true"
	 * @generated
	 */
	MCommand getCommand();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.commands.MKeyBinding#getCommand <em>Command</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Command</em>' reference.
	 * @see #getCommand()
	 * @generated
	 */
	void setCommand(MCommand value);

	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.commands.MParameter}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This allows a KeyBinding to provide a particular set of parameters to be used when
	 * the Command is to be executed. This allows generic commands like 'Open Part' to
	 * have bindings that will open a <i>specific</i> Part...
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parameters</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MParameter> getParameters();

} // MKeyBinding
