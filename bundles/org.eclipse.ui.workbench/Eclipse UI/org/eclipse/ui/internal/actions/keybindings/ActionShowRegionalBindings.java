/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions.keybindings;

import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedSet;

import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchMessages;

public class ActionShowRegionalBindings extends org.eclipse.jface.action.Action {

	public ActionShowRegionalBindings() {
		super(WorkbenchMessages.getString("ActionShowRegionalBindings.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("ActionShowRegionalBindings.toolTip")); //$NON-NLS-1$
		//WorkbenchHelp.setHelp(this, IHelpContextIds.ACTION_SHOW_REGIONAL_BINDINGS);
	}

	public void run() {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot("regionalBindingSet");
		SortedSet regionalBindingSet = Registry.getInstance().getRegionalBindingSet();
		KeyManager.writeRegionalBindingSet(xmlMemento, regionalBindingSet);
		
		try {
			String name = "c:\\ActionShowRegionalBindings.xml";
			FileWriter fileWriter = new FileWriter(name);
			xmlMemento.save(fileWriter);
			fileWriter.close();
			Runtime.getRuntime().exec(name);	
		} catch (IOException eIO) {			
		}
	}	
}
