package org.lunifera.ide.core.ui.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FileUtil.class);

	public static String readFile(String path) {
		InputStream inputStream = FileUtil.class.getResourceAsStream(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder fileContent = new StringBuilder();

		try {
			// Read file line by line
			try {
				while (reader.ready()) {
					String line = reader.readLine();
					fileContent.append(line);
					fileContent.append("\n");
				}
			} catch (IOException e) {
				LOGGER.error("{}", e);
				;
				e.printStackTrace();
			}
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}

		return fileContent.toString();
	}
}
