package org.eclipse.jface.wizard;

import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.Image;

/**
 * The WizardTableOfContentsNode is the class that represents 
 * each node in the table of contents.
 */
public class TableOfContentsNode implements ITableOfContentsNode {

	/**
	 * Image registry key for decision image (value <code>"toc_decision_image"</code>).
	 */
	private static final String TOC_IMG_DECISION = "toc_decision_image"; //$NON-NLS-1$

	/**
	 * Image registry key for last page image (value <code>"toc_disabled_image"</code>).
	 */
	private static final String TOC_IMG_DISABLED = "toc_disabled_image"; //$NON-NLS-1$

	/**
	 * Image registry key for next image (value <code>"toc_next_image"</code>).
	 */
	private static final String TOC_IMG_NEXT = "toc_next_image"; //$NON-NLS-1$

	private boolean enabled = true;

	static {
		ImageRegistry reg = JFaceResources.getImageRegistry();
		reg.put(TOC_IMG_DISABLED, ImageDescriptor.createFromFile(TableOfContentsNode.class, "images/disabled.gif")); //$NON-NLS-1$
		reg.put(TOC_IMG_NEXT, ImageDescriptor.createFromFile(TableOfContentsNode.class, "images/next.gif")); //$NON-NLS-1$
		reg.put(TOC_IMG_DECISION, ImageDescriptor.createFromFile(TableOfContentsNode.class, "images/decision.gif")); //$NON-NLS-1$

	}

	IWizardPage page;

	/**
	 * Create a new instance of the receiver with newPage as the page
	 * that is activated on selection.
	 * @param newPage
	 */
	public TableOfContentsNode(IWizardPage newPage) {
		this.page = newPage;
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

	/**
	 * Get the image for the receiver.
	 * @param enabled The boolean state used to determine the image to use.
	 * @return Image
	 */
	private Image getImage(boolean enabled) {
		if (getPage() instanceof IDecisionPage)
			return JFaceResources.getImage(TOC_IMG_DECISION);
		else {
			if (enabled)
				return JFaceResources.getImage(TOC_IMG_NEXT);
			else
				return JFaceResources.getImage(TOC_IMG_DISABLED);
		}
	}

	/**
	 * @see org.eclipse.jface.wizard.ITableOfContentsNode#getImage()
	 */
	public Image getImage() {
		return getImage(enabled);
	}

}
