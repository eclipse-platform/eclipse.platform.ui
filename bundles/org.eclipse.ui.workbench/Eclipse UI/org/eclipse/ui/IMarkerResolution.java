package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IMarker;
 
/** 
 * Resolution for a marker. When run, a resolution would 
 * typically eliminate the need for the marker.
 * 
 * @since 2.0
 */
public interface IMarkerResolution {
    /** 
     * Returns a short label indicating what the resolution will do. 
     * 
     * @return a short label for this resolution
     */ 
    public String getLabel(); 
    	
	/**
	 * Runs this resolution.
	 * 
	 * @param marker the marker to resolve
	 */
	public void run(IMarker marker);
}

