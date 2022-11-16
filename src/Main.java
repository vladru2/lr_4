import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        CustomClassLoader customClassLoader = new CustomClassLoader();
        Class<?> cl = customClassLoader.findClass("ClassTest");
        Object ob = cl.newInstance();
        Method method = cl.getMethod("test");
        method.invoke(ob);
    }
}