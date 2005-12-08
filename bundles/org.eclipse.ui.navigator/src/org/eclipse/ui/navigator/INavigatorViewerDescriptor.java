package org.eclipse.ui.navigator;

/**
 * Provides a handle to metadata about the abstract
 * viewer for a particular content service. 
 * 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 *
 */
public interface INavigatorViewerDescriptor {

	/**
	 * Returns the id of the viewer targeted by this extension.
	 * 
	 * @return the id of the viewer targeted by this extension.
	 */
	String getViewerId();
	
	/**
	 * The default value of the popup menu id is the viewer id.
	 * Clients may override this value using a 
	 * <b>navigatorConfiguration</b> extension.
	 *  
	 * @return The id of the context menu of the viewer.
	 */
	String getPopupMenuId();

	/**
	 * Returns true if the content extension of the given id is 'visible'. 
	 * A content extension is 'visible' if it matches a 
	 * viewerContentBinding for the given viewer id. 
	 * 
	 * @param aContentExtensionId The id to query
	 * @return 
	 * 	True if the content extension matches a 
	 * 	viewerContentBinding for the viewer id of this descriptor.
	 */
	boolean isVisibleContentExtension(String aContentExtensionId);
	

	/**
	 * Returns true if the action extension of the given id is 'visible'. 
	 * An action extension is 'visible' if it matches a 
	 * viewerActionBinding for the given viewer id. 
	 * 
	 * @param anActionExtensionId The id to query
	 * @return 
	 * 	True if the action extension matches a 
	 * 	viewerActionBinding for the viewer id of this descriptor.
	 */
	boolean isVisibleActionExtension(String anActionExtensionId);

	/**
	 * Returns true if the content extension of the given id 
	 * matches a viewerContentBinding extension that declares
	 * isRoot as true. 
	 * 
	 * @param aContentExtensionId The id to query
	 * @return True if the content extension matches
	 *  a viewerContentBinding which declares 'isRoot'
	 *  as true for the viewer id of this descriptor. 
	 */
	boolean isRootExtension(String aContentExtensionId);

	/**
	 * Returns true if there exists at least one matching
	 * viewerContentBinding which declares isRoot as true. 
	 * This behavior will override the default enablement
	 * for the viewer root. 
	 * @return True if there exists a matching viewerContentBinding
	 *   which declares isRoot as true.
	 */
	boolean hasOverriddenRootExtensions();

}