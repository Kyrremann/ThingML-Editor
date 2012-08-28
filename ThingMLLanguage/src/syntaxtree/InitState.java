package syntaxtree;

public class InitState extends States {
	
	String name;
	States next;
	
	public InitState (String name, States next) {
		this.name = name;
		this.next = next;
	}
	
	public String printAst() {
		String ast = "(Init " + name + ")\n\t";
		if (next != null)
			ast += next.printAst();
		return ast;
	}
}