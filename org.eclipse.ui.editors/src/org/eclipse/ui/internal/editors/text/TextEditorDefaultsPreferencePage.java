/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=22712
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.editors.text.TextEditorDefaultsPreferencePage.EnumeratedDomain.EnumValue;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;


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


	public static final class InitializerFactory {

		private final IPreferenceStore fPreferenceStore;

		public InitializerFactory(IPreferenceStore preferenceStore) {
			fPreferenceStore= preferenceStore;
		}

		private class TextInitializer extends Initializer {
			private final Text fText;

			public TextInitializer(Preference preference, Text control) {
				super(preference);
				fText= control;
			}
			public void initialize() {
				String value= fPreferenceStore.getString(fPreference.getKey());
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
				boolean value= fPreferenceStore.getBoolean(fPreference.getKey());
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
				int value= fPreferenceStore.getInt(fPreference.getKey());
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
				int value= fPreferenceStore.getInt(fPreference.getKey());
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

	private static class WhitespaceCharacterPainterOptionsDialog extends Dialog {

		private java.util.List fDialogInitializers= new ArrayList();

		private OverlayPreferenceStore fDialogOverlayStore;

		private final IPreferenceStore fParentPreferenceStore;

		private InitializerFactory fDialogInitializerFactory;

		private Text errorMessageText;

		protected WhitespaceCharacterPainterOptionsDialog(Shell parentShell, IPreferenceStore parent) {
			super(parentShell);
			fParentPreferenceStore= parent;
			fDialogOverlayStore= createDialogOverlayStore();
			fDialogInitializerFactory= new InitializerFactory(fDialogOverlayStore);
		}

		private OverlayPreferenceStore createDialogOverlayStore() {
			ArrayList overlayKeys= new ArrayList();

			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_LEADING_SPACES));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_ENCLOSED_SPACES));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TRAILING_SPACES));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_LEADING_IDEOGRAPHIC_SPACES));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TRAILING_IDEOGRAPHIC_SPACES));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_LEADING_TABS));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_ENCLOSED_TABS));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TRAILING_TABS));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_CARRIAGE_RETURN));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_LINE_FEED));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WHITESPACE_CHARACTER_ALPHA_VALUE));

			OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
			overlayKeys.toArray(keys);
			return new OverlayPreferenceStore(fParentPreferenceStore, keys);
		}

		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(TextEditorMessages.TextEditorDefaultsPreferencePage_showWhitespaceCharactersDialogTitle);
		}

		protected Control createContents(Composite parent) {
			Control contents= super.createContents(parent);
			Dialog.applyDialogFont(contents);
			fDialogOverlayStore.load();
			fDialogOverlayStore.start();
			initializeShowWhitespaceCharactersPreferences();
			return contents;
		}

		private void initializeShowWhitespaceCharactersPreferences() {
			for (Iterator it= fDialogInitializers.iterator(); it.hasNext();) {
				Initializer initializer= (Initializer)it.next();
				initializer.initialize();
			}
		}

		protected Control createDialogArea(Composite parent) {
			Composite composite= (Composite)super.createDialogArea(parent);

			Label description= new Label(composite, SWT.NONE);
			description.setText(TextEditorMessages.TextEditorDefaultsPreferencePage_configureWhitespaceCharacterPainterProperties);
			description.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

			Composite tabularComposite= new Composite(composite, SWT.NONE);
			GridLayout layout= new GridLayout();
			layout.numColumns= 4;
			layout.marginHeight= 0;
			layout.marginWidth= 0;
			layout.makeColumnsEqualWidth= true;
			tabularComposite.setLayout(layout);

			Label label;
			Button checkbox;
			Preference preference;

			label= new Label(tabularComposite, SWT.NONE);
			label.setText(""); //$NON-NLS-1$
			label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

			label= new Label(tabularComposite, SWT.NONE);
			label.setText(TextEditorMessages.TextEditorDefaultsPreferencePage_leading);
			label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

			label= new Label(tabularComposite, SWT.NONE);
			label.setText(TextEditorMessages.TextEditorDefaultsPreferencePage_enclosed);
			label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

			label= new Label(tabularComposite, SWT.NONE);
			label.setText(TextEditorMessages.TextEditorDefaultsPreferencePage_trailing);
			label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

			label= new Label(tabularComposite, SWT.NONE);
			label.setText(TextEditorMessages.TextEditorDefaultsPreferencePage_space);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			preference= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_LEADING_SPACES, "", null); //$NON-NLS-1$
			addCheckBox(tabularComposite, preference, new BooleanDomain(), 0);
			preference= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_ENCLOSED_SPACES, "", null); //$NON-NLS-1$
			addCheckBox(tabularComposite, preference, new BooleanDomain(), 0);
			preference= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TRAILING_SPACES, "", null); //$NON-NLS-1$
			addCheckBox(tabularComposite, preference, new BooleanDomain(), 0);

			label= new Label(tabularComposite, SWT.NONE);
			label.setText(TextEditorMessages.TextEditorDefaultsPreferencePage_ideographicSpace);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			preference= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_LEADING_IDEOGRAPHIC_SPACES, "", null); //$NON-NLS-1$
			addCheckBox(tabularComposite, preference, new BooleanDomain(), 0);
			preference= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES, "", null); //$NON-NLS-1$
			addCheckBox(tabularComposite, preference, new BooleanDomain(), 0);
			preference= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TRAILING_IDEOGRAPHIC_SPACES, "", null); //$NON-NLS-1$
			addCheckBox(tabularComposite, preference, new BooleanDomain(), 0);

			label= new Label(tabularComposite, SWT.NONE);
			label.setText(TextEditorMessages.TextEditorDefaultsPreferencePage_tab);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			preference= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_LEADING_TABS, "", null); //$NON-NLS-1$
			addCheckBox(tabularComposite, preference, new BooleanDomain(), 0);
			preference= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_ENCLOSED_TABS, "", null); //$NON-NLS-1$
			addCheckBox(tabularComposite, preference, new BooleanDomain(), 0);
			preference= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TRAILING_TABS, "", null); //$NON-NLS-1$
			addCheckBox(tabularComposite, preference, new BooleanDomain(), 0);

			label= new Label(tabularComposite, SWT.NONE);
			label.setText(TextEditorMessages.TextEditorDefaultsPreferencePage_carriageReturn);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			checkbox= new Button(tabularComposite, SWT.CHECK);
			checkbox.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			checkbox.setEnabled(false);
			checkbox= new Button(tabularComposite, SWT.CHECK);
			checkbox.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			checkbox.setEnabled(false);
			preference= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_CARRIAGE_RETURN, "", null); //$NON-NLS-1$
			addCheckBox(tabularComposite, preference, new BooleanDomain(), 0);

			label= new Label(tabularComposite, SWT.NONE);
			label.setText(TextEditorMessages.TextEditorDefaultsPreferencePage_lineFeed);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			checkbox= new Button(tabularComposite, SWT.CHECK);
			checkbox.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			checkbox.setEnabled(false);
			checkbox= new Button(tabularComposite, SWT.CHECK);
			checkbox.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			checkbox.setEnabled(false);
			preference= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_LINE_FEED, "", null); //$NON-NLS-1$
			addCheckBox(tabularComposite, preference, new BooleanDomain(), 0);

			Composite alphaComposite= new Composite(composite, SWT.NONE);
			layout= new GridLayout();
			layout.numColumns= 2;
			layout.marginHeight= 10;
			layout.marginWidth= 0;
			layout.makeColumnsEqualWidth= false;
			alphaComposite.setLayout(layout);
			preference= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WHITESPACE_CHARACTER_ALPHA_VALUE, TextEditorMessages.TextEditorDefaultsPreferencePage_transparencyLevel, null);
			addTextField(alphaComposite, preference, new IntegerDomain(0, 255), 5, 0);

			errorMessageText= new Text(composite, SWT.READ_ONLY | SWT.WRAP);
			errorMessageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			errorMessageText.setBackground(errorMessageText.getDisplay()
						.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			setErrorMessage(null);

			return composite;
		}

		/**
		 * Sets or clears the error message. If not <code>null</code>, the OK button is disabled.
		 * 
		 * @param errorMessage the error message, or <code>null</code> to clear
		 * @since 3.0
		 */
		public void setErrorMessage(String errorMessage) {
			if (errorMessageText != null && !errorMessageText.isDisposed()) {
				errorMessageText.setText(errorMessage == null ? "  " : errorMessage); //$NON-NLS-1$
				boolean hasError= errorMessage != null && (StringConverter.removeWhiteSpaces(errorMessage)).length() > 0;
				errorMessageText.setEnabled(hasError);
				errorMessageText.setVisible(hasError);
				errorMessageText.getParent().update();
				Control button= getButton(IDialogConstants.OK_ID);
				if (button != null) {
					button.setEnabled(errorMessage == null);
				}
			}
		}

		private Button addCheckBox(Composite composite, final Preference preference, final Domain domain, int indentation) {
			final Button checkBox= new Button(composite, SWT.CHECK);
			checkBox.setToolTipText(preference.getDescription());

			GridData gd= new GridData(SWT.CENTER, SWT.CENTER, false, false);
			gd.horizontalIndent= indentation;
			checkBox.setLayoutData(gd);
			checkBox.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					boolean value= checkBox.getSelection();
					IStatus status= domain.validate(Boolean.valueOf(value));
					if (!status.matches(IStatus.ERROR))
						fDialogOverlayStore.setValue(preference.getKey(), value);
				}
			});

			fDialogInitializers.add(fDialogInitializerFactory.create(preference, checkBox));
			return checkBox;
		}

		private Control[] addTextField(Composite composite, final Preference preference, final Domain domain, int textLimit, int indentation) {
			Label labelControl= new Label(composite, SWT.NONE);
			labelControl.setText(preference.getName());
			GridData gd= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			gd.horizontalIndent= indentation;
			labelControl.setLayoutData(gd);

			final Text textControl= new Text(composite, SWT.BORDER | SWT.SINGLE);
			gd= new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			gd.widthHint= convertWidthInCharsToPixels(textLimit + 1);
			textControl.setLayoutData(gd);
			textControl.setTextLimit(textLimit);
			textControl.setToolTipText(preference.getDescription());

			if (domain != null) {
				textControl.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						String value= textControl.getText();
						IStatus status= domain.validate(value);
						if (!status.matches(IStatus.ERROR)) {
							fDialogOverlayStore.setValue(preference.getKey(), value);
							setErrorMessage(null);
						}
						else {
							setErrorMessage(NLSUtility.format(TextEditorMessages.TextEditorDefaultsPreferencePage_showWhitespaceCharactersDialogInvalidInput, value));
						}
					}
				});
			}

			fDialogInitializers.add(fDialogInitializerFactory.create(preference, textControl));

			return new Control[] { labelControl, textControl };
		}

		protected void okPressed() {
			super.okPressed();
			fDialogOverlayStore.propagate();
		}
	}

	private final String[][] fAppearanceColorListModel= new String[][] {
		{TextEditorMessages.TextEditorPreferencePage_lineNumberForegroundColor, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR, null},
		{TextEditorMessages.TextEditorPreferencePage_currentLineHighlighColor, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR, null},
		{TextEditorMessages.TextEditorPreferencePage_printMarginColor, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR, null},
		{TextEditorMessages.TextEditorPreferencePage_findScopeColor, AbstractTextEditor.PREFERENCE_COLOR_FIND_SCOPE, null},
		{TextEditorMessages.TextEditorPreferencePage_selectionForegroundColor, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR},
		{TextEditorMessages.TextEditorPreferencePage_selectionBackgroundColor, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR},
		{TextEditorMessages.TextEditorPreferencePage_backgroundColor, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT},
		{TextEditorMessages.TextEditorPreferencePage_foregroundColor, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT},
		{TextEditorMessages.HyperlinkColor_label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_COLOR, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_COLOR_SYSTEM_DEFAULT},
	};

	private OverlayPreferenceStore fOverlayStore;

	private List fAppearanceColorList;
	private ColorSelector fAppearanceColorEditor;
	private Button fAppearanceColorDefault;

	/**
	 * Tells whether the fields are initialized.
	 */
	private boolean fFieldsInitialized= false;

	private ArrayList fMasterSlaveListeners= new ArrayList();

	private java.util.List fInitializers= new ArrayList();

	private InitializerFactory fInitializerFactory;

	private Map fDomains= new HashMap();


	public TextEditorDefaultsPreferencePage() {
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());

		fOverlayStore= createOverlayStore();
		fInitializerFactory= new InitializerFactory(fOverlayStore);
	}

	private OverlayPreferenceStore createOverlayStore() {

		ArrayList overlayKeys= new ArrayList();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractTextEditor.PREFERENCE_COLOR_FIND_SCOPE));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS));

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
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_COLOR_SYSTEM_DEFAULT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER_MASK));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_WHITESPACE_CHARACTERS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.SHOW_RANGE_INDICATOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WARN_IF_INPUT_DERIVED));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SMART_HOME_END));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TEXT_DRAG_AND_DROP_ENABLED));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HOVER_ENRICH_MODE));

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_LEADING_SPACES));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_ENCLOSED_SPACES));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TRAILING_SPACES));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_LEADING_IDEOGRAPHIC_SPACES));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TRAILING_IDEOGRAPHIC_SPACES));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_LEADING_TABS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_ENCLOSED_TABS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TRAILING_TABS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_CARRIAGE_RETURN));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_LINE_FEED));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WHITESPACE_CHARACTER_ALPHA_VALUE));

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
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;

		appearanceComposite.setLayout(layout);

		Link fontLink= new Link(appearanceComposite, SWT.NONE);
		fontLink.setText(TextEditorMessages.TextEditorPreferencePage_Font_link);
		fontLink.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(getShell(), "org.eclipse.ui.preferencePages.ColorsAndFonts", null, "selectFont:" + JFaceResources.TEXT_FONT); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		fontLink.setLayoutData(gd);

		addFiller(appearanceComposite, 2);

		String label= TextEditorMessages.TextEditorPreferencePage_undoHistorySize;
		Preference undoHistorySize= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNDO_HISTORY_SIZE, label, null);
		IntegerDomain undoHistorySizeDomain= new IntegerDomain(0, 99999);
		addTextField(appearanceComposite, undoHistorySize, undoHistorySizeDomain, 15, 0);

		label= TextEditorMessages.TextEditorPreferencePage_displayedTabWidth;
		Preference tabWidth= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, label, null);
		IntegerDomain tabWidthDomain= new IntegerDomain(1, 16);
		addTextField(appearanceComposite, tabWidth, tabWidthDomain, 15, 0);

		label= TextEditorMessages.TextEditorPreferencePage_convertTabsToSpaces;
		Preference spacesForTabs= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS, label, null);
		addCheckBox(appearanceComposite, spacesForTabs, new BooleanDomain(), 0);


		label= TextEditorMessages.TextEditorPreferencePage_highlightCurrentLine;
		Preference highlightCurrentLine= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, label, null);
		addCheckBox(appearanceComposite, highlightCurrentLine, new BooleanDomain(), 0);

		label= TextEditorMessages.TextEditorPreferencePage_showPrintMargin;
		Preference showPrintMargin= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, label, null);
		final Button showPrintMarginButton= addCheckBox(appearanceComposite, showPrintMargin, new BooleanDomain(), 0);


		label= TextEditorMessages.TextEditorPreferencePage_printMarginColumn;
		Preference printMarginColumn= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, label, null);
		final IntegerDomain printMarginDomain= new IntegerDomain(20, 200);
		final Control[] printMarginControls= addTextField(appearanceComposite, printMarginColumn, printMarginDomain, 15, 20);
		createDependency(showPrintMarginButton, showPrintMargin, printMarginControls);

		showPrintMarginButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateStatus(printMarginDomain);
			}
		});

		label= TextEditorMessages.TextEditorPreferencePage_showLineNumbers;
		Preference showLineNumbers= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, label, null);
		addCheckBox(appearanceComposite, showLineNumbers, new BooleanDomain(), 0);

		label= TextEditorMessages.TextEditorDefaultsPreferencePage_range_indicator;
		Preference showMagnet= new Preference(AbstractDecoratedTextEditorPreferenceConstants.SHOW_RANGE_INDICATOR, label, null);
		addCheckBox(appearanceComposite, showMagnet, new BooleanDomain(), 0);

		label= TextEditorMessages.TextEditorDefaultsPreferencePage_showWhitespaceCharacters;
		String linkText= TextEditorMessages.TextEditorDefaultsPreferencePage_showWhitespaceCharactersLinkText;
		Preference showWhitespaceCharacters= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_WHITESPACE_CHARACTERS, label, null);
		addCheckBoxWithLink(appearanceComposite, showWhitespaceCharacters, linkText, new BooleanDomain(), 0, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Dialog dialog= new WhitespaceCharacterPainterOptionsDialog(Display.getDefault().getActiveShell(), fOverlayStore);
				dialog.open();
			}
		});

		label= TextEditorMessages.TextEditorPreferencePage_showAffordance;
		Preference showAffordance= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE, label, null);
		addCheckBox(appearanceComposite, showAffordance, new BooleanDomain(), 0);

		label= TextEditorMessages.TextEditorDefaultsPreferencePage_enrichHoverMode;
		Preference hoverReplace= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HOVER_ENRICH_MODE, label, null);
		EnumeratedDomain hoverReplaceDomain= new EnumeratedDomain();
		hoverReplaceDomain.addValue(new EnumValue(-1, TextEditorMessages.TextEditorDefaultsPreferencePage_enrichHover_disabled));
		hoverReplaceDomain.addValue(new EnumValue(1, TextEditorMessages.TextEditorDefaultsPreferencePage_enrichHover_immediately));
		hoverReplaceDomain.addValue(new EnumValue(0, TextEditorMessages.TextEditorDefaultsPreferencePage_enrichHover_afterDelay));
		hoverReplaceDomain.addValue(new EnumValue(2, TextEditorMessages.TextEditorDefaultsPreferencePage_enrichHover_onClick));
		addCombo(appearanceComposite, hoverReplace, hoverReplaceDomain, 0);

		label= TextEditorMessages.TextEditorDefaultsPreferencePage_textDragAndDrop;
		Preference textDragAndDrop= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TEXT_DRAG_AND_DROP_ENABLED, label, null);
		addCheckBox(appearanceComposite, textDragAndDrop, new BooleanDomain(), 0);

		label= TextEditorMessages.TextEditorDefaultsPreferencePage_warn_if_derived;
		Preference warnIfDerived= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WARN_IF_INPUT_DERIVED, label, null);
		addCheckBox(appearanceComposite, warnIfDerived, new BooleanDomain(), 0);

		label= TextEditorMessages.TextEditorDefaultsPreferencePage_smartHomeEnd;
		Preference smartHomeEnd= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SMART_HOME_END, label, null);
		addCheckBox(appearanceComposite, smartHomeEnd, new BooleanDomain(), 0);

		addFiller(appearanceComposite, 2);

		Label l= new Label(appearanceComposite, SWT.LEFT);
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
				PreferencesUtil.createPreferenceDialogOn(getShell(), "org.eclipse.ui.preferencePages.ColorsAndFonts", null, "selectCategory:org.eclipse.ui.workbenchMisc"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

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

		fFieldsInitialized= true;
		updateStatus(new StatusInfo());

        // Update slaves
        Iterator iter= fMasterSlaveListeners.iterator();
        while (iter.hasNext()) {
            SelectionListener listener= (SelectionListener)iter.next();
            listener.widgetSelected(null);
        }

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
		fOverlayStore.propagate();
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

	private void checkboxControlChanged(final Preference preference, final Domain domain, final Button checkBox) {
		boolean value= checkBox.getSelection();
		IStatus status= domain.validate(Boolean.valueOf(value));
		if (!status.matches(IStatus.ERROR))
			fOverlayStore.setValue(preference.getKey(), value);
		updateStatus(status);
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
				checkboxControlChanged(preference, domain, checkBox);
			}
		});

		fInitializers.add(fInitializerFactory.create(preference, checkBox));

		return checkBox;
	}

	private Button addCheckBoxWithLink(Composite parent, final Preference preference, String linkText, final Domain domain, int indentation, final SelectionListener listener) {
		GridData gd= new GridData(GridData.FILL, GridData.FILL, true, false);
		gd.horizontalSpan= 3;
		gd.horizontalIndent= indentation;

		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.horizontalSpacing= 0;
		layout.numColumns= 2;
		composite.setLayout(layout);
		composite.setLayoutData(gd);

		final Button checkBox= new Button(composite, SWT.CHECK);
		checkBox.setFont(JFaceResources.getDialogFont());
		checkBox.setText(preference.getName());
		gd= new GridData(GridData.FILL, GridData.CENTER, false, false);
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkboxControlChanged(preference, domain, checkBox);
			}
		});

		gd= new GridData(GridData.FILL, GridData.CENTER, false, false);
		Link link= new Link(composite, SWT.NONE);
		link.setText(linkText);
		link.setLayoutData(gd);
		if (listener != null) {
			link.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					listener.widgetSelected(e);
				}
			});
		}

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
					updateStatus(domain);
				}
			});
		}

		fInitializers.add(fInitializerFactory.create(preference, textControl));

		fDomains.put(domain, textControl);

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
		setValid(!status.matches(IStatus.ERROR));
		applyToStatusLine(this, status);
	}

	void updateStatus(Domain checkedDomain) {
		if (!fFieldsInitialized)
			return;

		if (updateStatusOnError(checkedDomain))
			return;

		Iterator iter= fDomains.keySet().iterator();
		while (iter.hasNext()) {
			Domain domain= (Domain)iter.next();
			if (domain.equals(checkedDomain))
				continue;
			if (updateStatusOnError(domain))
				return;
		}
		updateStatus(new StatusInfo());
	}

	private boolean updateStatusOnError(Domain domain) {
		Text textWidget= (Text)fDomains.get(domain);
		if (textWidget.isEnabled()) {
			IStatus status= domain.validate(textWidget.getText());
			if (status.matches(IStatus.ERROR)) {
				updateStatus(status);
				return true;
			}
		}
		return false;
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

}
