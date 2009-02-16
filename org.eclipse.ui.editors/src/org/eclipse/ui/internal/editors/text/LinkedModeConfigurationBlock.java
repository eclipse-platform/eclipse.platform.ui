/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.text.Collator;

import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;

import org.eclipse.ui.editors.text.EditorsUI;


/**
 * Configures the linked mode preferences. The preferences belong to
 * org.eclipse.ui.editors. However, as they are chiefly used in the java editor,
 * we keep the preferences here for the time being.
 *
 * @since 3.2 (in jdt.ui since 3.1)
 */
class LinkedModeConfigurationBlock implements IPreferenceConfigurationBlock {

	private static final String EXIT= "org.eclipse.ui.internal.workbench.texteditor.link.exit"; //$NON-NLS-1$
	private static final String TARGET= "org.eclipse.ui.internal.workbench.texteditor.link.target"; //$NON-NLS-1$
	private static final String MASTER= "org.eclipse.ui.internal.workbench.texteditor.link.master"; //$NON-NLS-1$
	private static final String SLAVE= "org.eclipse.ui.internal.workbench.texteditor.link.slave"; //$NON-NLS-1$

	private static final class ListItem {
		final String label;
		final String colorKey;
		final String highlightKey;
		final String textStyleKey;
		final String textKey;
		final List validStyles;

		ListItem(String label, String colorKey, String textKey, String highlightKey, String textStyleKey, List validStyles) {
			this.label= label;
			this.colorKey= colorKey;
			this.highlightKey= highlightKey;
			this.textKey= textKey;
			this.textStyleKey= textStyleKey;
			this.validStyles= validStyles;
		}
	}

	private static final class ItemContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return (ListItem[]) inputElement;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private final class ItemLabelProvider extends LabelProvider {

		public String getText(Object element) {
			return ((ListItem) element).label;
		}
	}

	private static class ArrayLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((String[]) element)[0].toString();
		}
	}

	final static String[] HIGHLIGHT= new String[] {TextEditorMessages.LinkedModeConfigurationBlock_HIGHLIGHT, "unused"};  //$NON-NLS-1$
	final static String[] UNDERLINE= new String[] {TextEditorMessages.LinkedModeConfigurationBlock_UNDERLINE, AnnotationPreference.STYLE_UNDERLINE};
	final static String[] BOX= new String[] {TextEditorMessages.LinkedModeConfigurationBlock_BOX, AnnotationPreference.STYLE_BOX};
	final static String[] DASHED_BOX= new String[] {TextEditorMessages.LinkedModeConfigurationBlock_DASHED_BOX, AnnotationPreference.STYLE_DASHED_BOX};
	final static String[] IBEAM= new String[] {TextEditorMessages.LinkedModeConfigurationBlock_IBEAM, AnnotationPreference.STYLE_IBEAM};
	final static String[] SQUIGGLES= new String[] {TextEditorMessages.LinkedModeConfigurationBlock_SQUIGGLES, AnnotationPreference.STYLE_SQUIGGLES};

	private ColorSelector fAnnotationForegroundColorEditor;

	private Button fShowInTextCheckBox;

	private StructuredViewer fAnnotationTypeViewer;
	private final ListItem[] fListModel;

	private ComboViewer fDecorationViewer;
	private FontMetrics fFontMetrics;
	protected static final int INDENT= 20;
	private OverlayPreferenceStore fStore;

	private ArrayList fMasterSlaveListeners= new ArrayList();

	private OverlayPreferenceStore getPreferenceStore() {
		return fStore;
	}

	public LinkedModeConfigurationBlock(OverlayPreferenceStore store) {
		fStore= store;
		final MarkerAnnotationPreferences prefs= EditorsPlugin.getDefault().getMarkerAnnotationPreferences();
		getPreferenceStore().addKeys(createOverlayStoreKeys(prefs));
		fListModel= createAnnotationTypeListModel(prefs);
	}

	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys(MarkerAnnotationPreferences preferences) {
		ArrayList overlayKeys= new ArrayList();

		Iterator e= preferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();

			if (isLinkedModeAnnotation(info)) {
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, info.getColorPreferenceKey()));
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getTextPreferenceKey()));
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, info.getTextStylePreferenceKey()));
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, info.getHighlightPreferenceKey()));
			}
		}

		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	private boolean isLinkedModeAnnotation(AnnotationPreference info) {
		final Object type= info.getAnnotationType();
		return type.equals(MASTER)
				|| (type.equals(SLAVE))
				|| (type.equals(TARGET))
				|| (type.equals(EXIT));
	}

	private ListItem[] createAnnotationTypeListModel(MarkerAnnotationPreferences preferences) {
		ArrayList listModelItems= new ArrayList();
		Iterator e= preferences.getAnnotationPreferences().iterator();

		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();
			if (isLinkedModeAnnotation(info)) {
				String label= info.getPreferenceLabel();
				List styles= getStyles(info.getAnnotationType());
				listModelItems.add(new ListItem(label, info.getColorPreferenceKey(), info.getTextPreferenceKey(), info.getHighlightPreferenceKey(), info.getTextStylePreferenceKey(), styles));
			}
		}

		ListItem[] items= new ListItem[listModelItems.size()];
		listModelItems.toArray(items);
		return items;
	}


	private List getStyles(Object type) {
		if (type.equals(MASTER))
			return Arrays.asList(new String[][] {BOX, DASHED_BOX, HIGHLIGHT, UNDERLINE, SQUIGGLES});
		if (type.equals(SLAVE))
			return Arrays.asList(new String[][] {BOX, DASHED_BOX, HIGHLIGHT, UNDERLINE, SQUIGGLES});
		if (type.equals(TARGET))
			return Arrays.asList(new String[][] {BOX, DASHED_BOX, HIGHLIGHT, UNDERLINE, SQUIGGLES});
		if (type.equals(EXIT))
			return Arrays.asList(new String[][] {IBEAM});
		return new ArrayList();
	}

	/**
	 * Creates page for hover preferences.
	 *
	 * @param parent the parent composite
	 * @return the control for the preference page
	 */
	public Control createControl(Composite parent) {
		OverlayPreferenceStore store= getPreferenceStore();
		store.load();
		store.start();

		initializeDialogUnits(parent);

		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		composite.setLayout(layout);

		Label label= new Label(composite, SWT.LEFT);
		label.setText(TextEditorMessages.LinkedModeConfigurationBlock_annotationPresentationOptions);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		label.setLayoutData(gd);

		Composite editorComposite= new Composite(composite, SWT.NONE);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(SWT.FILL, SWT.FILL, true, true);
		editorComposite.setLayoutData(gd);

		fAnnotationTypeViewer= new TableViewer(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		fAnnotationTypeViewer.setLabelProvider(new ItemLabelProvider());
		fAnnotationTypeViewer.setContentProvider(new ItemContentProvider());
		gd= new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		gd.heightHint= convertHeightInCharsToPixels(5);
		fAnnotationTypeViewer.getControl().setLayoutData(gd);

		Composite optionsComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		optionsComposite.setLayout(layout);
		optionsComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

		// we only allow to set either "show in text" or "highlight in text", but not both

		fShowInTextCheckBox= new Button(optionsComposite, SWT.CHECK);
		fShowInTextCheckBox.setText(TextEditorMessages.LinkedModeConfigurationBlock_labels_showIn);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		fShowInTextCheckBox.setLayoutData(gd);

		fDecorationViewer= new ComboViewer(optionsComposite, SWT.READ_ONLY);
		fDecorationViewer.setContentProvider(new ArrayContentProvider());
		fDecorationViewer.setLabelProvider(new ArrayLabelProvider());
		fDecorationViewer.setComparator(new ViewerComparator(Collator.getInstance()));

		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		fDecorationViewer.getControl().setLayoutData(gd);
		fDecorationViewer.setInput(new Object[] {HIGHLIGHT, SQUIGGLES, BOX, DASHED_BOX, UNDERLINE, IBEAM});

		label= new Label(optionsComposite, SWT.LEFT);
		label.setText(TextEditorMessages.LinkedModeConfigurationBlock_color);
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		label.setLayoutData(gd);

		fAnnotationForegroundColorEditor= new ColorSelector(optionsComposite);
		Button foregroundColorButton= fAnnotationForegroundColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		createDependency(fShowInTextCheckBox, new Control[] {label, foregroundColorButton});

		fAnnotationTypeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleAnnotationListSelection();
			}
		});

		fShowInTextCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ListItem item= getSelectedItem();
				final boolean value= fShowInTextCheckBox.getSelection();
				if (value) {
					// enable whatever is in the combo
					String[] decoration= (String[]) ((IStructuredSelection) fDecorationViewer.getSelection()).getFirstElement();
					if (HIGHLIGHT.equals(decoration))
						getPreferenceStore().setValue(item.highlightKey, true);
					else
						getPreferenceStore().setValue(item.textKey, true);
				} else {
					// disable both
					getPreferenceStore().setValue(item.textKey, false);
					getPreferenceStore().setValue(item.highlightKey, false);
				}
				getPreferenceStore().setValue(item.textKey, value);
				updateDecorationViewer(item, false);
			}
		});

		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ListItem item= getSelectedItem();
				PreferenceConverter.setValue(getPreferenceStore(), item.colorKey, fAnnotationForegroundColorEditor.getColorValue());
			}
		});

		fDecorationViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			/*
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				String[] decoration= (String[]) ((IStructuredSelection) fDecorationViewer.getSelection()).getFirstElement();
				ListItem item= getSelectedItem();

				if (fShowInTextCheckBox.getSelection()) {
					if (HIGHLIGHT.equals(decoration)) {
						getPreferenceStore().setValue(item.highlightKey, true);
						getPreferenceStore().setValue(item.textKey, false);
						getPreferenceStore().setValue(item.textStyleKey, AnnotationPreference.STYLE_NONE);
					} else {
						getPreferenceStore().setValue(item.highlightKey, false);
						getPreferenceStore().setValue(item.textKey, true);
						getPreferenceStore().setValue(item.textStyleKey, decoration[1]);
					}
				}
			}
		});

		return composite;

	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.IPreferenceConfigurationBlock#applyData(java.lang.Object)
	 * @since 3.4
	 */
	public void applyData(Object data) {
	}

	/**
	 * Returns the number of pixels corresponding to the width of the given number of characters.
	 * <p>
	 * This method may only be called after <code>initializeDialogUnits</code> has been called.
	 * </p>
	 * <p>
	 * Clients may call this framework method, but should not override it.
	 * </p>
	 *
	 * @param chars the number of characters
	 * @return the number of pixels
	 */
    protected int convertWidthInCharsToPixels(int chars) {
        // test for failure to initialize for backward compatibility
        if (fFontMetrics == null)
            return 0;
        return Dialog.convertWidthInCharsToPixels(fFontMetrics, chars);
    }

	/**
	 * Returns the number of pixels corresponding to the height of the given number of characters.
	 * <p>
	 * This method may only be called after <code>initializeDialogUnits</code> has been called.
	 * </p>
	 * <p>
	 * Clients may call this framework method, but should not override it.
	 * </p>
	 *
	 * @param chars the number of characters
	 * @return the number of pixels
	 */
    protected int convertHeightInCharsToPixels(int chars) {
        // test for failure to initialize for backward compatibility
        if (fFontMetrics == null)
            return 0;
        return Dialog.convertHeightInCharsToPixels(fFontMetrics, chars);
    }

	/**
	 * Initializes the computation of horizontal and vertical dialog units based on the size of
	 * current font.
	 * <p>
	 * This method must be called before any of the dialog unit based conversion methods are called.
	 * </p>
	 *
	 * @param testControl a control from which to obtain the current font
	 */
    protected void initializeDialogUnits(Control testControl) {
        // Compute and store a font metric
        GC gc = new GC(testControl);
        gc.setFont(JFaceResources.getDialogFont());
        fFontMetrics = gc.getFontMetrics();
        gc.dispose();
    }

	private void handleAnnotationListSelection() {
		ListItem item= getSelectedItem();

		RGB rgb= PreferenceConverter.getColor(getPreferenceStore(), item.colorKey);
		fAnnotationForegroundColorEditor.setColorValue(rgb);

		boolean highlight= item.highlightKey == null ? false : getPreferenceStore().getBoolean(item.highlightKey);
		boolean showInText = item.textKey == null ? false : getPreferenceStore().getBoolean(item.textKey);
		fShowInTextCheckBox.setSelection(showInText || highlight);

		updateDecorationViewer(item, true);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.preferences.IPreferenceConfigurationBlock#initialize()
	 */
	public void initialize() {
		initializeFields();

		fAnnotationTypeViewer.setInput(fListModel);
		fAnnotationTypeViewer.getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fAnnotationTypeViewer != null && !fAnnotationTypeViewer.getControl().isDisposed()) {
					fAnnotationTypeViewer.setSelection(new StructuredSelection(fListModel[0]));
					initializeFields();
				}
			}
		});
	}

	private ListItem getSelectedItem() {
		return (ListItem) ((IStructuredSelection) fAnnotationTypeViewer.getSelection()).getFirstElement();
	}

	private void updateDecorationViewer(ListItem item, boolean changed) {
		// decoration selection: if the checkbox is enabled, there is
		// only one case where the combo is not enabled: if both the highlight and textStyle keys are null
		final boolean enabled= fShowInTextCheckBox.getSelection() && !(item.highlightKey == null && item.textStyleKey == null);
		fDecorationViewer.getControl().setEnabled(enabled);

		if (changed) {
			String[] selection= null;
			ArrayList list= new ArrayList();

			list.addAll(item.validStyles);

			if (getPreferenceStore().getBoolean(item.highlightKey))
				selection= HIGHLIGHT;

			// set selection
			if (selection == null) {
				String val= getPreferenceStore().getString(item.textStyleKey);
				for (Iterator iter= list.iterator(); iter.hasNext();) {
					String[] elem= (String[]) iter.next();
					if (elem[1].equals(val)) {
						selection= elem;
						break;
					}
				}
			}

			fDecorationViewer.setInput(list.toArray(new Object[list.size()]));
			if (selection == null)
				selection= (String[]) list.get(0);
			fDecorationViewer.setSelection(new StructuredSelection((Object) selection), true);
		}
	}


	public void performOk() {
		getPreferenceStore().propagate();

		try {
			Platform.getPreferencesService().getRootNode().node(InstanceScope.SCOPE).node(EditorsUI.PLUGIN_ID).flush();
		} catch (BackingStoreException e) {
			EditorsPlugin.log(e);
		}
	}


	public void performDefaults() {
		getPreferenceStore().loadDefaults();

		/*
		 * Only call super after updating fShowInTextCheckBox, so that
		 * the master-slave dependencies get properly updated.
		 */
		handleAnnotationListSelection();

		initializeFields();
	}

	public void dispose() {
		OverlayPreferenceStore store= getPreferenceStore();
		if (store != null) {
			store.stop();
		}
	}

	protected void createDependency(final Button master, final Control slave) {
		createDependency(master, new Control[] {slave});
	}

	protected void createDependency(final Button master, final Control[] slaves) {
		Assert.isTrue(slaves.length > 0);
		indent(slaves[0]);
		SelectionListener listener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean state= master.getSelection();
				for (int i= 0; i < slaves.length; i++) {
					slaves[i].setEnabled(state);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		};
		master.addSelectionListener(listener);
		fMasterSlaveListeners.add(listener);
	}

	protected static void indent(Control control) {
		((GridData) control.getLayoutData()).horizontalIndent+= INDENT;
	}

	private void initializeFields() {

        // Update slaves
        Iterator iter= fMasterSlaveListeners.iterator();
        while (iter.hasNext()) {
            SelectionListener listener= (SelectionListener)iter.next();
            listener.widgetSelected(null);
        }
	}

	/*
	 * @see org.eclipse.ui.internal.editors.text.IPreferenceConfigurationBlock#canPerformOk()
	 */
	public boolean canPerformOk() {
		return true;
	}

}
