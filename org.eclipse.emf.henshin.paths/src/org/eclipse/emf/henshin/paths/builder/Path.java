package org.eclipse.emf.henshin.paths.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class Path {
	private String stringPath;
	protected IResource path;
	protected IProject project;

	public Path(String path, IProject project) {
		this.stringPath = path;
		this.path = project.findMember(path);
		this.project = project;
	}

	public boolean changePath(String path) {
		this.stringPath = path;
		this.path = project.findMember(path);
		return path != null;
	}

	public boolean addPath(String path) {
		this.stringPath += "/" + path;
		this.path = project.findMember(this.stringPath);
		return this.path != null;
	}

	public String getPath() {
		return stringPath;
	}

	public boolean exists() {
		return path != null;
	}

	@Override
	public String toString() {
		return exists() + ":" + stringPath;
	}

	public boolean isFile() {
		return path == null && !stringPath.contains("/") || path != null && path.getType() == IResource.FILE;
	}
}
