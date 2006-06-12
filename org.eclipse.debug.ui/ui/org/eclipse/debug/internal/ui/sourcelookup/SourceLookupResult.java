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
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.ui.IEditorInput;

/**
 * The result of a source lookup contains the source element, editor id, and 
 * editor input resolved for a debug artifact.
 * 
 * @since 3.1
 */
public class SourceLookupResult implements ISourceLookupResult {
    
    /** 
     * Element that source was resolved for.
     */
    private Object fArtifact;
    /**
     * Corresponding source element, or <code>null</code>
     * if unknown.
     */
    private Object fSourceElement;
    /**
     * Associated editor id, used to display the source element,
     * or <code>null</code> if unknown.
     */
    private String fEditorId;
    /**
     * Associated editor input, used to display the source element,
     * or <code>null</code> if unknown.
     */
    private IEditorInput fEditorInput;

    /**
     * Creates a source lookup result on the given artifact, source element, 
     * editor id, and editor input.
     */
    public SourceLookupResult(Object artifact, Object sourceElement, String editorId, IEditorInput editorInput) {
        fArtifact = artifact;
        setSourceElement(sourceElement);
        setEditorId(editorId);
        setEditorInput(editorInput);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.sourcelookup.ISourceLookupResult#getArtifact()
     */
    public Object getArtifact() {
        return fArtifact;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.sourcelookup.ISourceLookupResult#getSourceElement()
     */
    public Object getSourceElement() {
        return fSourceElement;
    }
    
    /**
     * Sets the source element resolved for the artifact that source
     * lookup was performed for, or <code>null</code> if a source element
     * was not resolved.
     * 
     * @param element resolved source element or <code>null</code> if unknown
     */    
    protected void setSourceElement(Object element) {
        fSourceElement = element;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.sourcelookup.ISourceLookupResult#getEditorId()
     */
    public String getEditorId() {
        return fEditorId;
    }
    
    /**
     * Sets the identifier of the editor used to display this source
     * lookup result's source element, or <code>null</code> if unknown.
     * 
     * @param id the identifier of the editor used to display this source
     * lookup result's source element, or <code>null</code> if unknown
     */
    protected void setEditorId(String id) {
        fEditorId = id;
    }    

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.sourcelookup.ISourceLookupResult#getEditorInput()
     */
    public IEditorInput getEditorInput() {
        return fEditorInput;
    }
    
    /**
     * Sets the editor input used to display this source lookup
     * result's source element, or <code>null</code> if unknown.
     * 
     * @param input the editor input used to display this source lookup
     * result's source element, or <code>null</code> if unknown
     */
    protected void setEditorInput(IEditorInput input) {
        fEditorInput = input;
    }    
	
	/**
	 * Updates the artifact to refer to the given artifact
	 * if equal. For example, when a source lookup result is reused
	 * for the same stack frame, we still need to update in case
	 * the stack frame is not identical.
	 * 
	 * @param artifact new artifact state
	 */
	public void updateArtifact(Object artifact) {
		if (fArtifact.equals(artifact)) {
			fArtifact = artifact;
		}
	}
}
