/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;


public class ReplaceWithPreviousEditionAction extends EditionAction {
		
	public ReplaceWithPreviousEditionAction() {
		super(true, "org.eclipse.compare.internal.ReplaceWithEditionAction"); //$NON-NLS-1$
		fPrevious= true;
	}
}
