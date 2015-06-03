import java.io.IOException;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.*;

public class AddMain {

    static public void main(String args[]) {
        String className = (args.length >= 1) ? args[0] : "";
        JavaClass mod = null;
        try {
            mod = Repository.lookupClass(className);
        }
        catch (Exception e) {
            System.err.println("Could not get class " + className);
        }

        ClassGen modClass = new ClassGen(mod);
        ConstantPoolGen cp = modClass.getConstantPool();

        InstructionList il = new InstructionList();

        il.append(new GETSTATIC(cp.addFieldref("java.lang.System","out","Ljava/io/PrintStream;")));
        il.append(new PUSH(cp, "Hello World!"));
        il.append(new INVOKEVIRTUAL(cp.addMethodref("java.io.PrintStream","println","(Ljava/lang/String;)V")));
        il.append(new RETURN());

        MethodGen methodGen = new MethodGen(
            Constants.ACC_PUBLIC|Constants.ACC_STATIC, 
            Type.VOID, 
            new Type[]{new ArrayType(Type.STRING, 1)}, 
            new String[]{"args"}, 
            "main", 
            className, 
            il, 
            cp);

        methodGen.setMaxLocals();
        methodGen.setMaxStack();

        modClass.addMethod(methodGen.getMethod());
        modClass.update();

        try {
            JavaClass newClass = modClass.getJavaClass();
            String className2 = className.replace(".","/");
            newClass.dump(className2 + ".class");
            System.out.println("Class " + className + " modified");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}