/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.webapp;

/**
 * A view which contributes a frame to the help webapp
 * @since 3.5
 */

public abstract class AbstractFrame implements Comparable {
	
	/**
	 * Constant returned from getFrameLocation() function to indicate that
	 * the frame should be created below the content frame or the Main Help Toolbar
	 */
	public static final int BELOW_CONTENT = 1;
	public static final int HELP_TOOLBAR = 2;
	
	/**
	 * Function which defines the frame location
	 * @return a constant defined in this class which indicates the location of this frame
	 */
	public abstract int getLocation();
	
	/**
	 * 
	 * @return a non translated name which is the name of this frame
	 */
    public abstract String getName();

    /**
     * @return a URL path, relative to /help which is the 
     * location of the jsp files in the advanced presentation
     */
    public abstract String getURL();
    
    /**
     * @return a string which will be used in the rows or cols attribute of a 
     * frameset in the html
     */
    public String getSize() {
    	return "*"; //$NON-NLS-1$
    }

    /**
     * @return true if the frame should be shown in the advanced presentation
     */
    public boolean isVisible() {
        return true;
    }
    
    /**
     * allows the attributes of this frame other than name and src to be specified
     * @return a list of attributes
     */
    public String getFrameAttributes() {
    	return "\"marginwidth=\"1\" marginheight=\"1\" frameborder=\"1\" scrolling=\"no\""; //$NON-NLS-1$
    }
    
    final public int compareTo(Object o) {
    	if (o instanceof AbstractFrame) {
    		String objectName = ((AbstractFrame)o).getName();
			return (getName().compareTo(objectName));
    	}
    	return 0;
    }
    
}
