/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import java.io.*;
import java.util.*;

import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.*;

import org.eclipse.compare.*;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.structuremergeviewer.*;


public class ComparePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	class FakeInput implements ITypedElement, IEncodedStreamContentAccessor {
		static final String UTF_16= "UTF-16"; //$NON-NLS-1$
		String fContent;
		
		FakeInput(String name) {
			fContent= loadPreviewContentFromFile(name);
		}
		public Image getImage() {
			return null;
		}
		public String getName() {
			return "no name";	//$NON-NLS-1$
		}
		public String getType() {
			return "no type";	//$NON-NLS-1$
		}
		public InputStream getContents() {
			return new ByteArrayInputStream(Utilities.getBytes(fContent, UTF_16));
		}
		public String getCharset() {
			return UTF_16;
		}
	}

	private static final String PREFIX= CompareUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$
	public static final String OPEN_STRUCTURE_COMPARE= PREFIX + "OpenStructureCompare"; //$NON-NLS-1$
	public static final String SYNCHRONIZE_SCROLLING= PREFIX + "SynchronizeScrolling"; //$NON-NLS-1$
	public static final String SHOW_PSEUDO_CONFLICTS= PREFIX + "ShowPseudoConflicts"; //$NON-NLS-1$
	public static final String INITIALLY_SHOW_ANCESTOR_PANE= PREFIX + "InitiallyShowAncestorPane"; //$NON-NLS-1$
	public static final String PREF_SAVE_ALL_EDITORS= PREFIX + "SaveAllEditors"; //$NON-NLS-1$
	public static final String SHOW_MORE_INFO= PREFIX + "ShowMoreInfo"; //$NON-NLS-1$
	public static final String IGNORE_WHITESPACE= PREFIX + "IgnoreWhitespace"; //$NON-NLS-1$
	//public static final String USE_SPLINES= PREFIX + "UseSplines"; //$NON-NLS-1$
	public static final String USE_SINGLE_LINE= PREFIX + "UseSingleLine"; //$NON-NLS-1$
	//public static final String USE_RESOLVE_UI= PREFIX + "UseResolveUI"; //$NON-NLS-1$
	public static final String PATH_FILTER= PREFIX + "PathFilter"; //$NON-NLS-1$
	
	
	private TextMergeViewer fPreviewViewer;
	private IPropertyChangeListener	fPreferenceChangeListener;
	private CompareConfiguration fCompareConfiguration;
	private OverlayPreferenceStore fOverlayStore;
	private Map fCheckBoxes= new HashMap();
	private SelectionListener fCheckBoxListener;


	public final OverlayPreferenceStore.OverlayKey[] fKeys= new OverlayPreferenceStore.OverlayKey[] {	
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, OPEN_STRUCTURE_COMPARE),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, SYNCHRONIZE_SCROLLING),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, SHOW_PSEUDO_CONFLICTS),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, INITIALLY_SHOW_ANCESTOR_PANE),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, SHOW_MORE_INFO),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IGNORE_WHITESPACE),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PREF_SAVE_ALL_EDITORS),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT),
		
		//new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, USE_SPLINES),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, USE_SINGLE_LINE),
		//new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, USE_RESOLVE_UI),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, PATH_FILTER),
	};
	
	
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(OPEN_STRUCTURE_COMPARE, true);
		store.setDefault(SYNCHRONIZE_SCROLLING, true);
		store.setDefault(SHOW_PSEUDO_CONFLICTS, false);
		store.setDefault(INITIALLY_SHOW_ANCESTOR_PANE, false);
		store.setDefault(SHOW_MORE_INFO, false);
		store.setDefault(IGNORE_WHITESPACE, false);
		store.setDefault(PREF_SAVE_ALL_EDITORS, false);
		//store.setDefault(USE_SPLINES, false);
		store.setDefault(USE_SINGLE_LINE, true);
		//store.setDefault(USE_RESOLVE_UI, false);
		store.setDefault(PATH_FILTER, ""); //$NON-NLS-1$
		
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, true);
	}

	public ComparePreferencePage() {
		
		//setDescription(Utilities.getString("ComparePreferencePage.description"));	//$NON-NLS-1$
		
		setPreferenceStore(CompareUIPlugin.getDefault().getPreferenceStore());
		
		fOverlayStore= new OverlayPreferenceStore(getPreferenceStore(), fKeys);
		fPreferenceChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String key= event.getProperty();
				if (key.equals(INITIALLY_SHOW_ANCESTOR_PANE)) {
					boolean b= fOverlayStore.getBoolean(INITIALLY_SHOW_ANCESTOR_PANE);
					if (fCompareConfiguration != null) {
						fCompareConfiguration.setProperty(INITIALLY_SHOW_ANCESTOR_PANE, new Boolean(b));
					}
				}
			}
		};
		fOverlayStore.addPropertyChangeListener(fPreferenceChangeListener);
	}
	
	/*
	 * @see IWorkbenchPreferencePage#init()
	 */
	public void init(IWorkbench workbench) {
	}	

	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		fOverlayStore.propagate();
		return true;
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		
		fOverlayStore.loadDefaults();
		initializeFields();
		
		super.performDefaults();
	}
	
	/*
	 * @see DialogPage#dispose()
	 */
	public void dispose() {
				
		if (fOverlayStore != null) {
			if (fPreferenceChangeListener != null) {
				fOverlayStore.removePropertyChangeListener(fPreferenceChangeListener);
				fPreferenceChangeListener= null;
			}
			fOverlayStore.stop();
			fOverlayStore= null;
		}
		
		super.dispose();
	}

	static public boolean getSaveAllEditors() {
		IPreferenceStore store= CompareUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(PREF_SAVE_ALL_EDITORS);
	}
	
	static public void setSaveAllEditors(boolean value) {
		IPreferenceStore store= CompareUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PREF_SAVE_ALL_EDITORS, value);
	}	

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		
		WorkbenchHelp.setHelp(parent, ICompareContextIds.COMPARE_PREFERENCE_PAGE);
		
		fOverlayStore.load();
		fOverlayStore.start();
		
		TabFolder folder= new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());	
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabItem item= new TabItem(folder, SWT.NONE);
		item.setText(Utilities.getString("ComparePreferencePage.generalTab.label"));	//$NON-NLS-1$
		//item.setImage(JavaPluginImages.get(JavaPluginImages.IMG_OBJS_CFILE));
		item.setControl(createGeneralPage(folder));
		
		item= new TabItem(folder, SWT.NONE);
		item.setText(Utilities.getString("ComparePreferencePage.textCompareTab.label"));	//$NON-NLS-1$
		//item.setImage(JavaPluginImages.get(JavaPluginImages.IMG_OBJS_CFILE));
		item.setControl(createTextComparePage(folder));
		
		initializeFields();
		Dialog.applyDialogFont(folder);
		return folder;
	}
	
	private Control createGeneralPage(Composite parent) {
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		composite.setLayout(layout);
				
		addCheckBox(composite, "ComparePreferencePage.structureCompare.label", OPEN_STRUCTURE_COMPARE, 0);	//$NON-NLS-1$
		addCheckBox(composite, "ComparePreferencePage.showMoreInfo.label", SHOW_MORE_INFO, 0);	//$NON-NLS-1$
		addCheckBox(composite, "ComparePreferencePage.ignoreWhitespace.label", IGNORE_WHITESPACE, 0);	//$NON-NLS-1$
		
		// a spacer
		new Label(composite, SWT.NONE);

		addCheckBox(composite, "ComparePreferencePage.saveBeforePatching.label", PREF_SAVE_ALL_EDITORS, 0);	//$NON-NLS-1$

		// a spacer
		new Label(composite, SWT.NONE);
		
		Label l= new Label(composite, SWT.WRAP);
		l.setText(Utilities.getString("ComparePreferencePage.filter.description")); //$NON-NLS-1$
		
		Composite c2= new Composite(composite, SWT.NONE);
		c2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout= new GridLayout(2, false);
		layout.marginWidth= 0;
		c2.setLayout(layout);
		
		l= new Label(c2, SWT.NONE);
		l.setText(Utilities.getString("ComparePreferencePage.filter.label")); //$NON-NLS-1$
		
		final Text t= new Text(c2, SWT.BORDER);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		t.setText(fOverlayStore.getString(PATH_FILTER));
		t.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String filters= t.getText();
					String message= CompareFilter.validateResourceFilters(filters);
					setValid(message == null);
					setMessage(null);
					setErrorMessage(message);
					fOverlayStore.setValue(PATH_FILTER, filters);
				}
			}
		);
		
		return composite;
	}
	
	private Control createTextComparePage(Composite parent) {
		
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		composite.setLayout(layout);
				
		addCheckBox(composite, "ComparePreferencePage.synchronizeScrolling.label", SYNCHRONIZE_SCROLLING, 0);	//$NON-NLS-1$
		addCheckBox(composite, "ComparePreferencePage.initiallyShowAncestorPane.label", INITIALLY_SHOW_ANCESTOR_PANE, 0);	//$NON-NLS-1$
		addCheckBox(composite, "ComparePreferencePage.showPseudoConflicts.label", SHOW_PSEUDO_CONFLICTS, 0);	//$NON-NLS-1$
		
		//addCheckBox(composite, "ComparePreferencePage.useSplines.label", USE_SPLINES, 0);	//$NON-NLS-1$
		addCheckBox(composite, "ComparePreferencePage.useSingleLine.label", USE_SINGLE_LINE, 0);	//$NON-NLS-1$
		//addCheckBox(composite, "ComparePreferencePage.useResolveUI.label", USE_RESOLVE_UI, 0);	//$NON-NLS-1$
		
		// a spacer
		new Label(composite, SWT.NONE);
		
		Label previewLabel= new Label(composite, SWT.NULL);
		previewLabel.setText(Utilities.getString("ComparePreferencePage.preview.label"));	//$NON-NLS-1$
		
		Control previewer= createPreviewer(composite);
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(60);
		gd.heightHint= convertHeightInCharsToPixels(13);
		previewer.setLayoutData(gd);
		
		return composite;
	}
	
	private Control createPreviewer(Composite parent) {
				
		fCompareConfiguration= new CompareConfiguration(fOverlayStore);
		fCompareConfiguration.setAncestorLabel(Utilities.getString("ComparePreferencePage.ancestor.label"));	//$NON-NLS-1$
		
		fCompareConfiguration.setLeftLabel(Utilities.getString("ComparePreferencePage.left.label"));	//$NON-NLS-1$
		fCompareConfiguration.setLeftEditable(false);
		
		fCompareConfiguration.setRightLabel(Utilities.getString("ComparePreferencePage.right.label"));	//$NON-NLS-1$
		fCompareConfiguration.setRightEditable(false);
		
		fPreviewViewer= new TextMergeViewer(parent, SWT.BORDER, fCompareConfiguration);
		
		fPreviewViewer.setInput(
			new DiffNode(Differencer.CONFLICTING,
				new FakeInput("ComparePreferencePage.previewAncestor"),	//$NON-NLS-1$
				new FakeInput("ComparePreferencePage.previewLeft"),	//$NON-NLS-1$
				new FakeInput("ComparePreferencePage.previewRight")	//$NON-NLS-1$
			)
		);

		Control c= fPreviewViewer.getControl();
		c.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (fCompareConfiguration != null)
					fCompareConfiguration.dispose();
			}
		});
		
		return  c;
	}
			
	private void initializeFields() {
		
		Iterator e= fCheckBoxes.keySet().iterator();
		while (e.hasNext()) {
			Button b= (Button) e.next();
			String key= (String) fCheckBoxes.get(b);
			b.setSelection(fOverlayStore.getBoolean(key));
		}
	}

	// overlay stuff
	
	private Button addCheckBox(Composite parent, String labelKey, String key, int indentation) {
		
		String label= Utilities.getString(labelKey);
				
		Button checkBox= new Button(parent, SWT.CHECK);
		checkBox.setText(label);
		
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent= indentation;
		gd.horizontalSpan= 2;
		checkBox.setLayoutData(gd);
		
		if (fCheckBoxListener == null) {
			fCheckBoxListener= new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Button button= (Button) e.widget;
					fOverlayStore.setValue((String) fCheckBoxes.get(button), button.getSelection());
				}
			};
		}
		checkBox.addSelectionListener(fCheckBoxListener);
		
		fCheckBoxes.put(checkBox, key);
		
		return checkBox;
	}
	
	private String loadPreviewContentFromFile(String key) {
		
		String preview= Utilities.getString(key);
		String separator= System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buffer= new StringBuffer();
		for (int i= 0; i < preview.length(); i++) {
			char c= preview.charAt(i);
			if (c == '\n')
				buffer.append(separator);
			else
				buffer.append(c);
		}
		return buffer.toString();
	}
}
