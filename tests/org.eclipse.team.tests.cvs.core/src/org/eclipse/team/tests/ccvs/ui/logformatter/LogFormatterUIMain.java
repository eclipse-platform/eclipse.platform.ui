package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * Quick and dirty UI frontend for the log formatters.
 */
public class LogFormatterUIMain {
	
	public LogFormatterUIMain() {
	}
	
	public static void main(String[] args) {
		new LogFormatterUIMain().run();
	}
	
	public void run() {
		Display display = new Display();
		Shell shell = new Shell(display);

		shell.setText("Log Formatter UI");
		createContents(shell);
		
		shell.setSize(500, 300);
		shell.open();
		while (! shell.isDisposed()) {
			if (! display.readAndDispatch()) display.sleep();
		}
		shell.dispose();
		display.dispose();
	}
	
	protected void createContents(Composite parent) {
		parent.setLayout(new FillLayout());
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		createSummaryTabContents(new TabItem(tabFolder, SWT.NONE));
		createDiffTabContents(new TabItem(tabFolder, SWT.NONE));
	}
	
	protected void createSummaryTabContents(TabItem item) {
		Composite top = new Composite(item.getParent(), SWT.NONE);
		item.setControl(top);
		item.setText("Create Log Summary");
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		top.setLayout(layout);
		
		final Text logFileText = createFileSelector(top, "Log file path: ");
		final Text outputFileText = createFileSelector(top, "Output file path: ");

		final Button csvCheckButton = new Button(top, SWT.CHECK);
		csvCheckButton.setText("Produce comma separated values data");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER);
		data.horizontalSpan = 3;
		csvCheckButton.setLayoutData(data);

		final Button rawCheckButton = new Button(top, SWT.CHECK);
		rawCheckButton.setText("Do not merge results from successive iterations");
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER);
		data.horizontalSpan = 3;
		rawCheckButton.setLayoutData(data);

		createRunButton(top, new Runnable() {
			public void run() {
				PrintSummaryMain.main(new String[] {
					logFileText.getText(),
					"-out", outputFileText.getText(),
					csvCheckButton.getSelection() ? "-csv" : null,
					rawCheckButton.getSelection() ? "-raw" : null });
			}
		});
	}
	
	protected void createDiffTabContents(TabItem item) {
		Composite top = new Composite(item.getParent(), SWT.NONE);
		item.setControl(top);
		item.setText("Create Log Diff");
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		top.setLayout(layout);
		
		final Text newerLogFileText = createFileSelector(top, "Newer log file path: ");
		final Text olderLogFileText = createFileSelector(top, "Older log file path: ");
		final Text outputFileText = createFileSelector(top, "Output file path: ");

		Label label = new Label(top, SWT.NONE);
		label.setText("Threshold %");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
		
		final Text thresholdText = new Text(top, SWT.BORDER);
		thresholdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		thresholdText.setText("0");
		
		new Label(top, SWT.NONE);

		final Button csvCheckButton = new Button(top, SWT.CHECK);
		csvCheckButton.setText("Produce comma separated values data");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER);
		data.horizontalSpan = 3;
		csvCheckButton.setLayoutData(data);
		
		final Button ignoreCheckButton = new Button(top, SWT.CHECK);
		ignoreCheckButton.setText("Ignore negligible changes in results");
		data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER);
		data.horizontalSpan = 3;
		ignoreCheckButton.setLayoutData(data);

		createRunButton(top, new Runnable() {
			public void run() {
				PrintDiffMain.main(new String[] {
					newerLogFileText.getText(),
					olderLogFileText.getText(),
					"-out", outputFileText.getText(),
					"-t", thresholdText.getText(),
					csvCheckButton.getSelection() ? "-csv" : null,
					ignoreCheckButton.getSelection() ? "-i" : null });
			}
		});
	}
	
	protected Text createFileSelector(Composite parent, String labelText) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelText);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER));
		
		final Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		
		Button browseButton = new Button(parent, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog dialog = new FileDialog(text.getShell(), SWT.OPEN);
				dialog.setFileName(text.getText());
				String name = dialog.open();
				if (name != null) {
					text.setText(name);
				}
			}
		});
		browseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		return text;
	}
	
	protected Button createRunButton(Composite parent, final Runnable runnable) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END | GridData.GRAB_VERTICAL);
		data.horizontalSpan = 3;
		separator.setLayoutData(data);
		
		final Button runButton = new Button(parent, SWT.PUSH);
		runButton.setText("Run");
		data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_FILL);
		data.horizontalSpan = 3;
		runButton.setLayoutData(data);
		runButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				runButton.getDisplay().asyncExec(runnable);
			}
		});
		return runButton;
	}
}
