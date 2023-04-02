/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
package org.eclipse.jface.wizard;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

/**
 * Interface for a wizard.  A wizard maintains a list of wizard pages,
 * stacked on top of each other in card layout fashion.
 * <p>
 * The class <code>Wizard</code> provides an abstract implementation
 * of this interface. However, clients are also free to implement this
 * interface if <code>Wizard</code> does not suit their needs.
 * </p>
 * @see Wizard
 */
public interface IWizard {
	/**
	 * Adds any last-minute pages to this wizard.
	 * <p>
	 * This method is called just before the wizard becomes visible, to give the
	 * wizard the opportunity to add any lazily created pages.
	 * </p>
	 */
	void addPages();

	/**
	 * Returns whether this wizard could be finished without further user
	 * interaction.
	 * <p>
	 * The result of this method is typically used by the wizard container to enable
	 * or disable the Finish button.
	 * </p>
	 *
	 * @return <code>true</code> if the wizard could be finished,
	 *   and <code>false</code> otherwise
	 */
	boolean canFinish();

	/**
	 * Creates this wizard's controls in the given parent control.
	 * <p>
	 * The wizard container calls this method to create the controls
	 * for the wizard's pages before the wizard is opened. This allows
	 * the wizard to size correctly; otherwise a resize may occur when
	 * moving to a new page.
	 * </p>
	 *
	 * @param pageContainer the parent control
	 */
	void createPageControls(Composite pageContainer);

	/**
	 * Disposes of this wizard and frees all SWT resources.
	 */
	void dispose();

	/**
	 * Returns the container of this wizard.
	 *
	 * @return the wizard container, or <code>null</code> if this
	 *   wizard has yet to be added to a container
	 */
	IWizardContainer getContainer();

	/**
	 * Returns the default page image for this wizard.
	 * <p>
	 * This image can be used for pages which do not
	 * supply their own image.
	 * </p>
	 *
	 * @return the default page image
	 */
	Image getDefaultPageImage();

	/**
	 * Returns the dialog settings for this wizard.
	 * <p>
	 * The dialog store is used to record state between
	 * wizard invocations (for example, radio button selections,
	 * last directory, etc.).
	 * </p>
	 *
	 * @return the dialog settings, or <code>null</code> if none
	 */
	IDialogSettings getDialogSettings();

	/**
	 * Returns the successor of the given page.
	 * <p>
	 * This method is typically called by a wizard page
	 * </p>
	 *
	 * @param page the page
	 * @return the next page, or <code>null</code> if none
	 */
	IWizardPage getNextPage(IWizardPage page);

	/**
	 * Returns the minimum size of this wizard. The minimum size is calculated using
	 * the minimum page sizes of all wizard pages. May return {@code null} if none
	 * of the wizard pages specify a minimum size.
	 *
	 * @see IWizardPage#getMinimumPageSize()
	 * @return the minimum size encoded as {@code new Point(width,height)}
	 * @since 3.30
	 */
	default Point getMinimumWizardSize() {
		return null;
	}

	/**
	 * Returns the wizard page with the given name belonging to this wizard.
	 *
	 * @param pageName the name of the wizard page
	 * @return the wizard page with the given name, or <code>null</code> if none
	 */
	IWizardPage getPage(String pageName);

	/**
	 * Returns the number of pages in this wizard.
	 *
	 * @return the number of wizard pages
	 */
	int getPageCount();

	/**
	 * Returns all the pages in this wizard.
	 *
	 * @return a list of pages
	 */
	IWizardPage[] getPages();

	/**
	 * Returns the predecessor of the given page.
	 * <p>
	 * This method is typically called by a wizard page
	 * </p>
	 *
	 * @param page the page
	 * @return the previous page, or <code>null</code> if none
	 */
	IWizardPage getPreviousPage(IWizardPage page);

	/**
	 * Returns the first page to be shown in this wizard.
	 *
	 * @return the first wizard page
	 */
	IWizardPage getStartingPage();

	/**
	 * Returns the title bar color for this wizard.
	 *
	 * @return the title bar color
	 */
	RGB getTitleBarColor();

	/**
	 * Returns the window title string for this wizard.
	 *
	 * @return the window title string, or <code>null</code> for no title
	 */
	String getWindowTitle();

	/**
	 * Returns whether help is available for this wizard.
	 * <p>
	 * The result of this method is typically used by the container to show or hide a button labeled
	 * "Help".
	 * </p>
	 * <p>
	 * <strong>Note:</strong> This wizard's container might be a {@link TrayDialog} which provides
	 * its own help support that is independent of this property.
	 * </p>
	 * <p>
	 * <strong>Note 2:</strong> In the default {@link WizardDialog} implementation, the "Help"
	 * button only works when {@link org.eclipse.jface.dialogs.IDialogPage#performHelp()} is implemented.
	 * </p>
	 *
	 * @return <code>true</code> if help is available, <code>false</code> otherwise
	 * @see TrayDialog#isHelpAvailable()
	 * @see TrayDialog#setHelpAvailable(boolean)
	 */
	boolean isHelpAvailable();

	/**
	 * Returns whether this wizard needs Previous and Next buttons.
	 * <p>
	 * The result of this method is typically used by the container.
	 * </p>
	 *
	 * @return <code>true</code> if Previous and Next buttons are required,
	 *   and <code>false</code> if none are needed
	 */
	boolean needsPreviousAndNextButtons();

	/**
	 * Returns whether this wizard needs a progress monitor.
	 * <p>
	 * The result of this method is typically used by the container.
	 * </p>
	 *
	 * @return <code>true</code> if a progress monitor is required,
	 *   and <code>false</code> if none is needed
	 */
	boolean needsProgressMonitor();

	/**
	 * Performs any actions appropriate in response to the user
	 * having pressed the Cancel button, or refuse if canceling
	 * now is not permitted.
	 *
	 * @return <code>true</code> to indicate the cancel request
	 *   was accepted, and <code>false</code> to indicate
	 *   that the cancel request was refused
	 */
	boolean performCancel();

	/**
	 * Performs any actions appropriate in response to the user
	 * having pressed the Finish button, or refuse if finishing
	 * now is not permitted.
	 *
	 * Normally this method is only called on the container's
	 * current wizard. However if the current wizard is a nested wizard
	 * this method will also be called on all wizards in its parent chain.
	 * Such parents may use this notification to save state etc. However,
	 * the value the parents return from this method is ignored.
	 *
	 * @return <code>true</code> to indicate the finish request
	 *   was accepted, and <code>false</code> to indicate
	 *   that the finish request was refused
	 */
	boolean performFinish();

	/**
	 * Sets or clears the container of this wizard.
	 *
	 * @param wizardContainer the wizard container, or <code>null</code>
	 */
	void setContainer(IWizardContainer wizardContainer);
}
