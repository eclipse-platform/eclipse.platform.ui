package org.eclipse.jface.preference;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.preference.PreferenceConverter;
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
public class FontFieldEditor extends FieldEditor {
	
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

	/**
	 * Internal font previewer implementation.
	 */
	private static class DefaultPreviewer {
		private Text text;
		private String string;
		private Font font;
		public DefaultPreviewer(String s, Composite parent) {
			string= s;
			text= new Text(parent, SWT.READ_ONLY | SWT.BORDER);
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
			font= new Font(text.getDisplay(), fontData);
			text.setFont(font);
		}
		public int getPreferredHeight() {
			return 60;
		}
	}
/**
 * Creates a new font field editor 
 */
protected FontFieldEditor() {
}
/**
 * Creates a font field editor with a preview window.
 * 
 * @param name the name of the preference this field editor works on
 * @param labelText the label text of the field editor
 * @param previewText the text used for the preview window
 * @param parent the parent of the field editor's control
 */
public FontFieldEditor(String name, String labelText, String previewText, Composite parent) {
	this(name,labelText,parent);
	Assert.isNotNull(previewText);
	previewer = new DefaultPreviewer(previewText, parent);
	
}
/**
 * Creates a font field editor without a preview.
 * 
 * @param name the name of the preference this field editor works on
 * @param labelText the label text of the field editor
 * @param parent the parent of the field editor's control
 */
public FontFieldEditor(String name, String labelText, Composite parent) {
	init(name, labelText);
	changeButtonText = JFaceResources.getString("openChange");//$NON-NLS-1$
	createControl(parent);
}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
protected void adjustForNumColumns(int numColumns) {
	((GridData)valueControl.getLayoutData()).horizontalSpan = numColumns - 2;
	Control control = getPreviewControl();
	if (control != null) {
		((GridData)control.getLayoutData()).horizontalSpan = numColumns;
	}
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
	Control control = getLabelControl(parent);

	valueControl = getValueControl(parent);
	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	gd.horizontalSpan = numColumns - 2;
	valueControl.setLayoutData(gd);

	changeFontButton = getChangeControl(parent);
	gd = new GridData();
	gd.horizontalAlignment = gd.FILL;
	gd.heightHint = convertVerticalDLUsToPixels(changeFontButton, IDialogConstants.BUTTON_HEIGHT);
	int widthHint = convertHorizontalDLUsToPixels(changeFontButton, IDialogConstants.BUTTON_WIDTH);
	gd.widthHint = Math.max(widthHint, changeFontButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	changeFontButton.setLayoutData(gd);

	control = getPreviewControl();
	if (control != null) {
		gd = new GridData();
		gd.horizontalAlignment = gd.FILL;
		gd.horizontalSpan = numColumns;
		gd.heightHint = previewer.getPreferredHeight();
		control.setLayoutData(gd);
	}
}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
protected void doLoad() {
	if (changeFontButton == null)
		return;
	updateFont(PreferenceConverter.getFontDataArray(getPreferenceStore(), getPreferenceName()));
}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
protected void doLoadDefault() {
	if (changeFontButton == null)
		return;
	updateFont(PreferenceConverter.getDefaultFontDataArray(getPreferenceStore(), getPreferenceName()));
}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
protected void doStore() {
	PreferenceConverter.setValue(getPreferenceStore(), getPreferenceName(), chosenFont);
}
/**
 * Returns the change button for this field editor.
 *
 * @return the change button
 */
protected Button getChangeControl(Composite parent) {
	if (changeFontButton == null) {
		changeFontButton= new Button(parent, SWT.PUSH);
		if(changeButtonText != null)
			changeFontButton.setText(changeButtonText);
		changeFontButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				FontDialog fontDialog= new FontDialog(changeFontButton.getShell());
				fontDialog.setFontData(chosenFont);
				FontData font= fontDialog.open();
				if (font != null) {
					FontData oldFont= chosenFont;
					setPresentsDefaultValue(false);
					FontData[] newData = new FontData[1];
					newData[0] = font;
					updateFont(newData);
					fireValueChanged(VALUE, oldFont, font);
				}
				
			}
		});
		changeFontButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				changeFontButton= null;
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
	return 3;
}
/**
 * Returns the preferred preview height. 
 *
 * @return the height, or <code>-1</code> if no previewer
 *  is installed
 */
public int getPreferredPreviewHeight() {
	if (previewer == null)
		return -1;
	return previewer.getPreferredHeight();
}
/**
 * Returns the preview control for this field editor.
 *
 * @return the preview control
 */
public Control getPreviewControl() {
	if (previewer == null)
		return null;

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
	if (changeFontButton != null)
		changeFontButton.setText(text);
}
/**
 * Updates the change font button and the previewer to reflect the
 * newly selected font.
 */
private void updateFont(FontData font[]) {
	FontData bestFont = 
		JFaceResources.getFontRegistry().
			bestData(font,valueControl.getDisplay());

	//if we have nothing valid do as best we can
	if(bestFont == null)
		bestFont = getDefaultFontData();
		
	//Now cache this value in the receiver
	this.chosenFont = bestFont;
	
	if (valueControl != null) {
		valueControl.setText(StringConverter.asString(chosenFont));
	}
	if (previewer != null) {
		previewer.setFont(chosenFont);
	}
}
/**
 * Store the default preference for the field
 * being edited
 */
protected void setToDefault(){
	FontData[] defaultFontData = 
		PreferenceConverter.getDefaultFontDataArray(getPreferenceStore(),getPreferenceName());
	PreferenceConverter.setValue(
		getPreferenceStore(), 
		getPreferenceName(), 
		defaultFontData);
}

/**
 * Get the system default font data.
 */
private FontData getDefaultFontData(){
	return valueControl.getDisplay().getSystemFont().getFontData()[0];
}

}
