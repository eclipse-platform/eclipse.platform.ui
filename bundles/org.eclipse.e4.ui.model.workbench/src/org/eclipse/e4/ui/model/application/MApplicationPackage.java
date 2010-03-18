/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

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
 * @see org.eclipse.e4.ui.model.application.MApplicationFactory
 * @model kind="package"
 * @generated
 */
public interface MApplicationPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "application"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.eclipse.org/ui/2008/UIModel"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "application"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MApplicationPackage eINSTANCE = org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV____________ConstantsAndTypes_____________V <em>VConstants And Types V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV____________ConstantsAndTypes_____________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV____________ConstantsAndTypes_____________V()
	 * @generated
	 */
	int VCONSTANTS_AND_TYPES_V = 0;

	/**
	 * The number of structural features of the '<em>VConstants And Types V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VCONSTANTS_AND_TYPES_V_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV____________Abstract_____________V <em>VAbstract V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV____________Abstract_____________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV____________Abstract_____________V()
	 * @generated
	 */
	int VABSTRACT_V = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl <em>Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getApplicationElement()
	 * @generated
	 */
	int APPLICATION_ELEMENT = 3;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ContributionImpl <em>Contribution</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ContributionImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getContribution()
	 * @generated
	 */
	int CONTRIBUTION = 4;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.DirtyableImpl <em>Dirtyable</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.DirtyableImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getDirtyable()
	 * @generated
	 */
	int DIRTYABLE = 5;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.InputImpl <em>Input</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.InputImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getInput()
	 * @generated
	 */
	int INPUT = 6;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.UIElementImpl <em>UI Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.UIElementImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getUIElement()
	 * @generated
	 */
	int UI_ELEMENT = 7;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.UILabelImpl <em>UI Label</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.UILabelImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getUILabel()
	 * @generated
	 */
	int UI_LABEL = 8;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ContextImpl <em>Context</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ContextImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getContext()
	 * @generated
	 */
	int CONTEXT = 9;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V <em>VAbstract Containers V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV_________AbstractContainers__________V()
	 * @generated
	 */
	int VABSTRACT_CONTAINERS_V = 10;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ElementContainerImpl <em>Element Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ElementContainerImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getElementContainer()
	 * @generated
	 */
	int ELEMENT_CONTAINER = 11;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.GenericStackImpl <em>Generic Stack</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.GenericStackImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getGenericStack()
	 * @generated
	 */
	int GENERIC_STACK = 12;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.GenericTileImpl <em>Generic Tile</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.GenericTileImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getGenericTile()
	 * @generated
	 */
	int GENERIC_TILE = 13;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV______________MenusAndTBs_______________V <em>VMenus And TBs V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV______________MenusAndTBs_______________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________MenusAndTBs_______________V()
	 * @generated
	 */
	int VMENUS_AND_TBS_V = 14;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ItemImpl <em>Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getItem()
	 * @generated
	 */
	int ITEM = 15;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.MenuImpl <em>Menu</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.MenuImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getMenu()
	 * @generated
	 */
	int MENU = 18;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.MenuItemImpl <em>Menu Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.MenuItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getMenuItem()
	 * @generated
	 */
	int MENU_ITEM = 16;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.DirectMenuItemImpl <em>Direct Menu Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.DirectMenuItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getDirectMenuItem()
	 * @generated
	 */
	int DIRECT_MENU_ITEM = 17;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ToolItemImpl <em>Tool Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ToolItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getToolItem()
	 * @generated
	 */
	int TOOL_ITEM = 19;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl <em>Direct Tool Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getDirectToolItem()
	 * @generated
	 */
	int DIRECT_TOOL_ITEM = 20;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ToolBarImpl <em>Tool Bar</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ToolBarImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getToolBar()
	 * @generated
	 */
	int TOOL_BAR = 21;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV______________RCP_______________V <em>VRCP V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV______________RCP_______________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________RCP_______________V()
	 * @generated
	 */
	int VRCP_V = 22;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl <em>Application</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getApplication()
	 * @generated
	 */
	int APPLICATION = 23;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MPSCElement <em>PSC Element</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MPSCElement
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPSCElement()
	 * @generated
	 */
	int PSC_ELEMENT = 24;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PartImpl <em>Part</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PartImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPart()
	 * @generated
	 */
	int PART = 25;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PartDescriptorImpl <em>Part Descriptor</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PartDescriptorImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPartDescriptor()
	 * @generated
	 */
	int PART_DESCRIPTOR = 27;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PartDescriptorContainerImpl <em>Part Descriptor Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PartDescriptorContainerImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPartDescriptorContainer()
	 * @generated
	 */
	int PART_DESCRIPTOR_CONTAINER = 28;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PartStackImpl <em>Part Stack</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PartStackImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPartStack()
	 * @generated
	 */
	int PART_STACK = 29;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PartSashContainerImpl <em>Part Sash Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PartSashContainerImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPartSashContainer()
	 * @generated
	 */
	int PART_SASH_CONTAINER = 30;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.WindowImpl <em>Window</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.WindowImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getWindow()
	 * @generated
	 */
	int WINDOW = 31;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ModelComponentsImpl <em>Model Components</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ModelComponentsImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getModelComponents()
	 * @generated
	 */
	int MODEL_COMPONENTS = 32;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl <em>Model Component</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ModelComponentImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getModelComponent()
	 * @generated
	 */
	int MODEL_COMPONENT = 33;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV______________Commands_______________V <em>VCommands V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV______________Commands_______________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________Commands_______________V()
	 * @generated
	 */
	int VCOMMANDS_V = 34;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MBindingContainer <em>Binding Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MBindingContainer
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getBindingContainer()
	 * @generated
	 */
	int BINDING_CONTAINER = 35;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.CommandImpl <em>Command</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.CommandImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getCommand()
	 * @generated
	 */
	int COMMAND = 39;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.CommandParameterImpl <em>Command Parameter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.CommandParameterImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getCommandParameter()
	 * @generated
	 */
	int COMMAND_PARAMETER = 40;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.HandlerImpl <em>Handler</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.HandlerImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getHandler()
	 * @generated
	 */
	int HANDLER = 41;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MHandlerContainer <em>Handler Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MHandlerContainer
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getHandlerContainer()
	 * @generated
	 */
	int HANDLER_CONTAINER = 42;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.HandledItemImpl <em>Handled Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.HandledItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getHandledItem()
	 * @generated
	 */
	int HANDLED_ITEM = 43;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.HandledMenuItemImpl <em>Handled Menu Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.HandledMenuItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getHandledMenuItem()
	 * @generated
	 */
	int HANDLED_MENU_ITEM = 44;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl <em>Handled Tool Item</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getHandledToolItem()
	 * @generated
	 */
	int HANDLED_TOOL_ITEM = 45;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MKeySequence <em>Key Sequence</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MKeySequence
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getKeySequence()
	 * @generated
	 */
	int KEY_SEQUENCE = 47;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.KeyBindingImpl <em>Key Binding</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.KeyBindingImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getKeyBinding()
	 * @generated
	 */
	int KEY_BINDING = 46;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.ParameterImpl <em>Parameter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.ParameterImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getParameter()
	 * @generated
	 */
	int PARAMETER = 48;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV______________Trim_______________V <em>VTrim V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV______________Trim_______________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________Trim_______________V()
	 * @generated
	 */
	int VTRIM_V = 49;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.TrimContainerImpl <em>Trim Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.TrimContainerImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getTrimContainer()
	 * @generated
	 */
	int TRIM_CONTAINER = 50;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.WindowTrimImpl <em>Window Trim</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.WindowTrimImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getWindowTrim()
	 * @generated
	 */
	int WINDOW_TRIM = 51;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV______________SharedElements_______________V <em>VShared Elements V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV______________SharedElements_______________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________SharedElements_______________V()
	 * @generated
	 */
	int VSHARED_ELEMENTS_V = 52;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PlaceholderImpl <em>Placeholder</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PlaceholderImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPlaceholder()
	 * @generated
	 */
	int PLACEHOLDER = 53;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PerspectiveImpl <em>Perspective</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PerspectiveImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPerspective()
	 * @generated
	 */
	int PERSPECTIVE = 54;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.PerspectiveStackImpl <em>Perspective Stack</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.PerspectiveStackImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPerspectiveStack()
	 * @generated
	 */
	int PERSPECTIVE_STACK = 55;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MV_________Testing__________V <em>VTesting V</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MV_________Testing__________V
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV_________Testing__________V()
	 * @generated
	 */
	int VTESTING_V = 56;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl <em>Test Harness</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.TestHarnessImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getTestHarness()
	 * @generated
	 */
	int TEST_HARNESS = 57;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.StringToStringMapImpl <em>String To String Map</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.StringToStringMapImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getStringToStringMap()
	 * @generated
	 */
	int STRING_TO_STRING_MAP = 1;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_STRING_MAP__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_STRING_MAP__VALUE = 1;

	/**
	 * The number of structural features of the '<em>String To String Map</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_STRING_MAP_FEATURE_COUNT = 2;

	/**
	 * The number of structural features of the '<em>VAbstract V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VABSTRACT_V_FEATURE_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION_ELEMENT__ID = 0;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION_ELEMENT__TAGS = 1;

	/**
	 * The number of structural features of the '<em>Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION_ELEMENT_FEATURE_COUNT = 2;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTRIBUTION__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTRIBUTION__TAGS = APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTRIBUTION__URI = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTRIBUTION__OBJECT = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTRIBUTION__PERSISTED_STATE = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Contribution</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTRIBUTION_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Dirty</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRTYABLE__DIRTY = 0;

	/**
	 * The number of structural features of the '<em>Dirtyable</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRTYABLE_FEATURE_COUNT = 1;

	/**
	 * The feature id for the '<em><b>Input URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT__INPUT_URI = 0;

	/**
	 * The number of structural features of the '<em>Input</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_FEATURE_COUNT = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__TAGS = APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__WIDGET = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__RENDERER = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__TO_BE_RENDERED = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__ON_TOP = APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__VISIBLE = APPLICATION_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__PARENT = APPLICATION_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT__CONTAINER_DATA = APPLICATION_ELEMENT_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>UI Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_ELEMENT_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_LABEL__LABEL = 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_LABEL__ICON_URI = 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_LABEL__TOOLTIP = 2;

	/**
	 * The number of structural features of the '<em>UI Label</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UI_LABEL_FEATURE_COUNT = 3;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTEXT__CONTEXT = 0;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTEXT__VARIABLES = 1;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTEXT__PROPERTIES = 2;

	/**
	 * The number of structural features of the '<em>Context</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONTEXT_FEATURE_COUNT = 3;

	/**
	 * The number of structural features of the '<em>VAbstract Containers V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VABSTRACT_CONTAINERS_V_FEATURE_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__TAGS = UI_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__RENDERER = UI_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__TO_BE_RENDERED = UI_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__ON_TOP = UI_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__CONTAINER_DATA = UI_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__CHILDREN = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER__SELECTED_ELEMENT = UI_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Element Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ELEMENT_CONTAINER_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_STACK__ID = ELEMENT_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_STACK__TAGS = ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_STACK__WIDGET = ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_STACK__RENDERER = ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_STACK__TO_BE_RENDERED = ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_STACK__ON_TOP = ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_STACK__VISIBLE = ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_STACK__PARENT = ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_STACK__CONTAINER_DATA = ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_STACK__CHILDREN = ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_STACK__SELECTED_ELEMENT = ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The number of structural features of the '<em>Generic Stack</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_STACK_FEATURE_COUNT = ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__ID = ELEMENT_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__TAGS = ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__WIDGET = ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__RENDERER = ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__TO_BE_RENDERED = ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__ON_TOP = ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__VISIBLE = ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__PARENT = ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__CONTAINER_DATA = ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__CHILDREN = ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__SELECTED_ELEMENT = ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Horizontal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE__HORIZONTAL = ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Generic Tile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERIC_TILE_FEATURE_COUNT = ELEMENT_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>VMenus And TBs V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VMENUS_AND_TBS_V_FEATURE_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__TAGS = UI_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__RENDERER = UI_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__TO_BE_RENDERED = UI_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__ON_TOP = UI_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__CONTAINER_DATA = UI_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__LABEL = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__ICON_URI = UI_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__TOOLTIP = UI_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__ENABLED = UI_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__SELECTED = UI_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM__TYPE = UI_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ITEM_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__ID = ELEMENT_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__TAGS = ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__WIDGET = ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__RENDERER = ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__TO_BE_RENDERED = ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__ON_TOP = ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__VISIBLE = ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__PARENT = ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__CONTAINER_DATA = ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__CHILDREN = ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU__SELECTED_ELEMENT = ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The number of structural features of the '<em>Menu</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_FEATURE_COUNT = ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__ID = MENU__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__TAGS = MENU__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__WIDGET = MENU__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__RENDERER = MENU__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__TO_BE_RENDERED = MENU__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__ON_TOP = MENU__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__VISIBLE = MENU__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__PARENT = MENU__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__CONTAINER_DATA = MENU__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__CHILDREN = MENU__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__SELECTED_ELEMENT = MENU__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__LABEL = MENU_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__ICON_URI = MENU_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__TOOLTIP = MENU_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__ENABLED = MENU_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__SELECTED = MENU_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM__TYPE = MENU_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Menu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MENU_ITEM_FEATURE_COUNT = MENU_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__ID = CONTRIBUTION__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__TAGS = CONTRIBUTION__TAGS;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__URI = CONTRIBUTION__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__OBJECT = CONTRIBUTION__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__PERSISTED_STATE = CONTRIBUTION__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__WIDGET = CONTRIBUTION_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__RENDERER = CONTRIBUTION_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__TO_BE_RENDERED = CONTRIBUTION_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__ON_TOP = CONTRIBUTION_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__VISIBLE = CONTRIBUTION_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__PARENT = CONTRIBUTION_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__CONTAINER_DATA = CONTRIBUTION_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__CHILDREN = CONTRIBUTION_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__SELECTED_ELEMENT = CONTRIBUTION_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__LABEL = CONTRIBUTION_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__ICON_URI = CONTRIBUTION_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__TOOLTIP = CONTRIBUTION_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__ENABLED = CONTRIBUTION_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__SELECTED = CONTRIBUTION_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM__TYPE = CONTRIBUTION_FEATURE_COUNT + 14;

	/**
	 * The number of structural features of the '<em>Direct Menu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_MENU_ITEM_FEATURE_COUNT = CONTRIBUTION_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__ID = ITEM__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__TAGS = ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__WIDGET = ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__RENDERER = ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__TO_BE_RENDERED = ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__ON_TOP = ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__VISIBLE = ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__PARENT = ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__CONTAINER_DATA = ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__LABEL = ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__ICON_URI = ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__TOOLTIP = ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__ENABLED = ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__SELECTED = ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__TYPE = ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__CHILDREN = ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM__SELECTED_ELEMENT = ITEM_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Tool Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_ITEM_FEATURE_COUNT = ITEM_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__ID = TOOL_ITEM__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__TAGS = TOOL_ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__WIDGET = TOOL_ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__RENDERER = TOOL_ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__TO_BE_RENDERED = TOOL_ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__ON_TOP = TOOL_ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__VISIBLE = TOOL_ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__PARENT = TOOL_ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__CONTAINER_DATA = TOOL_ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__LABEL = TOOL_ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__ICON_URI = TOOL_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__TOOLTIP = TOOL_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__ENABLED = TOOL_ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__SELECTED = TOOL_ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__TYPE = TOOL_ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__CHILDREN = TOOL_ITEM__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__SELECTED_ELEMENT = TOOL_ITEM__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__URI = TOOL_ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__OBJECT = TOOL_ITEM_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM__PERSISTED_STATE = TOOL_ITEM_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Direct Tool Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIRECT_TOOL_ITEM_FEATURE_COUNT = TOOL_ITEM_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__ID = ELEMENT_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__TAGS = ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__WIDGET = ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__RENDERER = ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__TO_BE_RENDERED = ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__ON_TOP = ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__VISIBLE = ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__PARENT = ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__CONTAINER_DATA = ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__CHILDREN = ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR__SELECTED_ELEMENT = ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The number of structural features of the '<em>Tool Bar</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOOL_BAR_FEATURE_COUNT = ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>VRCP V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VRCP_V_FEATURE_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__ID = ELEMENT_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__TAGS = ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__WIDGET = ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__RENDERER = ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__TO_BE_RENDERED = ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__ON_TOP = ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__VISIBLE = ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__PARENT = ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__CONTAINER_DATA = ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__CHILDREN = ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__SELECTED_ELEMENT = ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__CONTEXT = ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__VARIABLES = ELEMENT_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__PROPERTIES = ELEMENT_CONTAINER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__HANDLERS = ELEMENT_CONTAINER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Binding Tables</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__BINDING_TABLES = ELEMENT_CONTAINER_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Root Context</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__ROOT_CONTEXT = ELEMENT_CONTAINER_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Descriptors</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__DESCRIPTORS = ELEMENT_CONTAINER_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__BINDING_CONTEXTS = ELEMENT_CONTAINER_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Commands</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION__COMMANDS = ELEMENT_CONTAINER_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>Application</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLICATION_FEATURE_COUNT = ELEMENT_CONTAINER_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__TAGS = UI_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__RENDERER = UI_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__TO_BE_RENDERED = UI_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__ON_TOP = UI_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT__CONTAINER_DATA = UI_ELEMENT__CONTAINER_DATA;

	/**
	 * The number of structural features of the '<em>PSC Element</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PSC_ELEMENT_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__ID = CONTRIBUTION__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__TAGS = CONTRIBUTION__TAGS;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__URI = CONTRIBUTION__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__OBJECT = CONTRIBUTION__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__PERSISTED_STATE = CONTRIBUTION__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__CONTEXT = CONTRIBUTION_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__VARIABLES = CONTRIBUTION_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__PROPERTIES = CONTRIBUTION_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__WIDGET = CONTRIBUTION_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__RENDERER = CONTRIBUTION_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__TO_BE_RENDERED = CONTRIBUTION_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__ON_TOP = CONTRIBUTION_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__VISIBLE = CONTRIBUTION_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__PARENT = CONTRIBUTION_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__CONTAINER_DATA = CONTRIBUTION_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__LABEL = CONTRIBUTION_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__ICON_URI = CONTRIBUTION_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__TOOLTIP = CONTRIBUTION_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__HANDLERS = CONTRIBUTION_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Dirty</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__DIRTY = CONTRIBUTION_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__BINDING_CONTEXTS = CONTRIBUTION_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Menus</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__MENUS = CONTRIBUTION_FEATURE_COUNT + 16;

	/**
	 * The feature id for the '<em><b>Toolbar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__TOOLBAR = CONTRIBUTION_FEATURE_COUNT + 17;

	/**
	 * The feature id for the '<em><b>Closeable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART__CLOSEABLE = CONTRIBUTION_FEATURE_COUNT + 18;

	/**
	 * The number of structural features of the '<em>Part</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_FEATURE_COUNT = CONTRIBUTION_FEATURE_COUNT + 19;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.InputPartImpl <em>Input Part</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.InputPartImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getInputPart()
	 * @generated
	 */
	int INPUT_PART = 26;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__ID = PART__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__TAGS = PART__TAGS;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__URI = PART__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__OBJECT = PART__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__PERSISTED_STATE = PART__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__CONTEXT = PART__CONTEXT;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__VARIABLES = PART__VARIABLES;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__PROPERTIES = PART__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__WIDGET = PART__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__RENDERER = PART__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__TO_BE_RENDERED = PART__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__ON_TOP = PART__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__VISIBLE = PART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__PARENT = PART__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__CONTAINER_DATA = PART__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__LABEL = PART__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__ICON_URI = PART__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__TOOLTIP = PART__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__HANDLERS = PART__HANDLERS;

	/**
	 * The feature id for the '<em><b>Dirty</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__DIRTY = PART__DIRTY;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__BINDING_CONTEXTS = PART__BINDING_CONTEXTS;

	/**
	 * The feature id for the '<em><b>Menus</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__MENUS = PART__MENUS;

	/**
	 * The feature id for the '<em><b>Toolbar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__TOOLBAR = PART__TOOLBAR;

	/**
	 * The feature id for the '<em><b>Closeable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__CLOSEABLE = PART__CLOSEABLE;

	/**
	 * The feature id for the '<em><b>Input URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART__INPUT_URI = PART_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Input Part</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INPUT_PART_FEATURE_COUNT = PART_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__ID = PART__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__TAGS = PART__TAGS;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__URI = PART__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__OBJECT = PART__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__PERSISTED_STATE = PART__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__CONTEXT = PART__CONTEXT;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__VARIABLES = PART__VARIABLES;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__PROPERTIES = PART__PROPERTIES;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__WIDGET = PART__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__RENDERER = PART__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__TO_BE_RENDERED = PART__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__ON_TOP = PART__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__VISIBLE = PART__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__PARENT = PART__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__CONTAINER_DATA = PART__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__LABEL = PART__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__ICON_URI = PART__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__TOOLTIP = PART__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__HANDLERS = PART__HANDLERS;

	/**
	 * The feature id for the '<em><b>Dirty</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__DIRTY = PART__DIRTY;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__BINDING_CONTEXTS = PART__BINDING_CONTEXTS;

	/**
	 * The feature id for the '<em><b>Menus</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__MENUS = PART__MENUS;

	/**
	 * The feature id for the '<em><b>Toolbar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__TOOLBAR = PART__TOOLBAR;

	/**
	 * The feature id for the '<em><b>Closeable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__CLOSEABLE = PART__CLOSEABLE;

	/**
	 * The feature id for the '<em><b>Allow Multiple</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__ALLOW_MULTIPLE = PART_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Category</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR__CATEGORY = PART_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Part Descriptor</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR_FEATURE_COUNT = PART_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Descriptors</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR_CONTAINER__DESCRIPTORS = 0;

	/**
	 * The number of structural features of the '<em>Part Descriptor Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__ID = GENERIC_STACK__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__TAGS = GENERIC_STACK__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__WIDGET = GENERIC_STACK__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__RENDERER = GENERIC_STACK__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__TO_BE_RENDERED = GENERIC_STACK__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__ON_TOP = GENERIC_STACK__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__VISIBLE = GENERIC_STACK__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__PARENT = GENERIC_STACK__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__CONTAINER_DATA = GENERIC_STACK__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__CHILDREN = GENERIC_STACK__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK__SELECTED_ELEMENT = GENERIC_STACK__SELECTED_ELEMENT;

	/**
	 * The number of structural features of the '<em>Part Stack</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_STACK_FEATURE_COUNT = GENERIC_STACK_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__ID = GENERIC_TILE__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__TAGS = GENERIC_TILE__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__WIDGET = GENERIC_TILE__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__RENDERER = GENERIC_TILE__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__TO_BE_RENDERED = GENERIC_TILE__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__ON_TOP = GENERIC_TILE__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__VISIBLE = GENERIC_TILE__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__PARENT = GENERIC_TILE__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__CONTAINER_DATA = GENERIC_TILE__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__CHILDREN = GENERIC_TILE__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__SELECTED_ELEMENT = GENERIC_TILE__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Horizontal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER__HORIZONTAL = GENERIC_TILE__HORIZONTAL;

	/**
	 * The number of structural features of the '<em>Part Sash Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PART_SASH_CONTAINER_FEATURE_COUNT = GENERIC_TILE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__ID = ELEMENT_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__TAGS = ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__WIDGET = ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__RENDERER = ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__TO_BE_RENDERED = ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__ON_TOP = ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__VISIBLE = ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__PARENT = ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__CONTAINER_DATA = ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__CHILDREN = ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__SELECTED_ELEMENT = ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__LABEL = ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__ICON_URI = ELEMENT_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__TOOLTIP = ELEMENT_CONTAINER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__CONTEXT = ELEMENT_CONTAINER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__VARIABLES = ELEMENT_CONTAINER_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__PROPERTIES = ELEMENT_CONTAINER_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__HANDLERS = ELEMENT_CONTAINER_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__BINDING_CONTEXTS = ELEMENT_CONTAINER_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Main Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__MAIN_MENU = ELEMENT_CONTAINER_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>X</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__X = ELEMENT_CONTAINER_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Y</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__Y = ELEMENT_CONTAINER_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>Width</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__WIDTH = ELEMENT_CONTAINER_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>Height</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW__HEIGHT = ELEMENT_CONTAINER_FEATURE_COUNT + 12;

	/**
	 * The number of structural features of the '<em>Window</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_FEATURE_COUNT = ELEMENT_CONTAINER_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Components</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENTS__COMPONENTS = 0;

	/**
	 * The number of structural features of the '<em>Model Components</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENTS_FEATURE_COUNT = 1;

	/**
	 * The feature id for the '<em><b>Descriptors</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT__DESCRIPTORS = PART_DESCRIPTOR_CONTAINER__DESCRIPTORS;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT__ID = PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT__TAGS = PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT__HANDLERS = PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Binding Tables</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT__BINDING_TABLES = PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Root Context</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT__ROOT_CONTEXT = PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Position In Parent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT__POSITION_IN_PARENT = PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Parent ID</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT__PARENT_ID = PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT__CHILDREN = PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Commands</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT__COMMANDS = PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Processor</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT__PROCESSOR = PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT__BINDINGS = PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT + 10;

	/**
	 * The number of structural features of the '<em>Model Component</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MODEL_COMPONENT_FEATURE_COUNT = PART_DESCRIPTOR_CONTAINER_FEATURE_COUNT + 11;

	/**
	 * The number of structural features of the '<em>VCommands V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VCOMMANDS_V_FEATURE_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Binding Tables</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_CONTAINER__BINDING_TABLES = 0;

	/**
	 * The feature id for the '<em><b>Root Context</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_CONTAINER__ROOT_CONTEXT = 1;

	/**
	 * The number of structural features of the '<em>Binding Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_CONTAINER_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.MBindings <em>Bindings</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.MBindings
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getBindings()
	 * @generated
	 */
	int BINDINGS = 36;

	/**
	 * The feature id for the '<em><b>Binding Contexts</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDINGS__BINDING_CONTEXTS = 0;

	/**
	 * The number of structural features of the '<em>Bindings</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDINGS_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.BindingContextImpl <em>Binding Context</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.BindingContextImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getBindingContext()
	 * @generated
	 */
	int BINDING_CONTEXT = 37;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_CONTEXT__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_CONTEXT__TAGS = APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_CONTEXT__NAME = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_CONTEXT__DESCRIPTION = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_CONTEXT__CHILDREN = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Binding Context</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_CONTEXT_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.impl.BindingTableImpl <em>Binding Table</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.impl.BindingTableImpl
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getBindingTable()
	 * @generated
	 */
	int BINDING_TABLE = 38;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_TABLE__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_TABLE__TAGS = APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Binding Context Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_TABLE__BINDING_CONTEXT_ID = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_TABLE__BINDINGS = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Binding Table</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BINDING_TABLE_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND__TAGS = APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Command Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND__COMMAND_NAME = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND__DESCRIPTION = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND__PARAMETERS = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Command</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND_PARAMETER__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND_PARAMETER__TAGS = APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND_PARAMETER__NAME = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Type Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND_PARAMETER__TYPE_ID = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Optional</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND_PARAMETER__OPTIONAL = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Command Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMAND_PARAMETER_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLER__ID = CONTRIBUTION__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLER__TAGS = CONTRIBUTION__TAGS;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLER__URI = CONTRIBUTION__URI;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLER__OBJECT = CONTRIBUTION__OBJECT;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLER__PERSISTED_STATE = CONTRIBUTION__PERSISTED_STATE;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLER__COMMAND = CONTRIBUTION_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Handler</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLER_FEATURE_COUNT = CONTRIBUTION_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLER_CONTAINER__HANDLERS = 0;

	/**
	 * The number of structural features of the '<em>Handler Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLER_CONTAINER_FEATURE_COUNT = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__ID = ITEM__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__TAGS = ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__WIDGET = ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__RENDERER = ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__TO_BE_RENDERED = ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__ON_TOP = ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__VISIBLE = ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__PARENT = ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__CONTAINER_DATA = ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__LABEL = ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__ICON_URI = ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__TOOLTIP = ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__ENABLED = ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__SELECTED = ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__TYPE = ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__COMMAND = ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Wb Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__WB_COMMAND = ITEM_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM__PARAMETERS = ITEM_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Handled Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_ITEM_FEATURE_COUNT = ITEM_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__ID = MENU_ITEM__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__TAGS = MENU_ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__WIDGET = MENU_ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__RENDERER = MENU_ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__TO_BE_RENDERED = MENU_ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__ON_TOP = MENU_ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__VISIBLE = MENU_ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__PARENT = MENU_ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__CONTAINER_DATA = MENU_ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__CHILDREN = MENU_ITEM__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__SELECTED_ELEMENT = MENU_ITEM__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__LABEL = MENU_ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__ICON_URI = MENU_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__TOOLTIP = MENU_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__ENABLED = MENU_ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__SELECTED = MENU_ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__TYPE = MENU_ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__COMMAND = MENU_ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Wb Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__WB_COMMAND = MENU_ITEM_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM__PARAMETERS = MENU_ITEM_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Handled Menu Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_MENU_ITEM_FEATURE_COUNT = MENU_ITEM_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__ID = TOOL_ITEM__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__TAGS = TOOL_ITEM__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__WIDGET = TOOL_ITEM__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__RENDERER = TOOL_ITEM__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__TO_BE_RENDERED = TOOL_ITEM__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__ON_TOP = TOOL_ITEM__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__VISIBLE = TOOL_ITEM__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__PARENT = TOOL_ITEM__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__CONTAINER_DATA = TOOL_ITEM__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__LABEL = TOOL_ITEM__LABEL;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__ICON_URI = TOOL_ITEM__ICON_URI;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__TOOLTIP = TOOL_ITEM__TOOLTIP;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__ENABLED = TOOL_ITEM__ENABLED;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__SELECTED = TOOL_ITEM__SELECTED;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__TYPE = TOOL_ITEM__TYPE;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__CHILDREN = TOOL_ITEM__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__SELECTED_ELEMENT = TOOL_ITEM__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__COMMAND = TOOL_ITEM_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Wb Command</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__WB_COMMAND = TOOL_ITEM_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM__PARAMETERS = TOOL_ITEM_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Handled Tool Item</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HANDLED_TOOL_ITEM_FEATURE_COUNT = TOOL_ITEM_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Key Sequence</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEY_SEQUENCE__KEY_SEQUENCE = 0;

	/**
	 * The number of structural features of the '<em>Key Sequence</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEY_SEQUENCE_FEATURE_COUNT = 1;

	/**
	 * The feature id for the '<em><b>Key Sequence</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEY_BINDING__KEY_SEQUENCE = KEY_SEQUENCE__KEY_SEQUENCE;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEY_BINDING__ID = KEY_SEQUENCE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEY_BINDING__TAGS = KEY_SEQUENCE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Command</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEY_BINDING__COMMAND = KEY_SEQUENCE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEY_BINDING__PARAMETERS = KEY_SEQUENCE_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Key Binding</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KEY_BINDING_FEATURE_COUNT = KEY_SEQUENCE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__TAGS = APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Tag</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__TAG = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER__VALUE = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PARAMETER_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>VTrim V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VTRIM_V_FEATURE_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER__ID = ELEMENT_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER__TAGS = ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER__WIDGET = ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER__RENDERER = ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER__TO_BE_RENDERED = ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER__ON_TOP = ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER__VISIBLE = ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER__PARENT = ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER__CONTAINER_DATA = ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER__CHILDREN = ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER__SELECTED_ELEMENT = ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Side</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER__SIDE = ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Trim Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRIM_CONTAINER_FEATURE_COUNT = ELEMENT_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM__ID = TRIM_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM__TAGS = TRIM_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM__WIDGET = TRIM_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM__RENDERER = TRIM_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM__TO_BE_RENDERED = TRIM_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM__ON_TOP = TRIM_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM__VISIBLE = TRIM_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM__PARENT = TRIM_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM__CONTAINER_DATA = TRIM_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM__CHILDREN = TRIM_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM__SELECTED_ELEMENT = TRIM_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Side</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM__SIDE = TRIM_CONTAINER__SIDE;

	/**
	 * The number of structural features of the '<em>Window Trim</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WINDOW_TRIM_FEATURE_COUNT = TRIM_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>VShared Elements V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VSHARED_ELEMENTS_V_FEATURE_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLACEHOLDER__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLACEHOLDER__TAGS = UI_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLACEHOLDER__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLACEHOLDER__RENDERER = UI_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLACEHOLDER__TO_BE_RENDERED = UI_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLACEHOLDER__ON_TOP = UI_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLACEHOLDER__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLACEHOLDER__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLACEHOLDER__CONTAINER_DATA = UI_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLACEHOLDER__REF = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Placeholder</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLACEHOLDER_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__ID = ELEMENT_CONTAINER__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__TAGS = ELEMENT_CONTAINER__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__WIDGET = ELEMENT_CONTAINER__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__RENDERER = ELEMENT_CONTAINER__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__TO_BE_RENDERED = ELEMENT_CONTAINER__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__ON_TOP = ELEMENT_CONTAINER__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__VISIBLE = ELEMENT_CONTAINER__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__PARENT = ELEMENT_CONTAINER__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__CONTAINER_DATA = ELEMENT_CONTAINER__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__CHILDREN = ELEMENT_CONTAINER__CHILDREN;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__SELECTED_ELEMENT = ELEMENT_CONTAINER__SELECTED_ELEMENT;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__LABEL = ELEMENT_CONTAINER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__ICON_URI = ELEMENT_CONTAINER_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__TOOLTIP = ELEMENT_CONTAINER_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__CONTEXT = ELEMENT_CONTAINER_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__VARIABLES = ELEMENT_CONTAINER_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE__PROPERTIES = ELEMENT_CONTAINER_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Perspective</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_FEATURE_COUNT = ELEMENT_CONTAINER_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__ID = UI_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__TAGS = UI_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__WIDGET = UI_ELEMENT__WIDGET;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__RENDERER = UI_ELEMENT__RENDERER;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__TO_BE_RENDERED = UI_ELEMENT__TO_BE_RENDERED;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__ON_TOP = UI_ELEMENT__ON_TOP;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__VISIBLE = UI_ELEMENT__VISIBLE;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__PARENT = UI_ELEMENT__PARENT;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__CONTAINER_DATA = UI_ELEMENT__CONTAINER_DATA;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__CHILDREN = UI_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK__SELECTED_ELEMENT = UI_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Perspective Stack</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PERSPECTIVE_STACK_FEATURE_COUNT = UI_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>VTesting V</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VTESTING_V_FEATURE_COUNT = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ID = APPLICATION_ELEMENT__ID;

	/**
	 * The feature id for the '<em><b>Tags</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__TAGS = APPLICATION_ELEMENT__TAGS;

	/**
	 * The feature id for the '<em><b>Command Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__COMMAND_NAME = APPLICATION_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__DESCRIPTION = APPLICATION_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__PARAMETERS = APPLICATION_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Context</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__CONTEXT = APPLICATION_ELEMENT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Variables</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__VARIABLES = APPLICATION_ELEMENT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__PROPERTIES = APPLICATION_ELEMENT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__URI = APPLICATION_ELEMENT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Object</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__OBJECT = APPLICATION_ELEMENT_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Persisted State</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__PERSISTED_STATE = APPLICATION_ELEMENT_FEATURE_COUNT + 8;

	/**
	 * The feature id for the '<em><b>Widget</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__WIDGET = APPLICATION_ELEMENT_FEATURE_COUNT + 9;

	/**
	 * The feature id for the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__RENDERER = APPLICATION_ELEMENT_FEATURE_COUNT + 10;

	/**
	 * The feature id for the '<em><b>To Be Rendered</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__TO_BE_RENDERED = APPLICATION_ELEMENT_FEATURE_COUNT + 11;

	/**
	 * The feature id for the '<em><b>On Top</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ON_TOP = APPLICATION_ELEMENT_FEATURE_COUNT + 12;

	/**
	 * The feature id for the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__VISIBLE = APPLICATION_ELEMENT_FEATURE_COUNT + 13;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__PARENT = APPLICATION_ELEMENT_FEATURE_COUNT + 14;

	/**
	 * The feature id for the '<em><b>Container Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__CONTAINER_DATA = APPLICATION_ELEMENT_FEATURE_COUNT + 15;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__CHILDREN = APPLICATION_ELEMENT_FEATURE_COUNT + 16;

	/**
	 * The feature id for the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__SELECTED_ELEMENT = APPLICATION_ELEMENT_FEATURE_COUNT + 17;

	/**
	 * The feature id for the '<em><b>Tag</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__TAG = APPLICATION_ELEMENT_FEATURE_COUNT + 18;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__VALUE = APPLICATION_ELEMENT_FEATURE_COUNT + 19;

	/**
	 * The feature id for the '<em><b>Input URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__INPUT_URI = APPLICATION_ELEMENT_FEATURE_COUNT + 20;

	/**
	 * The feature id for the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__LABEL = APPLICATION_ELEMENT_FEATURE_COUNT + 21;

	/**
	 * The feature id for the '<em><b>Icon URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ICON_URI = APPLICATION_ELEMENT_FEATURE_COUNT + 22;

	/**
	 * The feature id for the '<em><b>Tooltip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__TOOLTIP = APPLICATION_ELEMENT_FEATURE_COUNT + 23;

	/**
	 * The feature id for the '<em><b>Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__ENABLED = APPLICATION_ELEMENT_FEATURE_COUNT + 24;

	/**
	 * The feature id for the '<em><b>Selected</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__SELECTED = APPLICATION_ELEMENT_FEATURE_COUNT + 25;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__TYPE = APPLICATION_ELEMENT_FEATURE_COUNT + 26;

	/**
	 * The feature id for the '<em><b>Dirty</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS__DIRTY = APPLICATION_ELEMENT_FEATURE_COUNT + 27;

	/**
	 * The number of structural features of the '<em>Test Harness</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEST_HARNESS_FEATURE_COUNT = APPLICATION_ELEMENT_FEATURE_COUNT + 28;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.ItemType <em>Item Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.ItemType
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getItemType()
	 * @generated
	 */
	int ITEM_TYPE = 58;

	/**
	 * The meta object id for the '{@link org.eclipse.e4.ui.model.application.SideValue <em>Side Value</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.ui.model.application.SideValue
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getSideValue()
	 * @generated
	 */
	int SIDE_VALUE = 59;

	/**
	 * The meta object id for the '<em>IEclipse Context</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.e4.core.services.context.IEclipseContext
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getIEclipseContext()
	 * @generated
	 */
	int IECLIPSE_CONTEXT = 60;

	/**
	 * The meta object id for the '<em>Parameterized Command</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.core.commands.ParameterizedCommand
	 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getParameterizedCommand()
	 * @generated
	 */
	int PARAMETERIZED_COMMAND = 61;


	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV____________ConstantsAndTypes_____________V <em>VConstants And Types V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VConstants And Types V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV____________ConstantsAndTypes_____________V
	 * @generated
	 */
	EClass getV____________ConstantsAndTypes_____________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV____________Abstract_____________V <em>VAbstract V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VAbstract V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV____________Abstract_____________V
	 * @generated
	 */
	EClass getV____________Abstract_____________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MApplicationElement <em>Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplicationElement
	 * @generated
	 */
	EClass getApplicationElement();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MApplicationElement#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplicationElement#getId()
	 * @see #getApplicationElement()
	 * @generated
	 */
	EAttribute getApplicationElement_Id();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.e4.ui.model.application.MApplicationElement#getTags <em>Tags</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Tags</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplicationElement#getTags()
	 * @see #getApplicationElement()
	 * @generated
	 */
	EAttribute getApplicationElement_Tags();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MContribution <em>Contribution</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Contribution</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContribution
	 * @generated
	 */
	EClass getContribution();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MContribution#getURI <em>URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>URI</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContribution#getURI()
	 * @see #getContribution()
	 * @generated
	 */
	EAttribute getContribution_URI();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MContribution#getObject <em>Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Object</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContribution#getObject()
	 * @see #getContribution()
	 * @generated
	 */
	EAttribute getContribution_Object();

	/**
	 * Returns the meta object for the map '{@link org.eclipse.e4.ui.model.application.MContribution#getPersistedState <em>Persisted State</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Persisted State</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContribution#getPersistedState()
	 * @see #getContribution()
	 * @generated
	 */
	EReference getContribution_PersistedState();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MDirtyable <em>Dirtyable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Dirtyable</em>'.
	 * @see org.eclipse.e4.ui.model.application.MDirtyable
	 * @generated
	 */
	EClass getDirtyable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MDirtyable#isDirty <em>Dirty</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dirty</em>'.
	 * @see org.eclipse.e4.ui.model.application.MDirtyable#isDirty()
	 * @see #getDirtyable()
	 * @generated
	 */
	EAttribute getDirtyable_Dirty();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MInput <em>Input</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Input</em>'.
	 * @see org.eclipse.e4.ui.model.application.MInput
	 * @generated
	 */
	EClass getInput();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MInput#getInputURI <em>Input URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Input URI</em>'.
	 * @see org.eclipse.e4.ui.model.application.MInput#getInputURI()
	 * @see #getInput()
	 * @generated
	 */
	EAttribute getInput_InputURI();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MUIElement <em>UI Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>UI Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement
	 * @generated
	 */
	EClass getUIElement();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUIElement#getWidget <em>Widget</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Widget</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement#getWidget()
	 * @see #getUIElement()
	 * @generated
	 */
	EAttribute getUIElement_Widget();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUIElement#getRenderer <em>Renderer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Renderer</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement#getRenderer()
	 * @see #getUIElement()
	 * @generated
	 */
	EAttribute getUIElement_Renderer();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUIElement#isToBeRendered <em>To Be Rendered</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>To Be Rendered</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement#isToBeRendered()
	 * @see #getUIElement()
	 * @generated
	 */
	EAttribute getUIElement_ToBeRendered();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUIElement#isOnTop <em>On Top</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>On Top</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement#isOnTop()
	 * @see #getUIElement()
	 * @generated
	 */
	EAttribute getUIElement_OnTop();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUIElement#isVisible <em>Visible</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Visible</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement#isVisible()
	 * @see #getUIElement()
	 * @generated
	 */
	EAttribute getUIElement_Visible();

	/**
	 * Returns the meta object for the container reference '{@link org.eclipse.e4.ui.model.application.MUIElement#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Parent</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement#getParent()
	 * @see #getUIElement()
	 * @generated
	 */
	EReference getUIElement_Parent();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUIElement#getContainerData <em>Container Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Container Data</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUIElement#getContainerData()
	 * @see #getUIElement()
	 * @generated
	 */
	EAttribute getUIElement_ContainerData();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MUILabel <em>UI Label</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>UI Label</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUILabel
	 * @generated
	 */
	EClass getUILabel();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUILabel#getLabel <em>Label</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Label</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUILabel#getLabel()
	 * @see #getUILabel()
	 * @generated
	 */
	EAttribute getUILabel_Label();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUILabel#getIconURI <em>Icon URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Icon URI</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUILabel#getIconURI()
	 * @see #getUILabel()
	 * @generated
	 */
	EAttribute getUILabel_IconURI();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MUILabel#getTooltip <em>Tooltip</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tooltip</em>'.
	 * @see org.eclipse.e4.ui.model.application.MUILabel#getTooltip()
	 * @see #getUILabel()
	 * @generated
	 */
	EAttribute getUILabel_Tooltip();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MContext <em>Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Context</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContext
	 * @generated
	 */
	EClass getContext();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MContext#getContext <em>Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Context</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContext#getContext()
	 * @see #getContext()
	 * @generated
	 */
	EAttribute getContext_Context();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.e4.ui.model.application.MContext#getVariables <em>Variables</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Variables</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContext#getVariables()
	 * @see #getContext()
	 * @generated
	 */
	EAttribute getContext_Variables();

	/**
	 * Returns the meta object for the map '{@link org.eclipse.e4.ui.model.application.MContext#getProperties <em>Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Properties</em>'.
	 * @see org.eclipse.e4.ui.model.application.MContext#getProperties()
	 * @see #getContext()
	 * @generated
	 */
	EReference getContext_Properties();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V <em>VAbstract Containers V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VAbstract Containers V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V
	 * @generated
	 */
	EClass getV_________AbstractContainers__________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MElementContainer <em>Element Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Element Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.MElementContainer
	 * @generated
	 */
	EClass getElementContainer();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MElementContainer#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Children</em>'.
	 * @see org.eclipse.e4.ui.model.application.MElementContainer#getChildren()
	 * @see #getElementContainer()
	 * @generated
	 */
	EReference getElementContainer_Children();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MElementContainer#getSelectedElement <em>Selected Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Selected Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.MElementContainer#getSelectedElement()
	 * @see #getElementContainer()
	 * @generated
	 */
	EReference getElementContainer_SelectedElement();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MGenericStack <em>Generic Stack</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Generic Stack</em>'.
	 * @see org.eclipse.e4.ui.model.application.MGenericStack
	 * @generated
	 */
	EClass getGenericStack();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MGenericTile <em>Generic Tile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Generic Tile</em>'.
	 * @see org.eclipse.e4.ui.model.application.MGenericTile
	 * @generated
	 */
	EClass getGenericTile();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MGenericTile#isHorizontal <em>Horizontal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Horizontal</em>'.
	 * @see org.eclipse.e4.ui.model.application.MGenericTile#isHorizontal()
	 * @see #getGenericTile()
	 * @generated
	 */
	EAttribute getGenericTile_Horizontal();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV______________MenusAndTBs_______________V <em>VMenus And TBs V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VMenus And TBs V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV______________MenusAndTBs_______________V
	 * @generated
	 */
	EClass getV______________MenusAndTBs_______________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MItem <em>Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItem
	 * @generated
	 */
	EClass getItem();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MItem#isEnabled <em>Enabled</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Enabled</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItem#isEnabled()
	 * @see #getItem()
	 * @generated
	 */
	EAttribute getItem_Enabled();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MItem#isSelected <em>Selected</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Selected</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItem#isSelected()
	 * @see #getItem()
	 * @generated
	 */
	EAttribute getItem_Selected();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MItem#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see org.eclipse.e4.ui.model.application.MItem#getType()
	 * @see #getItem()
	 * @generated
	 */
	EAttribute getItem_Type();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MMenuItem <em>Menu Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Menu Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MMenuItem
	 * @generated
	 */
	EClass getMenuItem();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MDirectMenuItem <em>Direct Menu Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Direct Menu Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MDirectMenuItem
	 * @generated
	 */
	EClass getDirectMenuItem();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MMenu <em>Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.MMenu
	 * @generated
	 */
	EClass getMenu();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MToolItem <em>Tool Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tool Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MToolItem
	 * @generated
	 */
	EClass getToolItem();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MDirectToolItem <em>Direct Tool Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Direct Tool Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MDirectToolItem
	 * @generated
	 */
	EClass getDirectToolItem();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MToolBar <em>Tool Bar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tool Bar</em>'.
	 * @see org.eclipse.e4.ui.model.application.MToolBar
	 * @generated
	 */
	EClass getToolBar();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV______________RCP_______________V <em>VRCP V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VRCP V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV______________RCP_______________V
	 * @generated
	 */
	EClass getV______________RCP_______________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MApplication <em>Application</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Application</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplication
	 * @generated
	 */
	EClass getApplication();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MApplication#getCommands <em>Commands</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Commands</em>'.
	 * @see org.eclipse.e4.ui.model.application.MApplication#getCommands()
	 * @see #getApplication()
	 * @generated
	 */
	EReference getApplication_Commands();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPSCElement <em>PSC Element</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>PSC Element</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPSCElement
	 * @generated
	 */
	EClass getPSCElement();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPart <em>Part</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart
	 * @generated
	 */
	EClass getPart();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MPart#getMenus <em>Menus</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Menus</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getMenus()
	 * @see #getPart()
	 * @generated
	 */
	EReference getPart_Menus();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.MPart#getToolbar <em>Toolbar</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Toolbar</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#getToolbar()
	 * @see #getPart()
	 * @generated
	 */
	EReference getPart_Toolbar();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MPart#isCloseable <em>Closeable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Closeable</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPart#isCloseable()
	 * @see #getPart()
	 * @generated
	 */
	EAttribute getPart_Closeable();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MInputPart <em>Input Part</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Input Part</em>'.
	 * @see org.eclipse.e4.ui.model.application.MInputPart
	 * @generated
	 */
	EClass getInputPart();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPartDescriptor <em>Part Descriptor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part Descriptor</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPartDescriptor
	 * @generated
	 */
	EClass getPartDescriptor();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MPartDescriptor#isAllowMultiple <em>Allow Multiple</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Allow Multiple</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPartDescriptor#isAllowMultiple()
	 * @see #getPartDescriptor()
	 * @generated
	 */
	EAttribute getPartDescriptor_AllowMultiple();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MPartDescriptor#getCategory <em>Category</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Category</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPartDescriptor#getCategory()
	 * @see #getPartDescriptor()
	 * @generated
	 */
	EAttribute getPartDescriptor_Category();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPartDescriptorContainer <em>Part Descriptor Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part Descriptor Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPartDescriptorContainer
	 * @generated
	 */
	EClass getPartDescriptorContainer();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MPartDescriptorContainer#getDescriptors <em>Descriptors</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Descriptors</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPartDescriptorContainer#getDescriptors()
	 * @see #getPartDescriptorContainer()
	 * @generated
	 */
	EReference getPartDescriptorContainer_Descriptors();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPartStack <em>Part Stack</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part Stack</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPartStack
	 * @generated
	 */
	EClass getPartStack();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPartSashContainer <em>Part Sash Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Part Sash Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPartSashContainer
	 * @generated
	 */
	EClass getPartSashContainer();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MWindow <em>Window</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Window</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow
	 * @generated
	 */
	EClass getWindow();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.MWindow#getMainMenu <em>Main Menu</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Main Menu</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getMainMenu()
	 * @see #getWindow()
	 * @generated
	 */
	EReference getWindow_MainMenu();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MWindow#getX <em>X</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>X</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getX()
	 * @see #getWindow()
	 * @generated
	 */
	EAttribute getWindow_X();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MWindow#getY <em>Y</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Y</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getY()
	 * @see #getWindow()
	 * @generated
	 */
	EAttribute getWindow_Y();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MWindow#getWidth <em>Width</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Width</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getWidth()
	 * @see #getWindow()
	 * @generated
	 */
	EAttribute getWindow_Width();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MWindow#getHeight <em>Height</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Height</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindow#getHeight()
	 * @see #getWindow()
	 * @generated
	 */
	EAttribute getWindow_Height();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MModelComponents <em>Model Components</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Model Components</em>'.
	 * @see org.eclipse.e4.ui.model.application.MModelComponents
	 * @generated
	 */
	EClass getModelComponents();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MModelComponents#getComponents <em>Components</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Components</em>'.
	 * @see org.eclipse.e4.ui.model.application.MModelComponents#getComponents()
	 * @see #getModelComponents()
	 * @generated
	 */
	EReference getModelComponents_Components();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MModelComponent <em>Model Component</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Model Component</em>'.
	 * @see org.eclipse.e4.ui.model.application.MModelComponent
	 * @generated
	 */
	EClass getModelComponent();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MModelComponent#getPositionInParent <em>Position In Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Position In Parent</em>'.
	 * @see org.eclipse.e4.ui.model.application.MModelComponent#getPositionInParent()
	 * @see #getModelComponent()
	 * @generated
	 */
	EAttribute getModelComponent_PositionInParent();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MModelComponent#getParentID <em>Parent ID</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Parent ID</em>'.
	 * @see org.eclipse.e4.ui.model.application.MModelComponent#getParentID()
	 * @see #getModelComponent()
	 * @generated
	 */
	EAttribute getModelComponent_ParentID();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MModelComponent#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Children</em>'.
	 * @see org.eclipse.e4.ui.model.application.MModelComponent#getChildren()
	 * @see #getModelComponent()
	 * @generated
	 */
	EReference getModelComponent_Children();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MModelComponent#getCommands <em>Commands</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Commands</em>'.
	 * @see org.eclipse.e4.ui.model.application.MModelComponent#getCommands()
	 * @see #getModelComponent()
	 * @generated
	 */
	EReference getModelComponent_Commands();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MModelComponent#getProcessor <em>Processor</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Processor</em>'.
	 * @see org.eclipse.e4.ui.model.application.MModelComponent#getProcessor()
	 * @see #getModelComponent()
	 * @generated
	 */
	EAttribute getModelComponent_Processor();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MModelComponent#getBindings <em>Bindings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Bindings</em>'.
	 * @see org.eclipse.e4.ui.model.application.MModelComponent#getBindings()
	 * @see #getModelComponent()
	 * @generated
	 */
	EReference getModelComponent_Bindings();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV______________Commands_______________V <em>VCommands V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VCommands V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV______________Commands_______________V
	 * @generated
	 */
	EClass getV______________Commands_______________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MBindingContainer <em>Binding Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Binding Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.MBindingContainer
	 * @generated
	 */
	EClass getBindingContainer();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MBindingContainer#getBindingTables <em>Binding Tables</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Binding Tables</em>'.
	 * @see org.eclipse.e4.ui.model.application.MBindingContainer#getBindingTables()
	 * @see #getBindingContainer()
	 * @generated
	 */
	EReference getBindingContainer_BindingTables();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.e4.ui.model.application.MBindingContainer#getRootContext <em>Root Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Root Context</em>'.
	 * @see org.eclipse.e4.ui.model.application.MBindingContainer#getRootContext()
	 * @see #getBindingContainer()
	 * @generated
	 */
	EReference getBindingContainer_RootContext();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MBindings <em>Bindings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Bindings</em>'.
	 * @see org.eclipse.e4.ui.model.application.MBindings
	 * @generated
	 */
	EClass getBindings();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.e4.ui.model.application.MBindings#getBindingContexts <em>Binding Contexts</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Binding Contexts</em>'.
	 * @see org.eclipse.e4.ui.model.application.MBindings#getBindingContexts()
	 * @see #getBindings()
	 * @generated
	 */
	EAttribute getBindings_BindingContexts();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MBindingContext <em>Binding Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Binding Context</em>'.
	 * @see org.eclipse.e4.ui.model.application.MBindingContext
	 * @generated
	 */
	EClass getBindingContext();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MBindingContext#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.MBindingContext#getName()
	 * @see #getBindingContext()
	 * @generated
	 */
	EAttribute getBindingContext_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MBindingContext#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.e4.ui.model.application.MBindingContext#getDescription()
	 * @see #getBindingContext()
	 * @generated
	 */
	EAttribute getBindingContext_Description();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MBindingContext#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Children</em>'.
	 * @see org.eclipse.e4.ui.model.application.MBindingContext#getChildren()
	 * @see #getBindingContext()
	 * @generated
	 */
	EReference getBindingContext_Children();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MBindingTable <em>Binding Table</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Binding Table</em>'.
	 * @see org.eclipse.e4.ui.model.application.MBindingTable
	 * @generated
	 */
	EClass getBindingTable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MBindingTable#getBindingContextId <em>Binding Context Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Binding Context Id</em>'.
	 * @see org.eclipse.e4.ui.model.application.MBindingTable#getBindingContextId()
	 * @see #getBindingTable()
	 * @generated
	 */
	EAttribute getBindingTable_BindingContextId();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MBindingTable#getBindings <em>Bindings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Bindings</em>'.
	 * @see org.eclipse.e4.ui.model.application.MBindingTable#getBindings()
	 * @see #getBindingTable()
	 * @generated
	 */
	EReference getBindingTable_Bindings();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommand
	 * @generated
	 */
	EClass getCommand();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MCommand#getCommandName <em>Command Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Command Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommand#getCommandName()
	 * @see #getCommand()
	 * @generated
	 */
	EAttribute getCommand_CommandName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MCommand#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommand#getDescription()
	 * @see #getCommand()
	 * @generated
	 */
	EAttribute getCommand_Description();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MCommand#getParameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Parameters</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommand#getParameters()
	 * @see #getCommand()
	 * @generated
	 */
	EReference getCommand_Parameters();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MCommandParameter <em>Command Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Command Parameter</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommandParameter
	 * @generated
	 */
	EClass getCommandParameter();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MCommandParameter#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommandParameter#getName()
	 * @see #getCommandParameter()
	 * @generated
	 */
	EAttribute getCommandParameter_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MCommandParameter#getTypeId <em>Type Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type Id</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommandParameter#getTypeId()
	 * @see #getCommandParameter()
	 * @generated
	 */
	EAttribute getCommandParameter_TypeId();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MCommandParameter#isOptional <em>Optional</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Optional</em>'.
	 * @see org.eclipse.e4.ui.model.application.MCommandParameter#isOptional()
	 * @see #getCommandParameter()
	 * @generated
	 */
	EAttribute getCommandParameter_Optional();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MHandler <em>Handler</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Handler</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandler
	 * @generated
	 */
	EClass getHandler();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MHandler#getCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandler#getCommand()
	 * @see #getHandler()
	 * @generated
	 */
	EReference getHandler_Command();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MHandlerContainer <em>Handler Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Handler Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandlerContainer
	 * @generated
	 */
	EClass getHandlerContainer();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MHandlerContainer#getHandlers <em>Handlers</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Handlers</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandlerContainer#getHandlers()
	 * @see #getHandlerContainer()
	 * @generated
	 */
	EReference getHandlerContainer_Handlers();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MHandledItem <em>Handled Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Handled Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledItem
	 * @generated
	 */
	EClass getHandledItem();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MHandledItem#getCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledItem#getCommand()
	 * @see #getHandledItem()
	 * @generated
	 */
	EReference getHandledItem_Command();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MHandledItem#getWbCommand <em>Wb Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Wb Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledItem#getWbCommand()
	 * @see #getHandledItem()
	 * @generated
	 */
	EAttribute getHandledItem_WbCommand();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MHandledItem#getParameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Parameters</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledItem#getParameters()
	 * @see #getHandledItem()
	 * @generated
	 */
	EReference getHandledItem_Parameters();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MHandledMenuItem <em>Handled Menu Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Handled Menu Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledMenuItem
	 * @generated
	 */
	EClass getHandledMenuItem();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MHandledToolItem <em>Handled Tool Item</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Handled Tool Item</em>'.
	 * @see org.eclipse.e4.ui.model.application.MHandledToolItem
	 * @generated
	 */
	EClass getHandledToolItem();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MKeyBinding <em>Key Binding</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Key Binding</em>'.
	 * @see org.eclipse.e4.ui.model.application.MKeyBinding
	 * @generated
	 */
	EClass getKeyBinding();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MKeyBinding#getCommand <em>Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Command</em>'.
	 * @see org.eclipse.e4.ui.model.application.MKeyBinding#getCommand()
	 * @see #getKeyBinding()
	 * @generated
	 */
	EReference getKeyBinding_Command();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.e4.ui.model.application.MKeyBinding#getParameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Parameters</em>'.
	 * @see org.eclipse.e4.ui.model.application.MKeyBinding#getParameters()
	 * @see #getKeyBinding()
	 * @generated
	 */
	EReference getKeyBinding_Parameters();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MKeySequence <em>Key Sequence</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Key Sequence</em>'.
	 * @see org.eclipse.e4.ui.model.application.MKeySequence
	 * @generated
	 */
	EClass getKeySequence();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MKeySequence#getKeySequence <em>Key Sequence</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key Sequence</em>'.
	 * @see org.eclipse.e4.ui.model.application.MKeySequence#getKeySequence()
	 * @see #getKeySequence()
	 * @generated
	 */
	EAttribute getKeySequence_KeySequence();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MParameter <em>Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Parameter</em>'.
	 * @see org.eclipse.e4.ui.model.application.MParameter
	 * @generated
	 */
	EClass getParameter();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MParameter#getTag <em>Tag</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tag</em>'.
	 * @see org.eclipse.e4.ui.model.application.MParameter#getTag()
	 * @see #getParameter()
	 * @generated
	 */
	EAttribute getParameter_Tag();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MParameter#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.e4.ui.model.application.MParameter#getValue()
	 * @see #getParameter()
	 * @generated
	 */
	EAttribute getParameter_Value();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV______________Trim_______________V <em>VTrim V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VTrim V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV______________Trim_______________V
	 * @generated
	 */
	EClass getV______________Trim_______________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MTrimContainer <em>Trim Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Trim Container</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimContainer
	 * @generated
	 */
	EClass getTrimContainer();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.e4.ui.model.application.MTrimContainer#getSide <em>Side</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Side</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTrimContainer#getSide()
	 * @see #getTrimContainer()
	 * @generated
	 */
	EAttribute getTrimContainer_Side();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MWindowTrim <em>Window Trim</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Window Trim</em>'.
	 * @see org.eclipse.e4.ui.model.application.MWindowTrim
	 * @generated
	 */
	EClass getWindowTrim();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV______________SharedElements_______________V <em>VShared Elements V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VShared Elements V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV______________SharedElements_______________V
	 * @generated
	 */
	EClass getV______________SharedElements_______________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPlaceholder <em>Placeholder</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Placeholder</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPlaceholder
	 * @generated
	 */
	EClass getPlaceholder();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.e4.ui.model.application.MPlaceholder#getRef <em>Ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Ref</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPlaceholder#getRef()
	 * @see #getPlaceholder()
	 * @generated
	 */
	EReference getPlaceholder_Ref();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPerspective <em>Perspective</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Perspective</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPerspective
	 * @generated
	 */
	EClass getPerspective();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MPerspectiveStack <em>Perspective Stack</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Perspective Stack</em>'.
	 * @see org.eclipse.e4.ui.model.application.MPerspectiveStack
	 * @generated
	 */
	EClass getPerspectiveStack();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MV_________Testing__________V <em>VTesting V</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>VTesting V</em>'.
	 * @see org.eclipse.e4.ui.model.application.MV_________Testing__________V
	 * @generated
	 */
	EClass getV_________Testing__________V();

	/**
	 * Returns the meta object for class '{@link org.eclipse.e4.ui.model.application.MTestHarness <em>Test Harness</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Test Harness</em>'.
	 * @see org.eclipse.e4.ui.model.application.MTestHarness
	 * @generated
	 */
	EClass getTestHarness();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>String To String Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>String To String Map</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="org.eclipse.emf.ecore.EString"
	 *        valueDataType="org.eclipse.emf.ecore.EString"
	 * @generated
	 */
	EClass getStringToStringMap();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToStringMap()
	 * @generated
	 */
	EAttribute getStringToStringMap_Key();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToStringMap()
	 * @generated
	 */
	EAttribute getStringToStringMap_Value();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.e4.ui.model.application.ItemType <em>Item Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Item Type</em>'.
	 * @see org.eclipse.e4.ui.model.application.ItemType
	 * @generated
	 */
	EEnum getItemType();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.e4.ui.model.application.SideValue <em>Side Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Side Value</em>'.
	 * @see org.eclipse.e4.ui.model.application.SideValue
	 * @generated
	 */
	EEnum getSideValue();

	/**
	 * Returns the meta object for data type '{@link org.eclipse.e4.core.services.context.IEclipseContext <em>IEclipse Context</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>IEclipse Context</em>'.
	 * @see org.eclipse.e4.core.services.context.IEclipseContext
	 * @model instanceClass="org.eclipse.e4.core.services.context.IEclipseContext" serializeable="false"
	 * @generated
	 */
	EDataType getIEclipseContext();

	/**
	 * Returns the meta object for data type '{@link org.eclipse.core.commands.ParameterizedCommand <em>Parameterized Command</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Parameterized Command</em>'.
	 * @see org.eclipse.core.commands.ParameterizedCommand
	 * @model instanceClass="org.eclipse.core.commands.ParameterizedCommand" serializeable="false"
	 * @generated
	 */
	EDataType getParameterizedCommand();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	MApplicationFactory getApplicationFactory();

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
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV____________ConstantsAndTypes_____________V <em>VConstants And Types V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV____________ConstantsAndTypes_____________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV____________ConstantsAndTypes_____________V()
		 * @generated
		 */
		EClass VCONSTANTS_AND_TYPES_V = eINSTANCE.getV____________ConstantsAndTypes_____________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV____________Abstract_____________V <em>VAbstract V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV____________Abstract_____________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV____________Abstract_____________V()
		 * @generated
		 */
		EClass VABSTRACT_V = eINSTANCE.getV____________Abstract_____________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl <em>Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getApplicationElement()
		 * @generated
		 */
		EClass APPLICATION_ELEMENT = eINSTANCE.getApplicationElement();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute APPLICATION_ELEMENT__ID = eINSTANCE.getApplicationElement_Id();

		/**
		 * The meta object literal for the '<em><b>Tags</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute APPLICATION_ELEMENT__TAGS = eINSTANCE.getApplicationElement_Tags();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ContributionImpl <em>Contribution</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ContributionImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getContribution()
		 * @generated
		 */
		EClass CONTRIBUTION = eINSTANCE.getContribution();

		/**
		 * The meta object literal for the '<em><b>URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONTRIBUTION__URI = eINSTANCE.getContribution_URI();

		/**
		 * The meta object literal for the '<em><b>Object</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONTRIBUTION__OBJECT = eINSTANCE.getContribution_Object();

		/**
		 * The meta object literal for the '<em><b>Persisted State</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CONTRIBUTION__PERSISTED_STATE = eINSTANCE.getContribution_PersistedState();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.DirtyableImpl <em>Dirtyable</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.DirtyableImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getDirtyable()
		 * @generated
		 */
		EClass DIRTYABLE = eINSTANCE.getDirtyable();

		/**
		 * The meta object literal for the '<em><b>Dirty</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DIRTYABLE__DIRTY = eINSTANCE.getDirtyable_Dirty();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.InputImpl <em>Input</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.InputImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getInput()
		 * @generated
		 */
		EClass INPUT = eINSTANCE.getInput();

		/**
		 * The meta object literal for the '<em><b>Input URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INPUT__INPUT_URI = eINSTANCE.getInput_InputURI();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.UIElementImpl <em>UI Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.UIElementImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getUIElement()
		 * @generated
		 */
		EClass UI_ELEMENT = eINSTANCE.getUIElement();

		/**
		 * The meta object literal for the '<em><b>Widget</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_ELEMENT__WIDGET = eINSTANCE.getUIElement_Widget();

		/**
		 * The meta object literal for the '<em><b>Renderer</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_ELEMENT__RENDERER = eINSTANCE.getUIElement_Renderer();

		/**
		 * The meta object literal for the '<em><b>To Be Rendered</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_ELEMENT__TO_BE_RENDERED = eINSTANCE.getUIElement_ToBeRendered();

		/**
		 * The meta object literal for the '<em><b>On Top</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_ELEMENT__ON_TOP = eINSTANCE.getUIElement_OnTop();

		/**
		 * The meta object literal for the '<em><b>Visible</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_ELEMENT__VISIBLE = eINSTANCE.getUIElement_Visible();

		/**
		 * The meta object literal for the '<em><b>Parent</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference UI_ELEMENT__PARENT = eINSTANCE.getUIElement_Parent();

		/**
		 * The meta object literal for the '<em><b>Container Data</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_ELEMENT__CONTAINER_DATA = eINSTANCE.getUIElement_ContainerData();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.UILabelImpl <em>UI Label</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.UILabelImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getUILabel()
		 * @generated
		 */
		EClass UI_LABEL = eINSTANCE.getUILabel();

		/**
		 * The meta object literal for the '<em><b>Label</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_LABEL__LABEL = eINSTANCE.getUILabel_Label();

		/**
		 * The meta object literal for the '<em><b>Icon URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_LABEL__ICON_URI = eINSTANCE.getUILabel_IconURI();

		/**
		 * The meta object literal for the '<em><b>Tooltip</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UI_LABEL__TOOLTIP = eINSTANCE.getUILabel_Tooltip();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ContextImpl <em>Context</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ContextImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getContext()
		 * @generated
		 */
		EClass CONTEXT = eINSTANCE.getContext();

		/**
		 * The meta object literal for the '<em><b>Context</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONTEXT__CONTEXT = eINSTANCE.getContext_Context();

		/**
		 * The meta object literal for the '<em><b>Variables</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONTEXT__VARIABLES = eINSTANCE.getContext_Variables();

		/**
		 * The meta object literal for the '<em><b>Properties</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CONTEXT__PROPERTIES = eINSTANCE.getContext_Properties();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V <em>VAbstract Containers V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV_________AbstractContainers__________V()
		 * @generated
		 */
		EClass VABSTRACT_CONTAINERS_V = eINSTANCE.getV_________AbstractContainers__________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ElementContainerImpl <em>Element Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ElementContainerImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getElementContainer()
		 * @generated
		 */
		EClass ELEMENT_CONTAINER = eINSTANCE.getElementContainer();

		/**
		 * The meta object literal for the '<em><b>Children</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ELEMENT_CONTAINER__CHILDREN = eINSTANCE.getElementContainer_Children();

		/**
		 * The meta object literal for the '<em><b>Selected Element</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ELEMENT_CONTAINER__SELECTED_ELEMENT = eINSTANCE.getElementContainer_SelectedElement();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.GenericStackImpl <em>Generic Stack</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.GenericStackImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getGenericStack()
		 * @generated
		 */
		EClass GENERIC_STACK = eINSTANCE.getGenericStack();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.GenericTileImpl <em>Generic Tile</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.GenericTileImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getGenericTile()
		 * @generated
		 */
		EClass GENERIC_TILE = eINSTANCE.getGenericTile();

		/**
		 * The meta object literal for the '<em><b>Horizontal</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERIC_TILE__HORIZONTAL = eINSTANCE.getGenericTile_Horizontal();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV______________MenusAndTBs_______________V <em>VMenus And TBs V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV______________MenusAndTBs_______________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________MenusAndTBs_______________V()
		 * @generated
		 */
		EClass VMENUS_AND_TBS_V = eINSTANCE.getV______________MenusAndTBs_______________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ItemImpl <em>Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getItem()
		 * @generated
		 */
		EClass ITEM = eINSTANCE.getItem();

		/**
		 * The meta object literal for the '<em><b>Enabled</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITEM__ENABLED = eINSTANCE.getItem_Enabled();

		/**
		 * The meta object literal for the '<em><b>Selected</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITEM__SELECTED = eINSTANCE.getItem_Selected();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ITEM__TYPE = eINSTANCE.getItem_Type();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.MenuItemImpl <em>Menu Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.MenuItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getMenuItem()
		 * @generated
		 */
		EClass MENU_ITEM = eINSTANCE.getMenuItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.DirectMenuItemImpl <em>Direct Menu Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.DirectMenuItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getDirectMenuItem()
		 * @generated
		 */
		EClass DIRECT_MENU_ITEM = eINSTANCE.getDirectMenuItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.MenuImpl <em>Menu</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.MenuImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getMenu()
		 * @generated
		 */
		EClass MENU = eINSTANCE.getMenu();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ToolItemImpl <em>Tool Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ToolItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getToolItem()
		 * @generated
		 */
		EClass TOOL_ITEM = eINSTANCE.getToolItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl <em>Direct Tool Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.DirectToolItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getDirectToolItem()
		 * @generated
		 */
		EClass DIRECT_TOOL_ITEM = eINSTANCE.getDirectToolItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ToolBarImpl <em>Tool Bar</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ToolBarImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getToolBar()
		 * @generated
		 */
		EClass TOOL_BAR = eINSTANCE.getToolBar();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV______________RCP_______________V <em>VRCP V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV______________RCP_______________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________RCP_______________V()
		 * @generated
		 */
		EClass VRCP_V = eINSTANCE.getV______________RCP_______________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl <em>Application</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getApplication()
		 * @generated
		 */
		EClass APPLICATION = eINSTANCE.getApplication();

		/**
		 * The meta object literal for the '<em><b>Commands</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference APPLICATION__COMMANDS = eINSTANCE.getApplication_Commands();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MPSCElement <em>PSC Element</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MPSCElement
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPSCElement()
		 * @generated
		 */
		EClass PSC_ELEMENT = eINSTANCE.getPSCElement();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PartImpl <em>Part</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PartImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPart()
		 * @generated
		 */
		EClass PART = eINSTANCE.getPart();

		/**
		 * The meta object literal for the '<em><b>Menus</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PART__MENUS = eINSTANCE.getPart_Menus();

		/**
		 * The meta object literal for the '<em><b>Toolbar</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PART__TOOLBAR = eINSTANCE.getPart_Toolbar();

		/**
		 * The meta object literal for the '<em><b>Closeable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PART__CLOSEABLE = eINSTANCE.getPart_Closeable();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.InputPartImpl <em>Input Part</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.InputPartImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getInputPart()
		 * @generated
		 */
		EClass INPUT_PART = eINSTANCE.getInputPart();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PartDescriptorImpl <em>Part Descriptor</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PartDescriptorImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPartDescriptor()
		 * @generated
		 */
		EClass PART_DESCRIPTOR = eINSTANCE.getPartDescriptor();

		/**
		 * The meta object literal for the '<em><b>Allow Multiple</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PART_DESCRIPTOR__ALLOW_MULTIPLE = eINSTANCE.getPartDescriptor_AllowMultiple();

		/**
		 * The meta object literal for the '<em><b>Category</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PART_DESCRIPTOR__CATEGORY = eINSTANCE.getPartDescriptor_Category();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PartDescriptorContainerImpl <em>Part Descriptor Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PartDescriptorContainerImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPartDescriptorContainer()
		 * @generated
		 */
		EClass PART_DESCRIPTOR_CONTAINER = eINSTANCE.getPartDescriptorContainer();

		/**
		 * The meta object literal for the '<em><b>Descriptors</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PART_DESCRIPTOR_CONTAINER__DESCRIPTORS = eINSTANCE.getPartDescriptorContainer_Descriptors();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PartStackImpl <em>Part Stack</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PartStackImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPartStack()
		 * @generated
		 */
		EClass PART_STACK = eINSTANCE.getPartStack();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PartSashContainerImpl <em>Part Sash Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PartSashContainerImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPartSashContainer()
		 * @generated
		 */
		EClass PART_SASH_CONTAINER = eINSTANCE.getPartSashContainer();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.WindowImpl <em>Window</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.WindowImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getWindow()
		 * @generated
		 */
		EClass WINDOW = eINSTANCE.getWindow();

		/**
		 * The meta object literal for the '<em><b>Main Menu</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference WINDOW__MAIN_MENU = eINSTANCE.getWindow_MainMenu();

		/**
		 * The meta object literal for the '<em><b>X</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WINDOW__X = eINSTANCE.getWindow_X();

		/**
		 * The meta object literal for the '<em><b>Y</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WINDOW__Y = eINSTANCE.getWindow_Y();

		/**
		 * The meta object literal for the '<em><b>Width</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WINDOW__WIDTH = eINSTANCE.getWindow_Width();

		/**
		 * The meta object literal for the '<em><b>Height</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WINDOW__HEIGHT = eINSTANCE.getWindow_Height();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ModelComponentsImpl <em>Model Components</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ModelComponentsImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getModelComponents()
		 * @generated
		 */
		EClass MODEL_COMPONENTS = eINSTANCE.getModelComponents();

		/**
		 * The meta object literal for the '<em><b>Components</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_COMPONENTS__COMPONENTS = eINSTANCE.getModelComponents_Components();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ModelComponentImpl <em>Model Component</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ModelComponentImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getModelComponent()
		 * @generated
		 */
		EClass MODEL_COMPONENT = eINSTANCE.getModelComponent();

		/**
		 * The meta object literal for the '<em><b>Position In Parent</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_COMPONENT__POSITION_IN_PARENT = eINSTANCE.getModelComponent_PositionInParent();

		/**
		 * The meta object literal for the '<em><b>Parent ID</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_COMPONENT__PARENT_ID = eINSTANCE.getModelComponent_ParentID();

		/**
		 * The meta object literal for the '<em><b>Children</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_COMPONENT__CHILDREN = eINSTANCE.getModelComponent_Children();

		/**
		 * The meta object literal for the '<em><b>Commands</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_COMPONENT__COMMANDS = eINSTANCE.getModelComponent_Commands();

		/**
		 * The meta object literal for the '<em><b>Processor</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MODEL_COMPONENT__PROCESSOR = eINSTANCE.getModelComponent_Processor();

		/**
		 * The meta object literal for the '<em><b>Bindings</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference MODEL_COMPONENT__BINDINGS = eINSTANCE.getModelComponent_Bindings();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV______________Commands_______________V <em>VCommands V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV______________Commands_______________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________Commands_______________V()
		 * @generated
		 */
		EClass VCOMMANDS_V = eINSTANCE.getV______________Commands_______________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MBindingContainer <em>Binding Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MBindingContainer
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getBindingContainer()
		 * @generated
		 */
		EClass BINDING_CONTAINER = eINSTANCE.getBindingContainer();

		/**
		 * The meta object literal for the '<em><b>Binding Tables</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BINDING_CONTAINER__BINDING_TABLES = eINSTANCE.getBindingContainer_BindingTables();

		/**
		 * The meta object literal for the '<em><b>Root Context</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BINDING_CONTAINER__ROOT_CONTEXT = eINSTANCE.getBindingContainer_RootContext();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MBindings <em>Bindings</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MBindings
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getBindings()
		 * @generated
		 */
		EClass BINDINGS = eINSTANCE.getBindings();

		/**
		 * The meta object literal for the '<em><b>Binding Contexts</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BINDINGS__BINDING_CONTEXTS = eINSTANCE.getBindings_BindingContexts();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.BindingContextImpl <em>Binding Context</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.BindingContextImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getBindingContext()
		 * @generated
		 */
		EClass BINDING_CONTEXT = eINSTANCE.getBindingContext();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BINDING_CONTEXT__NAME = eINSTANCE.getBindingContext_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BINDING_CONTEXT__DESCRIPTION = eINSTANCE.getBindingContext_Description();

		/**
		 * The meta object literal for the '<em><b>Children</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BINDING_CONTEXT__CHILDREN = eINSTANCE.getBindingContext_Children();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.BindingTableImpl <em>Binding Table</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.BindingTableImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getBindingTable()
		 * @generated
		 */
		EClass BINDING_TABLE = eINSTANCE.getBindingTable();

		/**
		 * The meta object literal for the '<em><b>Binding Context Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BINDING_TABLE__BINDING_CONTEXT_ID = eINSTANCE.getBindingTable_BindingContextId();

		/**
		 * The meta object literal for the '<em><b>Bindings</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BINDING_TABLE__BINDINGS = eINSTANCE.getBindingTable_Bindings();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.CommandImpl <em>Command</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.CommandImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getCommand()
		 * @generated
		 */
		EClass COMMAND = eINSTANCE.getCommand();

		/**
		 * The meta object literal for the '<em><b>Command Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMAND__COMMAND_NAME = eINSTANCE.getCommand_CommandName();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMAND__DESCRIPTION = eINSTANCE.getCommand_Description();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference COMMAND__PARAMETERS = eINSTANCE.getCommand_Parameters();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.CommandParameterImpl <em>Command Parameter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.CommandParameterImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getCommandParameter()
		 * @generated
		 */
		EClass COMMAND_PARAMETER = eINSTANCE.getCommandParameter();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMAND_PARAMETER__NAME = eINSTANCE.getCommandParameter_Name();

		/**
		 * The meta object literal for the '<em><b>Type Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMAND_PARAMETER__TYPE_ID = eINSTANCE.getCommandParameter_TypeId();

		/**
		 * The meta object literal for the '<em><b>Optional</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMAND_PARAMETER__OPTIONAL = eINSTANCE.getCommandParameter_Optional();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.HandlerImpl <em>Handler</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.HandlerImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getHandler()
		 * @generated
		 */
		EClass HANDLER = eINSTANCE.getHandler();

		/**
		 * The meta object literal for the '<em><b>Command</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HANDLER__COMMAND = eINSTANCE.getHandler_Command();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MHandlerContainer <em>Handler Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MHandlerContainer
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getHandlerContainer()
		 * @generated
		 */
		EClass HANDLER_CONTAINER = eINSTANCE.getHandlerContainer();

		/**
		 * The meta object literal for the '<em><b>Handlers</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HANDLER_CONTAINER__HANDLERS = eINSTANCE.getHandlerContainer_Handlers();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.HandledItemImpl <em>Handled Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.HandledItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getHandledItem()
		 * @generated
		 */
		EClass HANDLED_ITEM = eINSTANCE.getHandledItem();

		/**
		 * The meta object literal for the '<em><b>Command</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HANDLED_ITEM__COMMAND = eINSTANCE.getHandledItem_Command();

		/**
		 * The meta object literal for the '<em><b>Wb Command</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HANDLED_ITEM__WB_COMMAND = eINSTANCE.getHandledItem_WbCommand();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HANDLED_ITEM__PARAMETERS = eINSTANCE.getHandledItem_Parameters();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.HandledMenuItemImpl <em>Handled Menu Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.HandledMenuItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getHandledMenuItem()
		 * @generated
		 */
		EClass HANDLED_MENU_ITEM = eINSTANCE.getHandledMenuItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl <em>Handled Tool Item</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getHandledToolItem()
		 * @generated
		 */
		EClass HANDLED_TOOL_ITEM = eINSTANCE.getHandledToolItem();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.KeyBindingImpl <em>Key Binding</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.KeyBindingImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getKeyBinding()
		 * @generated
		 */
		EClass KEY_BINDING = eINSTANCE.getKeyBinding();

		/**
		 * The meta object literal for the '<em><b>Command</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference KEY_BINDING__COMMAND = eINSTANCE.getKeyBinding_Command();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference KEY_BINDING__PARAMETERS = eINSTANCE.getKeyBinding_Parameters();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MKeySequence <em>Key Sequence</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MKeySequence
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getKeySequence()
		 * @generated
		 */
		EClass KEY_SEQUENCE = eINSTANCE.getKeySequence();

		/**
		 * The meta object literal for the '<em><b>Key Sequence</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute KEY_SEQUENCE__KEY_SEQUENCE = eINSTANCE.getKeySequence_KeySequence();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.ParameterImpl <em>Parameter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.ParameterImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getParameter()
		 * @generated
		 */
		EClass PARAMETER = eINSTANCE.getParameter();

		/**
		 * The meta object literal for the '<em><b>Tag</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER__TAG = eINSTANCE.getParameter_Tag();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PARAMETER__VALUE = eINSTANCE.getParameter_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV______________Trim_______________V <em>VTrim V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV______________Trim_______________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________Trim_______________V()
		 * @generated
		 */
		EClass VTRIM_V = eINSTANCE.getV______________Trim_______________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.TrimContainerImpl <em>Trim Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.TrimContainerImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getTrimContainer()
		 * @generated
		 */
		EClass TRIM_CONTAINER = eINSTANCE.getTrimContainer();

		/**
		 * The meta object literal for the '<em><b>Side</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TRIM_CONTAINER__SIDE = eINSTANCE.getTrimContainer_Side();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.WindowTrimImpl <em>Window Trim</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.WindowTrimImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getWindowTrim()
		 * @generated
		 */
		EClass WINDOW_TRIM = eINSTANCE.getWindowTrim();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV______________SharedElements_______________V <em>VShared Elements V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV______________SharedElements_______________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV______________SharedElements_______________V()
		 * @generated
		 */
		EClass VSHARED_ELEMENTS_V = eINSTANCE.getV______________SharedElements_______________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PlaceholderImpl <em>Placeholder</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PlaceholderImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPlaceholder()
		 * @generated
		 */
		EClass PLACEHOLDER = eINSTANCE.getPlaceholder();

		/**
		 * The meta object literal for the '<em><b>Ref</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PLACEHOLDER__REF = eINSTANCE.getPlaceholder_Ref();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PerspectiveImpl <em>Perspective</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PerspectiveImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPerspective()
		 * @generated
		 */
		EClass PERSPECTIVE = eINSTANCE.getPerspective();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.PerspectiveStackImpl <em>Perspective Stack</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.PerspectiveStackImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getPerspectiveStack()
		 * @generated
		 */
		EClass PERSPECTIVE_STACK = eINSTANCE.getPerspectiveStack();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.MV_________Testing__________V <em>VTesting V</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.MV_________Testing__________V
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getV_________Testing__________V()
		 * @generated
		 */
		EClass VTESTING_V = eINSTANCE.getV_________Testing__________V();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.TestHarnessImpl <em>Test Harness</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.TestHarnessImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getTestHarness()
		 * @generated
		 */
		EClass TEST_HARNESS = eINSTANCE.getTestHarness();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.impl.StringToStringMapImpl <em>String To String Map</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.impl.StringToStringMapImpl
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getStringToStringMap()
		 * @generated
		 */
		EClass STRING_TO_STRING_MAP = eINSTANCE.getStringToStringMap();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STRING_TO_STRING_MAP__KEY = eINSTANCE.getStringToStringMap_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STRING_TO_STRING_MAP__VALUE = eINSTANCE.getStringToStringMap_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.ItemType <em>Item Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.ItemType
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getItemType()
		 * @generated
		 */
		EEnum ITEM_TYPE = eINSTANCE.getItemType();

		/**
		 * The meta object literal for the '{@link org.eclipse.e4.ui.model.application.SideValue <em>Side Value</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.ui.model.application.SideValue
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getSideValue()
		 * @generated
		 */
		EEnum SIDE_VALUE = eINSTANCE.getSideValue();

		/**
		 * The meta object literal for the '<em>IEclipse Context</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.e4.core.services.context.IEclipseContext
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getIEclipseContext()
		 * @generated
		 */
		EDataType IECLIPSE_CONTEXT = eINSTANCE.getIEclipseContext();

		/**
		 * The meta object literal for the '<em>Parameterized Command</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.core.commands.ParameterizedCommand
		 * @see org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl#getParameterizedCommand()
		 * @generated
		 */
		EDataType PARAMETERIZED_COMMAND = eINSTANCE.getParameterizedCommand();

	}

} //MApplicationPackage
