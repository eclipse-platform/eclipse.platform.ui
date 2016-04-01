/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions.model;

import java.util.Collection;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Example implementation that cleans up listeners when the IServiceLocator
 * (site, window, etc) is disposed. It delegates to its parent for the actual
 * behaviour.
 * 
 * @since 3.4
 */
public class PersonServiceSlave implements IPersonService, IDisposable {

	private IServiceLocator serviceLocator;
	private IPersonService parentService;
	private ListenerList<IPropertyChangeListener> localListeners = new ListenerList<>(
			ListenerList.IDENTITY);

	public PersonServiceSlave(IServiceLocator locator, IPersonService parent) {
		serviceLocator = locator;
		parentService = parent;
		serviceLocator.hasService(IHandlerService.class);
	}

	@Override
	public void addPersonChangeListener(IPropertyChangeListener listener) {
		localListeners.add(listener);
		parentService.addPersonChangeListener(listener);
	}

	@Override
	public Collection<Person> getPeople() {
		return parentService.getPeople();
	}

	@Override
	public Person getPerson(int id) {
		return parentService.getPerson(id);
	}

	@Override
	public void removePersonChangeListener(IPropertyChangeListener listener) {
		localListeners.remove(listener);
		parentService.removePersonChangeListener(listener);
	}

	@Override
	public void updatePerson(Person person) {
		parentService.updatePerson(person);
	}

	@Override
	public Person createPerson(int id) {
		return parentService.createPerson(id);
	}

	@Override
	public void dispose() {
		Object[] array = localListeners.getListeners();
		localListeners.clear();
		for (int i = 0; i < array.length; i++) {
			parentService
					.removePersonChangeListener((IPropertyChangeListener) array[i]);
		}
		serviceLocator = null;
		parentService = null;
	}

	@Override
	public void login(Person person) {
		parentService.login(person);
	}

}
