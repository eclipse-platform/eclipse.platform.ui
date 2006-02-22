package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.StructuredViewer;

/**
 * 
 * Provides access to information required for the initialization of
 * CommonActionProviders.
 * 
 * <p>
 * See the documentation of the <b>org.eclipse.ui.navigator.navigatorContent</b>
 * extension point and {@link CommonActionProvider} for more information on
 * contributing actions.
 * </p> 
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see CommonActionProvider
 * 
 * @since 3.2
 */
public interface ICommonActionExtensionSite {

	/**
	 * By default, the extension state model returned is for the associated
	 * content extension (if this is NOT a top-level action provider).
	 * Otherwise, clients may use
	 * {@link INavigatorContentService#findStateModel(String)} to locate the
	 * state model of another content extension.
	 * 
	 * @return The extension state model of the associated Content Extension (if
	 *         any) or a state model specifically for this
	 *         ICommonActionProvider.
	 * @see IExtensionStateModel
	 */
	IExtensionStateModel getExtensionStateModel();

	/**
	 * 
	 * @return The unique identifier of the associated content extension or the
	 *         top-level Common Action Provider.
	 */
	String getExtensionId();

	/**
	 * 
	 * @return The associated content service for the instantiated Common Action
	 *         Provider.
	 */
	INavigatorContentService getContentService();

	/**
	 * 
	 * @return The associated structured viewer for the instantiated Common
	 *         Action Provider.
	 */
	StructuredViewer getStructuredViewer();

	/**
	 * 
	 * @return The ICommonViewerSite from the CommonViewer.
	 */
	ICommonViewerSite getViewSite();
}
