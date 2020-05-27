package net.guoyk.tune.elasticsearch;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new UpdateSiteTransformer());
    }

    private static class UpdateSiteTransformer implements ClassFileTransformer {

        private static final String CLASS_NAME = "org.elasticsearch.bootstrap.Natives";

        private static final String METHOD_NAME = "definitelyRunningAsRoot";

        private static final String METHOD_BODY = "{ return false; }";

        private static final String CLASS_NAME_INTERNAL = CLASS_NAME.replace('.', '/');

        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (className == null) {
                return null;
            }
            if (!CLASS_NAME_INTERNAL.equals(className)) {
                return null;
            }
            try {
                ClassPool cp = ClassPool.getDefault();
                cp.appendClassPath(new LoaderClassPath(loader));
                CtClass cc = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
                CtMethod cm = cc.getDeclaredMethod(METHOD_NAME);
                cm.setBody(METHOD_BODY);
                return cc.toBytecode();
            } catch (Exception e) {
                return null;
            }
        }

    }
}
