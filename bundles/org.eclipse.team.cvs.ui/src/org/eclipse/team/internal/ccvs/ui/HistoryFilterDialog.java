/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - bug 13100
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

public class HistoryFilterDialog extends Dialog {

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
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(CVSUIMessages.HistoryFilterDialog_title); 
	}

	protected Control createDialogArea(Composite parent) {
		Composite topLevel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		topLevel.setLayout(layout);
		
		//"and" and "or" search radio buttons
		Label label = new Label(topLevel, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setText(CVSUIMessages.HistoryFilterDialog_showMatching); 
		
		andRadio = new Button(topLevel, SWT.RADIO);
		andRadio.setText(CVSUIMessages.HistoryFilterDialog_matchingAll); 
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		andRadio.setLayoutData(data);
		andRadio.setSelection(true);
		
		orRadio = new Button(topLevel, SWT.RADIO);
		orRadio.setText(CVSUIMessages.HistoryFilterDialog_matchingAny); 
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		orRadio.setLayoutData(data);
		
		//author
		label = new Label(topLevel, SWT.NONE);
		label.setText(CVSUIMessages.HistoryFilterDialog_author); 
		author = new Text(topLevel, SWT.BORDER);
		author.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		//comment
		label = new Label(topLevel, SWT.NONE);
		label.setText(CVSUIMessages.HistoryFilterDialog_comment); 
		comment = new Text(topLevel, SWT.BORDER);
		comment.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		//"from" date
		label = new Label(topLevel, SWT.NONE);
		label.setText(CVSUIMessages.HistoryFilterDialog_fromDate); 
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
		label.setText(CVSUIMessages.HistoryFilterDialog_toDate); 
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
		String days[] = new String[32];
		days[0] = "---"; //$NON-NLS-1$
		for (int i = 1; i < 32; i++) {
			days[i] = String.valueOf(i);
		}

		String months[] = new String[13];
		months[0] = "---"; //$NON-NLS-1$
		SimpleDateFormat format = new SimpleDateFormat("MMMM"); //$NON-NLS-1$
		Calendar calendar = Calendar.getInstance();
		for (int i = 1; i < 13; i++) {
			calendar.set(Calendar.MONTH, i - 1);
			months[i] = format.format(calendar.getTime());
		}

		String years[] = new String[5];
		Calendar calender = Calendar.getInstance();
		for (int i = 0; i < 5; i++) {
			years[i] = String.valueOf(calender.get(1) - i);
		}
		fromDayCombo.setItems(days);
		fromDayCombo.select(0);
		toDayCombo.setItems(days);
		toDayCombo.select(0);
		fromMonthCombo.setItems(months);
		fromMonthCombo.select(0);
		toMonthCombo.setItems(months);
		toMonthCombo.select(0);
		fromYearCombo.setItems(years);
		toYearCombo.setItems(years);
		fromYearCombo.select(0);
		toYearCombo.select(0);

		initializeValues();
		
		// set F1 help
        PlatformUI.getWorkbench().getHelpSystem().setHelp(topLevel, IHelpContextIds.HISTORY_FILTER_DIALOG);
        Dialog.applyDialogFont(parent);
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
			fromDayCombo.select(calendar.get(Calendar.DATE));
			fromMonthCombo.select(calendar.get(Calendar.MONTH) + 1);
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
			toDayCombo.select(calendar.get(Calendar.DATE));
			toMonthCombo.select(calendar.get(Calendar.MONTH) + 1);
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

        boolean fromSet=
            (fromDayCombo.getSelectionIndex() > 0)
                && (fromMonthCombo.getSelectionIndex() > 0);
        boolean toSet=
            (toDayCombo.getSelectionIndex() > 0)
                && (toMonthCombo.getText().length() > 0);
        
        if (fromSet || toSet) {
            Calendar calendar = Calendar.getInstance();
            fromDate = getFromDate(calendar, fromSet);            
            toDate = getToDate(calendar, toSet);
        }

        //create the filter
        historyFilter = new HistoryFilter(
            author.getText(),
            comment.getText(),
            fromDate,
            toDate,
            orRadio.getSelection());
                
        super.buttonPressed(buttonId);
    }

    //either user input or the smallest date available
    private Date getFromDate(Calendar calendar, boolean fromSet) {
        if (fromSet) {
            calendar.set(Calendar.YEAR, Integer.parseInt(String.valueOf(fromYearCombo.getText())));
            calendar.set(Calendar.MONTH, fromMonthCombo.getSelectionIndex() - 1);
            calendar.set(Calendar.DATE, Integer.parseInt(String.valueOf(fromDayCombo.getText())));
        } else {
            calendar.set(Calendar.YEAR, Integer.parseInt(String.valueOf(fromYearCombo.getItem(fromYearCombo.getItemCount() - 1))));
            calendar.set(Calendar.MONTH, 0);
            calendar.set(Calendar.DATE, 1);
        }

        //set the hours, minutes and seconds to 00
        //so as to cover the whole day
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    //either user input or today
    private Date getToDate(Calendar calendar, boolean toSet) {
        if (toSet) { 
            calendar.set(Calendar.YEAR, Integer.parseInt(String.valueOf(toYearCombo.getText())));
            calendar.set(Calendar.MONTH, toMonthCombo.getSelectionIndex() - 1);
            calendar.set(Calendar.DATE, Integer.parseInt(String.valueOf(toDayCombo.getText())));
        } else
            calendar.setTimeInMillis(System.currentTimeMillis());

        //set the hours, minutes and seconds to 23, 59, 59
        //so as to cover the whole day
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
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
