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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.*;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;

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
			return "no name";	//$NON-NLS-1$
		}
		public String getType() {
			return "no type";	//$NON-NLS-1$
		}
		public InputStream getContents() {
			return new ByteArrayInputStream(fContent.getBytes());
		}
	};

	private static final String PREFIX= CompareUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$
	public static final String OPEN_STRUCTURE_COMPARE= PREFIX + "OpenStructureCompare"; //$NON-NLS-1$
	public static final String SYNCHRONIZE_SCROLLING= PREFIX + "SynchronizeScrolling"; //$NON-NLS-1$
	public static final String SHOW_PSEUDO_CONFLICTS= PREFIX + "ShowPseudoConflicts"; //$NON-NLS-1$
	public static final String INITIALLY_SHOW_ANCESTOR_PANE= PREFIX + "InitiallyShowAncestorPane"; //$NON-NLS-1$
	public static final String PREF_SAVE_ALL_EDITORS= PREFIX + "SaveAllEditors"; //$NON-NLS-1$
	public static final String SHOW_MORE_INFO= PREFIX + "ShowMoreInfo"; //$NON-NLS-1$
	public static final String TEXT_FONT= PREFIX + "TextFont"; //$NON-NLS-1$
	
	private WorkbenchChainedTextFontFieldEditor fFontEditor;
	private TextMergeViewer fPreviewViewer;
	private IPropertyChangeListener	fPreferenceChangeListener;
	private CompareConfiguration fCompareConfiguration;
	private OverlayPreferenceStore fOverlayStore;
	private Map fCheckBoxes= new HashMap();
	private SelectionListener fCheckBoxListener;
	private Button fBackgroundDefaultRadioButton;
	private Button fBackgroundCustomRadioButton;
	private Button fBackgroundColorButton;
	private ColorEditor fBackgroundColorEditor;


	public final OverlayPreferenceStore.OverlayKey[] fKeys= new OverlayPreferenceStore.OverlayKey[] {	
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, OPEN_STRUCTURE_COMPARE),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, SYNCHRONIZE_SCROLLING),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, SHOW_PSEUDO_CONFLICTS),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, INITIALLY_SHOW_ANCESTOR_PANE),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, SHOW_MORE_INFO),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TEXT_FONT),
		
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND),
		new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
	};
	
	
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(OPEN_STRUCTURE_COMPARE, true);
		store.setDefault(SYNCHRONIZE_SCROLLING, true);
		store.setDefault(SHOW_PSEUDO_CONFLICTS, false);
		store.setDefault(INITIALLY_SHOW_ANCESTOR_PANE, false);
		store.setDefault(SHOW_MORE_INFO, false);
		
		Display display= Display.getDefault();
		Color color= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		PreferenceConverter.setDefault(store,  AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, color.getRGB());		
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, true);

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
	
	/*
	 * @see IWorkbenchPreferencePage#init()
	 */
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
		
		return folder;
	}
	
	private Control createGeneralPage(Composite parent) {
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		composite.setLayout(layout);
				
		addCheckBox(composite, "ComparePreferencePage.structureCompare.label", OPEN_STRUCTURE_COMPARE, 0);	//$NON-NLS-1$
		
		addCheckBox(composite, "ComparePreferencePage.showMoreInfo.label", SHOW_MORE_INFO, 0);	//$NON-NLS-1$
		
		return composite;
	}
	
	private Control createTextComparePage(Composite parent) {
		
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		composite.setLayout(layout);
		
			// background color
			Composite backgroundComposite= new Composite(composite, SWT.NULL);
			layout= new GridLayout();
			layout.marginHeight= 0;
			layout.marginWidth= 0;
			layout.numColumns= 2;
			backgroundComposite.setLayout(new RowLayout());
	
			Label label= new Label(backgroundComposite, SWT.NULL);
			label.setText(Utilities.getString("ComparePreferencePage.backgroundColor1.label"));	//$NON-NLS-1$
	
			SelectionListener backgroundSelectionListener= new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {				
					boolean custom= fBackgroundCustomRadioButton.getSelection();
					fBackgroundColorButton.setEnabled(custom);
					fOverlayStore.setValue(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, !custom);
				}
				public void widgetDefaultSelected(SelectionEvent e) {}
			};
	
			fBackgroundDefaultRadioButton= new Button(backgroundComposite, SWT.RADIO | SWT.LEFT);
			fBackgroundDefaultRadioButton.setText(Utilities.getString("ComparePreferencePage.backgroundColor2.label"));	//$NON-NLS-1$
			fBackgroundDefaultRadioButton.addSelectionListener(backgroundSelectionListener);
	
			fBackgroundCustomRadioButton= new Button(backgroundComposite, SWT.RADIO | SWT.LEFT);
			fBackgroundCustomRadioButton.setText(Utilities.getString("ComparePreferencePage.backgroundColor3.label"));	//$NON-NLS-1$
			fBackgroundCustomRadioButton.addSelectionListener(backgroundSelectionListener);
	
			fBackgroundColorEditor= new ColorEditor(backgroundComposite);
			
			fBackgroundColorButton= fBackgroundColorEditor.getButton();
			fBackgroundColorButton.addSelectionListener(
				new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
						// do nothing
					}
					public void widgetSelected(SelectionEvent e) {
						PreferenceConverter.setValue(fOverlayStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, fBackgroundColorEditor.getColorValue());					
					}
				}
			);


			// background color end
		
		
		addCheckBox(composite, "ComparePreferencePage.synchronizeScrolling.label", SYNCHRONIZE_SCROLLING, 0);	//$NON-NLS-1$
		
		addCheckBox(composite, "ComparePreferencePage.initiallyShowAncestorPane.label", INITIALLY_SHOW_ANCESTOR_PANE, 0);	//$NON-NLS-1$
		
		addCheckBox(composite, "ComparePreferencePage.showPseudoConflicts.label", SHOW_PSEUDO_CONFLICTS, 0);	//$NON-NLS-1$
		
		fFontEditor= addTextFontEditor(composite, "ComparePreferencePage.textFont.label", TEXT_FONT);	//$NON-NLS-1$
		fFontEditor.setPreferenceStore(getPreferenceStore());
		fFontEditor.setPreferencePage(this);
		fFontEditor.load();
		
		Label previewLabel= new Label(composite, SWT.NULL);
		previewLabel.setText(Utilities.getString("ComparePreferencePage.preview.label"));	//$NON-NLS-1$
		
		Control previewer= createPreviewer(composite);
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(80);
		gd.heightHint= convertHeightInCharsToPixels(15);
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
				new FakeInput("previewAncestor.txt"),	//$NON-NLS-1$
				new FakeInput("previewLeft.txt"),	//$NON-NLS-1$
				new FakeInput("previewRight.txt")	//$NON-NLS-1$
			)
		);

		return fPreviewViewer.getControl();
	}
			
	private void initializeFields() {
		
		Iterator e= fCheckBoxes.keySet().iterator();
		while (e.hasNext()) {
			Button b= (Button) e.next();
			String key= (String) fCheckBoxes.get(b);
			b.setSelection(fOverlayStore.getBoolean(key));
		}

		RGB rgb= PreferenceConverter.getColor(fOverlayStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
		fBackgroundColorEditor.setColorValue(rgb);		
		boolean default_= fOverlayStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		fBackgroundDefaultRadioButton.setSelection(default_);
		fBackgroundCustomRadioButton.setSelection(!default_);
		fBackgroundColorButton.setEnabled(!default_);
	}

	// overlay stuff
	
	private WorkbenchChainedTextFontFieldEditor addTextFontEditor(Composite parent, String labelKey, String key) {
		
		String label= Utilities.getString(labelKey);

		Group group= new Group(parent, SWT.NULL);
		group.setText(label);
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		layout.marginWidth+= 3;
		group.setLayout(layout);		
		WorkbenchChainedTextFontFieldEditor fe= new WorkbenchChainedTextFontFieldEditor(key, "", group);	//$NON-NLS-1$
				
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan= 2;
		group.setLayoutData(gd);
		
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
	
	private String loadPreviewContentFromFile(String filename) {
		String separator= System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buffer= new StringBuffer(512);
		BufferedReader reader= null;
		try {
			reader= new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
			String line;
			while ((line= reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(separator);
			}
		} catch (IOException io) {
			CompareUIPlugin.log(io);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return buffer.toString();
	}
}
