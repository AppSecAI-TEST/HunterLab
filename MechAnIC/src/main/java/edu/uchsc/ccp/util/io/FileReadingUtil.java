/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Knowtator.
 *
 * The Initial Developer of the Original Code is University of Colorado.  
 * Copyright (C) 2005-2008.  All Rights Reserved.
 *
 * Knowtator was developed by the Center for Computational Pharmacology
 * (http://compbio.uchcs.edu) at the University of Colorado Health 
 *  Sciences Center School of Medicine with support from the National 
 *  Library of Medicine.  
 *
 * Current information about Knowtator can be obtained at 
 * http://knowtator.sourceforge.net/
 *
 * Contributor(s):
 *   Philip V. Ogren <philip@ogren.info> (Original Author)
 */
package edu.uchsc.ccp.util.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FileReadingUtil {

	/**
	 * Reads in a text file and returns it as an array of Strings. Risky from a
	 * scale point of view.
	 */
	public static String[] readFileLines(String infile) throws IOException {
		return readFileLines(new FileInputStream(infile));
	}

	public static String[] readFileLines(String infile, String comment) throws IOException {
		return readFileLines(new FileInputStream(infile), comment);
	}

	public static String[] readFileLines(InputStream inputStream) throws IOException {
		return readFileLines(inputStream, null);
	}

	public static String[] readFileLines(InputStream inputStream, String comment) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));

		String line = new String();
		List<String> lines = new ArrayList<String>();

		while ((line = reader.readLine()) != null) {
			if (comment == null || !line.startsWith(comment))
				lines.add(line);
		}

		return (String[]) (lines.toArray(new String[lines.size()]));

	}

	public static String toString(String infile) throws IOException {
		return toString(new FileInputStream(infile));
	}

	public static String toString(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer contents = new StringBuffer();

		String line = new String();
		while ((line = reader.readLine()) != null) {
			contents.append(line + "\n");
		}
		return contents.toString();
	}

	public static void main(String[] args) {
		try {
			String[] lines = readFileLines(args[0]);
			System.out.println("lines.length=" + lines.length);
			for (int i = 0; i < lines.length; i++) {
				System.out.println(lines[i]);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
