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

package org.eclipse.ui.internal.editors.text;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.jface.text.Assert;

import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.editors.text.TextEditorDefaultsPreferencePage.EnumeratedDomain.EnumValue;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;


/**
 * The preference page for setting the editor options.
 * <p>
 * This class is internal and not intended to be used by clients.</p>
 *
 * @since 3.1
 */
public class TextEditorDefaultsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static abstract class Initializer {

		protected final Preference fPreference;

		protected Initializer(Preference preference) {
			fPreference= preference;
		}

		public abstract void initialize();
	}


	public final class InitializerFactory {
		private class TextInitializer extends Initializer {
			private final Text fText;

			public TextInitializer(Preference preference, Text control) {
				super(preference);
				fText= control;
			}
			public void initialize() {
				String value= fOverlayStore.getString(fPreference.getKey());
				fText.setText(value);
			}
		}

		private class CheckboxInitializer extends Initializer {
			private final Button fControl;

			public CheckboxInitializer(Preference preference, Button control) {
				super(preference);
				fControl= control;
			}
			public void initialize() {
				boolean value= fOverlayStore.getBoolean(fPreference.getKey());
				fControl.setSelection(value);
			}
		}

		private class ComboInitializer extends Initializer {
			private final Combo fControl;
			private final EnumeratedDomain fDomain;

			public ComboInitializer(Preference preference, Combo control, EnumeratedDomain domain) {
				super(preference);
				fControl= control;
				fDomain= domain;
			}
			public void initialize() {
				int value= fOverlayStore.getInt(fPreference.getKey());
				EnumValue enumValue= fDomain.getValueByInteger(value);
				if (enumValue != null) {
					int index= fDomain.getIndex(enumValue);
					if (index >= 0)
						fControl.select(index);
				}
			}
		}

		private class SpinnerInitializer extends Initializer {
			private final Spinner fControl;
			private final EnumeratedDomain fDomain;

			public SpinnerInitializer(Preference preference, Spinner control, EnumeratedDomain domain) {
				super(preference);
				fControl= control;
				fDomain= domain;
			}
			public void initialize() {
				int value= fOverlayStore.getInt(fPreference.getKey());
				EnumValue enumValue= fDomain.getValueByInteger(value);
				if (enumValue != null) {
					fControl.setSelection(value);
				}
			}
		}

		public Initializer create(Preference preference, Text control) {
			return new TextInitializer(preference, control);
		}

		public Initializer create(Preference preference, Button control) {
			return new CheckboxInitializer(preference, control);
		}

		public Initializer create(Preference preference, Combo control, EnumeratedDomain domain) {
			return new ComboInitializer(preference, control, domain);
		}

		public Initializer create(Preference preference, Spinner control, EnumeratedDomain domain) {
			return new SpinnerInitializer(preference, control, domain);
		}
	}


	abstract static class Domain {
		public abstract IStatus validate(Object value);
		protected int parseInteger(Object val) throws NumberFormatException {
			if (val instanceof Integer) {
				return ((Integer) val).intValue();
			}
			if (val instanceof String) {
				return Integer.parseInt((String) val);
			}
			throw new NumberFormatException(NLSUtility.format(TextEditorMessages.TextEditorPreferencePage_invalidInput, String.valueOf(val)));
		}
	}

	static class IntegerDomain extends Domain {
		private final int fMax;
		private final int fMin;
		public IntegerDomain(int min, int max) {
			Assert.isLegal(max >= min);
			fMax= max;
			fMin= min;
		}

		public IStatus validate(Object value) {
			StatusInfo status= new StatusInfo();
			if (value instanceof String && ((String)value).length() == 0) {
				status.setError(TextEditorMessages.TextEditorPreferencePage_emptyInput);
				return status;
			}
			try {
				int integer= parseInteger(value);
				if (!rangeCheck(integer))
					status.setError(NLSUtility.format(TextEditorMessages.TextEditorPreferencePage_invalidInput, String.valueOf(integer)));
			} catch (NumberFormatException e) {
					status.setError(NLSUtility.format(TextEditorMessages.TextEditorPreferencePage_invalidInput, String.valueOf(value)));
			}
			return status;
		}

		protected boolean rangeCheck(int i) {
			return (i >= fMin && i <= fMax);
		}

	}

	static class EnumeratedDomain extends Domain {
		public final static class EnumValue {
			private final int fValue;
			private final String fName;
			public EnumValue(int value) {
				this(value, null);
			}
			public EnumValue(int value, String name) {
				fValue= value;
				fName= name;
			}
			public String getLabel() {
				return fName == null ? String.valueOf(fValue) : fName;
			}
			public int getIntValue() {
				return fValue;
			}
			public final int hashCode() {
				return getIntValue();
			}
			public boolean equals(Object obj) {
				if (obj instanceof EnumValue) {
					return ((EnumValue) obj).getIntValue() == fValue;
				}
				return false;
			}
		}

		private final java.util.List fItems= new ArrayList();
		private final Set fValueSet= new HashSet();

		public void addValue(EnumValue val) {
			if (fValueSet.contains(val))
				fItems.remove(val);
			fItems.add(val);
			fValueSet.add(val);
		}

		public int getIndex(EnumValue enumValue) {
			int i= 0;
			for (Iterator it= fItems.iterator(); it.hasNext();) {
				EnumValue ev= (EnumValue) it.next();
				if (ev.equals(enumValue))
					return i;
				i++;
			}
			return -1;
		}

		public EnumValue getValueByIndex (int index) {
			if (index >= 0 && fItems.size() > index)
				return (EnumValue) fItems.get(index);
			return null;
		}

		public EnumValue getValueByInteger(int intValue) {
			for (Iterator it= fItems.iterator(); it.hasNext();) {
				EnumValue e= (EnumValue) it.next();
				if (e.getIntValue() == intValue)
					return e;
			}
			return null;
		}

		public void addValue(int val) {
			addValue(new EnumValue(val));
		}

		public void addRange(int from, int to) {
			while (from <= to)
				addValue(from++);
		}

		public IStatus validate(Object value) {
			StatusInfo status= new StatusInfo();
			if (value instanceof String && ((String)value).length() == 0) {
				status.setError(TextEditorMessages.TextEditorPreferencePage_emptyInput);
				return status;
			}
			try {
				EnumValue e= parseEnumValue(value);
				if (!fValueSet.contains(e))
					status.setError(NLSUtility.format(TextEditorMessages.TextEditorPreferencePage_invalidRange, new String[] {getValueByIndex(0).getLabel(), getValueByIndex(fItems.size() - 1).getLabel()}));
			} catch (NumberFormatException e) {
				status.setError(NLSUtility.format(TextEditorMessages.TextEditorPreferencePage_invalidInput, String.valueOf(value)));
			}

			return status;
		}

		private EnumValue parseEnumValue(Object value) {
			if (value instanceof EnumValue)
				return (EnumValue) value;
			int integer= parseInteger(value);
			return getValueByInteger(integer);
		}

		public EnumValue getMinimumValue() {
			return getValueByIndex(0);
		}

		public EnumValue getMaximumValue() {
			return getValueByIndex(fItems.size() - 1);
		}
	}

	static class BooleanDomain extends Domain {
		public IStatus validate(Object value) {
			StatusInfo status= new StatusInfo();
			if (value instanceof String && ((String)value).length() == 0) {
				status.setError(TextEditorMessages.TextEditorPreferencePage_emptyInput);
				return status;
			}
			try {
				parseBoolean(value);
			} catch (NumberFormatException e) {
				status.setError(NLSUtility.format(TextEditorMessages.TextEditorPreferencePage_invalidInput, String.valueOf(value)));
			}

			return status;
		}

		private boolean parseBoolean(Object value) throws NumberFormatException {
			if (value instanceof Boolean)
				return ((Boolean) value).booleanValue();

			if (value instanceof String) {
				if (Boolean.TRUE.toString().equalsIgnoreCase((String) value))
					return true;
				if (Boolean.FALSE.toString().equalsIgnoreCase((String) value))
					return false;
			}

			throw new NumberFormatException(NLSUtility.format(TextEditorMessages.TextEditorPreferencePage_invalidInput, String.valueOf(value)));
		}
	}

	private static class Preference {
		private String fKey;
		private String fName;
		private String fDescription; // for tooltips

		public Preference(String key, String name, String description) {
			Assert.isNotNull(key);
			Assert.isNotNull(name);
			fKey= key;
			fName= name;
			fDescription= description;
		}
		public final String getKey() {
			return fKey;
		}
		public final String getName() {
			return fName;
		}
		public final String getDescription() {
			return fDescription;
		}
	}


	private static final String MODIFIER_DELIMITER= TextEditorMessages.HyperlinkKeyModifier_delimiter;

	private final String[][] fAppearanceColorListModel= new String[][] {
		{TextEditorMessages.TextEditorPreferencePage_lineNumberForegroundColor, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR, null},
		{TextEditorMessages.TextEditorPreferencePage_currentLineHighlighColor, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR, null},
		{TextEditorMessages.TextEditorPreferencePage_printMarginColor, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR, null},
		{TextEditorMessages.TextEditorPreferencePage_selectionForegroundColor, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR},
		{TextEditorMessages.TextEditorPreferencePage_selectionBackgroundColor, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR},
		{TextEditorMessages.TextEditorPreferencePage_backgroundColor, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT},
		{TextEditorMessages.TextEditorPreferencePage_foregroundColor, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT},
		{TextEditorMessages.HyperlinkColor_label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_COLOR, null},
	};

	private OverlayPreferenceStore fOverlayStore;

	private List fAppearanceColorList;
	private ColorSelector fAppearanceColorEditor;
	private Button fAppearanceColorDefault;

	private Text fHyperlinkKeyModifierText;
	private Button fHyperlinksEnabledCheckBox;
	private StatusInfo fHyperlinkKeyModifierStatus;

	/**
	 * Tells whether the fields are initialized.
	 */
	private boolean fFieldsInitialized= false;

	private ArrayList fMasterSlaveListeners= new ArrayList();

	private java.util.List fInitializers= new ArrayList();

	private InitializerFactory fInitializerFactory= new InitializerFactory();


	public TextEditorDefaultsPreferencePage() {
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());

		fOverlayStore= createOverlayStore();
	}

	private OverlayPreferenceStore createOverlayStore() {

		ArrayList overlayKeys= new ArrayList();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNDO_HISTORY_SIZE));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER_MASK));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_DISABLE_OVERWRITE_MODE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.SHOW_RANGE_INDICATOR));

		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return new OverlayPreferenceStore(getPreferenceStore(), keys);
	}

	/*
	 * @see IWorkbenchPreferencePage#init()
	 */
	public void init(IWorkbench workbench) {
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ITextEditorHelpContextIds.TEXT_EDITOR_PREFERENCE_PAGE);
	}

	private void handleAppearanceColorListSelection() {
		int i= fAppearanceColorList.getSelectionIndex();
		if (i == -1)
			return;

		String key= fAppearanceColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(fOverlayStore, key);
		fAppearanceColorEditor.setColorValue(rgb);
		updateAppearanceColorWidgets(fAppearanceColorListModel[i][2]);
	}

	private void updateAppearanceColorWidgets(String systemDefaultKey) {
		if (systemDefaultKey == null) {
			fAppearanceColorDefault.setSelection(false);
			fAppearanceColorDefault.setVisible(false);
			fAppearanceColorEditor.getButton().setEnabled(true);
		} else {
			boolean systemDefault= fOverlayStore.getBoolean(systemDefaultKey);
			fAppearanceColorDefault.setSelection(systemDefault);
			fAppearanceColorDefault.setVisible(true);
			fAppearanceColorEditor.getButton().setEnabled(!systemDefault);
		}
	}

	private Control createAppearancePage(Composite parent) {

		Composite appearanceComposite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout(); layout.numColumns= 2;
		appearanceComposite.setLayout(layout);

		String label= TextEditorMessages.TextEditorPreferencePage_displayedTabWidth;
		Preference tabWidth= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, label, null);
		IntegerDomain tabWidthDomain= new IntegerDomain(1, 16);
		addTextField(appearanceComposite, tabWidth, tabWidthDomain, 2, 0);

		label= TextEditorMessages.TextEditorPreferencePage_undoHistorySize;
		Preference undoHistorySize= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNDO_HISTORY_SIZE, label, null);
		IntegerDomain undoHistorySizeDomain= new IntegerDomain(0, 99999);
		addTextField(appearanceComposite, undoHistorySize, undoHistorySizeDomain, 5, 0);

		label= TextEditorMessages.TextEditorPreferencePage_highlightCurrentLine;
		Preference highlightCurrentLine= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, label, null);
		addCheckBox(appearanceComposite, highlightCurrentLine, new BooleanDomain(), 0);

		label= TextEditorMessages.TextEditorPreferencePage_showPrintMargin;
		Preference showPrintMargin= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, label, null);
		Button showPrintMarginButton= addCheckBox(appearanceComposite, showPrintMargin, new BooleanDomain(), 0);

		label= TextEditorMessages.TextEditorPreferencePage_printMarginColumn;
		Preference printMarginColumn= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, label, null);
		IntegerDomain printMarginDomain= new IntegerDomain(20, 200);
		Control[] printMarginControls= addTextField(appearanceComposite, printMarginColumn, printMarginDomain, 3, 20);
		createDependency(showPrintMarginButton, showPrintMargin, printMarginControls);

		label= TextEditorMessages.TextEditorPreferencePage_showLineNumbers;
		Preference showLineNumbers= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, label, null);
		addCheckBox(appearanceComposite, showLineNumbers, new BooleanDomain(), 0);

		label= TextEditorMessages.TextEditorDefaultsPreferencePage_range_indicator;
		Preference showMagnet= new Preference(AbstractDecoratedTextEditorPreferenceConstants.SHOW_RANGE_INDICATOR, label, null);
		addCheckBox(appearanceComposite, showMagnet, new BooleanDomain(), 0);
		
		label= TextEditorMessages.HyperlinksEnabled_label;
		Preference hyperlinksEnabled= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED, label, null);
		fHyperlinksEnabledCheckBox= addCheckBox(appearanceComposite, hyperlinksEnabled, new BooleanDomain(), 0);
		fHyperlinksEnabledCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean state= fHyperlinksEnabledCheckBox.getSelection();
				fHyperlinkKeyModifierText.setEnabled(state);
				handleHyperlinkKeyModifierModified();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Text field for modifier string
		label= TextEditorMessages.HyperlinkKeyModifier_label;
		Preference hyperlinkModifier= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER, label, null);
		fHyperlinkKeyModifierText= (Text)addTextField(appearanceComposite, hyperlinkModifier, null, 20, 20)[1];

		fHyperlinkKeyModifierText.addKeyListener(new KeyListener() {
			private boolean isModifierCandidate;
			public void keyPressed(KeyEvent e) {
				isModifierCandidate= e.keyCode > 0 && e.character == 0 && e.stateMask == 0;
			}

			public void keyReleased(KeyEvent e) {
				if (isModifierCandidate && e.stateMask > 0 && e.stateMask == e.stateMask && e.character == 0) {// && e.time -time < 1000) {
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

					if (needsPrefixDelimiter && needsPostfixDelimiter)
						insertString= NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_insertDelimiterAndModifierAndDelimiter, new String[] {Action.findModifierString(e.stateMask)});
					else if (needsPrefixDelimiter)
						insertString= NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_insertDelimiterAndModifier, new String[] {Action.findModifierString(e.stateMask)});
					else if (needsPostfixDelimiter)
						insertString= NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_insertModifierAndDelimiter, new String[] {Action.findModifierString(e.stateMask)});
					else
						insertString= Action.findModifierString(e.stateMask);

					fHyperlinkKeyModifierText.insert(insertString);
				}
			}
		});

		fHyperlinkKeyModifierText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleHyperlinkKeyModifierModified();
			}
		});

		label= TextEditorMessages.TextEditorPreferencePage_overwriteMode;
		Preference disableOverwrite= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_DISABLE_OVERWRITE_MODE, label, null);
		addCheckBox(appearanceComposite, disableOverwrite, new BooleanDomain(), 0);

		Label l= new Label(appearanceComposite, SWT.LEFT );
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		gd.heightHint= convertHeightInCharsToPixels(1) / 2;
		l.setLayoutData(gd);
		
		l= new Label(appearanceComposite, SWT.LEFT);
		l.setText(TextEditorMessages.TextEditorPreferencePage_appearanceOptions);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		l.setLayoutData(gd);

		Composite editorComposite= new Composite(appearanceComposite, SWT.NONE);
		layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);

		fAppearanceColorList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH);
		gd.heightHint= fAppearanceColorList.getItemHeight() * 8;
		fAppearanceColorList.setLayoutData(gd);

		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		l= new Label(stylesComposite, SWT.LEFT);
		l.setText(TextEditorMessages.TextEditorPreferencePage_color);
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		l.setLayoutData(gd);

		fAppearanceColorEditor= new ColorSelector(stylesComposite);
		Button foregroundColorButton= fAppearanceColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		SelectionListener colorDefaultSelectionListener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				boolean systemDefault= fAppearanceColorDefault.getSelection();
				fAppearanceColorEditor.getButton().setEnabled(!systemDefault);

				int i= fAppearanceColorList.getSelectionIndex();
				if (i == -1)
					return;

				String key= fAppearanceColorListModel[i][2];
				if (key != null)
					fOverlayStore.setValue(key, systemDefault);
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		};

		fAppearanceColorDefault= new Button(stylesComposite, SWT.CHECK);
		fAppearanceColorDefault.setText(TextEditorMessages.TextEditorPreferencePage_systemDefault);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		fAppearanceColorDefault.setLayoutData(gd);
		fAppearanceColorDefault.setVisible(false);
		fAppearanceColorDefault.addSelectionListener(colorDefaultSelectionListener);

		fAppearanceColorList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				handleAppearanceColorListSelection();
			}
		});
		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i= fAppearanceColorList.getSelectionIndex();
				if (i == -1)
					return;

				String key= fAppearanceColorListModel[i][1];
				PreferenceConverter.setValue(fOverlayStore, key, fAppearanceColorEditor.getColorValue());
			}
		});
		
		Link link= new Link(appearanceComposite, SWT.NONE);
		link.setText(TextEditorMessages.TextEditorPreferencePage_colorsAndFonts_link);
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(getShell(), "org.eclipse.ui.preferencePages.ColorsAndFonts", null, null); //$NON-NLS-1$
			}
		});
		// TODO replace by link-specific tooltips when
		// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=88866 gets fixed
		link.setToolTipText(TextEditorMessages.TextEditorPreferencePage_colorsAndFonts_link_tooltip); 
		
		GridData gridData= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gridData.widthHint= 150; // only expand further if anyone else requires it
		gridData.horizontalSpan= 2;
		link.setLayoutData(gridData);
		
		addFiller(appearanceComposite, 2);
		
		appearanceComposite.layout();
		
		return appearanceComposite;
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		initializeDefaultColors();

		fOverlayStore.load();
		fOverlayStore.start();

		Control control= createAppearancePage(parent);

		initialize();
		Dialog.applyDialogFont(control);
		return control;
	}

	private void initialize() {

		initializeFields();

		for (int i= 0; i < fAppearanceColorListModel.length; i++)
			fAppearanceColorList.add(fAppearanceColorListModel[i][0]);
		fAppearanceColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fAppearanceColorList != null && !fAppearanceColorList.isDisposed()) {
					fAppearanceColorList.select(0);
					handleAppearanceColorListSelection();
				}
			}
		});
	}

	private void initializeFields() {
		for (Iterator it= fInitializers.iterator(); it.hasNext();) {
			Initializer initializer= (Initializer) it.next();
			initializer.initialize();
		}

		if (computeStateMask(fOverlayStore.getString(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER)) == -1) {
			// Fix possible illegal modifier string
			int stateMask= fOverlayStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER_MASK);
			if (stateMask == -1)
				fHyperlinkKeyModifierText.setText(""); //$NON-NLS-1$
			else
				fHyperlinkKeyModifierText.setText(getModifierString(stateMask));
		}

		fFieldsInitialized= true;
		updateStatus(new StatusInfo()); //$NON-NLS-1$

        // Update slaves
        Iterator iter= fMasterSlaveListeners.iterator();
        while (iter.hasNext()) {
            SelectionListener listener= (SelectionListener)iter.next();
            listener.widgetSelected(null);
        }

		fHyperlinkKeyModifierText.setEnabled(fHyperlinksEnabledCheckBox.getSelection());
	}

	private void initializeDefaultColors() {
		if (!getPreferenceStore().contains(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR)) {
			RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();
			PreferenceConverter.setDefault(fOverlayStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, rgb);
		}
		if (!getPreferenceStore().contains(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR)) {
			RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT).getRGB();
			PreferenceConverter.setDefault(fOverlayStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, rgb);
		}
		if (!getPreferenceStore().contains(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND)) {
			RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();
			PreferenceConverter.setDefault(fOverlayStore, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(), AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, rgb);
		}
		if (!getPreferenceStore().contains(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND)) {
			RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB();
			PreferenceConverter.setDefault(fOverlayStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, rgb);
			PreferenceConverter.setDefault(getPreferenceStore(), AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, rgb);
		}
	}

	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		fOverlayStore.setValue(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER_MASK, computeStateMask(fHyperlinkKeyModifierText.getText()));
		fOverlayStore.propagate();
		EditorsPlugin.getDefault().savePluginPreferences();
		return true;
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {

		fOverlayStore.loadDefaults();

		initializeFields();

		handleAppearanceColorListSelection();

		super.performDefaults();
	}

	/*
	 * @see DialogPage#dispose()
	 */
	public void dispose() {

		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore= null;
		}

		super.dispose();
	}

	private void addFiller(Composite composite, int horizontalSpan) {
		PixelConverter pixelConverter= new PixelConverter(composite);
		Label filler= new Label(composite, SWT.LEFT );
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= horizontalSpan;
		gd.heightHint= pixelConverter.convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}

	Button addCheckBox(Composite composite, final Preference preference, final Domain domain, int indentation) {
		final Button checkBox= new Button(composite, SWT.CHECK);
		checkBox.setText(preference.getName());
		checkBox.setToolTipText(preference.getDescription());

		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		gd.horizontalSpan= 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean value= checkBox.getSelection();
				IStatus status= domain.validate(Boolean.valueOf(value));
				if (!status.matches(IStatus.ERROR))
					fOverlayStore.setValue(preference.getKey(), value);
				updateStatus(status);
			}
		});

		fInitializers.add(fInitializerFactory.create(preference, checkBox));

		return checkBox;
	}

	Control[] addCombo(Composite composite, final Preference preference, final EnumeratedDomain domain, int indentation) {
		Label labelControl= new Label(composite, SWT.NONE);
		labelControl.setText(preference.getName());
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		labelControl.setLayoutData(gd);

		final Combo combo= new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		combo.setLayoutData(gd);
		combo.setToolTipText(preference.getDescription());
		for (Iterator it= domain.fItems.iterator(); it.hasNext();) {
			EnumValue value= (EnumValue) it.next();
			combo.add(value.getLabel());
		}

		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index= combo.getSelectionIndex();
				EnumValue value= domain.getValueByIndex(index);
				IStatus status= domain.validate(value);
				if (!status.matches(IStatus.ERROR))
					fOverlayStore.setValue(preference.getKey(), value.getIntValue());
				updateStatus(status);
			}
		});

		fInitializers.add(fInitializerFactory.create(preference, combo, domain));

		return new Control[] {labelControl, combo};
	}

	/**
	 * Adds a spinner for the given preference and domain. Assumes that the
	 * <code>EnumeratedDomain</code> contains only numeric values in a
	 * continuous range, no custom entries (use <code>addCombo</code> in that
	 * case).
	 *
	 * @param composite the parent composite
	 * @param preference the preference
	 * @param domain its domain
	 * @param indentation the indentation
	 * @return the created controls, a <code>Label</code> and a
	 *         <code>Spinner</code> control
	 */
	Control[] addSpinner(Composite composite, final Preference preference, final EnumeratedDomain domain, int indentation) {
		Label labelControl= new Label(composite, SWT.NONE);
		labelControl.setText(preference.getName());
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		labelControl.setLayoutData(gd);

		final Spinner spinner= new Spinner(composite, SWT.READ_ONLY | SWT.BORDER);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		spinner.setLayoutData(gd);
		spinner.setToolTipText(preference.getDescription());
		spinner.setMinimum(domain.getMinimumValue().getIntValue());
		spinner.setMaximum(domain.getMaximumValue().getIntValue());
		spinner.setIncrement(1);
		spinner.setPageIncrement(4);

		spinner.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index= spinner.getSelection();
				EnumValue value= domain.getValueByInteger(index);
				IStatus status= domain.validate(value);
				if (!status.matches(IStatus.ERROR))
					fOverlayStore.setValue(preference.getKey(), value.getIntValue());
				updateStatus(status);
			}
		});

		fInitializers.add(fInitializerFactory.create(preference, spinner, domain));

		return new Control[] {labelControl, spinner};
	}

	private Control[] addTextField(Composite composite, final Preference preference, final Domain domain, int textLimit, int indentation) {
		Label labelControl= new Label(composite, SWT.NONE);
		labelControl.setText(preference.getName());
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indentation;
		labelControl.setLayoutData(gd);

		final Text textControl= new Text(composite, SWT.BORDER | SWT.SINGLE);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint= convertWidthInCharsToPixels(textLimit + 1);
		textControl.setLayoutData(gd);
		textControl.setTextLimit(textLimit);
		textControl.setToolTipText(preference.getDescription());

		if (domain != null) {
			textControl.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String value= textControl.getText();
					IStatus status= domain.validate(value);
					if (!status.matches(IStatus.ERROR))
						fOverlayStore.setValue(preference.getKey(), value);
					updateStatus(status);
				}
			});
		}

		fInitializers.add(fInitializerFactory.create(preference, textControl));

		return new Control[] {labelControl, textControl};
	}

	private void createDependency(final Button master, Preference preference, final Control[] slaves) {
		indent(slaves[0]);

		boolean masterState= fOverlayStore.getBoolean(preference.getKey());
		for (int i= 0; i < slaves.length; i++) {
			slaves[i].setEnabled(masterState);
		}

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

	private static void indent(Control control) {
		GridData gridData= new GridData();
		gridData.horizontalIndent= 20;
		control.setLayoutData(gridData);
	}

	void updateStatus(IStatus status) {
		if (!fFieldsInitialized)
			return;
		status= StatusUtil.getMoreSevere(getHyperlinkKeyModifierStatus(), status);
		setValid(!status.matches(IStatus.ERROR));
		applyToStatusLine(this, status);
	}

	/**
	 * Applies the status to the status line of a dialog page.
	 *
	 * @param page the dialog page
	 * @param status the status
	 */
	public void applyToStatusLine(DialogPage page, IStatus status) {
		String message= status.getMessage();
		switch (status.getSeverity()) {
			case IStatus.OK:
				page.setMessage(message, IMessageProvider.NONE);
				page.setErrorMessage(null);
				break;
			case IStatus.WARNING:
				page.setMessage(message, IMessageProvider.WARNING);
				page.setErrorMessage(null);
				break;
			case IStatus.INFO:
				page.setMessage(message, IMessageProvider.INFORMATION);
				page.setErrorMessage(null);
				break;
			default:
				if (message.length() == 0) {
					message= null;
				}
				page.setMessage(null);
				page.setErrorMessage(message);
				break;
		}
	}

	private void handleHyperlinkKeyModifierModified() {
		String modifiers= fHyperlinkKeyModifierText.getText();
		fOverlayStore.setValue(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER, modifiers);

		int stateMask= computeStateMask(modifiers);

		if (fHyperlinksEnabledCheckBox.getSelection() && (stateMask == -1 || (stateMask & SWT.SHIFT) != 0)) {
			if (stateMask == -1)
				fHyperlinkKeyModifierStatus= new StatusInfo(IStatus.ERROR, NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_error_modifierIsNotValid, modifiers));
			else
				fHyperlinkKeyModifierStatus= new StatusInfo(IStatus.ERROR, TextEditorMessages.HyperlinkKeyModifier_error_shiftIsDisabled);
			setValid(false);
			StatusUtil.applyToStatusLine(this, fHyperlinkKeyModifierStatus);
		} else {
			fHyperlinkKeyModifierStatus= new StatusInfo();
			updateStatus(fHyperlinkKeyModifierStatus);
		}
	}

	private IStatus getHyperlinkKeyModifierStatus() {
		if (fHyperlinkKeyModifierStatus == null)
		fHyperlinkKeyModifierStatus= new StatusInfo();
		return fHyperlinkKeyModifierStatus;
	}

	/**
	 * Computes the state mask for the given modifier string.
	 *
	 * @param modifiers	the string with the modifiers, separated by '+', '-', ';', ',' or '.'
	 * @return the state mask or -1 if the input is invalid
	 */
	private static final int computeStateMask(String modifiers) {
		if (modifiers == null)
			return -1;

		if (modifiers.length() == 0)
			return SWT.NONE;

		int stateMask= 0;
		StringTokenizer modifierTokenizer= new StringTokenizer(modifiers, ",;.:+-* "); //$NON-NLS-1$
		while (modifierTokenizer.hasMoreTokens()) {
			int modifier= findLocalizedModifier(modifierTokenizer.nextToken());
			if (modifier == 0 || (stateMask & modifier) == modifier)
				return -1;
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
		if (modifierName == null)
			return 0;

		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.CTRL)))
			return SWT.CTRL;
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.SHIFT)))
			return SWT.SHIFT;
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.ALT)))
			return SWT.ALT;
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.COMMAND)))
			return SWT.COMMAND;

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
		String modifierString= ""; //$NON-NLS-1$
		if ((stateMask & SWT.CTRL) == SWT.CTRL)
			modifierString= appendModifierString(modifierString, SWT.CTRL);
		if ((stateMask & SWT.ALT) == SWT.ALT)
			modifierString= appendModifierString(modifierString, SWT.ALT);
		if ((stateMask & SWT.SHIFT) == SWT.SHIFT)
			modifierString= appendModifierString(modifierString, SWT.SHIFT);
		if ((stateMask & SWT.COMMAND) == SWT.COMMAND)
			modifierString= appendModifierString(modifierString,  SWT.COMMAND);

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
		if (modifierString == null)
			modifierString= ""; //$NON-NLS-1$
		String newModifierString= Action.findModifierString(modifier);
		if (modifierString.length() == 0)
			return newModifierString;
		return NLSUtility.format(TextEditorMessages.HyperlinkKeyModifier_concatModifierStrings, new String[] {modifierString, newModifierString});
	}
}
