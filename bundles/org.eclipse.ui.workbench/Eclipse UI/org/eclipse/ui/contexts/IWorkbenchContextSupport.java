package org.eclipse.ui.contexts;

/**
 * An instance of this interface provides support for managing contexts at the
 * <code>IWorkbench</code> level.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IWorkbench#getAdaptable
 */
public interface IWorkbenchContextSupport {

	/**
	 * Returns the compound context activation service for the workbench.
	 * 
	 * @return the compound context activation service for the workbench.
	 *         Guaranteed not to be <code>null</code>.
	 */
	ICompoundContextActivationService getCompoundContextActivationService();

	/**
	 * Returns the context activation service for the workbench.
	 * 
	 * @return the context activation for the workbench. Guaranteed not to be
	 *         <code>null</code>.
	 */
	IContextActivationService getContextActivationService();

	/**
	 * Returns the context manager for the workbench.
	 * 
	 * @return the context manager for the workbench. Guaranteed not to be
	 *         <code>null</code>.
	 */
	IContextManager getContextManager();
}
