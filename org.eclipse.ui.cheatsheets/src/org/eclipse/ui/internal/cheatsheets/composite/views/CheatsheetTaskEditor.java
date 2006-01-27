/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.cheatsheets.CheatSheetListener;
import org.eclipse.ui.cheatsheets.CheatSheetViewerFactory;
import org.eclipse.ui.cheatsheets.ICheatSheetEvent;
import org.eclipse.ui.cheatsheets.ICheatSheetViewer;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.cheatsheets.ITaskEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.cheatsheets.composite.parser.ICompositeCheatsheetTags;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetViewer;

public class CheatsheetTaskEditor implements ITaskEditor {
	private ICheatSheetViewer viewer;
	private ICompositeCheatSheetTask task;

	public void createControl(Composite parent, FormToolkit toolkit) {
		viewer = CheatSheetViewerFactory.createCheatSheetView();
		viewer.createPartControl(parent);
	}
	
	public Control getControl() {
		return viewer.getControl();
	}

	public void start(ICompositeCheatSheetTask task) {
		this.task = task;
		Dictionary params = task.getParameters();
		String id = (String)params.get(ICompositeCheatsheetTags.CHEATSHEET_TASK_ID);
		String path = (String)params.get(ICompositeCheatsheetTags.CHEATSHEET_TASK_PATH);
		((CheatSheetViewer)viewer).setNextSavePath(task.getStateLocation());
		if (path != null) {
			URL url;
			try {
				url = task.getInputUrl(path);
				viewer.setInput(id, task.getName(), url);				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else {
		    viewer.setInput(id);
		}
		((CheatSheetViewer)viewer).addListener(new TaskListener());
	}
	
	/*
	 * Listener for the cheatsheet used by this class
	 */
	private class TaskListener extends CheatSheetListener {

		public void cheatSheetEvent(ICheatSheetEvent event) {
			if (event.getEventType() == ICheatSheetEvent.CHEATSHEET_COMPLETED) {
				task.advanceState();
			}
			
		}
		
	}
}
