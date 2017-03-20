
package org.eclipse.emf.henshin.interpreter.mark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

// TODO: Auto-generated Javadoc
/**
 * The Class PathHandler.
 */
public class PathHandler {
	
	/** The project. */
	private IProject project;
	
	/** The entries. */
	private Map<String, HenshinFile> entries;
	
	/** The thread. */
	private int thread;

	/**
	 * Instantiates a new path handler.
	 *
	 * @param project the project
	 */
	public PathHandler(IProject project) {
		this.entries = new HashMap<String, HenshinFile>();
		thread = 0;
		this.project = project;
	}

	/**
	 * Instantiates a new path handler.
	 *
	 * @param path the path
	 * @param identifier the identifier
	 * @param project the project
	 */
	public PathHandler(String path, String identifier, IProject project) {
		this.entries = new HashMap<String, HenshinFile>();
		entries.put(identifier, new HenshinFile(path, project));
		this.project = project;
	}

	/**
	 * Exists.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	public boolean exists(String path) {
		return project.findMember(path.replace("\"", "")) != null;
	}

	/**
	 * Exists.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	public boolean exists(ProjectFile path) {
		boolean ex = project.findMember(path.getPath()) != null;
		return ex;
		// TODO schauen ob rule oder parameter existieren
	}

	/**
	 * Gets the path as string.
	 *
	 * @param identifier the identifier
	 * @return the path as string
	 */
	public String getPathAsString(String identifier) {
		HenshinFile result = entries.get(identifier);
		return result != null ? result.getPath() : "";
	}

	/**
	 * Gets the path.
	 *
	 * @param identifier the identifier
	 * @return the path
	 */
	public HenshinFile getPath(String identifier) {
		return entries.get(identifier);
	}

	/**
	 * Adds the path.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	public boolean addPath(String path) {
		return addPath(path, null);
	}

	/**
	 * Adds the path.
	 *
	 * @param path the path
	 * @param identifier the identifier
	 * @return true, if successful
	 */
	public boolean addPath(String path, String identifier) {
		if (identifier != null && identifier.length() != 0 && path.length() != 0)
			this.entries.put(identifier, this.entries.get(path));
		else
			return false;
		return true;
	}

	/**
	 * Adds the path.
	 *
	 * @param path the path
	 * @param identifier the identifier
	 * @return true, if successful
	 */
	public boolean addPath(HenshinFile path, String identifier) {
		if (identifier != null && identifier.length() != 0)
			this.entries.put(identifier, path);
		else
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PH: " + entries;
	}
}
