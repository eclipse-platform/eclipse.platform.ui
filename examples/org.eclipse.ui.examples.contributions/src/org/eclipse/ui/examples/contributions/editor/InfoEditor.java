/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions.editor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.examples.contributions.ContributionMessages;
import org.eclipse.ui.examples.contributions.model.IPersonService;
import org.eclipse.ui.examples.contributions.model.Person;
import org.eclipse.ui.examples.contributions.model.PersonInput;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.EditorPart;

/**
 * Edit a person.
 * 
 * @since 3.3
 */
public class InfoEditor extends EditorPart {
	public static final String ID = "org.eclipse.ui.examples.contributions.editor"; //$NON-NLS-1$
	private static final String EDITOR_RESET_ID = "org.eclipse.ui.examples.contributions.editor.reset"; //$NON-NLS-1$

	private Person person;
	private Text surnameText;
	private Text givennameText;
	private boolean dirty = false;
	private IHandler resetHandler;

	@Override
	public void doSave(IProgressMonitor monitor) {
		monitor.beginTask(getPartName(), 3);
		person.setSurname(surnameText.getText());
		monitor.worked(1);
		person.setGivenname(givennameText.getText());
		monitor.worked(1);
		IPersonService service = getSite().getService(
				IPersonService.class);
		service.updatePerson(person);
		monitor.worked(1);
		monitor.done();
		setDirty(false);
	}

	@Override
	public void doSaveAs() {
		// nothing
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		if (!(input instanceof PersonInput)) {
			throw new PartInitException("Not a person"); //$NON-NLS-1$
		}
		PersonInput pinput = (PersonInput) input;
		IPersonService service = getSite().getService(
				IPersonService.class);
		person = service.getPerson(pinput.getIndex());
		if (person == null) {
			throw new PartInitException("person does not exist"); //$NON-NLS-1$
		}
		setPartName("Person - " + pinput.getIndex()); //$NON-NLS-1$
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	private void setDirty(boolean d) {
		dirty = d;
		firePropertyChange(ISaveablePart.PROP_DIRTY);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		KeyListener keyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.keyCode & SWT.MODIFIER_MASK) == 0) {
					setDirty(true);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// nothing
			}
		};

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		composite.setLayout(gridLayout);

		Label l = new Label(composite, SWT.RIGHT);
		l.setText(ContributionMessages.InfoEditor_surname);
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		surnameText = new Text(composite, SWT.SINGLE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		surnameText.setLayoutData(gridData);
		surnameText.addKeyListener(keyListener);

		l = new Label(composite, SWT.RIGHT);
		l.setText(ContributionMessages.InfoEditor_givenname);
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		givennameText = new Text(composite, SWT.SINGLE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		givennameText.setLayoutData(gridData);
		givennameText.addKeyListener(keyListener);

		updateText();

		createHandlers();
	}

	/**
	 * Set our text field to the person text.
	 */
	private void updateText() {
		surnameText.setText(person.getSurname());
		givennameText.setText(person.getGivenname());
	}

	/**
	 * Instantiate any handlers specific to this view and activate them.
	 */
	private void createHandlers() {
		IHandlerService handlerService = getSite().getService(IHandlerService.class);
		resetHandler = new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event) {
				updateText();
				setDirty(false);
				return null;
			}
		};
		handlerService.activateHandler(EDITOR_RESET_ID, resetHandler);
	}

	@Override
	public void setFocus() {
		surnameText.setFocus();
	}

	public Person getCurrentPerson() {
		person.setSurname(surnameText.getText());
		person.setGivenname(givennameText.getText());
		return person;
	}
}
