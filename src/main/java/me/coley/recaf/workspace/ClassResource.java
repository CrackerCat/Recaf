package me.coley.recaf.workspace;

import me.coley.recaf.parse.source.SourceCode;
import me.coley.recaf.parse.source.SourceCodeException;
import me.coley.recaf.util.IOUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Importable class resource.
 *
 * @author Matt
 */
public class ClassResource extends FileSystemResource {
	/**
	 * Constructs a class resource.
	 *
	 * @param path
	 * 		Path reference to a class file.
	 *
	 * @throws IOException
	 * 		When the path does not exist.
	 */
	public ClassResource(Path path) throws IOException {
		super(ResourceKind.CLASS, path);
	}
	
	/**
	 * Constructs a class resource.
	 *
	 * @param file
	 * 		File reference to a class file.
	 *
	 * @throws IOException
	 * 		When the file does not exist.
	 * @deprecated
	 * 		Use {@link ClassResource#ClassResource(Path)} instead.
	 */
	public ClassResource(File file) throws IOException {
		this(file.toPath());
	}

	@Override
	protected Map<String, byte[]> loadClasses() throws IOException {
		EntryLoader loader = getEntryLoader();
		try (FileInputStream stream = new FileInputStream(getFile())) {
			byte[] value = IOUtil.toByteArray(stream);
			loader.onClass(getFile().getName(), value);
			loader.finishClasses();
			return loader.getClasses();
		} catch(ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
			throw new IOException("Failed to load class '" + getFile().getName() + "'", ex);
		}
	}

	@Override
	protected Map<String, byte[]> loadFiles() {
		return Collections.emptyMap();
	}

	@Override
	protected Map<String, SourceCode> loadSources(Path path) throws IOException {
		if (IOUtil.getExtension(path).equals("java")) {
			try {
				SourceCode code = new SourceCode(this, String.join("", Files.readAllLines(path,
						StandardCharsets.UTF_8)));
				code.analyze();
				return Collections.singletonMap(code.getInternalName(), code);
			} catch(IOException ex) {
				throw new IOException("Failed to read from source file: " + path, ex);
			} catch(SourceCodeException ex) {
				throw new IOException("Invalid source code file: " + path, ex);
			}
		}
		return super.loadSources(path);
	}
}
