/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.dialogs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * <p>
 * A message dialog which also allows the user to adjust a toggle setting. If a
 * preference store is provided and the user selects the toggle, then the user's
 * answer (yes/ok or no) will be persisted in the store. If no store is
 * provided, then this information can be queried after the dialog closes.
 * </p>
 * <p>
 * This type of dialog should be used whenever you want to user to be able to
 * avoid being prompted in the future. It is <strong>strongly </strong>
 * recommended that a cancel option be provided, so that the user has the option
 * of making the decision at a later point in time. The semantic for a cancel
 * button should be to cancel the operation (if it has not yet started), or stop
 * the operation (if it has already started).
 * </p>
 * <p>
 * It is the reponsibility of the developer to provide a mechanism for the user
 * to change this preference at some later point in time (e.g., through a
 * preference page).
 * </p>
 * 
 * @since 3.0
 */
public class MessageDialogWithToggle extends MessageDialog {

    /**
     * The value of the preference when the user has asked that the answer to
     * the question always be "okay" or "yes".
     */
    public static final String ALWAYS = "always"; //$NON-NLS-1$

    /**
     * The value of the preference when the user has asked that the answer to
     * the question always be "no".
     */
    public static final String NEVER = "never"; //$NON-NLS-1$

    /**
     * The value of the preference when the user wishes to prompted for an
     * answer every time the question is to be asked.
     */
    public static final String PROMPT = "prompt"; //$NON-NLS-1$

    /**
     * Convenience method to open a standard error dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     * @param toggleMessage
     *            the message for the toggle control, or <code>null</code> for
     *            the default message
     * @param toggleState
     *            the initial state for the toggle
     * @param store
     *            the IPreference store in which the user's preference should be
     *            persisted; <code>null</code> if you don't want it persisted
     *            automatically.
     * @param key
     *            the key to use when persisting the user's preference;
     *            <code>null</code> if you don't want it persisted.
     * @return the dialog, after being closed by the user, which the client can
     *         only call <code>getReturnCode()</code> or
     *         <code>getToggleState()</code>
     */
    public static MessageDialogWithToggle openError(Shell parent, String title,
            String message, String toggleMessage, boolean toggleState,
            IPreferenceStore store, String key) {
        MessageDialogWithToggle dialog = new MessageDialogWithToggle(parent,
                title, null, // accept the default window icon
                message, ERROR, new String[] { IDialogConstants.OK_LABEL }, 0, // ok
                // is
                // the
                // default
                toggleMessage, toggleState);
        dialog.prefStore = store;
        dialog.prefKey = key;
        dialog.open();
        return dialog;
    }

    /**
     * Convenience method to open a standard information dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     * @param toggleMessage
     *            the message for the toggle control, or <code>null</code> for
     *            the default message
     * @param toggleState
     *            the initial state for the toggle
     * @param store
     *            the IPreference store in which the user's preference should be
     *            persisted; <code>null</code> if you don't want it persisted
     *            automatically.
     * @param key
     *            the key to use when persisting the user's preference;
     *            <code>null</code> if you don't want it persisted.
     * 
     * @return the dialog, after being closed by the user, which the client can
     *         only call <code>getReturnCode()</code> or
     *         <code>getToggleState()</code>
     */
    public static MessageDialogWithToggle openInformation(Shell parent,
            String title, String message, String toggleMessage,
            boolean toggleState, IPreferenceStore store, String key) {
        MessageDialogWithToggle dialog = new MessageDialogWithToggle(parent,
                title, null, // accept the default window icon
                message, INFORMATION,
                new String[] { IDialogConstants.OK_LABEL }, 0, // ok is the
                // default
                toggleMessage, toggleState);
        dialog.prefStore = store;
        dialog.prefKey = key;
        dialog.open();
        return dialog;
    }

    /**
     * Convenience method to open a simple confirm (OK/Cancel) dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     * @param toggleMessage
     *            the message for the toggle control, or <code>null</code> for
     *            the default message
     * @param toggleState
     *            the initial state for the toggle
     * @param store
     *            the IPreference store in which the user's preference should be
     *            persisted; <code>null</code> if you don't want it persisted
     *            automatically.
     * @param key
     *            the key to use when persisting the user's preference;
     *            <code>null</code> if you don't want it persisted.
     * @return the dialog, after being closed by the user, which the client can
     *         only call <code>getReturnCode()</code> or
     *         <code>getToggleState()</code>
     */
    public static MessageDialogWithToggle openOkCancelConfirm(Shell parent,
            String title, String message, String toggleMessage,
            boolean toggleState, IPreferenceStore store, String key) {
        MessageDialogWithToggle dialog = new MessageDialogWithToggle(parent,
                title, null, // accept the default window icon
                message, QUESTION, new String[] { IDialogConstants.OK_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0, // OK is the default
                toggleMessage, toggleState);
        dialog.prefStore = store;
        dialog.prefKey = key;
        dialog.open();
        return dialog;
    }

    /**
     * Convenience method to open a standard warning dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     * @param toggleMessage
     *            the message for the toggle control, or <code>null</code> for
     *            the default message
     * @param toggleState
     *            the initial state for the toggle
     * @param store
     *            the IPreference store in which the user's preference should be
     *            persisted; <code>null</code> if you don't want it persisted
     *            automatically.
     * @param key
     *            the key to use when persisting the user's preference;
     *            <code>null</code> if you don't want it persisted.
     * @return the dialog, after being closed by the user, which the client can
     *         only call <code>getReturnCode()</code> or
     *         <code>getToggleState()</code>
     */
    public static MessageDialogWithToggle openWarning(Shell parent,
            String title, String message, String toggleMessage,
            boolean toggleState, IPreferenceStore store, String key) {
        MessageDialogWithToggle dialog = new MessageDialogWithToggle(parent,
                title, null, // accept the default window icon
                message, WARNING, new String[] { IDialogConstants.OK_LABEL },
                0, // ok is the default
                toggleMessage, toggleState);
        dialog.open();
        dialog.prefStore = store;
        dialog.prefKey = key;
        return dialog;
    }

    /**
     * Convenience method to open a simple question Yes/No/Cancel dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     * @param toggleMessage
     *            the message for the toggle control, or <code>null</code> for
     *            the default message
     * @param toggleState
     *            the initial state for the toggle
     * @param store
     *            the IPreference store in which the user's preference should be
     *            persisted; <code>null</code> if you don't want it persisted
     *            automatically.
     * @param key
     *            the key to use when persisting the user's preference;
     *            <code>null</code> if you don't want it persisted.
     * @return the dialog, after being closed by the user, which the client can
     *         only call <code>getReturnCode()</code> or
     *         <code>getToggleState()</code>
     */
    public static MessageDialogWithToggle openYesNoCancelQuestion(Shell parent,
            String title, String message, String toggleMessage,
            boolean toggleState, IPreferenceStore store, String key) {
        MessageDialogWithToggle dialog = new MessageDialogWithToggle(parent,
                title, null, // accept the default window icon
                message, QUESTION, new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL,
                        IDialogConstants.CANCEL_LABEL }, 0, // YES is the
                // default
                toggleMessage, toggleState);
        dialog.prefStore = store;
        dialog.prefKey = key;
        dialog.open();
        return dialog;
    }

    /**
     * Convenience method to open a simple Yes/No question dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     * @param toggleMessage
     *            the message for the toggle control, or <code>null</code> for
     *            the default message
     * @param toggleState
     *            the initial state for the toggle
     * @param store
     *            the IPreference store in which the user's preference should be
     *            persisted; <code>null</code> if you don't want it persisted
     *            automatically.
     * @param key
     *            the key to use when persisting the user's preference;
     *            <code>null</code> if you don't want it persisted.
     * 
     * @return the dialog, after being closed by the user, which the client can
     *         only call <code>getReturnCode()</code> or
     *         <code>getToggleState()</code>
     */
    public static MessageDialogWithToggle openYesNoQuestion(Shell parent,
            String title, String message, String toggleMessage,
            boolean toggleState, IPreferenceStore store, String key) {
        MessageDialogWithToggle dialog = new MessageDialogWithToggle(parent,
                title, null, // accept the default window icon
                message, QUESTION, new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.NO_LABEL }, 0, // yes is the default
                toggleMessage, toggleState);
        dialog.prefStore = store;
        dialog.prefKey = key;
        dialog.open();
        return dialog;
    }

    /**
     * The key at which the toggle state should be stored within the
     * preferences. This value may be <code>null</code>, which indicates that
     * no preference should be updated automatically. It is then the
     * responsibility of the user of this API to use the information from the
     * toggle. Note: a <code>prefStore</code> is also needed.
     */
    protected String prefKey = null;

    /**
     * The preference store which will be affected by the toggle button. This
     * value may be <code>null</code>, which indicates that no preference
     * should be updated automatically. It is then the responsibility of the
     * user of this API to use the information from the toggle. Note: a
     * <code>prefKey</code> is also needed.
     */
    protected IPreferenceStore prefStore = null;

    /**
     * The toggle button (widget). This value is <code>null</code> until the
     * dialog is created.
     */
    protected Button toggleButton = null;

    /**
     * The message displayed to the user, with the toggle button. This is the
     * text besides the toggle. If it is <code>null</code>, this means that
     * the default text for the toggle should be used.
     */
    protected String toggleMessage;

    /**
     * The initial selected state of the toggle.
     */
    protected boolean toggleState;

    /**
     * Creates a message dialog with a toggle. See the superclass constructor
     * for info on the other parameters.
     * 
     * @param parentShell
     *            the parent shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param image
     *            the dialog title image, or <code>null</code> if none
     * @param message
     *            the dialog message
     * @param dialogImageType
     *            one of the following values:
     *            <ul>
     *            <li><code>MessageDialog.NONE</code> for a dialog with no
     *            image</li>
     *            <li><code>MessageDialog.ERROR</code> for a dialog with an
     *            error image</li>
     *            <li><code>MessageDialog.INFORMATION</code> for a dialog
     *            with an information image</li>
     *            <li><code>MessageDialog.QUESTION </code> for a dialog with a
     *            question image</li>
     *            <li><code>MessageDialog.WARNING</code> for a dialog with a
     *            warning image</li>
     *            </ul>
     * @param dialogButtonLabels
     *            an array of labels for the buttons in the button bar
     * @param defaultIndex
     *            the index in the button label array of the default button
     * @param toggleMessage
     *            the message for the toggle control, or <code>null</code> for
     *            the default message
     * @param toggleState
     *            the initial state for the toggle
     *  
     */
    public MessageDialogWithToggle(Shell parentShell, String dialogTitle,
            Image image, String message, int dialogImageType,
            String[] dialogButtonLabels, int defaultIndex,
            String toggleMessage, boolean toggleState) {
        super(parentShell, dialogTitle, image, message, dialogImageType,
                dialogButtonLabels, defaultIndex);
        this.toggleMessage = toggleMessage;
        this.toggleState = toggleState;
        this.buttonLabels = dialogButtonLabels;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);

        if (buttonId != IDialogConstants.CANCEL_ID && toggleState
                && prefStore != null && prefKey != null) {
            if (buttonId == IDialogConstants.YES_ID) {
                prefStore.setValue(prefKey, ALWAYS);
            } else if (buttonId == IDialogConstants.NO_ID) {
                prefStore.setValue(prefKey, NEVER);
            } else if (buttonId == IDialogConstants.OK_ID) {
                prefStore.setValue(prefKey, ALWAYS);
            }
        }
    }

    /**
     * @see Dialog#createButtonBar(Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        buttons = new Button[buttonLabels.length];
        for (int i = 0; i < buttonLabels.length; i++) {
            int id = i;
            String label = buttonLabels[i];
            if (label == IDialogConstants.OK_LABEL) {
                id = IDialogConstants.OK_ID;
            } else if (label == IDialogConstants.YES_LABEL) {
                id = IDialogConstants.YES_ID;
            } else if (label == IDialogConstants.NO_LABEL) {
                id = IDialogConstants.NO_ID;
            } else if (label == IDialogConstants.CANCEL_LABEL) {
                id = IDialogConstants.CANCEL_ID;
            }
            Button button = createButton(parent, id, label,
                    defaultButtonIndex == i);
            buttons[i] = button;
        }
    }

    /**
     * @see Dialog#createDialogArea(Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite dialogAreaComposite = (Composite) super
                .createDialogArea(parent);
        toggleButton = createToggleButton(dialogAreaComposite);
        return dialogAreaComposite;
    }

    /**
     * Creates a toggle button with the toggle message and state.
     * 
     * @param parent
     *            The composite in which the toggle button should be placed;
     *            must not be <code>null</code>.
     * @return The added toggle button; never <code>null</code>.
     */
    protected Button createToggleButton(Composite parent) {
        final Button button = new Button(parent, SWT.CHECK | SWT.LEFT);
        String text = toggleMessage;
        if (text == null) {
            text = JFaceResources
                    .getString("MessageDialogWithToggle.defaultToggleMessage"); //$NON-NLS-1$
        }
        button.setText(text);
        button.setSelection(toggleState);

        GridData data = new GridData(SWT.NONE);
        data.horizontalSpan = 2;
        data.horizontalAlignment = GridData.CENTER;
        button.setLayoutData(data);
        button.setFont(parent.getFont());

        button.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                toggleState = button.getSelection();
            }

        });
        return button;
    }

    /**
     * Returns the toggle button.
     * 
     * @return the toggle button
     */
    protected Button getToggleButton() {
        return toggleButton;
    }

    /**
     * Returns the toggle state. This can be called even after the dialog is
     * closed.
     * 
     * @return <code>true</code> if the toggle button is checked,
     *         <code>false</code> if not
     */
    public boolean getToggleState() {
        return toggleState;
    }

    /**
     * @param prefKey
     *            The prefKey to set.
     */
    public void setPrefKey(String prefKey) {
        this.prefKey = prefKey;
    }

    /**
     * @param prefStore
     *            The prefStore to set.
     */
    public void setPrefStore(IPreferenceStore prefStore) {
        this.prefStore = prefStore;
    }

}