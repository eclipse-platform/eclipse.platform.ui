package org.eclipse.update.internal.ui.wizards;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.parts.SWTUtil;
import org.eclipse.update.internal.ui.preferences.UpdateColors;

public class LicensePage extends WizardPage {
	private static final String KEY_TITLE = "InstallWizard.LicensePage.title"; //$NON-NLS-1$
	private static final String KEY_DESC = "InstallWizard.LicensePage.desc"; //$NON-NLS-1$
	private static final String KEY_DESC2 = "InstallWizard.LicensePage.desc2"; //$NON-NLS-1$
	private static final String KEY_HEADER = "InstallWizard.LicensePage.header"; //$NON-NLS-1$
	private static final String KEY_ACCEPT = "InstallWizard.LicensePage.accept"; //$NON-NLS-1$
	private static final String KEY_DECLINE = "InstallWizard.LicensePage.decline"; //$NON-NLS-1$
	private static final String KEY_ACCEPT2 = "InstallWizard.LicensePage.accept2"; //$NON-NLS-1$
	private static final String KEY_DECLINE2 = "InstallWizard.LicensePage.decline2"; //$NON-NLS-1$
	private boolean multiLicenseMode = false;
	private PendingChange[] jobs;
	private LicenseData[] licenses;
	private String separator;
	private StyledText styledText;
	private Button upButton;
	private Button downButton;
	private int cursor = 0;

	class LicenseData {
		IFeature feature;
		String text;
		int loc;
		int index;
		boolean addSeparator;
		StyleRange styleRange;
		public LicenseData(
			IFeature feature,
			int loc,
			int index,
			boolean addSeparator) {
			this.feature = feature;
			this.loc = loc;
			this.index = index;
			this.addSeparator = addSeparator;
			createFullText(feature.getLicense().getAnnotation());
		}
		private void createFullText(String license) {
			StringWriter swriter = new StringWriter();
			PrintWriter writer = new PrintWriter(swriter);
			String header = getHeader();
			int headerStart = loc;
			if (addSeparator) {
				writer.println(separator);
				writer.println();
				headerStart += swriter.getBuffer().toString().length();
			}
			int headerLength = header.length();
			writer.println(header);
			writer.println();
			writer.println(license);
			try {
				writer.close();
				swriter.close();
			} catch (IOException e) {
			}
			this.text = swriter.toString();
			styleRange =
				new StyleRange(
					headerStart,
					headerLength,
					UpdateColors.getTopicColor(styledText.getDisplay()),
					null,
					SWT.BOLD);
		}
		private String getHeader() {
			String label = feature.getLabel();
			String version =
				feature.getVersionedIdentifier().getVersion().toString();
			String[] args =
				new String[] { "" + index, "" + jobs.length, label, version };
			return UpdateUIPlugin.getFormattedMessage(KEY_HEADER, args);
		}
		public String getText() {
			return text;
		}
		public int getLoc() {
			return loc;
		}
		public int getLength() {
			return text.length();
		}
		public StyleRange getStyleRange() {
			return styleRange;
		}
	}

	/**
	 * Constructor for ReviewPage
	 */
	public LicensePage(boolean multiLicenseMode) {
		super("License"); //$NON-NLS-1$
		setTitle(UpdateUIPlugin.getResourceString(KEY_TITLE));
		setPageComplete(false);
		this.multiLicenseMode = multiLicenseMode;
		setDescription(
			UpdateUIPlugin.getResourceString(
				multiLicenseMode ? KEY_DESC2 : KEY_DESC));
	}
	
	public LicensePage(PendingChange job) {
		this(false);
		setJobs(new PendingChange[] {job});
	}

	public void setJobs(PendingChange[] jobs) {
		this.jobs = jobs;
	}

	/**
	 * @see DialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		if (multiLicenseMode)
			layout.numColumns = 2;
		client.setLayout(layout);
		styledText =
			new StyledText(
				client,
				SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		GridData gd = new GridData(GridData.FILL_BOTH);
		styledText.setLayoutData(gd);

		if (multiLicenseMode) {
			gd.verticalSpan = 2;
			upButton = new Button(client, SWT.PUSH);
			upButton.setText(UpdateUIPlugin.getResourceString("InstallWizard.LicensePage.up")); //$NON-NLS-1$
			upButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					cursor--;
					scrollToLicense(cursor);
					updateDirectionalButtons();
				}
			});
			gd =
				new GridData(
					GridData.VERTICAL_ALIGN_BEGINNING
						| GridData.HORIZONTAL_ALIGN_FILL);
			upButton.setLayoutData(gd);
			SWTUtil.setButtonDimensionHint(upButton);
			downButton = new Button(client, SWT.PUSH);
			downButton.setText(UpdateUIPlugin.getResourceString("InstallWizard.LicensePage.down")); //$NON-NLS-1$
			downButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					cursor++;
					scrollToLicense(cursor);
					updateDirectionalButtons();
				}
			});
			gd =
				new GridData(
					GridData.VERTICAL_ALIGN_BEGINNING
						| GridData.HORIZONTAL_ALIGN_FILL);
			downButton.setLayoutData(gd);
			SWTUtil.setButtonDimensionHint(downButton);
			updateDirectionalButtons();
		}

		Composite buttonContainer = new Composite(client, SWT.NULL);
		layout = new GridLayout();
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		if (multiLicenseMode)
			gd.horizontalSpan = 2;
		buttonContainer.setLayout(layout);
		buttonContainer.setLayoutData(gd);

		final Button acceptButton = new Button(buttonContainer, SWT.RADIO);
		acceptButton.setText(
			UpdateUIPlugin.getResourceString(
				multiLicenseMode ? KEY_ACCEPT2 : KEY_ACCEPT));
		acceptButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(acceptButton.getSelection());
			}
		});
		Button declineButton = new Button(buttonContainer, SWT.RADIO);
		declineButton.setText(
			UpdateUIPlugin.getResourceString(
				multiLicenseMode ? KEY_DECLINE2 : KEY_DECLINE));
		declineButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(acceptButton.getSelection());
			}
		});
		setControl(client);
	}
	private void updateDirectionalButtons() {
		upButton.setEnabled(cursor > 0);
		downButton.setEnabled(jobs!=null && jobs.length>0 && cursor < jobs.length - 1);
	}
	private void scrollToLicense(int index) {
		if (index < 0 || index >= jobs.length)
			return;
		LicenseData license = licenses[index];
		int offset = license.getStyleRange().start;
		styledText.setCaretOffset(offset);
		int line = styledText.getLineAtOffset(offset);
		styledText.setTopIndex(line);
	}
	
	public void setVisible(boolean visible) {
		if (visible) {
			loadLicenseText();
		}
		super.setVisible(visible);
	}
	private void loadLicenseText() {
		if (!multiLicenseMode) {
			String license = jobs[0].getFeature().getLicense().getAnnotation();
			styledText.setText(license);
			return;
		}
		licenses = new LicenseData[jobs.length];
		separator = createSeparator();
		// multi-license - must concatenate
		StringBuffer buff = new StringBuffer();
		int loc = 0;
		StyleRange[] ranges = new StyleRange[jobs.length];
		for (int i = 0; i < jobs.length; i++) {
			IFeature feature = jobs[i].getFeature();
			licenses[i] = new LicenseData(feature, loc, i + 1, i > 0);
			loc += licenses[i].getLength();
			buff.append(licenses[i].getText());
			ranges[i] = licenses[i].getStyleRange();
		}
		styledText.setText(buff.toString());
		styledText.setStyleRanges(ranges);
	}

	private String createSeparator() {
		int length = findLongestLine();
		StringBuffer sbuf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			sbuf.append('_');
		}
		return sbuf.toString();
	}
	private int findLongestLine() {
		int longestLine = 0;
		for (int i = 0; i < jobs.length; i++) {
			IFeature feature = jobs[i].getFeature();
			String license = feature.getLicense().getAnnotation();
			int length = findLongestLine(license);
			longestLine = Math.max(length, longestLine);
		}
		return longestLine;
	}
	private int findLongestLine(String license) {
		int length = 0;
		int localLength = 0;
		for (int i = 0; i < license.length(); i++) {
			char c = license.charAt(i);
			if (c == '\n') {
				length = Math.max(length, localLength);
				localLength = 0;
			} else if (c != '\r')
				localLength++;
		}
		return length;
	}
}