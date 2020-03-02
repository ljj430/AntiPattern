package tutorial691online.visitors;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;

import tutorial691online.patterns.AbstractFinder;

public class OverCatchVisitor extends AbstractVisitor{
	// all possible thrown exceptions of the current try block
	private Set<ITypeBinding> checkedExceptionTypes = new HashSet<ITypeBinding>();

	// check if there are not equal but sub-type compatible cases
	@Override
	public boolean visit(TryStatement node) {
		MethodInvocationVisitor miv = new MethodInvocationVisitor();
		node.getBody().accept(miv);
		@SuppressWarnings("unchecked")
		List<CatchClause> catches = node.catchClauses();
		boolean result = false;
		
		// if over catch checked exceptions
		for (ITypeBinding e : checkedExceptionTypes) {
			for (CatchClause cc : catches) {
				ITypeBinding catchedException = cc.getException().getType().resolveBinding();
				// nested if means it finds a caught exception that is the super class of thrown Exception
				if (e.isSubTypeCompatible(catchedException)) {
					if (!catchedException.getQualifiedName().equals(e.getQualifiedName())) {
						result |= true;
						break;
					}
				}
			}
			if (result) {
				break;
			}
		}

		// if over catch unchecked exceptions
		if (!result) {
			for (String exceptionName : miv.thrownException.keySet()) {
				Type type = miv.thrownException.get(exceptionName);
				ITypeBinding typeBinding = type.resolveBinding();
				for (CatchClause cc : catches) {
					ITypeBinding catchedException = cc.getException().getType().resolveBinding();
					// nested if means it finds a caught exception that is the super class of thrown Exception
					if (typeBinding.isSubTypeCompatible(catchedException)) {
						if (!catchedException.getQualifiedName().equals(typeBinding.getQualifiedName())) {
							result |= true;
							break;
						}
					}
				}
				if (result) {
					break;
				}
			}
		}
		if (result) {
			antipatternNodes.add(node);
		}
		return super.visit(node);
	}
	
	class MethodInvocationVisitor extends ASTVisitor {
		Map<String, Type> thrownException = new HashMap<String, Type>();
		Map<String, ITypeBinding> localJavadocExceptions = new HashMap<String, ITypeBinding>();
		Set<String> javadocExceptions = new HashSet<String>();
		@Override
		public boolean visit(MethodInvocation node) {
			IMethodBinding methodBinding = node.resolveMethodBinding();
			for(ITypeBinding typeBinding : methodBinding.getExceptionTypes()) {
				checkedExceptionTypes.add(typeBinding);
			}
			IMethod iMethod = (IMethod) methodBinding.getJavaElement();
			CompilationUnit cu = null;
			if (iMethod.isBinary()) {
				IClassFile icf = iMethod.getClassFile();
				if (icf != null) {
					cu = AbstractFinder.parse(icf);
				}
			} else {
				ICompilationUnit icu = iMethod.getCompilationUnit();
				if (icu != null) {
					cu = AbstractFinder.parse(icu);
				}
			}
			boolean hasLocalJavadoc = false;
			if (cu != null) {
				MethodDeclaration methodNode = (MethodDeclaration) cu.findDeclaringNode(methodBinding.getKey());
				ThrowVisitor checkThrowVisitor = new ThrowVisitor(new HashSet<String>());
				methodNode.accept(checkThrowVisitor);
				thrownException.putAll(checkThrowVisitor.getThrowException());
				javadocExceptions.addAll(checkThrowVisitor.getJavadocExceptions());
				localJavadocExceptions.putAll(checkThrowVisitor.getLocalJavadocExceptions());
				
				Map<String, ITypeBinding> localJdocException = Util.getLocalJavadocExceptions(methodNode.getJavadoc());
				hasLocalJavadoc = localJdocException == null;
				if (hasLocalJavadoc) {
					localJavadocExceptions.putAll(localJdocException);
				}
			}
			
			if (localJavadocExceptions == null) {
				if (iMethod.isBinary()) {
					javadocExceptions.addAll(Util.getJavadocExceptions(iMethod));
				}
			}
			return super.visit(node);
		}
	}
}
