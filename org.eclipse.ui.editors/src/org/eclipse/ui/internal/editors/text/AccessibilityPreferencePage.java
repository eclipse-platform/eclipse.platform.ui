/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.AccessibilityPreferencePage.EnumeratedDomain.EnumValue;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;


/**
 * The preference page for setting the editor options.
 * <p>
 * This class is internal and not intended to be used by clients.</p>
 *
 * @since 2.1
 */
public class AccessibilityPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {


	private abstract class Initializer {

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

		public Initializer create(Preference preference, Text control) {
			return new TextInitializer(preference, control);
		}

		public Initializer create(Preference preference, Button control) {
			return new CheckboxInitializer(preference, control);
		}

		public Initializer create(Preference preference, Combo control, EnumeratedDomain domain) {
			return new ComboInitializer(preference, control, domain);
		}
	}


	abstract static class Domain {
		public abstract IStatus validate(Object value);
		protected int parseInteger(Object val) throws NumberFormatException {
			if (val instanceof Integer)
				return ((Integer) val).intValue();

			if (val instanceof String)
				return Integer.parseInt((String) val);

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

	private OverlayPreferenceStore fOverlayStore;

	/**
	 * Tells whether the fields are initialized.
	 * @since 3.0
	 */
	private boolean fFieldsInitialized= false;

	private java.util.List fInitializers= new ArrayList();

	private InitializerFactory fInitializerFactory= new InitializerFactory();

	private Control fContents;
	private ArrayList fMasterSlaveListeners= new ArrayList();


	public AccessibilityPreferencePage() {
		setDescription(TextEditorMessages.AccessibilityPreferencePage_accessibility_title);
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());

		fOverlayStore= createOverlayStore();
	}


	protected Label createDescriptionLabel(Composite parent) {
		return null; // no description for new look
	}

	private OverlayPreferenceStore createOverlayStore() {

		ArrayList overlayKeys= new ArrayList();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WIDE_CARET));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.USE_SATURATED_COLORS_IN_OVERVIEW_RULER));

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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ITextEditorHelpContextIds.ACCESSIBILITY_PREFERENCE_PAGE);
	}

	private Control createAppearancePage(Composite parent) {


		Composite appearanceComposite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;

		appearanceComposite.setLayout(layout);

		String label= TextEditorMessages.TextEditorPreferencePage_accessibility_disableCustomCarets;
		Preference customCarets= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS, label, null);
		Button master= addCheckBox(appearanceComposite, customCarets, new BooleanDomain(), 0);

		label= TextEditorMessages.TextEditorPreferencePage_accessibility_wideCaret;
		Preference wideCaret= new Preference(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WIDE_CARET, label, null);
		Button slave= addCheckBox(appearanceComposite, wideCaret, new BooleanDomain(), 0);
		createDependency(master, customCarets, new Control[] { slave });

		label= TextEditorMessages.QuickDiffConfigurationBlock_characterMode;
		Preference quickDiffTextMode= new Preference(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE, label, null);
		addCheckBox(appearanceComposite, quickDiffTextMode, new BooleanDomain(), 0);

		label= TextEditorMessages.TextEditorPreferencePage_accessibility_useSaturatedColorsInOverviewRuler;
		Preference useSaturatedColors= new Preference(AbstractDecoratedTextEditorPreferenceConstants.USE_SATURATED_COLORS_IN_OVERVIEW_RULER, label, null);
		addCheckBox(appearanceComposite, useSaturatedColors, new BooleanDomain(), 0);

		return appearanceComposite;
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		fOverlayStore.load();
		fOverlayStore.start();

		fContents= createAppearancePage(parent);

		initialize();
		Dialog.applyDialogFont(fContents);
		return fContents;
	}

	private void initialize() {
		initializeFields();
	}

	private void initializeFields() {

		for (Iterator it= fInitializers.iterator(); it.hasNext();) {
			Initializer initializer= (Initializer) it.next();
			initializer.initialize();
		}

		fFieldsInitialized= true;

		updateStatus(new StatusInfo());

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

		for (Iterator iterator= fMasterSlaveListeners.iterator(); iterator.hasNext();) {
			SelectionListener listener= (SelectionListener)iterator.next();
			listener.widgetSelected(null);
		}

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



	private Button addCheckBox(Composite composite, final Preference preference, final Domain domain, int indentation) {
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
