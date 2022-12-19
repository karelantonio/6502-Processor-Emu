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
package cu.kareldv.proc6502.plugs;

import cu.kareldv.proc6502.CPU;
import java.security.SecureRandom;
import java.util.Random;

/**
 *
 * @author Karel
 */
public final class SetRandom implements CPU.PreInstruction, CPU.PostInstruction {
    private int address;
    private SecureRandom random=new SecureRandom();

    public SetRandom() {
        this(0xfe);
    }

    public SetRandom(int address) {
        this.address = address;
    }

    @Override
    public void execute(CPU cpu, byte instr) {
        cpu.memory().put(address, (byte) random.ints().findFirst().getAsInt());
    }
    
}
