package syntaxtree;

public abstract class States {

	String name;
	States next;
	int offset;

	public abstract String printAst();
}
