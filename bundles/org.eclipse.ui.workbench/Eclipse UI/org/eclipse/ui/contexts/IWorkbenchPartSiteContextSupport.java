package org.eclipse.ui.contexts;

/**
 * An instance of this interface provides support for managing contexts at the
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
public interface IWorkbenchPartSiteContextSupport {

	/**
	 * Returns the mutable context activation service for the workbench part site.
	 * 
	 * @return the mutable context activation service for the workbench part site.
	 *         Guaranteed not to be <code>null</code>.
	 */
	IMutableContextActivationService getMutableContextActivationService();
}
