/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Lars Vogel <Lars.Vogel@vogella.com> - Bug 472690
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * The PlainMessageDialog represents a message dialog with a clear, simple API
 * to create dialogs with message, buttons and image.
 *
 * <p>
 * Instances of this class can be created using the {@link Builder}. An instance
 * of the builder can be retrieved by calling the static method
 * {@link #getBuilder(Shell, String)}
 * </p>
 *
 * <p>
 * This class has to be favored over {@link MessageDialog}, which has an
 * evolved, non-clear API.
 * </p>
 *
 * @since 3.23
 *
 */
public class PlainMessageDialog extends IconAndMessageDialog {

	private final String title;
	private final Image titleImage;
	private final Image image;
	private final List<String> buttonLabels;
	private final int defaultButtonIndex;
	private Control customArea;

	/**
	 * The Builder to create PlainMessageDialog instances. It has a fluent API
	 * (every method returns the same builder instance).
	 *
	 * @since 3.23
	 *
	 */
	public static class Builder {
		private Shell shell;
		private String dialogTitle;
		private Image titleImage;
		private Image image;
		private String message;
		private List<String> buttonLabels = Arrays.asList(IDialogConstants.OK_LABEL);
		private int defaultButtonIndex = 0;

		private Builder(Shell shell, String dialogTitle) {
			this.shell = shell;
			this.dialogTitle = dialogTitle;
		}

		/**
		 * Sets the shell's image.
		 *
		 * @see Shell#setImage(Image)
		 * @param image the image
		 * @return this
		 */
		public Builder titleImage(Image image) {
			this.titleImage = image;
			return this;
		}

		/**
		 * Sets the dialog's image (e.g. information icon).
		 *
		 * @param image the image
		 * @return this
		 */
		public Builder image(Image image) {
			this.image = image;
			return this;
		}

		/**
		 * Sets the dialog's message.
		 *
		 * @param message the message
		 * @return this
		 */
		public Builder message(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Sets the dialog's button labels. Without calling
		 * {@link #defaultButtonIndex(int)} the first entry is used for the default
		 * button.
		 * <p>
		 * {@link List#of(Object...)} can be used to call this method in a handy way.
		 * </p>
		 *
		 * @param buttonLabels the button labels
		 * @return this
		 */
		public Builder buttonLabels(List<String> buttonLabels) {
			this.buttonLabels = buttonLabels;
			return this;
		}

		/**
		 * Sets another (other than 0) button as default button.
		 *
		 * @param defaultButtonIndex the default button index
		 * @return this
		 */
		public Builder defaultButtonIndex(int defaultButtonIndex) {
			this.defaultButtonIndex = defaultButtonIndex;
			return this;
		}

		/**
		 * Create the dialog with all the parameters set in the builder.
		 *
		 * @return the PlainMessageDialog instance
		 */
		public PlainMessageDialog build() {
			return new PlainMessageDialog(this);
		}
	}

	/**
	 * Creates a new Builder instance.
	 *
	 * @param shell       the parent shell
	 * @param dialogTitle the shell title
	 * @return the builder
	 */
	public static Builder getBuilder(Shell shell, String dialogTitle) {
		return new Builder(shell, dialogTitle);
	}

	private PlainMessageDialog(Builder builder) {
		super(builder.shell);

		this.title = builder.dialogTitle;
		this.titleImage = builder.titleImage;
		this.image = builder.image;
		this.message = builder.message;
		this.buttonLabels = builder.buttonLabels;
		this.defaultButtonIndex = builder.defaultButtonIndex;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		setReturnCode(buttonId);
		close();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
		if (titleImage != null) {
			shell.setImage(titleImage);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		for (int id = 0; id < buttonLabels.size(); id++) {
			createButton(parent, id, buttonLabels.get(id), defaultButtonIndex == id);
		}
	}

	/**
	 * Creates and returns the contents of an area of the dialog which appears below
	 * the message and above the button bar.
	 * <p>
	 * The default implementation of this framework method returns
	 * <code>null</code>. Subclasses may override.
	 * </p>
	 *
	 * @param parent parent composite to contain the custom area
	 * @return the custom area control, or <code>null</code>
	 */
	protected Control createCustomArea(Composite parent) {
		Label dummyLabelForSpacingPurposes = new Label(parent, SWT.NULL);
		return dummyLabelForSpacingPurposes;
	}

	/**
	 * This implementation of the <code>Dialog</code> framework method creates and
	 * lays out a composite and calls <code>createMessageArea</code> and
	 * <code>createCustomArea</code> to populate it. Subclasses should override
	 * <code>createCustomArea</code> to add contents below the message.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// create message area
		createMessageArea(parent);
		// create the top level composite for the dialog area
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		composite.setLayoutData(data);
		// allow subclasses to add custom controls
		customArea = createCustomArea(composite);
		return composite;
	}

	/**
	 * Handle the shell close. Set the return code to <code>SWT.DEFAULT</code> as
	 * there has been no explicit close by the user.
	 *
	 * @see org.eclipse.jface.window.Window#handleShellCloseEvent()
	 */
	@Override
	protected void handleShellCloseEvent() {
		// Sets a return code of SWT.DEFAULT since none of the dialog buttons
		// were pressed to close the dialog.
		super.handleShellCloseEvent();
		setReturnCode(SWT.DEFAULT);
	}

	/**
	 * Open
	 *
	 * @param style {@link SWT#NONE} for a default dialog, or {@link SWT#SHEET} for
	 *              a dialog with sheet behavior
	 * @return <code>true</code> if the user presses the OK or Yes button,
	 *         <code>false</code> otherwise
	 * @since 3.23
	 */
	public int open(int style) {
		style &= SWT.SHEET;
		this.setShellStyle(this.getShellStyle() | style);
		return this.open();
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		Button button = super.createButton(parent, id, label, defaultButton);
		// Be sure to set the focus if the custom area cannot so as not
		// to lose the defaultButton.
		if (defaultButton && !customShouldTakeFocus()) {
			button.setFocus();
		}
		return button;
	}

	/**
	 * Return whether or not we should apply the workaround where we take focus for
	 * the default button or if that should be determined by the dialog. By default
	 * only return true if the custom area is a label or CLabel that cannot take
	 * focus.
	 *
	 * @return boolean
	 */
	private boolean customShouldTakeFocus() {
		if (customArea instanceof Label) {
			return false;
		}
		if (customArea instanceof CLabel) {
			return (customArea.getStyle() & SWT.NO_FOCUS) > 0;
		}
		return true;
	}

	@Override
	protected Image getImage() {
		return image;
	}
}