package syntaxtree;

public class StateMachine {

	String name;
	States states;
	int offset;

	public StateMachine(String name, States states, int offset) {
		this.name = name;
		this.states = states;
		this.offset = offset;
	}

	public String printAst() {
		String ast = "(StateMachine " + name + "\n\t";
		if (states != null)
			ast += states.printAst();
		ast += ")";
		return ast;
	}
	
	public String toString() {
		return name + " - offset: " + offset;
	}
}
