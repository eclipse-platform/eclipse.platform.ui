package org.eclipse.jface.wizard;

import org.eclipse.swt.graphics.Image;

/**
 * The ITableOfContentsNode is the node that reprsents a page in the
 * wizard.
 */
interface ITableOfContentsNode {
	
	
	//Constants to indicate where in the table of content this is.
	public static final int PAST_NODE = 0;
	public static final int CURRENT_NODE = 1;
	public static final int FUTURE_NODE = 2;
	
	/**
	 * Returns the page. May be null.
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
	 * Get the image for the receiver based on the positionConstant.
	 * @param positionConstant - one of PAST_NODE, CURRENT_NODE or FUTURTE_NODE
	 */
	public Image getImage(int positionConstant);



}
