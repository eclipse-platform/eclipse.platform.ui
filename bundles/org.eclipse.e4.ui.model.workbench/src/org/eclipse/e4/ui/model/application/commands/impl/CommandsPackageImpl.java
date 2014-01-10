/**
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.commands.impl;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MBindingTableContainer;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MKeySequence;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.model.application.commands.MCommandsFactory
 * @model kind="package"
 * @generated
 */
public class CommandsPackageImpl extends EPackageImpl {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNAME = "commands"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_URI = "http://www.eclipse.org/ui/2010/UIModel/application/commands"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final String eNS_PREFIX = "commands"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final CommandsPackageImpl eINSTANCE = org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.commands.MBindingTableContainer <em>Binding Table Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.commands.MBindingTableContainer
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getBindingTableContainer()
	 * @generated
	 */
	public static final int BINDING_TABLE_CONTAINER = 0;

	/**
	 * The feature id for the '<em><b>Binding Tables</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE_CONTAINER__BINDING_TABLES = 0;

	/**
	 * The feature id for the '<em><b>Root Context</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE_CONTAINER__ROOT_CONTEXT = 1;

	/**
	 * The number of structural features of the '<em>Binding Table Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE_CONTAINER_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Binding Table Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE_CONTAINER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.commands.MBindings <em>Bindings</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.commands.MBindings
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getBindings()
	 * @generated
	 */
	public static final int BINDINGS = 1;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDINGS__BINDING_CONTEXTS = 0;

	/**
	 * The number of structural features of the '<em>Bindings</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDINGS_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Bindings</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDINGS_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.commands.impl.BindingContextImpl <em>Binding Context</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.commands.impl.BindingContextImpl
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getBindingContext()
	 * @generated
	 */
	public static final int BINDING_CONTEXT = 2;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_CONTEXT__ELEMENT_ID = ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_CONTEXT__PERSISTED_STATE = ApplicationPackageImpl.APPLICATION_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_CONTEXT__TAGS = ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_CONTEXT__CONTRIBUTOR_URI = ApplicationPackageImpl.APPLICATION_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_CONTEXT__TRANSIENT_DATA = ApplicationPackageImpl.APPLICATION_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_CONTEXT__NAME = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_CONTEXT__DESCRIPTION = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_CONTEXT__CHILDREN = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Binding Context</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_CONTEXT_FEATURE_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Binding Context</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_CONTEXT_OPERATION_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.commands.impl.BindingTableImpl <em>Binding Table</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.commands.impl.BindingTableImpl
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getBindingTable()
	 * @generated
	 */
	public static final int BINDING_TABLE = 3;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE__ELEMENT_ID = ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE__PERSISTED_STATE = ApplicationPackageImpl.APPLICATION_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE__TAGS = ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE__CONTRIBUTOR_URI = ApplicationPackageImpl.APPLICATION_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE__TRANSIENT_DATA = ApplicationPackageImpl.APPLICATION_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE__BINDINGS = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Binding Context</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE__BINDING_CONTEXT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Binding Table</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE_FEATURE_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Binding Table</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int BINDING_TABLE_OPERATION_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.commands.impl.CommandImpl <em>Command</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandImpl
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getCommand()
	 * @generated
	 */
	public static final int COMMAND = 4;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND__ELEMENT_ID = ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND__PERSISTED_STATE = ApplicationPackageImpl.APPLICATION_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND__TAGS = ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND__CONTRIBUTOR_URI = ApplicationPackageImpl.APPLICATION_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND__TRANSIENT_DATA = ApplicationPackageImpl.APPLICATION_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Command Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND__COMMAND_NAME = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND__DESCRIPTION = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND__PARAMETERS = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Category</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND__CATEGORY = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Localized Command Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND__LOCALIZED_COMMAND_NAME = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Localized Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND__LOCALIZED_DESCRIPTION = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Command</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND_FEATURE_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 6;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND___UPDATE_LOCALIZATION = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The number of operations of the '<em>Command</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND_OPERATION_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.commands.impl.CommandParameterImpl <em>Command Parameter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandParameterImpl
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getCommandParameter()
	 * @generated
	 */
	public static final int COMMAND_PARAMETER = 5;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND_PARAMETER__ELEMENT_ID = ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND_PARAMETER__PERSISTED_STATE = ApplicationPackageImpl.APPLICATION_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND_PARAMETER__TAGS = ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND_PARAMETER__CONTRIBUTOR_URI = ApplicationPackageImpl.APPLICATION_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND_PARAMETER__TRANSIENT_DATA = ApplicationPackageImpl.APPLICATION_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND_PARAMETER__NAME = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Type Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND_PARAMETER__TYPE_ID = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Optional</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND_PARAMETER__OPTIONAL = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Command Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND_PARAMETER_FEATURE_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Command Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int COMMAND_PARAMETER_OPERATION_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.commands.impl.HandlerImpl <em>Handler</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.commands.impl.HandlerImpl
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getHandler()
	 * @generated
	 */
	public static final int HANDLER = 6;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER__ELEMENT_ID = ApplicationPackageImpl.CONTRIBUTION__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER__PERSISTED_STATE = ApplicationPackageImpl.CONTRIBUTION__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER__TAGS = ApplicationPackageImpl.CONTRIBUTION__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER__CONTRIBUTOR_URI = ApplicationPackageImpl.CONTRIBUTION__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER__TRANSIENT_DATA = ApplicationPackageImpl.CONTRIBUTION__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Contribution URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER__CONTRIBUTION_URI = ApplicationPackageImpl.CONTRIBUTION__CONTRIBUTION_URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER__OBJECT = ApplicationPackageImpl.CONTRIBUTION__OBJECT;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER__COMMAND = ApplicationPackageImpl.CONTRIBUTION_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Handler</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER_FEATURE_COUNT = ApplicationPackageImpl.CONTRIBUTION_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Handler</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER_OPERATION_COUNT = ApplicationPackageImpl.CONTRIBUTION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.commands.MHandlerContainer <em>Handler Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.commands.MHandlerContainer
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getHandlerContainer()
	 * @generated
	 */
	public static final int HANDLER_CONTAINER = 7;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER_CONTAINER__HANDLERS = 0;

	/**
	 * The number of structural features of the '<em>Handler Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER_CONTAINER_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Handler Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int HANDLER_CONTAINER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.commands.MKeySequence <em>Key Sequence</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.commands.MKeySequence
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getKeySequence()
	 * @generated
	 */
	public static final int KEY_SEQUENCE = 9;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.commands.impl.KeyBindingImpl <em>Key Binding</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.commands.impl.KeyBindingImpl
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getKeyBinding()
	 * @generated
	 */
	public static final int KEY_BINDING = 8;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_BINDING__ELEMENT_ID = ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_BINDING__PERSISTED_STATE = ApplicationPackageImpl.APPLICATION_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_BINDING__TAGS = ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_BINDING__CONTRIBUTOR_URI = ApplicationPackageImpl.APPLICATION_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_BINDING__TRANSIENT_DATA = ApplicationPackageImpl.APPLICATION_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Key Sequence</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_BINDING__KEY_SEQUENCE = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_BINDING__COMMAND = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_BINDING__PARAMETERS = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Key Binding</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_BINDING_FEATURE_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Key Binding</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_BINDING_OPERATION_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Key Sequence</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_SEQUENCE__KEY_SEQUENCE = 0;

	/**
	 * The number of structural features of the '<em>Key Sequence</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_SEQUENCE_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Key Sequence</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int KEY_SEQUENCE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.commands.impl.ParameterImpl <em>Parameter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.commands.impl.ParameterImpl
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getParameter()
	 * @generated
	 */
	public static final int PARAMETER = 10;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PARAMETER__ELEMENT_ID = ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PARAMETER__PERSISTED_STATE = ApplicationPackageImpl.APPLICATION_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PARAMETER__TAGS = ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PARAMETER__CONTRIBUTOR_URI = ApplicationPackageImpl.APPLICATION_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PARAMETER__TRANSIENT_DATA = ApplicationPackageImpl.APPLICATION_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PARAMETER__NAME = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PARAMETER__VALUE = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PARAMETER_FEATURE_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int PARAMETER_OPERATION_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.commands.impl.CategoryImpl <em>Category</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CategoryImpl
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getCategory()
	 * @generated
	 */
	public static final int CATEGORY = 11;

	/**
	 * The feature id for the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CATEGORY__ELEMENT_ID = ApplicationPackageImpl.APPLICATION_ELEMENT__ELEMENT_ID;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CATEGORY__PERSISTED_STATE = ApplicationPackageImpl.APPLICATION_ELEMENT__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CATEGORY__TAGS = ApplicationPackageImpl.APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Contributor URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CATEGORY__CONTRIBUTOR_URI = ApplicationPackageImpl.APPLICATION_ELEMENT__CONTRIBUTOR_URI;

	/**
	 * The feature id for the '<em><b>Transient Data</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CATEGORY__TRANSIENT_DATA = ApplicationPackageImpl.APPLICATION_ELEMENT__TRANSIENT_DATA;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CATEGORY__NAME = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CATEGORY__DESCRIPTION = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Localized Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CATEGORY__LOCALIZED_NAME = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Localized Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CATEGORY__LOCALIZED_DESCRIPTION = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Category</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CATEGORY_FEATURE_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The operation id for the '<em>Update Localization</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CATEGORY___UPDATE_LOCALIZATION = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 0;

	/**
	 * The number of operations of the '<em>Category</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	public static final int CATEGORY_OPERATION_COUNT = ApplicationPackageImpl.APPLICATION_ELEMENT_OPERATION_COUNT + 1;

	/**
	 * The meta object id for the '<em>Parameterized Command</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.core.commands.ParameterizedCommand
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getParameterizedCommand()
	 * @generated
	 */
	public static final int PARAMETERIZED_COMMAND = 12;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass bindingTableContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass bindingsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass bindingContextEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass bindingTableEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass commandEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass commandParameterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass handlerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass handlerContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass keyBindingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass keySequenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass parameterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass categoryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType parameterizedCommandEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private CommandsPackageImpl() {
		super(eNS_URI, ((EFactory)MCommandsFactory.INSTANCE));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link CommandsPackageImpl#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static CommandsPackageImpl init() {
		if (isInited) return (CommandsPackageImpl)EPackage.Registry.INSTANCE.getEPackage(CommandsPackageImpl.eNS_URI);

		// Obtain or create and register package
		CommandsPackageImpl theCommandsPackage = (CommandsPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof CommandsPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new CommandsPackageImpl());

		isInited = true;

		// Obtain or create and register interdependencies
		ApplicationPackageImpl theApplicationPackage = (ApplicationPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ApplicationPackageImpl.eNS_URI) instanceof ApplicationPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ApplicationPackageImpl.eNS_URI) : ApplicationPackageImpl.eINSTANCE);
		UiPackageImpl theUiPackage = (UiPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI) instanceof UiPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI) : UiPackageImpl.eINSTANCE);
		MenuPackageImpl theMenuPackage = (MenuPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(MenuPackageImpl.eNS_URI) instanceof MenuPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(MenuPackageImpl.eNS_URI) : MenuPackageImpl.eINSTANCE);
		BasicPackageImpl theBasicPackage = (BasicPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(BasicPackageImpl.eNS_URI) instanceof BasicPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(BasicPackageImpl.eNS_URI) : BasicPackageImpl.eINSTANCE);
		AdvancedPackageImpl theAdvancedPackage = (AdvancedPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(AdvancedPackageImpl.eNS_URI) instanceof AdvancedPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(AdvancedPackageImpl.eNS_URI) : AdvancedPackageImpl.eINSTANCE);
		org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl theBasicPackage_1 = (org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI) instanceof org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI) : org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE);

		// Create package meta-data objects
		theCommandsPackage.createPackageContents();
		theApplicationPackage.createPackageContents();
		theUiPackage.createPackageContents();
		theMenuPackage.createPackageContents();
		theBasicPackage.createPackageContents();
		theAdvancedPackage.createPackageContents();
		theBasicPackage_1.createPackageContents();

		// Initialize created meta-data
		theCommandsPackage.initializePackageContents();
		theApplicationPackage.initializePackageContents();
		theUiPackage.initializePackageContents();
		theMenuPackage.initializePackageContents();
		theBasicPackage.initializePackageContents();
		theAdvancedPackage.initializePackageContents();
		theBasicPackage_1.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theCommandsPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(CommandsPackageImpl.eNS_URI, theCommandsPackage);
		return theCommandsPackage;
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.commands.MBindingTableContainer <em>Binding Table Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Binding Table Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindingTableContainer
	 * @generated
	 */
	public EClass getBindingTableContainer() {
		return bindingTableContainerEClass;
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.commands.MBindingTableContainer#getBindingTables <em>Binding Tables</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Binding Tables</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindingTableContainer#getBindingTables()
	 * @see #getBindingTableContainer()
	 * @generated
	 */
	public EReference getBindingTableContainer_BindingTables() {
		return (EReference)bindingTableContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.commands.MBindingTableContainer#getRootContext <em>Root Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Root Context</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindingTableContainer#getRootContext()
	 * @see #getBindingTableContainer()
	 * @generated
	 */
	public EReference getBindingTableContainer_RootContext() {
		return (EReference)bindingTableContainerEClass.getEStructuralFeatures().get(1);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.commands.MBindings <em>Bindings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Bindings</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindings
	 * @generated
	 */
	public EClass getBindings() {
		return bindingsEClass;
	}

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.e4.ui.model.application.commands.MBindings#getBindingContexts <em>Binding Contexts</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Binding Contexts</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindings#getBindingContexts()
	 * @see #getBindings()
	 * @generated
	 */
	public EReference getBindings_BindingContexts() {
		return (EReference)bindingsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.commands.MBindingContext <em>Binding Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Binding Context</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindingContext
	 * @generated
	 */
	public EClass getBindingContext() {
		return bindingContextEClass;
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MBindingContext#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindingContext#getName()
	 * @see #getBindingContext()
	 * @generated
	 */
	public EAttribute getBindingContext_Name() {
		return (EAttribute)bindingContextEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MBindingContext#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindingContext#getDescription()
	 * @see #getBindingContext()
	 * @generated
	 */
	public EAttribute getBindingContext_Description() {
		return (EAttribute)bindingContextEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.commands.MBindingContext#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Children</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindingContext#getChildren()
	 * @see #getBindingContext()
	 * @generated
	 */
	public EReference getBindingContext_Children() {
		return (EReference)bindingContextEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.commands.MBindingTable <em>Binding Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Binding Table</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindingTable
	 * @generated
	 */
	public EClass getBindingTable() {
		return bindingTableEClass;
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.commands.MBindingTable#getBindings <em>Bindings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Bindings</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindingTable#getBindings()
	 * @see #getBindingTable()
	 * @generated
	 */
	public EReference getBindingTable_Bindings() {
		return (EReference)bindingTableEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.commands.MBindingTable#getBindingContext <em>Binding Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Binding Context</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindingTable#getBindingContext()
	 * @see #getBindingTable()
	 * @generated
	 */
	public EReference getBindingTable_BindingContext() {
		return (EReference)bindingTableEClass.getEStructuralFeatures().get(1);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.commands.MCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCommand
	 * @generated
	 */
	public EClass getCommand() {
		return commandEClass;
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MCommand#getCommandName <em>Command Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Command Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCommand#getCommandName()
	 * @see #getCommand()
	 * @generated
	 */
	public EAttribute getCommand_CommandName() {
		return (EAttribute)commandEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MCommand#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCommand#getDescription()
	 * @see #getCommand()
	 * @generated
	 */
	public EAttribute getCommand_Description() {
		return (EAttribute)commandEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.commands.MCommand#getParameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Parameters</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCommand#getParameters()
	 * @see #getCommand()
	 * @generated
	 */
	public EReference getCommand_Parameters() {
		return (EReference)commandEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.commands.MCommand#getCategory <em>Category</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Category</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCommand#getCategory()
	 * @see #getCommand()
	 * @generated
	 */
	public EReference getCommand_Category() {
		return (EReference)commandEClass.getEStructuralFeatures().get(3);
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MCommand#getLocalizedCommandName <em>Localized Command Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Localized Command Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCommand#getLocalizedCommandName()
	 * @see #getCommand()
	 * @generated
	 */
	public EAttribute getCommand_LocalizedCommandName() {
		return (EAttribute)commandEClass.getEStructuralFeatures().get(4);
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MCommand#getLocalizedDescription <em>Localized Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Localized Description</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCommand#getLocalizedDescription()
	 * @see #getCommand()
	 * @generated
	 */
	public EAttribute getCommand_LocalizedDescription() {
		return (EAttribute)commandEClass.getEStructuralFeatures().get(5);
	}


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.commands.MCommandParameter <em>Command Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Command Parameter</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCommandParameter
	 * @generated
	 */
	public EClass getCommandParameter() {
		return commandParameterEClass;
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MCommandParameter#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCommandParameter#getName()
	 * @see #getCommandParameter()
	 * @generated
	 */
	public EAttribute getCommandParameter_Name() {
		return (EAttribute)commandParameterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MCommandParameter#getTypeId <em>Type Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type Id</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCommandParameter#getTypeId()
	 * @see #getCommandParameter()
	 * @generated
	 */
	public EAttribute getCommandParameter_TypeId() {
		return (EAttribute)commandParameterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MCommandParameter#isOptional <em>Optional</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Optional</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCommandParameter#isOptional()
	 * @see #getCommandParameter()
	 * @generated
	 */
	public EAttribute getCommandParameter_Optional() {
		return (EAttribute)commandParameterEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.commands.MHandler <em>Handler</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Handler</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MHandler
	 * @generated
	 */
	public EClass getHandler() {
		return handlerEClass;
	}

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.commands.MHandler#getCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MHandler#getCommand()
	 * @see #getHandler()
	 * @generated
	 */
	public EReference getHandler_Command() {
		return (EReference)handlerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.commands.MHandlerContainer <em>Handler Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Handler Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MHandlerContainer
	 * @generated
	 */
	public EClass getHandlerContainer() {
		return handlerContainerEClass;
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.commands.MHandlerContainer#getHandlers <em>Handlers</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Handlers</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MHandlerContainer#getHandlers()
	 * @see #getHandlerContainer()
	 * @generated
	 */
	public EReference getHandlerContainer_Handlers() {
		return (EReference)handlerContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.commands.MKeyBinding <em>Key Binding</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Key Binding</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MKeyBinding
	 * @generated
	 */
	public EClass getKeyBinding() {
		return keyBindingEClass;
	}

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.commands.MKeyBinding#getCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MKeyBinding#getCommand()
	 * @see #getKeyBinding()
	 * @generated
	 */
	public EReference getKeyBinding_Command() {
		return (EReference)keyBindingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.commands.MKeyBinding#getParameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Parameters</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MKeyBinding#getParameters()
	 * @see #getKeyBinding()
	 * @generated
	 */
	public EReference getKeyBinding_Parameters() {
		return (EReference)keyBindingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.commands.MKeySequence <em>Key Sequence</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Key Sequence</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MKeySequence
	 * @generated
	 */
	public EClass getKeySequence() {
		return keySequenceEClass;
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MKeySequence#getKeySequence <em>Key Sequence</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key Sequence</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MKeySequence#getKeySequence()
	 * @see #getKeySequence()
	 * @generated
	 */
	public EAttribute getKeySequence_KeySequence() {
		return (EAttribute)keySequenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.commands.MParameter <em>Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Parameter</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MParameter
	 * @generated
	 */
	public EClass getParameter() {
		return parameterEClass;
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MParameter#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MParameter#getName()
	 * @see #getParameter()
	 * @generated
	 */
	public EAttribute getParameter_Name() {
		return (EAttribute)parameterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MParameter#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MParameter#getValue()
	 * @see #getParameter()
	 * @generated
	 */
	public EAttribute getParameter_Value() {
		return (EAttribute)parameterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.commands.MCategory <em>Category</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Category</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCategory
	 * @generated
	 */
	public EClass getCategory() {
		return categoryEClass;
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MCategory#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCategory#getName()
	 * @see #getCategory()
	 * @generated
	 */
	public EAttribute getCategory_Name() {
		return (EAttribute)categoryEClass.getEStructuralFeatures().get(0);
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MCategory#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCategory#getDescription()
	 * @see #getCategory()
	 * @generated
	 */
	public EAttribute getCategory_Description() {
		return (EAttribute)categoryEClass.getEStructuralFeatures().get(1);
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MCategory#getLocalizedName <em>Localized Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Localized Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCategory#getLocalizedName()
	 * @see #getCategory()
	 * @generated
	 */
	public EAttribute getCategory_LocalizedName() {
		return (EAttribute)categoryEClass.getEStructuralFeatures().get(2);
	}


	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.commands.MCategory#getLocalizedDescription <em>Localized Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Localized Description</em>'.
	 * @see org.eclipse.e4.ui.model.application.commands.MCategory#getLocalizedDescription()
	 * @see #getCategory()
	 * @generated
	 */
	public EAttribute getCategory_LocalizedDescription() {
		return (EAttribute)categoryEClass.getEStructuralFeatures().get(3);
	}


	/**
	 * Returns the meta object for data type '{@link org.eclipse.core.commands.ParameterizedCommand <em>Parameterized Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Parameterized Command</em>'.
	 * @see org.eclipse.core.commands.ParameterizedCommand
	 * @model instanceClass="org.eclipse.core.commands.ParameterizedCommand" serializeable="false"
	 * @generated
	 */
	public EDataType getParameterizedCommand() {
		return parameterizedCommandEDataType;
	}

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	public MCommandsFactory getCommandsFactory() {
		return (MCommandsFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		bindingTableContainerEClass = createEClass(BINDING_TABLE_CONTAINER);
		createEReference(bindingTableContainerEClass, BINDING_TABLE_CONTAINER__BINDING_TABLES);
		createEReference(bindingTableContainerEClass, BINDING_TABLE_CONTAINER__ROOT_CONTEXT);

		bindingsEClass = createEClass(BINDINGS);
		createEReference(bindingsEClass, BINDINGS__BINDING_CONTEXTS);

		bindingContextEClass = createEClass(BINDING_CONTEXT);
		createEAttribute(bindingContextEClass, BINDING_CONTEXT__NAME);
		createEAttribute(bindingContextEClass, BINDING_CONTEXT__DESCRIPTION);
		createEReference(bindingContextEClass, BINDING_CONTEXT__CHILDREN);

		bindingTableEClass = createEClass(BINDING_TABLE);
		createEReference(bindingTableEClass, BINDING_TABLE__BINDINGS);
		createEReference(bindingTableEClass, BINDING_TABLE__BINDING_CONTEXT);

		commandEClass = createEClass(COMMAND);
		createEAttribute(commandEClass, COMMAND__COMMAND_NAME);
		createEAttribute(commandEClass, COMMAND__DESCRIPTION);
		createEReference(commandEClass, COMMAND__PARAMETERS);
		createEReference(commandEClass, COMMAND__CATEGORY);
		createEAttribute(commandEClass, COMMAND__LOCALIZED_COMMAND_NAME);
		createEAttribute(commandEClass, COMMAND__LOCALIZED_DESCRIPTION);

		commandParameterEClass = createEClass(COMMAND_PARAMETER);
		createEAttribute(commandParameterEClass, COMMAND_PARAMETER__NAME);
		createEAttribute(commandParameterEClass, COMMAND_PARAMETER__TYPE_ID);
		createEAttribute(commandParameterEClass, COMMAND_PARAMETER__OPTIONAL);

		handlerEClass = createEClass(HANDLER);
		createEReference(handlerEClass, HANDLER__COMMAND);

		handlerContainerEClass = createEClass(HANDLER_CONTAINER);
		createEReference(handlerContainerEClass, HANDLER_CONTAINER__HANDLERS);

		keyBindingEClass = createEClass(KEY_BINDING);
		createEReference(keyBindingEClass, KEY_BINDING__COMMAND);
		createEReference(keyBindingEClass, KEY_BINDING__PARAMETERS);

		keySequenceEClass = createEClass(KEY_SEQUENCE);
		createEAttribute(keySequenceEClass, KEY_SEQUENCE__KEY_SEQUENCE);

		parameterEClass = createEClass(PARAMETER);
		createEAttribute(parameterEClass, PARAMETER__NAME);
		createEAttribute(parameterEClass, PARAMETER__VALUE);

		categoryEClass = createEClass(CATEGORY);
		createEAttribute(categoryEClass, CATEGORY__NAME);
		createEAttribute(categoryEClass, CATEGORY__DESCRIPTION);
		createEAttribute(categoryEClass, CATEGORY__LOCALIZED_NAME);
		createEAttribute(categoryEClass, CATEGORY__LOCALIZED_DESCRIPTION);

		// Create data types
		parameterizedCommandEDataType = createEDataType(PARAMETERIZED_COMMAND);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		ApplicationPackageImpl theApplicationPackage = (ApplicationPackageImpl)EPackage.Registry.INSTANCE.getEPackage(ApplicationPackageImpl.eNS_URI);
		UiPackageImpl theUiPackage = (UiPackageImpl)EPackage.Registry.INSTANCE.getEPackage(UiPackageImpl.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		bindingContextEClass.getESuperTypes().add(theApplicationPackage.getApplicationElement());
		bindingTableEClass.getESuperTypes().add(theApplicationPackage.getApplicationElement());
		commandEClass.getESuperTypes().add(theApplicationPackage.getApplicationElement());
		commandEClass.getESuperTypes().add(theUiPackage.getLocalizable());
		commandParameterEClass.getESuperTypes().add(theApplicationPackage.getApplicationElement());
		handlerEClass.getESuperTypes().add(theApplicationPackage.getContribution());
		keyBindingEClass.getESuperTypes().add(theApplicationPackage.getApplicationElement());
		keyBindingEClass.getESuperTypes().add(this.getKeySequence());
		parameterEClass.getESuperTypes().add(theApplicationPackage.getApplicationElement());
		categoryEClass.getESuperTypes().add(theApplicationPackage.getApplicationElement());
		categoryEClass.getESuperTypes().add(theUiPackage.getLocalizable());

		// Initialize classes, features, and operations; add parameters
		initEClass(bindingTableContainerEClass, MBindingTableContainer.class, "BindingTableContainer", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getBindingTableContainer_BindingTables(), this.getBindingTable(), null, "bindingTables", null, 0, -1, MBindingTableContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getBindingTableContainer_RootContext(), this.getBindingContext(), null, "rootContext", null, 0, -1, MBindingTableContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(bindingsEClass, MBindings.class, "Bindings", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getBindings_BindingContexts(), this.getBindingContext(), null, "bindingContexts", null, 0, -1, MBindings.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(bindingContextEClass, MBindingContext.class, "BindingContext", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getBindingContext_Name(), ecorePackage.getEString(), "name", null, 0, 1, MBindingContext.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getBindingContext_Description(), ecorePackage.getEString(), "description", null, 0, 1, MBindingContext.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getBindingContext_Children(), this.getBindingContext(), null, "children", null, 0, -1, MBindingContext.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(bindingTableEClass, MBindingTable.class, "BindingTable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getBindingTable_Bindings(), this.getKeyBinding(), null, "bindings", null, 0, -1, MBindingTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getBindingTable_BindingContext(), this.getBindingContext(), null, "bindingContext", null, 1, 1, MBindingTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(commandEClass, MCommand.class, "Command", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getCommand_CommandName(), ecorePackage.getEString(), "commandName", null, 0, 1, MCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCommand_Description(), ecorePackage.getEString(), "description", null, 0, 1, MCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getCommand_Parameters(), this.getCommandParameter(), null, "parameters", null, 0, -1, MCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getCommand_Category(), this.getCategory(), null, "category", null, 0, 1, MCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCommand_LocalizedCommandName(), ecorePackage.getEString(), "localizedCommandName", null, 0, 1, MCommand.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCommand_LocalizedDescription(), ecorePackage.getEString(), "localizedDescription", null, 0, 1, MCommand.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(commandParameterEClass, MCommandParameter.class, "CommandParameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getCommandParameter_Name(), ecorePackage.getEString(), "name", null, 1, 1, MCommandParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCommandParameter_TypeId(), ecorePackage.getEString(), "typeId", null, 0, 1, MCommandParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCommandParameter_Optional(), ecorePackage.getEBoolean(), "optional", "true", 0, 1, MCommandParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$

		initEClass(handlerEClass, MHandler.class, "Handler", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getHandler_Command(), this.getCommand(), null, "command", null, 1, 1, MHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(handlerContainerEClass, MHandlerContainer.class, "HandlerContainer", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getHandlerContainer_Handlers(), this.getHandler(), null, "handlers", null, 0, -1, MHandlerContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(keyBindingEClass, MKeyBinding.class, "KeyBinding", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getKeyBinding_Command(), this.getCommand(), null, "command", null, 1, 1, MKeyBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getKeyBinding_Parameters(), this.getParameter(), null, "parameters", null, 0, -1, MKeyBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(keySequenceEClass, MKeySequence.class, "KeySequence", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getKeySequence_KeySequence(), ecorePackage.getEString(), "keySequence", null, 1, 1, MKeySequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(parameterEClass, MParameter.class, "Parameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getParameter_Name(), ecorePackage.getEString(), "name", null, 0, 1, MParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getParameter_Value(), ecorePackage.getEString(), "value", null, 0, 1, MParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(categoryEClass, MCategory.class, "Category", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getCategory_Name(), ecorePackage.getEString(), "name", null, 1, 1, MCategory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCategory_Description(), ecorePackage.getEString(), "description", null, 0, 1, MCategory.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCategory_LocalizedName(), ecorePackage.getEString(), "localizedName", "", 0, 1, MCategory.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(getCategory_LocalizedDescription(), ecorePackage.getEString(), "localizedDescription", null, 0, 1, MCategory.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		// Initialize data types
		initEDataType(parameterizedCommandEDataType, ParameterizedCommand.class, "ParameterizedCommand", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
	}

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.commands.MBindingTableContainer <em>Binding Table Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.commands.MBindingTableContainer
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getBindingTableContainer()
		 * @generated
		 */
		public static final EClass BINDING_TABLE_CONTAINER = eINSTANCE.getBindingTableContainer();

		/**
		 * The meta object literal for the '<em><b>Binding Tables</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference BINDING_TABLE_CONTAINER__BINDING_TABLES = eINSTANCE.getBindingTableContainer_BindingTables();

		/**
		 * The meta object literal for the '<em><b>Root Context</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference BINDING_TABLE_CONTAINER__ROOT_CONTEXT = eINSTANCE.getBindingTableContainer_RootContext();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.commands.MBindings <em>Bindings</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.commands.MBindings
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getBindings()
		 * @generated
		 */
		public static final EClass BINDINGS = eINSTANCE.getBindings();

		/**
		 * The meta object literal for the '<em><b>Binding Contexts</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference BINDINGS__BINDING_CONTEXTS = eINSTANCE.getBindings_BindingContexts();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.commands.impl.BindingContextImpl <em>Binding Context</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.commands.impl.BindingContextImpl
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getBindingContext()
		 * @generated
		 */
		public static final EClass BINDING_CONTEXT = eINSTANCE.getBindingContext();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute BINDING_CONTEXT__NAME = eINSTANCE.getBindingContext_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute BINDING_CONTEXT__DESCRIPTION = eINSTANCE.getBindingContext_Description();

		/**
		 * The meta object literal for the '<em><b>Children</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference BINDING_CONTEXT__CHILDREN = eINSTANCE.getBindingContext_Children();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.commands.impl.BindingTableImpl <em>Binding Table</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.commands.impl.BindingTableImpl
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getBindingTable()
		 * @generated
		 */
		public static final EClass BINDING_TABLE = eINSTANCE.getBindingTable();

		/**
		 * The meta object literal for the '<em><b>Bindings</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference BINDING_TABLE__BINDINGS = eINSTANCE.getBindingTable_Bindings();

		/**
		 * The meta object literal for the '<em><b>Binding Context</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference BINDING_TABLE__BINDING_CONTEXT = eINSTANCE.getBindingTable_BindingContext();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.commands.impl.CommandImpl <em>Command</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandImpl
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getCommand()
		 * @generated
		 */
		public static final EClass COMMAND = eINSTANCE.getCommand();

		/**
		 * The meta object literal for the '<em><b>Command Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute COMMAND__COMMAND_NAME = eINSTANCE.getCommand_CommandName();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute COMMAND__DESCRIPTION = eINSTANCE.getCommand_Description();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference COMMAND__PARAMETERS = eINSTANCE.getCommand_Parameters();

		/**
		 * The meta object literal for the '<em><b>Category</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference COMMAND__CATEGORY = eINSTANCE.getCommand_Category();

		/**
		 * The meta object literal for the '<em><b>Localized Command Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute COMMAND__LOCALIZED_COMMAND_NAME = eINSTANCE.getCommand_LocalizedCommandName();

		/**
		 * The meta object literal for the '<em><b>Localized Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute COMMAND__LOCALIZED_DESCRIPTION = eINSTANCE.getCommand_LocalizedDescription();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.commands.impl.CommandParameterImpl <em>Command Parameter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandParameterImpl
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getCommandParameter()
		 * @generated
		 */
		public static final EClass COMMAND_PARAMETER = eINSTANCE.getCommandParameter();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute COMMAND_PARAMETER__NAME = eINSTANCE.getCommandParameter_Name();

		/**
		 * The meta object literal for the '<em><b>Type Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute COMMAND_PARAMETER__TYPE_ID = eINSTANCE.getCommandParameter_TypeId();

		/**
		 * The meta object literal for the '<em><b>Optional</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute COMMAND_PARAMETER__OPTIONAL = eINSTANCE.getCommandParameter_Optional();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.commands.impl.HandlerImpl <em>Handler</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.commands.impl.HandlerImpl
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getHandler()
		 * @generated
		 */
		public static final EClass HANDLER = eINSTANCE.getHandler();

		/**
		 * The meta object literal for the '<em><b>Command</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference HANDLER__COMMAND = eINSTANCE.getHandler_Command();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.commands.MHandlerContainer <em>Handler Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.commands.MHandlerContainer
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getHandlerContainer()
		 * @generated
		 */
		public static final EClass HANDLER_CONTAINER = eINSTANCE.getHandlerContainer();

		/**
		 * The meta object literal for the '<em><b>Handlers</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference HANDLER_CONTAINER__HANDLERS = eINSTANCE.getHandlerContainer_Handlers();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.commands.impl.KeyBindingImpl <em>Key Binding</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.commands.impl.KeyBindingImpl
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getKeyBinding()
		 * @generated
		 */
		public static final EClass KEY_BINDING = eINSTANCE.getKeyBinding();

		/**
		 * The meta object literal for the '<em><b>Command</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference KEY_BINDING__COMMAND = eINSTANCE.getKeyBinding_Command();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EReference KEY_BINDING__PARAMETERS = eINSTANCE.getKeyBinding_Parameters();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.commands.MKeySequence <em>Key Sequence</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.commands.MKeySequence
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getKeySequence()
		 * @generated
		 */
		public static final EClass KEY_SEQUENCE = eINSTANCE.getKeySequence();

		/**
		 * The meta object literal for the '<em><b>Key Sequence</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute KEY_SEQUENCE__KEY_SEQUENCE = eINSTANCE.getKeySequence_KeySequence();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.commands.impl.ParameterImpl <em>Parameter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.commands.impl.ParameterImpl
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getParameter()
		 * @generated
		 */
		public static final EClass PARAMETER = eINSTANCE.getParameter();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute PARAMETER__NAME = eINSTANCE.getParameter_Name();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute PARAMETER__VALUE = eINSTANCE.getParameter_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.commands.impl.CategoryImpl <em>Category</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CategoryImpl
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getCategory()
		 * @generated
		 */
		public static final EClass CATEGORY = eINSTANCE.getCategory();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute CATEGORY__NAME = eINSTANCE.getCategory_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute CATEGORY__DESCRIPTION = eINSTANCE.getCategory_Description();

		/**
		 * The meta object literal for the '<em><b>Localized Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute CATEGORY__LOCALIZED_NAME = eINSTANCE.getCategory_LocalizedName();

		/**
		 * The meta object literal for the '<em><b>Localized Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		public static final EAttribute CATEGORY__LOCALIZED_DESCRIPTION = eINSTANCE.getCategory_LocalizedDescription();

		/**
		 * The meta object literal for the '<em>Parameterized Command</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.core.commands.ParameterizedCommand
		 * @see org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl#getParameterizedCommand()
		 * @generated
		 */
		public static final EDataType PARAMETERIZED_COMMAND = eINSTANCE.getParameterizedCommand();

	}

} //CommandsPackageImpl
