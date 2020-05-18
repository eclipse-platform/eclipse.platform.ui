package org.eclipse.urischeme.internal.registration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.eclipse.core.runtime.Platform;
import org.junit.Assume;
import org.junit.Test;

public class TestUnitWinRegistry {

	@Test
	public void testWinRegistry() throws Exception {
		Assume.assumeThat("Requires Windows OS", Platform.getOS(), is(Platform.OS_WIN32));

		WinRegistry winRegistry = new WinRegistry();
		String randomKeyMain = UUID.randomUUID().toString();
		String randomKeySub = UUID.randomUUID().toString();

		winRegistry.setValueForKey(randomKeyMain + "\\" + randomKeySub, "dummykey", "dummyval");

		String actualVal = winRegistry.getValueForKey(randomKeyMain + "\\" + randomKeySub, "dummykey");

		assertThat(actualVal, is("dummyval"));

		winRegistry.deleteKey(randomKeyMain + "\\" + randomKeySub);
		winRegistry.deleteKey(randomKeyMain);

		actualVal = winRegistry.getValueForKey(randomKeyMain + "\\" + randomKeySub, "dummykey");

		assertThat(actualVal, is(nullValue()));
	}
}
