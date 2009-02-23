/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.workbench.MPerspective;
import org.eclipse.e4.ui.model.workbench.MWorkbenchWindow;
import org.eclipse.e4.workbench.ui.internal.Workbench;

public interface ILegacyHook {
	public void init(Workbench e4Workbench, MApplication<MWorkbenchWindow> workbench);
	public void loadMenu(MMenu menuModel);
	public void loadPerspective(MPerspective<?> perspModel);
}
