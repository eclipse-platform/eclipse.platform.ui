/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.util.ResourceBundle;

import org.eclipse.compare.*;

/**
 * Toggles the <code>ICompareConfiguration.SHOW_PSEUDO_CONFLICTS</code> property of an
 * <code>ICompareConfiguration</code>.
 */
public class ShowPseudoConflicts extends ChangePropertyAction {

	public ShowPseudoConflicts(ResourceBundle bundle, CompareConfiguration cc) {
		super(bundle, cc, "action.ShowPseudoConflicts.", CompareConfiguration.SHOW_PSEUDO_CONFLICTS); //$NON-NLS-1$
	}
}
