package cn.wjdiankong.hackcharles;

import java.io.File;
import java.io.FileOutputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class CrackCharles {

    private final static String PKGNAME = "com.xk72.charles";
    private final static String CLASSNAME = "kKPk";
    private final static String JAR_DIR = "E:/GitHub/CrackCharles/";//替换成你的目录
    private final static String JAR_NAME = "charles.jar";//替换成你的本地目录

    public static void main(String[] args) throws Exception {

        //经测试，这两个方法不能一起执行，需分开执行，否则会失败
        //方法一： 先执行：crackCharlesJar（）方法，2，再执行 execmd()方法即可；
        //方法二：在cmd中使用命令操作：jar uvf charles.jar com/xk72/charles/kKPk.class

//      crackCharlesJar();
        execmd();
    }

    private static void execmd() throws Exception {

        String classPath = PKGNAME.replace(".", "/") + "/" + CLASSNAME + ".class";
        Process process = Runtime.getRuntime().exec("jar uvf " + JAR_NAME + " "  + classPath);
        System.out.println("jar uvf " + JAR_NAME + " " + classPath);
        int status = process.waitFor();
        if (status == 0) {
            System.out.println("执行成功：status： " + status);
        } else {
            System.out.println("执行失败：status： " + status);
        }
    }

    private static void crackCharlesJar() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext(JAR_DIR + JAR_NAME); //发送事件
            }
        }).map(new Function<String, byte[]>() { //输入jar文件，输出操作之后的类字节码数组
            @Override
            public byte[] apply(String jarPath) throws Exception {
                ClassPool classPool = ClassPool.getDefault();
                classPool.insertClassPath(jarPath);
                CtClass ctClass = classPool.get(PKGNAME + "." + CLASSNAME);
                CtMethod ctMethod = ctClass.getDeclaredMethod("lcJx", null);
                ctMethod.setBody("{return true;}");
                ctMethod = ctClass.getDeclaredMethod("JZlU", null);
                ctMethod.setBody("{return \"Charles is success for crack !!!!!\";}");
                return ctClass.toBytecode();
            }
        }).map(new Function<byte[], String>() { // 输入字节码数组，保存文件，输出文件路径
            @Override
            public String apply(byte[] byteArray) throws Exception {
                String classPath = PKGNAME.replace(".", "/") + "/";
                File dirFile = new File(JAR_DIR + classPath + CLASSNAME + ".class");
                if (!dirFile.getParentFile().exists()) {
                    dirFile.getParentFile().mkdirs();
                }
                FileOutputStream output = new FileOutputStream(dirFile);
                output.write(byteArray);
                output.flush();
                output.close();
                return dirFile.getAbsolutePath();
            }
        }).map(new Function<String, Integer>() { //输入class路径，jar uvf命令替换jar
            @Override
            public Integer apply(String s) throws Exception {
                //                String classPath = PKGNAME.replace(".", "/") + "/" + CLASSNAME + ".class";
                //                Process process = Runtime.getRuntime().exec("jar uvf " + JAR_DIR + JAR_NAME + " " + JAR_DIR + classPath);
                //                 System.out.println("jar uvf " + JAR_DIR + JAR_NAME + " " + JAR_DIR + classPath);
                //                int status = process.waitFor();
                return 0;
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer status) throws Exception {
                if (status == 0) {
                    System.out.println("执行成功：status： " + status);
                } else {
                    System.out.println("执行失败：status： " + status);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                System.out.println("error:" + throwable.toString());
            }
        });
    }

}
