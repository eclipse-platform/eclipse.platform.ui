/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.io.IOException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.commands.IActivityBinding;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;

import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.keys.KeySequenceText;
import org.eclipse.ui.internal.util.Util;

public class KeysPreferencePage
	extends org.eclipse.jface.preference.PreferencePage
	implements IWorkbenchPreferencePage {

	private final static class CommandAssignment implements Comparable {
		private String activityId;

		private KeySequenceBindingNode.Assignment assignment;
		private KeySequence keySequence;

		public int compareTo(Object object) {
			CommandAssignment castedObject = (CommandAssignment) object;
			int compareTo = Util.compare(activityId, castedObject.activityId);

			if (compareTo == 0) {
				compareTo = Util.compare(keySequence, castedObject.keySequence);

				if (compareTo == 0)
					compareTo = Util.compare(assignment, castedObject.assignment);
			}

			return compareTo;
		}

		public boolean equals(Object object) {
			if (!(object instanceof CommandAssignment))
				return false;

			CommandAssignment castedObject = (CommandAssignment) object;
			boolean equals = true;
			equals &= Util.equals(assignment, castedObject.assignment);
			equals &= Util.equals(activityId, castedObject.activityId);
			equals &= Util.equals(keySequence, castedObject.keySequence);
			return equals;
		}
	}

	private final static class KeySequenceAssignment implements Comparable {
		private String activityId;

		private KeySequenceBindingNode.Assignment assignment;

		public int compareTo(Object object) {
			KeySequenceAssignment castedObject = (KeySequenceAssignment) object;
			int compareTo = Util.compare(activityId, castedObject.activityId);

			if (compareTo == 0)
				compareTo = Util.compare(assignment, castedObject.assignment);

			return compareTo;
		}

		public boolean equals(Object object) {
			if (!(object instanceof CommandAssignment))
				return false;

			KeySequenceAssignment castedObject = (KeySequenceAssignment) object;
			boolean equals = true;
			equals &= Util.equals(assignment, castedObject.assignment);
			equals &= Util.equals(activityId, castedObject.activityId);
			return equals;
		}
	}

	private final static int DIFFERENCE_ADD = 0;
	private final static int DIFFERENCE_CHANGE = 1;
	private final static int DIFFERENCE_MINUS = 2;
	private final static int DIFFERENCE_NONE = 3;
	private final static Image IMAGE_BLANK = ImageFactory.getImage("blank"); //$NON-NLS-1$
	private final static Image IMAGE_CHANGE = ImageFactory.getImage("change"); //$NON-NLS-1$
	private final static Image IMAGE_MINUS = ImageFactory.getImage("minus"); //$NON-NLS-1$
	private final static Image IMAGE_PLUS = ImageFactory.getImage("plus"); //$NON-NLS-1$
	private final static ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(KeysPreferencePage.class.getName());
	private final static RGB RGB_MINUS = new RGB(160, 160, 160);

	private Map activityIdsByCommandId;
	private Map activityIdsByUniqueName;
	private IActivityManager activityManager;
	private Map activityUniqueNamesById;
	private Map assignmentsByActivityIdByKeySequence;
	private Button buttonAdd;
	private Button buttonAddKey;
	private Button buttonRemove;
	private Button buttonRestore;
	private Map categoryIdsByUniqueName;
	private Map categoryUniqueNamesById;
	private Button checkBoxMultiKeyAssist;
	private Button checkBoxMultiKeyRocker;
	private Combo comboActivity;
	private Combo comboCategory;
	private Combo comboCommand;
	private Combo comboKeyConfiguration;
	private Set commandAssignments;
	private Map commandIdsByCategoryId;
	private Map commandIdsByUniqueName;
	private CommandManager commandManager;
	private Map commandUniqueNamesById;
	private Group groupCommand;
	private Group groupKeySequence;
	private Map keyConfigurationIdsByUniqueName;
	private Map keyConfigurationUniqueNamesById;
	private Set keySequenceAssignments;
	private Label labelActivity;
	private Label labelActivityExtends;
	private Label labelAssignmentsForCommand;
	private Label labelAssignmentsForKeySequence;
	private Label labelCategory;
	private Label labelCommand;
	private Label labelKeyConfiguration;
	private Label labelKeyConfigurationExtends;
	private Label labelKeySequence;
	private Menu menuButtonAddKey;
	private Table tableAssignmentsForCommand;
	private Table tableAssignmentsForKeySequence;
	private Text textKeySequence;
	private KeySequenceText textKeySequenceManager;
	private IntegerFieldEditor textMultiKeyAssistTime;
	private SortedMap tree;
	private IWorkbench workbench;

	private void buildCommandAssignmentsTable() {
		tableAssignmentsForCommand.removeAll();

		for (Iterator iterator = commandAssignments.iterator(); iterator.hasNext();) {
			CommandAssignment commandAssignment = (CommandAssignment) iterator.next();
			KeySequenceBindingNode.Assignment assignment = commandAssignment.assignment;
			KeySequence keySequence = commandAssignment.keySequence;
			String commandString = null;
			int difference = DIFFERENCE_NONE;

			if (assignment.hasPreferenceCommandIdInFirstKeyConfiguration
				|| assignment.hasPreferenceCommandIdInInheritedKeyConfiguration) {
				String preferenceCommandId;

				if (assignment.hasPreferenceCommandIdInFirstKeyConfiguration)
					preferenceCommandId = assignment.preferenceCommandIdInFirstKeyConfiguration;
				else
					preferenceCommandId = assignment.preferenceCommandIdInInheritedKeyConfiguration;

				if (assignment.hasPluginCommandIdInFirstKeyConfiguration
					|| assignment.hasPluginCommandIdInInheritedKeyConfiguration) {
					String pluginCommandId;

					if (assignment.hasPluginCommandIdInFirstKeyConfiguration)
						pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
					else
						pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;
					if (preferenceCommandId != null) {
						difference = DIFFERENCE_CHANGE;
						commandString =
						/* commandUniqueNamesById.get(preferenceCommandId) */
						keySequence.format() + ""; //$NON-NLS-1$
					} else {
						difference = DIFFERENCE_MINUS;
						commandString = /* "Unassigned" */
						keySequence.format();
					}

					if (pluginCommandId != null)
						commandString += " (was: " + commandUniqueNamesById.get(pluginCommandId) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					else
						commandString += " (was: " + "Unassigned" + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					if (preferenceCommandId != null) {
						difference = DIFFERENCE_ADD;
						commandString =
						/* commandUniqueNamesById.get(preferenceCommandId) */
						keySequence.format() + ""; //$NON-NLS-1$
					} else {
						difference = DIFFERENCE_MINUS;
						commandString = /* "Unassigned" */
						keySequence.format();
					}
				}
			} else {
				String pluginCommandId;
				if (assignment.hasPluginCommandIdInFirstKeyConfiguration)
					pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
				else
					pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;

				if (pluginCommandId != null) {
					difference = DIFFERENCE_NONE;
					commandString =
					/* commandUniqueNamesById.get(preferenceCommandId) */
					keySequence.format() + ""; //$NON-NLS-1$
				} else {
					difference = DIFFERENCE_MINUS;
					commandString = /* "Unassigned" */
					keySequence.format();
				}
			}

			TableItem tableItem = new TableItem(tableAssignmentsForCommand, SWT.NULL);

			switch (difference) {
				case DIFFERENCE_ADD :
					tableItem.setImage(0, IMAGE_PLUS);
					break;

				case DIFFERENCE_CHANGE :
					tableItem.setImage(0, IMAGE_CHANGE);
					break;

				case DIFFERENCE_MINUS :
					tableItem.setImage(0, IMAGE_MINUS);
					break;

				case DIFFERENCE_NONE :
					tableItem.setImage(0, IMAGE_BLANK);
					break;
			}

			String activityId = commandAssignment.activityId;

			if (activityId == null)
				tableItem.setText(1, Util.translateString(RESOURCE_BUNDLE, "general")); //$NON-NLS-1$
			else
				tableItem.setText(1, (String) activityUniqueNamesById.get(activityId)); //$NON-NLS-1$

			tableItem.setText(2, commandString);

			if (difference == DIFFERENCE_MINUS)
				tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS));
		}
	}

	private void buildKeySequenceAssignmentsTable() {
		tableAssignmentsForKeySequence.removeAll();

		for (Iterator iterator = keySequenceAssignments.iterator(); iterator.hasNext();) {
			KeySequenceAssignment keySequenceAssignment = (KeySequenceAssignment) iterator.next();
			KeySequenceBindingNode.Assignment assignment = keySequenceAssignment.assignment;
			String commandString = null;
			int difference = DIFFERENCE_NONE;

			if (assignment.hasPreferenceCommandIdInFirstKeyConfiguration
				|| assignment.hasPreferenceCommandIdInInheritedKeyConfiguration) {
				String preferenceCommandId;

				if (assignment.hasPreferenceCommandIdInFirstKeyConfiguration)
					preferenceCommandId = assignment.preferenceCommandIdInFirstKeyConfiguration;
				else
					preferenceCommandId = assignment.preferenceCommandIdInInheritedKeyConfiguration;

				if (assignment.hasPluginCommandIdInFirstKeyConfiguration
					|| assignment.hasPluginCommandIdInInheritedKeyConfiguration) {
					String pluginCommandId;

					if (assignment.hasPluginCommandIdInFirstKeyConfiguration)
						pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
					else
						pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;

					if (preferenceCommandId != null) {
						difference = DIFFERENCE_CHANGE;
						commandString = commandUniqueNamesById.get(preferenceCommandId) + ""; //$NON-NLS-1$
					} else {
						difference = DIFFERENCE_MINUS;
						commandString = "Unassigned"; //$NON-NLS-1$
					}

					if (pluginCommandId != null)
						commandString += " (was: " + commandUniqueNamesById.get(pluginCommandId) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					else
						commandString += " (was: " + "Unassigned" + ")"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				} else {
					if (preferenceCommandId != null) {
						difference = DIFFERENCE_ADD;
						commandString = commandUniqueNamesById.get(preferenceCommandId) + ""; //$NON-NLS-1$
					} else {
						difference = DIFFERENCE_MINUS;
						commandString = "Unassigned"; //$NON-NLS-1$
					}
				}
			} else {
				String pluginCommandId;

				if (assignment.hasPluginCommandIdInFirstKeyConfiguration)
					pluginCommandId = assignment.pluginCommandIdInFirstKeyConfiguration;
				else
					pluginCommandId = assignment.pluginCommandIdInInheritedKeyConfiguration;

				if (pluginCommandId != null) {
					difference = DIFFERENCE_NONE;
					commandString = commandUniqueNamesById.get(pluginCommandId) + ""; //$NON-NLS-1$
				} else {
					difference = DIFFERENCE_MINUS;
					commandString = "Unassigned"; //$NON-NLS-1$
				}
			}

			TableItem tableItem = new TableItem(tableAssignmentsForKeySequence, SWT.NULL);

			switch (difference) {
				case DIFFERENCE_ADD :
					tableItem.setImage(0, IMAGE_PLUS);
					break;

				case DIFFERENCE_CHANGE :
					tableItem.setImage(0, IMAGE_CHANGE);
					break;

				case DIFFERENCE_MINUS :
					tableItem.setImage(0, IMAGE_MINUS);
					break;

				case DIFFERENCE_NONE :
					tableItem.setImage(0, IMAGE_BLANK);
					break;
			}

			String activityId = keySequenceAssignment.activityId;

			if (activityId == null)
				tableItem.setText(1, Util.translateString(RESOURCE_BUNDLE, "general")); //$NON-NLS-1$
			else
				tableItem.setText(1, (String) activityUniqueNamesById.get(activityId)); //$NON-NLS-1$	

			tableItem.setText(2, commandString);

			if (difference == DIFFERENCE_MINUS)
				tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS));
		}
	}

	private Composite createAdvancedTab(TabFolder parent) {
		GridData gridData = null;

		// The composite for this tab.
		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// The multi key assit button.
		checkBoxMultiKeyAssist = new Button(composite, SWT.CHECK);
		checkBoxMultiKeyAssist.setText(Util.translateString(RESOURCE_BUNDLE, "checkBoxMultiKeyAssist.Text")); //$NON-NLS-1$
		checkBoxMultiKeyAssist.setToolTipText(Util.translateString(RESOURCE_BUNDLE, "checkBoxMultiKeyAssist.ToolTipText")); //$NON-NLS-1$
		checkBoxMultiKeyAssist.setFont(composite.getFont());
		checkBoxMultiKeyAssist.setSelection(
			getPreferenceStore().getBoolean(IPreferenceConstants.MULTI_KEY_ASSIST));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		checkBoxMultiKeyAssist.setLayoutData(gridData);

		// The multi key assist time.
		final IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		textMultiKeyAssistTime = new IntegerFieldEditor(IPreferenceConstants.MULTI_KEY_ASSIST_TIME, Util.translateString(RESOURCE_BUNDLE, "textMultiKeyAssistTime.Text"), composite); //$NON-NLS-1$
		textMultiKeyAssistTime.setPreferenceStore(store);
		textMultiKeyAssistTime.setPreferencePage(this);
		textMultiKeyAssistTime.setTextLimit(2);
		textMultiKeyAssistTime.setErrorMessage(Util.translateString(RESOURCE_BUNDLE, "textMultiKeyAssistTime.ErrorMessage")); //$NON-NLS-1$
		textMultiKeyAssistTime.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		textMultiKeyAssistTime.setValidRange(0, Integer.MAX_VALUE);
		textMultiKeyAssistTime.setStringValue(
			Integer.toString(store.getInt(IPreferenceConstants.MULTI_KEY_ASSIST_TIME)));
		textMultiKeyAssistTime.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID))
					setValid(textMultiKeyAssistTime.isValid());
			}
		});

		// The multi-key rocker button.
		checkBoxMultiKeyRocker = new Button(composite, SWT.CHECK);
		checkBoxMultiKeyRocker.setText(Util.translateString(RESOURCE_BUNDLE, "checkBoxMultiKeyRocker.Text")); //$NON-NLS-1$
		checkBoxMultiKeyRocker.setToolTipText(Util.translateString(RESOURCE_BUNDLE, "checkBoxMultiKeyRocker.ToolTipText")); //$NON-NLS-1$
		checkBoxMultiKeyRocker.setFont(composite.getFont());
		checkBoxMultiKeyRocker.setSelection(
			getPreferenceStore().getBoolean(IPreferenceConstants.MULTI_KEY_ROCKER));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		checkBoxMultiKeyRocker.setLayoutData(gridData);

		// Conigure the layout of the composite.
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);
		
		return composite;
	}

	private Composite createBasicTab(TabFolder parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gridData);
		Composite compositeKeyConfiguration = new Composite(composite, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		compositeKeyConfiguration.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		compositeKeyConfiguration.setLayoutData(gridData);
		labelKeyConfiguration = new Label(compositeKeyConfiguration, SWT.LEFT);
		labelKeyConfiguration.setText(Util.translateString(RESOURCE_BUNDLE, "labelKeyConfiguration")); //$NON-NLS-1$
		comboKeyConfiguration = new Combo(compositeKeyConfiguration, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 200;
		comboKeyConfiguration.setLayoutData(gridData);

		comboKeyConfiguration.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboKeyConfiguration();
			}
		});

		labelKeyConfigurationExtends = new Label(compositeKeyConfiguration, SWT.LEFT);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		labelKeyConfigurationExtends.setLayoutData(gridData);
		Control spacer = new Composite(composite, SWT.NULL);
		gridData = new GridData();
		gridData.heightHint = 10;
		gridData.widthHint = 10;
		spacer.setLayoutData(gridData);
		groupCommand = new Group(composite, SWT.SHADOW_NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		groupCommand.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		groupCommand.setLayoutData(gridData);
		groupCommand.setText(Util.translateString(RESOURCE_BUNDLE, "groupCommand")); //$NON-NLS-1$	
		labelCategory = new Label(groupCommand, SWT.LEFT);
		gridData = new GridData();
		labelCategory.setLayoutData(gridData);
		labelCategory.setText(Util.translateString(RESOURCE_BUNDLE, "labelCategory")); //$NON-NLS-1$
		comboCategory = new Combo(groupCommand, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.widthHint = 200;
		comboCategory.setLayoutData(gridData);

		comboCategory.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboCategory();
			}
		});

		labelCommand = new Label(groupCommand, SWT.LEFT);
		gridData = new GridData();
		labelCommand.setLayoutData(gridData);
		labelCommand.setText(Util.translateString(RESOURCE_BUNDLE, "labelCommand")); //$NON-NLS-1$
		comboCommand = new Combo(groupCommand, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.widthHint = 300;
		comboCommand.setLayoutData(gridData);

		comboCommand.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboCommand();
			}
		});

		labelAssignmentsForCommand = new Label(groupCommand, SWT.LEFT);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		labelAssignmentsForCommand.setLayoutData(gridData);
		labelAssignmentsForCommand.setText(Util.translateString(RESOURCE_BUNDLE, "labelAssignmentsForCommand")); //$NON-NLS-1$
		tableAssignmentsForCommand =
			new Table(groupCommand, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableAssignmentsForCommand.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 60;
		gridData.horizontalSpan = 2;
		gridData.widthHint = "carbon".equals(SWT.getPlatform()) ? 520 : 420; //$NON-NLS-1$
		tableAssignmentsForCommand.setLayoutData(gridData);
		TableColumn tableColumnDelta = new TableColumn(tableAssignmentsForCommand, SWT.NULL, 0);
		tableColumnDelta.setResizable(false);
		tableColumnDelta.setText(Util.ZERO_LENGTH_STRING);
		tableColumnDelta.setWidth(20);
		TableColumn tableColumnActivity = new TableColumn(tableAssignmentsForCommand, SWT.NULL, 1);
		tableColumnActivity.setResizable(true);
		tableColumnActivity.setText(Util.translateString(RESOURCE_BUNDLE, "tableColumnActivity")); //$NON-NLS-1$
		tableColumnActivity.pack();
		tableColumnActivity.setWidth("carbon".equals(SWT.getPlatform()) ? 110 : 100); //$NON-NLS-1$
		TableColumn tableColumnKeySequence =
			new TableColumn(tableAssignmentsForCommand, SWT.NULL, 2);
		tableColumnKeySequence.setResizable(true);
		tableColumnKeySequence.setText(Util.translateString(RESOURCE_BUNDLE, "tableColumnKeySequence")); //$NON-NLS-1$
		tableColumnKeySequence.pack();
		tableColumnKeySequence.setWidth(300);

		tableAssignmentsForCommand.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent mouseEvent) {
				doubleClickedAssignmentsForCommand();
			}
		});

		tableAssignmentsForCommand.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedTableAssignmentsForCommand();
			}
		});

		groupKeySequence = new Group(composite, SWT.SHADOW_NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		groupKeySequence.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		groupKeySequence.setLayoutData(gridData);
		groupKeySequence.setText(Util.translateString(RESOURCE_BUNDLE, "groupKeySequence")); //$NON-NLS-1$	
		labelKeySequence = new Label(groupKeySequence, SWT.LEFT);
		gridData = new GridData();
		labelKeySequence.setLayoutData(gridData);
		labelKeySequence.setText(Util.translateString(RESOURCE_BUNDLE, "labelKeySequence")); //$NON-NLS-1$

		// The text widget into which the key strokes will be entered.
		textKeySequence = new Text(groupKeySequence, SWT.BORDER);
		// On MacOS X, this font will be changed by KeySequenceText
		textKeySequence.setFont(groupKeySequence.getFont());
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.widthHint = 300;
		textKeySequence.setLayoutData(gridData);
		textKeySequence.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				modifiedTextKeySequence();
			}
		});

		// The manager for the key sequence text widget.
		textKeySequenceManager = new KeySequenceText(textKeySequence);
		textKeySequenceManager.setKeyStrokeLimit(4);

		// Button for adding trapped key strokes
		buttonAddKey = new Button(groupKeySequence, SWT.LEFT | SWT.ARROW);
		buttonAddKey.setToolTipText(Util.translateString(RESOURCE_BUNDLE, "buttonAddKey.ToolTipText")); //$NON-NLS-1$
		gridData = new GridData();
		gridData.heightHint = comboCategory.getTextHeight();
		buttonAddKey.setLayoutData(gridData);
		buttonAddKey.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				Point buttonLocation = buttonAddKey.getLocation();
				buttonLocation = groupKeySequence.toDisplay(buttonLocation.x, buttonLocation.y);
				Point buttonSize = buttonAddKey.getSize();
				menuButtonAddKey.setLocation(buttonLocation.x, buttonLocation.y + buttonSize.y);
				menuButtonAddKey.setVisible(true);
			}
		});

		// Arrow buttons aren't normally added to the tab list. Let's fix that.
		Control[] tabStops = groupKeySequence.getTabList();
		ArrayList newTabStops = new ArrayList();
		for (int i = 0; i < tabStops.length; i++) {
			Control tabStop = tabStops[i];
			newTabStops.add(tabStop);
			if (textKeySequence.equals(tabStop)) {
				newTabStops.add(buttonAddKey);
			}
		}
		Control[] newTabStopArray =
			(Control[]) newTabStops.toArray(new Control[newTabStops.size()]);
		groupKeySequence.setTabList(newTabStopArray);

		// Construct the menu to attach to the above button.
		menuButtonAddKey = new Menu(buttonAddKey);
		Iterator trappedKeyItr = KeySequenceText.TRAPPED_KEYS.iterator();
		while (trappedKeyItr.hasNext()) {
			final KeyStroke trappedKey = (KeyStroke) trappedKeyItr.next();
			MenuItem menuItem = new MenuItem(menuButtonAddKey, SWT.PUSH);
			menuItem.setText(trappedKey.format());
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					textKeySequenceManager.insert(trappedKey);
					// TODO Focus selects everything.
					//textKeySequence.setFocus();
				}
			});
		}

		labelAssignmentsForKeySequence = new Label(groupKeySequence, SWT.LEFT);
		gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		labelAssignmentsForKeySequence.setLayoutData(gridData);
		labelAssignmentsForKeySequence.setText(Util.translateString(RESOURCE_BUNDLE, "labelAssignmentsForKeySequence")); //$NON-NLS-1$
		tableAssignmentsForKeySequence =
			new Table(
				groupKeySequence,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tableAssignmentsForKeySequence.setHeaderVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 60;
		gridData.horizontalSpan = 3;
		gridData.widthHint = "carbon".equals(SWT.getPlatform()) ? 520 : 420; //$NON-NLS-1$
		tableAssignmentsForKeySequence.setLayoutData(gridData);
		tableColumnDelta = new TableColumn(tableAssignmentsForKeySequence, SWT.NULL, 0);
		tableColumnDelta.setResizable(false);
		tableColumnDelta.setText(Util.ZERO_LENGTH_STRING);
		tableColumnDelta.setWidth(20);
		tableColumnActivity = new TableColumn(tableAssignmentsForKeySequence, SWT.NULL, 1);
		tableColumnActivity.setResizable(true);
		tableColumnActivity.setText(Util.translateString(RESOURCE_BUNDLE, "tableColumnActivity")); //$NON-NLS-1$
		tableColumnActivity.pack();
		tableColumnActivity.setWidth("carbon".equals(SWT.getPlatform()) ? 110 : 100); //$NON-NLS-1$
		TableColumn tableColumnCommand =
			new TableColumn(tableAssignmentsForKeySequence, SWT.NULL, 2);
		tableColumnCommand.setResizable(true);
		tableColumnCommand.setText(Util.translateString(RESOURCE_BUNDLE, "tableColumnCommand")); //$NON-NLS-1$
		tableColumnCommand.pack();
		tableColumnCommand.setWidth(300);

		tableAssignmentsForKeySequence.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent mouseEvent) {
				doubleClickedTableAssignmentsForKeySequence();
			}
		});

		tableAssignmentsForKeySequence.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedTableAssignmentsForKeySequence();
			}
		});

		Composite compositeActivity = new Composite(composite, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		compositeActivity.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		compositeActivity.setLayoutData(gridData);
		labelActivity = new Label(compositeActivity, SWT.LEFT);
		labelActivity.setText(Util.translateString(RESOURCE_BUNDLE, "labelActivity")); //$NON-NLS-1$
		comboActivity = new Combo(compositeActivity, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = 200;
		comboActivity.setLayoutData(gridData);

		comboActivity.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedComboActivity();
			}
		});

		labelActivityExtends = new Label(compositeActivity, SWT.LEFT);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		labelActivityExtends.setLayoutData(gridData);
		Composite compositeButton = new Composite(composite, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 20;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 3;
		compositeButton.setLayout(gridLayout);
		gridData = new GridData();
		compositeButton.setLayoutData(gridData);
		buttonAdd = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonAdd.setText(Util.translateString(RESOURCE_BUNDLE, "buttonAdd")); //$NON-NLS-1$
		gridData.widthHint =
			Math.max(widthHint, buttonAdd.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonAdd.setLayoutData(gridData);

		buttonAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonAdd();
			}
		});

		buttonRemove = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonRemove.setText(Util.translateString(RESOURCE_BUNDLE, "buttonRemove")); //$NON-NLS-1$
		gridData.widthHint =
			Math.max(widthHint, buttonRemove.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonRemove.setLayoutData(gridData);

		buttonRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonRemove();
			}
		});

		buttonRestore = new Button(compositeButton, SWT.CENTER | SWT.PUSH);
		gridData = new GridData();
		gridData.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		buttonRestore.setText(Util.translateString(RESOURCE_BUNDLE, "buttonRestore")); //$NON-NLS-1$
		gridData.widthHint =
			Math.max(widthHint, buttonRestore.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x) + 5;
		buttonRestore.setLayoutData(gridData);

		buttonRestore.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				selectedButtonRestore();
			}
		});

		// TODO WorkbenchHelp.setHelp(parent,
		// IHelpContextIds.WORKBENCH_KEY_PREFERENCE_PAGE);
		applyDialogFont(composite);
		return composite;
	}

	protected Control createContents(Composite parent) {
		final TabFolder tabFolder = new TabFolder(parent, SWT.BORDER);

		// Basic tab
		final TabItem basicTab = new TabItem(tabFolder, SWT.NULL);
		basicTab.setText(Util.translateString(RESOURCE_BUNDLE, "basicTab.Text")); //$NON-NLS-1$
		basicTab.setToolTipText(Util.translateString(RESOURCE_BUNDLE, "basicTab.ToolTipText")); //$NON-NLS-1$
		basicTab.setControl(createBasicTab(tabFolder));

		// Advanced tab
		final TabItem advancedTab = new TabItem(tabFolder, SWT.NULL);
		advancedTab.setText(Util.translateString(RESOURCE_BUNDLE, "advancedTab.Text")); //$NON-NLS-1$
		advancedTab.setToolTipText(Util.translateString(RESOURCE_BUNDLE, "advancedTab.ToolTipText")); //$NON-NLS-1$
		advancedTab.setControl(createAdvancedTab(tabFolder));

		return tabFolder;
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return PlatformUI.getWorkbench().getPreferenceStore();
	}

	private void doubleClickedAssignmentsForCommand() {
		update();
	}

	private void doubleClickedTableAssignmentsForKeySequence() {
		update();
	}

	private String getActivityId() {
		return comboActivity.getSelectionIndex() > 0
			? (String) activityIdsByUniqueName.get(comboActivity.getText())
			: null;
	}

	private String getCategoryId() {
		return !commandIdsByCategoryId.containsKey(null)
			|| comboCategory.getSelectionIndex() > 0
				? (String) categoryIdsByUniqueName.get(comboCategory.getText())
				: null;
	}

	private String getCommandId() {
		return (String) commandIdsByUniqueName.get(comboCommand.getText());
	}

	private String getKeyConfigurationId() {
		return comboKeyConfiguration.getSelectionIndex() >= 0
			? (String) keyConfigurationIdsByUniqueName.get(comboKeyConfiguration.getText())
			: null;
	}

	private KeySequence getKeySequence() {
		return textKeySequenceManager.getKeySequence();
	}

	public void init(IWorkbench workbench) {
		this.workbench = workbench;
		activityManager = workbench.getActivityManager();
		// TODO remove blind cast
		commandManager = (CommandManager) workbench.getCommandManager();
		commandAssignments = new TreeSet();
		keySequenceAssignments = new TreeSet();
	}

	private void modifiedTextKeySequence() {
		update();
	}

	protected void performDefaults() {
		String activeKeyConfigurationId = getKeyConfigurationId();
		List preferenceKeySequenceBindingDefinitions = new ArrayList();
		KeySequenceBindingNode.getKeySequenceBindingDefinitions(
			tree,
			KeySequence.getInstance(),
			0,
			preferenceKeySequenceBindingDefinitions);

		if (activeKeyConfigurationId != null
			|| !preferenceKeySequenceBindingDefinitions.isEmpty()) {
			MessageBox restoreDefaultsMessageBox =
				new MessageBox(
					getShell(),
					SWT.YES | SWT.NO | SWT.ICON_WARNING | SWT.APPLICATION_MODAL);
			restoreDefaultsMessageBox.setText(Util.translateString(RESOURCE_BUNDLE, "restoreDefaultsMessageBoxText")); //$NON-NLS-1$
			restoreDefaultsMessageBox.setMessage(Util.translateString(RESOURCE_BUNDLE, "restoreDefaultsMessageBoxMessage")); //$NON-NLS-1$

			if (restoreDefaultsMessageBox.open() == SWT.YES) {
				setKeyConfigurationId(null);
				Iterator iterator = preferenceKeySequenceBindingDefinitions.iterator();

				while (iterator.hasNext()) {
					IKeySequenceBindingDefinition keySequenceBindingDefinition =
						(IKeySequenceBindingDefinition) iterator.next();
					KeySequenceBindingNode.remove(
						tree,
						keySequenceBindingDefinition.getKeySequence(),
						keySequenceBindingDefinition.getActivityId(),
						keySequenceBindingDefinition.getKeyConfigurationId(),
						0,
						keySequenceBindingDefinition.getPlatform(),
						keySequenceBindingDefinition.getLocale(),
						keySequenceBindingDefinition.getCommandId());
				}
			}
		}

		// Set the defaults on the advanced tab.
		IPreferenceStore store = getPreferenceStore();
		checkBoxMultiKeyAssist.setSelection(
			store.getDefaultBoolean(IPreferenceConstants.MULTI_KEY_ASSIST));
		textMultiKeyAssistTime.setStringValue(
			Float.toString(store.getDefaultFloat(IPreferenceConstants.MULTI_KEY_ASSIST_TIME)));
		checkBoxMultiKeyRocker.setSelection(
			store.getDefaultBoolean(IPreferenceConstants.MULTI_KEY_ROCKER));

		update();
	}

	public boolean performOk() {
		List preferenceActiveKeyConfigurationDefinitions = new ArrayList();
		preferenceActiveKeyConfigurationDefinitions.add(
			new ActiveKeyConfigurationDefinition(getKeyConfigurationId(), null));
		PreferenceCommandRegistry preferenceCommandRegistry =
			(PreferenceCommandRegistry) commandManager.getMutableCommandRegistry();
		preferenceCommandRegistry.setActiveKeyConfigurationDefinitions(
			preferenceActiveKeyConfigurationDefinitions);
		List preferenceKeySequenceBindingDefinitions = new ArrayList();
		KeySequenceBindingNode.getKeySequenceBindingDefinitions(
			tree,
			KeySequence.getInstance(),
			0,
			preferenceKeySequenceBindingDefinitions);
		preferenceCommandRegistry.setKeySequenceBindingDefinitions(
			preferenceKeySequenceBindingDefinitions);

		try {
			preferenceCommandRegistry.save();
		} catch (IOException eIO) {
			// Do nothing
		}

		// Save the advanced settings.
		IPreferenceStore store = getPreferenceStore();
		store.setValue(
			IPreferenceConstants.MULTI_KEY_ASSIST,
			checkBoxMultiKeyAssist.getSelection());
		store.setValue(
			IPreferenceConstants.MULTI_KEY_ASSIST_TIME,
			textMultiKeyAssistTime.getIntValue());
		store.setValue(
			IPreferenceConstants.MULTI_KEY_ROCKER,
			checkBoxMultiKeyRocker.getSelection());

		// TODO remove the dependancy on Workbench. have Workbench rely on
		// events from CommandManager.
		if (workbench instanceof Workbench) {
			((Workbench) workbench).workbenchCommandsAndContexts.updateActiveContextIds();
			((Workbench) workbench)
				.workbenchCommandsAndContexts
				.updateActiveWorkbenchWindowMenuManager(true);
		}

		return super.performOk();
	}

	private void selectAssignmentForCommand(String activityId) {
		if (tableAssignmentsForCommand.getSelectionCount() > 1)
			tableAssignmentsForCommand.deselectAll();

		int i = 0;
		int selection = -1;
		KeySequence keySequence = getKeySequence();

		for (Iterator iterator = commandAssignments.iterator(); iterator.hasNext(); i++) {
			CommandAssignment commandAssignment = (CommandAssignment) iterator.next();

			if (Util.equals(activityId, commandAssignment.activityId)
				&& Util.equals(keySequence, commandAssignment.keySequence)) {
				selection = i;
				break;
			}
		}

		if (selection != tableAssignmentsForCommand.getSelectionIndex()) {
			if (selection == -1 || selection >= tableAssignmentsForCommand.getItemCount())
				tableAssignmentsForCommand.deselectAll();
			else
				tableAssignmentsForCommand.select(selection);
		}
	}

	private void selectAssignmentForKeySequence(String activityId) {
		if (tableAssignmentsForKeySequence.getSelectionCount() > 1)
			tableAssignmentsForKeySequence.deselectAll();

		int i = 0;
		int selection = -1;

		for (Iterator iterator = keySequenceAssignments.iterator(); iterator.hasNext(); i++) {
			KeySequenceAssignment keySequenceAssignment = (KeySequenceAssignment) iterator.next();

			if (Util.equals(activityId, keySequenceAssignment.activityId)) {
				selection = i;
				break;
			}
		}

		if (selection != tableAssignmentsForKeySequence.getSelectionIndex()) {
			if (selection == -1 || selection >= tableAssignmentsForKeySequence.getItemCount())
				tableAssignmentsForKeySequence.deselectAll();
			else
				tableAssignmentsForKeySequence.select(selection);
		}
	}

	private void selectedButtonAdd() {
		String commandId = getCommandId();
		String activityId = getActivityId();
		String keyConfigurationId = getKeyConfigurationId();
		KeySequence keySequence = getKeySequence();
		KeySequenceBindingNode.remove(
			tree,
			keySequence,
			activityId,
			keyConfigurationId,
			0,
			null,
			null);
		KeySequenceBindingNode.add(
			tree,
			keySequence,
			activityId,
			keyConfigurationId,
			0,
			null,
			null,
			commandId);
		List preferenceKeySequenceBindingDefinitions = new ArrayList();
		KeySequenceBindingNode.getKeySequenceBindingDefinitions(
			tree,
			KeySequence.getInstance(),
			0,
			preferenceKeySequenceBindingDefinitions);
		update();
	}

	private void selectedButtonRemove() {
		String activityId = getActivityId();
		String keyConfigurationId = getKeyConfigurationId();
		KeySequence keySequence = getKeySequence();
		KeySequenceBindingNode.remove(
			tree,
			keySequence,
			activityId,
			keyConfigurationId,
			0,
			null,
			null);
		KeySequenceBindingNode.add(
			tree,
			keySequence,
			activityId,
			keyConfigurationId,
			0,
			null,
			null,
			null);
		List preferenceKeySequenceBindingDefinitions = new ArrayList();
		KeySequenceBindingNode.getKeySequenceBindingDefinitions(
			tree,
			KeySequence.getInstance(),
			0,
			preferenceKeySequenceBindingDefinitions);
		update();
	}

	private void selectedButtonRestore() {
		String activityId = getActivityId();
		String keyConfigurationId = getKeyConfigurationId();
		KeySequence keySequence = getKeySequence();
		KeySequenceBindingNode.remove(
			tree,
			keySequence,
			activityId,
			keyConfigurationId,
			0,
			null,
			null);
		List preferenceKeySequenceBindingDefinitions = new ArrayList();
		KeySequenceBindingNode.getKeySequenceBindingDefinitions(
			tree,
			KeySequence.getInstance(),
			0,
			preferenceKeySequenceBindingDefinitions);
		update();
	}

	private void selectedComboActivity() {
		update();
	}

	private void selectedComboCategory() {
		update();
	}

	private void selectedComboCommand() {
		update();
	}

	private void selectedComboKeyConfiguration() {
		update();
	}

	private void selectedTableAssignmentsForCommand() {
		int selection = tableAssignmentsForCommand.getSelectionIndex();
		List commandAssignmentsAsList = new ArrayList(commandAssignments);

		if (selection >= 0
			&& selection < commandAssignmentsAsList.size()
			&& tableAssignmentsForCommand.getSelectionCount() == 1) {
			CommandAssignment commandAssignment =
				(CommandAssignment) commandAssignmentsAsList.get(selection);
			String activityId = commandAssignment.activityId;
			KeySequence keySequence = commandAssignment.keySequence;
			setActivityId(activityId);
			setKeySequence(keySequence);
		}

		update();
	}

	private void selectedTableAssignmentsForKeySequence() {
		int selection = tableAssignmentsForKeySequence.getSelectionIndex();
		List keySequenceAssignmentsAsList = new ArrayList(keySequenceAssignments);

		if (selection >= 0
			&& selection < keySequenceAssignmentsAsList.size()
			&& tableAssignmentsForKeySequence.getSelectionCount() == 1) {
			KeySequenceAssignment keySequenceAssignment =
				(KeySequenceAssignment) keySequenceAssignmentsAsList.get(selection);
			String activityId = keySequenceAssignment.activityId;
			setActivityId(activityId);
		}

		update();
	}

	private void setActivitiesForCommand() {
		String commandId = getCommandId();
		String activityId = getActivityId();
		Set activityIds = (Set) activityIdsByCommandId.get(commandId);
		Map activityIdsByUniqueName = new HashMap(this.activityIdsByUniqueName);

		// TODO for activity bound commands, this code retains only those
		// activities explictly bound. what about assigning key bindings to
		// implicit descendant activities?
		if (activityIds != null)
			activityIdsByUniqueName.values().retainAll(activityIds);

		List activityNames = new ArrayList(activityIdsByUniqueName.keySet());
		Collections.sort(activityNames, Collator.getInstance());

		if (activityIds == null)
			activityNames.add(0, Util.translateString(RESOURCE_BUNDLE, "general")); //$NON-NLS-1$

		comboActivity.setItems((String[]) activityNames.toArray(new String[activityNames.size()]));
		setActivityId(activityId);

		if (comboActivity.getSelectionIndex() == -1 && !activityNames.isEmpty())
			comboActivity.select(0);
	}

	private void setActivityId(String activityId) {
		comboActivity.clearSelection();
		comboActivity.deselectAll();
		String activityUniqueName = (String) activityUniqueNamesById.get(activityId);

		if (activityUniqueName != null) {
			String items[] = comboActivity.getItems();

			for (int i = 1; i < items.length; i++)
				if (activityUniqueName.equals(items[i])) {
					comboActivity.select(i);
					break;
				}
		} else
			comboActivity.select(0);
	}

	private void setAssignmentsForCommand() {
		commandAssignments.clear();
		String commandId = getCommandId();

		for (Iterator iterator = assignmentsByActivityIdByKeySequence.entrySet().iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeySequence keySequence = (KeySequence) entry.getKey();
			Map assignmentsByActivityId = (Map) entry.getValue();

			if (assignmentsByActivityId != null)
				for (Iterator iterator2 = assignmentsByActivityId.entrySet().iterator();
					iterator2.hasNext();
					) {
					Map.Entry entry2 = (Map.Entry) iterator2.next();
					CommandAssignment commandAssignment = new CommandAssignment();
					commandAssignment.assignment =
						(KeySequenceBindingNode.Assignment) entry2.getValue();
					commandAssignment.activityId = (String) entry2.getKey();
					commandAssignment.keySequence = keySequence;

					if (commandAssignment.assignment.contains(commandId))
						commandAssignments.add(commandAssignment);
				}
		}

		buildCommandAssignmentsTable();
	}

	private void setAssignmentsForKeySequence() {
		keySequenceAssignments.clear();
		KeySequence keySequence = getKeySequence();
		Map assignmentsByActivityId = (Map) assignmentsByActivityIdByKeySequence.get(keySequence);

		if (assignmentsByActivityId != null)
			for (Iterator iterator = assignmentsByActivityId.entrySet().iterator();
				iterator.hasNext();
				) {
				Map.Entry entry = (Map.Entry) iterator.next();
				KeySequenceAssignment keySequenceAssignment = new KeySequenceAssignment();
				keySequenceAssignment.assignment =
					(KeySequenceBindingNode.Assignment) entry.getValue();
				keySequenceAssignment.activityId = (String) entry.getKey();
				keySequenceAssignments.add(keySequenceAssignment);
			}

		buildKeySequenceAssignmentsTable();
	}

	private void setCommandId(String commandId) {
		comboCommand.clearSelection();
		comboCommand.deselectAll();
		String commandUniqueName = (String) commandUniqueNamesById.get(commandId);

		if (commandUniqueName != null) {
			String items[] = comboCommand.getItems();

			for (int i = 0; i < items.length; i++)
				if (commandUniqueName.equals(items[i])) {
					comboCommand.select(i);
					break;
				}
		}
	}

	private void setCommandsForCategory() {
		String categoryId = getCategoryId();
		String commandId = getCommandId();
		Set commandIds = (Set) commandIdsByCategoryId.get(categoryId);
		Map commandIdsByUniqueName = new HashMap(this.commandIdsByUniqueName);
		commandIdsByUniqueName.values().retainAll(commandIds);
		List commandNames = new ArrayList(commandIdsByUniqueName.keySet());
		Collections.sort(commandNames, Collator.getInstance());
		comboCommand.setItems((String[]) commandNames.toArray(new String[commandNames.size()]));
		setCommandId(commandId);

		if (comboCommand.getSelectionIndex() == -1 && !commandNames.isEmpty())
			comboCommand.select(0);
	}

	private void setKeyConfigurationId(String keyConfigurationId) {
		comboKeyConfiguration.clearSelection();
		comboKeyConfiguration.deselectAll();
		String keyConfigurationUniqueName =
			(String) keyConfigurationUniqueNamesById.get(keyConfigurationId);

		if (keyConfigurationUniqueName != null) {
			String items[] = comboKeyConfiguration.getItems();

			for (int i = 0; i < items.length; i++)
				if (keyConfigurationUniqueName.equals(items[i])) {
					comboKeyConfiguration.select(i);
					break;
				}
		}
	}

	private void setKeySequence(KeySequence keySequence) {
		textKeySequenceManager.setKeySequence(keySequence);
	}

	public void setVisible(boolean visible) {
		if (visible == true) {
			Map activitiesByName = new HashMap();

			for (Iterator iterator = activityManager.getDefinedActivityIds().iterator();
				iterator.hasNext();
				) {
				IActivity activity = activityManager.getActivity((String) iterator.next());

				try {
					String name = activity.getName();
					Collection activities = (Collection) activitiesByName.get(name);

					if (activities == null) {
						activities = new HashSet();
						activitiesByName.put(name, activities);
					}

					activities.add(activity);
				} catch (org.eclipse.ui.activities.NotDefinedException eNotDefined) {
					// Do nothing
				}
			}

			Map categoriesByName = new HashMap();

			for (Iterator iterator = commandManager.getDefinedCategoryIds().iterator();
				iterator.hasNext();
				) {
				ICategory category = commandManager.getCategory((String) iterator.next());

				try {
					String name = category.getName();
					Collection categories = (Collection) categoriesByName.get(name);

					if (categories == null) {
						categories = new HashSet();
						categoriesByName.put(name, categories);
					}

					categories.add(category);
				} catch (org.eclipse.ui.commands.NotDefinedException eNotDefined) {
					// Do nothing
				}
			}

			Map commandsByName = new HashMap();

			for (Iterator iterator = commandManager.getDefinedCommandIds().iterator();
				iterator.hasNext();
				) {
				ICommand command = commandManager.getCommand((String) iterator.next());

				try {
					String name = command.getName();
					Collection commands = (Collection) commandsByName.get(name);

					if (commands == null) {
						commands = new HashSet();
						commandsByName.put(name, commands);
					}

					commands.add(command);
				} catch (org.eclipse.ui.commands.NotDefinedException eNotDefined) {
					// Do nothing
				}
			}

			Map keyConfigurationsByName = new HashMap();

			for (Iterator iterator = commandManager.getDefinedKeyConfigurationIds().iterator();
				iterator.hasNext();
				) {
				IKeyConfiguration keyConfiguration =
					commandManager.getKeyConfiguration((String) iterator.next());

				try {
					String name = keyConfiguration.getName();
					Collection keyConfigurations = (Collection) keyConfigurationsByName.get(name);

					if (keyConfigurations == null) {
						keyConfigurations = new HashSet();
						keyConfigurationsByName.put(name, keyConfigurations);
					}

					keyConfigurations.add(keyConfiguration);
				} catch (org.eclipse.ui.commands.NotDefinedException eNotDefined) {
					// Do nothing
				}
			}

			activityIdsByUniqueName = new HashMap();
			activityUniqueNamesById = new HashMap();

			for (Iterator iterator = activitiesByName.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set activities = (Set) entry.getValue();
				Iterator iterator2 = activities.iterator();

				if (activities.size() == 1) {
					IActivity activity = (IActivity) iterator2.next();
					activityIdsByUniqueName.put(name, activity.getId());
					activityUniqueNamesById.put(activity.getId(), name);
				} else
					while (iterator2.hasNext()) {
						IActivity activity = (IActivity) iterator2.next();
						String uniqueName = MessageFormat.format(Util.translateString(RESOURCE_BUNDLE, "uniqueName"), new Object[] { name, activity.getId()}); //$NON-NLS-1$
						activityIdsByUniqueName.put(uniqueName, activity.getId());
						activityUniqueNamesById.put(activity.getId(), uniqueName);
					}
			}

			categoryIdsByUniqueName = new HashMap();
			categoryUniqueNamesById = new HashMap();

			for (Iterator iterator = categoriesByName.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set categories = (Set) entry.getValue();
				Iterator iterator2 = categories.iterator();

				if (categories.size() == 1) {
					ICategory category = (ICategory) iterator2.next();
					categoryIdsByUniqueName.put(name, category.getId());
					categoryUniqueNamesById.put(category.getId(), name);
				} else
					while (iterator2.hasNext()) {
						ICategory category = (ICategory) iterator2.next();
						String uniqueName = MessageFormat.format(Util.translateString(RESOURCE_BUNDLE, "uniqueName"), new Object[] { name, category.getId()}); //$NON-NLS-1$
						categoryIdsByUniqueName.put(uniqueName, category.getId());
						categoryUniqueNamesById.put(category.getId(), uniqueName);
					}
			}

			commandIdsByUniqueName = new HashMap();
			commandUniqueNamesById = new HashMap();

			for (Iterator iterator = commandsByName.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set commands = (Set) entry.getValue();
				Iterator iterator2 = commands.iterator();

				if (commands.size() == 1) {
					ICommand command = (ICommand) iterator2.next();
					commandIdsByUniqueName.put(name, command.getId());
					commandUniqueNamesById.put(command.getId(), name);
				} else
					while (iterator2.hasNext()) {
						ICommand command = (ICommand) iterator2.next();
						String uniqueName = MessageFormat.format(Util.translateString(RESOURCE_BUNDLE, "uniqueName"), new Object[] { name, command.getId()}); //$NON-NLS-1$
						commandIdsByUniqueName.put(uniqueName, command.getId());
						commandUniqueNamesById.put(command.getId(), uniqueName);
					}
			}

			keyConfigurationIdsByUniqueName = new HashMap();
			keyConfigurationUniqueNamesById = new HashMap();

			for (Iterator iterator = keyConfigurationsByName.entrySet().iterator();
				iterator.hasNext();
				) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String name = (String) entry.getKey();
				Set keyConfigurations = (Set) entry.getValue();
				Iterator iterator2 = keyConfigurations.iterator();

				if (keyConfigurations.size() == 1) {
					IKeyConfiguration keyConfiguration = (IKeyConfiguration) iterator2.next();
					keyConfigurationIdsByUniqueName.put(name, keyConfiguration.getId());
					keyConfigurationUniqueNamesById.put(keyConfiguration.getId(), name);
				} else
					while (iterator2.hasNext()) {
						IKeyConfiguration keyConfiguration = (IKeyConfiguration) iterator2.next();
						String uniqueName = MessageFormat.format(Util.translateString(RESOURCE_BUNDLE, "uniqueName"), new Object[] { name, keyConfiguration.getId()}); //$NON-NLS-1$
						keyConfigurationIdsByUniqueName.put(uniqueName, keyConfiguration.getId());
						keyConfigurationUniqueNamesById.put(keyConfiguration.getId(), uniqueName);
					}
			}

			String activeKeyConfigurationId = commandManager.getActiveKeyConfigurationId();
			activityIdsByCommandId = new HashMap();

			for (Iterator iterator = commandManager.getDefinedCommandIds().iterator();
				iterator.hasNext();
				) {
				ICommand command = commandManager.getCommand((String) iterator.next());
				List activityBindings = command.getActivityBindings();

				if (!activityBindings.isEmpty()) {
					Set activityIds = new HashSet();

					for (Iterator iterator2 = activityBindings.iterator(); iterator2.hasNext();) {
						IActivityBinding activityBinding = (IActivityBinding) iterator2.next();
						String activityId = activityBinding.getActivityId();

						if (activityManager.getDefinedActivityIds().contains(activityId))
							activityIds.add(activityId);
					}

					activityIdsByCommandId.put(command.getId(), activityIds);
				}
			}

			commandIdsByCategoryId = new HashMap();

			for (Iterator iterator = commandManager.getDefinedCommandIds().iterator();
				iterator.hasNext();
				) {
				ICommand command = commandManager.getCommand((String) iterator.next());

				try {
					String categoryId = command.getCategoryId();
					Collection commandIds = (Collection) commandIdsByCategoryId.get(categoryId);

					if (commandIds == null) {
						commandIds = new HashSet();
						commandIdsByCategoryId.put(categoryId, commandIds);
					}

					commandIds.add(command.getId());
				} catch (org.eclipse.ui.commands.NotDefinedException eNotDefined) {
					// Do nothing
				}
			}

			ICommandRegistry commandRegistry = commandManager.getCommandRegistry();
			ICommandRegistry mutableCommandRegistry = commandManager.getMutableCommandRegistry();

			List pluginKeySequenceBindingDefinitions =
				new ArrayList(commandRegistry.getKeySequenceBindingDefinitions());

			for (Iterator iterator = pluginKeySequenceBindingDefinitions.iterator();
				iterator.hasNext();
				) {
				IKeySequenceBindingDefinition keySequenceBindingDefinition =
					(IKeySequenceBindingDefinition) iterator.next();
				KeySequence keySequence = keySequenceBindingDefinition.getKeySequence();
				String commandId = keySequenceBindingDefinition.getCommandId();
				String activityId = keySequenceBindingDefinition.getActivityId();
				String keyConfigurationId = keySequenceBindingDefinition.getKeyConfigurationId();
				Set activityIds = (Set) activityIdsByCommandId.get(commandId);
				boolean validKeySequence =
					keySequence != null && CommandManager.validateKeySequence(keySequence);
				boolean validActivityId =
					activityId == null
						|| activityManager.getDefinedActivityIds().contains(activityId);
				boolean validCommandId =
					commandId == null || commandManager.getDefinedCommandIds().contains(commandId);
				boolean validKeyConfigurationId =
					keyConfigurationId == null
						|| commandManager.getDefinedKeyConfigurationIds().contains(keyConfigurationId);
				boolean validActivityIdForCommandId =
					activityIds == null || activityIds.contains(activityId);

				if (!validKeySequence
					|| !validCommandId
					|| !validActivityId
					|| !validKeyConfigurationId
					|| !validActivityIdForCommandId)
					iterator.remove();
			}

			List preferenceKeySequenceBindingDefinitions =
				new ArrayList(mutableCommandRegistry.getKeySequenceBindingDefinitions());

			for (Iterator iterator = preferenceKeySequenceBindingDefinitions.iterator();
				iterator.hasNext();
				) {
				IKeySequenceBindingDefinition keySequenceBindingDefinition =
					(IKeySequenceBindingDefinition) iterator.next();
				KeySequence keySequence = keySequenceBindingDefinition.getKeySequence();
				String commandId = keySequenceBindingDefinition.getCommandId();
				String activityId = keySequenceBindingDefinition.getActivityId();
				String keyConfigurationId = keySequenceBindingDefinition.getKeyConfigurationId();
				Set activityIds = (Set) activityIdsByCommandId.get(commandId);
				boolean validKeySequence =
					keySequence != null && CommandManager.validateKeySequence(keySequence);
				boolean validActivityId =
					activityId == null
						|| activityManager.getDefinedActivityIds().contains(activityId);
				boolean validCommandId =
					commandId == null || commandManager.getDefinedCommandIds().contains(commandId);
				boolean validKeyConfigurationId =
					keyConfigurationId == null
						|| commandManager.getDefinedKeyConfigurationIds().contains(keyConfigurationId);
				boolean validActivityIdForCommandId =
					activityIds == null || activityIds.contains(activityId);

				if (!validKeySequence
					|| !validCommandId
					|| !validActivityId
					|| !validKeyConfigurationId
					|| !validActivityIdForCommandId)
					iterator.remove();
			}

			tree = new TreeMap();

			for (Iterator iterator = pluginKeySequenceBindingDefinitions.iterator();
				iterator.hasNext();
				) {
				IKeySequenceBindingDefinition keySequenceBindingDefinition =
					(IKeySequenceBindingDefinition) iterator.next();
				KeySequenceBindingNode.add(
					tree,
					keySequenceBindingDefinition.getKeySequence(),
					keySequenceBindingDefinition.getActivityId(),
					keySequenceBindingDefinition.getKeyConfigurationId(),
					1,
					keySequenceBindingDefinition.getPlatform(),
					keySequenceBindingDefinition.getLocale(),
					keySequenceBindingDefinition.getCommandId());
			}

			for (Iterator iterator = preferenceKeySequenceBindingDefinitions.iterator();
				iterator.hasNext();
				) {
				IKeySequenceBindingDefinition keySequenceBindingDefinition =
					(IKeySequenceBindingDefinition) iterator.next();
				KeySequenceBindingNode.add(
					tree,
					keySequenceBindingDefinition.getKeySequence(),
					keySequenceBindingDefinition.getActivityId(),
					keySequenceBindingDefinition.getKeyConfigurationId(),
					0,
					keySequenceBindingDefinition.getPlatform(),
					keySequenceBindingDefinition.getLocale(),
					keySequenceBindingDefinition.getCommandId());
			}

			// TODO?
			//HashSet categoryIdsReferencedByCommandDefinitions = new
			// HashSet();
			//categoryDefinitionsById.keySet().retainAll(categoryIdsReferencedByCommandDefinitions);

			/*
			 * TODO rich client platform. simplify UI if possible boolean
			 * showCategory = !categoryIdsByUniqueName.isEmpty();
			 * labelCategory.setVisible(showCategory);
			 * comboCategory.setVisible(showCategory); boolean showActivity =
			 * !activityIdsByUniqueName.isEmpty();
			 * labelActivity.setVisible(showActivity);
			 * comboActivity.setVisible(showActivity);
			 * labelActivityExtends.setVisible(showActivity); boolean
			 * showKeyConfiguration =
			 * !keyConfigurationIdsByUniqueName.isEmpty();
			 * labelKeyConfiguration.setVisible(showKeyConfiguration);
			 * comboKeyConfiguration.setVisible(showKeyConfiguration);
			 * labelKeyConfigurationExtends.setVisible(showKeyConfiguration);
			 */

			List categoryNames = new ArrayList(categoryIdsByUniqueName.keySet());
			Collections.sort(categoryNames, Collator.getInstance());

			if (commandIdsByCategoryId.containsKey(null))
				categoryNames.add(0, Util.translateString(RESOURCE_BUNDLE, "other")); //$NON-NLS-1$

			comboCategory.setItems(
				(String[]) categoryNames.toArray(new String[categoryNames.size()]));
			comboCategory.clearSelection();
			comboCategory.deselectAll();

			if (commandIdsByCategoryId.containsKey(null) || !categoryNames.isEmpty())
				comboCategory.select(0);

			List keyConfigurationNames = new ArrayList(keyConfigurationIdsByUniqueName.keySet());
			Collections.sort(keyConfigurationNames, Collator.getInstance());
			comboKeyConfiguration.setItems(
				(String[]) keyConfigurationNames.toArray(new String[keyConfigurationNames.size()]));
			setKeyConfigurationId(activeKeyConfigurationId);
			update();
		}

		super.setVisible(visible);
	}

	private void update() {
		setCommandsForCategory();
		setActivitiesForCommand();
		String keyConfigurationId = getKeyConfigurationId();
		KeySequence keySequence = getKeySequence();
		String[] activeKeyConfigurationIds =
			CommandManager.extend(commandManager.getKeyConfigurationIds(keyConfigurationId));
		String[] activeLocales =
			CommandManager.extend(
				CommandManager.getPath(commandManager.getActiveLocale(), CommandManager.SEPARATOR));
		String[] activePlatforms =
			CommandManager.extend(
				CommandManager.getPath(
					commandManager.getActivePlatform(),
					CommandManager.SEPARATOR));
		KeySequenceBindingNode.solve(
			tree,
			activeKeyConfigurationIds,
			activePlatforms,
			activeLocales);
		assignmentsByActivityIdByKeySequence =
			KeySequenceBindingNode.getAssignmentsByActivityIdKeySequence(
				tree,
				KeySequence.getInstance());
		setAssignmentsForKeySequence();
		setAssignmentsForCommand();
		String commandId = getCommandId();
		String activityId = getActivityId();
		selectAssignmentForKeySequence(activityId);
		selectAssignmentForCommand(activityId);
		updateLabelKeyConfigurationExtends();
		updateLabelActivityExtends();
		labelAssignmentsForKeySequence.setEnabled(
			keySequence != null && !keySequence.getKeyStrokes().isEmpty());
		tableAssignmentsForKeySequence.setEnabled(
			keySequence != null && !keySequence.getKeyStrokes().isEmpty());
		labelAssignmentsForCommand.setEnabled(commandId != null);
		tableAssignmentsForCommand.setEnabled(commandId != null);
		boolean buttonsEnabled =
			commandId != null && keySequence != null && !keySequence.getKeyStrokes().isEmpty();
		boolean buttonAddEnabled = buttonsEnabled;
		boolean buttonRemoveEnabled = buttonsEnabled;
		boolean buttonRestoreEnabled = buttonsEnabled;
		// TODO better button enablement
		buttonAdd.setEnabled(buttonAddEnabled);
		buttonRemove.setEnabled(buttonRemoveEnabled);
		buttonRestore.setEnabled(buttonRestoreEnabled);
	}

	private void updateLabelActivityExtends() {
		String activityId = getActivityId();

		if (activityId != null) {
			IActivity activity = activityManager.getActivity(getActivityId());

			if (activity.isDefined()) {
				try {
					String name = (String) activityUniqueNamesById.get(activity.getParentId());

					if (name != null)
						labelActivityExtends.setText(MessageFormat.format(Util.translateString(RESOURCE_BUNDLE, "extends"), new Object[] { name })); //$NON-NLS-1$
					else
						labelActivityExtends.setText(Util.translateString(RESOURCE_BUNDLE, "extendsGeneral")); //$NON-NLS-1$

					return;
				} catch (org.eclipse.ui.activities.NotDefinedException eNotDefined) {
					// Do nothing
				}
			}
		}

		labelActivityExtends.setText(Util.ZERO_LENGTH_STRING);
	}

	private void updateLabelKeyConfigurationExtends() {
		String keyConfigurationId = getKeyConfigurationId();

		if (keyConfigurationId != null) {
			IKeyConfiguration keyConfiguration =
				commandManager.getKeyConfiguration(keyConfigurationId);

			try {
				String name =
					(String) keyConfigurationUniqueNamesById.get(keyConfiguration.getParentId());

				if (name != null) {
					labelKeyConfigurationExtends.setText(MessageFormat.format(Util.translateString(RESOURCE_BUNDLE, "extends"), new Object[] { name })); //$NON-NLS-1$
					return;
				}
			} catch (org.eclipse.ui.commands.NotDefinedException eNotDefined) {
				// Do nothing
			}
		}

		labelKeyConfigurationExtends.setText(Util.ZERO_LENGTH_STRING);
	}

	/*
	 * private void selectedButtonChange() { KeySequence keySequence =
	 * getKeySequence(); boolean validKeySequence = keySequence != null &&
	 * validateSequence(keySequence); String scopeId = getScopeId(); boolean
	 * validScopeId = scopeId != null && activitiesDefinitionsById.get(scopeId) !=
	 * null; String keyConfigurationId = getKeyConfigurationId(); boolean
	 * validKeyConfigurationId = keyConfigurationId != null &&
	 * keyConfigurationsById.get(keyConfigurationId) != null; if
	 * (validKeySequence && validScopeId && validKeyConfigurationId) { String
	 * commandId = null; ISelection selection =
	 * treeViewerCommands.getSelection(); if (selection instanceof
	 * IStructuredSelection && !selection.isEmpty()) { Object object =
	 * ((IStructuredSelection) selection).getFirstElement(); if (object
	 * instanceof ICommandDefinition) commandId = ((ICommandDefinition)
	 * object).getId(); } CommandRecord commandRecord =
	 * getSelectedCommandRecord(); if (commandRecord == null) set(tree,
	 * keySequence, scopeId, keyConfigurationId, commandId); else { if
	 * (!commandRecord.customSet.isEmpty()) clear(tree, keySequence, scopeId,
	 * keyConfigurationId); else set(tree, keySequence, scopeId,
	 * keyConfigurationId, null); } commandRecords.clear();
	 * buildCommandRecords(tree, commandId, commandRecords);
	 * buildTableCommand(); selectTableCommand(scopeId, keyConfigurationId,
	 * keySequence); keySequenceRecords.clear(); buildSequenceRecords(tree,
	 * keySequence, keySequenceRecords); buildTableKeySequence();
	 * selectTableKeySequence(scopeId, keyConfigurationId); update(); } }
	 * private void buildTableCommand() { tableSequencesForCommand.removeAll();
	 * for (int i = 0; i < commandRecords.size(); i++) { CommandRecord
	 * commandRecord = (CommandRecord) commandRecords.get(i); Set customSet =
	 * commandRecord.customSet; Set defaultSet = commandRecord.defaultSet; int
	 * difference = DIFFERENCE_NONE; //String commandId = null; // // boolean
	 * commandConflict = false; String alternateCommandId = null; boolean
	 * alternateCommandConflict = false; if (customSet.isEmpty()) { if
	 * (defaultSet.contains(commandRecord.command)) { //commandId // // =
	 * commandRecord.commandId; commandConflict =
	 * commandRecord.defaultConflict; } } else { if (defaultSet.isEmpty()) { if
	 * (customSet.contains(commandRecord.command)) { difference =
	 * DIFFERENCE_ADD; //commandId = // // commandRecord.commandId; // //
	 * commandConflict = commandRecord.customConflict; } } else { if
	 * (customSet.contains(commandRecord.command)) { difference =
	 * DIFFERENCE_CHANGE; //commandId = // // commandRecord.commandId;
	 * commandConflict = commandRecord.customConflict; alternateCommandId =
	 * commandRecord.defaultCommand; alternateCommandConflict =
	 * commandRecord.defaultConflict; } else { if
	 * (defaultSet.contains(commandRecord.command)) { difference =
	 * DIFFERENCE_MINUS; //commandId = // // commandRecord.commandId; // //
	 * commandConflict = commandRecord.defaultConflict; alternateCommandId =
	 * commandRecord.customCommand; alternateCommandConflict =
	 * commandRecord.customConflict; } } } } TableItem tableItem = new
	 * TableItem(tableSequencesForCommand, SWT.NULL); switch (difference) {
	 * case DIFFERENCE_ADD : tableItem.setImage(0, IMAGE_PLUS); break; case
	 * DIFFERENCE_CHANGE : tableItem.setImage(0, IMAGE_CHANGE); break; case
	 * DIFFERENCE_MINUS : tableItem.setImage(0, IMAGE_MINUS); break; case
	 * DIFFERENCE_NONE : tableItem.setImage(0, IMAGE_BLANK); break; }
	 * IActivityDefinition scope = (IActivityDefinition)
	 * activitiesById.get(commandRecord.scope); tableItem.setText(1, scope !=
	 * null ? scope.getName() : bracket(commandRecord.scope)); Configuration
	 * keyConfiguration = (Configuration)
	 * keyConfigurationsById.get(commandRecord.configuration);
	 * tableItem.setText(2, keyConfiguration != null ?
	 * keyConfiguration.getName() : bracket(commandRecord.configuration));
	 * boolean conflict = commandConflict || alternateCommandConflict;
	 * StringBuffer stringBuffer = new StringBuffer(); if
	 * (commandRecord.sequence != null)
	 * stringBuffer.append(KeySupport.formatSequence(commandRecord.sequence,
	 * true)); if (commandConflict) stringBuffer.append(SPACE +
	 * COMMAND_CONFLICT); String alternateCommandName = null; if
	 * (alternateCommandId == null) alternateCommandName = COMMAND_UNDEFINED;
	 * else if (alternateCommandId.length() == 0) alternateCommandName =
	 * COMMAND_NOTHING; else { ICommandDefinition command =
	 * (ICommandDefinition) commandsById.get(alternateCommandId); if (command !=
	 * null) alternateCommandName = command.getName(); else
	 * alternateCommandName = bracket(alternateCommandId); } if
	 * (alternateCommandConflict) alternateCommandName += SPACE +
	 * COMMAND_CONFLICT; stringBuffer.append(SPACE); if (difference ==
	 * DIFFERENCE_CHANGE)
	 * stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle,
	 * "was"), new Object[] { alternateCommandName })); //$NON-NLS-1$ else if
	 * (difference == DIFFERENCE_MINUS)
	 * stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle,
	 * "now"), new Object[] { alternateCommandName })); //$NON-NLS-1$
	 * tableItem.setText(3, stringBuffer.toString()); if (difference ==
	 * DIFFERENCE_MINUS) { if (conflict) tableItem.setForeground(new
	 * Color(getShell().getDisplay(), RGB_CONFLICT_MINUS)); else
	 * tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS)); }
	 * else if (conflict) tableItem.setForeground(new
	 * Color(getShell().getDisplay(), RGB_CONFLICT)); } } private void
	 * buildTableKeySequence() { tableCommandsForSequence.removeAll(); for (int
	 * i = 0; i < keySequenceRecords.size(); i++) { KeySequenceRecord
	 * keySequenceRecord = (KeySequenceRecord) keySequenceRecords.get(i); int
	 * difference = DIFFERENCE_NONE; String commandId = null; boolean
	 * commandConflict = false; String alternateCommandId = null; boolean
	 * alternateCommandConflict = false; if
	 * (keySequenceRecord.customSet.isEmpty()) { commandId =
	 * keySequenceRecord.defaultCommand; commandConflict =
	 * keySequenceRecord.defaultConflict; } else { commandId =
	 * keySequenceRecord.customCommand; commandConflict =
	 * keySequenceRecord.customConflict; if
	 * (keySequenceRecord.defaultSet.isEmpty()) difference = DIFFERENCE_ADD;
	 * else { difference = DIFFERENCE_CHANGE; alternateCommandId =
	 * keySequenceRecord.defaultCommand; alternateCommandConflict =
	 * keySequenceRecord.defaultConflict; } } TableItem tableItem = new
	 * TableItem(tableCommandsForSequence, SWT.NULL); switch (difference) {
	 * case DIFFERENCE_ADD : tableItem.setImage(0, IMAGE_PLUS); break; case
	 * DIFFERENCE_CHANGE : tableItem.setImage(0, IMAGE_CHANGE); break; case
	 * DIFFERENCE_MINUS : tableItem.setImage(0, IMAGE_MINUS); break; case
	 * DIFFERENCE_NONE : tableItem.setImage(0, IMAGE_BLANK); break; }
	 * IActivityDefinition scope = (IActivityDefinition)
	 * activitiesById.get(keySequenceRecord.scope); tableItem.setText(1, scope !=
	 * null ? scope.getName() : bracket(keySequenceRecord.scope));
	 * Configuration keyConfiguration = (Configuration)
	 * keyConfigurationsById.get(keySequenceRecord.configuration);
	 * tableItem.setText(2, keyConfiguration != null ?
	 * keyConfiguration.getName() : bracket(keySequenceRecord.configuration));
	 * boolean conflict = commandConflict || alternateCommandConflict;
	 * StringBuffer stringBuffer = new StringBuffer(); String commandName =
	 * null; if (commandId == null) commandName = COMMAND_UNDEFINED; else if
	 * (commandId.length() == 0) commandName = COMMAND_NOTHING; else {
	 * ICommandDefinition command = (ICommandDefinition)
	 * commandsById.get(commandId); if (command != null) commandName =
	 * command.getName(); else commandName = bracket(commandId); }
	 * stringBuffer.append(commandName); if (commandConflict)
	 * stringBuffer.append(SPACE + COMMAND_CONFLICT); String
	 * alternateCommandName = null; if (alternateCommandId == null)
	 * alternateCommandName = COMMAND_UNDEFINED; else if
	 * (alternateCommandId.length() == 0) alternateCommandName =
	 * COMMAND_NOTHING; else { ICommandDefinition command =
	 * (ICommandDefinition) commandsById.get(alternateCommandId); if (command !=
	 * null) alternateCommandName = command.getName(); else
	 * alternateCommandName = bracket(alternateCommandId); } if
	 * (alternateCommandConflict) alternateCommandName += SPACE +
	 * COMMAND_CONFLICT; stringBuffer.append(SPACE); if (difference ==
	 * DIFFERENCE_CHANGE)
	 * stringBuffer.append(MessageFormat.format(Util.getString(resourceBundle,
	 * "was"), new Object[] { alternateCommandName })); //$NON-NLS-1$
	 * tableItem.setText(3, stringBuffer.toString()); if (difference ==
	 * DIFFERENCE_MINUS) { if (conflict) tableItem.setForeground(new
	 * Color(getShell().getDisplay(), RGB_CONFLICT_MINUS)); else
	 * tableItem.setForeground(new Color(getShell().getDisplay(), RGB_MINUS)); }
	 * else if (conflict) tableItem.setForeground(new
	 * Color(getShell().getDisplay(), RGB_CONFLICT)); }
	 */

}
