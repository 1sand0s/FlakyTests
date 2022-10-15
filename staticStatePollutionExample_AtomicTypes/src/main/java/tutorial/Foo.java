package tutorial;

import java.util.concurrent.atomic.AtomicInteger;

public class Foo{

    static AtomicInteger count = new AtomicInteger();
    int id;

    Foo(){
	id = count.getAndIncrement();
    }
}
