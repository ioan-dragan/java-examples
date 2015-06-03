
import java.io.IOException;
import java.util.Arrays;

//import org.apache.bcell.classfile.ClassFormatException;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

public class insertCode {
	
	private static int sampleFiled;
	/**
	 * @param args
	 */
	public static void main(String[] args) throws ClassFormatException, IOException{
		// TODO Auto-generated method stub
		// Read the class file
	    String filename = null;
	    try{
		if (args.length < 1){
		    throw new IOException();
		}
		
		filename = args[0].substring(0,args[0].length()-6);
	    }
	    catch(IOException e){
		System.out.println("File not found! Try again :D");
		System.exit(-1);
	    }
		ClassParser parser = new ClassParser(filename.replace(".", "/")+".class"); 
		// parse the class file and store it into clasa
		JavaClass clasa = parser.parse();
		
		// create a new java class that we are planning to modify 
		ClassGen cgen = new ClassGen(clasa);
		// create a pool of constants. Here we can add new constants  
		ConstantPoolGen cpgen = new ConstantPoolGen(clasa.getConstantPool());
		
		//get the index of the class name (in our case HelloWorld)
		int index = clasa.getClassNameIndex();
		// compute the index in the new pool
		index = ((ConstantClass) cpgen.getConstant(index)).getNameIndex();
		//set the name of the class
		cpgen.setConstant(index, new ConstantUtf8(filename+"Modified"));
		
		//create a variable that holds our new method 
		MethodGen mg = null;
		//iterate over all the methods from the class
		for (int i=0; i < cgen.getMethods().length ; i++){
			//get the method at position i 
			Method m = cgen.getMethodAt(i);
			//if the method is the searched one
			if(m.getName().equals("test")){
				
				System.out.println("found it: "+ m.getName());
				//create a new MethodGen
				mg = new MethodGen(m, m.getName(), cpgen);
				//get he instruction list from this method
				InstructionList il = mg.getInstructionList();
				//in this instruction list we keep the ones that we want to use
				InstructionList copy_inst = new InstructionList();
				//iterate over the instruction list and copy the ones of interest
				for (InstructionHandle in:il.getInstructionHandles()){
					// in our case exclude only the return at the end of each function
					if ( !in.getInstruction().getName().equals("return")){
						copy_inst.append(in.getInstruction());
					}
				}
				
				//create a new System.out.println instruction
				copy_inst.append(new GETSTATIC(cpgen.addFieldref("java.lang.System","out","Ljava/io/PrintStream;")));
				//add the string that has to be printted out
		        copy_inst.append(new PUSH(cpgen, "This code was inserted!"));
		        // add the actual println command
		        copy_inst.append(new INVOKEVIRTUAL(cpgen.addMethodref("java.io.PrintStream","println","(Ljava/lang/String;)V")));
		        //add the return at the end of the method
		        copy_inst.append(new RETURN());
		        //update the instruction list
		        copy_inst.update();

		        //set the method instruction list
				mg.setInstructionList(copy_inst);
				//update method max's
				mg.setMaxLocals();
		        mg.setMaxStack();
		        //replace the method m from the class with the newly created method
		        cgen.replaceMethod(m, mg.getMethod());
		        cgen.setConstantPool(cpgen);
			    
		        //another way to do the last steps 
				
				 // create a new method with the header you are interested in
		        MethodGen methodGen = new MethodGen(
		                Constants.ACC_PUBLIC|Constants.ACC_STATIC, 
		                Type.VOID, 
		                null, 
		                 null,
		                "test", 
		                filename+"Modified", 
		                copy_inst, 
		                cpgen);
				//set the max's
		        methodGen.setMaxLocals();
		        methodGen.setMaxStack();
		        //replace the method m from the class with the newly created method
		        //cgen.replaceMethod(m, methodGen.getMethod());
				
				
		        cgen.update();
				
				//mg.setMaxStack();
				//cgen.setMethodAt(mg.getMethod(), i);
			}
			
		}
		
		
		//finished 
		JavaClass jc1 = cgen.getJavaClass();
		jc1.setConstantPool(cpgen.getFinalConstantPool());
		jc1.dump(filename+"Modified.class");
		
		System.out.println(clasa.getClassName());
		System.out.println(clasa.getFields());
		for (Method method:clasa.getMethods()){
			System.out.println(method.getName());
		}
	}

}
