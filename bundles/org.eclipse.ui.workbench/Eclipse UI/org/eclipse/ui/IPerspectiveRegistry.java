/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
 */
public interface IPerspectiveRegistry {
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
 * <p><p>
 * On startup of the platform UI the default perspective is determined using a 
 * multistep process.
 * </p>
 * <ol>
 *   <li>Initially the <code>Resource Perspective</code> is default. </li>
 *   <li>If a single perspective extension within the registry has a <b>default</b>
 *			attribute it will become the default perspective.  If two or more
 *			extensions have the <b>default</b> attribute the registry will ignore all
 *			of them and select the <code>Resource Perspective</code>. </li>
 *   <li>If the user has set the default perspective within the 
 *			<code>Perspective</code> dialog their preference will be selected 
 *			over all others. </li>
 * </ol>
 * </p>
 *
 * @return the default perspective id; will never be <code>null</code>
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
 * The id must correspond to one perspective extension within the workbench's 
 * perspective registry.
 *
 * @param id a perspective id; must not be <code>null</code>
 */
public void setDefaultPerspective(String id);
}
