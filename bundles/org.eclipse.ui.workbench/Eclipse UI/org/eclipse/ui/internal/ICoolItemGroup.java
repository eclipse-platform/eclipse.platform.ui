/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/
package org.eclipse.ui.internal;

public interface ICoolItemGroup {
	public String getContributingId();  // id of the action set that contributed the group
	public String getId(); // id of the contribution
}
