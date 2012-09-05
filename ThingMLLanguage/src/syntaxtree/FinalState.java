package syntaxtree;

public class FinalState extends States {

	String name;
	States next;
	int offset;

	public FinalState(String name, States next, int offset) {
		this.name = name;
		this.next = next;
		this.offset = offset;
	}

	public String printAst() {
		String ast = "(Final " + name + ")\n\t";
		if (next != null)
			ast += next.printAst();
		return ast;
	}
}
