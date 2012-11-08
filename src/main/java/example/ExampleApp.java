/*
 * Copyright (c) XIAOWEI CHEN, 2009.
 * All Rights Reserved. Reproduction in whole or in part is prohibited
 * without the written consent of the copyright owner.
 * 
 * XIAOWEI CHEN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. XIAOWEI CHEN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * All rights reserved.
 */
package example;

import com.candybon.memory.MemoryObserver;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Example on usage of the {@link MemoryObserver} utility.
 * 
 * @author XiaoweiChen
 */
public class ExampleApp {

    public static void calcSize(Object o) {
        long memShallow = MemoryObserver.shallowSizeOf(o);
        long memDeep = MemoryObserver.deepSizeOf(o);
        System.out.printf("%s, shallow=%d, deep=%d%n", o.getClass().getSimpleName(), memShallow, memDeep);
    }

    public static void main(String[] args) throws Exception {
        calcSize(Boolean.TRUE);

        Map<Integer, String> map = new HashMap<Integer, String>();
        calcSize(map);
        map.put(1, "One");
        calcSize(map);
        map.put(2, "Two");
        calcSize(map);

        map = new ConcurrentHashMap<Integer, String>();
        calcSize(map);
        map.put(1, "AAAAA");
        calcSize(map);
        map.put(2, "BBBBB");
        calcSize(map);

        TestObject to = new TestObject(45454, null);
        calcSize(to);
        to.setChild(new TestObject(446, null));
        calcSize(to);
    }
}