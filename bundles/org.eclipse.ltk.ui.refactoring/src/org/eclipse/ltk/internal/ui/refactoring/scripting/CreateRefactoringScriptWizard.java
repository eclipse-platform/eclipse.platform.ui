/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.scripting;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.IRefactoringSerializationConstants;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryManager;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

/**
 * Wizard to create a refactoring script.
 *
 * @since 3.2
 */
public final class CreateRefactoringScriptWizard extends Wizard {

	/** The dialog settings key */
	private static String DIALOG_SETTINGS_KEY= "CreateRefactoringScriptWizard"; //$NON-NLS-1$

	/** Has the wizard new dialog settings? */
	private boolean fNewSettings;

	/** The selected refactoring descriptors, or the empty array */
	private RefactoringDescriptorProxy[] fRefactoringDescriptors= {};

	/** The refactoring history */
	private RefactoringHistory fRefactoringHistory;

	/** The refactoring script location, or <code>null</code> */
	private URI fScriptLocation= null;

	/** Should the wizard put the refactoring script to the clipboard? */
	private boolean fUseClipboard= false;

	/** The create refactoring script wizard page */
	private final CreateRefactoringScriptWizardPage fWizardPage;

	/**
	 * Creates a new create refactoring script wizard.
	 */
	public CreateRefactoringScriptWizard() {
		setNeedsProgressMonitor(false);
		setWindowTitle(ScriptingMessages.CreateRefactoringScriptWizard_caption);
		setDefaultPageImageDescriptor(RefactoringPluginImages.DESC_WIZBAN_CREATE_SCRIPT);
		final IDialogSettings settings= RefactoringUIPlugin.getDefault().getDialogSettings();
		final IDialogSettings section= settings.getSection(DIALOG_SETTINGS_KEY);
		if (section == null)
			fNewSettings= true;
		else {
			fNewSettings= false;
			setDialogSettings(section);
		}
		fWizardPage= new CreateRefactoringScriptWizardPage(this);
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(fWizardPage);
	}

	@Override
	public boolean canFinish() {
		return (fUseClipboard || fScriptLocation != null) && fRefactoringDescriptors.length > 0;
	}

	/**
	 * Returns the refactoring history to create a script from.
	 *
	 * @return the refactoring history.
	 */
	public RefactoringHistory getRefactoringHistory() {
		return fRefactoringHistory;
	}

	/**
	 * Performs the actual refactoring script export.
	 *
	 * @return <code>true</code> if the wizard can be finished,
	 *         <code>false</code> otherwise
	 */
	private boolean performExport() {
		RefactoringDescriptorProxy[] writable= fRefactoringDescriptors;
		if (fScriptLocation != null) {
			final File file= new File(fScriptLocation);
			if (file.exists()) {
				final MessageDialog message= new MessageDialog(getShell(), getShell().getText(), null, Messages.format(ScriptingMessages.CreateRefactoringScriptWizard_overwrite_query, new String[] { ScriptingMessages.CreateRefactoringScriptWizard_merge_button, ScriptingMessages.CreateRefactoringScriptWizard_overwrite_button}), MessageDialog.QUESTION, new String[] { ScriptingMessages.CreateRefactoringScriptWizard_merge_button, ScriptingMessages.CreateRefactoringScriptWizard_overwrite_button, IDialogConstants.CANCEL_LABEL}, 0);
				final int result= message.open();
				if (result == 0) {
					try (InputStream stream= new BufferedInputStream(new FileInputStream(file))) {
						final RefactoringDescriptorProxy[] existing= RefactoringCore.getHistoryService().readRefactoringHistory(stream, RefactoringDescriptor.NONE).getDescriptors();
						final Set<RefactoringDescriptorProxy> set= new HashSet<>(Arrays.asList(existing));
						set.addAll(Arrays.asList(fRefactoringDescriptors));
						writable= new RefactoringDescriptorProxy[set.size()];
						set.toArray(writable);
					} catch (FileNotFoundException exception) {
						MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, exception.getLocalizedMessage());
						return false;
					} catch (CoreException exception) {
						handleCoreException(exception);
						return false;
					} catch (IOException closeException) {
						// Do nothing
					}
				} else if (result == 2)
					return false;
			}
			try {
				File parentFile= file.getParentFile();
				if (parentFile != null)
					parentFile.mkdirs();
				try (OutputStream stream= new BufferedOutputStream(new FileOutputStream(file))) {
					writeRefactoringDescriptorProxies(writable, stream);
				}
				return true;
			} catch (CoreException exception) {
				handleCoreException(exception);
				return false;
			} catch (FileNotFoundException exception) {
				MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, exception.getLocalizedMessage());
				return false;
			} catch (IOException closeException) {
				// Do nothing
			}
		} else if (fUseClipboard) {
			try {
				final ByteArrayOutputStream stream= new ByteArrayOutputStream(2048);
				writeRefactoringDescriptorProxies(writable, stream);
				try {
					final String string= stream.toString(IRefactoringSerializationConstants.OUTPUT_ENCODING);
					Clipboard clipboard= null;
					try {
						clipboard= new Clipboard(getShell().getDisplay());
						try {
							clipboard.setContents(new Object[] { string}, new Transfer[] { TextTransfer.getInstance()});
							return true;
						} catch (SWTError error) {
							MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, error.getLocalizedMessage());
							return false;
						}
					} finally {
						if (clipboard != null)
							clipboard.dispose();
					}
				} catch (UnsupportedEncodingException exception) {
					// Does not happen
					return false;
				}
			} catch (CoreException exception) {
				handleCoreException(exception);
				return false;
			}
		}
		return false;
	}

	private void handleCoreException(CoreException exception) {
		IStatus status= exception.getStatus();
		final Throwable throwable= status.getException();
		if (throwable instanceof IOException) {
			MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, throwable.getLocalizedMessage());
		} else {
			MessageDialog.openError(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, status.getMessage());
			RefactoringUIPlugin.log(exception);
		}
	}

	@Override
	public boolean performFinish() {
		if (fNewSettings) {
			final IDialogSettings settings= RefactoringUIPlugin.getDefault().getDialogSettings();
			IDialogSettings section= settings.getSection(DIALOG_SETTINGS_KEY);
			section= settings.addNewSection(DIALOG_SETTINGS_KEY);
			setDialogSettings(section);
		}
		fWizardPage.performFinish();
		return performExport();
	}

	/**
	 * Sets the selected refactoring descriptors.
	 *
	 * @param proxies
	 *            the selected refactoring descriptors
	 */
	public void setRefactoringDescriptors(final RefactoringDescriptorProxy[] proxies) {
		Assert.isNotNull(proxies);
		fRefactoringDescriptors= proxies;
		final IWizardContainer wizard= getContainer();
		if (wizard.getCurrentPage() != null)
			wizard.updateButtons();
	}

	/**
	 * Sets the refactoring history to use.
	 *
	 * @param history
	 *            the refactoring history to use
	 */
	public void setRefactoringHistory(final RefactoringHistory history) {
		Assert.isNotNull(history);
		fRefactoringHistory= history;
	}

	/**
	 * Sets the refactoring script location.
	 *
	 * @param location
	 *            the refactoring script location, or <code>null</code>
	 */
	public void setRefactoringScript(final URI location) {
		fScriptLocation= location;
		final IWizardContainer wizard= getContainer();
		if (wizard.getCurrentPage() != null)
			wizard.updateButtons();
	}

	/**
	 * Determines whether the wizard should save the refactoring script to the
	 * clipboard.
	 *
	 * @param clipboard
	 *            <code>true</code> to save the script to clipboard,
	 *            <code>false</code> otherwise
	 */
	public void setUseClipboard(final boolean clipboard) {
		fUseClipboard= clipboard;
		final IWizardContainer wizard= getContainer();
		if (wizard.getCurrentPage() != null)
			wizard.updateButtons();
	}

	/**
	 * Writes the refactoring descriptor proxies to the specified output stream.
	 *
	 * @param writable
	 *            the refactoring descriptor proxies
	 * @param stream
	 *            the output stream to write to
	 * @throws CoreException
	 *             if an error occurs
	 */
	private void writeRefactoringDescriptorProxies(final RefactoringDescriptorProxy[] writable, final OutputStream stream) throws CoreException {
		RefactoringHistoryManager.sortRefactoringDescriptorsAscending(writable);
		try {
			getContainer().run(false, false, monitor -> {
				try {
					RefactoringCore.getHistoryService().writeRefactoringDescriptors(writable, stream, RefactoringDescriptor.NONE, false, monitor);
				} catch (CoreException exception) {
					throw new InvocationTargetException(exception);
				}
			});
		} catch (InvocationTargetException exception) {
			final Throwable throwable= exception.getTargetException();
			if (throwable instanceof CoreException) {
				final CoreException extended= (CoreException) throwable;
				throw extended;
			} else {
				throw new CoreException(new Status(IStatus.ERROR, RefactoringCore.ID_PLUGIN, ScriptingMessages.CreateRefactoringScriptWizard_unexpected_exception, throwable));
			}
		} catch (InterruptedException exception) {
			// Do nothing
		}
	}
}
