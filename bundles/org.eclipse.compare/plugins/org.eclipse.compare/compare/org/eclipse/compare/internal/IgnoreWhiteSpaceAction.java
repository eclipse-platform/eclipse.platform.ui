/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.util.ResourceBundle;

import org.eclipse.compare.*;

/**
 * Toggles the <code>ICompareConfiguration.IGNORE_WS</code> property of an
 * <code>ICompareConfiguration</code>.
 */
public class IgnoreWhiteSpaceAction extends ChangePropertyAction {

	public IgnoreWhiteSpaceAction(ResourceBundle bundle, CompareConfiguration cc) {
		super(bundle, cc, "action.IgnoreWhiteSpace.", CompareConfiguration.IGNORE_WHITESPACE); //$NON-NLS-1$
	}
}
