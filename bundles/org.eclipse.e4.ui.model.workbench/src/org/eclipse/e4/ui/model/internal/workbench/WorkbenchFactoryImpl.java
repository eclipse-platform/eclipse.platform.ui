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
 * $Id: WorkbenchFactoryImpl.java,v 1.4 2009/06/16 16:09:51 pwebster Exp $
 */
package org.eclipse.e4.ui.model.internal.workbench;

import org.eclipse.e4.ui.model.application.MPart;

import org.eclipse.e4.ui.model.workbench.*;

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
public class WorkbenchFactoryImpl extends EFactoryImpl implements WorkbenchFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static WorkbenchFactory init() {
		try {
			WorkbenchFactory theWorkbenchFactory = (WorkbenchFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.eclipse.org/ui/2008/Workbench"); //$NON-NLS-1$ 
			if (theWorkbenchFactory != null) {
				return theWorkbenchFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new WorkbenchFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WorkbenchFactoryImpl() {
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
			case WorkbenchPackage.MWORKBENCH_WINDOW: return createMWorkbenchWindow();
			case WorkbenchPackage.MPERSPECTIVE: return createMPerspective();
			case WorkbenchPackage.MWORKBENCH: return createMWorkbench();
			case WorkbenchPackage.MMENU_ITEM_RENDERER: return createMMenuItemRenderer();
			case WorkbenchPackage.MTOOL_ITEM_RENDERER: return createMToolItemRenderer();
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
	public MWorkbenchWindow createMWorkbenchWindow() {
		MWorkbenchWindowImpl mWorkbenchWindow = new MWorkbenchWindowImpl();
		return mWorkbenchWindow;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public <P extends MPart<?>> MPerspective<P> createMPerspective() {
		MPerspectiveImpl<P> mPerspective = new MPerspectiveImpl<P>();
		return mPerspective;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MWorkbench createMWorkbench() {
		MWorkbenchImpl mWorkbench = new MWorkbenchImpl();
		return mWorkbench;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMenuItemRenderer createMMenuItemRenderer() {
		MMenuItemRendererImpl mMenuItemRenderer = new MMenuItemRendererImpl();
		return mMenuItemRenderer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolItemRenderer createMToolItemRenderer() {
		MToolItemRendererImpl mToolItemRenderer = new MToolItemRendererImpl();
		return mToolItemRenderer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WorkbenchPackage getWorkbenchPackage() {
		return (WorkbenchPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static WorkbenchPackage getPackage() {
		return WorkbenchPackage.eINSTANCE;
	}

} //WorkbenchFactoryImpl
