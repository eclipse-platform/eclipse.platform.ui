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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 429598
 *******************************************************************************/
package org.eclipse.jface.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * An abstract base implementation of a wizard. A typical client subclasses
 * <code>Wizard</code> to implement a particular wizard.
 * <p>
 * Subclasses may call the following methods to configure the wizard:
 * </p>
 * <ul>
 * <li><code>addPage</code></li>
 * <li><code>setHelpAvailable</code></li>
 * <li><code>setDefaultPageImageDescriptor</code></li>
 * <li><code>setDialogSettings</code></li>
 * <li><code>setNeedsProgressMonitor</code></li>
 * <li><code>setTitleBarColor</code></li>
 * <li><code>setWindowTitle</code></li>
 * </ul>
 * <p>
 * Subclasses may override these methods if required:
 * </p>
 * <ul>
 * <li>reimplement <code>createPageControls</code></li>
 * <li>reimplement <code>performCancel</code></li>
 * <li>extend <code>addPages</code></li>
 * <li>reimplement <code>performFinish</code></li>
 * <li>extend <code>dispose</code></li>
 * </ul>
 * <p>
 * Note that clients are free to implement <code>IWizard</code> from scratch
 * instead of subclassing <code>Wizard</code>. Correct implementations of
 * <code>IWizard</code> will work with any correct implementation of
 * <code>IWizardPage</code>.
 * </p>
 */
public abstract class Wizard implements IWizard, IShellProvider {
	/**
	 * Image registry key of the default image for wizard pages (value
	 * <code>"org.eclipse.jface.wizard.Wizard.pageImage"</code>).
	 */
	public static final String DEFAULT_IMAGE = "org.eclipse.jface.wizard.Wizard.pageImage";//$NON-NLS-1$

	/**
	 * The wizard container this wizard belongs to; <code>null</code> if none.
	 */
	private IWizardContainer container = null;

	/**
	 * This wizard's list of pages (element type: <code>IWizardPage</code>).
	 */
	private List<IWizardPage> pages = new ArrayList<>();

	/**
	 * Indicates whether this wizard needs a progress monitor.
	 */
	private boolean needsProgressMonitor = false;

	/**
	 * Indicates whether this wizard needs previous and next buttons even if the
	 * wizard has only one page.
	 */
	private boolean forcePreviousAndNextButtons = false;

	/**
	 * Indicates whether this wizard supports help.
	 */
	private boolean isHelpAvailable = false;

	/**
	 * The default page image for pages without one of their one;
	 * <code>null</code> if none.
	 */
	private Image defaultImage = null;

	/**
	 * The default page image descriptor, used for creating a default page image
	 * if required; <code>null</code> if none.
	 */
	private ImageDescriptor defaultImageDescriptor = JFaceResources.getImageRegistry().getDescriptor(DEFAULT_IMAGE);

	/**
	 * The color of the wizard title bar; <code>null</code> if none.
	 */
	private RGB titleBarColor = null;

	/**
	 * The window title string for this wizard; <code>null</code> if none.
	 */
	private String windowTitle = null;

	/**
	 * The dialog settings for this wizard; <code>null</code> if none.
	 */
	private IDialogSettings dialogSettings = null;

	/**
	 * Creates a new empty wizard.
	 */
	protected Wizard() {
		super();
	}

	/**
	 * Adds a new page to this wizard. The page is inserted at the end of the
	 * page list.
	 *
	 * @param page
	 *            the new page
	 */
	public void addPage(IWizardPage page) {
		pages.add(page);
		page.setWizard(this);
	}

	/**
	 * The <code>Wizard</code> implementation of this <code>IWizard</code>
	 * method does nothing. Subclasses should extend if extra pages need to be
	 * added before the wizard opens. New pages should be added by calling
	 * <code>addPage</code>.
	 */
	@Override
	public void addPages() {
	}

	@Override
	public boolean canFinish() {
		// Default implementation is to check if all pages are complete.
		for (IWizardPage page : pages) {
			if (!page.isPageComplete()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The <code>Wizard</code> implementation of this <code>IWizard</code>
	 * method creates all the pages controls using
	 * <code>IDialogPage.createControl</code>. Subclasses should reimplement
	 * this method if they want to delay creating one or more of the pages
	 * lazily. The framework ensures that the contents of a page will be created
	 * before attempting to show it.
	 */
	@Override
	public void createPageControls(Composite pageContainer) {
		// the default behavior is to create all the pages controls
		for (IWizardPage page : pages) {
			page.createControl(pageContainer);
			// page is responsible for ensuring the created control is
			// accessible
			// via getControl.
			Assert.isNotNull(
					page.getControl(),
					"getControl() of wizard page returns null. Did you call setControl() in your wizard page?"); //$NON-NLS-1$
		}
	}

	/**
	 * The <code>Wizard</code> implementation of this <code>IWizard</code>
	 * method disposes all the pages controls using
	 * <code>DialogPage.dispose</code>. Subclasses should extend this method
	 * if the wizard instance maintains addition SWT resource that need to be
	 * disposed.
	 */
	@Override
	public void dispose() {
		// notify pages
		for (IWizardPage page : pages) {
			try {
				page.dispose();
			} catch (Exception e) {
				Status status = new Status(IStatus.ERROR, Policy.JFACE, IStatus.ERROR, e.getMessage(), e);
				Policy.getLog().log(status);
			}
		}
		// dispose of image
		if (defaultImage != null) {
			JFaceResources.getResources().destroy(defaultImageDescriptor);
			defaultImage = null;
		}
	}

	@Override
	public IWizardContainer getContainer() {
		return container;
	}

	@Override
	public Image getDefaultPageImage() {
		if (defaultImage == null) {
			defaultImage = JFaceResources.getResources().createImageWithDefault(defaultImageDescriptor);
		}
		return defaultImage;
	}

	@Override
	public IDialogSettings getDialogSettings() {
		return dialogSettings;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		int index = pages.indexOf(page);
		if (index == pages.size() - 1 || index == -1) {
			// last page or page not found
			return null;
		}
		return pages.get(index + 1);
	}

	@Override
	public IWizardPage getPage(String name) {
		for (IWizardPage page : pages) {
			String pageName = page.getName();
			if (pageName.equals(name)) {
				return page;
			}
		}
		return null;
	}

	@Override
	public int getPageCount() {
		return pages.size();
	}

	@Override
	public IWizardPage[] getPages() {
		return pages.toArray(new IWizardPage[pages.size()]);
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		int index = pages.indexOf(page);
		if (index == 0 || index == -1) {
			// first page or page not found
			return null;
		}
		return pages.get(index - 1);
	}

	/**
	 * Returns the wizard's shell if the wizard is visible. Otherwise
	 * <code>null</code> is returned.
	 *
	 * @return Shell
	 */
	@Override
	public Shell getShell() {
		if (container == null) {
			return null;
		}
		return container.getShell();
	}

	@Override
	public IWizardPage getStartingPage() {
		if (pages.isEmpty()) {
			return null;
		}
		return pages.get(0);
	}

	@Override
	public RGB getTitleBarColor() {
		return titleBarColor;
	}

	@Override
	public String getWindowTitle() {
		return windowTitle;
	}

	@Override
	public Point getMinimumWizardSize() {
		int minWidth = SWT.DEFAULT;
		int minHeight = SWT.DEFAULT;

		for (IWizardPage page : pages) {
			Point minPageSize = page.getMinimumPageSize();

			if (minPageSize != null) {
				minWidth = Math.max(minWidth, minPageSize.x);
				minHeight = Math.max(minHeight, minPageSize.y);
			}
		}

		if (minWidth == SWT.DEFAULT || minHeight == SWT.DEFAULT) {
			return null;
		}

		return new Point(minWidth, minHeight);
	}

	@Override
	public boolean isHelpAvailable() {
		return isHelpAvailable;
	}

	@Override
	public boolean needsPreviousAndNextButtons() {
		return forcePreviousAndNextButtons || pages.size() > 1;
	}

	@Override
	public boolean needsProgressMonitor() {
		return needsProgressMonitor;
	}

	/**
	 * The <code>Wizard</code> implementation of this <code>IWizard</code>
	 * method does nothing and returns <code>true</code>. Subclasses should
	 * reimplement this method if they need to perform any special cancel
	 * processing for their wizard.
	 */
	@Override
	public boolean performCancel() {
		return true;
	}

	/**
	 * Subclasses must implement this <code>IWizard</code> method to perform
	 * any special finish processing for their wizard.
	 */
	@Override
	public abstract boolean performFinish();

	@Override
	public void setContainer(IWizardContainer wizardContainer) {
		container = wizardContainer;
	}

	/**
	 * Sets the default page image descriptor for this wizard.
	 * <p>
	 * This image descriptor will be used to generate an image for a page with
	 * no image of its own; the image will be computed once and cached.
	 * </p>
	 *
	 * @param imageDescriptor
	 *            the default page image descriptor
	 */
	public void setDefaultPageImageDescriptor(ImageDescriptor imageDescriptor) {
		defaultImageDescriptor = imageDescriptor;
	}

	/**
	 * Sets the dialog settings for this wizard.
	 * <p>
	 * The dialog settings is used to record state between wizard invocations
	 * (for example, radio button selection, last import directory, etc.)
	 * </p>
	 *
	 * @param settings
	 *            the dialog settings, or <code>null</code> if none
	 * @see #getDialogSettings
	 *
	 */
	public void setDialogSettings(IDialogSettings settings) {
		dialogSettings = settings;
	}

	/**
	 * Controls whether the wizard needs Previous and Next buttons even if it
	 * currently contains only one page.
	 * <p>
	 * This flag should be set on wizards where the first wizard page adds
	 * follow-on wizard pages based on user input.
	 * </p>
	 *
	 * @param b
	 *            <code>true</code> to always show Next and Previous buttons,
	 *            and <code>false</code> to suppress Next and Previous buttons
	 *            for single page wizards
	 */
	public void setForcePreviousAndNextButtons(boolean b) {
		forcePreviousAndNextButtons = b;
	}

	/**
	 * Sets whether help is available for this wizard.
	 * <p>
	 * The result of this method is typically used by the container to show or hide the button
	 * labeled "Help".
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
	 * @param b <code>true</code> if help is available, <code>false</code> otherwise
	 * @see #isHelpAvailable()
	 * @see TrayDialog#isHelpAvailable()
	 * @see TrayDialog#setHelpAvailable(boolean)
	 */
	public void setHelpAvailable(boolean b) {
		isHelpAvailable = b;
	}

	/**
	 * Sets whether this wizard needs a progress monitor.
	 *
	 * @param b
	 *            <code>true</code> if a progress monitor is required, and
	 *            <code>false</code> if none is needed
	 * @see #needsProgressMonitor()
	 */
	public void setNeedsProgressMonitor(boolean b) {
		needsProgressMonitor = b;
	}

	/**
	 * Sets the title bar color for this wizard.
	 *
	 * @param color
	 *            the title bar color
	 */
	public void setTitleBarColor(RGB color) {
		titleBarColor = color;
	}

	/**
	 * Sets the window title for the container that hosts this page to the given
	 * string.
	 *
	 * @param newTitle
	 *            the window title for the container
	 */
	public void setWindowTitle(String newTitle) {
		windowTitle = newTitle;
		if (container != null) {
			container.updateWindowTitle();
		}
	}
}
