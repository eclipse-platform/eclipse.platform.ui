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
package org.eclipse.ant.tests.ui.support.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.types.EnumeratedAttribute;


public class AntTestTask2 extends Task {
	
	String fMessage= "Default";
	
	public AntTestTask2() {
		super();
	}
	
	/**
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		Echo echo= new Echo();
		echo.setProject(getProject());
		echo.setMessage("Testing Ant in Eclipse with a custom task2: " + fMessage);
		echo.execute();
	}
	
    public void setMessage(Cool attr) {
        fMessage = attr.getValue();
    }
	
	/**
     * Enumerated attribute with the values "cool", "chillin" and "awesome".
     */
    public static class Cool extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"cool", "chillin", "awesome"};
        }
    }
}
