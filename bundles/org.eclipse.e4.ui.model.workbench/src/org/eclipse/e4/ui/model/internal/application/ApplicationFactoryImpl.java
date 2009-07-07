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
 * $Id: ApplicationFactoryImpl.java,v 1.6 2009/07/07 14:27:31 emoffatt Exp $
 */
package org.eclipse.e4.ui.model.internal.application;

import org.eclipse.core.commands.ParameterizedCommand;
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
public class ApplicationFactoryImpl extends EFactoryImpl implements ApplicationFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ApplicationFactory init() {
		try {
			ApplicationFactory theApplicationFactory = (ApplicationFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.eclipse.org/ui/2008/Application"); //$NON-NLS-1$ 
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
			case ApplicationPackage.MAPPLICATION: return createMApplication();
			case ApplicationPackage.MPART: return createMPart();
			case ApplicationPackage.MSTACK: return createMStack();
			case ApplicationPackage.MSASH_FORM: return createMSashForm();
			case ApplicationPackage.MCONTRIBUTED_PART: return createMContributedPart();
			case ApplicationPackage.MHANDLER: return createMHandler();
			case ApplicationPackage.MHANDLED_ITEM: return createMHandledItem();
			case ApplicationPackage.MMENU_ITEM: return createMMenuItem();
			case ApplicationPackage.MTOOL_BAR_ITEM: return createMToolBarItem();
			case ApplicationPackage.MMENU: return createMMenu();
			case ApplicationPackage.MTOOL_BAR: return createMToolBar();
			case ApplicationPackage.MTRIMMED_PART: return createMTrimmedPart();
			case ApplicationPackage.MITEM_PART: return createMItemPart();
			case ApplicationPackage.MWINDOW: return createMWindow();
			case ApplicationPackage.MCOMMAND: return createMCommand();
			case ApplicationPackage.MTOOL_BAR_CONTAINER: return createMToolBarContainer();
			case ApplicationPackage.MPARAMETER: return createMParameter();
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
			case ApplicationPackage.PARAMETERIZED_COMMAND:
				return createParameterizedCommandFromString(eDataType, initialValue);
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
			case ApplicationPackage.PARAMETERIZED_COMMAND:
				return convertParameterizedCommandToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public <W extends MWindow<?>> MApplication<W> createMApplication() {
		MApplicationImpl<W> mApplication = new MApplicationImpl<W>();
		return mApplication;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public <P extends MPart<?>> MPart<P> createMPart() {
		MPartImpl<P> mPart = new MPartImpl<P>();
		return mPart;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MStack createMStack() {
		MStackImpl mStack = new MStackImpl();
		return mStack;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public <P extends MPart<?>> MSashForm<P> createMSashForm() {
		MSashFormImpl<P> mSashForm = new MSashFormImpl<P>();
		return mSashForm;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public <P extends MPart<?>> MContributedPart<P> createMContributedPart() {
		MContributedPartImpl<P> mContributedPart = new MContributedPartImpl<P>();
		return mContributedPart;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MHandler createMHandler() {
		MHandlerImpl mHandler = new MHandlerImpl();
		return mHandler;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MHandledItem createMHandledItem() {
		MHandledItemImpl mHandledItem = new MHandledItemImpl();
		return mHandledItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMenuItem createMMenuItem() {
		MMenuItemImpl mMenuItem = new MMenuItemImpl();
		return mMenuItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBarItem createMToolBarItem() {
		MToolBarItemImpl mToolBarItem = new MToolBarItemImpl();
		return mToolBarItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMenu createMMenu() {
		MMenuImpl mMenu = new MMenuImpl();
		return mMenu;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBar createMToolBar() {
		MToolBarImpl mToolBar = new MToolBarImpl();
		return mToolBar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public <P extends MPart<?>> MTrimmedPart<P> createMTrimmedPart() {
		MTrimmedPartImpl<P> mTrimmedPart = new MTrimmedPartImpl<P>();
		return mTrimmedPart;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public <P extends MPart<?>> MItemPart<P> createMItemPart() {
		MItemPartImpl<P> mItemPart = new MItemPartImpl<P>();
		return mItemPart;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public <P extends MPart<?>> MWindow<P> createMWindow() {
		MWindowImpl<P> mWindow = new MWindowImpl<P>();
		return mWindow;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MCommand createMCommand() {
		MCommandImpl mCommand = new MCommandImpl();
		return mCommand;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBarContainer createMToolBarContainer() {
		MToolBarContainerImpl mToolBarContainer = new MToolBarContainerImpl();
		return mToolBarContainer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MParameter createMParameter() {
		MParameterImpl mParameter = new MParameterImpl();
		return mParameter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ParameterizedCommand createParameterizedCommandFromString(EDataType eDataType, String initialValue) {
		return (ParameterizedCommand)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertParameterizedCommandToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ApplicationPackage getApplicationPackage() {
		return (ApplicationPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ApplicationPackage getPackage() {
		return ApplicationPackage.eINSTANCE;
	}

} //ApplicationFactoryImpl
