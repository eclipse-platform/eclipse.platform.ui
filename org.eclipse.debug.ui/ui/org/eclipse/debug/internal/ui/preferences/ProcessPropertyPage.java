/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

public class ProcessPropertyPage extends PropertyPage {

	private static Font fHeadingFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
	
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
		noDefaultAndApplyButton();
		Composite parent = SWTFactory.createComposite(ancestor, ancestor.getFont(), 1, 1, GridData.FILL_BOTH);
		
		IProcess proc = getProcess();
		
	//create the process time section
		SWTFactory.createLabel(parent, DebugPreferencesMessages.ProcessPropertyPage_0, fHeadingFont, 1);
		Text text = SWTFactory.createText(parent, SWT.READ_ONLY, 1);
		((GridData)text.getLayoutData()).horizontalIndent = 10;
		PlatformUI.getWorkbench().getHelpSystem().setHelp(text, IDebugHelpContextIds.PROCESS_PAGE_RUN_AT);
		text.setText(getTimeText(proc));
		text.setBackground(parent.getBackground());
		SWTFactory.createVerticalSpacer(parent, 2);
		
	//create the path name section
		SWTFactory.createLabel(parent, DebugPreferencesMessages.ProcessPropertyPage_1, fHeadingFont, 1);		
		text = SWTFactory.createText(parent, SWT.WRAP | SWT.READ_ONLY, 1);
		((GridData)text.getLayoutData()).horizontalIndent = 10;
		text.setText(getPathText(proc));
		text.setBackground(parent.getBackground());
		SWTFactory.createVerticalSpacer(parent, 2);
		
	//create commandline section
		SWTFactory.createLabel(parent, DebugPreferencesMessages.ProcessPropertyPage_Command_Line__1, fHeadingFont, 1);
		text = SWTFactory.createText(parent, 
				SWT.WRAP | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL, 
				1, 
				convertWidthInCharsToPixels(10),
				convertHeightInCharsToPixels(15),
				GridData.FILL_BOTH);
		text.setBackground(parent.getBackground());
		((GridData)text.getLayoutData()).horizontalIndent = 10;
		String commandLineText = getCommandLineText(proc);
		if (commandLineText != null) {
			text.setText(commandLineText);
		}
		
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
	 * returns the path text
	 * @param proc the process to extract the path text from
	 * @return the path text or a message indicating no path text available
	 * 
	 * @since 3.2
	 */
	private String getPathText(IProcess proc) {
		String text = DebugPreferencesMessages.ProcessPropertyPage_3;
		if(proc != null) {
			String tmp = proc.getLabel();
			int idx = tmp.lastIndexOf("("); //$NON-NLS-1$
			if(idx < 0) {
				idx = tmp.length();
			}
			text = tmp.substring(0, idx); 
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
			if(matcher.find()) {
				text = matcher.group(0);
			}
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
