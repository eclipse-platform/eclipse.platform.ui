package org.eclipse.urischeme.internal.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.eclipse.core.runtime.Platform;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class TestUnitWinRegistry {

	@Test
	public void testWinRegistry() throws Exception {
		Assumptions.assumeTrue(Platform.OS_WIN32.equals(Platform.getOS()), "Requires Windows OS");

		WinRegistry winRegistry = new WinRegistry();
		String randomKeyMain = UUID.randomUUID().toString();
		String randomKeySub = UUID.randomUUID().toString();

		winRegistry.setValueForKey(randomKeyMain + "\\" + randomKeySub, "dummykey", "dummyval");

		String actualVal = winRegistry.getValueForKey(randomKeyMain + "\\" + randomKeySub, "dummykey");

		assertEquals("dummyval", actualVal);

		winRegistry.deleteKey(randomKeyMain + "\\" + randomKeySub);
		winRegistry.deleteKey(randomKeyMain);

		actualVal = winRegistry.getValueForKey(randomKeyMain + "\\" + randomKeySub, "dummykey");

		assertNull(actualVal);
	}
}
