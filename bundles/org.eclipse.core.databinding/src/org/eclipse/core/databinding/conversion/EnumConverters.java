/*******************************************************************************
 * Copyright (c) 2020 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - initial API and implementation (Bug 148327)
 ******************************************************************************/

package org.eclipse.core.databinding.conversion;

import java.util.Objects;

/**
 * Contains static methods the create converters for working with {@link Enum}s.
 *
 * @since 1.11
 */
public class EnumConverters {
	/**
	 * Creates a converter which converts from {@link Enum#ordinal}s to enum values
	 * of the given type. Invalid ordinal values are converted to {@code null}.
	 *
	 * @param enumToType to type; not null
	 * @return the created converter
	 */
	public static <T extends Enum<T>> IConverter<Integer, T> fromOrdinal(Class<T> enumToType) {
		Objects.requireNonNull(enumToType);
		T[] ordinals = enumToType.getEnumConstants();
		return IConverter.create(Integer.class, enumToType,
				i -> i == null || i < 0 || i >= ordinals.length ? null : ordinals[i]);
	}

	/**
	 * Creates a converter which converts from the {@link #toString} values of enums
	 * values to enum values themselves. Invalid string values are converted to
	 * {@code null}.
	 *
	 * @param enumToType to type; not null
	 * @return the created converter
	 */
	public static <T extends Enum<T>> IConverter<String, T> fromString(Class<T> enumToType) {
		Objects.requireNonNull(enumToType);
		return IConverter.create(String.class, enumToType, text -> {
			if (text == null) {
				return null;
			}

			try {
				return Enum.valueOf(enumToType, text);
			} catch (IllegalArgumentException e) {
				return null;
			}
		});
	}

	/**
	 * Creates a converter which converts from {@link Enum#ordinal}s to enum values
	 * of the given type. {@code null} in the converter input is converted to
	 * {@code null}.
	 *
	 * @param enumFromType from type; not null
	 * @return the created converter
	 */
	public static <T extends Enum<T>> IConverter<T, Integer> toOrdinal(Class<T> enumFromType) {
		Objects.requireNonNull(enumFromType);
		return IConverter.create(enumFromType, Integer.class, e -> e == null ? null : e.ordinal());
	}

	/**
	 * Creates a converter which converts to the {@link #toString} values of the
	 * enum constants. {@code null} in the converter input is converted to
	 * {@code null}.
	 *
	 * @param enumFromType from type; not null
	 * @return the created converter
	 */
	public static <T extends Enum<T>> IConverter<T, String> toString(Class<T> enumFromType) {
		Objects.requireNonNull(enumFromType);
		return IConverter.create(enumFromType, String.class, e -> e == null ? null : e.toString());
	}
}
