package org.eclipse.ant.tests.core.tasks;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Echo;
import org.eclipse.ant.tests.core.types.AntTestType;


public class AntTestTaskWithCustomType extends Task {
	
	AntTestType testType;
	
	public AntTestTaskWithCustomType() {
		super();
	}
	
	public void addAntTestType(AntTestType type) {
		testType= type;
	}
	
	/**
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		Echo echo= new Echo();
		echo.setProject(getProject());
		echo.setMessage(testType.getMessage());
		echo.execute();
	}
}
