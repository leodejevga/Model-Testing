package org.eclipse.emf.henshin.paths;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.henshin.paths.builder.AddRemoveNatureHandler;
import org.eclipse.emf.henshin.paths.builder.Builder;
import org.eclipse.emf.henshin.paths.builder.Nature;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IStartup;

public class Start implements IStartup {
	private InputStream contents;
	private boolean configured = false;

	@Override
	public void earlyStartup() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for(IProject p : projects){
			IFile manifest = (IFile) p.findMember("META-INF/MANIFEST.MF");
			System.out.println("Project: " + p.getName() + ", File: " + manifest.getName());
			int startSearch = 0;
			try {
				contents = manifest.getContents();
				String text = "";
				int b = contents.read();
				while(b!=-1){
					text += (char)b;
					b = contents.read();
					if((char)b=='\n' || (char)b=='\r'){
						if(startSearch==0 && text.contains("Require-Bundle"))
							startSearch = 1;
						if(startSearch>0){
							if(text.contains("Export-Package"))
								startSearch = 0;
							else if(text.contains("henshin.interpreter"))
								startSearch++;
						}
						text = "";
					}
					if(startSearch>1){
						AddRemoveNatureHandler.toggleNature(p, true);
						configured=true;
						break;
					}
				}
				if(!configured)
					AddRemoveNatureHandler.toggleNature(p, false);
				System.out.println("\nPath Builder " + (configured?"activated ":"deactivated") + "\n");
				configured = false;
			} catch (CoreException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
