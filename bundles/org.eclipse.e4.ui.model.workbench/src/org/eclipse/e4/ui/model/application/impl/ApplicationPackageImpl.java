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
import org.eclipse.core.commands.ParameterizedCommand;

import org.eclipse.e4.core.contexts.IEclipseContext;

import org.eclipse.e4.ui.model.application.ItemType;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MBindingContainer;
import org.eclipse.e4.ui.model.application.MBindingContext;
import org.eclipse.e4.ui.model.application.MBindingTable;
import org.eclipse.e4.ui.model.application.MBindings;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MCommandParameter;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.MDirectToolItem;
import org.eclipse.e4.ui.model.application.MDirtyable;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MGenericStack;
import org.eclipse.e4.ui.model.application.MGenericTile;
import org.eclipse.e4.ui.model.application.MHandledItem;
import org.eclipse.e4.ui.model.application.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.MHandledToolItem;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MHandlerContainer;
import org.eclipse.e4.ui.model.application.MInput;
import org.eclipse.e4.ui.model.application.MInputPart;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.e4.ui.model.application.MKeySequence;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MModelComponent;
import org.eclipse.e4.ui.model.application.MModelComponents;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MParameter;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.e4.ui.model.application.MPartDescriptorContainer;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MPerspective;
import org.eclipse.e4.ui.model.application.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.MPlaceholder;
import org.eclipse.e4.ui.model.application.MTestHarness;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolItem;
import org.eclipse.e4.ui.model.application.MTrimContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MUILabel;
import org.eclipse.e4.ui.model.application.MV_________AbstractContainers__________V;
import org.eclipse.e4.ui.model.application.MV_________Testing__________V;
import org.eclipse.e4.ui.model.application.MV____________Abstract_____________V;
import org.eclipse.e4.ui.model.application.MV____________ConstantsAndTypes_____________V;
import org.eclipse.e4.ui.model.application.MV______________Commands_______________V;
import org.eclipse.e4.ui.model.application.MV______________MenusAndTBs_______________V;
import org.eclipse.e4.ui.model.application.MV______________RCP_______________V;
import org.eclipse.e4.ui.model.application.MV______________SharedElements_______________V;
import org.eclipse.e4.ui.model.application.MV______________Trim_______________V;
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
	private EClass v____________ConstantsAndTypes_____________VEClass = null;

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
	private EClass dirtyableEClass = null;

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
	private EClass uiElementEClass = null;

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
	private EClass contextEClass = null;

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
	private EClass genericStackEClass = null;

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
	private EClass v______________MenusAndTBs_______________VEClass = null;

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
	private EClass menuItemEClass = null;

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
	private EClass inputPartEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass partDescriptorEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass partDescriptorContainerEClass = null;

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
	private EClass modelComponentsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass modelComponentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass v______________Commands_______________VEClass = null;

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
	private EClass handledItemEClass = null;

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
	private EClass handledToolItemEClass = null;

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
	private EClass v______________Trim_______________VEClass = null;

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
	private EClass windowTrimEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass v______________SharedElements_______________VEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass placeholderEClass = null;

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
	private EClass stringToStringMapEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum itemTypeEEnum = null;

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
	public EClass getV____________ConstantsAndTypes_____________V() {
		return v____________ConstantsAndTypes_____________VEClass;
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
	public EAttribute getApplicationElement_Tags() {
		return (EAttribute)applicationElementEClass.getEStructuralFeatures().get(1);
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
	public EReference getContribution_PersistedState() {
		return (EReference)contributionEClass.getEStructuralFeatures().get(2);
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
	public EAttribute getUIElement_OnTop() {
		return (EAttribute)uiElementEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getUIElement_Visible() {
		return (EAttribute)uiElementEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getUIElement_Parent() {
		return (EReference)uiElementEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getUIElement_ContainerData() {
		return (EAttribute)uiElementEClass.getEStructuralFeatures().get(6);
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
	public EReference getContext_Properties() {
		return (EReference)contextEClass.getEStructuralFeatures().get(2);
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
	public EReference getElementContainer_SelectedElement() {
		return (EReference)elementContainerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getGenericStack() {
		return genericStackEClass;
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
	public EAttribute getGenericTile_Horizontal() {
		return (EAttribute)genericTileEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getV______________MenusAndTBs_______________V() {
		return v______________MenusAndTBs_______________VEClass;
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
	public EAttribute getItem_Type() {
		return (EAttribute)itemEClass.getEStructuralFeatures().get(2);
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
	public EAttribute getPart_Closeable() {
		return (EAttribute)partEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getInputPart() {
		return inputPartEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPartDescriptor() {
		return partDescriptorEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPartDescriptor_AllowMultiple() {
		return (EAttribute)partDescriptorEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPartDescriptor_Category() {
		return (EAttribute)partDescriptorEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPartDescriptorContainer() {
		return partDescriptorContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getPartDescriptorContainer_Descriptors() {
		return (EReference)partDescriptorContainerEClass.getEStructuralFeatures().get(0);
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
	public EClass getModelComponents() {
		return modelComponentsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getModelComponents_Components() {
		return (EReference)modelComponentsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getModelComponent() {
		return modelComponentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getModelComponent_PositionInParent() {
		return (EAttribute)modelComponentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getModelComponent_ParentID() {
		return (EAttribute)modelComponentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getModelComponent_Children() {
		return (EReference)modelComponentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getModelComponent_Commands() {
		return (EReference)modelComponentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getModelComponent_Processor() {
		return (EAttribute)modelComponentEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getModelComponent_Bindings() {
		return (EReference)modelComponentEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getV______________Commands_______________V() {
		return v______________Commands_______________VEClass;
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
	public EReference getBindingContainer_BindingTables() {
		return (EReference)bindingContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBindingContainer_RootContext() {
		return (EReference)bindingContainerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBindings() {
		return bindingsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBindings_BindingContexts() {
		return (EAttribute)bindingsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBindingContext() {
		return bindingContextEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBindingContext_Name() {
		return (EAttribute)bindingContextEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBindingContext_Description() {
		return (EAttribute)bindingContextEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBindingContext_Children() {
		return (EReference)bindingContextEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getBindingTable() {
		return bindingTableEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getBindingTable_BindingContextId() {
		return (EAttribute)bindingTableEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getBindingTable_Bindings() {
		return (EReference)bindingTableEClass.getEStructuralFeatures().get(1);
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
	public EClass getHandledMenuItem() {
		return handledMenuItemEClass;
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
	public EClass getV______________Trim_______________V() {
		return v______________Trim_______________VEClass;
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
	public EAttribute getTrimContainer_Side() {
		return (EAttribute)trimContainerEClass.getEStructuralFeatures().get(0);
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
	public EClass getV______________SharedElements_______________V() {
		return v______________SharedElements_______________VEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPlaceholder() {
		return placeholderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getPlaceholder_Ref() {
		return (EReference)placeholderEClass.getEStructuralFeatures().get(0);
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
	public EClass getStringToStringMap() {
		return stringToStringMapEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStringToStringMap_Key() {
		return (EAttribute)stringToStringMapEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getStringToStringMap_Value() {
		return (EAttribute)stringToStringMapEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getItemType() {
		return itemTypeEEnum;
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
		v____________ConstantsAndTypes_____________VEClass = createEClass(VCONSTANTS_AND_TYPES_V);

		stringToStringMapEClass = createEClass(STRING_TO_STRING_MAP);
		createEAttribute(stringToStringMapEClass, STRING_TO_STRING_MAP__KEY);
		createEAttribute(stringToStringMapEClass, STRING_TO_STRING_MAP__VALUE);

		v____________Abstract_____________VEClass = createEClass(VABSTRACT_V);

		applicationElementEClass = createEClass(APPLICATION_ELEMENT);
		createEAttribute(applicationElementEClass, APPLICATION_ELEMENT__ID);
		createEAttribute(applicationElementEClass, APPLICATION_ELEMENT__TAGS);

		contributionEClass = createEClass(CONTRIBUTION);
		createEAttribute(contributionEClass, CONTRIBUTION__URI);
		createEAttribute(contributionEClass, CONTRIBUTION__OBJECT);
		createEReference(contributionEClass, CONTRIBUTION__PERSISTED_STATE);

		dirtyableEClass = createEClass(DIRTYABLE);
		createEAttribute(dirtyableEClass, DIRTYABLE__DIRTY);

		inputEClass = createEClass(INPUT);
		createEAttribute(inputEClass, INPUT__INPUT_URI);

		uiElementEClass = createEClass(UI_ELEMENT);
		createEAttribute(uiElementEClass, UI_ELEMENT__WIDGET);
		createEAttribute(uiElementEClass, UI_ELEMENT__RENDERER);
		createEAttribute(uiElementEClass, UI_ELEMENT__TO_BE_RENDERED);
		createEAttribute(uiElementEClass, UI_ELEMENT__ON_TOP);
		createEAttribute(uiElementEClass, UI_ELEMENT__VISIBLE);
		createEReference(uiElementEClass, UI_ELEMENT__PARENT);
		createEAttribute(uiElementEClass, UI_ELEMENT__CONTAINER_DATA);

		uiLabelEClass = createEClass(UI_LABEL);
		createEAttribute(uiLabelEClass, UI_LABEL__LABEL);
		createEAttribute(uiLabelEClass, UI_LABEL__ICON_URI);
		createEAttribute(uiLabelEClass, UI_LABEL__TOOLTIP);

		contextEClass = createEClass(CONTEXT);
		createEAttribute(contextEClass, CONTEXT__CONTEXT);
		createEAttribute(contextEClass, CONTEXT__VARIABLES);
		createEReference(contextEClass, CONTEXT__PROPERTIES);

		v_________AbstractContainers__________VEClass = createEClass(VABSTRACT_CONTAINERS_V);

		elementContainerEClass = createEClass(ELEMENT_CONTAINER);
		createEReference(elementContainerEClass, ELEMENT_CONTAINER__CHILDREN);
		createEReference(elementContainerEClass, ELEMENT_CONTAINER__SELECTED_ELEMENT);

		genericStackEClass = createEClass(GENERIC_STACK);

		genericTileEClass = createEClass(GENERIC_TILE);
		createEAttribute(genericTileEClass, GENERIC_TILE__HORIZONTAL);

		v______________MenusAndTBs_______________VEClass = createEClass(VMENUS_AND_TBS_V);

		itemEClass = createEClass(ITEM);
		createEAttribute(itemEClass, ITEM__ENABLED);
		createEAttribute(itemEClass, ITEM__SELECTED);
		createEAttribute(itemEClass, ITEM__TYPE);

		menuItemEClass = createEClass(MENU_ITEM);

		directMenuItemEClass = createEClass(DIRECT_MENU_ITEM);

		menuEClass = createEClass(MENU);

		toolItemEClass = createEClass(TOOL_ITEM);

		directToolItemEClass = createEClass(DIRECT_TOOL_ITEM);

		toolBarEClass = createEClass(TOOL_BAR);

		v______________RCP_______________VEClass = createEClass(VRCP_V);

		applicationEClass = createEClass(APPLICATION);
		createEReference(applicationEClass, APPLICATION__COMMANDS);

		pscElementEClass = createEClass(PSC_ELEMENT);

		partEClass = createEClass(PART);
		createEReference(partEClass, PART__MENUS);
		createEReference(partEClass, PART__TOOLBAR);
		createEAttribute(partEClass, PART__CLOSEABLE);

		inputPartEClass = createEClass(INPUT_PART);

		partDescriptorEClass = createEClass(PART_DESCRIPTOR);
		createEAttribute(partDescriptorEClass, PART_DESCRIPTOR__ALLOW_MULTIPLE);
		createEAttribute(partDescriptorEClass, PART_DESCRIPTOR__CATEGORY);

		partDescriptorContainerEClass = createEClass(PART_DESCRIPTOR_CONTAINER);
		createEReference(partDescriptorContainerEClass, PART_DESCRIPTOR_CONTAINER__DESCRIPTORS);

		partStackEClass = createEClass(PART_STACK);

		partSashContainerEClass = createEClass(PART_SASH_CONTAINER);

		windowEClass = createEClass(WINDOW);
		createEReference(windowEClass, WINDOW__MAIN_MENU);
		createEAttribute(windowEClass, WINDOW__X);
		createEAttribute(windowEClass, WINDOW__Y);
		createEAttribute(windowEClass, WINDOW__WIDTH);
		createEAttribute(windowEClass, WINDOW__HEIGHT);

		modelComponentsEClass = createEClass(MODEL_COMPONENTS);
		createEReference(modelComponentsEClass, MODEL_COMPONENTS__COMPONENTS);

		modelComponentEClass = createEClass(MODEL_COMPONENT);
		createEAttribute(modelComponentEClass, MODEL_COMPONENT__POSITION_IN_PARENT);
		createEAttribute(modelComponentEClass, MODEL_COMPONENT__PARENT_ID);
		createEReference(modelComponentEClass, MODEL_COMPONENT__CHILDREN);
		createEReference(modelComponentEClass, MODEL_COMPONENT__COMMANDS);
		createEAttribute(modelComponentEClass, MODEL_COMPONENT__PROCESSOR);
		createEReference(modelComponentEClass, MODEL_COMPONENT__BINDINGS);

		v______________Commands_______________VEClass = createEClass(VCOMMANDS_V);

		bindingContainerEClass = createEClass(BINDING_CONTAINER);
		createEReference(bindingContainerEClass, BINDING_CONTAINER__BINDING_TABLES);
		createEReference(bindingContainerEClass, BINDING_CONTAINER__ROOT_CONTEXT);

		bindingsEClass = createEClass(BINDINGS);
		createEAttribute(bindingsEClass, BINDINGS__BINDING_CONTEXTS);

		bindingContextEClass = createEClass(BINDING_CONTEXT);
		createEAttribute(bindingContextEClass, BINDING_CONTEXT__NAME);
		createEAttribute(bindingContextEClass, BINDING_CONTEXT__DESCRIPTION);
		createEReference(bindingContextEClass, BINDING_CONTEXT__CHILDREN);

		bindingTableEClass = createEClass(BINDING_TABLE);
		createEAttribute(bindingTableEClass, BINDING_TABLE__BINDING_CONTEXT_ID);
		createEReference(bindingTableEClass, BINDING_TABLE__BINDINGS);

		commandEClass = createEClass(COMMAND);
		createEAttribute(commandEClass, COMMAND__COMMAND_NAME);
		createEAttribute(commandEClass, COMMAND__DESCRIPTION);
		createEReference(commandEClass, COMMAND__PARAMETERS);

		commandParameterEClass = createEClass(COMMAND_PARAMETER);
		createEAttribute(commandParameterEClass, COMMAND_PARAMETER__NAME);
		createEAttribute(commandParameterEClass, COMMAND_PARAMETER__TYPE_ID);
		createEAttribute(commandParameterEClass, COMMAND_PARAMETER__OPTIONAL);

		handlerEClass = createEClass(HANDLER);
		createEReference(handlerEClass, HANDLER__COMMAND);

		handlerContainerEClass = createEClass(HANDLER_CONTAINER);
		createEReference(handlerContainerEClass, HANDLER_CONTAINER__HANDLERS);

		handledItemEClass = createEClass(HANDLED_ITEM);
		createEReference(handledItemEClass, HANDLED_ITEM__COMMAND);
		createEAttribute(handledItemEClass, HANDLED_ITEM__WB_COMMAND);
		createEReference(handledItemEClass, HANDLED_ITEM__PARAMETERS);

		handledMenuItemEClass = createEClass(HANDLED_MENU_ITEM);

		handledToolItemEClass = createEClass(HANDLED_TOOL_ITEM);

		keyBindingEClass = createEClass(KEY_BINDING);
		createEReference(keyBindingEClass, KEY_BINDING__COMMAND);
		createEReference(keyBindingEClass, KEY_BINDING__PARAMETERS);

		keySequenceEClass = createEClass(KEY_SEQUENCE);
		createEAttribute(keySequenceEClass, KEY_SEQUENCE__KEY_SEQUENCE);

		parameterEClass = createEClass(PARAMETER);
		createEAttribute(parameterEClass, PARAMETER__TAG);
		createEAttribute(parameterEClass, PARAMETER__VALUE);

		v______________Trim_______________VEClass = createEClass(VTRIM_V);

		trimContainerEClass = createEClass(TRIM_CONTAINER);
		createEAttribute(trimContainerEClass, TRIM_CONTAINER__SIDE);

		windowTrimEClass = createEClass(WINDOW_TRIM);

		v______________SharedElements_______________VEClass = createEClass(VSHARED_ELEMENTS_V);

		placeholderEClass = createEClass(PLACEHOLDER);
		createEReference(placeholderEClass, PLACEHOLDER__REF);

		perspectiveEClass = createEClass(PERSPECTIVE);

		perspectiveStackEClass = createEClass(PERSPECTIVE_STACK);

		v_________Testing__________VEClass = createEClass(VTESTING_V);

		testHarnessEClass = createEClass(TEST_HARNESS);

		// Create enums
		itemTypeEEnum = createEEnum(ITEM_TYPE);
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
		ETypeParameter genericStackEClass_T = addETypeParameter(genericStackEClass, "T"); //$NON-NLS-1$
		ETypeParameter genericTileEClass_T = addETypeParameter(genericTileEClass, "T"); //$NON-NLS-1$
		ETypeParameter trimContainerEClass_T = addETypeParameter(trimContainerEClass, "T"); //$NON-NLS-1$

		// Set bounds for type parameters
		EGenericType g1 = createEGenericType(this.getUIElement());
		elementContainerEClass_T.getEBounds().add(g1);
		g1 = createEGenericType(this.getUIElement());
		genericStackEClass_T.getEBounds().add(g1);
		g1 = createEGenericType(this.getUIElement());
		genericTileEClass_T.getEBounds().add(g1);
		g1 = createEGenericType(this.getUIElement());
		trimContainerEClass_T.getEBounds().add(g1);

		// Add supertypes to classes
		contributionEClass.getESuperTypes().add(this.getApplicationElement());
		uiElementEClass.getESuperTypes().add(this.getApplicationElement());
		elementContainerEClass.getESuperTypes().add(this.getUIElement());
		g1 = createEGenericType(this.getElementContainer());
		EGenericType g2 = createEGenericType(genericStackEClass_T);
		g1.getETypeArguments().add(g2);
		genericStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(genericTileEClass_T);
		g1.getETypeArguments().add(g2);
		genericTileEClass.getEGenericSuperTypes().add(g1);
		itemEClass.getESuperTypes().add(this.getUIElement());
		itemEClass.getESuperTypes().add(this.getUILabel());
		menuItemEClass.getESuperTypes().add(this.getMenu());
		menuItemEClass.getESuperTypes().add(this.getItem());
		directMenuItemEClass.getESuperTypes().add(this.getContribution());
		directMenuItemEClass.getESuperTypes().add(this.getMenuItem());
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
		directToolItemEClass.getESuperTypes().add(this.getToolItem());
		directToolItemEClass.getESuperTypes().add(this.getContribution());
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getToolItem());
		g1.getETypeArguments().add(g2);
		toolBarEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getWindow());
		g1.getETypeArguments().add(g2);
		applicationEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getContext());
		applicationEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getHandlerContainer());
		applicationEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getBindingContainer());
		applicationEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPartDescriptorContainer());
		applicationEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getBindings());
		applicationEClass.getEGenericSuperTypes().add(g1);
		pscElementEClass.getESuperTypes().add(this.getUIElement());
		partEClass.getESuperTypes().add(this.getContribution());
		partEClass.getESuperTypes().add(this.getContext());
		partEClass.getESuperTypes().add(this.getPSCElement());
		partEClass.getESuperTypes().add(this.getUILabel());
		partEClass.getESuperTypes().add(this.getHandlerContainer());
		partEClass.getESuperTypes().add(this.getDirtyable());
		partEClass.getESuperTypes().add(this.getBindings());
		inputPartEClass.getESuperTypes().add(this.getPart());
		inputPartEClass.getESuperTypes().add(this.getInput());
		partDescriptorEClass.getESuperTypes().add(this.getPart());
		g1 = createEGenericType(this.getGenericStack());
		g2 = createEGenericType(this.getPart());
		g1.getETypeArguments().add(g2);
		partStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPSCElement());
		partStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getGenericTile());
		g2 = createEGenericType(this.getPSCElement());
		g1.getETypeArguments().add(g2);
		partSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPSCElement());
		partSashContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getPSCElement());
		g1.getETypeArguments().add(g2);
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getUILabel());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getContext());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getHandlerContainer());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPSCElement());
		windowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getBindings());
		windowEClass.getEGenericSuperTypes().add(g1);
		modelComponentEClass.getESuperTypes().add(this.getPartDescriptorContainer());
		modelComponentEClass.getESuperTypes().add(this.getApplicationElement());
		modelComponentEClass.getESuperTypes().add(this.getHandlerContainer());
		modelComponentEClass.getESuperTypes().add(this.getBindingContainer());
		bindingContextEClass.getESuperTypes().add(this.getApplicationElement());
		bindingTableEClass.getESuperTypes().add(this.getApplicationElement());
		commandEClass.getESuperTypes().add(this.getApplicationElement());
		commandParameterEClass.getESuperTypes().add(this.getApplicationElement());
		handlerEClass.getESuperTypes().add(this.getContribution());
		handledItemEClass.getESuperTypes().add(this.getItem());
		handledMenuItemEClass.getESuperTypes().add(this.getMenuItem());
		handledMenuItemEClass.getESuperTypes().add(this.getHandledItem());
		handledToolItemEClass.getESuperTypes().add(this.getToolItem());
		handledToolItemEClass.getESuperTypes().add(this.getHandledItem());
		keyBindingEClass.getESuperTypes().add(this.getKeySequence());
		keyBindingEClass.getESuperTypes().add(this.getApplicationElement());
		parameterEClass.getESuperTypes().add(this.getApplicationElement());
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(trimContainerEClass_T);
		g1.getETypeArguments().add(g2);
		trimContainerEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getTrimContainer());
		g2 = createEGenericType(this.getUIElement());
		g1.getETypeArguments().add(g2);
		windowTrimEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPSCElement());
		windowTrimEClass.getEGenericSuperTypes().add(g1);
		placeholderEClass.getESuperTypes().add(this.getUIElement());
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getPSCElement());
		g1.getETypeArguments().add(g2);
		perspectiveEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getUILabel());
		perspectiveEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getContext());
		perspectiveEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getUIElement());
		perspectiveStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getPSCElement());
		perspectiveStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getGenericStack());
		g2 = createEGenericType(this.getPerspective());
		g1.getETypeArguments().add(g2);
		perspectiveStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getApplicationElement());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getCommand());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getContext());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getContribution());
		testHarnessEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getUIElement());
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

		// Initialize classes and features; add operations and parameters
		initEClass(v____________ConstantsAndTypes_____________VEClass, MV____________ConstantsAndTypes_____________V.class, "V____________ConstantsAndTypes_____________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(stringToStringMapEClass, Map.Entry.class, "StringToStringMap", !IS_ABSTRACT, !IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getStringToStringMap_Key(), ecorePackage.getEString(), "key", null, 0, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getStringToStringMap_Value(), ecorePackage.getEString(), "value", null, 0, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(v____________Abstract_____________VEClass, MV____________Abstract_____________V.class, "V____________Abstract_____________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(applicationElementEClass, MApplicationElement.class, "ApplicationElement", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getApplicationElement_Id(), ecorePackage.getEString(), "id", null, 0, 1, MApplicationElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getApplicationElement_Tags(), ecorePackage.getEString(), "tags", null, 0, -1, MApplicationElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED); //$NON-NLS-1$

		initEClass(contributionEClass, MContribution.class, "Contribution", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getContribution_URI(), ecorePackage.getEString(), "URI", null, 0, 1, MContribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getContribution_Object(), ecorePackage.getEJavaObject(), "object", null, 0, 1, MContribution.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getContribution_PersistedState(), this.getStringToStringMap(), null, "persistedState", null, 0, -1, MContribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(dirtyableEClass, MDirtyable.class, "Dirtyable", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getDirtyable_Dirty(), ecorePackage.getEBoolean(), "dirty", null, 0, 1, MDirtyable.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(inputEClass, MInput.class, "Input", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getInput_InputURI(), ecorePackage.getEString(), "inputURI", null, 0, 1, MInput.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(uiElementEClass, MUIElement.class, "UIElement", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getUIElement_Widget(), ecorePackage.getEJavaObject(), "widget", null, 0, 1, MUIElement.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getUIElement_Renderer(), ecorePackage.getEJavaObject(), "renderer", null, 0, 1, MUIElement.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getUIElement_ToBeRendered(), ecorePackage.getEBoolean(), "toBeRendered", "true", 0, 1, MUIElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(getUIElement_OnTop(), ecorePackage.getEBoolean(), "onTop", null, 0, 1, MUIElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getUIElement_Visible(), ecorePackage.getEBoolean(), "visible", "true", 0, 1, MUIElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		g1 = createEGenericType(this.getElementContainer());
		g2 = createEGenericType(this.getUIElement());
		g1.getETypeArguments().add(g2);
		initEReference(getUIElement_Parent(), g1, this.getElementContainer_Children(), "parent", null, 0, 1, MUIElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getUIElement_ContainerData(), ecorePackage.getEString(), "containerData", null, 0, 1, MUIElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(uiLabelEClass, MUILabel.class, "UILabel", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getUILabel_Label(), ecorePackage.getEString(), "label", null, 0, 1, MUILabel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getUILabel_IconURI(), ecorePackage.getEString(), "iconURI", null, 0, 1, MUILabel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getUILabel_Tooltip(), ecorePackage.getEString(), "tooltip", null, 0, 1, MUILabel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(contextEClass, MContext.class, "Context", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getContext_Context(), this.getIEclipseContext(), "context", null, 0, 1, MContext.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getContext_Variables(), ecorePackage.getEString(), "variables", null, 0, -1, MContext.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED); //$NON-NLS-1$
		initEReference(getContext_Properties(), this.getStringToStringMap(), null, "properties", null, 0, -1, MContext.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(v_________AbstractContainers__________VEClass, MV_________AbstractContainers__________V.class, "V_________AbstractContainers__________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(elementContainerEClass, MElementContainer.class, "ElementContainer", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		g1 = createEGenericType(elementContainerEClass_T);
		initEReference(getElementContainer_Children(), g1, this.getUIElement_Parent(), "children", null, 0, -1, MElementContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		g1 = createEGenericType(elementContainerEClass_T);
		initEReference(getElementContainer_SelectedElement(), g1, null, "selectedElement", null, 0, 1, MElementContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(genericStackEClass, MGenericStack.class, "GenericStack", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(genericTileEClass, MGenericTile.class, "GenericTile", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getGenericTile_Horizontal(), ecorePackage.getEBoolean(), "horizontal", null, 0, 1, MGenericTile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(v______________MenusAndTBs_______________VEClass, MV______________MenusAndTBs_______________V.class, "V______________MenusAndTBs_______________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(itemEClass, MItem.class, "Item", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getItem_Enabled(), ecorePackage.getEBoolean(), "enabled", "true", 0, 1, MItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(getItem_Selected(), ecorePackage.getEBoolean(), "selected", null, 0, 1, MItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getItem_Type(), this.getItemType(), "type", null, 1, 1, MItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(menuItemEClass, MMenuItem.class, "MenuItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(directMenuItemEClass, MDirectMenuItem.class, "DirectMenuItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(menuEClass, MMenu.class, "Menu", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(toolItemEClass, MToolItem.class, "ToolItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(directToolItemEClass, MDirectToolItem.class, "DirectToolItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(toolBarEClass, MToolBar.class, "ToolBar", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(v______________RCP_______________VEClass, MV______________RCP_______________V.class, "V______________RCP_______________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(applicationEClass, MApplication.class, "Application", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getApplication_Commands(), this.getCommand(), null, "commands", null, 0, -1, MApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(pscElementEClass, MPSCElement.class, "PSCElement", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(partEClass, MPart.class, "Part", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getPart_Menus(), this.getMenu(), null, "menus", null, 0, -1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getPart_Toolbar(), this.getToolBar(), null, "toolbar", null, 0, 1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPart_Closeable(), ecorePackage.getEBoolean(), "closeable", "false", 0, 1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$

		initEClass(inputPartEClass, MInputPart.class, "InputPart", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(partDescriptorEClass, MPartDescriptor.class, "PartDescriptor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPartDescriptor_AllowMultiple(), ecorePackage.getEBoolean(), "allowMultiple", null, 0, 1, MPartDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPartDescriptor_Category(), ecorePackage.getEString(), "category", null, 0, 1, MPartDescriptor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(partDescriptorContainerEClass, MPartDescriptorContainer.class, "PartDescriptorContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getPartDescriptorContainer_Descriptors(), this.getPartDescriptor(), null, "descriptors", null, 0, -1, MPartDescriptorContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(partStackEClass, MPartStack.class, "PartStack", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(partSashContainerEClass, MPartSashContainer.class, "PartSashContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(windowEClass, MWindow.class, "Window", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getWindow_MainMenu(), this.getMenu(), null, "mainMenu", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getWindow_X(), ecorePackage.getEInt(), "x", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getWindow_Y(), ecorePackage.getEInt(), "y", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getWindow_Width(), ecorePackage.getEInt(), "width", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getWindow_Height(), ecorePackage.getEInt(), "height", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(modelComponentsEClass, MModelComponents.class, "ModelComponents", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getModelComponents_Components(), this.getModelComponent(), null, "components", null, 0, -1, MModelComponents.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(modelComponentEClass, MModelComponent.class, "ModelComponent", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getModelComponent_PositionInParent(), ecorePackage.getEString(), "positionInParent", "", 0, 1, MModelComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(getModelComponent_ParentID(), ecorePackage.getEString(), "parentID", null, 1, 1, MModelComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getModelComponent_Children(), this.getUIElement(), null, "children", null, 0, -1, MModelComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getModelComponent_Commands(), this.getCommand(), null, "commands", null, 0, -1, MModelComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getModelComponent_Processor(), ecorePackage.getEString(), "processor", null, 0, 1, MModelComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getModelComponent_Bindings(), this.getKeyBinding(), null, "bindings", null, 0, -1, MModelComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(v______________Commands_______________VEClass, MV______________Commands_______________V.class, "V______________Commands_______________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(bindingContainerEClass, MBindingContainer.class, "BindingContainer", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getBindingContainer_BindingTables(), this.getBindingTable(), null, "bindingTables", null, 0, -1, MBindingContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getBindingContainer_RootContext(), this.getBindingContext(), null, "rootContext", null, 0, 1, MBindingContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(bindingsEClass, MBindings.class, "Bindings", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getBindings_BindingContexts(), ecorePackage.getEString(), "bindingContexts", null, 0, -1, MBindings.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, !IS_ORDERED); //$NON-NLS-1$

		initEClass(bindingContextEClass, MBindingContext.class, "BindingContext", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getBindingContext_Name(), ecorePackage.getEString(), "name", null, 0, 1, MBindingContext.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getBindingContext_Description(), ecorePackage.getEString(), "description", null, 0, 1, MBindingContext.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getBindingContext_Children(), this.getBindingContext(), null, "children", null, 0, -1, MBindingContext.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(bindingTableEClass, MBindingTable.class, "BindingTable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getBindingTable_BindingContextId(), ecorePackage.getEString(), "bindingContextId", null, 1, 1, MBindingTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getBindingTable_Bindings(), this.getKeyBinding(), null, "bindings", null, 0, -1, MBindingTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(commandEClass, MCommand.class, "Command", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getCommand_CommandName(), ecorePackage.getEString(), "commandName", null, 0, 1, MCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCommand_Description(), ecorePackage.getEString(), "description", null, 0, 1, MCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getCommand_Parameters(), this.getCommandParameter(), null, "parameters", null, 0, -1, MCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(commandParameterEClass, MCommandParameter.class, "CommandParameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getCommandParameter_Name(), ecorePackage.getEString(), "name", null, 1, 1, MCommandParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCommandParameter_TypeId(), ecorePackage.getEString(), "typeId", null, 0, 1, MCommandParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getCommandParameter_Optional(), ecorePackage.getEBoolean(), "optional", "true", 0, 1, MCommandParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$

		initEClass(handlerEClass, MHandler.class, "Handler", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getHandler_Command(), this.getCommand(), null, "command", null, 1, 1, MHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(handlerContainerEClass, MHandlerContainer.class, "HandlerContainer", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getHandlerContainer_Handlers(), this.getHandler(), null, "handlers", null, 0, -1, MHandlerContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(handledItemEClass, MHandledItem.class, "HandledItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getHandledItem_Command(), this.getCommand(), null, "command", null, 0, 1, MHandledItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getHandledItem_WbCommand(), this.getParameterizedCommand(), "wbCommand", null, 0, 1, MHandledItem.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getHandledItem_Parameters(), this.getParameter(), null, "parameters", null, 0, -1, MHandledItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(handledMenuItemEClass, MHandledMenuItem.class, "HandledMenuItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(handledToolItemEClass, MHandledToolItem.class, "HandledToolItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(keyBindingEClass, MKeyBinding.class, "KeyBinding", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getKeyBinding_Command(), this.getCommand(), null, "command", null, 1, 1, MKeyBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getKeyBinding_Parameters(), this.getParameter(), null, "parameters", null, 0, -1, MKeyBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(keySequenceEClass, MKeySequence.class, "KeySequence", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getKeySequence_KeySequence(), ecorePackage.getEString(), "keySequence", null, 1, 1, MKeySequence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(parameterEClass, MParameter.class, "Parameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getParameter_Tag(), ecorePackage.getEString(), "tag", null, 0, 1, MParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getParameter_Value(), ecorePackage.getEString(), "value", null, 0, 1, MParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(v______________Trim_______________VEClass, MV______________Trim_______________V.class, "V______________Trim_______________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(trimContainerEClass, MTrimContainer.class, "TrimContainer", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getTrimContainer_Side(), this.getSideValue(), "side", null, 1, 1, MTrimContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(windowTrimEClass, MWindowTrim.class, "WindowTrim", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(v______________SharedElements_______________VEClass, MV______________SharedElements_______________V.class, "V______________SharedElements_______________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(placeholderEClass, MPlaceholder.class, "Placeholder", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getPlaceholder_Ref(), this.getUIElement(), null, "ref", null, 1, 1, MPlaceholder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(perspectiveEClass, MPerspective.class, "Perspective", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(perspectiveStackEClass, MPerspectiveStack.class, "PerspectiveStack", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(v_________Testing__________VEClass, MV_________Testing__________V.class, "V_________Testing__________V", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(testHarnessEClass, MTestHarness.class, "TestHarness", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		// Initialize enums and add enum literals
		initEEnum(itemTypeEEnum, ItemType.class, "ItemType"); //$NON-NLS-1$
		addEEnumLiteral(itemTypeEEnum, ItemType.PUSH);
		addEEnumLiteral(itemTypeEEnum, ItemType.CHECK);
		addEEnumLiteral(itemTypeEEnum, ItemType.RADIO);
		addEEnumLiteral(itemTypeEEnum, ItemType.SEPARATOR);

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
