/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

/**
 * A content stamp object represent the content of an <code>IFile</code>.
 * A content stamp object is updated whenever the content of a file
 * changes. In contrast to a modification stamp a content stamp is reverted
 * to its previous value if the content of the file is reverted back by
 * performing a corresponding undo change.
 * <p>
 * Clients of the refactoring framework don't need to take care of content
 * stamps. They are managed by the framework itself. 
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * 
 * @since 3.0
 */
public abstract class ContentStamp {

}
