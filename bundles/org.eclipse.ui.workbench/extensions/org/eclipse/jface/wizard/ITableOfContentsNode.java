package org.eclipse.jface.wizard;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

/**
 * The ITableOfContentsNode is the node that reprsents a page in the
 * wizard.
 */
public interface ITableOfContentsNode {
	
	/**
	 * Create the two labels used to select this node as direct children
	 * of the composite.
	 * @param Composite composite.
	 * @param foreground. The foreground color for the widgets.
	 * @param background. The background color for the widgets.
	 */
	public void createWidgets(Composite parentComposite, Color foreground, Color background);

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


}
