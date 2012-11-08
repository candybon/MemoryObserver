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

/**
 * Object for demonstrating the memory agent.
 * 
 * @author XiaoweiChen
 */
public class TestObject {

    private int value1;
    private TestObject child;

    public TestObject(int i, TestObject to) {
        this.value1 = i;
        this.child = to;
    }

    public TestObject getChild() {
        return this.child;
    }

    public void setChild(TestObject to) {
        this.child = to;
    }

    public int getValue1() {
        return this.value1;
    }

    public void setValue1(int value1) {
        this.value1 = value1;
    }
}
