package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.apache.tools.ant.BuildException;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;

/**
 * Exception to indicate the build was canceled.
 */
public class BuildCanceledException extends BuildException {

	public BuildCanceledException() {
		super(ToolMessages.getString("BuildCanceledException.canceled")); //$NON-NLS-1$;
	}
}