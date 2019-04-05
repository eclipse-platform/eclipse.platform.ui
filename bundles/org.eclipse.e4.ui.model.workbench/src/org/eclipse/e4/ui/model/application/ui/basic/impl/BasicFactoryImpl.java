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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.model.application.ui.basic.MInputPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWizardDialog;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
@SuppressWarnings("deprecation")
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
		case BasicPackageImpl.INPUT_PART:
			return (EObject) createInputPart();
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
		case BasicPackageImpl.DIALOG:
			return (EObject) createDialog();
		case BasicPackageImpl.WIZARD_DIALOG:
			return (EObject) createWizardDialog();
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
		PartImpl part = new PartImpl();
		return part;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 */
	@Override
	public MCompositePart createCompositePart() {
		CompositePartImpl compositePart = new CompositePartImpl();
		return compositePart;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @deprecated See {@link MInputPart model documentation} for details.
	 * @noreference See {@link MInputPart model documentation} for details.
	 * @generated
	 */
	@Deprecated
	@Override
	public MInputPart createInputPart() {
		InputPartImpl inputPart = new InputPartImpl();
		return inputPart;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public MPartStack createPartStack() {
		PartStackImpl partStack = new PartStackImpl();
		return partStack;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public MPartSashContainer createPartSashContainer() {
		PartSashContainerImpl partSashContainer = new PartSashContainerImpl();
		return partSashContainer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public MWindow createWindow() {
		WindowImpl window = new WindowImpl();
		return window;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public MTrimmedWindow createTrimmedWindow() {
		TrimmedWindowImpl trimmedWindow = new TrimmedWindowImpl();
		return trimmedWindow;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public MTrimBar createTrimBar() {
		TrimBarImpl trimBar = new TrimBarImpl();
		return trimBar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @deprecated
	 * @noreference See {@link MDialog model documentation} for details.
	 * @generated  NOT
	 */
	@Deprecated
	@Override
	public MDialog createDialog() {
		DialogImpl dialog = new DialogImpl();
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Status s = new Status(IStatus.ERROR, bundle.getSymbolicName(), // $NON-NLS-1$
				"Your application model still contains a deprecated Dialog instance. Please remove it to keep compatibility with future versions."); //$NON-NLS-1$
		Platform.getLog(bundle).log(s);
		return dialog;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @deprecated
	 * @noreference See {@link MWizardDialog model documentation} for details.
	 * @generated NOT
	 */
	@Deprecated
	@Override
	public MWizardDialog createWizardDialog() {
		WizardDialogImpl wizardDialog = new WizardDialogImpl();
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		Status s = new Status(IStatus.ERROR, bundle.getSymbolicName(), // $NON-NLS-1$
				"Your application model still contains a deprecated WizardDialog instance. Please remove it to keep compatibility with future versions."); //$NON-NLS-1$
		Platform.getLog(bundle).log(s);
		return wizardDialog;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BasicPackageImpl getBasicPackage() {
		return (BasicPackageImpl) getEPackage();
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
