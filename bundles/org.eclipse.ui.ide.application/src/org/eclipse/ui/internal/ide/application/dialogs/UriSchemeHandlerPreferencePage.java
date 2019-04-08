/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     SAP SE - initial API and implementation
*******************************************************************************/
package org.eclipse.ui.internal.ide.application.dialogs;

import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UriHandlerPreferencePage_Confirm_Handle;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UriHandlerPreferencePage_Warning_OtherApp;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UriHandlerPreferencePage_Warning_OtherApp_Confirmation;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UriHandlerPreferencePage_Warning_OtherApp_Confirmation_Description;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UriHandlerPreferencePage_Warning_OtherApp_Description;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UrlHandlerPreferencePage_ColumnName_Handler;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UrlHandlerPreferencePage_ColumnName_SchemeDescription;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UrlHandlerPreferencePage_ColumnName_SchemeName;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UrlHandlerPreferencePage_Column_Handler_Text_Current_Application;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UrlHandlerPreferencePage_Column_Handler_Text_Other_Application;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UrlHandlerPreferencePage_Error_Reading_Scheme;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UrlHandlerPreferencePage_Error_Writing_Scheme;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UrlHandlerPreferencePage_Handler_Label;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UrlHandlerPreferencePage_Handler_Text_No_Application;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UrlHandlerPreferencePage_LauncherCannotBeDetermined;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UrlHandlerPreferencePage_Page_Description;
import static org.eclipse.ui.internal.ide.IDEWorkbenchMessages.UrlHandlerPreferencePage_UnsupportedOperatingSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.widgets.TableColumnFactory;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.urischeme.IOperatingSystemRegistration;
import org.eclipse.urischeme.IScheme;
import org.eclipse.urischeme.ISchemeInformation;
import org.eclipse.urischeme.IUriSchemeExtensionReader;

/**
 * This page contributes to URL handler for URISchemes in preference page of
 * General section
 *
 */
@SuppressWarnings("restriction")
public class UriSchemeHandlerPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	Text handlerLocation;
	CheckboxTableViewer tableViewer;
	private String currentLocation = null;
	IStatusManagerWrapper statusManagerWrapper = new IStatusManagerWrapper() {
	};
	IMessageDialogWrapper messageDialogWrapper = new IMessageDialogWrapper() {
	};

	IOperatingSystemRegistration operatingSystemRegistration = null;
	IUriSchemeExtensionReader extensionReader = null;
	private Composite handlerComposite;

	@SuppressWarnings("javadoc")
	public UriSchemeHandlerPreferencePage() {
		super.setDescription(UrlHandlerPreferencePage_Page_Description);
	}

	@Override
	public void init(IWorkbench workbench) {
		if (operatingSystemRegistration == null) {
			operatingSystemRegistration = IOperatingSystemRegistration.getInstance();
		}
		if (extensionReader == null) {
			extensionReader = IUriSchemeExtensionReader.newInstance();
		}
		if (operatingSystemRegistration != null) {
			currentLocation = operatingSystemRegistration.getEclipseLauncher();
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		if (PlatformUI.isWorkbenchRunning()) {
			// enabled plain JUnit tests
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
					IWorkbenchHelpContextIds.LINK_HANDLERS_PREFERENCE_PAGE);
		}
		noDefaultAndApplyButton();
		addFiller(parent);
		createTableViewerForSchemes(parent);
		createHandlerLocationControls(parent);
		if (operatingSystemRegistration == null) {
			setErrorMessage(NLS.bind(UrlHandlerPreferencePage_UnsupportedOperatingSystem,
					Platform.isRunning() ? Platform.getOS() : null)); // running check for plain JUnit tests
			tableViewer.getControl().setEnabled(false);
		} else if (currentLocation == null) {
			setErrorMessage(UrlHandlerPreferencePage_LauncherCannotBeDetermined);
			tableViewer.getControl().setEnabled(false);
		}
		return parent;
	}

	private void addFiller(Composite composite) {
		PixelConverter pixelConverter = new PixelConverter(composite);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(1) / 2;

		WidgetFactory.label(SWT.LEFT).layoutData(() -> gd).create(composite);
	}

	private void createTableViewerForSchemes(Composite parent) {
		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, true);
		gridDataFactory.span(2, 1).indent(0, SWT.DEFAULT);

		TableColumnLayout tableColumnLayout = new TableColumnLayout();

		Composite tableComposite = WidgetFactory.composite(SWT.NONE).layoutData(gridDataFactory.create())
				.layout(tableColumnLayout).create(parent);

		Table schemeTable = WidgetFactory
				.table(SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK)
				.headerVisible(true).linesVisible(true).font(parent.getFont()).create(tableComposite);

		TableColumnFactory columnFactory = WidgetFactory.tableColumn(SWT.NONE);

		TableColumn nameColumn = columnFactory.text(UrlHandlerPreferencePage_ColumnName_SchemeName).create(schemeTable);
		TableColumn descriptionColumn = columnFactory.text(UrlHandlerPreferencePage_ColumnName_SchemeDescription)
				.create(schemeTable);
		TableColumn appColumn = columnFactory.text(UrlHandlerPreferencePage_ColumnName_Handler).create(schemeTable);

		tableColumnLayout.setColumnData(nameColumn, new ColumnWeightData(20));
		tableColumnLayout.setColumnData(descriptionColumn, new ColumnWeightData(60));
		tableColumnLayout.setColumnData(appColumn, new ColumnWeightData(20));

		tableViewer = new CheckboxTableViewer(schemeTable);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(new ItemLabelProvider());

		TableSchemeSelectionListener listener = new TableSchemeSelectionListener();
		tableViewer.addSelectionChangedListener(listener);
		tableViewer.addCheckStateListener(listener);

		Collection<UiSchemeInformation> schemeInformationList = Collections.emptyList();
		// Gets the schemes from extension points for URI schemes
		try {
			schemeInformationList = retrieveSchemeInformationList();
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
					UrlHandlerPreferencePage_Error_Reading_Scheme, e);
			statusManagerWrapper.handle(status, StatusManager.BLOCK | StatusManager.LOG);
		}
		tableViewer.setInput(schemeInformationList);

		for (UiSchemeInformation schemeInformation : schemeInformationList) {
			tableViewer.setChecked(schemeInformation, schemeInformation.checked);
		}
	}

	private void createHandlerLocationControls(Composite parent) {
		handlerComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(handlerComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(handlerComposite);

		WidgetFactory.label(SWT.NONE).text(UrlHandlerPreferencePage_Handler_Label).create(handlerComposite);

		handlerLocation = WidgetFactory.text(SWT.READ_ONLY | SWT.BORDER)
				.layoutData(() -> new GridData(SWT.FILL, SWT.CENTER, true, false)).create(handlerComposite);

		handlerComposite.setVisible(false); // set visible on table selection
	}

	/**
	 * Schemes which are part of extension points for URI Schemes and are registered
	 * to operating system are consolidated here
	 *
	 * @return the supported and registered URI schemes of this instance of eclipse
	 * @throws Exception
	 */
	private Collection<UiSchemeInformation> retrieveSchemeInformationList() throws Exception {
		Collection<UiSchemeInformation> returnList = new ArrayList<>();
		Collection<IScheme> schemes = extensionReader.getSchemes();
		if (operatingSystemRegistration != null) {
			for (ISchemeInformation info : operatingSystemRegistration.getSchemesInformation(schemes)) {
				returnList.add(new UiSchemeInformation(info.isHandled(), info));
			}
		}
		return returnList;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean performOk() {
		if (operatingSystemRegistration == null || currentLocation == null) {
			return true;
		}

		List<IScheme> toAdd = new ArrayList<>();
		List<IScheme> toRemove = new ArrayList<>();
		for (UiSchemeInformation info : (Collection<UiSchemeInformation>) tableViewer.getInput()) {
			if (info.checked && !info.information.isHandled()) {
				toAdd.add(info.information);
			}
			if (!info.checked && info.information.isHandled()) {
				toRemove.add(info.information);
			}
		}

		try {
			operatingSystemRegistration.handleSchemes(toAdd, toRemove);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, 1,
					UrlHandlerPreferencePage_Error_Writing_Scheme, e);
			statusManagerWrapper.handle(status, StatusManager.BLOCK | StatusManager.LOG);
		}
		return true;
	}

	private class TableSchemeSelectionListener implements ICheckStateListener, ISelectionChangedListener {
		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.
		 * eclipse.jface.viewers.SelectionChangedEvent)
		 */
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			handleSelection();

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.
		 * jface.viewers.CheckStateChangedEvent)
		 */
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			handleCheckbox(event);

		}

		private void handleSelection() {
			IStructuredSelection selection = tableViewer.getStructuredSelection();
			Object firstElement = selection != null ? selection.getFirstElement() : null;
			if (firstElement != null && firstElement instanceof UiSchemeInformation) {
				setSchemeDetails((UiSchemeInformation) firstElement);
				handlerComposite.setVisible(true);
			}
		}

		private void setSchemeDetails(UiSchemeInformation schemeInfo) {
			if (schemeInfo.checked) {
				handlerLocation.setText(currentLocation);

			} else if (schemeIsHandledByOther(schemeInfo.information)) {
				handlerLocation.setText(schemeInfo.information.getHandlerInstanceLocation());
			} else {
				// checkbox not checked and:
				// - no other handler handles it
				// - or this eclipse handled it before (checkbox was unchecked but not yet
				// applied)
				handlerLocation.setText(UrlHandlerPreferencePage_Handler_Text_No_Application);
			}
		}

		private void handleCheckbox(CheckStateChangedEvent event) {
			UiSchemeInformation schemeInformation = (UiSchemeInformation) event.getElement();
			if (event.getChecked() && schemeIsHandledByOther(schemeInformation.information)) {
				if (operatingSystemRegistration.canOverwriteOtherApplicationsRegistration()) {
					boolean answer = messageDialogWrapper.openQuestion(getShell(),
							UriHandlerPreferencePage_Warning_OtherApp_Confirmation,
							NLS.bind(UriHandlerPreferencePage_Warning_OtherApp_Confirmation_Description,
							schemeInformation.information.getHandlerInstanceLocation(),
							schemeInformation.information.getName()));
					if (answer == false) {
						schemeInformation.checked = false;
						tableViewer.setChecked(schemeInformation, schemeInformation.checked);
						return;
					}
				} else {
					schemeInformation.checked = false;
					tableViewer.setChecked(schemeInformation, schemeInformation.checked);

					messageDialogWrapper.openWarning(getShell(), UriHandlerPreferencePage_Warning_OtherApp,
							NLS.bind(UriHandlerPreferencePage_Warning_OtherApp_Description,
									schemeInformation.information.getHandlerInstanceLocation(),
									schemeInformation.information.getName()));

					return;
				}

			}
			schemeInformation.checked = event.getChecked();
			setSchemeDetails(schemeInformation);
			tableViewer.update(schemeInformation, null);
		}
	}

	private boolean schemeIsHandledByOther(ISchemeInformation info) {
		boolean schemeIsNotHandled = !info.isHandled();
		boolean handlerLocationIsSet = info.getHandlerInstanceLocation() != null
				&& !info.getHandlerInstanceLocation().isEmpty();
		return schemeIsNotHandled && handlerLocationIsSet;
	}

	private final class ItemLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof UiSchemeInformation) {
				UiSchemeInformation schemeInfo = (UiSchemeInformation) element;
				switch (columnIndex) {
				case 0:
					return schemeInfo.information.getName();
				case 1:
					return schemeInfo.information.getDescription();
				case 2:
					String text = ""; //$NON-NLS-1$
					if (schemeInfo.checked) {
						text = UrlHandlerPreferencePage_Column_Handler_Text_Current_Application;

					} else if (schemeIsHandledByOther(schemeInfo.information)) {
						text = UrlHandlerPreferencePage_Column_Handler_Text_Other_Application;
					}
					return text;
				default:
					throw new IllegalArgumentException("Unknown column"); //$NON-NLS-1$
				}
			}
			return null; // cannot happen
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
	}

	final static class UiSchemeInformation {
		public boolean checked;
		public ISchemeInformation information;

		public UiSchemeInformation(boolean checked, ISchemeInformation information) {
			this.checked = checked;
			this.information = information;
		}
	}

	interface IStatusManagerWrapper {
		default void handle(IStatus status, int style) {
			StatusManager.getManager().handle(status, style);
		}
	}

	interface IMessageDialogWrapper {
		default void openWarning(Shell shell, String title, String message) {
			MessageDialog.openWarning(shell, title, message);
		}

		default boolean openQuestion(Shell parent, String title, String message) {
			MessageDialog dlg = new MessageDialog(parent, title, null, message, MessageDialog.CONFIRM,
					0, UriHandlerPreferencePage_Confirm_Handle, IDialogConstants.CANCEL_LABEL);
			dlg.open();
			if (dlg.getReturnCode() != IDialogConstants.OK_ID) {
				return false;
			}
			return true;
		}
	}
}
