package org.eclipse.team.internal.ccvs.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class HistoryFilterDialog extends Dialog {

	private boolean dateEntered;
	private HistoryView historyView;
	private HistoryFilter historyFilter;	
	
	//widgets
	private Button orRadio;
	private Button andRadio;
	private Combo fromDayCombo;
	private Combo toDayCombo;
	private Combo fromMonthCombo;
	private Combo toMonthCombo;
	private Combo fromYearCombo;
	private Combo toYearCombo;
	private Text author;
	private Text comment;

	public HistoryFilterDialog(HistoryView view) {
		super(view.getViewSite().getShell());
		this.historyView = view;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Policy.bind("HistoryFilterDialog.title")); //$NON-NLS-1$
	}

	protected Control createDialogArea(Composite parent) {
		Composite topLevel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		topLevel.setLayout(layout);
		
		//"and" and "or" search radio buttons
		Label label = new Label(topLevel, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(Policy.bind("HistoryFilterDialog.showMatching")); //$NON-NLS-1$
		
		andRadio = new Button(topLevel, SWT.RADIO);
		andRadio.setText(Policy.bind("HistoryFilterDialog.matchingAll")); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		andRadio.setLayoutData(data);
		andRadio.setSelection(true);
		
		orRadio = new Button(topLevel, SWT.RADIO);
		orRadio.setText(Policy.bind("HistoryFilterDialog.matchingAny")); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		orRadio.setLayoutData(data);
		
		//author
		label = new Label(topLevel, SWT.NONE);
		label.setText(Policy.bind("HistoryFilterDialog.author")); //$NON-NLS-1$
		author = new Text(topLevel, SWT.BORDER);
		author.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		//comment
		label = new Label(topLevel, SWT.NONE);
		label.setText(Policy.bind("HistoryFilterDialog.comment")); //$NON-NLS-1$
		comment = new Text(topLevel, SWT.BORDER);
		comment.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		//"from" date
		label = new Label(topLevel, SWT.NONE);
		label.setText(Policy.bind("HistoryFilterDialog.fromDate")); //$NON-NLS-1$
		Composite fdComposite = new Composite(topLevel, SWT.NONE);
		GridLayout fdLayout = new GridLayout();
		fdLayout.numColumns = 3;
		fdComposite.setLayout(fdLayout);
		fromMonthCombo = new Combo(fdComposite, SWT.READ_ONLY);
		fromDayCombo = new Combo(fdComposite, SWT.READ_ONLY);
		fromYearCombo = new Combo(fdComposite, SWT.NONE);
		fromYearCombo.setTextLimit(4);

		//"to" date	
		label = new Label(topLevel, SWT.NONE);
		label.setText(Policy.bind("HistoryFilterDialog.toDate")); //$NON-NLS-1$
		Composite tdComposite = new Composite(topLevel, SWT.NONE);
		GridLayout tdLayout = new GridLayout();
		tdLayout.numColumns = 3;
		tdComposite.setLayout(tdLayout);
		toMonthCombo = new Combo(tdComposite, SWT.READ_ONLY);
		toDayCombo = new Combo(tdComposite, SWT.READ_ONLY);
		toYearCombo = new Combo(tdComposite, SWT.NONE);
		toYearCombo.setTextLimit(4);

		//set day, month and year combos with numbers
		//years allows a selection from the past 5 years
		//or any year written in
		String days[] = new String[31];
		for (int i = 0; i < 31; i++) {
			days[i] = String.valueOf(i + 1);
		}

		String months[] = new String[12];
		SimpleDateFormat format = new SimpleDateFormat("MMMM"); //$NON-NLS-1$
		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < 12; i++) {
			calendar.set(Calendar.MONTH, i);
			months[i] = format.format(calendar.getTime());
		}

		String years[] = new String[5];
		Calendar calender = Calendar.getInstance();
		for (int i = 0; i < 5; i++) {
			years[i] = String.valueOf(calender.get(1) - i);
		}
		fromDayCombo.setItems(days);
		toDayCombo.setItems(days);
		fromMonthCombo.setItems(months);
		toMonthCombo.setItems(months);
		fromYearCombo.setItems(years);
		toYearCombo.setItems(years);
		fromYearCombo.select(0);
		toYearCombo.select(0);

		initializeValues();
		return topLevel;
	}
	void initializeValues() {
		if (historyFilter == null) return;
		if (historyFilter.author != null) {
			author.setText(historyFilter.author);
		}
		if (historyFilter.comment != null) {
			comment.setText(historyFilter.comment);
		}
		orRadio.setSelection(historyFilter.isOr);
		andRadio.setSelection(!historyFilter.isOr);
		Calendar calendar = Calendar.getInstance();
		if (historyFilter.fromDate != null) {
			calendar.setTime(historyFilter.fromDate);
			fromDayCombo.select(calendar.get(Calendar.DATE) - 1);
			fromMonthCombo.select(calendar.get(Calendar.MONTH));
			String yearValue = String.valueOf(calendar.get(Calendar.YEAR));
			int index = fromYearCombo.indexOf(yearValue);
			if (index == -1) {
				fromYearCombo.add(yearValue);
				index = fromYearCombo.indexOf(yearValue);
			}
			fromYearCombo.select(index);
		}
		if (historyFilter.toDate != null) {
			calendar.setTime(historyFilter.toDate);
			toDayCombo.select(calendar.get(Calendar.DATE) - 1);
			toMonthCombo.select(calendar.get(Calendar.MONTH));
			String yearValue = String.valueOf(calendar.get(Calendar.YEAR));
			int index = toYearCombo.indexOf(yearValue);
			if (index == -1) {
				toYearCombo.add(yearValue);
				index = toYearCombo.indexOf(yearValue);
			}
			toYearCombo.select(index);
		}
	}
	/**
	 * A button has been pressed.  Process the dialog contents.
	 */
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.CANCEL_ID == buttonId) {
			super.buttonPressed(buttonId);
			return;
		}
		Date fromDate = null, toDate = null;

		if ((fromMonthCombo.getSelectionIndex() >= 0)
			&& (toMonthCombo.getSelectionIndex() >= 0)
			&& (fromDayCombo.getSelectionIndex() >= 0)
			&& (toDayCombo.getSelectionIndex() >= 0)
			&& (fromYearCombo.getText().length() > 0)
			&& (toYearCombo.getText().length() > 0)) {

			//set the calander with the user input
			//set the hours, minutes and seconds to 00
			//so as to cover the whole day
			Calendar calendar = Calendar.getInstance();
			calendar.set(
				Integer.parseInt(String.valueOf(fromYearCombo.getText())),
				fromMonthCombo.getSelectionIndex(),
				Integer.parseInt(String.valueOf(fromDayCombo.getText())),
				00, 00, 00);
			fromDate = calendar.getTime();

			//set the calander with the user input
			//set the hours, minutes and seconds to 23, 59, 59
			//so as to cover the whole day
			calendar.set(
				Integer.parseInt(String.valueOf(toYearCombo.getText())),
				toMonthCombo.getSelectionIndex(),
				Integer.parseInt(String.valueOf(toDayCombo.getText())),
				23, 59, 59);
			toDate = calendar.getTime();
		}

		//create the filter
		historyFilter = new HistoryFilter(
			historyView,
			author.getText(),
			comment.getText(),
			fromDate,
			toDate,
			orRadio.getSelection());
				
		super.buttonPressed(buttonId);
	}

	/**
	 * Returns the filter that was created from the provided
	 * user input.
	 */
	public HistoryFilter getFilter() {
		return historyFilter;
	}
	/**
	 * Set the intial value of the dialog to the given filter.
	 */
	public void setFilter(HistoryFilter filter) {
		this.historyFilter = filter;
	}
}