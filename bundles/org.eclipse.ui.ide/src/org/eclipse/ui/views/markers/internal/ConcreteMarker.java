/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import com.ibm.icu.text.CollationKey; 
import com.ibm.icu.text.Collator; 

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * This is a concrete class that stores the same type of information as the IMarkers
 * used by the IDE. This class exists as an optimization. The various get* methods
 * on IMarker are extremely slow, which makes it very slow to sort markers (for example,
 * in the problems view). This marker class stores the fields in the most efficient form
 * for sorting and display, but necessarily removes some generality from IMarker.
 */
public class ConcreteMarker extends MarkerNode{

    private String description;

    private String resourceName;

    private String inFolder;

    private CollationKey descriptionKey;

    private CollationKey resourceNameKey;

    private int line;
    
    private String locationString;

    private long creationTime;

    private String type;

    private IMarker marker;

    /**
     * Cache for the marker ID.
     */
	private long id = -1L;

	private MarkerNode markerCategory;

	private String shortFolder;
	
	private Object group;

	public ConcreteMarker(IMarker toCopy) {
        marker = toCopy;
        refresh();
    }

    /**
     * Clears any cached information. This frees up some memory, but will slow down
     * the next comparison operation. It is a good idea to call this on a set of markers
     * after sorting them, in order to reduce their memory cost. 
     */
    public void clearCache() {
        resourceNameKey = null;
        descriptionKey = null;
    }

    /**
     * Refresh the properties of this marker from the underlying IMarker instance
     */
    public void refresh() {
        clearCache();

        description = Util.getProperty(IMarker.MESSAGE, marker);
        resourceName = Util.getResourceName(marker);
        inFolder = Util.getContainerName(marker);
        shortFolder = null;
        line = marker.getAttribute(IMarker.LINE_NUMBER, -1);  
        locationString = marker.getAttribute(IMarker.LOCATION,
				Util.EMPTY_STRING);
		
        try {
            creationTime = marker.getCreationTime();
        } catch (CoreException e) {
            creationTime = 0;
        }

        try {
            type = marker.getType();
        } catch (CoreException e1) {
            type = Util.EMPTY_STRING; 
        }
        
        // store the marker ID locally
        id = marker.getId();
    }

    public IResource getResource() {
        return marker.getResource();
    }

    public String getType() {
        return type;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.MarkerNode#getDescription()
     */
    public String getDescription() {
        return description;
    }

    public CollationKey getDescriptionKey() {
        if (descriptionKey == null) {
            descriptionKey = Collator.getInstance()
                    .getCollationKey(description);
        }

        return descriptionKey;
    }

    public String getResourceName() {
        return resourceName;
    }

    public CollationKey getResourceNameKey() {
        if (resourceNameKey == null) {
            resourceNameKey = Collator.getInstance().getCollationKey(
                    resourceName);
        }
        return resourceNameKey;
    }

    public int getLine() {
        return line;
    }

    public String getFolder() {
        return inFolder;
    }

    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * The underlying marker ID value.
     * @return the marker's ID.
     */
    public long getId() {
    	return id;
    }

    public IMarker getMarker() {
        return marker;
    }

    public boolean equals(Object object) {
        if (!(object instanceof ConcreteMarker)) {
            return false;
        }

        ConcreteMarker other = (ConcreteMarker) object;

        return other.getMarker().equals(getMarker());
    }

    public int hashCode() {
        return getMarker().hashCode();
    }

	/**
	 * Set the category the receiver is in.
	 * @param category
	 */
	public void setCategory(MarkerNode category) {
		markerCategory = category;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getChildren()
	 */
	public MarkerNode[] getChildren() {
		return Util.EMPTY_MARKER_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getParent()
	 */
	public MarkerNode getParent() {
		return markerCategory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#isConcrete()
	 */
	public boolean isConcrete() {
		return true;
	}

	/**
	 * Return the short name for the folder.
	 * @return String
	 */
	public String getShortFolder() {
		if(shortFolder == null) {
			shortFolder = Util.getShortContainerName(marker);
		}
		return shortFolder;
	}


	/**
	 * Get the location string. If the {@link IMarker#LOCATION }
	 * attribute was not set then return an empty String.
	 * @return String
	 */
	public String getLocationString() {
		return locationString;
	}


	/**
	 * Get the group for the reciever.
	 * @return Returns the group.
	 */
	public Object getGroup() {
		return group;
	}

	/**
	 * Set the group name.
	 * @param group the group name
	 */
	public void setGroup(Object group) {
		this.group = group;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getConcreteRepresentative()
	 */
	public ConcreteMarker getConcreteRepresentative() {
		return this;
	}
}
