/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.actions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;

/**
 * Launch the LTK aware resource operations ... but sneaky!
 * 
 * @since 3.4
 */
public class LTKLauncher {
	/**
	 * The LTK UI plugin ID.
	 */
	public static final String LTK_UI_ID = "org.eclipse.ltk.ui.refactoring"; //$NON-NLS-1$

	private static final String OPEN_OPERATION_CLASS = "org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation"; //$NON-NLS-1$
	private static final String OPEN_OPERATION_RUN_METHOD = "run"; //$NON-NLS-1$
	private static final String REFACTOR_WIZARD_CLASS = "org.eclipse.ltk.ui.refactoring.RefactoringWizard"; //$NON-NLS-1$

	private static final String DELETE_WIZARD_CLASS = "org.eclipse.ltk.ui.refactoring.resource.DeleteResourcesWizard"; //$NON-NLS-1$
	private static final String MOVE_WIZARD_CLASS = "org.eclipse.ltk.ui.refactoring.resource.MoveResourcesWizard"; //$NON-NLS-1$
	private static final String RENAME_WIZARD_CLASS = "org.eclipse.ltk.ui.refactoring.resource.RenameResourceWizard"; //$NON-NLS-1$

	private static Constructor deleteWizardConstructor;
	private static Constructor moveWizardConstructor;
	private static Constructor renameWizardConstructor;

	private static Class openOperationClass;
	private static Constructor openOperationConstructor;
	private static Method runOpenOperation;

	/**
	 * Open the LTK delete resources wizard if available.
	 * 
	 * @param shell
	 *            the parent shell for the wizard
	 * @param title
	 *            a title for the wizard
	 * @param resources
	 *            the resources to delete.
	 * @return <code>true</code> if we can launch the wizard
	 */
	public static boolean openDeleteWizard(final Shell shell,
			final String title, final IResource[] resources) {
		if (!init()) {
			return false;
		}
		Object deleteWizard;
		try {
			deleteWizard = deleteWizardConstructor
					.newInstance(new Object[] { resources });
			runOpenOperation(shell, title, deleteWizard);
			return true;
		} catch (IllegalArgumentException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		return false;
	}

	/**
	 * Open the LTK move resources wizard if available.
	 * 
	 * @param shell
	 *            the parent shell for the wizard
	 * @param title
	 *            a title for the wizard
	 * @param resources
	 *            the resources to delete.
	 * @return <code>true</code> if we can launch the wizard
	 */
	public static boolean openMoveWizard(final Shell shell, final String title,
			final IResource[] resources) {
		if (!init()) {
			return false;
		}
		Object moveWizard;
		try {
			moveWizard = moveWizardConstructor
					.newInstance(new Object[] { resources });
			runOpenOperation(shell, title, moveWizard);
			return true;
		} catch (IllegalArgumentException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		return false;
	}

	/**
	 * Open the LTK rename resource wizard if available.
	 * 
	 * @param shell
	 *            the parent shell for the wizard
	 * @param title
	 *            a title for the wizard
	 * @param resource
	 *            the resource to rename.
	 * @return <code>true</code> if we can launch the wizard
	 */
	public static boolean openRenameWizard(final Shell shell,
			final String title, final IResource resource) {
		if (!init()) {
			return false;
		}
		Object renameWizard;
		try {
			renameWizard = renameWizardConstructor
					.newInstance(new Object[] { resource });
			runOpenOperation(shell, title, renameWizard);
			return true;
		} catch (IllegalArgumentException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		return false;
	}

	private static void runOpenOperation(final Shell shell, final String title,
			Object wizard) throws IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Object openOperation = openOperationConstructor
				.newInstance(new Object[] { wizard });
		runOpenOperation.invoke(openOperation, new Object[] { shell, title });
	}

	private static boolean init() {
		if (openOperationClass == null) {
			Bundle bundle = Platform.getBundle(LTK_UI_ID);
			if (bundle == null) {
				return false;
			}

			try {
				openOperationClass = bundle.loadClass(OPEN_OPERATION_CLASS);
				Class refactorWizardClass = bundle
						.loadClass(REFACTOR_WIZARD_CLASS);
				openOperationConstructor = openOperationClass
						.getDeclaredConstructor(new Class[] { refactorWizardClass });
				runOpenOperation = openOperationClass.getDeclaredMethod(
						OPEN_OPERATION_RUN_METHOD, new Class[] { Shell.class,
								String.class });
				Class wizardClass = bundle.loadClass(DELETE_WIZARD_CLASS);
				deleteWizardConstructor = wizardClass
						.getDeclaredConstructor(new Class[] { IResource[].class });
				wizardClass = bundle.loadClass(MOVE_WIZARD_CLASS);
				moveWizardConstructor = wizardClass
						.getDeclaredConstructor(new Class[] { IResource[].class });
				wizardClass = bundle.loadClass(RENAME_WIZARD_CLASS);
				renameWizardConstructor = wizardClass
						.getDeclaredConstructor(new Class[] { IResource.class });

			} catch (ClassNotFoundException e) {
			} catch (SecurityException e) {
			} catch (NoSuchMethodException e) {
			} finally {
				if (renameWizardConstructor == null) {
					clear();
				}
			}
		}
		return openOperationClass != null;
	}

	/**
	 * Release the reflected classes.
	 */
	public static void clear() {
		moveWizardConstructor = null;
		deleteWizardConstructor = null;
		runOpenOperation = null;
		openOperationClass = null;
		openOperationConstructor = null;
	}
}
