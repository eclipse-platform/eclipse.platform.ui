package org.eclipse.ui.contexts;

/**
 * An instance of this interface provides support for managing contexts at the
 * <code>IWorkbenchPage</code> level.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IWorkbenchPage#getAdaptable
 */
public interface IWorkbenchPageContextSupport {

	/**
	 * Returns the compound context activation service for the workbench page.
	 * 
	 * @return the compound context activation service for the workbench page.
	 *         Guaranteed not to be <code>null</code>.
	 */
	ICompoundContextActivationService getCompoundContextActivationService();
}
