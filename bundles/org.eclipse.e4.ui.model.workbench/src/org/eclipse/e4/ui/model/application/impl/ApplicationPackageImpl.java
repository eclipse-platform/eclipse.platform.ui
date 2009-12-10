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

import org.eclipse.core.commands.ParameterizedCommand;

import org.eclipse.e4.core.services.context.IEclipseContext;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MBindingContainer;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MCommandParameter;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.MDirectToolItem;
import org.eclipse.e4.ui.model.application.MDirtyable;
import org.eclipse.e4.ui.model.application.MESCElement;
import org.eclipse.e4.ui.model.application.MEditor;
import org.eclipse.e4.ui.model.application.MEditorSashContainer;
import org.eclipse.e4.ui.model.application.MEditorStack;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MGenericTile;
import org.eclipse.e4.ui.model.application.MHandledItem;
import org.eclipse.e4.ui.model.application.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.MHandledToolItem;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MHandlerContainer;
import org.eclipse.e4.ui.model.application.MIDEWindow;
import org.eclipse.e4.ui.model.application.MInput;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.e4.ui.model.application.MKeySequence;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MMultiEditor;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MParameter;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MPerspective;
import org.eclipse.e4.ui.model.application.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.MSaveablePart;
import org.eclipse.e4.ui.model.application.MTestHarness;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolItem;
import org.eclipse.e4.ui.model.application.MTrimContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MUILabel;
import org.eclipse.e4.ui.model.application.MVSCElement;
import org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V;
import org.eclipse.e4.ui.model.application.MV_________Testing__________V;
import org.eclipse.e4.ui.model.application.MV____________Abstract_____________V;
import org.eclipse.e4.ui.model.application.MV______________IDE_______________V;
import org.eclipse.e4.ui.model.application.MV______________RCP_______________V;
import org.eclipse.e4.ui.model.application.MView;
import org.eclipse.e4.ui.model.application.MViewSashContainer;
import org.eclipse.e4.ui.model.application.MViewStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.model.application.MWindowTrim;
import org.eclipse.e4.ui.model.application.SideValue;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypeParameter;

import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ApplicationPackageImpl extends EPackageImpl implements MApplicationPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass v____________Abstract_____________VEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass applicationElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass contributionEClass = null;

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
	private EClass dirtyableEClass = null;

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
	private EClass inputEClass = null;

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
	private EClass uiLabelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass uiElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass contextEClass = null;

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
	private EClass v_________Testing__________VEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass testHarnessEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass v_________AbstractContainers__________VEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass elementContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass genericTileEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass trimContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass v______________RCP_______________VEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass applicationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass itemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass handledItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass menuItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass handledMenuItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass directMenuItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass menuEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toolItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass handledToolItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass directToolItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass toolBarEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass pscElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass partEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass saveablePartEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass partStackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass partSashContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass windowEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass windowTrimEClass = null;

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
	private EClass bindingContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass v______________IDE_______________VEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass vscElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass viewEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass viewStackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass editorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass viewSashContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass multiEditorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass escElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass editorStackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass editorSashContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass perspectiveEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass perspectiveStackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass ideWindowEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum sideValueEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType iEclipseContextEDataType = null;

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
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ApplicationPackageImpl() {
		super(eNS_URI, MApplicationFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link MApplicationPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static MApplicationPackage init() {
		if (isInited) return (MApplicationPackage)EPackage.Registry.INSTANCE.getEPackage(MApplicationPackage.eNS_URI);

		// Obtain or create and register package
		ApplicationPackageImpl theApplicationPackage = (ApplicationPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof ApplicationPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new ApplicationPackageImpl());

		isInited = true;

		// Create package meta-data objects
		theApplicationPackage.createPackageContents();

		// Initialize created meta-data
		theApplicationPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theApplicationPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(MApplicationPackage.eNS_URI, theApplicationPackage);
		return theApplicationPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getV____________Abstract_____________V() {
		return v____________Abstract_____________VEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getApplicationElement() {
		return applicationElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getApplicationElement_Id() {
		return (EAttribute)applicationElementEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getContribution() {
		return contributionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getContribution_URI() {
		return (EAttribute)contributionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getContribution_Object() {
		return (EAttribute)contributionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getContribution_PersistedState() {
		return (EAttribute)contributionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getCommand() {
		return commandEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getCommand_CommandName() {
		return (EAttribute)commandEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getCommand_Description() {
		return (EAttribute)commandEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getCommand_Parameters() {
		return (EReference)commandEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getCommandParameter() {
		return commandParameterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getCommandParameter_Name() {
		return (EAttribute)commandParameterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getCommandParameter_TypeId() {
		return (EAttribute)commandParameterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getCommandParameter_Optional() {
		return (EAttribute)commandParameterEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDirtyable() {
		return dirtyableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDirtyable_Dirty() {
		return (EAttribute)dirtyableEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getHandler() {
		return handlerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getHandler_Command() {
		return (EReference)handlerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getHandlerContainer() {
		return handlerContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getHandlerContainer_Handlers() {
		return (EReference)handlerContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getInput() {
		return inputEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getInput_InputURI() {
		return (EAttribute)inputEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getParameter() {
		return parameterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getParameter_Tag() {
		return (EAttribute)parameterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getParameter_Value() {
		return (EAttribute)parameterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getUILabel() {
		return uiLabelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getUILabel_Label() {
		return (EAttribute)uiLabelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getUILabel_IconURI() {
		return (EAttribute)uiLabelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getUILabel_Tooltip() {
		return (EAttribute)uiLabelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getUIElement() {
		return uiElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getUIElement_Widget() {
		return (EAttribute)uiElementEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getUIElement_Renderer() {
		return (EAttribute)uiElementEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getUIElement_ToBeRendered() {
		return (EAttribute)uiElementEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getUIElement_Visible() {
		return (EAttribute)uiElementEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getUIElement_Parent() {
		return (EReference)uiElementEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getContext() {
		return contextEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getContext_Context() {
		return (EAttribute)contextEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getContext_Variables() {
		return (EAttribute)contextEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getKeySequence() {
		return keySequenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getKeySequence_KeySequence() {
		return (EAttribute)keySequenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getV_________Testing__________V() {
		return v_________Testing__________VEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTestHarness() {
		return testHarnessEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getV_________AbstractContainers__________V() {
		return v_________AbstractContainers__________VEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getElementContainer() {
		return elementContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getElementContainer_Children() {
		return (EReference)elementContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getElementContainer_ActiveChild() {
		return (EReference)elementContainerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getGenericTile() {
		return genericTileEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGenericTile_Weights() {
		return (EAttribute)genericTileEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGenericTile_Horizontal() {
		return (EAttribute)genericTileEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTrimContainer() {
		return trimContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTrimContainer_Horizontal() {
		return (EAttribute)trimContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTrimContainer_Side() {
		return (EAttribute)trimContainerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getV______________RCP_______________V() {
		return v______________RCP_______________VEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getApplication() {
		return applicationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getApplication_Commands() {
		return (EReference)applicationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getItem() {
		return itemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getItem_Enabled() {
		return (EAttribute)itemEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getItem_Selected() {
		return (EAttribute)itemEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getItem_Separator() {
		return (EAttribute)itemEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getHandledItem() {
		return handledItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getHandledItem_Command() {
		return (EReference)handledItemEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getHandledItem_WbCommand() {
		return (EAttribute)handledItemEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getHandledItem_Parameters() {
		return (EReference)handledItemEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMenuItem() {
		return menuItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getHandledMenuItem() {
		return handledMenuItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDirectMenuItem() {
		return directMenuItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMenu() {
		return menuEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getToolItem() {
		return toolItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getHandledToolItem() {
		return handledToolItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDirectToolItem() {
		return directToolItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getToolBar() {
		return toolBarEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPSCElement() {
		return pscElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPart() {
		return partEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getPart_Menus() {
		return (EReference)partEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getPart_Toolbar() {
		return (EReference)partEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getSaveablePart() {
		return saveablePartEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPartStack() {
		return partStackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPartSashContainer() {
		return partSashContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getWindow() {
		return windowEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getWindow_MainMenu() {
		return (EReference)windowEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getWindow_X() {
		return (EAttribute)windowEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getWindow_Y() {
		return (EAttribute)windowEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getWindow_Width() {
		return (EAttribute)windowEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getWindow_Height() {
		return (EAttribute)windowEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getWindowTrim() {
		return windowTrimEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getKeyBinding() {
		return keyBindingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getKeyBinding_Command() {
		return (EReference)keyBindingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getKeyBinding_Parameters() {
		return (EReference)keyBindingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBindingContainer() {
		return bindingContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBindingContainer_Bindings() {
		return (EReference)bindingContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getV______________IDE_______________V() {
		return v______________IDE_______________VEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getVSCElement() {
		return vscElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getView() {
		return viewEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getViewStack() {
		return viewStackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getEditor() {
		return editorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getViewSashContainer() {
		return viewSashContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMultiEditor() {
		return multiEditorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getESCElement() {
		return escElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getEditorStack() {
		return editorStackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getEditorStack_InputURI() {
		return (EAttribute)editorStackEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getEditorSashContainer() {
		return editorSashContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPerspective() {
		return perspectiveEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPerspectiveStack() {
		return perspectiveStackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIDEWindow() {
		return ideWindowEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getIDEWindow_MainMenu() {
		return (EReference)ideWindowEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getSideValue() {
		return sideValueEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getIEclipseContext() {
		return iEclipseContextEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getParameterizedCommand() {
		return parameterizedCommandEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MApplicationFactory getApplicationFactory() {
		return (MApplicationFactory)getEFactoryInstance();
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
		v____________Abstract_____________VEClass = createEClass(VABSTRACT_V);

		applicationElementEClass = createEClass(APPLICATION_ELEMENT);
		createEAttribute(applicationElementEClass, APPLICATION_ELEMENT__ID);

		contributionEClass = createEClass(CONTRIBUTION);
		createEAttribute(contributionEClass, CONTRIBUTION__URI);
		createEAttribute(contributionEClass, CONTRIBUTION__OBJECT);
		createEAttribute(contributionEClass, CONTRIBUTION__PERSISTED_STATE);

		commandEClass = createEClass(COMMAND);
		createEAttribute(commandEClass, COMMAND__COMMAND_NAME);
		createEAttribute(commandEClass, COMMAND__DESCRIPTION);
		createEReference(commandEClass, COMMAND__PARAMETERS);

		commandParameterEClass = createEClass(COMMAND_PARAMETER);
		createEAttribute(commandParameterEClass, COMMAND_PARAMETER__NAME);
		createEAttribute(commandParameterEClass, COMMAND_PARAMETER__TYPE_ID);
		createEAttribute(commandParameterEClass, COMMAND_PARAMETER__OPTIONAL);

		dirtyableEClass = createEClass(DIRTYABLE);
		createEAttribute(dirtyableEClass, DIRTYABLE__DIRTY);

		handlerEClass = createEClass(HANDLER);
		createEReference(handlerEClass, HANDLER__COMMAND);

		handlerContainerEClass = createEClass(HANDLER_CONTAINER);
		createEReference(handlerContainerEClass, HANDLER_CONTAINER__HANDLERS);

		inputEClass = createEClass(INPUT);
		createEAttribute(inputEClass, INPUT__INPUT_URI);

		parameterEClass = createEClass(PARAMETER);
		createEAttribute(parameterEClass, PARAMETER__TAG);
		createEAttribute(parameterEClass, PARAMETER__VALUE);

		uiLabelEClass = createEClass(UI_LABEL);
		createEAttribute(uiLabelEClass, UI_LABEL__LABEL);
		createEAttribute(uiLabelEClass, UI_LABEL__ICON_URI);
		createEAttribute(uiLabelEClass, UI_LABEL__TOOLTIP);

		uiElementEClass = createEClass(UI_ELEMENT);
		createEAttribute(uiElementEClass, UI_ELEMENT__WIDGET);
		createEAttribute(uiElementEClass, UI_ELEMENT__RENDERER);
		createEAttribute(uiElementEClass, UI_ELEMENT__TO_BE_RENDERED);
		createEAttribute(uiElementEClass, UI_ELEMENT__VISIBLE);
		createEReference(uiElementEClass, UI_ELEMENT__PARENT);

		contextEClass = createEClass(CONTEXT);
		createEAttribute(contextEClass, CONTEXT__CONTEXT);
		createEAttribute(contextEClass, CONTEXT__VARIABLES);

		keySequenceEClass = createEClass(KEY_SEQUENCE);
		createEAttribute(keySequenceEClass, KEY_SEQUENCE__KEY_SEQUENCE);

		v_________Testing__________VEClass = createEClass(VTESTING_V);

		testHarnessEClass = createEClass(TEST_HARNESS);

		v_________AbstractContainers__________VEClass = createEClass(VABSTRACT_CONTAINERS_V);

		elementContainerEClass = createEClass(ELEMENT_CONTAINER);
		createEReference(elementContainerEClass, ELEMENT_CONTAINER__CHILDREN);
		createEReference(elementContainerEClass, ELEMENT_CONTAINER__ACTIVE_CHILD);

		genericTileEClass = createEClass(GENERIC_TILE);
		createEAttribute(genericTileEClass, GENERIC_TILE__WEIGHTS);
		createEAttribute(genericTileEClass, GENERIC_TILE__HORIZONTAL);

		trimContainerEClass = createEClass(TRIM_CONTAINER);
		createEAttribute(trimContainerEClass, TRIM_CONTAINER__HORIZONTAL);
		createEAttribute(trimContainerEClass, TRIM_CONTAINER__SIDE);

		v______________RCP_______________VEClass = createEClass(VRCP_V);

		applicationEClass = createEClass(APPLICATION);
		createEReference(applicationEClass, APPLICATION__COMMANDS);

		itemEClass = createEClass(ITEM);
		createEAttribute(itemEClass, ITEM__ENABLED);
		createEAttribute(itemEClass, ITEM__SELECTED);
		createEAttribute(itemEClass, ITEM__SEPARATOR);

		handledItemEClass = createEClass(HANDLED_ITEM);
		createEReference(handledItemEClass, HANDLED_ITEM__COMMAND);
		createEAttribute(handledItemEClass, HANDLED_ITEM__WB_COMMAND);
		createEReference(handledItemEClass, HANDLED_ITEM__PARAMETERS);

		menuItemEClass = createEClass(MENU_ITEM);

		handledMenuItemEClass = createEClass(HANDLED_MENU_ITEM);

		directMenuItemEClass = createEClass(DIRECT_MENU_ITEM);

		menuEClass = createEClass(MENU);

		toolItemEClass = createEClass(TOOL_ITEM);

		handledToolItemEClass = createEClass(HANDLED_TOOL_ITEM);

		directToolItemEClass = createEClass(DIRECT_TOOL_ITEM);

		toolBarEClass = createEClass(TOOL_BAR);

		pscElementEClass = createEClass(PSC_ELEMENT);

		partEClass = createEClass(PART);
		createEReference(partEClass, PART__MENUS);
		createEReference(partEClass, PART__TOOLBAR);

		saveablePartEClass = createEClass(SAVEABLE_PART);

		partStackEClass = createEClass(PART_STACK);

		partSashContainerEClass = createEClass(PART_SASH_CONTAINER);

		windowEClass = createEClass(WINDOW);
		createEReference(windowEClass, WINDOW__MAIN_MENU);
		createEAttribute(windowEClass, WINDOW__X);
		createEAttribute(windowEClass, WINDOW__Y);
		createEAttribute(windowEClass, WINDOW__WIDTH);
		createEAttribute(windowEClass, WINDOW__HEIGHT);

		windowTrimEClass = createEClass(WINDOW_TRIM);

		keyBindingEClass = createEClass(KEY_BINDING);
		createEReference(keyBindingEClass, KEY_BINDING__COMMAND);
		createEReference(keyBindingEClass, KEY_BINDING__PARAMETERS);

		bindingContainerEClass = createEClass(BINDING_CONTAINER);
		createEReference(bindingContainerEClass, BINDING_CONTAINER__BINDINGS);

		v______________IDE_______________VEClass = createEClass(VIDE_V);

		vscElementEClass = createEClass(VSC_ELEMENT);

		viewEClass = createEClass(VIEW);

		viewStackEClass = createEClass(VIEW_STACK);

		editorEClass = createEClass(EDITOR);

		viewSashContainerEClass = createEClass(VIEW_SASH_CONTAINER);

		multiEditorEClass = createEClass(MULTI_EDITOR);

		escElementEClass = createEClass(ESC_ELEMENT);

		editorStackEClass = createEClass(EDITOR_STACK);
		createEAttribute(editorStackEClass, EDITOR_STACK__INPUT_URI);

		editorSashContainerEClass = createEClass(EDITOR_SASH_CONTAINER);

		perspectiveEClass = createEClass(PERSPECTIVE);

		perspectiveStackEClass = createEClass(PERSPECTIVE_STACK);

		ideWindowEClass = createEClass(IDE_WINDOW);
		createEReference(ideWindowEClass, IDE_WINDOW__MAIN_MENU);

		// Create enums
		sideValueEEnum = createEEnum(SIDE_VALUE);

		// Create data types
		iEclipseContextEDataType = createEDataType(IECLIPSE_CONTEXT);
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

		// Create type parameters
		ETypeParameter elementContainerEClass_T = addETypeParameter(elementContainerEClass, "T"); //$NON-NLS-1$
		ETypeParameter genericTileEClass_T = addETypeParameter(genericTileEClass, "T"); //$NON-NLS-1$
		ETypeParameter trimContainerEClass_T = addETypeParameter(trimContainerEClass, "T"); //$NON-NLS-1$

		// Set bounds for type parameters
		EGenericType g1 = createEGenericType(this.getUIElement());
		elementContainerEClass_T.getEBounds().add(g1);
		g1 = createEGenericType(this.getUIElement());
		genericTileEClass_T.getEBounds().add(g1);
		g1 = createEGenericType(this.getUIElement());
		trimContainerEClass_T.getEBounds().add(g1);

		// Add supertypes to classes
		contributionEClass.getESuperTypes().add(this.getApplicationElement());
		commandEClass.getESuperTypes().add(this.getApplicationElement());
		commandParameterEClass.getESuperTypes().add(this.getApplicationElement());
		handlerEClass.getESuperTypes().add(this.getContribution());
		parameterEClass.getESuperTypes().add(this.getApplicationElement());
		uiElementEClass.getESuperTypes().add(this.getApplicationElement());
		g1 = createEGenericType(this.getApplicationElement());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getCommand());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getContext());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getContribution());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		EGenericType g2 = createEGenericType(this.getUIElement());
		g1.getETypeArguments().add(g2);
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getParameter());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getInput());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getItem());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getUILabel());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getDirtyable());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		elementContainerEClass.getESuperTypes().add(this.getUIElement());
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(genericTileEClass_T);
		g1.getETypeArguments().add(g2);
		genericTileEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(trimContainerEClass_T);
		g1.getETypeArguments().add(g2);
		trimContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getContext());
		applicationEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getWindow());
		g1.getETypeArguments().add(g2);
		applicationEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getHandlerContainer());
		applicationEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getBindingContainer());
		applicationEClass.getEGenericSuperTypes().add(g1);
		itemEClass.getESuperTypes().add(this.getUIElement());
		itemEClass.getESuperTypes().add(this.getUILabel());
		handledItemEClass.getESuperTypes().add(this.getItem());
		menuItemEClass.getESuperTypes().add(this.getMenu());
		menuItemEClass.getESuperTypes().add(this.getItem());
		handledMenuItemEClass.getESuperTypes().add(this.getMenuItem());
		handledMenuItemEClass.getESuperTypes().add(this.getHandledItem());
		directMenuItemEClass.getESuperTypes().add(this.getContribution());
		directMenuItemEClass.getESuperTypes().add(this.getMenuItem());
		g1 = createEGenericType(this.getUIElement());
		menuEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getMenuItem());
		g1.getETypeArguments().add(g2);
		menuEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getItem());
		toolItemEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getMenuItem());
		g1.getETypeArguments().add(g2);
		toolItemEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getMenuItem());
		g1.getETypeArguments().add(g2);
		handledToolItemEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getHandledItem());
		handledToolItemEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getToolItem());
		handledToolItemEClass.getEGenericSuperTypes().add(g1);
		directToolItemEClass.getESuperTypes().add(this.getContribution());
		directToolItemEClass.getESuperTypes().add(this.getToolItem());
		g1 = createEGenericType(this.getUIElement());
		toolBarEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getToolItem());
		g1.getETypeArguments().add(g2);
		toolBarEClass.getEGenericSuperTypes().add(g1);
		pscElementEClass.getESuperTypes().add(this.getUIElement());
		partEClass.getESuperTypes().add(this.getContribution());
		partEClass.getESuperTypes().add(this.getContext());
		partEClass.getESuperTypes().add(this.getPSCElement());
		partEClass.getESuperTypes().add(this.getUILabel());
		partEClass.getESuperTypes().add(this.getHandlerContainer());
		partEClass.getESuperTypes().add(this.getBindingContainer());
		saveablePartEClass.getESuperTypes().add(this.getPart());
		saveablePartEClass.getESuperTypes().add(this.getDirtyable());
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getPart());
		g1.getETypeArguments().add(g2);
		partStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getUIElement());
		partStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPSCElement());
		partStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getGenericTile());
		g2 = createEGenericType(this.getPSCElement());
		g1.getETypeArguments().add(g2);
		partSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPSCElement());
		partSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getUILabel());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getPSCElement());
		g1.getETypeArguments().add(g2);
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getContext());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getUIElement());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getHandlerContainer());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getBindingContainer());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getTrimContainer());
		g2 = createEGenericType(this.getUIElement());
		g1.getETypeArguments().add(g2);
		windowTrimEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPSCElement());
		windowTrimEClass.getEGenericSuperTypes().add(g1);
		keyBindingEClass.getESuperTypes().add(this.getKeySequence());
		keyBindingEClass.getESuperTypes().add(this.getApplicationElement());
		vscElementEClass.getESuperTypes().add(this.getUIElement());
		viewEClass.getESuperTypes().add(this.getPart());
		viewEClass.getESuperTypes().add(this.getVSCElement());
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getView());
		g1.getETypeArguments().add(g2);
		viewStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getVSCElement());
		viewStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getUIElement());
		viewStackEClass.getEGenericSuperTypes().add(g1);
		editorEClass.getESuperTypes().add(this.getInput());
		editorEClass.getESuperTypes().add(this.getESCElement());
		editorEClass.getESuperTypes().add(this.getSaveablePart());
		g1 = createEGenericType(this.getGenericTile());
		g2 = createEGenericType(this.getVSCElement());
		g1.getETypeArguments().add(g2);
		viewSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getVSCElement());
		viewSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPSCElement());
		viewSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getSaveablePart());
		multiEditorEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getSaveablePart());
		g1.getETypeArguments().add(g2);
		multiEditorEClass.getEGenericSuperTypes().add(g1);
		escElementEClass.getESuperTypes().add(this.getUIElement());
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getSaveablePart());
		g1.getETypeArguments().add(g2);
		editorStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getESCElement());
		editorStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getUIElement());
		editorStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getGenericTile());
		g2 = createEGenericType(this.getESCElement());
		g1.getETypeArguments().add(g2);
		editorSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getESCElement());
		editorSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getVSCElement());
		editorSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getUILabel());
		perspectiveEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getPSCElement());
		g1.getETypeArguments().add(g2);
		perspectiveEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getContext());
		perspectiveEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPSCElement());
		perspectiveEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getUIElement());
		perspectiveStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getPerspective());
		g1.getETypeArguments().add(g2);
		perspectiveStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getVSCElement());
		perspectiveStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPSCElement());
		perspectiveStackEClass.getEGenericSuperTypes().add(g1);
		ideWindowEClass.getESuperTypes().add(this.getUILabel());
		ideWindowEClass.getESuperTypes().add(this.getContext());
		ideWindowEClass.getESuperTypes().add(this.getHandlerContainer());
		ideWindowEClass.getESuperTypes().add(this.getBindingContainer());

		// Initialize classes and features; add operations and parameters
		initEClass(v____________Abstract_____________VEClass, MV____________Abstract_____________V.class, "V____________Abstract_____________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(applicationElementEClass, MApplicationElement.class, "ApplicationElement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getApplicationElement_Id(), ecorePackage.getEString(), "id", null, 0, 1, MApplicationElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(contributionEClass, MContribution.class, "Contribution", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getContribution_URI(), ecorePackage.getEString(), "URI", null, 0, 1, MContribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getContribution_Object(), ecorePackage.getEJavaObject(), "object", null, 0, 1, MContribution.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getContribution_PersistedState(), ecorePackage.getEString(), "persistedState", null, 0, 1, MContribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(commandEClass, MCommand.class, "Command", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getCommand_CommandName(), ecorePackage.getEString(), "commandName", null, 0, 1, MCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCommand_Description(), ecorePackage.getEString(), "description", null, 0, 1, MCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getCommand_Parameters(), this.getCommandParameter(), null, "parameters", null, 0, -1, MCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(commandParameterEClass, MCommandParameter.class, "CommandParameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getCommandParameter_Name(), ecorePackage.getEString(), "name", null, 1, 1, MCommandParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCommandParameter_TypeId(), ecorePackage.getEString(), "typeId", null, 0, 1, MCommandParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCommandParameter_Optional(), ecorePackage.getEBoolean(), "optional", "true", 0, 1, MCommandParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$

		initEClass(dirtyableEClass, MDirtyable.class, "Dirtyable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getDirtyable_Dirty(), ecorePackage.getEBoolean(), "dirty", null, 0, 1, MDirtyable.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(handlerEClass, MHandler.class, "Handler", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getHandler_Command(), this.getCommand(), null, "command", null, 1, 1, MHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(handlerContainerEClass, MHandlerContainer.class, "HandlerContainer", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getHandlerContainer_Handlers(), this.getHandler(), null, "handlers", null, 0, -1, MHandlerContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(inputEClass, MInput.class, "Input", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getInput_InputURI(), ecorePackage.getEString(), "inputURI", null, 0, 1, MInput.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(parameterEClass, MParameter.class, "Parameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getParameter_Tag(), ecorePackage.getEString(), "tag", null, 0, 1, MParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getParameter_Value(), ecorePackage.getEString(), "value", null, 0, 1, MParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(uiLabelEClass, MUILabel.class, "UILabel", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getUILabel_Label(), ecorePackage.getEString(), "label", null, 0, 1, MUILabel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getUILabel_IconURI(), ecorePackage.getEString(), "iconURI", null, 0, 1, MUILabel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getUILabel_Tooltip(), ecorePackage.getEString(), "tooltip", null, 0, 1, MUILabel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(uiElementEClass, MUIElement.class, "UIElement", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getUIElement_Widget(), ecorePackage.getEJavaObject(), "widget", null, 0, 1, MUIElement.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getUIElement_Renderer(), ecorePackage.getEJavaObject(), "renderer", null, 0, 1, MUIElement.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getUIElement_ToBeRendered(), ecorePackage.getEBoolean(), "toBeRendered", "true", 0, 1, MUIElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(getUIElement_Visible(), ecorePackage.getEBoolean(), "visible", null, 0, 1, MUIElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getUIElement());
		g1.getETypeArguments().add(g2);
		initEReference(getUIElement_Parent(), g1, this.getElementContainer_Children(), "parent", null, 0, 1, MUIElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(contextEClass, MContext.class, "Context", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getContext_Context(), this.getIEclipseContext(), "context", null, 0, 1, MContext.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getContext_Variables(), ecorePackage.getEString(), "variables", null, 0, -1, MContext.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED); //$NON-NLS-1$

		initEClass(keySequenceEClass, MKeySequence.class, "KeySequence", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getKeySequence_KeySequence(), ecorePackage.getEString(), "keySequence", null, 1, 1, MKeySequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(v_________Testing__________VEClass, MV_________Testing__________V.class, "V_________Testing__________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(testHarnessEClass, MTestHarness.class, "TestHarness", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(v_________AbstractContainers__________VEClass, MV_________AbstractContainers__________V.class, "V_________AbstractContainers__________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(elementContainerEClass, MElementContainer.class, "ElementContainer", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		g1 = createEGenericType(elementContainerEClass_T);
		initEReference(getElementContainer_Children(), g1, this.getUIElement_Parent(), "children", null, 0, -1, MElementContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		g1 = createEGenericType(elementContainerEClass_T);
		initEReference(getElementContainer_ActiveChild(), g1, null, "activeChild", null, 0, 1, MElementContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(genericTileEClass, MGenericTile.class, "GenericTile", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getGenericTile_Weights(), ecorePackage.getEInt(), "weights", null, 0, -1, MGenericTile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGenericTile_Horizontal(), ecorePackage.getEBoolean(), "horizontal", null, 0, 1, MGenericTile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(trimContainerEClass, MTrimContainer.class, "TrimContainer", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getTrimContainer_Horizontal(), ecorePackage.getEBoolean(), "horizontal", null, 0, 1, MTrimContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getTrimContainer_Side(), this.getSideValue(), "side", null, 1, 1, MTrimContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(v______________RCP_______________VEClass, MV______________RCP_______________V.class, "V______________RCP_______________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(applicationEClass, MApplication.class, "Application", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getApplication_Commands(), this.getCommand(), null, "commands", null, 0, -1, MApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(itemEClass, MItem.class, "Item", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getItem_Enabled(), ecorePackage.getEBoolean(), "enabled", "true", 0, 1, MItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(getItem_Selected(), ecorePackage.getEBoolean(), "selected", null, 0, 1, MItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getItem_Separator(), ecorePackage.getEBoolean(), "separator", null, 0, 1, MItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(handledItemEClass, MHandledItem.class, "HandledItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getHandledItem_Command(), this.getCommand(), null, "command", null, 0, 1, MHandledItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getHandledItem_WbCommand(), this.getParameterizedCommand(), "wbCommand", null, 0, 1, MHandledItem.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getHandledItem_Parameters(), this.getParameter(), null, "parameters", null, 0, -1, MHandledItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(menuItemEClass, MMenuItem.class, "MenuItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(handledMenuItemEClass, MHandledMenuItem.class, "HandledMenuItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(directMenuItemEClass, MDirectMenuItem.class, "DirectMenuItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(menuEClass, MMenu.class, "Menu", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(toolItemEClass, MToolItem.class, "ToolItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(handledToolItemEClass, MHandledToolItem.class, "HandledToolItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(directToolItemEClass, MDirectToolItem.class, "DirectToolItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(toolBarEClass, MToolBar.class, "ToolBar", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(pscElementEClass, MPSCElement.class, "PSCElement", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(partEClass, MPart.class, "Part", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getPart_Menus(), this.getMenu(), null, "menus", null, 0, -1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getPart_Toolbar(), this.getToolBar(), null, "toolbar", null, 0, 1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(saveablePartEClass, MSaveablePart.class, "SaveablePart", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(partStackEClass, MPartStack.class, "PartStack", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(partSashContainerEClass, MPartSashContainer.class, "PartSashContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(windowEClass, MWindow.class, "Window", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getWindow_MainMenu(), this.getMenu(), null, "mainMenu", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getWindow_X(), ecorePackage.getEInt(), "x", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getWindow_Y(), ecorePackage.getEInt(), "y", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getWindow_Width(), ecorePackage.getEInt(), "width", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getWindow_Height(), ecorePackage.getEInt(), "height", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(windowTrimEClass, MWindowTrim.class, "WindowTrim", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(keyBindingEClass, MKeyBinding.class, "KeyBinding", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getKeyBinding_Command(), this.getCommand(), null, "command", null, 1, 1, MKeyBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getKeyBinding_Parameters(), this.getParameter(), null, "parameters", null, 0, -1, MKeyBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(bindingContainerEClass, MBindingContainer.class, "BindingContainer", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getBindingContainer_Bindings(), this.getKeyBinding(), null, "bindings", null, 0, -1, MBindingContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(v______________IDE_______________VEClass, MV______________IDE_______________V.class, "V______________IDE_______________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(vscElementEClass, MVSCElement.class, "VSCElement", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(viewEClass, MView.class, "View", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(viewStackEClass, MViewStack.class, "ViewStack", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(editorEClass, MEditor.class, "Editor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(viewSashContainerEClass, MViewSashContainer.class, "ViewSashContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(multiEditorEClass, MMultiEditor.class, "MultiEditor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(escElementEClass, MESCElement.class, "ESCElement", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(editorStackEClass, MEditorStack.class, "EditorStack", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getEditorStack_InputURI(), ecorePackage.getEString(), "inputURI", null, 0, 1, MEditorStack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(editorSashContainerEClass, MEditorSashContainer.class, "EditorSashContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(perspectiveEClass, MPerspective.class, "Perspective", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(perspectiveStackEClass, MPerspectiveStack.class, "PerspectiveStack", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(ideWindowEClass, MIDEWindow.class, "IDEWindow", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getIDEWindow_MainMenu(), this.getMenu(), null, "mainMenu", null, 0, 1, MIDEWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		// Initialize enums and add enum literals
		initEEnum(sideValueEEnum, SideValue.class, "SideValue"); //$NON-NLS-1$
		addEEnumLiteral(sideValueEEnum, SideValue.TOP);
		addEEnumLiteral(sideValueEEnum, SideValue.BOTTOM);
		addEEnumLiteral(sideValueEEnum, SideValue.LEFT);
		addEEnumLiteral(sideValueEEnum, SideValue.RIGHT);

		// Initialize data types
		initEDataType(iEclipseContextEDataType, IEclipseContext.class, "IEclipseContext", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEDataType(parameterizedCommandEDataType, ParameterizedCommand.class, "ParameterizedCommand", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		// Create resource
		createResource(eNS_URI);
	}

} //ApplicationPackageImpl
