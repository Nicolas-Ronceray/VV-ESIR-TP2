package fr.istic.vv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

// This class visits a compilation unit and
// prints all public enum, classes or interfaces along with their public methods
public class PublicElementsPrinter extends VoidVisitorWithDefaults<Void> {
	private int nbMaxConnections = 0;
	private Set<String> setFieldsName;
	private Map<String, Set<String>> mapMethodsVariables;

	@Override
	public void visit(CompilationUnit unit, Void arg) {
		for (TypeDeclaration<?> type : unit.getTypes()) {
			type.accept(this, null);
		}
	}

	public void visitTypeDeclaration(TypeDeclaration<?> declaration, Void arg) {
		if (!declaration.isPublic())
			return;
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
		System.out.println("\n" + declaration.getFullyQualifiedName().orElse("[Anonymous]"));
		nbMaxConnections = 0;
		List<FieldDeclaration> listFields = declaration.getFields();
		ListIterator<FieldDeclaration> it = listFields.listIterator();

		setFieldsName = new HashSet<String>();
		mapMethodsVariables = new HashMap<String, Set<String>>();
		boolean isAbstract = declaration.isAbstract();

		// Add all the names of the variable in a list in the current class
		if (!isAbstract) {
			while (it.hasNext()) {
				FieldDeclaration current = it.next();
				setFieldsName.add(current.getVariable(0).getNameAsString());
			}
		}
		//D:\Dossier_Esir_Travail\ESIR\ESIR_2\MDI\commons-collections-master\srcSystem.out.println("Fields in class : " + setFieldsName);

		visitTypeDeclaration(declaration, arg);
		nbMaxConnections = (nbMaxConnections * (nbMaxConnections - 1)) / 2;
		System.out.println("TCC = " + directConnections() + "/" + nbMaxConnections);
	}

	@Override
	public void visit(EnumDeclaration declaration, Void arg) {
		visitTypeDeclaration(declaration, arg);
	}

	@Override
	public void visit(MethodDeclaration declaration, Void arg) {
		if (!declaration.isPublic())
			return;

		List<NameExpr> listNameExpr = declaration.findAll(NameExpr.class);
		ListIterator<NameExpr> itNameExpr = listNameExpr.listIterator();

		List<FieldAccessExpr> listFieldAccessExpr = declaration.findAll(FieldAccessExpr.class);
		ListIterator<FieldAccessExpr> itFieldAccess = listFieldAccessExpr.listIterator();

		List<String> listNameExprString = new ArrayList<String>();

		// variables used in method
		while (itNameExpr.hasNext()) {
			NameExpr current = itNameExpr.next();
			listNameExprString.add(current.getNameAsString());
		}

		// variable accessed from scope in method
		while (itFieldAccess.hasNext()) {
			FieldAccessExpr current = itFieldAccess.next();
			listNameExprString.add(current.getNameAsString());
		}
		// Keep only attributes from the class
		listNameExprString.retainAll(setFieldsName);
		Set<String> varSet = new HashSet<String>(listNameExprString);
		// Add a set of variables from a method that is the key
		mapMethodsVariables.put(declaration.getNameAsString(), varSet);
		// Count the number of methods
		nbMaxConnections++;
	}

	/**
	 * Return the number of direct connections of methods in a class
	 * 
	 * @return an int of the number of comparison
	 */
	public int directConnections() {
		int res = 0;
		// Index of the current method variables
		int primaryIndex = 0;

		// Current set of variables
		for (Set<String> setMethodVariables : mapMethodsVariables.values()) {
			// Index of the method variables to compare
			int secondaryIndex = 0;
			// Other sets of variables
			for (Set<String> setOtherMethodVariables : mapMethodsVariables.values()) {
				// Verify if we don't do the same comparison multiple times
				if (secondaryIndex > primaryIndex) {
					Set<String> intersection = new HashSet<String>(setMethodVariables);
					intersection.retainAll(setOtherMethodVariables);
					if (!intersection.isEmpty()) {
						res++;
					}
				}
				secondaryIndex++;
			}
			primaryIndex++;
		}
		return res;
	}

}