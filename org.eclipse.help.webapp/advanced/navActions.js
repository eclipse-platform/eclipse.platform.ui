/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

var isIE = navigator.userAgent.indexOf('MSIE') != -1;

function resynchNav(button)
{
	try {
		parent.parent.parent.parent.ContentFrame.ContentToolbarFrame.resynch(button);
	} catch(e){
	}
	if (isIE && button && document.getElementById(button)){
		document.getElementById(button).blur();
	}
}
