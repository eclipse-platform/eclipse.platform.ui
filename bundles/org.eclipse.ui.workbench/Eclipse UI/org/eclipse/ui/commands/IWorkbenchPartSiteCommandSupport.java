package org.eclipse.ui.commands;

/**
 * An instance of this interface provides support for managing commands at the
 * <code>IWorkbenchPartSite</code> level.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IWorkbenchPartSite#getAdaptable
 */
public interface IWorkbenchPartSiteCommandSupport {

	/**
	 * Returns the mutable command handler service for the workbench part site.
	 * 
	 * @return the mutable command handler service for the workbench part site.
	 *         Guaranteed not to be <code>null</code>.
	 */
	//IMutableCommandHandlerService getMutableCommandHandlerService();
}
