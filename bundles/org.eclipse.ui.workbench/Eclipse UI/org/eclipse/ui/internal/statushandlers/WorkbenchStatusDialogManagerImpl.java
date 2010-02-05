/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.statushandlers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.statushandlers.StatusManager.INotificationTypes;

/**
 * <p>
 * This class is the actual dialog manager. Status dialog is a very bad thing to
 * manage, because it can switch its modality. As you know this is not possible,
 * so dialog is closed and then reopened. This approach makes impossible to keep
 * dialog data inside dialog class, because the dialog will be disposed.
 * </p>
 * <p>
 * To overcome this issue, a {@link Map} dialogState is introduced, which holds
 * the actual state (and configuration) of the dialog. This map is passed to all
 * dialog subcomponents.
 * </p>
 * <p>
 * Dialog state variables are defined in {@link IStatusDialogConstants}.
 * </p>
 * 
 * @since 3.6
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class WorkbenchStatusDialogManagerImpl {

	static final QualifiedName HINT = new QualifiedName(
			IStatusAdapterConstants.PROPERTY_PREFIX, "hint"); //$NON-NLS-1$

	private final class StatusDialogDisposeListener implements DisposeListener {

		public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
			cleanUp();
		}
	}

	private DisposeListener disposeListener = new StatusDialogDisposeListener();

	/**
	 * This field stores the real dialog that appears to the user.
	 */
	private InternalDialog dialog;

	/**
	 * This variable holds the real state of the dialog.
	 */
	private Map dialogState = new HashMap();

	/**
	 * Returns whether the given StatusAdapter object should be displayed.
	 * 
	 * @param statusAdapter
	 *            a status object
	 * @return <code>true</code> if the given status should be displayed, and
	 *         <code>false</code> otherwise
	 * @see org.eclipse.core.runtime.IStatus#matches(int)
	 */
	public boolean shouldAccept(StatusAdapter statusAdapter) {
		IStatus status = statusAdapter.getStatus();
		IStatus[] children = status.getChildren();
		int mask = ((Integer) dialogState.get(IStatusDialogConstants.MASK))
				.intValue();
		boolean handleOKStatuses = ((Boolean) dialogState
				.get(IStatusDialogConstants.HANDLE_OK_STATUSES)).booleanValue();
		if (children == null || children.length == 0) {
			return status.matches(mask) || (handleOKStatuses && status.isOK());
		}
		for (int i = 0; i < children.length; i++) {
			if (children[i].matches(mask)) {
				return true;
			}
		}
		if (handleOKStatuses && status.isOK()) {
			return true;
		}
		return false;
	}

	/**
	 * Creates workbench status dialog.
	 * 
	 * @param displayMask
	 *            the mask used to filter the handled <code>StatusAdapter</code>
	 *            objects, the mask is a logical sum of status severities
	 * @param dialogTitle
	 *            the title of the dialog. If null, than default will be used.
	 */
	public WorkbenchStatusDialogManagerImpl(int displayMask, String dialogTitle) {

		Assert
				.isNotNull(Display.getCurrent(),
						"WorkbenchStatusDialogManager must be instantiated in UI thread"); //$NON-NLS-1$

		dialogState = initDialogState(dialogState, displayMask, dialogTitle);
	}

	/**
	 * This method creates the initial state of the dialog.
	 * 
	 * @param dialogState
	 *            - the map to fill in.
	 * @param displayMask
	 *            - the mask suggesting which statuses should be displayed
	 * @param dialogTitle
	 *            - the dialog title.
	 * @return populated dialogState
	 */
	public Map initDialogState(Map dialogState, int displayMask, String dialogTitle) {
		dialogState.put(IStatusDialogConstants.MASK, new Integer(displayMask));
		dialogState.put(IStatusDialogConstants.TITLE,
				dialogTitle == null ? JFaceResources
						.getString("Problem_Occurred") : //$NON-NLS-1$
						dialogTitle);
		dialogState.put(IStatusDialogConstants.HANDLE_OK_STATUSES,
				Boolean.FALSE);

		dialogState.put(IStatusDialogConstants.SHOW_SUPPORT, Boolean.FALSE);
		dialogState.put(IStatusDialogConstants.ENABLE_DEFAULT_SUPPORT_AREA,
				Boolean.FALSE);
		dialogState.put(IStatusDialogConstants.DETAILS_OPENED, Boolean.FALSE);
		dialogState.put(IStatusDialogConstants.TRAY_OPENED, Boolean.FALSE);
		dialogState.put(IStatusDialogConstants.HIDE_SUPPORT_BUTTON,
				Boolean.FALSE);
		dialogState.put(IStatusDialogConstants.STATUS_ADAPTERS, Collections
				.synchronizedSet(new HashSet()));
		dialogState.put(IStatusDialogConstants.STATUS_MODALS, new HashMap());
		dialogState.put(IStatusDialogConstants.LABEL_PROVIDER, new LabelProviderWrapper(
				dialogState));
		dialogState.put(IStatusDialogConstants.MODALITY_SWITCH, Boolean.FALSE);
		dialogState.put(IStatusDialogConstants.ANIMATION, Boolean.TRUE);
		return dialogState;
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
		if (ErrorDialog.AUTOMATED_MODE == true) {
			return;
		}
		try {
			doAddStatusAdapter(statusAdapter, modal);
		} catch (Exception e) {
			// if dialog is open, dispose it (and all child controls)
			if (!isDialogClosed()) {
				dialog.getShell().dispose();
			}
			// reset the state
			cleanUp();
			// log original problem
			// TODO: check if is it possible to discover duplicates
			WorkbenchPlugin.log(statusAdapter.getStatus());
			// log the problem with status handling
			WorkbenchPlugin.log(e);
			e.printStackTrace();
		}
	}

	private boolean isDialogClosed() {
		return dialog == null || dialog.getShell() == null
				|| dialog.getShell().isDisposed();
	}

	private void cleanUp() {
		dialog = null;
		getErrors().clear();
		getModals().clear();
		dialogState.put(IStatusDialogConstants.DETAILS_OPENED, Boolean.FALSE);
		dialogState.put(IStatusDialogConstants.TRAY_OPENED, Boolean.FALSE);
		dialogState.put(IStatusDialogConstants.MODALITY_SWITCH, Boolean.FALSE);
	}

	private void doAddStatusAdapter(final StatusAdapter statusAdapter,
			final boolean modal) {

		if (!PlatformUI.isWorkbenchRunning()) {
			// we are shutting down, so just log
			WorkbenchPlugin.log(statusAdapter.getStatus());
			return;
		}
		
		// if statusAdapter does not match the mask, ignore it
		if (!shouldAccept(statusAdapter)) {
			return;
		}

		// Add the error in the UI thread to ensure thread safety in the
		// dialog
		if (isDialogClosed()) {

			getErrors().add(statusAdapter);
			getModals().put(statusAdapter, new Boolean(modal));
			// Delay prompting if the status adapter property is set
			if (shouldPrompt(statusAdapter)) {
				// notify all interested parties that status adapters will be
				// handled
				StatusManager.getManager().fireNotification(
						INotificationTypes.HANDLED,
						(StatusAdapter[]) getErrors()
								.toArray(new StatusAdapter[] {}));
				
				if (dialog == null) {
					setSelectedStatusAdapter(statusAdapter);
					dialog = new InternalDialog(dialogState, shouldBeModal());
					dialog.create();
					dialog.getShell().addDisposeListener(disposeListener);
					boolean showSupport = ((Boolean) dialogState
							.get(IStatusDialogConstants.SHOW_SUPPORT))
							.booleanValue();
					if (showSupport) {
						dialog.openTray();
						dialog.getShell().setLocation(
								dialog.getInitialLocation(dialog.getShell().getSize()));
					}
					dialog.open();
				}
				dialog.refresh();
				dialog.refreshDialogSize();
			}

		} else {
			StatusManager.getManager().fireNotification(
					INotificationTypes.HANDLED,
					new StatusAdapter[] { statusAdapter });
			if (statusAdapter
					.getProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY) != null) {
				statusAdapter.setProperty(
						IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY,
						Boolean.FALSE);
			}
			openStatusDialog(modal, statusAdapter);
		}
	}

	/**
	 * Gets a collection of status adapters that were passed to the dialog.
	 * 
	 * @return collection of {@link StatusAdapter} objects
	 */
	public Collection getStatusAdapters() {
		return Collections.unmodifiableCollection(getErrors());
	}

	/**
	 * Opens status dialog with particular statusAdapter selected.
	 * 
	 * @param modal
	 *            decides if window is modal or not.
	 * @param statusAdapter
	 *            status adapter to be selected.
	 */
	private void openStatusDialog(final boolean modal,
			final StatusAdapter statusAdapter) {
		getErrors().add(statusAdapter);
		getModals().put(statusAdapter, new Boolean(modal));
		boolean shouldBeModal = shouldBeModal();
		if (shouldBeModal ^ dialog.isModal()) {
			dialog.getShell().removeDisposeListener(disposeListener);
			dialogState.put(IStatusDialogConstants.MODALITY_SWITCH, Boolean.TRUE);
			dialog.close();
			dialog = new InternalDialog(dialogState, modal);
			dialog.open();
			dialog.getShell().addDisposeListener(disposeListener);
			dialogState.put(IStatusDialogConstants.MODALITY_SWITCH, Boolean.FALSE);
		}
		dialog.refresh();
	}

	/**
	 * Sets current status adapter.
	 * 
	 * @param statusAdapter
	 *            The statusAdapter to set.
	 */
	public void setSelectedStatusAdapter(StatusAdapter statusAdapter) {
		dialogState.put(IStatusDialogConstants.CURRENT_STATUS_ADAPTER,
				statusAdapter);
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
		Assert.isLegal(labelProvider != null, "Label Provider cannot be null"); //$NON-NLS-1$
		dialogState.put(IStatusDialogConstants.CUSTOM_LABEL_PROVIDER,
				labelProvider);
	}

	/**
	 * Decides if dialog should be modal. Dialog will be modal if any of the
	 * statuses contained by StatusAdapters had been reported with
	 * {@link StatusManager#BLOCK} flag.
	 * 
	 * @return true if any StatusHandler should be displayed in modal window
	 */
	public boolean shouldBeModal() {
		Map modals = (Map) dialogState
				.get(IStatusDialogConstants.STATUS_MODALS);
		for (Iterator it = modals.keySet().iterator(); it.hasNext();) {
			Object o = it.next();
			Object value = modals.get(o);
			if (value instanceof Boolean) {
				Boolean b = (Boolean) value;
				if (b.booleanValue()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the user should be prompted immediately about
	 * {@link StatusAdapter}
	 * 
	 * @param statusAdapter
	 *            to be checked.
	 * @return true if the statusAdapter should be prompted, false otherwise.
	 */
	public boolean shouldPrompt(final StatusAdapter statusAdapter) {
		Object noPromptProperty = statusAdapter
				.getProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY);

		boolean prompt = true;
		if (noPromptProperty instanceof Boolean) {
			prompt = !((Boolean) noPromptProperty).booleanValue();
		}
		return prompt;
	}

	/**
	 * Gets the shell of the managed dialog.
	 * 
	 * @return Shell or null
	 * 
	 */
	public Shell getShell() {
		if (this.dialog == null)
			return null;
		return this.dialog.getShell();
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
		dialogState.put(IStatusDialogConstants.DECORATOR, decorator);
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
	public void setProperty(Object key, Object value) {
		dialogState.put(key, value);
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
		if(key == IStatusDialogConstants.SHELL){
			return getShell();
		}
		if (key == IStatusDialogConstants.MANAGER_IMPL) {
			return this;
		}
		return dialogState.get(key);
	}

	/**
	 * This method makes the dialog to be similar to the JFace ErrorDialog. The
	 * dialog handles {@link StatusAdapter}s wrapping {@link IStatus} with
	 * severity {@link IStatus#OK}, does not display the link to the error log,
	 * does not display the link to the support area but always opens it.
	 * 
	 * @see ErrorDialog
	 * @since 3.6
	 */
	public void enableErrorDialogCompatibility(){
		setProperty(IStatusDialogConstants.ERRORLOG_LINK, Boolean.FALSE);
		setProperty(IStatusDialogConstants.HANDLE_OK_STATUSES, Boolean.TRUE);
		setProperty(IStatusDialogConstants.SHOW_SUPPORT, Boolean.TRUE);
		setProperty(IStatusDialogConstants.HIDE_SUPPORT_BUTTON, Boolean.TRUE);
	}

	/**
	 * This method is public for testing purposes only.
	 * 
	 * @return Returns the dialog.
	 */
	public InternalDialog getDialog() {
		return dialog;
	}

	/**
	 * This method is public for testing purposes only.
	 * 
	 * @param dialog
	 *            The dialog to set.
	 */
	public void setDialog(InternalDialog dialog) {
		this.dialog = dialog;
	}

	/**
	 * This method is public for testing purposes only.
	 * 
	 * @return dialog state.
	 */
	public Map getDialogState() {
		return dialogState;
	}

	/**
	 * Utility method to access StatusAdapters
	 * 
	 * @return Collection of StatusAdapters
	 */
	private Collection getErrors() {
		return (Collection) dialogState
				.get(IStatusDialogConstants.STATUS_ADAPTERS);
	}

	/**
	 * Utility method to access StatusAdapter modal flag.
	 * 
	 * @return Collection of StatusAdapter modal flag.
	 */
	private Map getModals() {
		return (Map) dialogState
				.get(IStatusDialogConstants.STATUS_MODALS);
	}
}
