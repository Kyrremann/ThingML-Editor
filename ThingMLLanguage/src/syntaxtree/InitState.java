package syntaxtree;

public class InitState extends States {

    String name;
    States next;
    int offset;

    public InitState(String name, States next, int offset) {
	this.name = name;
	this.next = next;
	this.offset = offset;
    }

    public String printAst() {
	String ast = "(Init " + name + ")\n\t";
	if (next != null)
	    ast += next.printAst();
	return ast;
    }

    public String toString() {
	return name;
    }
}
