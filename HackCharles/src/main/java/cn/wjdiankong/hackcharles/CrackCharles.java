package cn.wjdiankong.hackcharles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    private final static String JAR_DIR = "E:/GitHub/CrackCharles/";//替换成你的目录（当前项目目录）
    private final static String JAR_NAME = "charles_src.jar";//替换成你的本地目录(当前项目目录下，原始的charles.jar文件)
    private final static String JAR_NAME_BUILD = "charles.jar";//（也放在当前项目目录下，最终替换生成的jar 文件,使用两个目录，否则是因为文件句柄未释放，导致操作不成功）

    public static void main(String[] args) throws Exception {

        crackCharlesJar();//破解Charles
        //      execmd();//其他命令执行测试


    }

    private static void execmd() throws Exception {

        String dir = "C:/Users/justi/Desktop/imsi.apk";

        System.out.println("apktool d " + dir);
        Process process = Runtime.getRuntime().exec("cmd /c apktool d imsi.apk");//出现找不到目录，需要指定（cmd /c）

        StringBuffer stringBuffer = new StringBuffer();
        InputStream is = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuffer.append(line + "\n");
        }
        int status = process.waitFor();
        is.close();
        reader.close();
        process.destroy();

        System.out.println("status = " + status);
        System.out.println("stringBuffer = " + stringBuffer);
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
                ctMethod.setBody("{return \"Charles is success for crack 300 year\";}");
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
                String classPath = PKGNAME.replace(".", "/") + "/" + CLASSNAME + ".class";
                Process process = Runtime.getRuntime().exec("cmd.exe /c jar uvf " + JAR_NAME_BUILD + " " + classPath);
                System.out.println("cmd /c jar uvf " + JAR_NAME_BUILD + " " + classPath);
                int status = process.waitFor();
                return status;
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
