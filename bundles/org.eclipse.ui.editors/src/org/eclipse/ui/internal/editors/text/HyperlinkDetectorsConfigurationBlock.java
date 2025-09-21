/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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
package org.eclipse.ui.internal.editors.text;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.internal.editors.text.OverlayPreferenceStore.OverlayKey;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.HyperlinkDetectorDescriptor;
import org.eclipse.ui.texteditor.HyperlinkDetectorTargetDescriptor;

import org.eclipse.ui.editors.text.EditorsUI;


/**
 * Configures hyperlink detector preferences.
 *
 * @since 3.3
 */
class HyperlinkDetectorsConfigurationBlock implements IPreferenceConfigurationBlock {


	private static final class ListItem {
		final String id;
		final String name;
		final String targetName;
		String modifierKeys= ""; //$NON-NLS-1$

		ListItem(String id, String name, String targetName, String modifierKeys) {
			this.id= id;
			this.name= name;
			this.targetName= targetName;
			this.modifierKeys= modifierKeys;
		}
	}


	private static final class ItemContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return (ListItem[]) inputElement;
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}


	private final class ItemLabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return ((ListItem) element).name;
			case 1:
				String text= ((ListItem) element).modifierKeys;
				if (text == null) {
					return fHyperlinkDefaultKeyModifierText.getText();
				}
				return text;
			case 2:
				return ((ListItem) element).targetName;
			default:
				Assert.isLegal(false);
			}
			return null; // cannot happen
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
	}


	private static final String MODIFIER_DELIMITER= TextEditorMessages.HyperlinkKeyModifier_delimiter;


	private final OverlayPreferenceStore fStore;

	private CheckboxTableViewer fHyperlinkDetectorsViewer;
	private ListItem[] fListModel;
	private final HyperlinkDetectorDescriptor[] fHyperlinkDetectorDescriptors;

	private Text fHyperlinkDefaultKeyModifierText;
	private Text fHyperlinkKeyModifierText;
	private Button fHyperlinksEnabledCheckBox;
	private StatusInfo fHyperlinkKeyModifierStatus;

	private final PreferencePage fPreferencePage;



	public HyperlinkDetectorsConfigurationBlock(PreferencePage preferencePage, OverlayPreferenceStore store) {
		Assert.isNotNull(store);
		Assert.isNotNull(preferencePage);
		fStore= store;
		fPreferencePage= preferencePage;
		fHyperlinkDetectorDescriptors= EditorsUI.getHyperlinkDetectorRegistry().getHyperlinkDetectorDescriptors();
		fStore.addKeys(createOverlayStoreKeys());
	}

	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
		ArrayList<OverlayKey> overlayKeys= new ArrayList<>();

		for (HyperlinkDetectorDescriptor fHyperlinkDetectorDescriptor : fHyperlinkDetectorDescriptors) {
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, fHyperlinkDetectorDescriptor.getId()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, fHyperlinkDetectorDescriptor.getId() + HyperlinkDetectorDescriptor.STATE_MASK_POSTFIX));
		}

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED));

		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	@Override
	public Control createControl(Composite parent) {

		PixelConverter pixelConverter= new PixelConverter(parent);

		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		composite.setLayout(layout);

		addFiller(composite, 2);

		String label= TextEditorMessages.HyperlinksEnabled_label;
		fHyperlinksEnabledCheckBox= addCheckBox(composite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED, 0);
		fHyperlinksEnabledCheckBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean state= fHyperlinksEnabledCheckBox.getSelection();
				fHyperlinkDefaultKeyModifierText.setEnabled(state);
				fHyperlinkKeyModifierText.setEnabled(state && getSelectedItem() != null);
				fHyperlinkDetectorsViewer.getTable().setEnabled(state);
				handleHyperlinkKeyModifierModified();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Text field for default modifier string
		label= TextEditorMessages.HyperlinkDefaultKeyModifier_label;
		fHyperlinkDefaultKeyModifierText= (Text)addTextField(composite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER, 15, 20, pixelConverter)[1];

		fHyperlinkDefaultKeyModifierText.addKeyListener(new KeyListener() {
			private boolean isModifierCandidate;
			@Override
			public void keyPressed(KeyEvent e) {
				isModifierCandidate= e.keyCode > 0 && e.character == 0 && e.stateMask == 0;
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (isModifierCandidate && e.stateMask > 0 && e.character == 0) {// && e.time -time < 1000) {
					String modifierString= fHyperlinkDefaultKeyModifierText.getText();
					Point selection= fHyperlinkDefaultKeyModifierText.getSelection();
					int i= selection.x - 1;
					while (i > -1 && Character.isWhitespace(modifierString.charAt(i))) {
						i--;
					}
					boolean needsPrefixDelimiter= i > -1 && !String.valueOf(modifierString.charAt(i)).equals(MODIFIER_DELIMITER);

					i= selection.y;
					while (i < modifierString.length() && Character.isWhitespace(modifierString.charAt(i))) {
						i++;
					}
					boolean needsPostfixDelimiter= i < modifierString.length() && !String.valueOf(modifierString.charAt(i)).equals(MODIFIER_DELIMITER);

					String insertString;

					if (needsPrefixDelimiter && needsPostfixDelimiter) {
						insertString= NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_insertDelimiterAndModifierAndDelimiter, Action.findModifierString(e.stateMask));
					} else if (needsPrefixDelimiter) {
						insertString= NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_insertDelimiterAndModifier, Action.findModifierString(e.stateMask));
					} else if (needsPostfixDelimiter) {
						insertString= NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_insertModifierAndDelimiter, Action.findModifierString(e.stateMask));
					} else {
						insertString= Action.findModifierString(e.stateMask);
					}

					fHyperlinkDefaultKeyModifierText.insert(insertString);
				}
			}
		});

		fHyperlinkDefaultKeyModifierText.addModifyListener(e -> handleHyperlinkDefaultKeyModifierModified());

		addFiller(composite, 2);

		Composite editorComposite= new Composite(composite, SWT.NONE);
		GridData gd;

		gd= new GridData(SWT.FILL, SWT.TOP, true, false);
		gd.horizontalSpan= 2;
		gd.horizontalIndent= 20;
		editorComposite.setLayoutData(gd);
		TableColumnLayout tableColumnlayout= new TableColumnLayout();
		editorComposite.setLayout(tableColumnlayout);

		// Hyperlink detector table
		Table hyperlinkDetectorTable= new Table(editorComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
		hyperlinkDetectorTable.setHeaderVisible(true);
		hyperlinkDetectorTable.setLinesVisible(true);
		hyperlinkDetectorTable.setFont(parent.getFont());

		hyperlinkDetectorTable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleListSelection();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		ColumnLayoutData columnLayoutData= new ColumnWeightData(1);

		TableColumn nameColumn= new TableColumn(hyperlinkDetectorTable, SWT.NONE, 0);
		nameColumn.setText(TextEditorMessages.HyperlinkDetectorTable_nameColumn);
		tableColumnlayout.setColumnData(nameColumn, columnLayoutData);

		TableColumn modifierKeysColumn= new TableColumn(hyperlinkDetectorTable, SWT.NONE, 1);
		modifierKeysColumn.setText(TextEditorMessages.HyperlinkDetectorTable_modifierKeysColumn);
		tableColumnlayout.setColumnData(modifierKeysColumn, columnLayoutData);

		TableColumn targetNameColumn= new TableColumn(hyperlinkDetectorTable, SWT.NONE, 2);
		targetNameColumn.setText(TextEditorMessages.HyperlinkDetectorTable_targetNameColumn);
		tableColumnlayout.setColumnData(targetNameColumn, columnLayoutData);

		fHyperlinkDetectorsViewer= new CheckboxTableViewer(hyperlinkDetectorTable);
		fHyperlinkDetectorsViewer.setUseHashlookup(true);

		fHyperlinkDetectorsViewer.addCheckStateListener(event -> {
			String id= ((ListItem) event.getElement()).id;
			if (id == null) {
				return;
			}
			fStore.setValue(id, !event.getChecked());
		});

		fHyperlinkDetectorsViewer.setLabelProvider(new ItemLabelProvider());
		fHyperlinkDetectorsViewer.setContentProvider(new ItemContentProvider());
		gd= new GridData(SWT.FILL, SWT.FILL, true , false);
		gd.heightHint= pixelConverter.convertHeightInCharsToPixels(10);
		fHyperlinkDetectorsViewer.getControl().setLayoutData(gd);

//		addFiller(composite, 2);

		// Text field for modifier string
		label= TextEditorMessages.HyperlinkKeyModifier_label;
		fHyperlinkKeyModifierText= (Text)addTextField(composite, label, null, 15, 20, pixelConverter)[1];

		fHyperlinkKeyModifierText.addKeyListener(new KeyListener() {
			private boolean isModifierCandidate;
			@Override
			public void keyPressed(KeyEvent e) {
				isModifierCandidate= e.keyCode > 0 && e.character == 0 && e.stateMask == 0;
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (isModifierCandidate && e.stateMask > 0 && e.character == 0) {// && e.time -time < 1000) {
					String modifierString= fHyperlinkKeyModifierText.getText();
					Point selection= fHyperlinkKeyModifierText.getSelection();
					int i= selection.x - 1;
					while (i > -1 && Character.isWhitespace(modifierString.charAt(i))) {
						i--;
					}
					boolean needsPrefixDelimiter= i > -1 && !String.valueOf(modifierString.charAt(i)).equals(MODIFIER_DELIMITER);

					i= selection.y;
					while (i < modifierString.length() && Character.isWhitespace(modifierString.charAt(i))) {
						i++;
					}
					boolean needsPostfixDelimiter= i < modifierString.length() && !String.valueOf(modifierString.charAt(i)).equals(MODIFIER_DELIMITER);

					String insertString;

					if (needsPrefixDelimiter && needsPostfixDelimiter) {
						insertString= NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_insertDelimiterAndModifierAndDelimiter, Action.findModifierString(e.stateMask));
					} else if (needsPrefixDelimiter) {
						insertString= NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_insertDelimiterAndModifier, Action.findModifierString(e.stateMask));
					} else if (needsPostfixDelimiter) {
						insertString= NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_insertModifierAndDelimiter, Action.findModifierString(e.stateMask));
					} else {
						insertString= Action.findModifierString(e.stateMask);
					}

					fHyperlinkKeyModifierText.insert(insertString);
				}
			}
		});

		fHyperlinkKeyModifierText.addModifyListener(e -> handleHyperlinkKeyModifierModified());

		return composite;
	}

	@Override
	public void applyData(Object data) {
	}

	private void addFiller(Composite composite, int horizontalSpan) {
		PixelConverter pixelConverter= new PixelConverter(composite);
		Label filler= new Label(composite, SWT.LEFT );
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= horizontalSpan;
		gd.heightHint= pixelConverter.convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}

	private Button addCheckBox(Composite composite, String label, final String key, int indentation) {
		final Button checkBox= new Button(composite, SWT.CHECK);
		checkBox.setText(label);

		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		gd.horizontalSpan= 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean value= checkBox.getSelection();
				fStore.setValue(key, value);
			}
		});

		return checkBox;
	}

	private Control[] addTextField(Composite composite, String label, final String key, int textLimit, int indentation, PixelConverter pixelConverter) {
		Label labelControl= new Label(composite, SWT.NONE);
		labelControl.setText(label);
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		labelControl.setLayoutData(gd);

		final Text textControl= new Text(composite, SWT.BORDER | SWT.SINGLE);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint= pixelConverter.convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);

		textControl.addModifyListener(e -> {
			String value= textControl.getText();
			if (key != null) {
				fStore.setValue(key, value);
			}
		});

		return new Control[] {labelControl, textControl};
	}

	private Object[] getCheckedItems() {
		List<ListItem> result= new ArrayList<>();
		for (ListItem i : fListModel) {
			if (!fStore.getBoolean(i.id)) {
				result.add(i);
			}
		}
		return result.toArray();
	}

	@Override
	public boolean canPerformOk() {
		return true;
	}

	@Override
	public void performOk() {
		fStore.setValue(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER_MASK, computeStateMask(fHyperlinkKeyModifierText.getText()));
	}

	@Override
	public void performDefaults() {
		fStore.loadDefaults();
		initialize();
	}

	private void handleListSelection() {
		ListItem item= getSelectedItem();
		if (item == null) {
			fHyperlinkKeyModifierText.setEnabled(false);
			return;
		}
		fHyperlinkKeyModifierText.setEnabled(fHyperlinkDetectorsViewer.getChecked(item));
		String text= item.modifierKeys;
		if (text == null) {
			text= fHyperlinkDefaultKeyModifierText.getText();
		}
		fHyperlinkKeyModifierText.setText(text);

	}

	@Override
	public void initialize() {
		String modifierString= fStore.getString(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER);
		if (computeStateMask(modifierString) == -1) {
			// Fix possible illegal modifier string
			int stateMask= fStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER_MASK);
			if (stateMask == -1) {
				fHyperlinkDefaultKeyModifierText.setText(""); //$NON-NLS-1$
			} else {
				fHyperlinkDefaultKeyModifierText.setText(getModifierString(stateMask));
			}
		} else {
			fHyperlinkDefaultKeyModifierText.setText(modifierString);
		}
		boolean isEnabled= fStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED);
		fHyperlinksEnabledCheckBox.setSelection(isEnabled);
		fHyperlinkKeyModifierText.setEnabled(isEnabled);
		fHyperlinkDefaultKeyModifierText.setEnabled(isEnabled);

		fListModel= createListModel();
		fHyperlinkDetectorsViewer.setInput(fListModel);
		fHyperlinkDetectorsViewer.setAllChecked(false);
		fHyperlinkDetectorsViewer.setCheckedElements(getCheckedItems());
		fHyperlinkDetectorsViewer.getTable().setEnabled(isEnabled);

		fHyperlinkKeyModifierText.setText(""); //$NON-NLS-1$

		handleListSelection();
	}

	private ListItem[] createListModel() {
		ArrayList<ListItem> listModelItems= new ArrayList<>();
		for (HyperlinkDetectorDescriptor desc : fHyperlinkDetectorDescriptors) {
			HyperlinkDetectorTargetDescriptor target= desc.getTarget();

			int stateMask= fStore.getInt(desc.getId() + HyperlinkDetectorDescriptor.STATE_MASK_POSTFIX);
			String modifierKeys= getModifierString(stateMask);

			listModelItems.add(new ListItem(desc.getId(), desc.getName(), target.getName(), modifierKeys));
		}

		Comparator<ListItem> comparator= (o1, o2) -> {
			String label1= o1.name;
			String label2= o2.name;
			return Collator.getInstance().compare(label1, label2);

		};
		Collections.sort(listModelItems, comparator);

		ListItem[] items= new ListItem[listModelItems.size()];
		listModelItems.toArray(items);
		return items;
	}

	private ListItem getSelectedItem() {
		return (ListItem) fHyperlinkDetectorsViewer.getStructuredSelection().getFirstElement();
	}

	private void handleHyperlinkKeyModifierModified() {
		String modifiers= fHyperlinkKeyModifierText.getText();

		int stateMask= computeStateMask(modifiers);

		if (fHyperlinksEnabledCheckBox.getSelection() && (stateMask == -1 || (stateMask & SWT.SHIFT) != 0)) {
			if (stateMask == -1) {
				fHyperlinkKeyModifierStatus= new StatusInfo(IStatus.ERROR, NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_error_modifierIsNotValid, modifiers));
			} else {
				fHyperlinkKeyModifierStatus= new StatusInfo(IStatus.ERROR, TextEditorMessages.HyperlinkKeyModifier_error_shiftIsDisabled);
			}
			applyToStatusLine(getHyperlinkKeyModifierStatus());
			fPreferencePage.setValid(getHyperlinkKeyModifierStatus().isOK());
		} else {
			ListItem item= getSelectedItem();
			if (item != null) {
				if (item.modifierKeys != null || !modifiers.equalsIgnoreCase(fHyperlinkDefaultKeyModifierText.getText())) {
					item.modifierKeys= modifiers;
				}
				fHyperlinkDetectorsViewer.refresh(getSelectedItem());
				fStore.setValue(item.id + HyperlinkDetectorDescriptor.STATE_MASK_POSTFIX, stateMask);
			}
			fHyperlinkKeyModifierStatus= new StatusInfo();
			fPreferencePage.setValid(true);
			applyToStatusLine(fHyperlinkKeyModifierStatus);
		}
	}

	private void handleHyperlinkDefaultKeyModifierModified() {
		String modifiers= fHyperlinkDefaultKeyModifierText.getText();

		int stateMask= computeStateMask(modifiers);

		if (fHyperlinksEnabledCheckBox.getSelection() && (stateMask == -1 || (stateMask & SWT.SHIFT) != 0)) {
			if (stateMask == -1) {
				fHyperlinkKeyModifierStatus= new StatusInfo(IStatus.ERROR, NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_error_modifierIsNotValid, modifiers));
			} else {
				fHyperlinkKeyModifierStatus= new StatusInfo(IStatus.ERROR, TextEditorMessages.HyperlinkKeyModifier_error_shiftIsDisabled);
			}
			applyToStatusLine(getHyperlinkKeyModifierStatus());
			fPreferencePage.setValid(getHyperlinkKeyModifierStatus().isOK());
		} else {
			fStore.setValue(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER, modifiers);
			fHyperlinkKeyModifierStatus= new StatusInfo();
			fPreferencePage.setValid(true);
			applyToStatusLine(fHyperlinkKeyModifierStatus);
			fHyperlinkDetectorsViewer.refresh();
			handleListSelection();
		}
	}

	private IStatus getHyperlinkKeyModifierStatus() {
		if (fHyperlinkKeyModifierStatus == null) {
			fHyperlinkKeyModifierStatus= new StatusInfo();
		}
		return fHyperlinkKeyModifierStatus;
	}

	/**
	 * Computes the state mask for the given modifier string.
	 *
	 * @param modifiers	the string with the modifiers, separated by '+', '-', ';', ',' or '.'
	 * @return the state mask or -1 if the input is invalid
	 */
	private static final int computeStateMask(String modifiers) {
		if (modifiers == null) {
			return -1;
		}

		if (modifiers.isEmpty()) {
			return SWT.NONE;
		}

		int stateMask= 0;
		StringTokenizer modifierTokenizer= new StringTokenizer(modifiers, ",;.:+-* "); //$NON-NLS-1$
		while (modifierTokenizer.hasMoreTokens()) {
			int modifier= findLocalizedModifier(modifierTokenizer.nextToken());
			if (modifier == 0 || (stateMask & modifier) == modifier) {
				return -1;
			}
			stateMask= stateMask | modifier;
		}
		return stateMask;
	}

	/**
	 * Maps the localized modifier name to a code in the same
	 * manner as #findModifier.
	 *
	 * @param modifierName the modifier name
	 * @return the SWT modifier bit, or <code>0</code> if no match was found
	 */
	private static final int findLocalizedModifier(String modifierName) {
		if (modifierName == null) {
			return 0;
		}

		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.CTRL))) {
			return SWT.CTRL;
		}
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.SHIFT))) {
			return SWT.SHIFT;
		}
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.ALT))) {
			return SWT.ALT;
		}
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.COMMAND))) {
			return SWT.COMMAND;
		}

		return 0;
	}

	/**
	 * Returns the modifier string for the given SWT modifier
	 * modifier bits.
	 *
	 * @param stateMask	the SWT modifier bits
	 * @return the modifier string
	 */
	private static final String getModifierString(int stateMask) {
		if (stateMask == -1) {
			return null;
		}

		String modifierString= ""; //$NON-NLS-1$
		if ((stateMask & SWT.CTRL) == SWT.CTRL) {
			modifierString= appendModifierString(modifierString, SWT.CTRL);
		}
		if ((stateMask & SWT.ALT) == SWT.ALT) {
			modifierString= appendModifierString(modifierString, SWT.ALT);
		}
		if ((stateMask & SWT.SHIFT) == SWT.SHIFT) {
			modifierString= appendModifierString(modifierString, SWT.SHIFT);
		}
		if ((stateMask & SWT.COMMAND) == SWT.COMMAND) {
			modifierString= appendModifierString(modifierString,  SWT.COMMAND);
		}

		return modifierString;
	}

	/**
	 * Appends to modifier string of the given SWT modifier bit
	 * to the given modifierString.
	 *
	 * @param modifierString	the modifier string
	 * @param modifier			an int with SWT modifier bit
	 * @return the concatenated modifier string
	 */
	private static final String appendModifierString(String modifierString, int modifier) {
		if (modifierString == null) {
			modifierString= ""; //$NON-NLS-1$
		}
		String newModifierString= Action.findModifierString(modifier);
		if (modifierString.isEmpty()) {
			return newModifierString;
		}
		return NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_concatModifierStrings, new String[] {modifierString, newModifierString});
	}

	/**
	 * Applies the status to the status line of a dialog page.
	 *
	 * @param status the status
	 */
	private void applyToStatusLine(IStatus status) {
		String message= status.getMessage();
		switch (status.getSeverity()) {
			case IStatus.OK:
				fPreferencePage.setMessage(message, IMessageProvider.NONE);
				fPreferencePage.setErrorMessage(null);
				break;
			case IStatus.WARNING:
				fPreferencePage.setMessage(message, IMessageProvider.WARNING);
				fPreferencePage.setErrorMessage(null);
				break;
			case IStatus.INFO:
				fPreferencePage.setMessage(message, IMessageProvider.INFORMATION);
				fPreferencePage.setErrorMessage(null);
				break;
			default:
				if (message.isEmpty()) {
					message= null;
				}
				fPreferencePage.setMessage(null);
				fPreferencePage.setErrorMessage(message);
				break;
		}
	}

	@Override
	public void dispose() {
	}

}
