/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.keys;

import java.util.Objects;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

public final class KeySequenceBinding implements IKeySequenceBinding {

	/**
	 * This is the identifier for the default context. This is used wherever some
	 * default is needed. For example, this is the context that is used for key
	 * bindings that specify no context. This is also used to select a default
	 * context in the keys preference page.
	 */
	public static final String DEFAULT_CONTEXT_ID = "org.eclipse.ui.contexts.window"; //$NON-NLS-1$

	private static final int HASH_FACTOR = 89;

	private static final int HASH_INITIAL = KeySequenceBinding.class.getName().hashCode();

	private transient int hashCode;

	private transient boolean hashCodeComputed;

	private KeySequence keySequence;

	private int match;

	private transient String string;

	public KeySequenceBinding(KeySequence keySequence, int match) {
		if (keySequence == null) {
			throw new NullPointerException();
		}

		if (match < 0) {
			throw new IllegalArgumentException();
		}

		this.keySequence = keySequence;
		this.match = match;
	}

	@Override
	public int compareTo(Object object) {
		KeySequenceBinding castedObject = (KeySequenceBinding) object;
		int compareTo = Util.compare(match, castedObject.match);

		if (compareTo == 0) {
			compareTo = Util.compare(keySequence, castedObject.keySequence);
		}

		return compareTo;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof KeySequenceBinding)) {
			return false;
		}

		final KeySequenceBinding castedObject = (KeySequenceBinding) object;
		return Objects.equals(keySequence, castedObject.keySequence) && match == castedObject.match;
	}

	@Override
	public KeySequence getKeySequence() {
		return keySequence;
	}

	public int getMatch() {
		return match;
	}

	@Override
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(keySequence);
			hashCode = hashCode * HASH_FACTOR + Integer.hashCode(match);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	@Override
	public String toString() {
		if (string == null) {
			final StringBuilder stringBuffer = new StringBuilder();
			stringBuffer.append('[');
			stringBuffer.append(keySequence);
			stringBuffer.append(',');
			stringBuffer.append(match);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
