/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 *  Created Oct 21, 2005 by Gili Mendel
 * 
 *  $RCSfile: UpdatableFactoriesTest.java,v $
 *  $Revision: 1.2 $  $Date: 2005/11/10 18:47:21 $ 
 */
package org.eclipse.jface.tests.databinding.scenarios;

import java.util.Map;

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.IValidationContext;

public class UpdatableFactoriesTest extends ScenariosTestCase {

	interface Root {
	};

	interface None extends Root {
	};

	interface Middle extends None {
	};

	interface StandAlone {
	};

	class RootClass implements Root {
	};

	class NoneClass implements None {
	};

	class MiddleClass implements Middle {
	};

	class AllClass implements StandAlone, Middle, Root {
	};

	class MiddleChild extends MiddleClass {
	};

	interface TestIUpdatable extends IUpdatable {
		public Class getType();
	}

	class Factory implements IUpdatableFactory {
		Class c;

		public Factory(Class c) {
			this.c = c;
		}

		public IUpdatable createUpdatable(Map properties, Object description,
				IDataBindingContext bindingContext, IValidationContext validationContext) {
			if (c.isInstance(description)) {
				return new TestIUpdatable() {
					public void dispose() {
					}

					public void removeChangeListener(
							IChangeListener changeListener) {
					}

					public void addChangeListener(IChangeListener changeListener) {
					}

					public Class getType() {
						return c;
					}
				};
			}
			return null;
		}
	}

	IUpdatableFactory root = new Factory(Root.class);

	IUpdatableFactory middle = new Factory(Middle.class);

	IUpdatableFactory sa = new Factory(StandAlone.class);

	IUpdatableFactory factory = new Factory(Object.class);

	protected Class getFactoryType(Object src) throws BindingException {
		TestIUpdatable u = (TestIUpdatable) getDbc().createUpdatable(src);
		return u.getType();
	}

	public void test_factoryRegistration() throws BindingException {

		getDbc().addUpdatableFactory(root);
		getDbc().addUpdatableFactory(middle);

		// Direct mapping
		assertEquals(Root.class, getFactoryType(new RootClass()));
		assertEquals(Middle.class, getFactoryType(new MiddleClass()));

		// Inherent interface
		assertEquals(Root.class, getFactoryType(new NoneClass()));

		// AllClass inherent interface
		assertEquals(Middle.class, getFactoryType(new AllClass()));

		// class inheretence
		assertEquals(Middle.class, getFactoryType(new MiddleChild()));

		// Direct, first interface
		getDbc().addUpdatableFactory(sa);
		assertEquals(StandAlone.class, getFactoryType(new AllClass()));

		// Class based contribution.
		getDbc().addUpdatableFactory(factory);
		assertEquals(Object.class, getFactoryType(new AllClass()));

	}

}
