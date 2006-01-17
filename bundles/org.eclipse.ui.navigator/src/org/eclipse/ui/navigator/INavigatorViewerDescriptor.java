package org.eclipse.ui.navigator;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.navigator.internal.extensions.InsertionPoint;

/**
 * Provides a basic metadata about the abstract viewer for a particular
 * content service.
 * 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
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
	 * The default value of the popup menu id is the viewer id. Clients may
	 * override this value using a <b>navigatorConfiguration</b> extension.
	 * 
	 * @return The id of the context menu of the viewer.
	 */
	String getPopupMenuId();

	/**
	 * Returns true if the content extension of the given id is 'visible'. A
	 * content extension is 'visible' if it matches a viewerContentBinding for
	 * the given viewer id.
	 * 
	 * @param aContentExtensionId
	 *            The id to query
	 * @return True if the content extension matches a viewerContentBinding for
	 *         the viewer id of this descriptor.
	 */
	boolean isVisibleContentExtension(String aContentExtensionId);

	/**
	 * Returns true if the action extension of the given id is 'visible'. An
	 * action extension is 'visible' if it matches a viewerActionBinding for the
	 * given viewer id.
	 * 
	 * @param anActionExtensionId
	 *            The id to query
	 * @return True if the action extension matches a viewerActionBinding for
	 *         the viewer id of this descriptor.
	 */
	boolean isVisibleActionExtension(String anActionExtensionId);

	/**
	 * Returns true if the content extension of the given id matches a
	 * viewerContentBinding extension that declares isRoot as true.
	 * 
	 * @param aContentExtensionId
	 *            The id to query
	 * @return True if the content extension matches a viewerContentBinding
	 *         which declares 'isRoot' as true for the viewer id of this
	 *         descriptor.
	 */
	boolean isRootExtension(String aContentExtensionId);

	/**
	 * Returns true if there exists at least one matching viewerContentBinding
	 * which declares isRoot as true. This behavior will override the default
	 * enablement for the viewer root.
	 * 
	 * @return True if there exists a matching viewerContentBinding which
	 *         declares isRoot as true.
	 */
	boolean hasOverriddenRootExtensions();

	/**
	 * Returns true by default. A true value indicates that object and view
	 * contributions should be supported by the popup menu of any viewer
	 * described by this viewer descriptor. The value may be overridden from the
	 * &lt;popupMenu /&gt; child element of the &lt;viewer /&gt; element in the
	 * <b>org.eclipse.ui.navigator.viewer</b> extension point.
	 * 
	 * @return True if object/view contributions should be allowed or False
	 *         otherwise.
	 */
	boolean allowsPlatformContributionsToContextMenu();

	/**
	 * 
	 * Custom insertion points are declared through a nested 'popupMenu' element
	 * in the <b>org.eclipse.ui.navigator.viewer</b> extension point. Each
	 * insertion point represents either a {@link Separator} or {@link GroupMarker} in
	 * the context menu of the viewer.<p>
	 * 
	 * @return The set of custom insertion points, if any. A null list indicates
	 *         the default set (as defined by {@link NavigatorActionService})
	 *         should be used. An empty list indicates there are no declarative
	 *         insertion points.
	 */
	InsertionPoint[] getCustomInsertionPoints();
 
}