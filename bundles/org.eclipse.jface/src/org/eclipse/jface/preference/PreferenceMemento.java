/*******************************************************************************
 * Copyright (c) 2019 Paul Pazderski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Paul Pazderski - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.preference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A utility to change and remember preference values and later restore there
 * original value. The first time a preference value is changed its current
 * value is stored. Later you can reset all preference values (which are changed
 * through this class) to the state they had when creating the memento.
 * <p>
 * This class is most useful for testing to prevent leakage of preference
 * changes between individual tests.
 * </p>
 *
 * @since 3.18
 */
public class PreferenceMemento {
	/**
	 * Map of stored preference values before they were changed the first time,
	 * indexed by there containing preference store.
	 */
	private final Map<IPreferenceStore, Map<String, Object>> savedPreferences = new HashMap<>();

	/**
	 * Set the value for the given preference on the given store.
	 * <p>
	 * The first time this preference is changed its value before change is
	 * remembered and can later be restored using {@link #resetPreferences()}.
	 * </p>
	 *
	 * @param <T>   the preference value type. The type must have a corresponding
	 *              {@link IPreferenceStore} setter.
	 * @param store the preference store to manipulate (not <code>null</code>)
	 * @param name  the name of the preference (not <code>null</code>)
	 * @param value the new current value of the preference.
	 * @throws IllegalArgumentException when setting a type which is not supported
	 *                                  by {@link IPreferenceStore}
	 *
	 * @see IPreferenceStore#setValue(String, double)
	 * @see IPreferenceStore#setValue(String, float)
	 * @see IPreferenceStore#setValue(String, int)
	 * @see IPreferenceStore#setValue(String, long)
	 * @see IPreferenceStore#setValue(String, boolean)
	 * @see IPreferenceStore#setValue(String, String)
	 * @see #resetPreferences()
	 */
	public synchronized <T> void setValue(IPreferenceStore store, String name, T value) {
		Objects.requireNonNull(store);
		if (value instanceof Double) {
			setValueInternal(store, name, (Double) value, store::getDouble, store::setValue);
		} else if (value instanceof Float) {
			setValueInternal(store, name, (Float) value, store::getFloat, store::setValue);
		} else if (value instanceof Integer) {
			setValueInternal(store, name, (Integer) value, store::getInt, store::setValue);
		} else if (value instanceof Long) {
			setValueInternal(store, name, (Long) value, store::getLong, store::setValue);
		} else if (value instanceof Boolean) {
			setValueInternal(store, name, (Boolean) value, store::getBoolean, store::setValue);
		} else if (value instanceof String || value == null) {
			setValueInternal(store, name, (String) value, store::getString, store::setValue);
		} else {
			throw new IllegalArgumentException("Unsupported value type " + value.getClass()); //$NON-NLS-1$
		}
	}

	/**
	 * Reset every preference changed by this utility to its state before the first
	 * change. After this the instance can still be used as if it was just created.
	 */
	public synchronized void resetPreferences() {
		for (Map.Entry<IPreferenceStore, Map<String, Object>> backupedStore : savedPreferences.entrySet()) {
			final IPreferenceStore store = backupedStore.getKey();
			for (Map.Entry<String, Object> preference : backupedStore.getValue().entrySet()) {
				final String preferenceName = preference.getKey();
				final Object preferenceValue = preference.getValue();
				if (preferenceValue instanceof Double) {
					store.setValue(preferenceName, ((Double) preferenceValue).doubleValue());
				} else if (preferenceValue instanceof Float) {
					store.setValue(preferenceName, ((Float) preferenceValue).floatValue());
				} else if (preferenceValue instanceof Integer) {
					store.setValue(preferenceName, ((Integer) preferenceValue).intValue());
				} else if (preferenceValue instanceof Long) {
					store.setValue(preferenceName, ((Long) preferenceValue).longValue());
				} else if (preferenceValue instanceof Boolean) {
					store.setValue(preferenceName, ((Boolean) preferenceValue).booleanValue());
				} else if (preferenceValue instanceof String || preferenceValue == null) {
					store.setValue(preferenceName, (String) preferenceValue);
				} else {
					// should be impossible since there is no way to set an unsupported type
					throw new RuntimeException(
							"PreferenceUtil got an unsupported value of type " + preferenceValue.getClass()); //$NON-NLS-1$
				}
			}
		}
		savedPreferences.clear();
	}

	/**
	 * Internal preference setter. Backups the current preference value if it is
	 * changed the first time.
	 *
	 * @param <T>        preference value type
	 * @param store      preference store to manipulate
	 * @param name       preference name
	 * @param value      preference value
	 * @param prefGetter method used to get the current preference value before
	 *                   change
	 * @param prefSetter method used to set the new preference value
	 */
	private <T> void setValueInternal(IPreferenceStore store, String name, T value, Function<String, T> prefGetter,
			BiConsumer<String, T> prefSetter) {
		Map<String, Object> knownValues = savedPreferences.computeIfAbsent(store, key -> new HashMap<>());
		if (!knownValues.containsKey(name)) {
			knownValues.put(name, prefGetter.apply(name));
		}
		prefSetter.accept(name, value);
	}
}
