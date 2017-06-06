/**
 * @author Mr.Yuan
 * @since 2017/1/20.
 */
public class Demo {
    public void fun() {
        new Demo() {
            private String d;

            public void fun2() {
                new Demo() {
                };new Demo() {private int i;
                };
            }
        };
    }
}
