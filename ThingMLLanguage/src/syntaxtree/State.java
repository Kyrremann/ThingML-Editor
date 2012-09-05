package syntaxtree;

public class State extends States {
    
    String name;
    Transition transitions;
    States next;
    int offset;
    
    public State(String name, Transition transitions, States next, int offset) {
	this.name = name;
	this.transitions = transitions;
	this.next = next;
	this.offset = offset;
    }

    public String printAst() {
	String ast = "(State " + name + "\n\t";
	if (transitions != null) {
	    ast += "\t";
	    ast += transitions.printAst();
	}
	ast += ")\n";
	if (next != null) {
	    ast += "\t";
	    ast += next.printAst();
	}
	return ast;
    }

    public String toString() {
	return name;
    }
}
