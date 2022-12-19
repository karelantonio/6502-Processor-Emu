/*
 * Copyright 2022 Karel Gonzalez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cu.kareldv.proc6502;

import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Karel
 */
public class MemoryTest {
    private Memory memory;
    
    public MemoryTest() {
        memory=new Memory();
    }

    @Test
    public void testChangeData() {
        System.out.println("Test Change Data");
        memory.put(2, (byte) 5);
        assertEquals(memory.get(2), (byte) 5);
    }

    @Test
    public void testGetWord() {
        System.out.println("Test get Word");
        memory.put(0, (byte)1).put(1, (byte)2);
        
        assertEquals(memory.getWord(0), 0b10000_0010);
    }

    @Test
    @After
    public void testDump() {
        System.out.println("Test Dump");
        
        System.out.println(memory.dump());
    }
}
