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
package org.eclipse.debug.internal.ui.preferences;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

public class ProcessPropertyPage extends PropertyPage {

	/**
	 * Constructor for ProcessPropertyPage
	 */
	public ProcessPropertyPage() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite ancestor) {
		Font font = ancestor.getFont();
		noDefaultAndApplyButton();
		Composite parent = new Composite(ancestor, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);
		
		IProcess proc = getProcess();
		
	//create the process time section
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText(DebugPreferencesMessages.ProcessPropertyPage_0);
		lbl.setLayoutData(gd);
		lbl.setFont(font);
		
		Text text = new Text(parent, SWT.READ_ONLY);
		text.setText(getTimeText(proc));
		text.setFont(font);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 10;
		text.setLayoutData(gd);
		createVerticalSpacer(parent, 2);
		
	//create the path name section
		gd = new GridData(GridData.FILL_HORIZONTAL);
		lbl = new Label(parent, SWT.NONE);
		lbl.setText(DebugPreferencesMessages.ProcessPropertyPage_1);
		lbl.setFont(font);
		lbl.setLayoutData(gd);
		
		text = new Text(parent, SWT.WRAP | SWT.READ_ONLY);
		text.setText(getPathText(proc));
		text.setFont(font);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 10;
		text.setLayoutData(gd);
		createVerticalSpacer(parent, 2);
		
	//create commandline section
		gd = new GridData(GridData.FILL_HORIZONTAL);
		lbl = new Label(parent, SWT.NULL);
		lbl.setText(DebugPreferencesMessages.ProcessPropertyPage_Command_Line__1); 
		lbl.setLayoutData(gd);
		lbl.setFont(font);
		
		text = new Text(parent, SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(80);
		gd.heightHint= convertHeightInCharsToPixels(15);
		gd.horizontalIndent = 10;
		text.setLayoutData(gd);
		text.setFont(font);
		text.setText(getCommandLineText(proc));	
		
		setTitle(DebugPreferencesMessages.ProcessPropertyPage_2);
		return parent;
	}
	
	/**
	 * Gets the process from the selected element
	 * @return the process or null if the element is not a process
	 * 
	 * @since 3.2
	 */
	private IProcess getProcess() {
		IProcess proc = null;
		Object obj = getElement();
		if (obj instanceof IDebugElement) {
			obj = ((IDebugElement)obj).getDebugTarget().getProcess();
		}
		if (obj instanceof IProcess) {
			proc = ((IProcess)obj);
		}
		return proc;
	}
	
	/**
	 * creates a vertical spacer for seperating components
	 * @param comp
	 * @param numlines
	 */
	private void createVerticalSpacer(Composite comp, int numlines) {
		Label lbl = new Label(comp, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = numlines;
		lbl.setLayoutData(gd);
	}
	
	private String getPathText(IProcess proc) {
		String text = DebugPreferencesMessages.ProcessPropertyPage_3;
		if(proc != null) {
			String tmp = proc.getLabel();
			text = tmp.substring(0, tmp.lastIndexOf("(")); //$NON-NLS-1$
		}
		return text;
	}
	
	/**
	 * gets the pattern of text from the process label specified by regex
	 * @param proc the process to compile the regex against
	 * @param deftext the default text to return if the process is null
	 * @param regex the regex to match in the process label
	 * @return the regex matched text or the default supplied text if the process is null
	 * 
	 * @since 3.2
	 */
	private String getTimeText(IProcess proc) {
		String text = DebugPreferencesMessages.ProcessPropertyPage_4;
		if(proc != null) {
			Pattern pattern = Pattern.compile("\\(.*\\)"); //$NON-NLS-1$
			Matcher matcher = pattern.matcher(proc.getLabel());
			matcher.find();
			text = matcher.group(0);
		}
		return text;
	}
	
	/** 
	 * Initializes the text to be displayed in the commandline text widget
	 * @param proc the process to compile the label fragment from
	 * @return the commandline text or the empty string
	 * 
	 * @since 3.2
	 */
	private String getCommandLineText(IProcess proc) {
		String cmdline = DebugPreferencesMessages.ProcessPropertyPage_5;
		if(proc != null) {
			cmdline = proc.getAttribute(IProcess.ATTR_CMDLINE);
		}
		return cmdline;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),	IDebugHelpContextIds.PROCESS_PROPERTY_PAGE);
	}

}
