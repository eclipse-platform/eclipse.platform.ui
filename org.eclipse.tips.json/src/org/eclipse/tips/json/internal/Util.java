package org.eclipse.tips.json.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Util {

	/**
	 * Parses the passed json or returns a default value.
	 * 
	 * @param jsonObject
	 * @param element      the value to return in case the jsonObject does not
	 *                     contain the specified value.
	 * @param defaultValue the value to return in case the jsonObject does not
	 *                     contain the specified value.
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
	 * @param element      the value to return in case the jsonObject does not
	 *                     contain the specified value.
	 * @param defaultValue the value to return in case the jsonObject does not
	 *                     contain the specified value.
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
	 * @param element      the value to return in case the jsonObject does not
	 *                     contain the specified value.
	 * @param defaultValue the value to return in case the jsonObject does not
	 *                     contain the specified value.
	 * @return the returned value
	 */
	public static double getValueOrDefault(JsonObject jsonObject, String element, double defaultValue) {
		if (jsonObject.has(element)) {
			return jsonObject.get(element).getAsDouble();
		}
		return defaultValue;
	}

	/**
	 * Replaces all keys in the passed, json object that represent a primitive
	 * value, in the input string. If the passed json object contains a
	 * {@link JsonConstants#T_VARIABLES} object then this is parsed as well.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * json object: {"first": "Wim", "last": "Jongman", "variables": {"title": "Mr.", "age": 53}} 
	 * input: "${title} ${first} ${last} is ${age} years old."
	 * output: "Mr. Wim Jongman is 53 years old"
	 * </pre>
	 * 
	 * @param object the input json object
	 * @param input  the string to scan
	 * @return the replaced string
	 */
	public static String replace(JsonObject object, String input) {
		String result = doReplace(object, input);
		JsonObject vars = object.getAsJsonObject(JsonConstants.T_VARIABLES);
		if (vars != null) {
			result = Util.replace(vars, result);
		}
		return result;
	}

	private static String doReplace(JsonObject object, String input) {
		String result = input;
		for (Entry<String, JsonElement> entry : object.entrySet()) {
			JsonElement jsonElement = entry.getValue();
			if (jsonElement.isJsonPrimitive()) {
				String search = "${" + entry.getKey() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
				String replace = jsonElement.getAsString();
				int index = result.indexOf(search);
				while (index > -1) {
					result = result.substring(0, index) + replace + result.substring(index + search.length());
					index = result.indexOf(search);
				}
			}
		}
		return result;
	}

	/**
	 * @param input the json string representation
	 * @return the parsed json object
	 * @throws IOException
	 */
	public static JsonObject getJson(String input) throws IOException {
		try (InputStream stream = new ByteArrayInputStream(input.getBytes());
				InputStreamReader reader = new InputStreamReader(stream)) {
			return (JsonObject) new JsonParser().parse(reader);
		}
	}
}
