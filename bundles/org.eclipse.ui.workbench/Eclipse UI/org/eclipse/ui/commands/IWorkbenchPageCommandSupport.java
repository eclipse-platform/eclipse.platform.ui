package org.eclipse.ui.commands;

/**
 * An instance of this interface provides support for managing commands at the
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
public interface IWorkbenchPageCommandSupport {

	/**
	 * Returns the compound command handler service for the workbench page.
	 * 
	 * @return the compound command handler service for the workbench page.
	 *         Guaranteed not to be <code>null</code>.
	 */
	//ICompoundCommandHandlerService getCompoundCommandHandlerService();
}
