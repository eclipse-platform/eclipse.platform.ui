package org.eclipse.update.internal.ui.parts;

import java.util.StringTokenizer;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.update.core.IFeature;

public class EnvironmentUtil {
	public static boolean isValidEnvironment(IFeature candidate) {
		String os = candidate.getOS();
		String ws = candidate.getWS();
		String nl = candidate.getNL();
		String arch = candidate.getArch();
		if (os!=null && isMatching(os, BootLoader.getOS())==false) return false;
		if (ws!=null && isMatching(ws, BootLoader.getWS())==false) return false;
		if (nl!=null && isMatching(nl, BootLoader.getNL())==false) return false;
		if (arch!=null && isMatching(arch, BootLoader.getOSArch())==false) return false;
		return true;
	}
	
	private static boolean isMatching(String values, String current) {
		StringTokenizer stok = new StringTokenizer(values, ",");
		while (stok.hasMoreTokens()) {
			String token = stok.nextToken();
			if (token.equalsIgnoreCase(current)) return true;
		}
		return false;
	}

}
