/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * Simple factory implementation that will fail on command. This is used to test
 * that working set restoration does not die if one of the factories dies.
 *
 * @since 3.4
 */
public class BadElementFactory implements IElementFactory {

	/**
	 * Set to cause the factory to fail.
	 */
	public static boolean shouldFailOnCreateElement = false;


	/**
	 * Set to true when {@link #createElement(IMemento)} is called while
	 * shouldFailOnCreateElement fail is true.
	 */
	public static boolean elementCreationAttemptedWhileShouldFail = false;

	public static class BadElementInstance implements IAdaptable,
			IPersistableElement {

		/**
		 * Set to cause save to fail.
		 */
		public static boolean shouldSaveFail = false;


		/**
		 * Set to true when {@link #saveState(IMemento)} is called while shouldSaveFail
		 * is true.
		 */
		public static boolean saveAttemptedWhileShouldFail = false;


		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(Class<T> adapter) {
			if (adapter.equals(IPersistableElement.class)) {
				return (T) this;
			}
			return null;
		}

		@Override
		public String getFactoryId() {
			return "org.eclipse.ui.tests.badFactory";
		}

		@Override
		public void saveState(IMemento memento) {
			if (shouldSaveFail) {
				saveAttemptedWhileShouldFail = true;
				throw new TestException();
			}

		}

	}

	@Override
	public IAdaptable createElement(IMemento memento) {
		if (shouldFailOnCreateElement) {
			elementCreationAttemptedWhileShouldFail = true;
			throw new TestException();
		}
		return new BadElementInstance();
	}

}
