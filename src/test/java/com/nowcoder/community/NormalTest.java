package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;

@RunWith(JUnit4.class)
public class NormalTest {

    @Test
    public void testPrint(){
        System.out.println("hello,mark tian");
    }

    @Test
    public void testPrint2(){
        System.out.println("hello,mark tian2");
    }

    @Test
    public void testEmpty(){
        Map<String,Object> map=new HashMap<>();
        System.out.println("isEmpty:"+map.isEmpty());
        System.out.println("==null"+(map==null));

        map.put("key","value");
        System.out.println("isEmpty:"+map.isEmpty());
        System.out.println("==null"+map==null);
    }
}
