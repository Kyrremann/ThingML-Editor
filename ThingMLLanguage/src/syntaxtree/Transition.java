package syntaxtree;

public class Transition {

	String name;
	String weight;
	Transition next;
	int offset;
	int weightOffset;

	public Transition(String weight, String name, Transition next, int offset,
			int weightOffset) {
		this.name = name;
		this.weight = weight;
		this.next = next;
		this.offset = offset;
		this.weightOffset = weightOffset;
	}

	public String printAst() {
		String ast = "(Transition \n\t\t\t(Weight " + weight
				+ ")\n\t\t\t(Target " + name + "))\n\t";
		if (next != null) {
			ast += "\t";
			ast += next.printAst();
		}
		return ast;
	}
}
