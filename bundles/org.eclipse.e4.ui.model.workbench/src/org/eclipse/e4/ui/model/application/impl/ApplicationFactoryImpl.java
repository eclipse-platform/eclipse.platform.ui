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
			case MApplicationPackage.APPLICATION_ELEMENT: return (EObject)createApplicationElement();
			case MApplicationPackage.COMMAND: return (EObject)createCommand();
			case MApplicationPackage.HANDLER: return (EObject)createHandler();
			case MApplicationPackage.INPUT: return (EObject)createInput();
			case MApplicationPackage.PARAMETER: return (EObject)createParameter();
			case MApplicationPackage.CONTEXT: return (EObject)createContext();
			case MApplicationPackage.TEST_HARNESS: return (EObject)createTestHarness();
			case MApplicationPackage.TRIM_STRUCTURE: return (EObject)createTrimStructure();
			case MApplicationPackage.APPLICATION: return (EObject)createApplication();
			case MApplicationPackage.ITEM: return (EObject)createItem();
			case MApplicationPackage.HANDLED_ITEM: return (EObject)createHandledItem();
			case MApplicationPackage.MENU_ITEM: return (EObject)createMenuItem();
			case MApplicationPackage.MENU: return (EObject)createMenu();
			case MApplicationPackage.TOOL_ITEM: return (EObject)createToolItem();
			case MApplicationPackage.TOOL_BAR: return (EObject)createToolBar();
			case MApplicationPackage.PART: return (EObject)createPart();
			case MApplicationPackage.PART_STACK: return (EObject)createPartStack();
			case MApplicationPackage.PART_SASH_CONTAINER: return (EObject)createPartSashContainer();
			case MApplicationPackage.WINDOW: return (EObject)createWindow();
			case MApplicationPackage.VIEW: return (EObject)createView();
			case MApplicationPackage.VIEW_STACK: return (EObject)createViewStack();
			case MApplicationPackage.VIEW_SASH_CONTAINER: return (EObject)createViewSashContainer();
			case MApplicationPackage.EDITOR: return (EObject)createEditor();
			case MApplicationPackage.MULTI_EDITOR: return (EObject)createMultiEditor();
			case MApplicationPackage.EDITOR_STACK: return (EObject)createEditorStack();
			case MApplicationPackage.EDITOR_SASH_CONTAINER: return (EObject)createEditorSashContainer();
			case MApplicationPackage.PERSPECTIVE: return (EObject)createPerspective();
			case MApplicationPackage.PERSPECTIVE_STACK: return (EObject)createPerspectiveStack();
			case MApplicationPackage.IDE_WINDOW: return (EObject)createIDEWindow();
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
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MApplicationElement createApplicationElement() {
		ApplicationElementImpl applicationElement = new ApplicationElementImpl();
		return applicationElement;
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
	public MHandler createHandler() {
		HandlerImpl handler = new HandlerImpl();
		return handler;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MInput createInput() {
		InputImpl input = new InputImpl();
		return input;
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
	public MContext createContext() {
		ContextImpl context = new ContextImpl();
		return context;
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
	public <T extends MUIElement> MTrimStructure<T> createTrimStructure() {
		TrimStructureImpl<T> trimStructure = new TrimStructureImpl<T>();
		return trimStructure;
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
	public MItem createItem() {
		ItemImpl item = new ItemImpl();
		return item;
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
	public MMenuItem createMenuItem() {
		MenuItemImpl menuItem = new MenuItemImpl();
		return menuItem;
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
	public MToolBar createToolBar() {
		ToolBarImpl toolBar = new ToolBarImpl();
		return toolBar;
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
	public MView createView() {
		ViewImpl view = new ViewImpl();
		return view;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MViewStack createViewStack() {
		ViewStackImpl viewStack = new ViewStackImpl();
		return viewStack;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MViewSashContainer createViewSashContainer() {
		ViewSashContainerImpl viewSashContainer = new ViewSashContainerImpl();
		return viewSashContainer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MEditor createEditor() {
		EditorImpl editor = new EditorImpl();
		return editor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMultiEditor createMultiEditor() {
		MultiEditorImpl multiEditor = new MultiEditorImpl();
		return multiEditor;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MEditorStack createEditorStack() {
		EditorStackImpl editorStack = new EditorStackImpl();
		return editorStack;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MEditorSashContainer createEditorSashContainer() {
		EditorSashContainerImpl editorSashContainer = new EditorSashContainerImpl();
		return editorSashContainer;
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
	public MIDEWindow createIDEWindow() {
		IDEWindowImpl ideWindow = new IDEWindowImpl();
		return ideWindow;
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
