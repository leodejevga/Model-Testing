package org.eclipse.emf.henshin.paths;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.text.AbstractDocument.Content;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.henshin.paths.builder.AddRemoveNatureHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.ui.IStartup;

public class Start implements IStartup {
	private boolean configured = false;
	IWorkspace workspace = null;

	@Override
	public void earlyStartup() {
		if (true) {
			workspace = ResourcesPlugin.getWorkspace();
			IProject[] projects = workspace.getRoot().getProjects();
			String ignoreProjects = "";
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream("projects.bin"));
				ignoreProjects = (String) ois.readObject();
				ois.close();
			} catch (IOException | ClassNotFoundException e1) {
				System.out.println("Fehler beim Lesen von projects.bin");
			}

			IResourceChangeListener listener = new ManifestReporter();
			workspace.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);

			for (IProject p : projects) {
				IFile manifest = (IFile) p.findMember("META-INF/MANIFEST.MF");
				System.out.println("Project: " + p.getName() + ", File: " + manifest.getName());

				if (!ignoreProjects.contains(p.getName())) {
					try {
						toggleNature(manifest.getContents(), p);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	private class ManifestReporter implements IResourceChangeListener {

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				try {
					event.getDelta().accept(new ManifestVisitor());
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		private class ManifestVisitor implements IResourceDeltaVisitor {

			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {

				if (delta.getKind() == IResourceDelta.CHANGED) {

					if (delta.getAffectedChildren(IResourceDelta.CHANGED).length == 0
							&& delta.getResource().getProjectRelativePath().toString().equals("META-INF/MANIFEST.MF")) {
						IFile manifest = (IFile) delta.getResource();
						IProject project = manifest.getProject();
						String ignoreProjects = "";
						try {
							ObjectInputStream ois = new ObjectInputStream(new FileInputStream("projects.bin"));
							ignoreProjects = (String) ois.readObject();
							ois.close();
						} catch (IOException | ClassNotFoundException e1) {
							System.out.println("Fehler beim Lesen von projects.bin");
						}
						if (ignoreProjects.contains(project.getName()))
							return false;
						toggleNature(manifest.getContents(), project);
						return false;
					}
					return true;
				}
				return false;
			}

			private IResourceDelta findManifest(IResourceDelta[] children) {
				for (IResourceDelta c : children) {
					IResourceDelta[] newChildren = c.getAffectedChildren(IResourceDelta.CHANGED);
					if (newChildren.length == 0) {
						if (c.getResource().getProjectRelativePath().toString().equals("META-INF/MANIFEST.MF"))
							return c;
					} else {
						return findManifest(newChildren);
					}
				}
				return null;
			}

		}
	}

	private void toggleNature(InputStream contents, IProject p) {

		Timer a = new Timer();
		a.schedule(new TimerTask() {

			@Override
			public void run() {

				int startSearch = 0;
				String text = "";
				int b;
				try {
					b = contents.read();
					while (b != -1) {
						text += (char) b;
						b = contents.read();
						if ((char) b == '\n' || (char) b == '\r') {
							if (startSearch == 0 && text.contains("Require-Bundle"))
								startSearch = 1;
							if (startSearch > 0) {
								if (text.contains("Export-Package"))
									startSearch = 0;
								else if (text.contains("henshin.interpreter"))
									startSearch++;
							}
							text = "";
						}
						if (startSearch > 1) {
							AddRemoveNatureHandler.toggleNature(p, true);
							configured = true;
							break;
						}
					}
					if (!configured)
						AddRemoveNatureHandler.toggleNature(p, false);
					System.out.println("\nPath Builder " + (configured ? "activated " : "deactivated") + "  "
							+ p.getName() + "\n");
					configured = false;
				} catch (IOException | CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 1000);
	}

}
