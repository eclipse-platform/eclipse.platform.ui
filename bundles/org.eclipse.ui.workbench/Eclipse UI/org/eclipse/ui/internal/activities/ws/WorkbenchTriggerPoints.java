/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

/**
 * @since 3.1
 */
public interface WorkbenchTriggerPoints {

    /**
     * New wizard trigger point identifier.  Value <code>org.eclipse.ui.newWizards</code>.
     */
    public static final String NEW_WIZARDS = "org.eclipse.ui.newWizards"; //$NON-NLS-1$
    
    /**
     * Perspective opening trigger point identifier.  Value <code>org.eclipse.ui.openPerspectiveDialog</code>.
     */
    public static final String OPEN_PERSPECITVE_DIALOG = "org.eclipse.ui.openPerspectiveDialog"; //$NON-NLS-1$
    
    /**
     * Import wizards trigger point identifier.  Value <code>org.eclipse.ui.importWizards</code>.
     */
    public static final String IMPORT_WIZARDS = "org.eclipse.ui.importWizards"; //$NON-NLS-1$
    
    /**
     * Export wizards trigger point identifier.  Value <code>org.eclipse.ui.exportWizards</code>.
     */
    public static final String EXPORT_WIZARDS = "org.eclipse.ui.exportWizards"; //$NON-NLS-1$

}
