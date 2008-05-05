/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.views;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.cheatsheets.CheatSheetListener;
import org.eclipse.ui.cheatsheets.CheatSheetViewerFactory;
import org.eclipse.ui.cheatsheets.ICheatSheetEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.composite.parser.ICompositeCheatsheetTags;
import org.eclipse.ui.internal.cheatsheets.state.MementoStateManager;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetViewer;
import org.eclipse.ui.internal.provisional.cheatsheets.IEditableTask;
import org.eclipse.ui.internal.provisional.cheatsheets.TaskEditor;

public class CheatsheetTaskEditor extends TaskEditor {
	private CheatSheetViewer viewer;
	private IEditableTask task;

	public void createControl(Composite parent, FormToolkit toolkit) {
		viewer = (CheatSheetViewer)CheatSheetViewerFactory.createCheatSheetView();
		viewer.createPartControl(parent);
	}
	
	public Control getControl() {
		return viewer.getControl();
	}


	public void setInput(IEditableTask task, IMemento memento) {
		this.task = task;	
		Dictionary params = task.getParameters();
		String id = (String)params.get(ICompositeCheatsheetTags.CHEATSHEET_TASK_ID);
		String path = (String)params.get(ICompositeCheatsheetTags.CHEATSHEET_TASK_PATH);
		boolean showIntro = true;
		String showIntroParam = (String)params.get(ICompositeCheatsheetTags.CHEATSHEET_TASK_SHOW_INTRO);
		if (showIntroParam != null) {
			showIntro = showIntroParam.equalsIgnoreCase("true"); //$NON-NLS-1$
		}
		
		MementoStateManager stateManager = new MementoStateManager(memento, task.getCompositeCheatSheet().getCheatSheetManager());
		if (path != null) {
			URL url;
			try {
				url = task.getInputUrl(path);
				if (id == null) { 
					id = task.getId();
				}
				if (url != null) {
				    viewer.setInput(id, task.getName(), url, stateManager, false);	
				} else {
					errorBadUrl(path);
				}
			} catch (MalformedURLException e) {
				errorBadUrl(path);
			}
		} else if (id != null){
		    viewer.setInput(id, stateManager);
		} else {
			viewer.showError(Messages.CHEATSHEET_TASK_NO_ID);
		}
		if (!showIntro) {
			viewer.advanceIntroItem();
		}
		viewer.addListener(new TaskListener());
	}

	private void errorBadUrl(String path) {
		String message = NLS.bind(Messages.ERROR_OPENING_FILE_IN_PARSER, (new Object[] {path}));			
		viewer.showError(message);
	}
	
	/*
	 * Listener for the cheatsheet used by this class
	 */
	private class TaskListener extends CheatSheetListener {

		public void cheatSheetEvent(ICheatSheetEvent event) {
			if (event.getEventType() == ICheatSheetEvent.CHEATSHEET_COMPLETED) {
				task.complete();
			}	
		}	
	}

	public void saveState(IMemento memento) {
		viewer.saveState(memento);
	}
}
