package syntaxtree;

public class FinalState extends States {
	
	String name;
	States next;
	
	public FinalState (String name, States next) {
		this.name = name;
		this.next = next;
	}
	
	public String printAst() {
		String ast = "(Final " + name + ")\n\t";
		if (next != null)
			ast += next.printAst();
		return ast;
	}
}