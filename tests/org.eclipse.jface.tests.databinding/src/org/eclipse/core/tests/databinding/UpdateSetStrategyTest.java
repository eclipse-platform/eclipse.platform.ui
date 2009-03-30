/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 270461)
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.UpdateSetStrategy;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.internal.databinding.conversion.IdentityConverter;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 1.1
 */
public class UpdateSetStrategyTest extends AbstractDefaultRealmTestCase {
	public void testFillDefaults_AssertSourceTypeExtendsConverterFromType() {
		// Valid use: source type String extends converter from-type Object
		UpdateSetStrategyStub strategy = new UpdateSetStrategyStub();
		strategy
				.setConverter(new IdentityConverter(Object.class, Object.class));
		strategy.fillDefaults(WritableSet.withElementType(String.class),
				WritableSet.withElementType(Object.class));

		// Invalid use: source type Object does not extend converter from-type
		// String
		strategy = new UpdateSetStrategyStub();
		strategy
				.setConverter(new IdentityConverter(String.class, Object.class));
		try {
			strategy.fillDefaults(WritableSet.withElementType(Object.class),
					WritableSet.withElementType(Object.class));
			fail("Expected BindingException since Object does not extend String");
		} catch (BindingException expected) {
		}
	}

	public void testFillDefaults_AssertConverterToTypeExtendsDestinationType() {
		// Valid use: converter to-type String extends destination type Object
		UpdateSetStrategyStub strategy = new UpdateSetStrategyStub();
		strategy
				.setConverter(new IdentityConverter(Object.class, String.class));
		strategy.fillDefaults(WritableSet.withElementType(Object.class),
				WritableSet.withElementType(Object.class));

		// Invalid use: converter to-type Object does not extend destination
		// type String
		strategy = new UpdateSetStrategyStub();
		strategy
				.setConverter(new IdentityConverter(Object.class, Object.class));
		try {
			strategy.fillDefaults(WritableSet.withElementType(Object.class),
					WritableSet.withElementType(String.class));
			fail("Expected BindingException since Object does not extend String");
		} catch (BindingException expected) {
		}
	}

	class UpdateSetStrategyStub extends UpdateSetStrategy {
		protected void fillDefaults(IObservableSet source,
				IObservableSet destination) {
			super.fillDefaults(source, destination);
		}
	}
}
