/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.cheatsheets.pattern.actions;

import org.eclipse.ui.cheatsheets.ICheatSheetManager;

public class FactoryCustomizeAction extends CustomizeAction {
	public void run(String[] params, ICheatSheetManager csm) {
		System.out.println("In FactoryCustomizeAction");
		super.run(params, csm);
	}
}
