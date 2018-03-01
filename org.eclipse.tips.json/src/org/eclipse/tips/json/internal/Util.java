package org.eclipse.tips.json.internal;

import com.google.gson.JsonObject;

public class Util {

	/**
	 * Parses the passed json or returns a default value.
	 * 
	 * @param jsonObject
	 * @param element
	 *            the value to return in case the jsonObject does not contain the
	 *            specified value.
	 * @param defaultValue
	 *            the value to return in case the jsonObject does not contain the
	 *            specified value.
	 * @return the returned value
	 */
	public static String getValueOrDefault(JsonObject jsonObject, String element, String defaultValue) {
		if (jsonObject.has(element)) {
			return jsonObject.get(element).getAsString();
		}
		return defaultValue;
	}

	/**
	 * Parses the passed json or returns a default value.
	 * 
	 * @param jsonObject
	 * @param element
	 *            the value to return in case the jsonObject does not contain the
	 *            specified value.
	 * @param defaultValue
	 *            the value to return in case the jsonObject does not contain the
	 *            specified value.
	 * @return the returned value
	 */
	public static int getValueOrDefault(JsonObject jsonObject, String element, int defaultValue) {
		if (jsonObject.has(element)) {
			return jsonObject.get(element).getAsInt();
		}
		return defaultValue;
	}

	/**
	 * Parses the passed json or returns a default value.
	 * 
	 * @param jsonObject
	 * @param element
	 *            the value to return in case the jsonObject does not contain the
	 *            specified value.
	 * @param defaultValue
	 *            the value to return in case the jsonObject does not contain the
	 *            specified value.
	 * @return the returned value
	 */
	public static double getValueOrDefault(JsonObject jsonObject, String element, double defaultValue) {
		if (jsonObject.has(element)) {
			return jsonObject.get(element).getAsDouble();
		}
		return defaultValue;
	}
}
