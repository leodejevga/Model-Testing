package org.eclipse.emf.henshin.paths.builder;

import org.eclipse.core.resources.IProject;

public class Path {
	protected String path;
	protected boolean exists;
	protected IProject project;

	public Path(String path, IProject project){
		this.path = path;
		this.exists = project.findMember(this.path) != null;
		this.project = project;
	}

	public boolean changePath(String path){
		this.path = path;
		exists = project.findMember(this.path) != null;
		return exists;
	}
	public boolean addPath(String path){
		this.path += "/" + path;
		exists = project.findMember(this.path) != null;
		return exists;
	}
	public String getPath(){
		return path;
	}

	public boolean exists(String path){
		return project.findMember(this.path + "/" + path) != null;
	}
	@Override
	public String toString() {
		return exists + ":" + path;
	}
}
