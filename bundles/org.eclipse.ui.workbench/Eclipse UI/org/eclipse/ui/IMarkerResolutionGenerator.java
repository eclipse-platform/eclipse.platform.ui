package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IMarker;
 
/** 
 * Creates resolutions for a given marker. 
 * When run, a resolution would typically eliminate 
 * the need for the marker.
 * 
 * @since 2.0
 */
public interface IMarkerResolutionGenerator {
    /** 
     * Returns resolutions for the given marker (may
     * be empty). 
     * 
     * @return resolutions for the given marker
     */ 
    public IMarkerResolution[] getResolutions(IMarker marker); 
}

