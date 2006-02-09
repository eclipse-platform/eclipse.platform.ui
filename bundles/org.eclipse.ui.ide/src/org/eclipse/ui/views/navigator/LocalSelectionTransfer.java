/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.navigator;


/**
 * A LocalSelectionTransfer may be used for drag and drop operations
 * within the same instance of Eclipse.
 * The selection is made available directly for use in the DropTargetListener.
 * dropAccept method. The DropTargetEvent passed to dropAccept does not contain
 * the drop data. The selection may be used for validation purposes so that the
 * drop can be aborted if appropriate.
 *
 * This class is not intended to be subclassed.
 * 
 * @since 2.1
 */
public class LocalSelectionTransfer extends org.eclipse.jface.util.LocalSelectionTransfer {

    private static final LocalSelectionTransfer INSTANCE = new LocalSelectionTransfer();

    /**
     * Only the singleton instance of this class may be used. 
     */
    private LocalSelectionTransfer() {
    }

    /**
     * Returns the singleton.
     * @return the singleton
     */
    public static LocalSelectionTransfer getInstance() {
        return INSTANCE;
    }

}
