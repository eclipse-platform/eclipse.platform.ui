/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
package org.eclipse.compare.internal;

import java.util.ResourceBundle;

import org.eclipse.jface.action.Action;
import org.eclipse.compare.CompareConfiguration;

/**
 * Toggles a boolean property of an <code>ICompareConfiguration</code>.
 */
public class ChangePropertyAction extends Action {

	private CompareConfiguration fCompareConfiguration;
	private String fPropertyKey;


	public ChangePropertyAction(ResourceBundle bundle, CompareConfiguration cc, String rkey, String pkey) {
		fPropertyKey= pkey;
		Utilities.initAction(this, bundle, rkey);
		setCompareConfiguration(cc);
	}

	public void run() {
		boolean b= !Utilities.getBoolean(fCompareConfiguration, fPropertyKey, false);
		setChecked(b);
		if (fCompareConfiguration != null)
			fCompareConfiguration.setProperty(fPropertyKey, new Boolean(b));
	}

	public void setCompareConfiguration(CompareConfiguration cc) {
		fCompareConfiguration= cc;
		setChecked(Utilities.getBoolean(fCompareConfiguration, fPropertyKey, false));
	}
}
