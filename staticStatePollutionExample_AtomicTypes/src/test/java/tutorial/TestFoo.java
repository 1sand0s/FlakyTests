package tutorial;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestFoo{
    @Test
    public void test0() {
	Foo a0 = new Foo();
	assertTrue(a0.id == 0);
    }

    @Test
    public void test1() {
	Foo a0 = new Foo();
	assertTrue(a0.id == 0);
    }
}
