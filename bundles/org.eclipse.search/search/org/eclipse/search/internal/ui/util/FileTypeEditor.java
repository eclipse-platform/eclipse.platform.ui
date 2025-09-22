/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.window.Window;

import org.eclipse.ui.dialogs.TypeFilteringDialog;

import org.eclipse.search.internal.ui.SearchMessages;

public class FileTypeEditor extends SelectionAdapter implements DisposeListener {

	private Combo fTextField;
	private Button fBrowseButton;

	private final static String TYPE_DELIMITER= SearchMessages.FileTypeEditor_typeDelimiter;
	public final static String FILE_PATTERN_NEGATOR= "!"; //$NON-NLS-1$

	private static final Comparator<String> FILE_TYPES_COMPARATOR= (fp1, fp2) -> {
		boolean isNegative1= fp1.startsWith(FILE_PATTERN_NEGATOR);
		boolean isNegative2= fp2.startsWith(FILE_PATTERN_NEGATOR);
		if (isNegative1 != isNegative2) {
			return isNegative1 ? 1 : -1;
		}
		return fp1.compareTo(fp2);
	};

	public FileTypeEditor(Combo textField, Button browseButton) {
		fTextField= textField;
		fBrowseButton= browseButton;

		fTextField.addDisposeListener(this);
		fBrowseButton.addDisposeListener(this);
		fBrowseButton.addSelectionListener(this);
	}

	@Override
	public void widgetDisposed(DisposeEvent event) {
		Widget widget= event.widget;
		if (widget == fTextField) {
			fTextField= null;
		} else if (widget	== fBrowseButton) {
			fBrowseButton= null;
		}
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == fBrowseButton) {
			handleBrowseButton();
		}
	}

	public String[] getFileTypes() {
		Set<String> result= new HashSet<>();
		StringTokenizer tokenizer= new StringTokenizer(fTextField.getText(), TYPE_DELIMITER);

		while (tokenizer.hasMoreTokens()) {
			String currentExtension= tokenizer.nextToken().trim();
			result.add(currentExtension);
		}
		return result.toArray(new String[result.size()]);
	}

	public void setFileTypes(String[] types) {
		fTextField.setText(typesToString(types));
	}

	protected void handleBrowseButton() {
		TypeFilteringDialog dialog= new TypeFilteringDialog(fTextField.getShell(), Arrays.asList(getFileTypes()));
		if (dialog.open() == Window.OK) {
			Object[] result= dialog.getResult();
			HashSet<String> patterns= new HashSet<>();
			boolean starIncluded= false;
			for (Object element : result) {
				String curr= element.toString();
				if (curr.equals("*")) { //$NON-NLS-1$
					starIncluded= true;
				} else {
					patterns.add("*." + curr); //$NON-NLS-1$
				}
			}
			if (patterns.isEmpty() && starIncluded) { // remove star when other file extensions active
				patterns.add("*"); //$NON-NLS-1$
			}
			String[] filePatterns= patterns.toArray(new String[patterns.size()]);
			Arrays.sort(filePatterns);
			setFileTypes(filePatterns);
		}
	}

	public static String typesToString(String[] types) {
		Arrays.sort(types, FILE_TYPES_COMPARATOR);
		StringBuilder result= new StringBuilder();
		for (int i= 0; i < types.length; i++) {
			if (i > 0) {
				result.append(TYPE_DELIMITER);
				result.append(" "); //$NON-NLS-1$
			}
			result.append(types[i]);
		}
		return result.toString();
	}
}
