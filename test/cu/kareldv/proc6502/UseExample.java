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

/**
 *
 * @author Karel
 */
public final class UseExample {
    public static void main(String[] args) {
        UseExample inst = new UseExample();
        inst.runLoadInstructions();
    }
    
    public void runLoadInstructions(){
        /**
         * Work with Load/Store/Bitwise AND operations
         *  lda #$f
         *  sta $200
         *  ldx $200
         *  stx $201
         *  ldy $201
         *  sty $202
         *  and #$02
         *  sta $200
         */
        final int[] data = {0xa9, 0x0f, 0x8d, 0x00, 0x02, 0xae, 0x00,
            0x02, 0x8e, 0x01, 0x02, 0xac, 0x01, 0x02, 0x8c, 0x02,
            0x02, 0x29, 0x02, 0x8d, 0x00, 0x02};
        byte[] theData = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            theData[i] = (byte) (data[i] & 0xff);
        }
        //The above step should be ommited, it only loads some ByteCodes to run
        //on the processor
        
        //Initialize Processor
        CPU cpu = CPU.newInstance();
        cpu.setup(true, true, true, 0x600);
        cpu.memory().loadBytes(theData, theData.length, 0x600);
        cpu.executeAsync(() -> {
            //Executed, lets check the memory to verify
            //it runned correctly
            Memory mem = cpu.memory();
            
            
            assert(mem.get(0x200) == 0x02);
            assert(mem.get(0x201) == 0x0f);
            assert(mem.get(0x202) == 0x0f);
            System.out.println("[*] Everything is OK onm Load/Store/BotwiseAnD operations");
        });
    }
}
