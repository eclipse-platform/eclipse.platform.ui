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
package org.eclipse.debug.ui.sourcelookup;

import org.eclipse.ui.IEditorInput;

/**
 * The result of performing source lookup on a debug artifact.
 * The result contains the resolved source element and description
 * of an editor (editor id, and editor input) in which to display
 * the result.
 *
 * @see org.eclipse.debug.ui.DebugUITools#lookupSource(Object, ISourceLocator)
 * @see org.eclipse.debug.ui.DebugUITools#displaySource(ISourceLookupResult, IWorkbenchPage)  
 * @since 3.1
 */
public interface ISourceLookupResult {

    /**
     * Returns the artifact for which source lookup was performed,
     * such as a stack frame.
     * 
     * @return the artifact for which source lookup was performed
     */
    public Object getArtifact();
    
    /**
     * Returns the source element resolved during source lookup,
     * or <code>null</code> if a source element was not resolved.
     * 
     * @return resolved source element or <code>null</code> if unknown
     */
    public Object getSourceElement();
        
    /**
     * Returns the identifier of an editor used to display this result,
     * or <code>null</code> if unknown.
     * 
     * @return the identifier of an editor used to display this result,
     * or <code>null</code> if unknown
     */
    public String getEditorId();
    
    /**
     * Returns the editor input used to display result,
     * or <code>null</code> if unknown.
     * 
     * @return the editor input used to display result,
     * or <code>null</code> if unknown
     */
    public IEditorInput getEditorInput();
}
