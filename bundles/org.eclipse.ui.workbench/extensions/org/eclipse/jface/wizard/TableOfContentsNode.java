package org.eclipse.jface.wizard;

import java.net.*;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The WizardTableOfContentsNode is the class that represents 
 * each node in the table of contents.
 */
class TableOfContentsNode implements ITableOfContentsNode {

	/**
	 * Keys for support images.
	 */
	private static final String BREAK_ENABLED = "break_enabled";
	private static final String BREAK_DISABLED = "break_disabled";
	private static final String FINISH_NOT_PRESSED = "finish_not_pressed";
	private static final String FINISH_PRESSED = "finish_pressed";
	private static final String FINISH_DISABLED = "finish_disabled";
	private static final String START = "start";
	private static final String UNKNOWN = "unknown";

	/**
	 * Keys for disabled images.
	 */
	private static final String BRANCH_DISABLED = "branch_disabled";
	private static final String KNOWN_DISABLED = "known_disabled";

	/**
	 * Keys for past images
	 */
	private static final String BRANCH_PAST = "branch_past";
	private static final String KNOWN_PAST = "known_past";

	/**
	 * Keys for current images
	 */
	private static final String BRANCH_CURRENT = "branch_current";
	private static final String KNOWN_CURRENT = "known_current";

	/**
	 * Keys for future images
	 */
	private static final String BRANCH_FUTURE = "branch_future";
	private static final String KNOWN_FUTURE = "known_future";

	private boolean enabled = true;

	static {
		ImageRegistry reg = JFaceResources.getImageRegistry();
		URL installURL =
			WorkbenchPlugin.getDefault().getDescriptor().getInstallURL();

		try {
			installURL = new URL(installURL, "icons/full/");

			reg.put(BREAK_DISABLED, ImageDescriptor.createFromURL(new URL(installURL, "dtoc/break_toc.gif"))); //$NON-NLS-1$
			reg.put(FINISH_DISABLED, ImageDescriptor.createFromURL(new URL(installURL, "dtoc/break_toc.gif"))); //$NON-NLS-1$
			reg.put(BRANCH_DISABLED, ImageDescriptor.createFromURL(new URL(installURL, "dtoc/pagebranch_toc.gif"))); //$NON-NLS-1$
			reg.put(KNOWN_DISABLED, ImageDescriptor.createFromURL(new URL(installURL, "dtoc/pageknown_toc.gif"))); //$NON-NLS-1$
			reg.put(UNKNOWN, ImageDescriptor.createFromURL(new URL(installURL, "dtoc/pageunknown_toc.gif"))); //$NON-NLS-1$

			reg.put(BREAK_ENABLED, ImageDescriptor.createFromURL(new URL(installURL, "etoc/break_toc.gif"))); //$NON-NLS-1$
			reg.put(FINISH_NOT_PRESSED, ImageDescriptor.createFromURL(new URL(installURL, "etoc/finish_toc.gif"))); //$NON-NLS-1$
			reg.put(BRANCH_FUTURE, ImageDescriptor.createFromURL(new URL(installURL, "etoc/pagebranch_toc.gif"))); //$NON-NLS-1$
			reg.put(KNOWN_FUTURE, ImageDescriptor.createFromURL(new URL(installURL, "etoc/pageknown_toc.gif"))); //$NON-NLS-1$

			reg.put(KNOWN_PAST, ImageDescriptor.createFromURL(new URL(installURL, "ftoc/pageknown_toc.gif"))); //$NON-NLS-1$
			reg.put(FINISH_PRESSED, ImageDescriptor.createFromURL(new URL(installURL, "ftoc/finish_toc.gif"))); //$NON-NLS-1$
			reg.put(BRANCH_PAST, ImageDescriptor.createFromURL(new URL(installURL, "ftoc/pagebranch_toc.gif"))); //$NON-NLS-1$
			reg.put(START, ImageDescriptor.createFromURL(new URL(installURL, "ftoc/start_toc.gif"))); //$NON-NLS-1$

			reg.put(BRANCH_CURRENT, ImageDescriptor.createFromURL(new URL(installURL, "stoc/pagebranch_toc.gif"))); //$NON-NLS-1$
			reg.put(KNOWN_CURRENT, ImageDescriptor.createFromURL(new URL(installURL, "stoc/pageknown_toc.gif"))); //$NON-NLS-1$

		} catch (MalformedURLException exception) {
			IStatus errorStatus =
				new Status(
					IStatus.ERROR,
					WorkbenchPlugin
						.getDefault()
						.getDescriptor()
						.getUniqueIdentifier(),
					0,
					JFaceResources.getString("Problem_Occurred"),
				//$NON-NLS-1$
	exception);
			WorkbenchPlugin.getDefault().getLog().log(errorStatus);
		}

	}

	IWizardPage page;

	/**
	 * Create a new instance of the receiver with newPage as the page
	 * that is activated on selection.
	 * @param newPage IWizardPage or null
	 */
	public TableOfContentsNode(IWizardPage newPage) {
		this.page = newPage;
		//Is this an unknown node?
		if (newPage == null)
			setEnabled(false);
	}

	/*
	 * @see ITableOfContentsNode.getPage()
	 */
	public IWizardPage getPage() {
		return page;
	}

	/**
	 * Sets the page.
	 * @param page The page to set.
	 */
	public void setPage(IWizardPage page) {
		this.page = page;
	}

	/*
	 * @see ITableOfContentsNode.dispose()
	 */
	public void dispose() {
	}

	/*
	 * @see ITableOfContentsNode.setEnabled(boolean)
	 */
	public void setEnabled(boolean enabledValue) {
		this.enabled = enabledValue;
	}

	/*
	 * @see org.eclipse.jface.wizard.ITableOfContentsNode#getImage(int)
	 */
	public Image getImage(int positionConstant) {
		return JFaceResources.getImage(getImageConstant(positionConstant));

	}

	/**
	 * Get the String constant for positionConstant.
	 */
	private String getImageConstant(int positionConstant) {
		IWizardPage page = this.getPage();

		if (page == null)
			return UNKNOWN;
		if (page instanceof IDecisionPage) {
			if (enabled) {
				if (positionConstant == PAST_NODE)
					return BRANCH_PAST;
				if (positionConstant == FUTURE_NODE)
					return BRANCH_FUTURE;
				return BRANCH_CURRENT;
			} else
				return BRANCH_DISABLED;
		}

		if (enabled) {
			if (positionConstant == PAST_NODE)
				return KNOWN_PAST;
			if (positionConstant == FUTURE_NODE)
				return KNOWN_FUTURE;
			return KNOWN_CURRENT;
		} else
			return KNOWN_DISABLED;
	}
}
