/*******************************************************************************
 * Copyright (c) 2009 Siemens AG and others.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Kai Tödter - initial implementation
 ******************************************************************************/

package org.eclipse.e4.demo.contacts.views;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.demo.contacts.model.Contact;
import org.eclipse.e4.ui.model.application.MDirtyable;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class DetailsView {

	private final DetailComposite detailComposite;

	private final MDirtyable dirtyable;

	@Inject
	private EHandlerService handlerService;

	@Inject
	private ECommandService commandService;

	public DetailsView(Composite parent, MDirtyable dirtyable) {
		detailComposite = new DetailComposite(dirtyable, parent, SWT.NONE);
		this.dirtyable = dirtyable;

		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}
	
	public boolean isSaveOnCloseNeeded() {
		return true;
	}

	public void doSave(@Optional IProgressMonitor monitor) throws IOException,
			InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("Saving contact details to vCard...", 16);

		Contact originalContact = detailComposite.getOriginalContact();
		Contact modifiedContact = detailComposite.getModifiedContact();
		saveAsVCard(modifiedContact, modifiedContact.getSourceFile());

		originalContact.setCity(modifiedContact.getCity());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setCompany(modifiedContact.getCompany());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setCountry(modifiedContact.getCountry());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setEmail(modifiedContact.getEmail());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setFirstName(modifiedContact.getFirstName());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setJobTitle(modifiedContact.getJobTitle());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setLastName(modifiedContact.getLastName());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setMiddleName(modifiedContact.getMiddleName());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setMobile(modifiedContact.getMobile());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setNote(modifiedContact.getNote());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setPhone(modifiedContact.getPhone());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setState(modifiedContact.getState());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setStreet(modifiedContact.getStreet());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setTitle(modifiedContact.getTitle());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setWebPage(modifiedContact.getWebPage());
		Thread.sleep(50);
		monitor.worked(1);

		originalContact.setZip(modifiedContact.getZip());
		Thread.sleep(50);
		monitor.worked(1);

		monitor.done();
		dirtyable.setDirty(false);
	}

	private String getName(Contact contact, String charSet) {
		StringBuilder builder = new StringBuilder();
		builder.append("N;").append(charSet).append(':'); //$NON-NLS-1$
		builder.append(contact.getLastName()).append(';');
		builder.append(contact.getFirstName()).append(';');
		builder.append(contact.getMiddleName());

		String title = contact.getTitle();
		if (title.length() != 0) {
			builder.append(';').append(title);
		}

		builder.append('\n');
		return builder.toString();
	}

	private void saveAsVCard(Contact contact, String fileName)
			throws IOException {
		String charSet = "CHARSET=" + Charset.defaultCharset().name();
		String vCard = "BEGIN:VCARD" + "\nVERSION:2.1" + "\n"
				+ getName(contact, charSet) + "FN;" + charSet + ":"
				+ contact.getFirstName() + " " + contact.getLastName()
				+ "\nORG;" + charSet + ":" + contact.getCompany() + "\nTITLE:"
				+ contact.getJobTitle() + "\nNOTE:" + contact.getNote()
				+ "\nTEL;WORK;VOICE:" + contact.getPhone()
				+ "\nTEL;CELL;VOICE:" + contact.getMobile() + "\nADR;WORK;"
				+ charSet + ":" + ";;" + contact.getStreet() + ";"
				+ contact.getCity() + ";" + contact.getState() + ";"
				+ contact.getZip() + ";" + contact.getCountry() + "\nURL;WORK:"
				+ contact.getWebPage() + "\nEMAIL;PREF;INTERNET:"
				+ contact.getEmail() + "\n";

		if (!contact.getJpegString().equals("")) {
			vCard += "PHOTO;TYPE=JPEG;ENCODING=BASE64:\n "
					+ contact.getJpegString() + "\n";
		}

		vCard += "END:VCARD\n";

		PrintWriter out = new PrintWriter(fileName);
		out.println(vCard);
		out.close();
	}
	
	@Inject
	IStylingEngine engine;

	private void applyDialogStyles(Control control) {
		if (engine != null) {
			Shell shell = control.getShell();
			if (shell.getBackgroundMode() == SWT.INHERIT_NONE) {
				shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
			}

			engine.style(shell);
		}
	}

	@Inject
	public void setSelection(@Named("selection") Contact selection) {
		if (selection != null) {
			if (dirtyable.isDirty()) {
				MessageDialog dialog = new MessageDialog(detailComposite
						.getShell(), "Save vCard", null,
						"The current vCard has been modified. Save changes?",
						MessageDialog.CONFIRM, new String[] {
								IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL }, 0);
				dialog.create();
				applyDialogStyles(dialog.getShell());
				if (dialog.open() == Window.OK) {
					ParameterizedCommand saveCommand = commandService
							.createCommand("contacts.save",
									Collections.EMPTY_MAP);
					handlerService.executeHandler(saveCommand);
				}
				dirtyable.setDirty(false);
			}
		}
		detailComposite.update(selection);
	}
}
