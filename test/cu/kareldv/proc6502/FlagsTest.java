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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Karel
 */
public class FlagsTest {
    
    public FlagsTest() {
    }

    @Test
    public void testValue() {
        System.out.println("Testing sets values");
        Flags instance = new Flags();
        instance.c((byte)1);
        instance.z((byte)1);
        byte expResult = 0b0000_0011;
        byte result = instance.value();
        assertEquals(expResult, result);
        System.out.println("OK");
    }

    @Test
    public void testCopy() {
        System.out.println("Testing copy values...");
        Flags instance = new Flags();
        instance.c((byte)1);
        instance.z((byte)1);
        byte expResult = 0b0000_0011;
        byte result = new Flags(instance).value();
        assertEquals(expResult, result);
        System.out.println("OK");
    }
}
