package compiler;

import java.io.*;

import syntaxtree.*;
import thingmlparser.*;

public class Compiler {
    private String inFilename = null;
    private String astFilename = null;
    public String syntaxError;
    public String error;

	public Compiler(String inFilename, String astFilename){
        this.inFilename = inFilename;
        this.astFilename = astFilename;
    }

    public int compile() throws Exception {
        InputStream inputStream = null;
        inputStream = new FileInputStream(this.inFilename);
        Lexer lexer = new Lexer(inputStream);
        parser parser = new parser(lexer);
        Program program;

        try {
            program = (Program) parser.parse().value;
        } catch (Exception e) {
            // Do something here?
            throw e;
        }
		writeAST(program);
        // Check semantics.
		// int semanticCode = program.checkSemantic();
		// if(semanticCode == 0) { // If it is all ok:
	    // writeAST(program);
            // return 0;
        // } else if (semanticCode == 1) { // If there is a SYNTAX ERROR (Should not get that for the tests):
            // return 1;
        // } else { // If there is a SEMANTIC ERROR (Should get that for the test with "_fail" in the name):
            // return 2;
        // }
		return 0;
    }

    private void writeAST(Program program) throws Exception {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.astFilename));
        bufferedWriter.write(program.printAst());
        bufferedWriter.close();
    }

    public static void main(String[] args) {
        Compiler compiler = new Compiler(args[0], args[1]); //, args[2]);
        int result;
        try {
            result = compiler.compile();
			System.out.println("Result " + result);
	
			if(result == 1){
                System.out.println(compiler.syntaxError);
            } else if(result == 2){
                System.out.println(compiler.error);
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e);
			e.printStackTrace();
            // If unknown error.
            System.exit(3);
        }
    }
}
