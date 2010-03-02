/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.statushandlers;

import org.eclipse.ui.internal.statushandlers.IStatusDialogConstants;

import java.util.Collection;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ErrorSupportProvider;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.statushandlers.WorkbenchStatusDialogManagerImpl;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * <p>
 * The {@link WorkbenchStatusDialogManager} is a utility class for displaying
 * one or more messages (errors, warnings or infos) to the user. The dialog
 * supplied has a Details button that opens/closes the details area. The default
 * {@link AbstractStatusAreaProvider} displays a tree of {@link StatusAdapter}s
 * related to the selected item on the messages list. The dialog also hasa
 * Support button that opens/closes the support area which contains the provided
 * {@link AbstractStatusAreaProvider}. The Support button is disabled and not
 * visible unless
 * {@link WorkbenchStatusDialogManager#enableDefaultSupportArea(boolean)} is
 * invoked.
 * </p>
 * 
 * <p>
 * The default details area can be replaced using
 * {@link WorkbenchStatusDialogManager#setDetailsAreaProvider(AbstractStatusAreaProvider)}
 * </p>
 * 
 * <p>
 * The default support area can be replaced using
 * {@link WorkbenchStatusDialogManager#setSupportAreaProvider(AbstractStatusAreaProvider)}
 * or {@link Policy#setErrorSupportProvider(ErrorSupportProvider)}.
 * </p>
 * 
 * <p>
 * The manager can switch from a non-modal dialog to a modal dialog. See
 * {@link #addStatusAdapter(StatusAdapter, boolean)}
 * </p>
 * 
 * @see Policy#setErrorSupportProvider(ErrorSupportProvider)
 * @see ErrorSupportProvider
 * @see AbstractStatusAreaProvider
 * 
 * @since 3.4
 * @noextend This class is not intended to be subclassed by clients.
 */
public class WorkbenchStatusDialogManager {
	
	static final QualifiedName HINT = new QualifiedName(
			IStatusAdapterConstants.PROPERTY_PREFIX, "hint"); //$NON-NLS-1$

	private WorkbenchStatusDialogManagerImpl manager;

	/**
	 * Creates workbench status dialog.
	 * 
	 * @param displayMask
	 *            the mask used to filter the handled <code>StatusAdapter</code>
	 *            objects, the mask is a logical sum of status severities
	 * @param dialogTitle
	 *            the title of the dialog. If null, than default will be used.
	 */
	
	public WorkbenchStatusDialogManager(int displayMask, String dialogTitle) {
		manager = new WorkbenchStatusDialogManagerImpl(displayMask, dialogTitle);
	}
	
	/**
	 * Creates workbench status dialog.
	 * 
	 * @param parentShell
	 *            the parent shell for the dialog. It may be null.
	 * @param displayMask
	 *            the mask used to filter the handled <code>StatusAdapter</code>
	 *            objects, the mask is a logical sum of status severities
	 * @param dialogTitle
	 *            the title of the dialog. If null, than default will be used.
	 * @deprecated As of 3.4 the <code>parentShell<code> is ignored
	 * @see #WorkbenchStatusDialogManager(int, String)
	 */
	public WorkbenchStatusDialogManager(Shell parentShell, int displayMask,
			String dialogTitle) {
		this(displayMask, dialogTitle);
	}

	/**
	 * Creates workbench status dialog.
	 * 
	 * @param dialogTitle
	 *            the title of the dialog. If null, than default will be used.
	 */
	public WorkbenchStatusDialogManager(String dialogTitle) {
		this(IStatus.INFO | IStatus.WARNING | IStatus.ERROR | IStatus.CANCEL,
				dialogTitle);
	}

	/**
	 * Creates workbench status dialog.
	 * 
	 * @param parentShell
	 *            the parent shell for the dialog. It may be null.
	 * @param dialogTitle
	 *            the title of the dialog. If null, than default will be used.
	 * @deprecated As of 3.4 the <code>parentShell<code> is ignored
	 * @see #WorkbenchStatusDialogManager(String)
	 */
	public WorkbenchStatusDialogManager(Shell parentShell, String dialogTitle) {
		this(dialogTitle);
	}

	/**
	 * <p>
	 * Adds a new {@link StatusAdapter} to the status adapters list in the
	 * dialog.
	 * </p>
	 * <p>
	 * If the dialog is already visible, the status adapter will be shown
	 * immediately. Otherwise, the dialog with the added status adapter will
	 * show up, if all conditions below are false.
	 * <ul>
	 * <li>the status adapter has
	 * {@link IProgressConstants#NO_IMMEDIATE_ERROR_PROMPT_PROPERTY} set to true</li>
	 * </ul>
	 * </p>
	 * <p>
	 * All not shown status adapters will be displayed as soon as the dialog
	 * shows up.
	 * </p>
	 * 
	 * @param modal
	 *            <code>true</code> if the dialog should be modal,
	 *            <code>false</code> otherwise
	 * @param statusAdapter
	 *            the status adapter
	 */
	public void addStatusAdapter(final StatusAdapter statusAdapter,
			final boolean modal) {
		manager.addStatusAdapter(statusAdapter, modal);
	}

	/**
	 * Enables the default support area that shows stack trace of the exception
	 * contained in the selected status.
	 * 
	 * @param enable
	 *            true enables, false disables default support
	 */
	public void enableDefaultSupportArea(boolean enable) {
		manager.getDialogState().put(
				IStatusDialogConstants.ENABLE_DEFAULT_SUPPORT_AREA,
				Boolean.valueOf(enable));
	}

	/**
	 * Gets a collection of status adapters that were passed to the dialog.
	 * 
	 * @return collection of {@link StatusAdapter} objects
	 */
	public Collection getStatusAdapters() {
		return manager.getStatusAdapters();
	}

	/**
	 * Sets the details area provider. If null is set, the default area provider
	 * will be used.
	 * 
	 * @param provider
	 *            A details area provider to be set.
	 */
	public void setDetailsAreaProvider(AbstractStatusAreaProvider provider) {
		manager.setProperty(IStatusDialogConstants.CUSTOM_DETAILS_PROVIDER,
				provider);
	}

	/**
	 * Sets new label provider for the status list. This label provider is used
	 * also to display the second message on the dialog if only one status is
	 * available.
	 * 
	 * <p>
	 * This method is no longer recommended to use as it is impossible to
	 * achieve consistent behavior after changing only one label provider.
	 * </p>
	 * 
	 * @deprecated As of 3.5 {@link #setMessageDecorator} is recommended. 
	 * 
	 * @param labelProvider
	 *            a label provider to be used when displaying status adapters.
	 */
	public void setStatusListLabelProvider(ITableLabelProvider labelProvider) {
		manager.setStatusListLabelProvider(labelProvider);
	}

	/**
	 * Sets the support provider.
	 * 
	 * The policy for choosing support provider is:
	 * <ol>
	 * <li>use the support provider set by this method, if set</li>
	 * <li>use the support provider set in JFace Policy, if set</li>
	 * <li>use the default support area, if enabled</li>
	 * </ol>
	 * 
	 * @param provider
	 *            Support provider to be set.
	 */
	public void setSupportAreaProvider(AbstractStatusAreaProvider provider) {
		manager.setProperty(IStatusDialogConstants.CUSTOM_SUPPORT_PROVIDER,
				provider);
	}

	/**
	 * <p>
	 * This methods sets up the decorator, which is used to modify displayed
	 * strings extracted from StatusAdapter. The decorator should be used to
	 * remove technical codes from the dialog, f.e. following message
	 * "<i>ERR2008 Invalid password</i>" can be translated into
	 * "<i>Invalid password</i>".
	 * </p>
	 * <p>
	 * The decorator will be applied only to messages extracted from
	 * StatusAdapter (predefined messages like
	 * "This status has children statuses. See 'Details' for more information."
	 * are not affected.
	 * </p>
	 * <p>
	 * This method should not be used together with
	 * {@link #setStatusListLabelProvider(ITableLabelProvider)}.
	 * </p>
	 * 
	 * @param decorator
	 *            - the decorator to be set. Only
	 *            {@link ILabelDecorator#decorateText(String, Object)} method
	 *            will be used. This method should return <code>null</code> if
	 *            and only if the first argument is null. StatusAdapter is
	 *            passed as second parameter. Other methods should have default
	 *            behavior as they may be used in future versions of the dialog.
	 * @since 3.5
	 */
	public void setMessageDecorator(ILabelDecorator decorator){
		manager.setMessageDecorator(decorator);
	}
	
	/**
	 * This method sets various properties on the manager.
	 * 
	 * @param key
	 *            a key of the property to be set.
	 * @param value
	 *            a value of the property to be set. The value must be of type
	 *            specified by the property key. <code>null</code> should never
	 *            be passed unless the property key javadoc allows for that.
	 * @since 3.5
	 */
	public void setProperty(Object key, Object value){
		manager.setProperty(key, value);
	}

	/**
	 * This method gets various dialog properties.
	 * 
	 * @param key
	 *            a key of the property to be get.
	 * @return a value of the property. The value will be of type specified by
	 *         the property key. <code>null</code> can be returned.
	 * @since 3.5
	 */
	public Object getProperty(Object key){
		return manager.getProperty(key);
	}
	
	
	/**
	 * This method makes the dialog to be similar to the JFace ErrorDialog. The
	 * dialog handles {@link StatusAdapter}s wrapping {@link IStatus} with
	 * severity {@link IStatus#OK}, does not display the link to the error log,
	 * does not display the link to the support area but always opens it.
	 * 
	 * @see ErrorDialog
	 * @since 3.5
	 */
	public void enableErrorDialogCompatibility(){
		manager.enableErrorDialogCompatibility();
	}
}
