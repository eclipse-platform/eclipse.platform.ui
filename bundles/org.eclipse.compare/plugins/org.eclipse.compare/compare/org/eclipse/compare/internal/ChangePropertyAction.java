/*
 * Copyright (c) 2000, 2003 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
	private ResourceBundle fBundle;
	private String fPrefix;


	public ChangePropertyAction(ResourceBundle bundle, CompareConfiguration cc, String rkey, String pkey) {
		fPropertyKey= pkey;
		fBundle= bundle;
		fPrefix= rkey;
		Utilities.initAction(this, fBundle, fPrefix);
		setCompareConfiguration(cc);
	}

	public void run() {
		boolean b= !Utilities.getBoolean(fCompareConfiguration, fPropertyKey, false);
		setChecked(b);
		if (fCompareConfiguration != null)
			fCompareConfiguration.setProperty(fPropertyKey, new Boolean(b));
	}

	public void setChecked(boolean state) {
		super.setChecked(state);
		Utilities.initToggleAction(this, fBundle, fPrefix, state);
	}
	
	public void setCompareConfiguration(CompareConfiguration cc) {
		fCompareConfiguration= cc;
		setChecked(Utilities.getBoolean(fCompareConfiguration, fPropertyKey, false));
	}
}
