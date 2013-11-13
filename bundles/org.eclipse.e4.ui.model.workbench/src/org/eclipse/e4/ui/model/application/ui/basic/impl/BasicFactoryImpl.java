/**
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.basic.impl;

import org.eclipse.e4.ui.model.application.ui.basic.*;
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
			BasicFactoryImpl theBasicFactory = (BasicFactoryImpl)EPackage.Registry.INSTANCE.getEFactory(BasicPackageImpl.eNS_URI);
			if (theBasicFactory != null) {
				return theBasicFactory;
			}
		}
		catch (Exception exception) {
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
			case BasicPackageImpl.PART: return (EObject)createPart();
			case BasicPackageImpl.COMPOSITE_PART: return (EObject)createCompositePart();
			case BasicPackageImpl.INPUT_PART: return (EObject)createInputPart();
			case BasicPackageImpl.PART_STACK: return (EObject)createPartStack();
			case BasicPackageImpl.PART_SASH_CONTAINER: return (EObject)createPartSashContainer();
			case BasicPackageImpl.WINDOW: return (EObject)createWindow();
			case BasicPackageImpl.TRIMMED_WINDOW: return (EObject)createTrimmedWindow();
			case BasicPackageImpl.TRIM_BAR: return (EObject)createTrimBar();
			case BasicPackageImpl.DIALOG: return (EObject)createDialog();
			case BasicPackageImpl.WIZARD_DIALOG: return (EObject)createWizardDialog();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
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
	public MCompositePart createCompositePart() {
		CompositePartImpl compositePart = new CompositePartImpl();
		return compositePart;
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
	public MTrimmedWindow createTrimmedWindow() {
		TrimmedWindowImpl trimmedWindow = new TrimmedWindowImpl();
		return trimmedWindow;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MTrimBar createTrimBar() {
		TrimBarImpl trimBar = new TrimBarImpl();
		return trimBar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDialog createDialog() {
		DialogImpl dialog = new DialogImpl();
		return dialog;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MWizardDialog createWizardDialog() {
		WizardDialogImpl wizardDialog = new WizardDialogImpl();
		return wizardDialog;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BasicPackageImpl getBasicPackage() {
		return (BasicPackageImpl)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static BasicPackageImpl getPackage() {
		return BasicPackageImpl.eINSTANCE;
	}

} //BasicFactoryImpl
