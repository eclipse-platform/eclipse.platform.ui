/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Red Hat, Inc - Extracted methods from WizardArchiveFileResourceImportPage1
 *******************************************************************************/

package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.IOException;
import java.util.zip.ZipFile;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * @since 3.1
 */
public class ArchiveFileManipulations {


	/**
	 * Determine whether the file with the given filename is in .tar.gz or .tar
	 * format.
	 *
	 * @param fileName
	 *            file to test
	 * @return true if the file is in tar format
	 */
	public static boolean isTarFile(String fileName) {
		if (fileName.isEmpty()) {
			return false;
		}

		try (TarFile tarFile = new TarFile(fileName)) {
		} catch (TarException | IOException ioException) {
			return false;
		}

		return true;
	}

	/**
	 * Determine whether the file with the given filename is in .zip or .jar
	 * format.
	 *
	 * @param fileName
	 *            file to test
	 * @return true if the file is in tar format
	 */
	public static boolean isZipFile(String fileName) {
		if (fileName.isEmpty()) {
			return false;
		}

		try (ZipFile zipFile = new ZipFile(fileName)) {
			return true;
		} catch (IOException ioException) {
			return false;
		}

	}

	/**
	 * Closes the given structure provider.
	 *
	 * @param structureProvider
	 *            The structure provider to be closed, can be <code>null</code>
	 * @param shell
	 *            The shell to display any possible Dialogs in
	 */
	@SuppressWarnings("resource")
	public static void closeStructureProvider(ILeveledImportStructureProvider structureProvider, Shell shell) {
		if (structureProvider instanceof ZipLeveledStructureProvider) {
			closeZipFile(((ZipLeveledStructureProvider) structureProvider).getZipFile(), shell);
		}
		if (structureProvider instanceof TarLeveledStructureProvider) {
			closeTarFile(((TarLeveledStructureProvider) structureProvider).getTarFile(), shell);
		}
	}

	/**
	 * Attempts to close the passed zip file, and answers a boolean indicating
	 * success.
	 *
	 * @param file
	 *            The zip file to attempt to close
	 * @param shell
	 *            The shell to display error dialogs in
	 * @return Returns true if the operation was successful
	 */
	public static boolean closeZipFile(ZipFile file, Shell shell) {
		try {
			file.close();
		} catch (IOException e) {
			displayErrorDialog(
					NLS.bind(DataTransferMessages.ZipImport_couldNotClose, file.getName()),
					shell);
			return false;
		}

		return true;
	}

	/**
	 * Attempts to close the passed tar file, and answers a boolean indicating
	 * success.
	 *
	 * @param file
	 *            The tar file to attempt to close
	 * @param shell
	 *            The shell to display error dialogs in
	 * @return Returns true if the operation was successful
	 * @since 3.4
	 */
	public static boolean closeTarFile(TarFile file, Shell shell) {
		try {
			file.close();
		} catch (IOException e) {
			displayErrorDialog(
					NLS.bind(DataTransferMessages.ZipImport_couldNotClose, file.getName()),
					shell);
			return false;
		}

		return true;
	}

	/**
	 * Display an error dialog with the specified message.
	 *
	 * @param message
	 *            the error message
	 */
	protected static void displayErrorDialog(String message, Shell shell) {
		MessageDialog.open(MessageDialog.ERROR, shell, getErrorDialogTitle(), message, SWT.SHEET);
	}

	/**
	 * Get the title for an error dialog. Subclasses should override.
	 */
	protected static String getErrorDialogTitle() {
		return IDEWorkbenchMessages.WizardExportPage_internalErrorTitle;
	}
}
