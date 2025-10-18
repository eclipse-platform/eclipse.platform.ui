package org.eclipse.jface.tests.labelProviders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Function;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

public class LabelProviderTest {

	private static final Car HORCH = new Car("Horch");

	private static Image horchImage = new Image(Display.getDefault(), (gc, width, height) -> {}, 50, 10);
	private static Image defaultImage = new Image(Display.getDefault(), (gc, width, height) -> {}, 1, 1);

	private final Function<Object, String> textFunction = o -> o instanceof Car ? ((Car) o).getMake() : "unknown";
	private final Function<Object, Image> imageFunction = o -> o instanceof Car ? horchImage : defaultImage;

	@AfterAll
	public static void classTeardown() {
		horchImage.dispose();
		defaultImage.dispose();
	}

	@Test
	public void getTextGivesEmptyTextOnNull() {
		assertEquals("", new LabelProvider().getText(null));
	}

	@Test
	public void getTextGivesToString() {
		assertEquals("Make: Horch", new LabelProvider().getText(HORCH));
	}

	@Test
	public void getImageGivesNullOnNull() {
		assertNull(new LabelProvider().getImage(null));
	}

	@Test
	public void textProviderGivesTexts() {
		LabelProvider labelProvider = LabelProvider.createTextProvider(textFunction);

		assertEquals("Horch", labelProvider.getText(HORCH));
		assertEquals("unknown", labelProvider.getText(new Object()));

		assertNull(labelProvider.getImage(HORCH));
	}

	@Test
	public void imageProviderGivesImages() {
		LabelProvider labelProvider = LabelProvider.createImageProvider(imageFunction);

		assertEquals(horchImage, labelProvider.getImage(HORCH));
		assertEquals(defaultImage, labelProvider.getImage(new Object()));

		// no text lambda given, default implementation does toString
		assertEquals("Make: Horch", labelProvider.getText(HORCH));
	}

	@Test
	public void textImageProviderGivesBoth() {
		LabelProvider labelProvider = LabelProvider.createTextImageProvider(textFunction, imageFunction);

		assertEquals(horchImage, labelProvider.getImage(HORCH));
		assertEquals(defaultImage, labelProvider.getImage(new Object()));

		assertEquals("Horch", labelProvider.getText(HORCH));
		assertEquals("unknown", labelProvider.getText(new Object()));
	}

	@Test
	public void throwsExceptionOnNullTextProvider() {
		assertThrows(NullPointerException.class, () -> {
			LabelProvider.createTextProvider(null);
		});
	}

	@Test
	public void throwsExceptionOnNullImageProvider() {
		assertThrows(NullPointerException.class, () -> {
			LabelProvider.createImageProvider(null);
		});
	}

	@Test
	public void throwsExceptionOnNullTextProviderInTextImageProvider() {
		assertThrows(NullPointerException.class, () -> {
			LabelProvider.createTextImageProvider(null, imageFunction);
		});
	}

	@Test
	public void throwsExceptionOnNullImageProviderInTextImageProvider() {
		assertThrows(NullPointerException.class, () -> {
			LabelProvider.createTextImageProvider(textFunction, null);
		});
	}

	static class Car {
		private final String make;

		public Car(String make) {
			this.make = make;
		}

		public String getMake() {
			return make;
		}

		@Override
		public String toString() {
			return "Make: " + make;
		}
	}
}