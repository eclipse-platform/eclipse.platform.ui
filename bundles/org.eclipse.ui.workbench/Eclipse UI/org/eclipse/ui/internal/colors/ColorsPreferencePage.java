/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.colors;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for management of system colors defined in the 
 * <code>org.eclipse.ui.colorDefinitions</code> extension point.
 * 
 * @since 3.0
 */
public class ColorsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * The translation bundle in which to look up internationalized text.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(ColorsPreferencePage.class.getName());
	private ListViewer colorList;

	private ColorSelector colorSelector;
	private Text commentText;
	private Text descriptionText;

	/**
	 * Map of defintion id->RGB objects that map to changes expressed in this
	 * UI session.  These changes should be made in preferences and the 
	 * registry.
	 */
	private Map preferencesToSet = new HashMap(7);
	private Button resetButton;

	/**
	 * Map of defintion id->RGB objects that map to changes expressed in this
	 * UI session.  These changes should be made in the registry.
	 */
	private Map valuesToSet = new HashMap(7);

	/**
	 * Create a new instance of the receiver. 
	 */
	public ColorsPreferencePage() {
		//no-op
	}

	/**
	 * Create the color selection control.
	 * 
	 * @param parent the parent <code>Composite</code>.
	 */
	private void createColorControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.LEFT);
		label.setText(RESOURCE_BUNDLE.getString("ColorsPreferencePage.value")); //$NON-NLS-1$
		Dialog.applyDialogFont(label);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		colorSelector = new ColorSelector(composite);
		colorSelector.getButton().setLayoutData(new GridData());
		colorSelector.setEnabled(false);

		resetButton = new Button(composite, SWT.PUSH);
		resetButton.setText(RESOURCE_BUNDLE.getString("ColorsPreferencePage.reset")); //$NON-NLS-1$
		resetButton.setEnabled(false);
	}

	/**
	 * Create the <code>ListViewer</code> that will contain all color 
	 * definitions as defined in the extension point.
	 * 
	 * @param parent the parent <code>Composite</code>.
	 */
	private void createColorList(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.LEFT);
		label.setText(RESOURCE_BUNDLE.getString("ColorsPreferencePage.colors")); //$NON-NLS-1$
		Dialog.applyDialogFont(label);

		colorList =
			new ListViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		colorList.setContentProvider(new ArrayContentProvider());
		colorList.setInput(ColorDefinition.getDefinitions());
		colorList.setLabelProvider(new LabelProvider() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return ((ColorDefinition) element).getLabel();
			}
		});

		colorList.getControl().setFont(JFaceResources.getViewerFont());
		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		colorList.getControl().setLayoutData(data);
	}

	/**
	 * Create the text box that will contain the current colors comment text 
	 * (if any).  This includes whether the color is set to its default value or
	 * and what that might be in the case of a mapping.
	 * 
	 * @param parent the parent <code>Composite</code>.
	 */
	private void createCommentControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.LEFT);
		label.setText(RESOURCE_BUNDLE.getString("ColorsPreferencePage.comment")); //$NON-NLS-1$
		Dialog.applyDialogFont(label);

		commentText = new Text(composite, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
		commentText.setLayoutData(new GridData(GridData.FILL_BOTH));

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite mainColumn = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		mainColumn.setFont(parent.getFont());
		mainColumn.setLayout(layout);

		createColorList(mainColumn);
		Composite controlColumn = new Composite(mainColumn, SWT.NONE);
		controlColumn.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		controlColumn.setLayout(layout);

		createColorControl(controlColumn);
		createCommentControl(controlColumn);

		createDescriptionControl(mainColumn);

		hookListeners();

		return mainColumn;
	}

	/**
	 * Create the text box that will contain the current colors description 
	 * text (if any).
	 * 
	 * @param parent the parent <code>Composite</code>.
	 */
	private void createDescriptionControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		composite.setLayoutData(data);

		Label label = new Label(composite, SWT.LEFT);
		label.setText(RESOURCE_BUNDLE.getString("ColorsPreferencePage.description")); //$NON-NLS-1$
		Dialog.applyDialogFont(label);

		descriptionText = new Text(composite, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
		data = new GridData(GridData.FILL_BOTH);
		descriptionText.setLayoutData(data);
	}

	/**
	 * Get the ancestor of the given color, if any.
	 * 
	 * @param definition the descendant <code>ColorDefinition</code>.
	 * @return the ancestror <code>ColorDefinition</code>, or <code>null</code> 
	 * 		if none.
	 */
	private ColorDefinition getAncestor(ColorDefinition definition) {
		String defaultsTo = definition.getDefaultsTo();
		if (defaultsTo == null)
			return null;

		int idx =
			Arrays.binarySearch(
				ColorDefinition.getDefinitions(),
				defaultsTo,
				ColorDefinition.ID_COMPARATOR);
		if (idx < 0)
			return null;
		return ColorDefinition.getDefinitions()[idx];
	}

	/**
	 * Get the RGB value of the given colors ancestor, if any.
	 * 
	 * @param definition the descendant <code>ColorDefinition</code>.
	 * @return the ancestror <code>RGB</code>, or <code>null</code> if none.
	 */
	private RGB getAncestorValue(ColorDefinition definition) {
		ColorDefinition ancestor = getAncestor(definition);
		if (ancestor == null)
			return null;

		return getValue(ancestor);
	}

	/**
	 * Get colors that descend from the provided color.
	 * 
	 * @param definition the ancestor <code>ColorDefinition</code>.
	 * @return the ColorDefinitions that have the provided definition as their 
	 * 		defaultsTo attribute.
	 */
	private ColorDefinition[] getDescendantColors(ColorDefinition definition) {
		List list = new ArrayList(5);
		String id = definition.getId();

		ColorDefinition[] sorted = new ColorDefinition[ColorDefinition.getDefinitions().length];
		System.arraycopy(ColorDefinition.getDefinitions(), 0, sorted, 0, sorted.length);

		Arrays.sort(sorted, ColorDefinition.HIERARCHY_COMPARATOR);

		for (int i = 0; i < sorted.length; i++) {
			if (id.equals(sorted[i].getDefaultsTo()))
				list.add(sorted[i]);
		}

		return (ColorDefinition[]) list.toArray(new ColorDefinition[list.size()]);
	}

	/**
	 * Get the RGB value for the specified definition.  Cascades through 
	 * preferenceToSet, valuesToSet and finally the registry.
	 * 
	 * @param definition the <code>ColorDefinition</code>.
	 * @return the <code>RGB</code> value.
	 */
	private RGB getValue(ColorDefinition definition) {
		String id = definition.getId();
		RGB updatedRGB = (RGB) preferencesToSet.get(id);
		if (updatedRGB == null) {
			updatedRGB = (RGB) valuesToSet.get(id);
			if (updatedRGB == null)
				updatedRGB = JFaceResources.getColorRegistry().getRGB(id);
		}
		return updatedRGB;
	}

	/**
	 * Hook all control listeners.
	 */
	private void hookListeners() {
		colorSelector.addListener(new IPropertyChangeListener() {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				ColorDefinition definition =
					(ColorDefinition) ((IStructuredSelection) colorList.getSelection())
						.getFirstElement();

				RGB newRGB = (RGB) event.getNewValue();
				if (definition != null && newRGB != null && !newRGB.equals(event.getOldValue())) {
					setPreferenceValue(definition, newRGB);
				}

				updateControls(definition);
			}
		});

		colorList.addSelectionChangedListener(new ISelectionChangedListener() {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty()) {
					updateControls(null);
				} else {
					updateControls(
						(ColorDefinition) ((IStructuredSelection) event.getSelection())
							.getFirstElement());
				}
			}
		});

		resetButton.addSelectionListener(new SelectionAdapter() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				ColorDefinition definition =
					(ColorDefinition) ((IStructuredSelection) colorList.getSelection())
						.getFirstElement();
				if (resetColor(definition))
					updateControls(definition);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		setPreferenceStore(workbench.getPreferenceStore());
	}

	/**
	 * Answers whether the definition is currently set to the default value.
	 * 
	 * @param definition the <code>ColorDefinition</code> to check.
	 * @return Return whether the definition is currently mapped to the default 
	 * 		value, either in the preference store or in the local change record 
	 * 		of this preference page.
	 */
	private boolean isDefault(ColorDefinition definition) {
		String id = definition.getId();

		if (preferencesToSet.containsKey(id)) {
			if (definition.getValue() != null) { // value-based color
				if (preferencesToSet
					.get(id)
					.equals(StringConverter.asRGB(getPreferenceStore().getDefaultString(id), null)))
					return true;
			} else {
				if (preferencesToSet.get(id).equals(getAncestorValue(definition)))
					return true;
			}
		} else {
			if (definition.getValue() != null) { // value-based color
				if (getPreferenceStore().isDefault(id))
					return true;
			} else {
				// a descendant is default if it's the same value as its ancestor
				if (getValue(definition).equals(getAncestorValue(definition)))
					return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		ColorDefinition[] definitions = ColorDefinition.getDefinitions();

		// apply defaults in depth-order.
		ColorDefinition[] definitionsCopy = new ColorDefinition[definitions.length];
		System.arraycopy(definitions, 0, definitionsCopy, 0, definitions.length);

		Arrays.sort(definitionsCopy, ColorDefinition.HIERARCHY_COMPARATOR);

		for (int i = 0; i < definitionsCopy.length; i++)
			resetColor(definitionsCopy[i]);

		updateControls(
			(ColorDefinition) ((IStructuredSelection) colorList.getSelection()).getFirstElement());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		for (Iterator i = preferencesToSet.keySet().iterator(); i.hasNext();) {
			String id = (String) i.next();
			RGB rgb = (RGB) preferencesToSet.get(id);
			String rgbString = StringConverter.asString(rgb);
			String storeString = getPreferenceStore().getString(id);

			if (!rgbString.equals(storeString)) {
				JFaceResources.getColorRegistry().put(id, rgb);
				getPreferenceStore().setValue(id, rgbString);
			}
		}

		preferencesToSet.clear();

		for (Iterator i = valuesToSet.keySet().iterator(); i.hasNext();) {
			String id = (String) i.next();
			RGB rgb = (RGB) valuesToSet.get(id);

			JFaceResources.getColorRegistry().put(id, rgb);
		}

		valuesToSet.clear();

		return true;
	}

	/**
	 * Resets the supplied definition to its default value.
	 * 
	 * @param definition the <code>ColorDefinition</code> to reset.
	 * @return whether any change was made.
	 */
	private boolean resetColor(ColorDefinition definition) {
		if (!isDefault(definition)) {

			RGB newRGB;
			if (definition.getValue() != null) {
				newRGB =
					StringConverter.asRGB(
						getPreferenceStore().getDefaultString(definition.getId()),
						null);
			} else {
				newRGB = getAncestorValue(definition);
			}

			if (newRGB != null) {
				setPreferenceValue(definition, newRGB);
				return true;
			}
		}
		return false;
	}

	/**
	 * Set the value (in registry) for the given colors children.  
	 * 
	 * @param definition the <code>ColorDefinition</code> whos children should 
	 * 		be set.
	 * @param newRGB the new <code>RGB</code> value for the definitions 
	 * 		identifier.
	 */
	private void setDescendantRegistryValues(ColorDefinition definition, RGB newRGB) {
		ColorDefinition[] children = getDescendantColors(definition);

		for (int i = 0; i < children.length; i++) {
			if (isDefault(children[i])) {
				valuesToSet.put(children[i].getId(), newRGB);
				setDescendantRegistryValues(children[i], newRGB);
			}
		}
	}

	/**
	 * Set the value (in preferences) for the given color.  
	 * 
	 * @param definition the <code>ColorDefinition</code> to set.
	 * @param newRGB the new <code>RGB</code> value for the definitions 
	 * 		identifier.
	 */
	protected void setPreferenceValue(ColorDefinition definition, RGB newRGB) {
		setDescendantRegistryValues(definition, newRGB);
		preferencesToSet.put(definition.getId(), newRGB);
	}

	/**
	 * Update the color controls based on the supplied definition.
	 * 
	 * @param definition The currently selected <code>ColorDefinition</code>.
	 */
	private void updateControls(ColorDefinition definition) {
		if (definition != null)
			colorSelector.setColorValue(getValue(definition));

		if (definition != null) {
			resetButton.setEnabled(true);
			colorSelector.setEnabled(true);
			if (isDefault(definition)) {
				if (definition.getDefaultsTo() != null) {
					int idx =
						Arrays.binarySearch(
							ColorDefinition.getDefinitions(),
							definition.getDefaultsTo(),
							ColorDefinition.ID_COMPARATOR);

					if (idx >= 0) {
						commentText.setText(MessageFormat.format(RESOURCE_BUNDLE.getString("ColorsPreferencePage.currentlyMappedTo"), //$NON-NLS-1$
						new Object[] { ColorDefinition.getDefinitions()[idx].getLabel()}));
					} else
						commentText.setText(""); //$NON-NLS-1$
				} else
					commentText.setText(RESOURCE_BUNDLE.getString("ColorsPreferencePage.currentlyDefault")); //$NON-NLS-1$
			} else
				commentText.setText(RESOURCE_BUNDLE.getString("ColorsPreferencePage.customValue")); //$NON-NLS-1$

			String description = definition.getDescription();
			descriptionText.setText(description == null ? "" : description); //$NON-NLS-1$
		} else {
			resetButton.setEnabled(false);
			colorSelector.setEnabled(false);
			commentText.setText(""); //$NON-NLS-1$
			descriptionText.setText(""); //$NON-NLS-1$
		}
	}
}
