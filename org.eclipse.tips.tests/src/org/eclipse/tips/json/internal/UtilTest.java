package org.eclipse.tips.json.internal;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UtilTest {

	@SuppressWarnings("restriction")
	@Test
	public void testGetValueOrDefaultJsonObjectStringString() throws IOException {
		String jsonString = "{\"first\": \"Wim\", \"last\": \"Jongman\", \"variables\": {\"title\": \"Mr.\", \"age\": 53}}";
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(jsonString);
		assertTrue(Util.getValueOrDefault(jsonObject, "first", "Mark").equals("Wim"));
		assertTrue(Util.getValueOrDefault(jsonObject, "fake", "Mark").equals("Mark"));
	}

	@SuppressWarnings("restriction")
	@Test
	public void testGetValueOrDefaultJsonObjectStringInt() throws IOException {
		String jsonString = "{\"age\": \"53\", \"last\": \"Jongman\"}";
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(jsonString);
		assertTrue(Util.getValueOrDefault(jsonObject, "age", 100) == 53);
		assertTrue(Util.getValueOrDefault(jsonObject, "fake", 101) == 101);
	}

	@SuppressWarnings("restriction")
	@Test
	public void testGetValueOrDefaultJsonObjectStringDouble() throws IOException {
		String jsonString = "{\"double\": 5.21, \"last\": \"Jongman\"}";
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(jsonString);
		assertTrue(Util.getValueOrDefault(jsonObject, "double", 10.10) == 5.21);
		assertTrue(Util.getValueOrDefault(jsonObject, "fake", 101.6) == 101.6);
	}

	@SuppressWarnings("restriction")
	@Test
	public void testReplace() throws IOException {
		String input = "${title} ${first} ${last} is ${age} years old.";
		String result = "Mr. Wim Jongman is 53 years old.";
		String jsonString = "{\"first\": \"Wim\", \"last\": \"Jongman\", \"variables\": {\"title\": \"Mr.\", \"age\": 53}}";
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(jsonString);
		String replace = Util.replace(jsonObject, input);
		assertTrue(replace, replace.equals(result));
	}

	@SuppressWarnings("restriction")
	@Test
	public void testReplace2() throws IOException {
		String input = "${title} ${first} ${last} ${ddd} is ${age} years old.${title}";
		String result = "Mr. Wim Jongman ${ddd} is 53 years old.Mr.";
		String jsonString = "{\"first\": \"Wim\", \"last\": \"Jongman\", \"variables\": {\"title\": \"Mr.\", \"age\": 53}}";
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(jsonString);
		String replace = Util.replace(jsonObject, input);
		assertTrue(replace, replace.equals(result));
	}

	@SuppressWarnings("restriction")
	@Test
	public void testReplace3() throws IOException {
		String input = "${tit${empty}le}";
		String result = "Mr.";
		String jsonString = "{\"first\": \"Wim\", \"empty\": \"\", \"variables\": {\"title\": \"Mr.\", \"age\": 53}}";
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(jsonString);
		String replace = Util.replace(jsonObject, input);
		assertTrue(replace, replace.equals(result));
	}
}
