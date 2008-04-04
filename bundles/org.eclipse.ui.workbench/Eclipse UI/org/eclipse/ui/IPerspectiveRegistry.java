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
package org.eclipse.ui;

/**
 * The workbench's global registry of perspectives. 
 * <p>
 * This registry contains a descriptor for each perspectives in the workbench.
 * It is initially populated with stock perspectives from the workbench's 
 * perspective extension point (<code>"org.eclipse.ui.perspectives"</code>) and 
 * with custom perspectives defined by the user.
 * </p><p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @see IWorkbench#getPerspectiveRegistry
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPerspectiveRegistry {
    /**
     * Clones an existing perspective.
     * 
     * @param id the id for the cloned perspective, which must not already be used by 
     *   any registered perspective
     * @param label the label assigned to the cloned perspective
     * @param desc the perspective to clone
     * @return the cloned perspective descriptor
     * @throws IllegalArgumentException if there is already a perspective with the given id 
     * 
     * @since 3.0
     */
    public IPerspectiveDescriptor clonePerspective(String id, String label,
            IPerspectiveDescriptor desc) throws IllegalArgumentException;

	/**
	 * Deletes a perspective. Has no effect if the perspective is defined in an
	 * extension.
	 * 
	 * @param persp the perspective to delete
	 * @since 3.2
	 */
	public void deletePerspective(IPerspectiveDescriptor persp);
    
    /**
     * Finds and returns the registered perspective with the given perspective id.
     *
     * @param perspectiveId the perspective id 
     * @return the perspective, or <code>null</code> if none
     * @see IPerspectiveDescriptor#getId
     */
    public IPerspectiveDescriptor findPerspectiveWithId(String perspectiveId);

    /**
     * Finds and returns the registered perspective with the given label.
     *
     * @param label the label
     * @return the perspective, or <code>null</code> if none
     * @see IPerspectiveDescriptor#getLabel
     */
    public IPerspectiveDescriptor findPerspectiveWithLabel(String label);

    /**
     * Returns the id of the default perspective for the workbench.  This identifies one
     * perspective extension within the workbench's perspective registry.
     * <p>
     * Returns <code>null</code> if there is no default perspective.
     * </p> 
     *
     * @return the default perspective id, or <code>null</code>
     */
    public String getDefaultPerspective();

    /**
     * Returns a list of the perspectives known to the workbench.
     *
     * @return a list of perspectives
     */
    public IPerspectiveDescriptor[] getPerspectives();

    /**
     * Sets the default perspective for the workbench to the given perspective id.
     * If non-<code>null</code>, the id must correspond to a perspective extension 
     * within the workbench's perspective registry.
     * <p>
     * A <code>null</code> id indicates no default perspective.
     * </p>
     *
     * @param id a perspective id, or <code>null</code>
     */
    public void setDefaultPerspective(String id);

    /**
     * Reverts a perspective back to its original definition
     * as specified in the plug-in manifest.
     * 
     * @param perspToRevert the perspective to revert  
     * 
     * @since 3.0
     */
    public void revertPerspective(IPerspectiveDescriptor perspToRevert);
}
