/*******************************************************************************
 * Copyright (c) 2006, 2012 Soyatec (http://www.soyatec.com) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Soyatec - initial API and implementation
 *     IBM Corporation - ongoing enhancements
 *******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.project;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.internal.ui.wizards.IProjectProvider;
import org.eclipse.pde.internal.ui.wizards.plugin.AbstractFieldData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.branding.IProductConstants;

/**
 * @author jin.liu (jin.liu@soyatec.com)
 */
public class NewApplicationWizardPage extends WizardPage {
	public static final String E4_APPLICATION = "org.eclipse.e4.ui.workbench.swt.E4Application";
	public static final String APPLICATION_CSS_PROPERTY = "applicationCSS";
	public static final String PRODUCT_NAME = "productName";
	public static final String APPLICATION = "application";

	private final Map<String, String> data;

	private IProject project;
	private IProjectProvider projectProvider;
	private Text proNameText;
	private Text proApplicationText;
	private Group propertyGroup;
	private AbstractFieldData pluginData;

	private PropertyData[] PROPERTIES;

	protected NewApplicationWizardPage(IProjectProvider projectProvider, AbstractFieldData pluginData) {
		super("New Eclipse 4 Application Wizard Page");
		this.projectProvider = projectProvider;
		this.pluginData = pluginData;
		data = new HashMap<String, String>();
		setTitle("Eclipse 4 Application");
		setMessage("Configure application with special values.");
	}

	public IProject getProject() {
		if (project == null && projectProvider != null) {
			project = projectProvider.getProject();
		}
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		Group productGroup = createProductGroup(control);
		productGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		propertyGroup = createPropertyGroup(control);
		propertyGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setControl(control);
	}

	static class PropertyData {
		private String name;
		private String label;

		private String value;
		private Class<?> type;
		private boolean editable;

		public PropertyData(String name, String label, String value, Class<?> type,
				boolean editable) {
			this.name = name;
			this.value = value;
			this.label = label;
			this.type = type;
			this.editable = editable;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public Class<?> getType() {
			return type;
		}

		public boolean isEditable() {
			return editable;
		}
		
		public String getLabel() {
			return label;
		}
	}

	private Group createPropertyGroup(Composite control) {
		Group group = new Group(control, SWT.NONE);
		group.setText("Properties");

		group.setLayout(new GridLayout(3, false));

		return group;
	}

	private void createPropertyItem(final Composite parent,
			final PropertyData property) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(property.getLabel());
		label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		label.setToolTipText("Property \"" + property.getName() + "\"");
		
		final Text valueText = new Text(parent, SWT.BORDER);
		valueText.setText(property.getValue());
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		valueText.setLayoutData(gridData);
		if (!property.isEditable()) {
			valueText.setEditable(false);
		}
		valueText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				handleTextEvent(property.getName(), valueText);
			}
		});

		if (property.getType() == Color.class
				|| property.getType() == Rectangle.class) {
			Button button = new Button(parent, SWT.PUSH);
			button.setText("...");
			button.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					handleLinkEvent(property, valueText, parent.getShell());
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		else {
			new Label(parent, SWT.NONE);
		}
		data.put(property.getName(), property.getValue());
	}

	private void handleLinkEvent(PropertyData property, Text valueText,
			Shell shell) {
		if (property == null || valueText == null || valueText.isDisposed()) {
			return;
		}
		if (property.getType() == Color.class) {
			ColorDialog colorDialog = new ColorDialog(shell);
			RGB selectRGB = colorDialog.open();
			if (selectRGB != null) {
				valueText.setText((this.hexColorConvert(Integer
						.toHexString(selectRGB.blue))
						+ this.hexColorConvert(Integer
								.toHexString(selectRGB.green)) + this
						.hexColorConvert(Integer.toHexString(selectRGB.red)))
						.toUpperCase());
			}
		} else if (property.getType() == Rectangle.class) {
			this.createRectDialog(shell, valueText).open();
		}
	}

	/**
	 * exchange the color pattern of hex numeric
	 * 
	 * @param number
	 * @return
	 */
	public String hexColorConvert(String color) {
		if (color.length() == 1) {
			return "0" + color;
		}
		return color;
	}

	/**
	 * create Rect Set dialog
	 * 
	 * @param parent
	 * @param valueText
	 * @return
	 */
	public Dialog createRectDialog(final Composite parent, final Text valueText) {
		return new Dialog(parent.getShell()) {
			Text xPointText, yPointText, widthText, heightText;

			@Override
			protected Button createButton(Composite parent, int id,
					String label, boolean defaultButton) {
				return super.createButton(parent, id, label, defaultButton);
			}

			@Override
			protected Control createDialogArea(final Composite parent) {
				Composite composite = (Composite) super
						.createDialogArea(parent);
				composite.getShell().setText("Set Rect");
				Group group = new Group(composite, SWT.NONE);
				group.setText("Rect");
				GridLayout gridLayout = new GridLayout();
				gridLayout.numColumns = 4;
				group.setLayout(gridLayout);

				Label xPointLabel = new Label(group, SWT.NONE);
				xPointLabel.setText("X:");
				xPointText = new Text(group, SWT.BORDER);
				VerifyListener verifyListener = createVerifyListener(parent
						.getShell());
				xPointText.addVerifyListener(verifyListener);
				Label yPointLabel = new Label(group, SWT.NONE);
				yPointLabel.setText("Y:");
				yPointText = new Text(group, SWT.BORDER);
				yPointText.addVerifyListener(verifyListener);
				Label widthLabel = new Label(group, SWT.NONE);
				widthLabel.setText("Width:");
				widthText = new Text(group, SWT.BORDER);
				widthText.addVerifyListener(verifyListener);
				Label heighttLabel = new Label(group, SWT.NONE);
				heighttLabel.setText("Height:");
				heightText = new Text(group, SWT.BORDER);
				heightText.addVerifyListener(verifyListener);

				return composite;
			}

			@Override
			protected void buttonPressed(int buttonId) {
				if (IDialogConstants.OK_ID == buttonId) {
					String xPoint = xPointText.getText();
					String yPoint = yPointText.getText();
					String width = widthText.getText();
					String height = heightText.getText();
					if (xPoint.length() == 0 || yPoint.length() == 0
							|| width.length() == 0 || height.length() == 0) {
						MessageDialog.openWarning(parent.getShell(),
								"Input value empty",
								"Value shoud not be empty!");
					} else {
						valueText.setText(xPoint + "," + yPoint + "," + width
								+ "," + height);
						okPressed();
					}
				} else if (IDialogConstants.CANCEL_ID == buttonId) {
					cancelPressed();
				}
			}
		};
	}

	/**
	 * create verify Listener
	 * 
	 * @param shell
	 * @return
	 */
	public VerifyListener createVerifyListener(final Shell shell) {
		return new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				char c = e.character;
				if ("0123456789".indexOf(c) == -1) {
					e.doit = false;
					MessageDialog.openWarning(shell, "Input value error",
							"Only numeric is allowed!");
					return;
				}
			}
		};
	}

	private void handleTextEvent(String property, Text valueText) {
		if (property == null || valueText == null || valueText.isDisposed()) {
			return;
		}
		String value = valueText.getText();
		if (value.equals("")) {
			value = null;
		}
		data.put(property, value);
	}

	private Group createProductGroup(Composite control) {
		Group proGroup = new Group(control, SWT.NONE);
		proGroup.setText("Product");

		proGroup.setLayout(new GridLayout(2, false));

		Label proNameLabel = new Label(proGroup, SWT.NONE);
		proNameLabel.setText("Name:*");

		proNameText = new Text(proGroup, SWT.BORDER);
		proNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		proNameText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				handleTextEvent(PRODUCT_NAME, proNameText);
			}
		});

		Label proApplicationLabel = new Label(proGroup, SWT.NONE);
		proApplicationLabel.setText("Application:");

		proApplicationText = new Text(proGroup, SWT.BORDER);
		proApplicationText
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		proApplicationText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				handleTextEvent(APPLICATION, proApplicationText);
			}
		});
		return proGroup;
	}

	protected PropertyData[] getPropertyData() {
		if (PROPERTIES == null) {
			PROPERTIES = new PropertyData[] {
					new PropertyData(IProductConstants.APP_NAME, "Application Name:",
							projectProvider.getProjectName(), String.class,
							true),
					new PropertyData(APPLICATION_CSS_PROPERTY, "CSS Style:",
							"css/default.css", String.class, true),
					new PropertyData(IProductConstants.ABOUT_TEXT, "About Message:", "",
							String.class, true),
					new PropertyData(
							IProductConstants.STARTUP_FOREGROUND_COLOR, "Startup Foreground:", "",
							Color.class, false),
					new PropertyData(IProductConstants.STARTUP_MESSAGE_RECT, "Startup Message Region:",
							"", Rectangle.class, false),
					new PropertyData(IProductConstants.STARTUP_PROGRESS_RECT, "Startup Progress Region:",
							"", Rectangle.class, false),
					new PropertyData(
							IProductConstants.PREFERENCE_CUSTOMIZATION, "Preference Customization:", "",
							String.class, true) }; // plugin_customization.ini
		}
		return PROPERTIES;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible && PROPERTIES == null) {
			
			// Use the plug-in name for the product name (not project name which can contain illegal characters)
			proNameText.setText(pluginData.getId());

			proApplicationText.setText(E4_APPLICATION);

			for (PropertyData property : getPropertyData()) {
				createPropertyItem(propertyGroup, property);
			}
			propertyGroup.getParent().layout();
		}
		super.setVisible(visible);
	}

	/**
	 * @return the data
	 */
	public Map<String, String> getData() {
		if (PROPERTIES == null) {
			for (PropertyData property : getPropertyData()) {
				data.put(property.getName(), property.getValue());
			}
			
			// Use the plug-in name for the product name (not project name which can contain illegal characters)
			String productName = pluginData.getId();
			
			data.put(PRODUCT_NAME, productName);
			data.put(APPLICATION, E4_APPLICATION);
		}
		Map<String, String> map = new HashMap<String, String>();
		map.putAll(data);
		return map;
	}
}
