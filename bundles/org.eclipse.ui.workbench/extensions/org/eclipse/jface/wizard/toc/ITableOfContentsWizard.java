package org.eclipse.jface.wizard.toc;

import org.eclipse.jface.wizard.IWizard;

/**
 * ITableOfContentsWizard is a class that implements the methods for
 * adding pages to a TableOfContents.
 */
public interface ITableOfContentsWizard extends IWizard {
	
	
	/**
	 * Get the table of contents nodes for the initial
	 * pages
	 */
	public ITableOfContentsNode[] getInitialNodes();

}
