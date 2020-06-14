/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.basic.impl;

import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.emf.ecore.EClass;
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
public class BasicFactoryImpl extends EFactoryImpl implements MBasicFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final BasicFactoryImpl eINSTANCE = init();

	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static BasicFactoryImpl init() {
		try {
			BasicFactoryImpl theBasicFactory = (BasicFactoryImpl) EPackage.Registry.INSTANCE
					.getEFactory(BasicPackageImpl.eNS_URI);
			if (theBasicFactory != null) {
				return theBasicFactory;
			}
		} catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new BasicFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BasicFactoryImpl() {
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
		case BasicPackageImpl.PART:
			return (EObject) createPart();
		case BasicPackageImpl.COMPOSITE_PART:
			return (EObject) createCompositePart();
		case BasicPackageImpl.PART_STACK:
			return (EObject) createPartStack();
		case BasicPackageImpl.PART_SASH_CONTAINER:
			return (EObject) createPartSashContainer();
		case BasicPackageImpl.WINDOW:
			return (EObject) createWindow();
		case BasicPackageImpl.TRIMMED_WINDOW:
			return (EObject) createTrimmedWindow();
		case BasicPackageImpl.TRIM_BAR:
			return (EObject) createTrimBar();
		default:
			throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public MPart createPart() {
		return new PartImpl();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 */
	@Override
	public MCompositePart createCompositePart() {
		return new CompositePartImpl();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public MPartStack createPartStack() {
		return new PartStackImpl();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public MPartSashContainer createPartSashContainer() {
		return new PartSashContainerImpl();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public MWindow createWindow() {
		return new WindowImpl();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public MTrimmedWindow createTrimmedWindow() {
		return new TrimmedWindowImpl();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public MTrimBar createTrimBar() {
		return new TrimBarImpl();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BasicPackageImpl getBasicPackage() {
		return (BasicPackageImpl) getEPackage();
	}

} //BasicFactoryImpl
