package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.*;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * This is a view descriptor. It provides a "description" of a given
 * given view so that the view can later be constructed.
 * <p>
 * [Issue: This interface is not exposed in API, but time may
 * demonstrate that it should be.  For the short term leave it be.
 * In the long term its use should be re-evaluated. ]
 * </p>
 * <p>
 * The view registry provides facilities to map from an extension
 * to a IViewDescriptor.
 * </p>
 * 
 */
public interface IViewDescriptor {
/**
 * Create an instance of the view defined in the descriptor.
 */
public IViewPart createView() throws CoreException;
/**
 * Returns an array of strings that represent
 * view's category path. This array will be used
 * for hierarchical presentation of the
 * view in places like submenus.
 * @return array of category tokens or null if not specified.
 */
public String[] getCategoryPath();
/**
 * Return the configuration element which contributed this view.
 */
public IConfigurationElement getConfigurationElement();
/**
 * Return the id of the view.
 */
public String getID() ;
/**
 * Return the descriptor for the icon to show for this view.
 */
public ImageDescriptor getImageDescriptor();
/**
 * Return the label to show for this view.
 */
public String getLabel() ;
}
