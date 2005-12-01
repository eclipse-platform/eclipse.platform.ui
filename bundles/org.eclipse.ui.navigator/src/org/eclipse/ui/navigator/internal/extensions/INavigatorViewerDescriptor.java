package org.eclipse.ui.navigator.internal.extensions;

public interface INavigatorViewerDescriptor {

	/**
	 * Returns the id of the viewer targeted by this extension.
	 * 
	 * @return the id of the viewer targeted by this extension.
	 */
	String getViewerId();

	String getPopupMenuId();

	boolean isVisibleExtension(String aContentExtensionId);

	boolean isRootExtension(String aContentExtensionId);

	boolean hasOverriddenRootExtensions();

}