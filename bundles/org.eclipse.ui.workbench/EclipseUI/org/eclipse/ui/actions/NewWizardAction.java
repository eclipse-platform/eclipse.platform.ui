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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.actions;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.internal.PerspectiveTracker;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.NewWizard;
import org.osgi.framework.FrameworkUtil;

/**
 * Invoke the resource creation wizard selection Wizard.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * This method automatically registers listeners so that it can keep its
 * enablement state up to date. Ordinarily, the window's references to these
 * listeners will be dropped automatically when the window closes. However, if
 * the client needs to get rid of an action while the window is still open, the
 * client must call #dispose() to give the action an opportunity to deregister
 * its listeners and to perform any other cleanup.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class NewWizardAction extends Action implements ActionFactory.IWorkbenchAction {

	/**
	 * The wizard dialog width
	 */
	private static final int SIZING_WIZARD_WIDTH = 500;

	/**
	 * The wizard dialog height
	 */
	private static final int SIZING_WIZARD_HEIGHT = 500;

	/**
	 * The id of the category to show or <code>null</code> to show all the
	 * categories.
	 */
	private String categoryId = null;

	/**
	 * The title of the wizard window or <code>null</code> to use the default wizard
	 * window title.
	 */
	private String windowTitle = null;

	/**
	 * The workbench window; or <code>null</code> if this action has been
	 * <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow;

	/**
	 * Tracks perspective activation, to update this action's enabled state.
	 */
	private PerspectiveTracker tracker;

	/**
	 * Create a new instance of this class.
	 *
	 * @param window the parent window
	 */
	public NewWizardAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.NewWizardAction_text);
		if (window == null) {
			throw new IllegalArgumentException();
		}
		this.workbenchWindow = window;
		tracker = new PerspectiveTracker(window, this);
		// @issues should be IDE-specific images
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
		setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED));
		setToolTipText(WorkbenchMessages.NewWizardAction_toolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.NEW_ACTION);
	}

	/**
	 * Create a new instance of this class
	 *
	 * @deprecated use the constructor
	 *             <code>NewWizardAction(IWorkbenchWindow)</code>
	 */
	@Deprecated
	public NewWizardAction() {
		this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}

	/**
	 * Returns the id of the category of wizards to show or <code>null</code> to
	 * show all categories.
	 *
	 * @return String
	 */
	public String getCategoryId() {
		return categoryId;
	}

	/**
	 * Sets the id of the category of wizards to show or <code>null</code> to show
	 * all categories.
	 *
	 * @param id the wizards's category id; may be <code>null</code>
	 */
	public void setCategoryId(String id) {
		categoryId = id;
	}

	/**
	 * <p>
	 * Sets the title of the wizard window
	 * </p>
	 *
	 * <p>
	 * If the title of the wizard window is <code>null</code>, the default wizard
	 * window title will be used.
	 * </p>
	 *
	 * @param windowTitle The title of the wizard window, otherwise
	 *                    <code>null</code> (default wizard window title).
	 *
	 * @since 3.6
	 */
	public void setWizardWindowTitle(String windowTitle) {
		this.windowTitle = windowTitle;
	}

	@Override
	public void run() {
		if (workbenchWindow == null) {
			// action has been disposed
			return;
		}
		NewWizard wizard = new NewWizard();
		wizard.setCategoryId(categoryId);
		wizard.setWindowTitle(windowTitle);

		ISelection selection = workbenchWindow.getSelectionService().getSelection();
		IStructuredSelection selectionToPass = StructuredSelection.EMPTY;
		if (selection instanceof IStructuredSelection) {
			selectionToPass = (IStructuredSelection) selection;
		} else {
			// @issue the following is resource-specific legacy code
			// Build the selection from the IFile of the editor
			Class<?> resourceClass = LegacyResourceSupport.getResourceClass();
			if (resourceClass != null) {
				IWorkbenchPart part = workbenchWindow.getPartService().getActivePart();
				if (part instanceof IEditorPart) {
					IEditorInput input = ((IEditorPart) part).getEditorInput();
					Object resource = Adapters.adapt(input, resourceClass);
					if (resource != null) {
						selectionToPass = new StructuredSelection(resource);
					}
				}
			}
		}

		wizard.init(workbenchWindow.getWorkbench(), selectionToPass);

		IDialogSettings workbenchSettings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(NewWizardAction.class)).getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings.getSection("NewWizardAction"); //$NON-NLS-1$
		if (wizardSettings == null) {
			wizardSettings = workbenchSettings.addNewSection("NewWizardAction"); //$NON-NLS-1$
		}
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);

		Shell parent = workbenchWindow.getShell();
		WizardDialog dialog = new WizardDialog(parent, wizard);
		dialog.create();
		dialog.getShell().setSize(Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IWorkbenchHelpContextIds.NEW_WIZARD);
		dialog.open();
	}

	@Override
	public void dispose() {
		if (workbenchWindow == null) {
			// action has already been disposed
			return;
		}
		tracker.dispose();
		workbenchWindow = null;
	}

}
