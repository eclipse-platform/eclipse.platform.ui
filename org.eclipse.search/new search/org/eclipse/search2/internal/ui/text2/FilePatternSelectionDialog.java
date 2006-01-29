/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

import org.eclipse.search2.internal.ui.SearchMessages;


/**
 * @author markus.schorn@windriver.com
 */
public class FilePatternSelectionDialog extends SelectionDialog {
	static final String FILE_PATTERN_SEPERATOR= ","; //$NON-NLS-1$
	private final static int SIZING_SELECTION_WIDGET_HEIGHT= 250;
	private final static int SIZING_SELECTION_WIDGET_WIDTH= 300;

	private String fInitialSelection;
	private CheckboxTableViewer fListViewer;
	private String fResult;
	private Object[] fInput;


	/**
	 * Creates a type selection dialog using the supplied entries. Set the
	 * initial selections to those whose extensions match the preselections.
	 */
	public FilePatternSelectionDialog(Shell parentShell, String string) {
		super(parentShell);
		setTitle(SearchMessages.FilePatternSelectionDialog_title);
		fInitialSelection= string;
		setMessage(SearchMessages.FilePatternSelectionDialog_message);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite= (Composite) super.createDialogArea(parent);
		createMessageArea(composite);

		fListViewer= CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData data= new GridData(GridData.FILL_BOTH);
		data.heightHint= SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint= SIZING_SELECTION_WIDGET_WIDTH;
		fListViewer.getTable().setLayoutData(data);

		fListViewer.setLabelProvider(EditorDescriptorLabelProvider.INSTANCE);
		fListViewer.setContentProvider(new ArrayContentProvider());

		addSelectionButtons(composite);
		initializeViewer(fInitialSelection);

		applyDialogFont(composite);
		return composite;
	}

	/**
	 * Add the selection and deselection buttons to the dialog.
	 */
	private void addSelectionButtons(Composite composite) {
		Composite buttonComposite= new Composite(composite, SWT.RIGHT);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		buttonComposite.setLayout(layout);
		GridData data= new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace= true;
		composite.setData(data);

		Button selectButton= createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, SearchMessages.FilePatternSelectionDialog_selectAll, false);

		SelectionListener listener= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getListViewer().setAllChecked(true);
			}
		};
		selectButton.addSelectionListener(listener);

		Button deselectButton= createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, SearchMessages.FilePatternSelectionDialog_deselectAll, false);

		listener= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getListViewer().setAllChecked(false);

			}
		};
		deselectButton.addSelectionListener(listener);
	}

	/*
	 * (non-Javadoc) Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		//        IWorkbenchHelpSystem help= PlatformUI.getWorkbench().getHelpSystem();
		//        help.setHelp(shell, IContextIDs.DIALOG_selectSymbol);
	}

	/**
	 * Initializes this dialog's viewer after it has been laid out.
	 */
	private void initializeViewer(String initialSelection) {
		IContentTypeManager ctm= Platform.getContentTypeManager();
		IContentType[] cts= ctm.getAllContentTypes();
		IContentType txtCt= ctm.getContentType(IContentTypeManager.CT_TEXT);

		HashMap editorToExtensions= new HashMap();
		for (int i= 0; i < cts.length; i++) {
			IContentType ct= cts[i];
			if (ct.isKindOf(txtCt)) {
				String[] exts= ct.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
				String[] files= ct.getFileSpecs(IContentType.FILE_NAME_SPEC);
				if (files.length > 30) { // ignore the c++ headers
					files= new String[0];
				}
				if (exts.length > 0 || files.length > 0) {
					IEditorDescriptor editorDescriptor= searchForEditor(exts, files, ct);
					HashSet patterns= (HashSet) editorToExtensions.get(editorDescriptor);
					if (patterns == null) {
						patterns= new HashSet();
						editorToExtensions.put(editorDescriptor, patterns);
					}

					if (exts.length > 0) {
						for (int j= 0; j < exts.length; j++) {
							patterns.add("*." + exts[j]); //$NON-NLS-1$
						}
					}
					if (files.length > 0) {
						for (int j= 0; j < files.length; j++) {
							String file= files[j];
							int idx= file.lastIndexOf('.');
							if (idx >= 0) {
								patterns.add("*" + file.substring(idx)); //$NON-NLS-1$
							} else {
								patterns.add(file);
							}
						}
					}
				}
			}
		}

		// remove all subsets
		HashMap oldMap= editorToExtensions;
		editorToExtensions= new HashMap();
		for (Iterator iter= oldMap.entrySet().iterator(); iter.hasNext();) {
			boolean ignore= false;
			Entry entry= (Entry) iter.next();
			HashSet patterns= (HashSet) entry.getValue();
			for (Iterator iter2= editorToExtensions.values().iterator(); iter2.hasNext();) {
				HashSet patterns2= (HashSet) iter2.next();
				if (patterns.size() <= patterns2.size()) {
					if (patterns2.containsAll(patterns)) {
						ignore= true;
					}
				} else {
					if (patterns.containsAll(patterns2)) {
						iter2.remove();
					}
				}
			}
			if (!ignore) {
				editorToExtensions.put(entry.getKey(), patterns);
			}
		}

		HashSet initialPatterns= new HashSet();
		HashSet remainingPatterns= new HashSet();
		if (initialPatterns != null) {
			List helper= Arrays.asList(initialSelection.split(FILE_PATTERN_SEPERATOR));
			for (Iterator iter= helper.iterator(); iter.hasNext();) {
				String element= (String) iter.next();
				element= element.trim();
				if (element.length() > 0) {
					initialPatterns.add(element);
					remainingPatterns.add(element);
				}
			}
		}
		final Comparator stringComparator= new Comparator() {
			public int compare(Object arg0, Object arg1) {
				String s1= (String) arg0;
				String s2= (String) arg1;
				int cmp= s1.compareToIgnoreCase(s2);
				if (cmp == 0) {
					cmp= -s1.compareTo(s2);
				}
				return cmp;
			}
		};
		TreeMap filePatterns= new TreeMap(stringComparator);
		List checkmark= new ArrayList();
		for (Iterator iter= editorToExtensions.entrySet().iterator(); iter.hasNext();) {
			Entry entry= (Entry) iter.next();
			Collection extensions= (Collection) entry.getValue();
			Object editor= entry.getKey();
			filePatterns.put(combineExtensions(extensions, stringComparator), editor);
			if (initialPatterns.containsAll(extensions)) {
				checkmark.add(editor);
				remainingPatterns.removeAll(extensions);
			}
		}

		if (!remainingPatterns.isEmpty()) {
			filePatterns.put(combineExtensions(remainingPatterns, stringComparator), null);
			checkmark.add(null);
		}

		fInput= filePatterns.entrySet().toArray();
		fListViewer.setInput(fInput);
		for (int i= 0; i < fInput.length; i++) {
			Entry element= (Entry) fInput[i];
			if (checkmark.contains(element.getValue())) {
				fListViewer.setChecked(element, true);
			}
		}
	}

	private IEditorDescriptor searchForEditor(String[] exts, String[] files, IContentType ct) {
		IEditorRegistry reg= PlatformUI.getWorkbench().getEditorRegistry();

		// try the extensions first
		for (int i= 0; i < exts.length; i++) {
			String ext= exts[i];
			String sample= "file." + ext; //$NON-NLS-1$
			IEditorDescriptor ed= reg.getDefaultEditor(sample, ct);
			if (ed != null) {
				return ed;
			}
		}
		// next try files
		for (int i= 0; i < files.length; i++) {
			String file= files[i];
			IEditorDescriptor ed= reg.getDefaultEditor(file, ct);
			if (ed != null) {
				return ed;
			}
		}

		// give up
		return null;
	}

	private String combineExtensions(Collection input, Comparator comp) {
		ArrayList extensions= new ArrayList();
		extensions.addAll(input);
		Collections.sort(extensions, comp);
		String last= null;
		boolean needSep= false;
		StringBuffer pattern= new StringBuffer();
		for (Iterator iterator= extensions.iterator(); iterator.hasNext();) {
			String extension= (String) iterator.next();
			if (!extension.equals(last)) {
				if (needSep) {
					pattern.append(FILE_PATTERN_SEPERATOR);
				}
				needSep= true;
				pattern.append(extension);
				last= extension;
			}
		}
		return pattern.toString();
	}

	protected void okPressed() {
		// Get the input children.
		List list= new ArrayList();
		for (int i= 0; i < fInput.length; ++i) {
			Entry element= (Entry) fInput[i];
			if (fListViewer.getChecked(element)) {
				String pattern= (String) element.getKey();
				list.addAll(Arrays.asList(pattern.split(FILE_PATTERN_SEPERATOR)));
			}
		}
		setResult(list);
		fResult= combineExtensions(list, String.CASE_INSENSITIVE_ORDER);
		super.okPressed();
	}

	protected CheckboxTableViewer getListViewer() {
		return fListViewer;
	}

	public String getFilePatterns() {
		return fResult;
	}
}
