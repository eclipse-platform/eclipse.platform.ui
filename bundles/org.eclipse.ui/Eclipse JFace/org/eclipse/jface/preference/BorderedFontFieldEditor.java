package org.eclipse.jface.preference;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * A field editor for a font type preference. This is not a standard
 * field editor in that it is not a simple row of widgets hence it
 * is not in the field editor hierarchy.
 */
public class BorderedFontFieldEditor {

	/**
	 * The preference store we will be manipulating.
	 */
	private IPreferenceStore preferenceStore;

	/** 
	 * The preference name we are using.
	 */
	private String preferenceName;

	/**
	 * Font data for the chosen font button, or <code>null</code> if none.
	 */
	private FontData chosenFont;

	/**
	 * The label that displays the selected font, or <code>null</code> if none.
	 */
	private Label valueControl;

	/**
	 * The previewer, or <code>null</code> if none.
	 */
	private DefaultPreviewer previewer;

	/**
	 * Internal font previewer implementation.
	 */
	private static class DefaultPreviewer {
		private Text text;
		private String string;
		private Font font;
		public DefaultPreviewer(String s, Composite parent) {
			string = s;
			text = new Text(parent, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP);
			text.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (font != null)
						font.dispose();
				}
			});
			if (string != null)
				text.setText(string);
		}

		public Control getControl() {
			return text;
		}

		public void setFont(FontData fontData) {
			if (font != null)
				font.dispose();
			font = new Font(text.getDisplay(), fontData);
			text.setFont(font);
		}
		public int getPreferredHeight() {
			return 30;
		}
	}
	/**
	 * Creates a font field editor with a preview window.
	 * 
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param previewText the text used for the preview window
	 * @param changeButtonLabel the text used for the change button
	 * @param parent the parent of the field editor's control
	 * @param store the preference store used for accessing and setting values
	 */
	public BorderedFontFieldEditor(
		String name,
		String labelText,
		String previewText,
		String changeButtonLabel,
		Composite parent,
		IPreferenceStore store) {

		this.preferenceStore = store;
		this.preferenceName = name;
		chosenFont = PreferenceConverter.getFontData(preferenceStore, preferenceName);

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		GridData data =
			new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		Composite group = createGroup(composite, labelText);

		Composite innerComposite = new Composite(group, SWT.LEFT);
		data =
			new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
		data.grabExcessHorizontalSpace = true;
		innerComposite.setLayoutData(data);

		createControl(innerComposite, previewText, changeButtonLabel);
	}

	/**
	* Creates this field editor's main control containing all of its
	* basic controls.
	*/
	private void createControl(
		Composite parent, 
		String previewText,
		String changeButtonLabel) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		parent.setLayout(layout);
		
		createValueControl(parent);
		createChangeControl(parent,changeButtonLabel);
		createPreviewControl(parent, previewText);
	}

	/**
	 * Creates a group for encapsulating the contents.
	 */
	private Composite createGroup(Composite composite, String labelText) {

		Group group = new Group(composite, SWT.SHADOW_NONE);
		group.setText(labelText);
		//GridLayout
		group.setLayout(new GridLayout());
		//GridData
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		group.setLayoutData(data);

		return group;
	}


	/**
	 * Gets the preference store we are using
	 */
	private IPreferenceStore getPreferenceStore() {
		return this.preferenceStore;
	}

	/**
	 * Loads the default value for the editor
	 */
	public void loadDefault() {
		updateFont(
			PreferenceConverter.getDefaultFontData(getPreferenceStore(), preferenceName));
	}
	/**
	 * Stores the current value.
	 */
	public void store() {
		PreferenceConverter.setValue(preferenceStore, preferenceName, chosenFont);
	}
	/**
	 * Creates the change button for this field editor.=
	 */
	private void createChangeControl(Composite parent, String changeButtonLabel) {
		final Button changeFontButton = new Button(parent, SWT.PUSH);

		changeFontButton.setText(changeButtonLabel); //$NON-NLS-1$
		changeFontButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				FontDialog fontDialog = new FontDialog(changeFontButton.getShell());
				fontDialog.setFontData(chosenFont);
				FontData font = fontDialog.open();
				if (font != null) {
					updateFont(font);
				}

			}
		});
	}
	
	/**
	 * Creates the preview control for this field editor.
	 */
	private void createPreviewControl(Composite parent, String previewContents) {
		previewer = new DefaultPreviewer(previewContents, parent);
		previewer.setFont(chosenFont);
		
		Control control = previewer.getControl();
		GridData gd = new GridData();
		gd.horizontalAlignment = gd.FILL;
		gd.horizontalSpan = 2;
		gd.heightHint = previewer.getPreferredHeight();
		control.setLayoutData(gd);
	}

	/**
	 * Creates the value control for this field editor. The value control
	 * displays the currently selected font name.
	 */
	private void createValueControl(Composite parent) {
		valueControl = new Label(parent, SWT.LEFT);

		valueControl.setText(StringConverter.asString(this.chosenFont));

		valueControl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				valueControl = null;
			}
		});

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		valueControl.setLayoutData(gd);
	}

	/**
	 * Updates the value label and the previewer to reflect the
	 * newly selected font.
	 */
	private void updateFont(FontData font) {
		chosenFont = font;
		valueControl.setText(StringConverter.asString(chosenFont));
		previewer.setFont(chosenFont);
	}
}