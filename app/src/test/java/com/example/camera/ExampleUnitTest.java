package com.example.camera;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        int  j;
        assertEquals(4, 2 + 2);

        for (int i=0;i<110;i++){
//            j=(i & 0x01);
//            j=i & 2;
            if ((i & 2 )!=0)
            System.out.println("j::"+"...."+"i::"+i);
        }
    }
}