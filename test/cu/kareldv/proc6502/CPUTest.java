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
public class CPUTest {
    
    public CPUTest() {
    }

    @Test
    public void generalTest() {
        System.out.println("General test...");
        
        CPU mCpu = CPU.newInstance();
        mCpu.registers().regPC((short)0x5ff);
        mCpu.memory().put(0x600, (byte) 0xa9);
        mCpu.memory().put(0x601, (byte) 0xff);
        mCpu.memory().put(0x602, (byte) 0x85);
        mCpu.memory().put(0x603, (byte) 0x11);
        
        while(mCpu.step()){}
    }
}
