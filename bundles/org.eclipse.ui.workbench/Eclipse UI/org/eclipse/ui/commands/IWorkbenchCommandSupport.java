package org.eclipse.ui.commands;

/**
 * An instance of this interface provides support for managing commands at the
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
public interface IWorkbenchCommandSupport {

	/**
	 * Returns the command manager for the workbench.
	 * 
	 * @return the command manager for the workbench. Guaranteed not to be
	 *         <code>null</code>.
	 */
	ICommandManager getCommandManager();

	/**
	 * Returns the compound command handler service for the workbench.
	 * 
	 * @return the compound command handler service for the workbench.
	 *         Guaranteed not to be <code>null</code>.
	 */
	ICompoundCommandHandlerService getCompoundCommandHandlerService();
}
