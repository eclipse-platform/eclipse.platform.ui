package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
********************************************************************/

/**
 * Action delegate to open the external tools launch configuration dialog
 */

public class AntRunWithActionDelegate extends AntRunActionDelegate {
	
	public AntRunWithActionDelegate() {
		runWith= true;
	}

}
