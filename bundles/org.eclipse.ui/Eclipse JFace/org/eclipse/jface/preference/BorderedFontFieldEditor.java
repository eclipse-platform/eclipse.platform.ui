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
 * A field editor for a font type preference.
 */
public class BorderedFontFieldEditor extends FieldEditor {

	/**
	 * The change font button, or <code>null</code> if none
	 * (before creation and after disposal).
	 */
	private Button changeFontButton = null;

	/**
	 * The text for the change font button, or <code>null</code>
	 * if missing.
	 */
	private String changeButtonText;

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

	private String previewContents;

	/**
	 * Internal font previewer implementation.
	 */
	private static class DefaultPreviewer {
		private Text text;
		private String string;
		private Font font;
		public DefaultPreviewer(String s, Composite parent) {
			string = s;
			text = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
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
			return 60;
		}
	}
	/**
	 * Creates a font field editor with a preview window.
	 * 
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param previewText the text used for the preview window
	 * @param parent the parent of the field editor's control
	 */
	public BorderedFontFieldEditor(
		String name,
		String labelText,
		String previewText,
		Composite parent) {

		init(name, labelText);

		Assert.isNotNull(previewText);
		changeButtonText = JFaceResources.getString("openChange"); //$NON-NLS-1$
		previewContents = previewText;

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		GridData data =
			new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		Composite group = createGroup(composite);

		Composite innerComposite = new Composite(group, SWT.LEFT);
		data =
			new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL);
		data.grabExcessHorizontalSpace = true;
		innerComposite.setLayoutData(data);

		createControl(innerComposite);
	}

	/**
		* Create a group for encapsualting the contents.
		* @param composite Composite
		* @param store IPreferenceStore
		*/
	private Composite createGroup(Composite composite) {

		Group group = new Group(composite, SWT.SHADOW_NONE);
		group.setText(getLabelText());
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

	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	protected void adjustForNumColumns(int numColumns) {

		Control control = getPreviewControl();
		((GridData) control.getLayoutData()).horizontalSpan = 2;
		((GridData) valueControl.getLayoutData()).horizontalSpan = 1;

	}
	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	protected void applyFont() {
		if (chosenFont != null && previewer != null)
			previewer.setFont(chosenFont);
	}
	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {

		if (previewContents == null)
			getLabelControl(parent);

		valueControl = getValueControl(parent);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		valueControl.setLayoutData(gd);

		changeFontButton = getChangeControl(parent);
		gd = new GridData();
		gd.horizontalAlignment = gd.FILL;
		gd.heightHint =
			convertVerticalDLUsToPixels(changeFontButton, IDialogConstants.BUTTON_HEIGHT);
		int widthHint =
			convertHorizontalDLUsToPixels(changeFontButton, IDialogConstants.BUTTON_WIDTH);
		gd.widthHint =
			Math.max(
				widthHint,
				changeFontButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		changeFontButton.setLayoutData(gd);

		Control control = getPreviewControl(parent);
		gd = new GridData();
		gd.horizontalAlignment = gd.FILL;
		gd.horizontalSpan = numColumns;
		gd.heightHint = previewer.getPreferredHeight();
		control.setLayoutData(gd);
	}
	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	protected void doLoad() {
		if (changeFontButton == null)
			return;
		updateFont(
			PreferenceConverter.getFontData(getPreferenceStore(), getPreferenceName()));
	}
	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	protected void doLoadDefault() {
		if (changeFontButton == null)
			return;
		updateFont(
			PreferenceConverter.getDefaultFontData(
				getPreferenceStore(),
				getPreferenceName()));
	}
	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	protected void doStore() {
		PreferenceConverter.setValue(
			getPreferenceStore(),
			getPreferenceName(),
			chosenFont);

		FontData[] data = new FontData[] { chosenFont };

		JFaceResources.getFontRegistry().put(getPreferenceName(), data);
	}
	/**
	 * Returns the change button for this field editor.
	 *
	 * @return the change button
	 */
	protected Button getChangeControl(Composite parent) {
		if (changeFontButton == null) {
			changeFontButton = new Button(parent, SWT.PUSH);
			if (changeButtonText != null)
				changeFontButton.setText(changeButtonText);
			changeFontButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					FontDialog fontDialog = new FontDialog(changeFontButton.getShell());
					fontDialog.setFontData(chosenFont);
					FontData font = fontDialog.open();
					if (font != null) {
						FontData oldFont = chosenFont;
						setPresentsDefaultValue(false);
						updateFont(font);
						fireValueChanged(VALUE, oldFont, font);
					}

				}
			});
			changeFontButton.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					changeFontButton = null;
				}
			});
		} else {
			checkParent(changeFontButton, parent);
		}
		return changeFontButton;
	}
	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	public int getNumberOfControls() {
		return 2;
	}
	/**
	 * Returns the preferred preview height. 
	 *
	 * @return the height, or <code>-1</code> if no previewer
	 *  is installed
	 */
	public int getPreferredPreviewHeight() {
		return previewer.getPreferredHeight();
	}
	/**
	 * Returns the preview control for this field editor.
	 *
	 * @return the preview control
	 */
	public Control getPreviewControl(Composite parent) {
		if (previewer == null)
			previewer = new DefaultPreviewer(previewContents, parent);

		return previewer.getControl();
	}

	/**
	 * Returns the preview control for this field editor.
	 *
	 * @return the preview control
	 */
	public Control getPreviewControl() {
		return previewer.getControl();
	}

	/**
	 * Returns the value control for this field editor. The value control
	 * displays the currently selected font name.
	 *
	 * @return the value control
	 */
	protected Label getValueControl(Composite parent) {
		if (valueControl == null) {
			valueControl = new Label(parent, SWT.LEFT);
			valueControl.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					valueControl = null;
				}
			});
		} else {
			checkParent(valueControl, parent);
		}
		return valueControl;
	}
	/**
	 * Sets the text of the change button.
	 *
	 * @param text the new text
	 */
	public void setChangeButtonText(String text) {
		Assert.isNotNull(text);
		changeButtonText = text;
		changeFontButton.setText(text);
	}
	/**
	 * Updates the change font button and the previewer to reflect the
	 * newly selected font.
	 */
	private void updateFont(FontData font) {
		chosenFont = font;
		if (valueControl != null) {
			valueControl.setText(StringConverter.asString(chosenFont));
		}
		previewer.setFont(chosenFont);
	}

	/**
	* Store the default preference for the field
	* being edited
	*/
	protected void setToDefault() {
		getPreferenceStore().setToDefault(
			PreferenceConverter.localizeFontName(getPreferenceName()));
	}
}