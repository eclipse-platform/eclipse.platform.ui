/*******************************************************************************
 * Copyright (c) 2009 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator;

import org.eclipse.swt.widgets.Item;

/**
 * Allows improved performance by optimizing label updates of the CommonViewer.
 * 
 * Often label updates come in the form of resource updates, and the resources
 * themselves are not directly stored in the {@link CommonViewer}; instead other model
 * objects are stored.  In addition, it may be the case where the objects that
 * have changed are not present in the Tree associated with the CommonViewer
 * because they have not been opened or expanded.
 * 
 * You can use an instance of this class to define a mapping between the current
 * Tree Items associated with the CommonViewer and the (typically resource) objects
 * where a change is notified.  The change is notified to the 
 * {@link CommonViewer#handleLabelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)}.
 * 
 * The mapper object (implementing this interface) is associated with the CommonViewer using 
 * the {@link CommonViewer#setMapper(ICommonViewerMapper)} method.
 * 
 * When this mapper wishes to update the CommonViewer, the 
 * {@link CommonViewer#doUpdateItem(org.eclipse.swt.widgets.Widget)} 
 * method is used.
 * 
 * @since 3.4
 * @noextend This interface is not intended to be extended by clients.
 *
 */
public interface ICommonViewerMapper {
	
	/**
	 * Adds a new item to the map.  Called by the {@link CommonViewer} when the element is
	 * added to the Tree.
	 * 
	 * @param element Element to map
	 * @param item The item used for the element
	 */
	public void addToMap(Object element, Item item);

	/**
	 * Removes an element from the map. Called by the {@link CommonViewer} when the element is
	 * removed from the Tree.
	 * 
	 * @param element The data element
	 * @param item The table or tree item
	 */
	public void removeFromMap(Object element, Item item);

	/**
	 * Clears the map.
	 */
	public void clearMap();

	/**
	 * Tests if the map is empty
	 * @return Returns if there are mappings
	 */
	public boolean isEmpty();
	
	/**
	 * Tests if this mapper handles the specified object.  This is used only
	 * by the viewer to determine if the object should be looked up in the mapper.
	 * This is not to be used when the object is being added to the mapper, as
	 * the mapper may adapt to the desired object to be mapped (like adapting
	 * from a Java model object to a resource object for example).
	 * 
	 * @param object the object that the mapper can handle
	 * @return true if it does
	 */
	public boolean handlesObject(Object object);
		
	/**
	 * Indicates the specified object has changed.  If the object
	 * has a corresponding Item in the map, the associated {@link CommonViewer}
	 * is notified of the change using the {@link CommonViewer#doUpdateItem(org.eclipse.swt.widgets.Widget)} 
	 * method so that it can update its state.
	 * 
	 * @param object the object that changed
	 */
	public void objectChanged(Object object);
	
	
	

	
}
