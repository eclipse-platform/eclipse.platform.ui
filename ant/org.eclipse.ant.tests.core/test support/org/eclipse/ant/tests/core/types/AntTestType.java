package org.eclipse.ant.tests.core.types;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/



public class AntTestType {
	
	String message;
	
	public AntTestType() {
	}
	
	public void setMessage(String message) {
		this.message= message;
	}
	
	public String getMessage() {
		return message;
	}
	
}