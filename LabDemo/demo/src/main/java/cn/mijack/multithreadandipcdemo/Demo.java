package cn.mijack.multithreadandipcdemo;

/**
 * @author Mr.Yuan
 * @date 2017/5/18
 */
public class Demo {
    public void fun1() {
        int a = 1;
        if (a == 2) {
            System.out.println("s");
        }
    }

    public void fun2() {
        int a = 1;
        if (a == 2) {
            System.out.println("s");
        } else {
            System.out.println("b");
        }
    }
    public void fun3() {
        int a = 1;
        if (a == 2) {
            System.out.println("s");
            if (a==3){
                System.out.println("hello");
            }
        } else {
            System.out.println("b");
        }
    }
}
