/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.preferences;


import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BidiSegmentEvent;
import org.eclipse.swt.custom.BidiSegmentListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ibm.icu.text.DateFormat;

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
	@Override
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

	//create working directory section
		SWTFactory.createLabel(parent, DebugPreferencesMessages.ProcessPropertyPage_6, fHeadingFont, 1);
		text = SWTFactory.createText(parent, SWT.WRAP | SWT.READ_ONLY, 1);
		((GridData)text.getLayoutData()).horizontalIndent = 10;
		text.setText(getWorkingDirectory(proc));
		text.setBackground(parent.getBackground());
		SWTFactory.createVerticalSpacer(parent, 2);

	//create command line section
		SWTFactory.createLabel(parent, DebugPreferencesMessages.ProcessPropertyPage_Command_Line__1, fHeadingFont, 1);
		StyledText styledText = SWTFactory.createStyledText(parent,
				SWT.WRAP | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL,
				1,
				convertWidthInCharsToPixels(13),
				convertHeightInCharsToPixels(10),
				GridData.FILL_BOTH);
		styledText.setBackground(parent.getBackground());
		((GridData)styledText.getLayoutData()).horizontalIndent = 10;
		String commandLineText = DebugPreferencesMessages.ProcessPropertyPage_5;
		if (proc != null) {
			commandLineText = proc.getAttribute(IProcess.ATTR_CMDLINE);
			String[] arguments = DebugPlugin.parseArguments(commandLineText);
			int count = arguments.length;
			if (count > 1) {
				// render as one argument per line, but don't copy line delimiters to clipboard:
				final int[] segments = new int[count - 1];
				commandLineText = DebugPlugin.renderArguments(arguments, segments);

				styledText.addBidiSegmentListener(new BidiSegmentListener() {
					@Override
					public void lineGetSegments(BidiSegmentEvent event) {
						int offset = event.lineOffset;
						int end = offset + event.lineText.length();

						// extract segments for the current line:
						int iStart = Arrays.binarySearch(segments, offset);
						if (iStart < 0) {
							iStart = -iStart - 1;
						}
						int i = iStart;
						while (i < segments.length && segments[i] < end) {
							i++;
						}
						int n = i - iStart;
						if (n > 0) {
							if (n == segments.length) {
								event.segments = segments;
							} else {
								int[] lineSegments = new int[n];
								System.arraycopy(segments, iStart, lineSegments, 0, n);
								event.segments = lineSegments;
							}
							final char[] chars = new char[n];
							Arrays.fill(chars, '\n');
							event.segmentsChars = chars;
						}
					}
				});
			}
		}
		if(commandLineText != null) {
			styledText.setText(commandLineText);
		}

	//create environment section
		SWTFactory.createLabel(parent, DebugPreferencesMessages.ProcessPropertyPage_7, fHeadingFont, 1);
		text = SWTFactory.createText(parent,
				SWT.H_SCROLL | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL,
				1,
				convertWidthInCharsToPixels(13),
				convertHeightInCharsToPixels(8),
				GridData.FILL_BOTH);
		text.setBackground(parent.getBackground());
		((GridData)text.getLayoutData()).horizontalIndent = 10;
		text.setText(getEnvironment(proc));

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
	 * @see DebugPlugin#ATTR_PATH
	 * @since 3.2
	 */
	private String getPathText(IProcess proc) {
		String text = DebugPreferencesMessages.ProcessPropertyPage_3;
		if(proc != null) {
			String tmp = proc.getAttribute(DebugPlugin.ATTR_PATH);
			if(tmp != null) {
				return tmp;
			}
			tmp = proc.getLabel();
			int idx = tmp.lastIndexOf("("); //$NON-NLS-1$
			if(idx < 0) {
				idx = tmp.length();
			}
			text = tmp.substring(0, idx);
		}
		return text;
	}

	/**
	 * gets the pattern of text from the process label specified by RegEx
	 * @param proc the process to compile the RegEx against
	 * @param deftext the default text to return if the process is null
	 * @param regex the RegEx to match in the process label
	 * @return the RegEx matched text or the default supplied text if the process is null
	 *
	 * @see DebugPlugin#ATTR_RUN_AT_TIME
	 * @since 3.2
	 */
	private String getTimeText(IProcess proc) {
		String text = DebugPreferencesMessages.ProcessPropertyPage_4;
		if(proc != null) {
			String tmp = proc.getAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP);
			if(tmp != null) {
				//check to see if the date/time is just the raw long (as a string)
				try {
					long l = Long.parseLong(tmp);
					return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(l));
				}
				catch(NumberFormatException nfe) {
					//not a number try to format the string so it always looks the same
					try {
						Date fdate = DateFormat.getInstance().parse(tmp);
						return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(fdate);
					}
					catch(ParseException pe) {
						//couldn't do it, return the raw string
					}
				}
				return tmp;
			}
			Pattern pattern = Pattern.compile("\\(.*\\)"); //$NON-NLS-1$
			Matcher matcher = pattern.matcher(proc.getLabel());
			if(matcher.find()) {
				text = matcher.group(0);
			}
		}
		return text;
	}

	/**
	 * Initializes the text to be displayed in the environment text widget
	 * @param proc
	 * @return the environment path or a default string never <code>null</code>
	 *
	 * @see DebugPlugin#ATTR_ENVIRONMENT
	 * @since 3.8
	 */
	String getEnvironment(IProcess proc) {
		String env = DebugPreferencesMessages.ProcessPropertyPage_8;
		if(proc != null) {
			String tmp = proc.getAttribute(DebugPlugin.ATTR_ENVIRONMENT);
			if(tmp != null) {
				return tmp;
			}
		}
		return env;
	}

	/**
	 * Initializes the text to be displayed in the working directory text widget
	 *
	 * @param proc
	 * @return the text to display or a default {@link String} never <code>null</code>
	 *
	 * @see DebugPlugin#ATTR_WORKING_DIRECTORY
	 * @since 3.8
	 */
	String getWorkingDirectory(IProcess proc) {
		String wd = DebugPreferencesMessages.ProcessPropertyPage_9;
		if(proc != null) {
			String tmp = proc.getAttribute(DebugPlugin.ATTR_WORKING_DIRECTORY);
			if(tmp != null) {
				return tmp;
			}
		}
		return wd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),	IDebugHelpContextIds.PROCESS_PROPERTY_PAGE);
	}
}
