package org.eclipse.ui.contexts;

/**
 * An instance of this interface provides support for managing contexts at the
 * <code>IWorkbenchWindow</code> level.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IWorkbenchWindow#getAdaptable
 */
public interface IWorkbenchWindowContextSupport {

	/**
	 * Returns the context activation service for the workbench window.
	 * 
	 * @return the context activation for the workbench window. Guaranteed not
	 *         to be <code>null</code>.
	 */
	IContextActivationService getContextActivationService();
}
