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
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Example implementation of the person service.
 * 
 * @since 3.4
 */
public class PersonService implements IPersonService, IDisposable {

	private static final int ME = 1114;
	private Map<Integer, Person> people = new TreeMap<>();
	private IServiceLocator serviceLocator;
	private ListenerList<IPropertyChangeListener> listeners = new ListenerList<>(ListenerList.IDENTITY);

	public PersonService(IServiceLocator locator) {
		serviceLocator = locator;
		serviceLocator.hasService(IHandlerService.class);
		fillModel();
	}

	private static final String[] datafill = {
			"Webster", "Paul", "Doe", "John", "Doe", "Jane", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Public", "John", "Public", "Jane" }; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$

	private void fillModel() {
		int i = ME;
		for (int j = 0; j < datafill.length; j += 2) {
			int iid = i++;
			Person p = new Person(iid, datafill[j], datafill[j + 1]);
			if (p.getId() == ME) {
				p.setAdminRights(true);
			}
			people.put(iid, p);
		}
	}

	@Override
	public void addPersonChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public Collection<Person> getPeople() {
		return Collections.unmodifiableCollection(people.values());
	}

	@Override
	public Person getPerson(int id) {
		Person p = people.get(Integer.valueOf(id));
		if (p == null) {
			return null;
		}
		return p.copy();
	}

	@Override
	public void removePersonChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void updatePerson(Person person) {
		Assert.isNotNull(person);
		Person p = people.get(Integer.valueOf(person.getId()));
		if (p == null) {
			Assert.isNotNull(p, "Must update a real person"); //$NON-NLS-1$
		}
		if (person.equals(p)) {
			return;
		}
		Person oldVal = p.copy();
		p.setGivenname(person.getGivenname());
		p.setSurname(person.getSurname());
		firePersonChange(PROP_CHANGE, oldVal, person);
	}

	/**
	 * @param oldVal
	 * @param person
	 */
	private void firePersonChange(String property, Person oldVal, Person person) {
		if (listeners.isEmpty()) {
			return;
		}
		PropertyChangeEvent event = new PropertyChangeEvent(this, property,
				oldVal, person);
		Object[] array = listeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			((IPropertyChangeListener) array[i]).propertyChange(event);
		}
	}

	@Override
	public Person createPerson(int id) {
		Integer iid = Integer.valueOf(id);
		if (people.containsKey(iid)) {
			return null;
		}
		Person person = new Person(id, "surname", "givenname"); //$NON-NLS-1$//$NON-NLS-2$
		people.put(iid, person);
		firePersonChange(PROP_ADD, null, person);
		return person;
	}

	@Override
	public void dispose() {
		// we'd save stuff here, maybe, if we cared
		listeners.clear();
		serviceLocator = null;
	}

	@Override
	public void login(Person person) {
		ISourceProviderService sources = serviceLocator
				.getService(ISourceProviderService.class);
		// should do some more checks
		UserSourceProvider userProvider = (UserSourceProvider) sources
				.getSourceProvider(UserSourceProvider.USER);
		userProvider.login(person);
	}
}
