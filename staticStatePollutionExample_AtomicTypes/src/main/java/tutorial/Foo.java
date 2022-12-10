package tutorial;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Foo{

    static AtomicInteger count1 = new AtomicInteger();
    static AtomicInteger count2 = new AtomicInteger(10);
    static AtomicLong count3 = new AtomicLong();
    static AtomicLong count4 = new AtomicLong(10);
    int id1;
    int id2;
    long id3;
    long id4;

    Foo(){
	id1 = count1.getAndIncrement();
	id2 = count2.getAndIncrement();
	id3 = count3.getAndIncrement();
	id4 = count4.getAndIncrement();
    }
}
