package syntaxtree;

public class StateMachine {
	
	String name;
	States states;
	
	public StateMachine(String name, States states) {
		this.name = name;
		this.states = states;
	}
	
	public String printAst() {
		String ast = "(StateMachine " + name + "\n\t";
		if(states != null)
			ast += states.printAst();
		ast += ")";
		return ast;
	}
}