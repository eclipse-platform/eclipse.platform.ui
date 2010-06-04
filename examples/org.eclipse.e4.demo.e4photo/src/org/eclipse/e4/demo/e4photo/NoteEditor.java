/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.e4photo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.MInput;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class NoteEditor {

	@Inject
	private Composite parent;
	
	@Inject
	private MUILabel uiItem;
	
	private StyledText text;
	private final String inputURI;
	private final MDirtyable dirtyable;
	
	@Inject
	public NoteEditor(MInput input, MDirtyable dirtyable) {
		super();
		inputURI = input.getInputURI();
		this.dirtyable = dirtyable;
	}

	@PostConstruct
	void init() {
		parent.setLayout(new FillLayout());
		text = new StyledText(parent, SWT.MULTI | SWT.WRAP);
		
		Path fullPath = new Path(inputURI);
		String shortName = fullPath.segment(fullPath.segmentCount() - 1);
		uiItem.setLabel("Notes - " + shortName);
		String noteText = readNote();
		if (noteText != null)
			text.setText(noteText);
		
		// add modify listener after the initial contents is set
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dirtyable.setDirty(true);
			}});
	}
	
	public boolean isSaveOnCloseNeeded() {
		return true;
	}
	
	@Persist
	public void doSave(@Optional IProgressMonitor monitor) {
		writeNote(text.getText());
		dirtyable.setDirty(false);
	}
	
	private String readNote() {
		FileReader reader;
		try {
			reader = new FileReader(inputURI);
		} catch (FileNotFoundException e) {
			return null; // this is OK - new file
		}
		StringBuffer stringBuffer = new StringBuffer();
		char[] buffer = new char[1024];
		try {
			for(;;) {
				int charsRead = reader.read(buffer);
				if (charsRead == -1)
					break;
				stringBuffer.append(buffer, 0, charsRead);
			}
		} catch (IOException e) {
			System.err.println("Unable to read file " + inputURI);
			e.printStackTrace();
			return null;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return stringBuffer.toString();
	}
	
	private boolean writeNote(String noteText) {
		try {
			File file = new File(inputURI);
			if (!file.exists())
				file.createNewFile();
			FileWriter writer = new FileWriter(file);
			writer.write(noteText);
			writer.close();
		} catch (IOException e) {
			System.err.println("Unable to write file " + inputURI);
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
