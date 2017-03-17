package org.eclipse.emf.henshin.paths.builder;

import java.util.Map;

import javax.swing.text.Position;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The Class Builder.<br>
 * Perform searching for path errors.
 */
public class Builder extends IncrementalProjectBuilder {

	/**
	 * The Class SampleDeltaVisitor. <br>
	 * Handles resource change kinds.
	 */
	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		
		/**
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				checkPath(resource);
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				checkPath(resource);
				break;
			}
			return true;
		}
	}

	/**
	 * The Class SampleResourceVisitor.
	 */
	class SampleResourceVisitor implements IResourceVisitor {
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
		 */
		public boolean visit(IResource resource) {
			checkPath(resource);
			// return true to continue visiting children.
			return true;
		}
	}

	/**
	 * The Class PathErrorHandler.
	 */
	class PathErrorHandler extends DefaultHandler {

		/** The file that was changed and has to be handled. */
		private IFile file;

		/**
		 * Instantiates a new path error handler.
		 *
		 * @param file the file
		 */
		public PathErrorHandler(IFile file) {
			this.file = file;
		}

		/**
		 * Adds the marker with PathException.
		 *
		 * @param e the PathException, that has to be thrown
		 * @param message the error message to display
		 * @param severity the severity (Error, Warning, Info)
		 */
		private void addMarker(PathException e, String message, int severity) {
			Builder.this.addMarker(file, message + e.getMessage(), e.getPosition(), severity);
		}

		/**
		 * Throws an Error.
		 *
		 * @param exception the PathException
		 */
		public void error(PathException exception) {
			addMarker(exception, "Path Error: ", IMarker.SEVERITY_ERROR);
		}

		/**
		 * Throws a Fatal error.
		 *
		 * @param exception the PathException
		 */
		public void fatalError(PathException exception) {
			addMarker(exception, "Path Fatal Error: ", IMarker.SEVERITY_ERROR);
		}

		/**
		 * Throws a Warning.
		 *
		 * @param exception the PathException
		 */
		public void warning(PathException exception) {
			addMarker(exception, "Path Warning: ", IMarker.SEVERITY_WARNING);
		}
		/**
		 * Throws an Info.
		 *
		 * @param exception the PathException
		 */
		public void info(PathException exception) {
			addMarker(exception, "Path Warning: ", IMarker.SEVERITY_INFO);
		}
	}

	/** The Constant BUILDER_ID. */
	public static final String BUILDER_ID = "org.eclipse.emf.henshin.paths.Builder";

	/** The Constant MARKER_TYPE. */
	public static final String MARKER_TYPE = "org.eclipse.emf.henshin.paths.pathProblem";

	/** The parser. */
	private PathParser parser;

	/**
	 * Adds the marker of given severity with a message at position by a specific File.
	 *
	 * @param file the file
	 * @param message the error message
	 * @param ep the Position of the marker and underlining coordinates
	 * @param severity the severity
	 */
	private void addMarker(IFile file, String message, PathException.ErrorPosition ep, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (ep.line < 0) {
				ep.line = 1;
			}

			marker.setAttribute(IMarker.LINE_NUMBER, ep.line);
			marker.setAttribute(IMarker.CHAR_START, ep.start);
			marker.setAttribute(IMarker.CHAR_END, ep.end);
		} catch (CoreException e) {
			System.out.println("Path Error not successfull: " + message);
		}
	}

	/**
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void clean(IProgressMonitor monitor) throws CoreException {
		// delete markers set and files created
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	/**
	 * Checks java files for paths that are failure.
	 *
	 * @param resource the resource
	 */
	private void checkPath(IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
			IFile file = (IFile) resource;
			deleteMarkers(file);
			PathErrorHandler reporter = new PathErrorHandler(file);
			try {
				getParser().parse(file, reporter);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Delete markers.
	 *
	 * @param file the file
	 */
	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	/**
	 * Full build.
	 *
	 * @param monitor the monitor
	 * @throws CoreException the core exception
	 */
	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			getProject().accept(new SampleResourceVisitor());
		} catch (CoreException e) {
		}
	}

	/**
	 * Gets the parser.
	 *
	 * @return the parser
	 * @throws ParserConfigurationException the parser configuration exception
	 */
	private PathParser getParser() throws ParserConfigurationException {
		if (parser == null) {
			parser = new PathParser();
		}
		return parser;
	}

	/**
	 * Incremental build.
	 *
	 * @param delta the delta
	 * @param monitor the monitor
	 * @throws CoreException the core exception
	 */
	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor());
	}
}
