/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *
 * $Id: ApplicationPackageImpl.java,v 1.7 2009/03/15 21:26:03 bbokowski Exp $
 */
package org.eclipse.e4.ui.model.internal.application;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.ApplicationFactory;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MContributedPart;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.MHandledItem;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MItemContainer;
import org.eclipse.e4.ui.model.application.MItemPart;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MSashForm;
import org.eclipse.e4.ui.model.application.MStack;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolBarItem;
import org.eclipse.e4.ui.model.application.MTrim;
import org.eclipse.e4.ui.model.application.MWindow;

import org.eclipse.e4.ui.model.internal.workbench.WorkbenchPackageImpl;

import org.eclipse.e4.ui.model.workbench.WorkbenchPackage;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
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
public class ApplicationPackageImpl extends EPackageImpl implements ApplicationPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mApplicationElementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mApplicationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mPartEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mStackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mSashFormEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mContributedPartEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mContributionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mHandlerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mHandledItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mMenuItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mToolBarItemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mItemContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mMenuEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mToolBarEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mTrimEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mItemPartEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mWindowEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mCommandEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType iEclipseContextEDataType = null;

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
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ApplicationPackageImpl() {
		super(eNS_URI, ApplicationFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this
	 * model, and for any others upon which it depends.  Simple
	 * dependencies are satisfied by calling this method on all
	 * dependent packages before doing anything else.  This method drives
	 * initialization for interdependent packages directly, in parallel
	 * with this package, itself.
	 * <p>Of this package and its interdependencies, all packages which
	 * have not yet been registered by their URI values are first created
	 * and registered.  The packages are then initialized in two steps:
	 * meta-model objects for all of the packages are created before any
	 * are initialized, since one package's meta-model objects may refer to
	 * those of another.
	 * <p>Invocation of this method will not affect any packages that have
	 * already been initialized.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ApplicationPackage init() {
		if (isInited) return (ApplicationPackage)EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI);

		// Obtain or create and register package
		ApplicationPackageImpl theApplicationPackage = (ApplicationPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof ApplicationPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new ApplicationPackageImpl());

		isInited = true;

		// Obtain or create and register interdependencies
		WorkbenchPackageImpl theWorkbenchPackage = (WorkbenchPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(WorkbenchPackage.eNS_URI) instanceof WorkbenchPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(WorkbenchPackage.eNS_URI) : WorkbenchPackage.eINSTANCE);

		// Create package meta-data objects
		theApplicationPackage.createPackageContents();
		theWorkbenchPackage.createPackageContents();

		// Initialize created meta-data
		theApplicationPackage.initializePackageContents();
		theWorkbenchPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theApplicationPackage.freeze();

		return theApplicationPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMApplicationElement() {
		return mApplicationElementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMApplicationElement_Owner() {
		return (EAttribute)mApplicationElementEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMApplicationElement_Id() {
		return (EAttribute)mApplicationElementEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMApplication() {
		return mApplicationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMApplication_Windows() {
		return (EReference)mApplicationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMApplication_Command() {
		return (EReference)mApplicationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMPart() {
		return mPartEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMPart_Menu() {
		return (EReference)mPartEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMPart_ToolBar() {
		return (EReference)mPartEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMPart_Policy() {
		return (EAttribute)mPartEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMPart_Children() {
		return (EReference)mPartEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMPart_ActiveChild() {
		return (EReference)mPartEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMPart_Handlers() {
		return (EReference)mPartEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMPart_Trim() {
		return (EReference)mPartEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMPart_Widget() {
		return (EAttribute)mPartEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMPart_Parent() {
		return (EReference)mPartEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMPart_Visible() {
		return (EAttribute)mPartEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMPart_Context() {
		return (EAttribute)mPartEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMStack() {
		return mStackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMSashForm() {
		return mSashFormEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMSashForm_Weights() {
		return (EAttribute)mSashFormEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMContributedPart() {
		return mContributedPartEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMContribution() {
		return mContributionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMContribution_URI() {
		return (EAttribute)mContributionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMContribution_Object() {
		return (EAttribute)mContributionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMContribution_PersistedState() {
		return (EAttribute)mContributionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMHandler() {
		return mHandlerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMHandler_Command() {
		return (EReference)mHandlerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMItem() {
		return mItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMItem_IconURI() {
		return (EAttribute)mItemEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMItem_Name() {
		return (EAttribute)mItemEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMItem_Tooltip() {
		return (EAttribute)mItemEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMHandledItem() {
		return mHandledItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMHandledItem_Command() {
		return (EReference)mHandledItemEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMHandledItem_Menu() {
		return (EReference)mHandledItemEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMMenuItem() {
		return mMenuItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMMenuItem_Separator() {
		return (EAttribute)mMenuItemEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMToolBarItem() {
		return mToolBarItemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMItemContainer() {
		return mItemContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMItemContainer_Items() {
		return (EReference)mItemContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMMenu() {
		return mMenuEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMToolBar() {
		return mToolBarEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMTrim() {
		return mTrimEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMTrim_TopTrim() {
		return (EReference)mTrimEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMTrim_LeftTrim() {
		return (EReference)mTrimEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMTrim_RightTrim() {
		return (EReference)mTrimEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMTrim_BottomTrim() {
		return (EReference)mTrimEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMItemPart() {
		return mItemPartEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMWindow() {
		return mWindowEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMWindow_X() {
		return (EAttribute)mWindowEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMWindow_Y() {
		return (EAttribute)mWindowEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMWindow_Width() {
		return (EAttribute)mWindowEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMWindow_Height() {
		return (EAttribute)mWindowEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMCommand() {
		return mCommandEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMCommand_Name() {
		return (EAttribute)mCommandEClass.getEStructuralFeatures().get(0);
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
	public ApplicationFactory getApplicationFactory() {
		return (ApplicationFactory)getEFactoryInstance();
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
		mApplicationElementEClass = createEClass(MAPPLICATION_ELEMENT);
		createEAttribute(mApplicationElementEClass, MAPPLICATION_ELEMENT__OWNER);
		createEAttribute(mApplicationElementEClass, MAPPLICATION_ELEMENT__ID);

		mApplicationEClass = createEClass(MAPPLICATION);
		createEReference(mApplicationEClass, MAPPLICATION__WINDOWS);
		createEReference(mApplicationEClass, MAPPLICATION__COMMAND);

		mPartEClass = createEClass(MPART);
		createEReference(mPartEClass, MPART__MENU);
		createEReference(mPartEClass, MPART__TOOL_BAR);
		createEAttribute(mPartEClass, MPART__POLICY);
		createEReference(mPartEClass, MPART__CHILDREN);
		createEReference(mPartEClass, MPART__ACTIVE_CHILD);
		createEReference(mPartEClass, MPART__HANDLERS);
		createEReference(mPartEClass, MPART__TRIM);
		createEAttribute(mPartEClass, MPART__WIDGET);
		createEReference(mPartEClass, MPART__PARENT);
		createEAttribute(mPartEClass, MPART__VISIBLE);
		createEAttribute(mPartEClass, MPART__CONTEXT);

		mStackEClass = createEClass(MSTACK);

		mSashFormEClass = createEClass(MSASH_FORM);
		createEAttribute(mSashFormEClass, MSASH_FORM__WEIGHTS);

		mContributedPartEClass = createEClass(MCONTRIBUTED_PART);

		mContributionEClass = createEClass(MCONTRIBUTION);
		createEAttribute(mContributionEClass, MCONTRIBUTION__URI);
		createEAttribute(mContributionEClass, MCONTRIBUTION__OBJECT);
		createEAttribute(mContributionEClass, MCONTRIBUTION__PERSISTED_STATE);

		mHandlerEClass = createEClass(MHANDLER);
		createEReference(mHandlerEClass, MHANDLER__COMMAND);

		mItemEClass = createEClass(MITEM);
		createEAttribute(mItemEClass, MITEM__ICON_URI);
		createEAttribute(mItemEClass, MITEM__NAME);
		createEAttribute(mItemEClass, MITEM__TOOLTIP);

		mHandledItemEClass = createEClass(MHANDLED_ITEM);
		createEReference(mHandledItemEClass, MHANDLED_ITEM__COMMAND);
		createEReference(mHandledItemEClass, MHANDLED_ITEM__MENU);

		mMenuItemEClass = createEClass(MMENU_ITEM);
		createEAttribute(mMenuItemEClass, MMENU_ITEM__SEPARATOR);

		mToolBarItemEClass = createEClass(MTOOL_BAR_ITEM);

		mItemContainerEClass = createEClass(MITEM_CONTAINER);
		createEReference(mItemContainerEClass, MITEM_CONTAINER__ITEMS);

		mMenuEClass = createEClass(MMENU);

		mToolBarEClass = createEClass(MTOOL_BAR);

		mTrimEClass = createEClass(MTRIM);
		createEReference(mTrimEClass, MTRIM__TOP_TRIM);
		createEReference(mTrimEClass, MTRIM__LEFT_TRIM);
		createEReference(mTrimEClass, MTRIM__RIGHT_TRIM);
		createEReference(mTrimEClass, MTRIM__BOTTOM_TRIM);

		mItemPartEClass = createEClass(MITEM_PART);

		mWindowEClass = createEClass(MWINDOW);
		createEAttribute(mWindowEClass, MWINDOW__X);
		createEAttribute(mWindowEClass, MWINDOW__Y);
		createEAttribute(mWindowEClass, MWINDOW__WIDTH);
		createEAttribute(mWindowEClass, MWINDOW__HEIGHT);

		mCommandEClass = createEClass(MCOMMAND);
		createEAttribute(mCommandEClass, MCOMMAND__NAME);

		// Create data types
		iEclipseContextEDataType = createEDataType(IECLIPSE_CONTEXT);
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
		ETypeParameter mApplicationEClass_W = addETypeParameter(mApplicationEClass, "W"); //$NON-NLS-1$
		ETypeParameter mPartEClass_P = addETypeParameter(mPartEClass, "P"); //$NON-NLS-1$
		ETypeParameter mSashFormEClass_P = addETypeParameter(mSashFormEClass, "P"); //$NON-NLS-1$
		ETypeParameter mContributedPartEClass_P = addETypeParameter(mContributedPartEClass, "P"); //$NON-NLS-1$
		ETypeParameter mItemContainerEClass_I = addETypeParameter(mItemContainerEClass, "I"); //$NON-NLS-1$
		ETypeParameter mItemPartEClass_P = addETypeParameter(mItemPartEClass, "P"); //$NON-NLS-1$
		ETypeParameter mWindowEClass_P = addETypeParameter(mWindowEClass, "P"); //$NON-NLS-1$

		// Set bounds for type parameters
		EGenericType g1 = createEGenericType(this.getMWindow());
		EGenericType g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		mApplicationEClass_W.getEBounds().add(g1);
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		mPartEClass_P.getEBounds().add(g1);
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		mSashFormEClass_P.getEBounds().add(g1);
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		mContributedPartEClass_P.getEBounds().add(g1);
		g1 = createEGenericType(this.getMItem());
		mItemContainerEClass_I.getEBounds().add(g1);
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		mItemPartEClass_P.getEBounds().add(g1);
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		mWindowEClass_P.getEBounds().add(g1);

		// Add supertypes to classes
		mApplicationEClass.getESuperTypes().add(this.getMApplicationElement());
		mPartEClass.getESuperTypes().add(this.getMApplicationElement());
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType(this.getMItemPart());
		g1.getETypeArguments().add(g2);
		EGenericType g3 = createEGenericType();
		g2.getETypeArguments().add(g3);
		mStackEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType(mSashFormEClass_P);
		g1.getETypeArguments().add(g2);
		mSashFormEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getMItemPart());
		g2 = createEGenericType(mContributedPartEClass_P);
		g1.getETypeArguments().add(g2);
		mContributedPartEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getMContribution());
		mContributedPartEClass.getEGenericSuperTypes().add(g1);
		mContributionEClass.getESuperTypes().add(this.getMApplicationElement());
		mHandlerEClass.getESuperTypes().add(this.getMContribution());
		mItemEClass.getESuperTypes().add(this.getMApplicationElement());
		mHandledItemEClass.getESuperTypes().add(this.getMItem());
		mMenuItemEClass.getESuperTypes().add(this.getMHandledItem());
		mToolBarItemEClass.getESuperTypes().add(this.getMHandledItem());
		mItemContainerEClass.getESuperTypes().add(this.getMApplicationElement());
		g1 = createEGenericType(this.getMItemContainer());
		g2 = createEGenericType(this.getMMenuItem());
		g1.getETypeArguments().add(g2);
		mMenuEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getMItemContainer());
		g2 = createEGenericType(this.getMToolBarItem());
		g1.getETypeArguments().add(g2);
		mToolBarEClass.getEGenericSuperTypes().add(g1);
		mTrimEClass.getESuperTypes().add(this.getMApplicationElement());
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType(mItemPartEClass_P);
		g1.getETypeArguments().add(g2);
		mItemPartEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getMItem());
		mItemPartEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(this.getMItemPart());
		g2 = createEGenericType(mWindowEClass_P);
		g1.getETypeArguments().add(g2);
		mWindowEClass.getEGenericSuperTypes().add(g1);
		mCommandEClass.getESuperTypes().add(this.getMApplicationElement());

		// Initialize classes and features; add operations and parameters
		initEClass(mApplicationElementEClass, MApplicationElement.class, "MApplicationElement", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getMApplicationElement_Owner(), ecorePackage.getEJavaObject(), "owner", null, 0, 1, MApplicationElement.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getMApplicationElement_Id(), ecorePackage.getEString(), "id", null, 0, 1, MApplicationElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mApplicationEClass, MApplication.class, "MApplication", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		g1 = createEGenericType(mApplicationEClass_W);
		initEReference(getMApplication_Windows(), g1, null, "windows", null, 1, -1, MApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getMApplication_Command(), this.getMCommand(), null, "command", null, 1, -1, MApplication.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mPartEClass, MPart.class, "MPart", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getMPart_Menu(), this.getMMenu(), null, "menu", null, 0, 1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getMPart_ToolBar(), this.getMToolBar(), null, "toolBar", null, 0, 1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getMPart_Policy(), ecorePackage.getEString(), "policy", null, 0, 1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		g1 = createEGenericType(mPartEClass_P);
		initEReference(getMPart_Children(), g1, this.getMPart_Parent(), "children", null, 0, -1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		g1 = createEGenericType(mPartEClass_P);
		initEReference(getMPart_ActiveChild(), g1, null, "activeChild", null, 0, 1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getMPart_Handlers(), this.getMHandler(), null, "handlers", null, 0, -1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getMPart_Trim(), this.getMTrim(), null, "trim", null, 0, 1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getMPart_Widget(), ecorePackage.getEJavaObject(), "widget", null, 0, 1, MPart.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		initEReference(getMPart_Parent(), g1, this.getMPart_Children(), "parent", null, 0, 1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getMPart_Visible(), ecorePackage.getEBoolean(), "visible", "true", 0, 1, MPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED); //$NON-NLS-1$ //$NON-NLS-2$
		initEAttribute(getMPart_Context(), this.getIEclipseContext(), "context", null, 0, 1, MPart.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mStackEClass, MStack.class, "MStack", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(mSashFormEClass, MSashForm.class, "MSashForm", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getMSashForm_Weights(), ecorePackage.getEInt(), "weights", null, 0, -1, MSashForm.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mContributedPartEClass, MContributedPart.class, "MContributedPart", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(mContributionEClass, MContribution.class, "MContribution", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getMContribution_URI(), ecorePackage.getEString(), "URI", null, 0, 1, MContribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getMContribution_Object(), ecorePackage.getEJavaObject(), "object", null, 0, 1, MContribution.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getMContribution_PersistedState(), ecorePackage.getEString(), "persistedState", null, 0, 1, MContribution.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mHandlerEClass, MHandler.class, "MHandler", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getMHandler_Command(), this.getMCommand(), null, "command", null, 1, 1, MHandler.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mItemEClass, MItem.class, "MItem", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getMItem_IconURI(), ecorePackage.getEString(), "iconURI", null, 0, 1, MItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getMItem_Name(), ecorePackage.getEString(), "name", null, 0, 1, MItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getMItem_Tooltip(), ecorePackage.getEString(), "tooltip", null, 0, 1, MItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mHandledItemEClass, MHandledItem.class, "MHandledItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getMHandledItem_Command(), this.getMCommand(), null, "command", null, 0, 1, MHandledItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getMHandledItem_Menu(), this.getMMenu(), null, "menu", null, 0, 1, MHandledItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mMenuItemEClass, MMenuItem.class, "MMenuItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getMMenuItem_Separator(), ecorePackage.getEBoolean(), "separator", null, 0, 1, MMenuItem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mToolBarItemEClass, MToolBarItem.class, "MToolBarItem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(mItemContainerEClass, MItemContainer.class, "MItemContainer", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		g1 = createEGenericType(mItemContainerEClass_I);
		initEReference(getMItemContainer_Items(), g1, null, "items", null, 0, -1, MItemContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mMenuEClass, MMenu.class, "MMenu", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(mToolBarEClass, MToolBar.class, "MToolBar", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(mTrimEClass, MTrim.class, "MTrim", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		initEReference(getMTrim_TopTrim(), g1, null, "topTrim", null, 0, 1, MTrim.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		initEReference(getMTrim_LeftTrim(), g1, null, "leftTrim", null, 0, 1, MTrim.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		initEReference(getMTrim_RightTrim(), g1, null, "rightTrim", null, 0, 1, MTrim.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		g1 = createEGenericType(this.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		initEReference(getMTrim_BottomTrim(), g1, null, "bottomTrim", null, 0, 1, MTrim.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mItemPartEClass, MItemPart.class, "MItemPart", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(mWindowEClass, MWindow.class, "MWindow", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getMWindow_X(), ecorePackage.getEInt(), "x", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getMWindow_Y(), ecorePackage.getEInt(), "y", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getMWindow_Width(), ecorePackage.getEInt(), "width", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getMWindow_Height(), ecorePackage.getEInt(), "height", null, 0, 1, MWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mCommandEClass, MCommand.class, "MCommand", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getMCommand_Name(), ecorePackage.getEString(), "name", null, 0, 1, MCommand.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		// Initialize data types
		initEDataType(iEclipseContextEDataType, IEclipseContext.class, "IEclipseContext", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		// Create resource
		createResource(eNS_URI);
	}

} //ApplicationPackageImpl
