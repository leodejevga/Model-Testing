package org.eclipse.emf.henshin.paths.builder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import org.eclipse.core.commands.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddRemoveNatureHandler extends AbstractHandler {

	private ISelection selection;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		//
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
				}
				if (project != null) {
					try {
						boolean enabled = toggleNature(project);
						try {
							ObjectInputStream ois = new ObjectInputStream(new FileInputStream("projects.bin"));
							String projects = "" + (String) ois.readObject();
							ois.close();
							projects = enabled?projects.replace(project.getName()+"\n", ""):projects.contains(project.getName())?"":projects + project.getName()+"\n";
							ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("projects.bin"));
							oos.writeObject(projects);
							oos.close();
						} catch (IOException | ClassNotFoundException e1) {
							System.out.println("Fehler beim Schreiben von " + project.getName());
						}
						return false;
					} catch (CoreException e) {
						// TODO log something
						throw new ExecutionException("Failed to toggle nature", e);
					}
				}
			}
		}

		return null;

	}

	private static boolean toggleNature(IProject project) throws CoreException {
		return toggleNature(project, null);
	}

	/**
	 * Toggles sample nature on a project
	 *
	 * @param project to have sample nature added or removed
	 */
	public static boolean toggleNature(IProject project, Boolean enable) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length];
		int index = 0;
		if (enable == null || !enable) {
			boolean ready = false;
			for (int i = 0; i < natures.length; ++i) {
				if (!Nature.NATURE_ID.equals(natures[i])) {
					// Remove the nature
					newNatures[index++] = natures[i];
				}
				else
					ready = true;
			}
			String[] temp = new String[index];
			System.arraycopy(newNatures, 0, temp, 0, index);
			description.setNatureIds(temp);
			project.setDescription(description, null);
			if(ready)
				return false;
		}
		newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = Nature.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
		return true;
	}

}