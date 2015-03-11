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
package org.eclipse.e4.ui.model.application;

import java.util.List;
import org.eclipse.e4.ui.model.application.commands.MBindingTableContainer;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Application</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * The MApplication acts as the root of the UI Model. It's children are the
 *  MWindows representing the UI for this application. It also owns the application's
 * context (which is hooked to the OSGI context, allowing access not only to its
 * own runtime information but also to any registered OSGI service.
 * </p><p>
 * It also owns a number of caches which, while independent of the UI itself are
 * used by the appliecation to populate new windows or to define state that is
 * epected to be the same for all windows:
 * <ui>
 * <li>Keybindings, Handlers, Commands</li>
 * <li>Part Descriptors (to support a 'Show View' dialog...)</li>
 * <li>Snippets of model (such as saved perspectives...)</li>
 * </ui>
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplication#getCommands <em>Commands</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplication#getAddons <em>Addons</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplication#getCategories <em>Categories</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplication#getDialogs <em>Dialogs</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 */
public interface MApplication extends MElementContainer<MWindow>, MContext, MHandlerContainer, MBindingTableContainer, MPartDescriptorContainer, MBindings, MMenuContributions, MToolBarContributions, MTrimContributions, MSnippetContainer {
	/**
	 * Returns the value of the '<em><b>Commands</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.commands.MCommand}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is the list of MCommand elements available in the application. Commands
	 * represent some logical operation. The actual implementation of the operation is
	 * determined by the MHandler chosen by the system based on the current execution
	 * context.
	 *
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Commands</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MCommand> getCommands();

	/**
	 * Returns the value of the '<em><b>Addons</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.MAddon}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is the ordered list of MAddons for this model. The individual addons will be
	 * created through injection after the model loads but before it is rendered.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Addons</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MAddon> getAddons();

	/**
	 * Returns the value of the '<em><b>Categories</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.commands.MCategory}.
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
	 * @return the value of the '<em>Categories</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MCategory> getCategories();

	/**
	 * Returns the value of the '<em><b>Dialogs</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.basic.MDialog}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is the ordered list of MDialogs for this model.
	 * </p>
	 * @since 1.1
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Dialogs</em>' reference list.
	 * @model
	 * @generated
	 */
	List<MDialog> getDialogs();

} // MApplication
