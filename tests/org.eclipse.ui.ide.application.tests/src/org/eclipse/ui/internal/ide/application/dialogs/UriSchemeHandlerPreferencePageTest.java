/*******************************************************************************
* Copyright (c) 2018, 2021 SAP SE and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     SAP SE - initial API and implementation
*******************************************************************************/
package org.eclipse.ui.internal.ide.application.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.application.dialogs.UriSchemeHandlerPreferencePage.IMessageDialogWrapper;
import org.eclipse.ui.internal.ide.application.dialogs.UriSchemeHandlerPreferencePage.IStatusManagerWrapper;
import org.eclipse.ui.internal.ide.application.dialogs.UriSchemeHandlerPreferencePage.UiSchemeInformation;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.urischeme.IOperatingSystemRegistration;
import org.eclipse.urischeme.IScheme;
import org.eclipse.urischeme.ISchemeInformation;
import org.eclipse.urischeme.IUriSchemeExtensionReader;
import org.eclipse.urischeme.IUriSchemeHandler;
import org.junit.Before;
import org.junit.Test;

public class UriSchemeHandlerPreferencePageTest {
	private static final String NO_APPLICATION = IDEWorkbenchMessages.UrlHandlerPreferencePage_Handler_Text_No_Application;
	private static final String THIS_ECLIPSE_HANDLER_LOCATION = "/this/eclipse";
	private static final String OTHER_ECLIPSE_HANDLER_LOCATION = "/other/Eclipse";

	private final IScheme noAppScheme = new SchemeStub("hello", "helloScheme");
	private final ISchemeInformation noAppSchemeInfo = new SchemeInformationStub(noAppScheme, false, null);

	private final IScheme thisAppScheme = new SchemeStub("hello1", "hello1Scheme");
	private final ISchemeInformation thisAppSchemeInfo = new SchemeInformationStub(thisAppScheme, true,
			THIS_ECLIPSE_HANDLER_LOCATION);

	private final IScheme otherAppScheme = new SchemeStub("hello2", "hello2Scheme");
	private final ISchemeInformation otherAppSchemeInfo = new SchemeInformationStub(otherAppScheme, false,
			OTHER_ECLIPSE_HANDLER_LOCATION);

	private UriSchemeHandlerPreferencePage page;
	private OperatingSystemRegistrationMock operatingSystemRegistration;
	private MessageDialogWrapperSpy messageDialogSpy;

	@Before
	public void setup() {
		this.page = createStandalonePreferencePage();
	}

	@SuppressWarnings("cast")
	@Test
	public void handlerControlIsText() {
		this.page.createContents(this.page.getShell());

		assertTrue(this.page.handlerLocation instanceof Text);
		assertEquals(SWT.READ_ONLY, this.page.handlerLocation.getStyle() & SWT.READ_ONLY);
	}

	@Test
	public void schemesShown() throws Exception {
		this.page.createContents(this.page.getShell());
		waitForJob();
		assertScheme(getTableItem(0), false, noAppSchemeInfo);
		assertScheme(getTableItem(1), true, thisAppSchemeInfo);
		assertScheme(getTableItem(2), false, otherAppSchemeInfo);
	}

	@Test
	public void handlerTextShown() throws Exception {
		this.page.createContents(this.page.getShell());
		waitForJob();
		assertHandlerTextForSelection(page, 0, NO_APPLICATION);
		assertHandlerTextForSelection(page, 1, THIS_ECLIPSE_HANDLER_LOCATION);
		assertHandlerTextForSelection(page, 2, OTHER_ECLIPSE_HANDLER_LOCATION);
	}

	@Test
	public void checkNoAppScheme() throws Exception {
		this.page.createContents(this.page.getShell());
		waitForJob();

		clickTableViewerCheckbox(0, true);

		assertScheme(getTableItem(0), true, noAppSchemeInfo);
	}

	@Test
	public void uncheckThisAppScheme() throws Exception {
		this.page.createContents(this.page.getShell());
		waitForJob();

		clickTableViewerCheckbox(1, false);

		assertScheme(getTableItem(1), false, thisAppSchemeInfo);
	}

	@Test
	public void checkOtherAppSchemeGivesWarningAndRevertsClick() throws Exception {
		this.page.createContents(this.page.getShell());
		waitForJob();

		clickTableViewerCheckbox(2, true);

		MessageDialogWrapperSpy spy = (MessageDialogWrapperSpy) page.messageDialogWrapper;

		assertEquals(IDEWorkbenchMessages.UriHandlerPreferencePage_Warning_NotPossible, spy.title);

		String expected = NLS.bind(IDEWorkbenchMessages.UriHandlerPreferencePage_Warning_OtherApp_Description,
				OTHER_ECLIPSE_HANDLER_LOCATION, "hello2");
		assertEquals(expected, spy.message);

		assertScheme(getTableItem(2), false, otherAppSchemeInfo);
		assertHandlerTextForSelection(page, 2, OTHER_ECLIPSE_HANDLER_LOCATION);
	}

	@Test
	public void checkSchemeOnRegistrationNotSupportedGivesWarningAndRevertsClick() throws Exception {
		this.page.createContents(this.page.getShell());
		waitForJob();
		operatingSystemRegistration.supportsRegistration = false;

		clickTableViewerCheckbox(0, true);

		MessageDialogWrapperSpy spy = (MessageDialogWrapperSpy) page.messageDialogWrapper;
		assertEquals(IDEWorkbenchMessages.UriHandlerPreferencePage_Warning_NotPossible, spy.title);
		String expected = NLS.bind(IDEWorkbenchMessages.UrlHandlerPreferencePage_RegistrationUnsupported,
				OTHER_ECLIPSE_HANDLER_LOCATION, "hello");
		assertEquals(expected, spy.message);
		assertScheme(getTableItem(0), false, noAppSchemeInfo);
	}

	@Test
	public void checkOtherAppSchemeOnWindowsIsAllowed() throws Exception {
		this.page.createContents(this.page.getShell());
		waitForJob();

		operatingSystemRegistration.canOverwriteOtherApplicationsRegistration = true;
		messageDialogSpy.actualAnswer = true;

		clickTableViewerCheckbox(2, true);

		MessageDialogWrapperSpy spy = (MessageDialogWrapperSpy) page.messageDialogWrapper;

		assertEquals(IDEWorkbenchMessages.UriHandlerPreferencePage_Warning_OtherApp_Confirmation, spy.title);
		String expected = NLS.bind(
				IDEWorkbenchMessages.UriHandlerPreferencePage_Warning_OtherApp_Confirmation_Description,
				OTHER_ECLIPSE_HANDLER_LOCATION, "hello2");
		assertEquals(expected, spy.message);

		assertScheme(getTableItem(2), true, otherAppSchemeInfo);
		assertHandlerTextForSelection(page, 2, THIS_ECLIPSE_HANDLER_LOCATION);
	}

	@Test
	public void checkOtherAppSchemeOnWindowsIsAllowedButNothingChangesWhenUserSaysNo() throws Exception {
		this.page.createContents(this.page.getShell());
		waitForJob();

		operatingSystemRegistration.canOverwriteOtherApplicationsRegistration = true;
		messageDialogSpy.actualAnswer = false;

		clickTableViewerCheckbox(2, true);

		MessageDialogWrapperSpy spy = (MessageDialogWrapperSpy) page.messageDialogWrapper;

		assertEquals(IDEWorkbenchMessages.UriHandlerPreferencePage_Warning_OtherApp_Confirmation, spy.title);
		String expected = NLS.bind(
				IDEWorkbenchMessages.UriHandlerPreferencePage_Warning_OtherApp_Confirmation_Description,
				OTHER_ECLIPSE_HANDLER_LOCATION, "hello2");
		assertEquals(expected, spy.message);

		assertScheme(getTableItem(2), false, otherAppSchemeInfo);
		assertHandlerTextForSelection(page, 2, OTHER_ECLIPSE_HANDLER_LOCATION);
	}

	@Test
	public void registersSchemesInOperatingSystemOnApply() throws Exception {
		this.page.createContents(this.page.getShell());
		waitForJob();
		clickTableViewerCheckbox(0, true);
		clickTableViewerCheckbox(1, false);
		page.performOk();

		OperatingSystemRegistrationMock mock = (OperatingSystemRegistrationMock) page.operatingSystemRegistration;
		assertEquals(1, mock.addedSchemes.size());
		assertEquals("hello", mock.addedSchemes.iterator().next().getName());

		assertEquals(1, mock.removedSchemes.size());
		assertEquals("hello1", mock.removedSchemes.iterator().next().getName());
	}

	@Test
	public void doesNotRegistersSchemesInOperatingSystemOnCancel() throws Exception {
		this.page.createContents(this.page.getShell());
		waitForJob();

		clickTableViewerCheckbox(0, true);
		clickTableViewerCheckbox(1, false);
		page.performCancel();

		OperatingSystemRegistrationMock mock = (OperatingSystemRegistrationMock) page.operatingSystemRegistration;
		assertEquals(0, mock.addedSchemes.size());

		assertEquals(0, mock.removedSchemes.size());
	}

	@Test
	public void doesNotRegistersSchemesInOperatingSystemOnApplyWhenLoading() throws Exception {
		this.page.createContents(this.page.getShell());
		waitForJob();
		// tableItem[1] (hello1 scheme) is true (refer to members), but page is still
		// loading
		assertNotNull("Page is null", page);
		page.performOk();

		OperatingSystemRegistrationMock mock = (OperatingSystemRegistrationMock) page.operatingSystemRegistration;
		assertNotNull("mock is null", mock);

		assertNotNull("addedSchemes is null", mock.addedSchemes);
		assertEquals(0, mock.addedSchemes.size());

		assertNotNull("removedSchemes is null", mock.removedSchemes);
		assertEquals(0, mock.removedSchemes.size());
		waitForJob();
	}

	@Test
	public void showsErrorOnOperatingSystemRegistrationReadError() throws Exception {
		OperatingSystemRegistrationMock mock = (OperatingSystemRegistrationMock) page.operatingSystemRegistration;
		mock.schemeInformationReadException = new IOExceptionWithoutStackTrace("Error reading from OS");

		this.page.createContents(this.page.getShell());
		waitForJob();

		assertErrorStatusRaised(IDEWorkbenchMessages.UrlHandlerPreferencePage_Error_Reading_Scheme);
	}

	@Test
	public void showsErrorOnOperatingSystemRegistrationWriteError() throws Exception {
		OperatingSystemRegistrationMock mock = (OperatingSystemRegistrationMock) page.operatingSystemRegistration;
		mock.schemeInformationRegisterException = new IOExceptionWithoutStackTrace("Error writing into OS");

		this.page.createContents(this.page.getShell());
		waitForJob();

		page.performOk();

		assertErrorStatusRaised(IDEWorkbenchMessages.UrlHandlerPreferencePage_Error_Writing_Scheme);
	}

	@Test
	public void doesNothingOnUnkownOperatingSystem() {
		// reset operationSystemRegistration, like it was not initialized due to unknown
		// OS
		page.operatingSystemRegistration = null;

		this.page.createContents(this.page.getShell());

		assertFalse(page.tableViewer.getControl().getEnabled());
		assertTrue(((Collection<?>) page.tableViewer.getInput()).isEmpty());

		assertTrue(page.performOk());
	}

	@Test
	public void doesNothingIfEclipseLauncherPathCannotBeDetermined() {
		operatingSystemRegistration.launcherPath = null;

		this.page.init(null);

		this.page.createContents(this.page.getShell());

		assertFalse(page.tableViewer.getControl().getEnabled());
		assertNotNull(page.getErrorMessage());

		assertTrue(page.performOk());
	}

	@Test
	public void loadingSchemesShownAfterPageOpened() throws Exception {
		this.page.createContents(this.page.getShell());
		assertLoadingScheme(getTableItem(0), noAppSchemeInfo);
		assertLoadingScheme(getTableItem(1), thisAppSchemeInfo);
		assertLoadingScheme(getTableItem(2), otherAppSchemeInfo);
		waitForJob();
	}

	private void waitForJob() throws InterruptedException {
		this.page.osRegistrationReadingJob.join();
		// jobs sets data asynchronously in TableViewer
		Display display = Display.getCurrent();
		while (display.readAndDispatch()) {
		}
	}

	private void clickTableViewerCheckbox(int itemIndex, boolean checked) {
		TableItem item = page.tableViewer.getTable().getItem(itemIndex);

		Event event = new Event();
		item.setChecked(checked);
		event.item = item;
		event.widget = page.tableViewer.getTable();
		event.display = event.item.getDisplay();
		event.detail = SWT.CHECK;

		page.tableViewer.handleSelect(new SelectionEvent(event));
	}

	private void assertScheme(TableItem tableItem, boolean checked, ISchemeInformation information) {
		// check pojo
		UiSchemeInformation uiInformation = (UiSchemeInformation) tableItem.getData();
		assertEquals(checked, uiInformation.isChecked());
		assertEquals(information.getName(), uiInformation.getName());
		assertEquals(information.getDescription(), uiInformation.getDescription());
		assertEquals(information.getHandlerInstanceLocation(), uiInformation.getHandlerInstanceLocation());

		// check UI
		assertEquals(checked, tableItem.getChecked());
		assertEquals(information.getName(), tableItem.getText(0));
		assertEquals(information.getDescription(), tableItem.getText(1));
		if (checked) {
			assertEquals(IDEWorkbenchMessages.UrlHandlerPreferencePage_Column_Handler_Text_Current_Application,
					tableItem.getText(2));
		} else if (!information.isHandled() && information.getHandlerInstanceLocation() != null) {
			assertEquals(IDEWorkbenchMessages.UrlHandlerPreferencePage_Column_Handler_Text_Other_Application,
					tableItem.getText(2));
		} else {
			assertEquals("", tableItem.getText(2));
		}
	}

	private void assertLoadingScheme(TableItem tableItem, ISchemeInformation information) {
		// check pojo
		UiSchemeInformation uiInformation = (UiSchemeInformation) tableItem.getData();
		assertFalse(uiInformation.isChecked());
		assertEquals(information.getName(), uiInformation.getName());
		assertEquals(information.getDescription(), uiInformation.getDescription());
		assertEquals(IDEWorkbenchMessages.UrlHandlerPreferencePage_LoadingText,
				uiInformation.getHandlerInstanceLocation());

		// check UI
		assertFalse(tableItem.getChecked());
		assertEquals(information.getName(), tableItem.getText(0));
		assertEquals(information.getDescription(), tableItem.getText(1));
		assertEquals(IDEWorkbenchMessages.UrlHandlerPreferencePage_LoadingText, tableItem.getText(2));
	}

	private void assertHandlerTextForSelection(UriSchemeHandlerPreferencePage page, int selection, String text) {
		page.tableViewer.setSelection(new StructuredSelection(getTableItem(selection).getData()));
		assertEquals(text, page.handlerLocation.getText());
	}

	private void assertErrorStatusRaised(String expectedMessage) {
		StatusManagerWrapperSpy spy = (StatusManagerWrapperSpy) page.statusManagerWrapper;

		assertEquals(IStatus.ERROR, spy.handledStatus.getSeverity());
		assertEquals(expectedMessage, spy.handledStatus.getMessage());
		assertEquals(StatusManager.BLOCK | StatusManager.LOG, spy.style);
	}

	private TableItem getTableItem(int item) {
		return page.tableViewer.getTable().getItems()[item];
	}

	private UriSchemeHandlerPreferencePage createStandalonePreferencePage() {

		UriSchemeHandlerPreferencePage page = new UriSchemeHandlerPreferencePage() {
			@Override
			public Shell getShell() {
				return new Shell();
			}
		};

		page.extensionReader = createExtensionReaderStub();
		operatingSystemRegistration = createOperatingSystemMock();
		page.operatingSystemRegistration = operatingSystemRegistration;

		page.statusManagerWrapper = new StatusManagerWrapperSpy();
		messageDialogSpy = new MessageDialogWrapperSpy();
		page.messageDialogWrapper = messageDialogSpy;

		page.init(null);

		return page;
	}

	private OperatingSystemRegistrationMock createOperatingSystemMock() {
		return new OperatingSystemRegistrationMock(
				Arrays.asList(noAppSchemeInfo, thisAppSchemeInfo, otherAppSchemeInfo));
	}

	private ExtensionReaderStub createExtensionReaderStub() {
		return new ExtensionReaderStub(Arrays.asList(noAppScheme, thisAppScheme, otherAppScheme));
	}

	private static final class StatusManagerWrapperSpy implements IStatusManagerWrapper {
		public IStatus handledStatus;
		public int style;

		@Override
		public void handle(IStatus status, int style) {
			handledStatus = status;
			this.style = style;
		}
	}

	private static final class MessageDialogWrapperSpy implements IMessageDialogWrapper {

		public String title;
		public String message;
		public boolean actualAnswer = false;

		@Override
		public void openWarning(Shell shell, String title, String message) {
			this.title = title;
			this.message = message;
		}

		@Override
		public boolean openQuestion(Shell parent, String title, String message) {
			this.title = title;
			this.message = message;
			return actualAnswer;
		}
	}

	private static final class ExtensionReaderStub implements IUriSchemeExtensionReader {
		public Collection<IScheme> schemes;

		public ExtensionReaderStub(Collection<IScheme> schemes) {
			this.schemes = schemes;
		}

		@Override
		public Collection<IScheme> getSchemes() {
			return schemes;
		}

		@Override
		public IUriSchemeHandler getHandlerFromExtensionPoint(String uriScheme) {
			return null;
		}
	}

	private static class SchemeStub implements IScheme {

		private final String name;
		private final String description;

		public SchemeStub(String name, String description) {
			super();
			this.name = name;
			this.description = description;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return description;
		}
	}

	private static class SchemeInformationStub implements ISchemeInformation {

		private final IScheme scheme;
		private final boolean handled;
		private final String handlerInstanceLocation;

		public SchemeInformationStub(IScheme scheme, boolean handled, String handlerInstanceLocation) {
			this.scheme = scheme;
			this.handled = handled;
			this.handlerInstanceLocation = handlerInstanceLocation;
		}

		@Override
		public String getName() {
			return scheme.getName();
		}

		@Override
		public boolean isHandled() {
			return handled;
		}

		@Override
		public String getHandlerInstanceLocation() {
			return handlerInstanceLocation;
		}

		@Override
		public String getDescription() {
			return scheme.getDescription();
		}
	}

	private static final class OperatingSystemRegistrationMock implements IOperatingSystemRegistration {

		private final List<ISchemeInformation> schemeInformations;
		public Exception schemeInformationReadException = null;
		public Exception schemeInformationRegisterException = null;
		public Collection<IScheme> addedSchemes = Collections.emptyList();
		public Collection<IScheme> removedSchemes = Collections.emptyList();
		public boolean canOverwriteOtherApplicationsRegistration = false;
		public String launcherPath = THIS_ECLIPSE_HANDLER_LOCATION;
		public boolean supportsRegistration = true;

		public OperatingSystemRegistrationMock(List<ISchemeInformation> schemeInformations) {
			this.schemeInformations = schemeInformations;
		}

		@Override
		public void handleSchemes(Collection<IScheme> toAdd, Collection<IScheme> toRemove) throws Exception {
			if (schemeInformationRegisterException != null) {
				throw schemeInformationRegisterException;
			}
			this.addedSchemes = toAdd;
			this.removedSchemes = toRemove;
		}

		@Override
		public List<ISchemeInformation> getSchemesInformation(Collection<IScheme> schemes) throws Exception {
			if (schemeInformationReadException != null) {
				throw schemeInformationReadException;
			}
			return schemeInformations;
		}

		@Override
		public String getEclipseLauncher() {
			return launcherPath;
		}

		@Override
		public boolean canOverwriteOtherApplicationsRegistration() {
			return canOverwriteOtherApplicationsRegistration;
		}

		@Override
		public boolean supportsRegistration() {
			return supportsRegistration;
		}

	}

	private static class IOExceptionWithoutStackTrace extends IOException {

		private static final long serialVersionUID = 1L;

		public IOExceptionWithoutStackTrace(String message) {
			super(message);
		}

		@Override
		public synchronized Throwable fillInStackTrace() {
			return null;
		}
	}
}