package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.widgets.Event;
 
/** 
 * Resolution for a marker. When run, a resolution would 
 * typically eliminate the need for the marker.
 * 
 * @since 2.0
 */
public interface IMarkerResolution {
	/**
	 * Initializes the resolution with the given marker.
	 * It is assumed that the marker meets all the criteria specified in the 
     * extension point which contributed this resolution. 
	 * 
	 * @param marker the marker for this resolution
	 */
	public void init(IMarker marker);
	
    /** 
     * Returns <code>true</code> if the resolution is appropriate for its marker
     * <code>false</code> otherwise.
     * <p>
     * Implementors should not assume that this method
     * will always be called before <code>runWithEvent</code>
     * is called.
     * </p>
     * 
     * @return <code>true</code> if the resolution is appropriate
     */ 
    public boolean isAppropriate(); 
    
    /** 
     * Returns a short label indicating what the resolution will do. 
     * 
     * @return a short label for this resolution
     */ 
    public String getLabel(); 
    	
	/**
	 * Runs this resolution.
	 */
	public void run();
}

