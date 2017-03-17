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

/**
 * This class will start while eclipse opening. <br>
 * It looks for projects, where path builder should be activated.
 */
public class Start implements IStartup {

	/** The workspace. */
	private IWorkspace workspace = null;

	/**
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
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

	/**
	 * The Class ManifestReporter. Listen to changing of resources. <br>
	 * This reporter listen just for MANIFEST.MF files.
	 */
	private class ManifestReporter implements IResourceChangeListener {

		/**
		 * Listen to Post Change events.
		 * 
		 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.
		 * IResourceChangeEvent)
		 */
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

		/**
		 * The Class ManifestVisitor. Visits the MANIFEST.MF files to recognize if they are containing henshin interpreter. <br>
		 * If true, the Path Builder will be activated for this project.
		 */
		private class ManifestVisitor implements IResourceDeltaVisitor {

			/**
			 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
			 */
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
		}
	}

	/**
	 * Toggles the nature if the given project containing henshin interpreter plug-in. <br>
	 * This method will be executed one sekond in the future to avoid some changing problems.
	 *
	 * @param contents the input stream of the MANIFEST.MF file.
	 * @param p the project
	 */
	private void toggleNature(InputStream contents, IProject p) {
		Timer a = new Timer();
		a.schedule(new TimerTask() {
			boolean configured = false;

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

					contents.close();
				} catch (IOException | CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 1000);
	}

}
