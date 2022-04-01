package fr.istic.vv;

import java.util.List;
import java.util.ListIterator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods
public class PublicElementsPrinter extends VoidVisitorWithDefaults<Void> {

	private List<String> listFields;

	@Override
	public void visit(CompilationUnit unit, Void arg) {
		for (TypeDeclaration<?> type : unit.getTypes()) {
			type.accept(this, null);
		}
	}

	public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {
		if (!declaration.isPublic())
			return;
		System.out.println(declaration.getFullyQualifiedName().orElse("[Anonymous]"));
		for (MethodDeclaration method : declaration.getMethods()) {
			method.accept(this, arg);
		}
		// Printing nested types in the top level
		for (BodyDeclaration<?> member : declaration.getMembers()) {
			if (member instanceof TypeDeclaration)
				member.accept(this, arg);
		}
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
		visitTypeDeclaration(declaration, arg);
		listFields = new ArrayList<String>();
		ListIterator<FieldDeclaration> it = listFields.listIterator();
//		while (it.hasNext()) {
//			FieldDeclaration current = it.next();
//			System.out.println("    The field "+current.getVariable(0).getNameAsString());
//		}
	}

	@Override
	public void visit(EnumDeclaration declaration, Void arg) {
		visitTypeDeclaration(declaration, arg);
	}

	@Override
	public void visit(MethodDeclaration declaration, Void arg) {
		if (!declaration.isPublic())
			return;
          System.out.println("  In method " + declaration.getNameAsString());
        System.out.println("  " + declaration.findAll(NameExpr.class));
//        System.out.println("  " + declaration.findAll(FieldAccessExpr.class).retainAll(listFields));
	}

}