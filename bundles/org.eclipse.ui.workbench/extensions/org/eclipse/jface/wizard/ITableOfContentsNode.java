package org.eclipse.jface.wizard;

import org.eclipse.swt.graphics.Image;

/**
 * The ITableOfContentsNode is the node that reprsents a page in the
 * wizard.
 */
public interface ITableOfContentsNode {
	
	/**
	 * Returns the page.
	 * @return IWizardPage
	 */
	public IWizardPage getPage();

	/**
	 * 	Enable or disable the widgets.
	 * @param boolean
	 */
	public void setEnabled(boolean enabled);
	
	/**
	 * Dispose the widgets for the receiver.
	 */
	public void dispose();
	
	/**
	 * Get the image for the recevier
	 */
	public Image getImage();



}
