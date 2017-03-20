
package org.eclipse.emf.henshin.interpreter.mark;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

// TODO: Auto-generated Javadoc
/**
 * The Class Path.
 */
public class ProjectFile {
	
	/** The path in a string form. */
	private String stringPath;
	
	/** The file as IResource. */
	protected IResource file;
	
	public ProjectFile(){
		stringPath = "";
		file = null;
	}

	/**
	 * Instantiates a new path.
	 *
	 * @param path the path
	 * @param project the project
	 */
	public ProjectFile(String path, IProject project) {
		this.stringPath = path;
		this.file = project==null?null:project.findMember(path);
	}

	/**
	 * Changes path and updates the file.
	 *
	 * @param path the new path
	 * @return true, if the path exists
	 */
	public boolean changePath(String path) {
		this.stringPath = path;
		this.file = this.file.getProject().findMember(path);
		return path != null;
	}

	/**
	 * Adds the path to existing path.
	 *
	 * @param path the path
	 * @return true, if new path exists
	 */
	public boolean addPath(String path) {
		this.stringPath += "/" + path;
		this.file = this.file.getProject().findMember(this.stringPath);
		return this.file != null;
	}

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return stringPath;
	}

	/**
	 * True if the file is existing.
	 *
	 * @return true, if the file exists
	 */
	public boolean exists() {
		return file != null;
	}


	/**
	 * Returns a string like this: <br>
	 * <ul>
	 * <li>if HenshinFile not exists<br>
	 * <ul><code>false:path/to/file.f</code></ul></li>
	 * <li>if HenshinFile exists</li>
	 * <ul><code>true:path/to/file.f</code></ul></li>
	 * 
	 * </ul>
	 * @see org.eclipse.emf.henshin.paths.builder.ProjectFile#toString()
	 */
	@Override
	public String toString() {
		return exists() + ":" + stringPath;
	}

	/**
	 * Checks if this file represents a <code>IResource.FILE</code> <br>
	 * If the File don't exists, check if the path contains a point (because of extension).
	 *
	 * @return true, if is a file.
	 */
	public boolean isFile() {
		return file == null && stringPath.contains(".") || file != null && file.getType() == IResource.FILE;
	}
	/**
	 * Checks if this file represents a <code>IResource.FOLDER</code> <br>
	 * If the File don't exists, check if the path dont contains a point (because of extension).
	 *
	 * @return true, if is a folder.
	 */
	public boolean isFolder() {
		return file == null && !stringPath.contains(".") || file != null && file.getType() == IResource.FOLDER;
	}
}
