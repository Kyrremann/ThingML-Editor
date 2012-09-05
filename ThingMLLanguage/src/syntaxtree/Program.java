package syntaxtree;

public class Program {

	StateMachine stateMachine;

	public Program(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}

	public String printAst() {
		return stateMachine.printAst();
	}
}
