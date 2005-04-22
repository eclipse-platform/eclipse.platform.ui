/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.jface.action.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.forms.IFormPart;

public interface IHelpPart extends IFormPart {
	void init(ReusableHelpPart parent, String id, IMemento memento);
	void saveState(IMemento memento);
	Control getControl();
	String getId();
	void setVisible(boolean visible);
	boolean hasFocusControl(Control control);
	boolean fillContextMenu(IMenuManager manager);
	IAction getGlobalAction(String id);
	void stop();
	void toggleRoleFilter();
	void refilter();
}
