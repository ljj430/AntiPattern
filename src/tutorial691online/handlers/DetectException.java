package tutorial691online.handlers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaModelException;
import tutorial691online.patterns.DestructiveWrappingFinder;
import tutorial691online.patterns.ExceptionFinder;
import tutorial691online.patterns.OverCatchFinder;

import tutorial691online.patterns.NestedTryFinder;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;


public class DetectException extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		
		IProject[] projects = root.getProjects();
			
		detectInProjects(projects);
		
		SampleHandler.printMessage("DONE DETECTING");
		System.out.println("DONE DETECTING");
		
		return null;
	}
	
	private void detectInProjects(IProject[] projects) {
		for(IProject project : projects) {
			SampleHandler.printMessage("DETECTING IN: " + project.getName());

			//NestedTryFinder nestedTry = new NestedTryFinder();
			//DestructiveWrappingFinder destructiveWrapping = new DestructiveWrappingFinder();
			OverCatchFinder overCatchFinder = new OverCatchFinder();
			
			try {
				// find the exceptions and print the methods that contain the exceptions
				
//				destructiveWrapping.findExceptions(project);
//				destructiveWrapping.printExceptions();	
				
//				nestedTry.findExceptions(project);
//				nestedTry.printExceptions();
				overCatchFinder.findExceptions(project);
				
	
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			
	}
	}
}
