/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.util.*;
import java.io.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.*;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;

import org.eclipse.compare.*;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.structuremergeviewer.*;


public class ComparePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	class FakeInput implements ITypedElement, IStreamContentAccessor {
		String fContent;
		
		FakeInput(String name) {
			fContent= loadPreviewContentFromFile(name);
		}
		public Image getImage() {
			return null;
		}
		public String getName() {
			return "Name";	//$NON-NLS-1$
		}
		public String getType() {
			return "Type";	//$NON-NLS-1$
		}
		public InputStream getContents() {
			return new ByteArrayInputStream(fContent.getBytes());
		}
	};
		

	private static final String PREFIX= CompareUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$
	public static final String SYNCHRONIZE_SCROLLING= PREFIX + "SynchronizeScrolling"; //$NON-NLS-1$
	public static final String SHOW_PSEUDO_CONFLICTS= PREFIX + "ShowPseudoConflicts"; //$NON-NLS-1$
	public static final String INITIALLY_SHOW_ANCESTOR_PANE= PREFIX + "InitiallyShowAncestorPane"; //$NON-NLS-1$
	public static final String PREF_SAVE_ALL_EDITORS= PREFIX + "SaveAllEditors"; //$NON-NLS-1$
	public static final String SHOW_MORE_INFO= PREFIX + "ShowMoreInfo"; //$NON-NLS-1$
	public static final String TEXT_FONT= PREFIX + "TextFont"; //$NON-NLS-1$
	
	private WorkbenchChainedTextFontFieldEditor fFontEditor;
	private TextMergeViewer fTextMergeViewer;
	private IPropertyChangeListener	fPreferenceChangeListener;
	private CompareConfiguration fCompareConfiguration;
	private OverlayPreferenceStore fOverlayStore;
	private Map fCheckBoxes= new HashMap();
	

	public final OverlayPreferenceStore.OverlayKey[] fKeys= new OverlayPreferenceStore.OverlayKey[] {	
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, SYNCHRONIZE_SCROLLING),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, SHOW_PSEUDO_CONFLICTS),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, INITIALLY_SHOW_ANCESTOR_PANE),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, SHOW_MORE_INFO),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TEXT_FONT)
	};
	

	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(SYNCHRONIZE_SCROLLING, true);
		store.setDefault(SHOW_PSEUDO_CONFLICTS, false);
		store.setDefault(INITIALLY_SHOW_ANCESTOR_PANE, false);
		store.setDefault(SHOW_MORE_INFO, false);
		
		WorkbenchChainedTextFontFieldEditor.startPropagate(store, TEXT_FONT);
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
	
	public void init(IWorkbench workbench) {
	}	

	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		fFontEditor.store();
		fOverlayStore.propagate();
		return true;
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		
		fFontEditor.loadDefault();
		
		fOverlayStore.loadDefaults();
		initializeFields();
		
		super.performDefaults();
		
		//fPreviewViewer.invalidateTextPresentation();
	}
	
	/*
	 * @see DialogPage#dispose()
	 */
	public void dispose() {
		
		fFontEditor.setPreferencePage(null);
		fFontEditor.setPreferenceStore(null);
		
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
		
		fOverlayStore.load();
		fOverlayStore.start();
		
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		composite.setLayout(layout);
				
		addCheckBox(composite, "ComparePreferences.synchronizeScrolling.label", SYNCHRONIZE_SCROLLING, 0);	//$NON-NLS-1$
		
		addCheckBox(composite, "ComparePreferences.initiallyShowAncestorPane.label", INITIALLY_SHOW_ANCESTOR_PANE, 0);	//$NON-NLS-1$
		
		addCheckBox(composite, "ComparePreferences.showPseudoConflicts.label", SHOW_PSEUDO_CONFLICTS, 0);	//$NON-NLS-1$
		
		addCheckBox(composite, "ComparePreferences.showMoreInfo.label", SHOW_MORE_INFO, 0);	//$NON-NLS-1$
		
		fFontEditor= addTextFontEditor(composite, "ComparePreferences.textFont.label", TEXT_FONT);	//$NON-NLS-1$
		fFontEditor.setPreferenceStore(getPreferenceStore());
		fFontEditor.setPreferencePage(this);
		fFontEditor.load();
		
		Label previewLabel= new Label(composite, SWT.NULL);
		previewLabel.setText("Preview:");
		
		Control previewer= createPreviewer(composite);
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(80);
		gd.heightHint= convertHeightInCharsToPixels(15);
		previewer.setLayoutData(gd);
		
		initializeFields();
		
		return composite;
	}
	
	private Control createPreviewer(Composite parent) {
				
		fCompareConfiguration= new CompareConfiguration(fOverlayStore);
		fCompareConfiguration.setAncestorLabel("Common Ancestor");
		
		fCompareConfiguration.setLeftLabel("Local");
		fCompareConfiguration.setLeftEditable(false);
		
		fCompareConfiguration.setRightLabel("Remote");
		fCompareConfiguration.setRightEditable(false);
		
		fTextMergeViewer= new TextMergeViewer(parent, SWT.BORDER, fCompareConfiguration);
				
		fTextMergeViewer.setInput(
			new DiffNode(Differencer.CONFLICTING,
				new FakeInput("previewAncestor.txt"),	//$NON-NLS-1$
				new FakeInput("previewLeft.txt"),	//$NON-NLS-1$
				new FakeInput("previewRight.txt")	//$NON-NLS-1$
			)
		);

		return fTextMergeViewer.getControl();
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
	
	private SelectionListener fCheckBoxListener= new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			Button button= (Button) e.widget;
			fOverlayStore.setValue((String) fCheckBoxes.get(button), button.getSelection());
		}
	};
	
	private WorkbenchChainedTextFontFieldEditor addTextFontEditor(Composite parent, String labelKey, String key) {
		
		String label= Utilities.getString(labelKey);

		Composite editorComposite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		editorComposite.setLayout(layout);		
		WorkbenchChainedTextFontFieldEditor fe= new WorkbenchChainedTextFontFieldEditor(key, label, editorComposite);
		//fFontEditor.setChangeButtonText("C&hange...");
				
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);
		
		return fe;
	}
	
	private Button addCheckBox(Composite parent, String labelKey, String key, int indentation) {
		
		String label= Utilities.getString(labelKey);
				
		Button checkBox= new Button(parent, SWT.CHECK);
		checkBox.setText(label);
		
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent= indentation;
		gd.horizontalSpan= 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(fCheckBoxListener);
		
		fCheckBoxes.put(checkBox, key);
		
		return checkBox;
	}
	
	private String loadPreviewContentFromFile(String filename) {
		String line;
		String separator= System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buffer= new StringBuffer(512);
		BufferedReader reader= null;
		try {
			reader= new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
			while ((line= reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(separator);
			}
		} catch (IOException io) {
			CompareUIPlugin.log(io);
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (IOException e) {}
			}
		}
		return buffer.toString();
	}
}
