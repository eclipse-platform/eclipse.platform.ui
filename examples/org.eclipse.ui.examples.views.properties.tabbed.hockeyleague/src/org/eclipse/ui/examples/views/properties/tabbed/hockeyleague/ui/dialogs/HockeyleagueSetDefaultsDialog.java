/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.dialogs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Arena;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.DefencePositionKind;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ForwardPositionKind;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.League;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.WeightKind;

/**
 * A defaults dialog that allows for data entry for the Tabbed Properties View
 * Hockey League Example.
 * 
 * @author Anthony Hunter
 */
public class HockeyleagueSetDefaultsDialog
	extends Dialog {

	private class DefaultValue {

		protected Method method;

		protected String label;

		protected Object value;

		protected Object widget;

		protected DefaultValue(Method method) {
			this.method = method;
			label = method.getName().substring(3) + ":";//$NON-NLS-1$
		}

		public String toString() {
			return method.toString();
		}
	}

	private EObject owner = null;

	private EObject child = null;

	private ArrayList defaultValues = null;

	public HockeyleagueSetDefaultsDialog(Shell parentShell,
			AddCommand addCommand) {
		super(parentShell);
		Collection collection = addCommand.getCollection();
		this.owner = addCommand.getOwner();
		this.child = (EObject) collection.iterator().next();
		this.defaultValues = getDefaultValues(child);
	}

	public HockeyleagueSetDefaultsDialog(Shell parentShell,
			SetCommand setCommand) {
		super(parentShell);
		this.owner = setCommand.getOwner();
		this.child = (EObject) setCommand.getValue();
		this.defaultValues = getDefaultValues(child);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Add New " + child.eClass().getName()); //$NON-NLS-1$
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Group group = createGroup(composite, child.eClass().getName(), 2);
		for (Iterator i = defaultValues.iterator(); i.hasNext();) {
			DefaultValue defaultValue = (DefaultValue) i.next();
			Label label = createLabel(group, defaultValue.label);
			label.setToolTipText(""); //$NON-NLS-1$
			Class setType = defaultValue.method.getParameterTypes()[0];
			if (setType.equals(String.class)) {
				defaultValue.widget = createTextField(group);
				((Text) defaultValue.widget).setText(defaultValue.method
					.getName().substring(3));
			} else if (setType.equals(int.class)) {
				defaultValue.widget = createTextField(group);
				((Text) defaultValue.widget).setText("0");//$NON-NLS-1$
			} else if (setType.equals(float.class)) {
				defaultValue.widget = createTextField(group);
				((Text) defaultValue.widget).setText("0.0F");//$NON-NLS-1$
			} else if (setType.equals(Boolean.class)) {
				defaultValue.widget = createCheckBox(group, "Yes"); //$NON-NLS-1$
			} else if (setType.equals(DefencePositionKind.class)) {
				defaultValue.widget = createCombo(group, new String[] {
					DefencePositionKind.LEFT_DEFENCE_LITERAL.getName(),
					DefencePositionKind.RIGHT_DEFENCE_LITERAL.getName()});
			} else if (setType.equals(ForwardPositionKind.class)) {
				defaultValue.widget = createCombo(group, new String[] {
					ForwardPositionKind.LEFT_WING_LITERAL.getName(),
					ForwardPositionKind.RIGHT_WING_LITERAL.getName(),
					ForwardPositionKind.CENTER_LITERAL.getName()});
			} else if (setType.equals(WeightKind.class)) {
				defaultValue.widget = createCombo(group, new String[] {
					WeightKind.POUNDS_LITERAL.getName(),
					WeightKind.KILOGRAMS_LITERAL.getName()});
			} else if (setType.equals(ShotKind.class)) {
				defaultValue.widget = createCombo(group, new String[] {
					ShotKind.LEFT_LITERAL.getName(),
					ShotKind.RIGHT_LITERAL.getName()});
			} else if (setType.equals(HeightKind.class)) {
				defaultValue.widget = createCombo(group, new String[] {
					HeightKind.INCHES_LITERAL.getName(),
					HeightKind.CENTIMETERS_LITERAL.getName()});
			} else if (setType.equals(Team.class)) {
				League league = (League) owner.eResource().getContents().get(0);
				Team[] teams = (Team[]) league.getTeams().toArray();
				String[] teamNames = new String[teams.length];
				for (int t = 0; t < teams.length; t++) {
					teamNames[t] = teams[t].getName();
				}
				defaultValue.widget = createCombo(group, teamNames);
			} else {
				defaultValue.widget = createLabel(group, "N/A");//$NON-NLS-1$
			}
		}
		return composite;
	}

	protected Group createGroup(Composite parent, String text, int numColumns) {
		Group composite = new Group(parent, SWT.NONE);
		composite.setText(text);
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.makeColumnsEqualWidth = false;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		return composite;
	}

	protected Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}

	protected Text createTextField(Composite parent) {
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		data.widthHint = 250;
		text.setLayoutData(data);
		return text;
	}

	protected Button createCheckBox(Composite group, String label) {
		Button button = new Button(group, SWT.CHECK | SWT.LEFT);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	protected Combo createCombo(Composite parent, String[] items) {
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setFont(parent.getFont());
		combo.setItems(items);
		combo.select(0);
		return combo;
	}

	protected void okPressed() {
		for (Iterator i = defaultValues.iterator(); i.hasNext();) {
			DefaultValue defaultValue = (DefaultValue) i.next();
			Class setType = defaultValue.method.getParameterTypes()[0];
			if (setType.equals(String.class)) {
				defaultValue.value = ((Text) defaultValue.widget).getText();
			} else if (setType.equals(int.class)) {
				String text = ((Text) defaultValue.widget).getText();
				defaultValue.value = Integer.valueOf(text);
			} else if (setType.equals(float.class)) {
				String text = ((Text) defaultValue.widget).getText();
				defaultValue.value = Float.valueOf(text);
			} else if (setType.equals(DefencePositionKind.class)) {
				switch (((Combo) defaultValue.widget).getSelectionIndex()) {
					case DefencePositionKind.LEFT_DEFENCE:
						defaultValue.value = DefencePositionKind.LEFT_DEFENCE_LITERAL;
						break;
					case DefencePositionKind.RIGHT_DEFENCE:
						defaultValue.value = DefencePositionKind.RIGHT_DEFENCE_LITERAL;
						break;
				}
			} else if (setType.equals(ForwardPositionKind.class)) {
				switch (((Combo) defaultValue.widget).getSelectionIndex()) {
					case ForwardPositionKind.LEFT_WING:
						defaultValue.value = ForwardPositionKind.LEFT_WING_LITERAL;
						break;
					case ForwardPositionKind.CENTER:
						defaultValue.value = ForwardPositionKind.CENTER_LITERAL;
						break;
					case ForwardPositionKind.RIGHT_WING:
						defaultValue.value = ForwardPositionKind.RIGHT_WING_LITERAL;
						break;
				}
			} else if (setType.equals(WeightKind.class)) {
				switch (((Combo) defaultValue.widget).getSelectionIndex()) {
					case WeightKind.KILOGRAMS:
						defaultValue.value = WeightKind.KILOGRAMS_LITERAL;
						break;
					case WeightKind.POUNDS:
						defaultValue.value = WeightKind.POUNDS_LITERAL;
						break;
				}
			} else if (setType.equals(ShotKind.class)) {
				switch (((Combo) defaultValue.widget).getSelectionIndex()) {
					case ShotKind.LEFT:
						defaultValue.value = ShotKind.LEFT_LITERAL;
						break;
					case ShotKind.RIGHT:
						defaultValue.value = ShotKind.RIGHT_LITERAL;
						break;
				}
			} else if (setType.equals(HeightKind.class)) {
				switch (((Combo) defaultValue.widget).getSelectionIndex()) {
					case HeightKind.CENTIMETERS:
						defaultValue.value = HeightKind.CENTIMETERS_LITERAL;
						break;
					case HeightKind.INCHES:
						defaultValue.value = HeightKind.INCHES_LITERAL;
						break;
				}
			} else if (setType.equals(Team.class)) {
				League league = (League) owner.eResource().getContents().get(0);
				Team[] teams = (Team[]) league.getTeams().toArray();
				defaultValue.value = teams[((Combo) defaultValue.widget)
					.getSelectionIndex()];
			} else if (setType.equals(Boolean.class)) {
				defaultValue.value = ((Button) defaultValue.widget)
					.getSelection() ? Boolean.TRUE
					: Boolean.FALSE;
			}
		}
		setDefaultValues(defaultValues, child);
		super.okPressed();
	}

	private ArrayList getDefaultValues(EObject aChild) {
		ArrayList ret = new ArrayList();
		Class childClassImpl = aChild.getClass();
		Method[] methods = childClassImpl.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().startsWith("set")) {//$NON-NLS-1$
				Class setType = methods[i].getParameterTypes()[0];
				if (!setType.equals(Class.class)
					&& !setType.equals(Arena.class)
					&& !setType.equals(EList.class)) {
					ret.add(new DefaultValue(methods[i]));
				}
			}
		}
		return ret;
	}

	private void setDefaultValues(ArrayList defaultValues, EObject child) {
		for (Iterator i = defaultValues.iterator(); i.hasNext();) {
			DefaultValue defaultValue = (DefaultValue) i.next();
			try {
				defaultValue.method.invoke(child,
					new Object[] {defaultValue.value});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}