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
 * $Id: WorkbenchPackageImpl.java,v 1.3 2009/02/03 14:25:36 emoffatt Exp $
 */
package org.eclipse.e4.ui.model.internal.workbench;

import org.eclipse.e4.ui.model.application.ApplicationPackage;

import org.eclipse.e4.ui.model.internal.application.ApplicationPackageImpl;

import org.eclipse.e4.ui.model.workbench.MPerspective;
import org.eclipse.e4.ui.model.workbench.MProxyPart;
import org.eclipse.e4.ui.model.workbench.MWorkbench;
import org.eclipse.e4.ui.model.workbench.MWorkbenchWindow;
import org.eclipse.e4.ui.model.workbench.WorkbenchFactory;
import org.eclipse.e4.ui.model.workbench.WorkbenchPackage;

import org.eclipse.emf.ecore.EClass;
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
public class WorkbenchPackageImpl extends EPackageImpl implements WorkbenchPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mWorkbenchWindowEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mProxyPartEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mPerspectiveEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mWorkbenchEClass = null;

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
	 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private WorkbenchPackageImpl() {
		super(eNS_URI, WorkbenchFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link WorkbenchPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static WorkbenchPackage init() {
		if (isInited) return (WorkbenchPackage)EPackage.Registry.INSTANCE.getEPackage(WorkbenchPackage.eNS_URI);

		// Obtain or create and register package
		WorkbenchPackageImpl theWorkbenchPackage = (WorkbenchPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof WorkbenchPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new WorkbenchPackageImpl());

		isInited = true;

		// Obtain or create and register interdependencies
		ApplicationPackageImpl theApplicationPackage = (ApplicationPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI) instanceof ApplicationPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI) : ApplicationPackage.eINSTANCE);

		// Create package meta-data objects
		theWorkbenchPackage.createPackageContents();
		theApplicationPackage.createPackageContents();

		// Initialize created meta-data
		theWorkbenchPackage.initializePackageContents();
		theApplicationPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theWorkbenchPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(WorkbenchPackage.eNS_URI, theWorkbenchPackage);
		return theWorkbenchPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMWorkbenchWindow() {
		return mWorkbenchWindowEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMWorkbenchWindow_SharedParts() {
		return (EReference)mWorkbenchWindowEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMProxyPart() {
		return mProxyPartEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMProxyPart_Part() {
		return (EReference)mProxyPartEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMPerspective() {
		return mPerspectiveEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMWorkbench() {
		return mWorkbenchEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMWorkbench_WbWindows() {
		return (EReference)mWorkbenchEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getMWorkbench_CurWBW() {
		return (EReference)mWorkbenchEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WorkbenchFactory getWorkbenchFactory() {
		return (WorkbenchFactory)getEFactoryInstance();
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
		mWorkbenchWindowEClass = createEClass(MWORKBENCH_WINDOW);
		createEReference(mWorkbenchWindowEClass, MWORKBENCH_WINDOW__SHARED_PARTS);

		mProxyPartEClass = createEClass(MPROXY_PART);
		createEReference(mProxyPartEClass, MPROXY_PART__PART);

		mPerspectiveEClass = createEClass(MPERSPECTIVE);

		mWorkbenchEClass = createEClass(MWORKBENCH);
		createEReference(mWorkbenchEClass, MWORKBENCH__WB_WINDOWS);
		createEReference(mWorkbenchEClass, MWORKBENCH__CUR_WBW);
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
		ApplicationPackage theApplicationPackage = (ApplicationPackage)EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI);

		// Create type parameters
		ETypeParameter mProxyPartEClass_P = addETypeParameter(mProxyPartEClass, "P"); //$NON-NLS-1$
		ETypeParameter mPerspectiveEClass_P = addETypeParameter(mPerspectiveEClass, "P"); //$NON-NLS-1$

		// Set bounds for type parameters
		EGenericType g1 = createEGenericType(theApplicationPackage.getMPart());
		EGenericType g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		mProxyPartEClass_P.getEBounds().add(g1);
		g1 = createEGenericType(theApplicationPackage.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		mPerspectiveEClass_P.getEBounds().add(g1);

		// Add supertypes to classes
		g1 = createEGenericType(theApplicationPackage.getMWindow());
		g2 = createEGenericType(this.getMPerspective());
		g1.getETypeArguments().add(g2);
		EGenericType g3 = createEGenericType();
		g2.getETypeArguments().add(g3);
		mWorkbenchWindowEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theApplicationPackage.getMPart());
		g2 = createEGenericType(mProxyPartEClass_P);
		g1.getETypeArguments().add(g2);
		mProxyPartEClass.getEGenericSuperTypes().add(g1);
		g1 = createEGenericType(theApplicationPackage.getMItemPart());
		g2 = createEGenericType(mPerspectiveEClass_P);
		g1.getETypeArguments().add(g2);
		mPerspectiveEClass.getEGenericSuperTypes().add(g1);

		// Initialize classes and features; add operations and parameters
		initEClass(mWorkbenchWindowEClass, MWorkbenchWindow.class, "MWorkbenchWindow", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		g1 = createEGenericType(theApplicationPackage.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		initEReference(getMWorkbenchWindow_SharedParts(), g1, null, "sharedParts", null, 0, -1, MWorkbenchWindow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mProxyPartEClass, MProxyPart.class, "MProxyPart", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		g1 = createEGenericType(theApplicationPackage.getMPart());
		g2 = createEGenericType();
		g1.getETypeArguments().add(g2);
		initEReference(getMProxyPart_Part(), g1, null, "part", null, 0, 1, MProxyPart.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(mPerspectiveEClass, MPerspective.class, "MPerspective", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$

		initEClass(mWorkbenchEClass, MWorkbench.class, "MWorkbench", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getMWorkbench_WbWindows(), this.getMWorkbenchWindow(), null, "wbWindows", null, 0, -1, MWorkbench.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getMWorkbench_CurWBW(), this.getMWorkbenchWindow(), null, "curWBW", null, 0, 1, MWorkbench.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		// Create resource
		createResource(eNS_URI);
	}

} //WorkbenchPackageImpl
