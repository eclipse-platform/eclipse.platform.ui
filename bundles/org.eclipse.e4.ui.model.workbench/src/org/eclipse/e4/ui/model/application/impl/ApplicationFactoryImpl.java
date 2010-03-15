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
package org.eclipse.e4.ui.model.application.impl;

import java.util.Map;
import org.eclipse.e4.ui.model.application.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ApplicationFactoryImpl extends EFactoryImpl implements MApplicationFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static MApplicationFactory init() {
		try {
			MApplicationFactory theApplicationFactory = (MApplicationFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.eclipse.org/ui/2008/UIModel"); //$NON-NLS-1$ 
			if (theApplicationFactory != null) {
				return theApplicationFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ApplicationFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ApplicationFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case MApplicationPackage.STRING_TO_STRING_MAP: return (EObject)createStringToStringMap();
			case MApplicationPackage.ITEM: return (EObject)createItem();
			case MApplicationPackage.MENU_ITEM: return (EObject)createMenuItem();
			case MApplicationPackage.DIRECT_MENU_ITEM: return (EObject)createDirectMenuItem();
			case MApplicationPackage.MENU: return (EObject)createMenu();
			case MApplicationPackage.TOOL_ITEM: return (EObject)createToolItem();
			case MApplicationPackage.DIRECT_TOOL_ITEM: return (EObject)createDirectToolItem();
			case MApplicationPackage.TOOL_BAR: return (EObject)createToolBar();
			case MApplicationPackage.APPLICATION: return (EObject)createApplication();
			case MApplicationPackage.PART: return (EObject)createPart();
			case MApplicationPackage.INPUT_PART: return (EObject)createInputPart();
			case MApplicationPackage.PART_DESCRIPTOR: return (EObject)createPartDescriptor();
			case MApplicationPackage.PART_DESCRIPTOR_CONTAINER: return (EObject)createPartDescriptorContainer();
			case MApplicationPackage.PART_STACK: return (EObject)createPartStack();
			case MApplicationPackage.PART_SASH_CONTAINER: return (EObject)createPartSashContainer();
			case MApplicationPackage.WINDOW: return (EObject)createWindow();
			case MApplicationPackage.MODEL_COMPONENTS: return (EObject)createModelComponents();
			case MApplicationPackage.MODEL_COMPONENT: return (EObject)createModelComponent();
			case MApplicationPackage.BINDING_CONTEXT: return (EObject)createBindingContext();
			case MApplicationPackage.BINDING_TABLE: return (EObject)createBindingTable();
			case MApplicationPackage.COMMAND: return (EObject)createCommand();
			case MApplicationPackage.COMMAND_PARAMETER: return (EObject)createCommandParameter();
			case MApplicationPackage.HANDLER: return (EObject)createHandler();
			case MApplicationPackage.HANDLED_ITEM: return (EObject)createHandledItem();
			case MApplicationPackage.HANDLED_MENU_ITEM: return (EObject)createHandledMenuItem();
			case MApplicationPackage.HANDLED_TOOL_ITEM: return (EObject)createHandledToolItem();
			case MApplicationPackage.KEY_BINDING: return (EObject)createKeyBinding();
			case MApplicationPackage.PARAMETER: return (EObject)createParameter();
			case MApplicationPackage.WINDOW_TRIM: return (EObject)createWindowTrim();
			case MApplicationPackage.PLACEHOLDER: return (EObject)createPlaceholder();
			case MApplicationPackage.PERSPECTIVE: return (EObject)createPerspective();
			case MApplicationPackage.PERSPECTIVE_STACK: return (EObject)createPerspectiveStack();
			case MApplicationPackage.TEST_HARNESS: return (EObject)createTestHarness();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case MApplicationPackage.ITEM_TYPE:
				return createItemTypeFromString(eDataType, initialValue);
			case MApplicationPackage.SIDE_VALUE:
				return createSideValueFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case MApplicationPackage.ITEM_TYPE:
				return convertItemTypeToString(eDataType, instanceValue);
			case MApplicationPackage.SIDE_VALUE:
				return convertSideValueToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MItem createItem() {
		ItemImpl item = new ItemImpl();
		return item;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMenuItem createMenuItem() {
		MenuItemImpl menuItem = new MenuItemImpl();
		return menuItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDirectMenuItem createDirectMenuItem() {
		DirectMenuItemImpl directMenuItem = new DirectMenuItemImpl();
		return directMenuItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMenu createMenu() {
		MenuImpl menu = new MenuImpl();
		return menu;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolItem createToolItem() {
		ToolItemImpl toolItem = new ToolItemImpl();
		return toolItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDirectToolItem createDirectToolItem() {
		DirectToolItemImpl directToolItem = new DirectToolItemImpl();
		return directToolItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBar createToolBar() {
		ToolBarImpl toolBar = new ToolBarImpl();
		return toolBar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MApplication createApplication() {
		ApplicationImpl application = new ApplicationImpl();
		return application;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPart createPart() {
		PartImpl part = new PartImpl();
		return part;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MInputPart createInputPart() {
		InputPartImpl inputPart = new InputPartImpl();
		return inputPart;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPartDescriptor createPartDescriptor() {
		PartDescriptorImpl partDescriptor = new PartDescriptorImpl();
		return partDescriptor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPartDescriptorContainer createPartDescriptorContainer() {
		PartDescriptorContainerImpl partDescriptorContainer = new PartDescriptorContainerImpl();
		return partDescriptorContainer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPartStack createPartStack() {
		PartStackImpl partStack = new PartStackImpl();
		return partStack;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPartSashContainer createPartSashContainer() {
		PartSashContainerImpl partSashContainer = new PartSashContainerImpl();
		return partSashContainer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MWindow createWindow() {
		WindowImpl window = new WindowImpl();
		return window;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MModelComponents createModelComponents() {
		ModelComponentsImpl modelComponents = new ModelComponentsImpl();
		return modelComponents;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MModelComponent createModelComponent() {
		ModelComponentImpl modelComponent = new ModelComponentImpl();
		return modelComponent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MBindingContext createBindingContext() {
		BindingContextImpl bindingContext = new BindingContextImpl();
		return bindingContext;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MBindingTable createBindingTable() {
		BindingTableImpl bindingTable = new BindingTableImpl();
		return bindingTable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MCommand createCommand() {
		CommandImpl command = new CommandImpl();
		return command;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MCommandParameter createCommandParameter() {
		CommandParameterImpl commandParameter = new CommandParameterImpl();
		return commandParameter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MHandler createHandler() {
		HandlerImpl handler = new HandlerImpl();
		return handler;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MHandledItem createHandledItem() {
		HandledItemImpl handledItem = new HandledItemImpl();
		return handledItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MHandledMenuItem createHandledMenuItem() {
		HandledMenuItemImpl handledMenuItem = new HandledMenuItemImpl();
		return handledMenuItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MHandledToolItem createHandledToolItem() {
		HandledToolItemImpl handledToolItem = new HandledToolItemImpl();
		return handledToolItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MKeyBinding createKeyBinding() {
		KeyBindingImpl keyBinding = new KeyBindingImpl();
		return keyBinding;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MParameter createParameter() {
		ParameterImpl parameter = new ParameterImpl();
		return parameter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MWindowTrim createWindowTrim() {
		WindowTrimImpl windowTrim = new WindowTrimImpl();
		return windowTrim;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPlaceholder createPlaceholder() {
		PlaceholderImpl placeholder = new PlaceholderImpl();
		return placeholder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPerspective createPerspective() {
		PerspectiveImpl perspective = new PerspectiveImpl();
		return perspective;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPerspectiveStack createPerspectiveStack() {
		PerspectiveStackImpl perspectiveStack = new PerspectiveStackImpl();
		return perspectiveStack;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MTestHarness createTestHarness() {
		TestHarnessImpl testHarness = new TestHarnessImpl();
		return testHarness;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Map.Entry<String, String> createStringToStringMap() {
		StringToStringMapImpl stringToStringMap = new StringToStringMapImpl();
		return stringToStringMap;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ItemType createItemTypeFromString(EDataType eDataType, String initialValue) {
		ItemType result = ItemType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertItemTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SideValue createSideValueFromString(EDataType eDataType, String initialValue) {
		SideValue result = SideValue.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertSideValueToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MApplicationPackage getApplicationPackage() {
		return (MApplicationPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static MApplicationPackage getPackage() {
		return MApplicationPackage.eINSTANCE;
	}

} //ApplicationFactoryImpl
