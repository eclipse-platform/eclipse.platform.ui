/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part.intro;

import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * 
 * <em>EXPERIMENTAL</em>
 * 
 * @since 3.0
 */
public abstract class IntroPart extends WorkbenchPart implements IIntroPart {


	/* (non-Javadoc)
	 * @see org.eclipse.ui.intro.IIntroPart#getIntroSite()
	 */
	public IIntroSite getIntroSite() {
		return (IIntroSite) getSite();
	}
}
