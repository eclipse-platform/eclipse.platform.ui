package org.eclipse.team.internal.ccvs.core.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.zip.CRC32;

import javax.swing.plaf.FileChooserUI;
import org.eclipse.team.internal.ccvs.core.CVSException;

public class FileUtil {
		
	public static void deepDelete(File resource) {
		if (resource.isDirectory()) {
			File[] fileList = resource.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				deepDelete(fileList[i]);
			}
		}
		resource.delete();
	}
}