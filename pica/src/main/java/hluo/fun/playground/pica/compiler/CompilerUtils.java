package hluo.fun.playground.pica.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CompilerUtils
{
    private final JavaCompiler compiler;
    private final ClassFileManager classFileManager;

    public CompilerUtils()
    {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.classFileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
    }

    // compile java code from source
    // TODO: should save the file and bytecode somewhere in the file system for future debugging
    public ClassInfo compileSingleSource(String className, String sourceCode)
    {
        List<JavaFileObject> sourceFiles = new ArrayList<JavaFileObject>();
        sourceFiles.add(new CharSequenceJavaFileObject(className, sourceCode));
        List options = Arrays.asList("-classpath", System.getProperty("java.class.path"));
        compiler.getTask(null, classFileManager, null, options,
                null, sourceFiles).call();

        return new ClassInfo(className, classFileManager.getClassBytes());
    }

    public Class loadClass(ClassInfo classInfo)
            throws ClassNotFoundException
    {
        ByteArrayClassLoader classLoader = new ByteArrayClassLoader(classInfo.getByteCode());
        return classLoader.loadClass(classInfo.getClasssName());
    }

    // in-memory input source code
    private class CharSequenceJavaFileObject
            extends SimpleJavaFileObject
    {
        private CharSequence content;

        public CharSequenceJavaFileObject(String className,
                CharSequence content)
        {
            super(URI.create("string:///" + className.replace('.', '/')
                    + Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }

        public CharSequence getCharContent(
                boolean ignoreEncodingErrors)
        {
            return content;
        }
    }

    // in-memory output bytecode
    private class JavaClassObject
            extends SimpleJavaFileObject
    {
        protected final ByteArrayOutputStream bos =
                new ByteArrayOutputStream();

        public JavaClassObject(String name, Kind kind)
        {
            super(URI.create("string:///" + name.replace('.', '/')
                    + kind.extension), kind);
        }

        public byte[] getBytes()
        {
            return bos.toByteArray();
        }

        @Override
        public OutputStream openOutputStream()
        {
            return bos;
        }
    }

    // redirect output to JavaClassObject
    private class ClassFileManager
            extends
            ForwardingJavaFileManager
    {
        private JavaClassObject classObject;

        public ClassFileManager(StandardJavaFileManager
                standardManager)
        {
            super(standardManager);
        }

        public JavaFileObject getJavaFileForOutput(Location location,
                String className, JavaFileObject.Kind kind, FileObject sibling)
        {
            classObject = new JavaClassObject(className, kind);
            return classObject;
        }

        public byte[] getClassBytes()
        {
            return classObject.getBytes();
        }
    }

    private class ByteArrayClassLoader
            extends ClassLoader
    {
        private final byte[] bytes;

        public ByteArrayClassLoader(byte[] bytes)
        {
            this.bytes = bytes;
        }

        public Class findClass(String name)
        {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
