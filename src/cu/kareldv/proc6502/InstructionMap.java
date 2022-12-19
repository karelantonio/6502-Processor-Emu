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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Karel
 */
public final class InstructionMap {
    private static final Map<String, Instruction> instructions = new HashMap<>();
    
    protected boolean eval(byte val, CPU cpu) {
        String asHex = ","+Integer.toHexString(val&0xff);
        for(String key: instructions.keySet()) {
            if (key.contains(asHex)) {
                instructions.get(key).consume(val, cpu.registers().regPC(), cpu);
                if(val==0){
                    return false; //BRK
                }
                return true;
            }
        }
        //Unkown opcode
        return false;
    }
    
    
    static {
        //Initialize instructions
        //IncDec
        instructions.put(",e6,f6,ee,fe,c6,d6,ce,de", InstructionMap::incdec);
        //Brk
        instructions.put(",0", InstructionMap::brk);
        //Bit
        instructions.put(",24,2c", InstructionMap::bit);
        //Rotate
        instructions.put(",2a,26,36,2e,3e,6a,66,76,6e,7e", InstructionMap::rotate);
        //Shift
        instructions.put(",a,6,16,e,1e,4a,46,56,4e,5e", InstructionMap::shift);
        //Branch
        instructions.put(",10,30,50,70,90,b0,d0,f0", InstructionMap::branch);
        //Registers
        instructions.put(",aa,8a,ca,e8,a8,98,88,c8", InstructionMap::registers);
        //Flags
        instructions.put(",18,38,58,78,b8,d8,f8", InstructionMap::flags);
        //Stack instructions
        instructions.put(",9a,ba,48,68,08,28,", InstructionMap::stack);
        //Jump
        instructions.put(",4c,6c,20", InstructionMap::jump);
        //Compare
        instructions.put(",c9,c5,d5,cd,dd,d9,c1,d1,e0,e4,ec,c0,c4,cc", InstructionMap::compare);
        //Store
        instructions.put(",85,95,8d,9d,99,81,91,86,96,8e,84,94,8c", InstructionMap::store);
        //Load
        instructions.put(",a9,a5,b5,ad,bd,b9,a1,b1,a2,a6,b6,ae,be,a0,a4,b4,ac,bc", InstructionMap::load);
        //ADC
        instructions.put(",69,65,75,6d,7d,79,61,71", InstructionMap::adc);
        //AND
        instructions.put(",29,25,35,2d,3d,39,21,31", InstructionMap::and);
        //NOP
        instructions.put(",ea", InstructionMap::nop);
        //SBC
        instructions.put(",e9,e5,f5,ed,fd,f9,e1,f1", InstructionMap::sbc);
        //EOR
        instructions.put(",49,45,55,4d,5d,59,41,51", InstructionMap::eor);
        //ORA
        instructions.put(",9,5,15,d,1d,19,1,11", InstructionMap::ora);
        //RTI
        instructions.put(",40", InstructionMap::rti);
        //RTS
        instructions.put(",60", InstructionMap::rts);
    }
    
    /**
     * Test BIT
     */
    private static final void bit(byte instr, int pos, CPU cpu){
        switch(instr&0xff){
            case 0x24:
                BIT(cpu, cpu.popZeroPage());
                cpu.clock().consumeTicks(3);
                break;
            case 0x2c:
                BIT(cpu, cpu.popAbsolute());
                cpu.clock().consumeTicks(4);
                break;
        }
    }
    
    /**
     * Rotate left and right
     */
    private static final void rotate(byte instr, int pos, CPU cpu) {
        Registers r = cpu.registers();
        Memory m = cpu.memory();
        switch(instr&0xff){
            //ROL
            case 0x2a: //Accumulator
                r.regA((byte) doRol(cpu, r.regA()));
                cpu.clock().consumeTicks(2);
                break;
            case 0x26: //Zero Page
                int addr = cpu.popByte();
                m.put(addr,
                        (byte) doRol(cpu, m.get(pos)));
                cpu.clock().consumeTicks(5);
                break;
            case 0x36: //Zero Page, X
                int addr2 = cpu.popByte() + r.regX();
                m.put(addr2 & 0xff,
                        (byte) doRol(cpu, m.get(pos)));
                cpu.clock().consumeTicks(6);
                break;
            case 0x2e: //Absolute
                int addr3 = cpu.popWord();
                m.put(addr3,
                        (byte) doRol(cpu, m.get(addr3)));
                cpu.clock().consumeTicks(6);
                break;
            case 0x3e: //Absolute, X
               int addr4 = cpu.popWord() + r.regX();
                m.put(addr4,
                        (byte) doRol(cpu, m.get(addr4)));
                cpu.clock().consumeTicks(7);
                break;
            //ROR
            case 0x6a: //Accumulator
                r.regA((byte) doRor(cpu, r.regA()));
                cpu.clock().consumeTicks(2);
                break;
            case 0x66: //Zero Page
                int addr5 = cpu.popByte();
                m.put(addr5,
                        (byte) doRor(cpu, m.get(pos)));
                cpu.clock().consumeTicks(5);
                break;
            case 0x76: //Zero Page, X
                int addr6 = cpu.popByte() + r.regX();
                m.put(addr6 & 0xff,
                        (byte) doRor(cpu, m.get(pos)));
                cpu.clock().consumeTicks(6);
                break;
            case 0x6e: //Absolute
                int addr7 = cpu.popWord();
                m.put(addr7,
                        (byte) doRor(cpu, m.get(addr7)));
                cpu.clock().consumeTicks(6);
                break;
            case 0x7e: //Absolute, X
               int addr8 = cpu.popWord() + r.regX();
                m.put(addr8,
                        (byte) doRor(cpu, m.get(addr8)));
                cpu.clock().consumeTicks(7);
                break;
        }
    }
    
    /**
     * Logical and arithmetic shift
     */
    private static final void shift(byte instr, int pos, CPU cpu){
        Registers r = cpu.registers();
        switch(instr&0xff){
            //Arithmetic Shift Left
            case 0x0a: //Accumulator
                setCarryFlagForBit7(r.regA(), cpu);
                r.regA((byte) ((r.regA()<<1)&0xff));
                setNZforA(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0x06: //Zero Page
                byte zp = cpu.popByte();
                byte zpval = cpu.memory().get(zp);
                setCarryFlagForBit7(zpval, cpu);
                zpval<<=1;
                cpu.memory().put(zp, zpval);
                setNZ(cpu, zpval);
                cpu.clock().consumeTicks(5);
                break;
            case 0x16: //Zero Page, X
                byte zpx = (byte) ((cpu.popByte() + r.regX()) & 0xff);
                byte zpxval = cpu.memory().get(zpx);
                setCarryFlagForBit7(zpxval, cpu);
                zpxval<<=1;
                cpu.memory().put(zpx, zpxval);
                setNZ(cpu, zpxval);
                cpu.clock().consumeTicks(6);
                break;
            case 0x0e: //Absolute
                int aaddr = cpu.popWord();
                byte aval = cpu.memory().get(aaddr);
                setCarryFlagForBit7(aval, cpu);
                aval<<=1;
                cpu.memory().put(aaddr, aval);
                setNZ(cpu, aval);
                cpu.clock().consumeTicks(6);
                break;
            case 0x13: //Absolute, X
                int axaddr = cpu.popWord() + r.regX();
                byte axval = cpu.memory().get(axaddr);
                setCarryFlagForBit7(axval, cpu);
                axval<<=1;
                cpu.memory().put(axaddr, axval);
                setNZ(cpu, axval);
                cpu.clock().consumeTicks(7);
                break;
            //LSR
            case 0x4a: //Accumulator
                setCarryFlagForBit0(r.regA(), cpu);
                r.regA((byte) (r.regA()<<1));
                setNZ(cpu, r.regA());
                cpu.clock().consumeTicks(2);
                break;
            case 0x46: //Zero Page
                int zpaddr = cpu.popByte();
                byte zpval1 = cpu.memory().get(zpaddr);
                setCarryFlagForBit0(zpval1, cpu);
                zpval1>>=1;
                cpu.memory().put(zpaddr, zpval1);
                setNZ(cpu, zpval1);
                cpu.clock().consumeTicks(5);
                break;
            case 0x56: //Zero Page, X
                int zpaddr2 = cpu.popByte() + r.regX();
                byte zpxval1 = cpu.memory().get(zpaddr2& 0xff);
                setCarryFlagForBit0(zpxval1, cpu);
                zpxval1>>=1;
                cpu.memory().put(zpaddr2, zpxval1);
                setNZ(cpu, zpxval1);
                cpu.clock().consumeTicks(6);
                break;
            case 0x4e: //Absolute
                int lsaddr = cpu.popWord();
                byte lsval = cpu.memory().get(lsaddr);
                setCarryFlagForBit0(lsval, cpu);
                lsval>>=1;
                cpu.memory().put(lsaddr, lsval);
                setNZ(cpu, lsval);
                cpu.clock().consumeTicks(6);
                break;
            case 0x5e: //Absolute, X
                int lsaddr1 = cpu.popWord() + r.regX();
                byte lsval1 = cpu.memory().get(lsaddr1);
                setCarryFlagForBit0(lsval1, cpu);
                lsval1>>=1;
                cpu.memory().put(lsaddr1, lsval1);
                setNZ(cpu, lsval1);
                cpu.clock().consumeTicks(7);
                break;
        }
    }
    
    /**
     * Branch instructions:
     * - BPL: Branch on plus
     * - BMI: Branch on minus
     * - BVC: Branch on Overflow clear
     * - BVS: Branch on overflow set
     * - BCC: Branch on carry clear
     * - BCS: Branch on carry set
     * - BNE: Branch on not equal
     * - BEQ: Branch on equal
     */
    private static final void branch(byte instr, int pos, CPU cpu) {
        switch(instr&0xff) {
            case 0x10: //Branch on Plus
                byte off10 = cpu.popByte();
                if ( cpu.flags().n() == 0 ){
                    jumpBranch(cpu, off10);
                    cpu.clock().consumeTicks(3);
                }else{
                    cpu.clock().consumeTicks(2);
                }
                break;
            case 0x30: //Branch on Minus
                byte off30 = cpu.popByte();
                if ( cpu.flags().n() != 0 ){
                    jumpBranch(cpu, off30);
                    cpu.clock().consumeTicks(3);
                }else{
                    cpu.clock().consumeTicks(2);
                }
                break;
            case 0x50: //Branch on Overflow Clear
                byte off50 = cpu.popByte();
                if (cpu.flags().v()==0) {
                    jumpBranch(cpu, off50);
                    cpu.clock().consumeTicks(3);
                }else{
                    cpu.clock().consumeTicks(2);
                }
                break;
            case 0x70: //Branch on Overflow Set
                byte off70 = cpu.popByte();
                if (cpu.flags().v()!=0) {
                    jumpBranch(cpu, off70);
                    cpu.clock().consumeTicks(3);
                }else{
                    cpu.clock().consumeTicks(2);
                }
                break;
            case 0x90: //Branch on carry clear
                byte off90 = cpu.popByte();
                if (cpu.flags().c()==0) {
                    jumpBranch(cpu, off90);
                    cpu.clock().consumeTicks(3);
                }else{
                    cpu.clock().consumeTicks(2);
                }
                break;
            case 0xb0: //Branch on carry set
                byte offb0 = cpu.popByte();
                if (cpu.flags().c()!=0) {
                    jumpBranch(cpu, offb0);
                    cpu.clock().consumeTicks(3);
                }else{
                    cpu.clock().consumeTicks(2);
                }
                break;
            case 0xd0: //Branch on not equal
                byte offd0 = cpu.popByte();
                if (cpu.flags().z()==0) {
                    jumpBranch(cpu, offd0);
                    cpu.clock().consumeTicks(3);
                }else{
                    cpu.clock().consumeTicks(2);
                }
                break;
            case 0xf0: //Branch on equal
                byte offf0 = cpu.popByte();
                if (cpu.flags().z()!=0) {
                    jumpBranch(cpu, offf0);
                    cpu.clock().consumeTicks(3);
                }else{
                    cpu.clock().consumeTicks(2);
                }
                break;
        }
    }
    
    /**
     * Register instructions
     */
    private static final void registers(byte instr, int pos, CPU cpu){
        Registers r = cpu.registers();
        switch(instr&0xff){
            case 0xaa: //TAX
                r.regX(r.regA());
                setNZforX(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0x8a: //TXA
                r.regA(r.regX());
                setNZforA(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0xca: //DEX
                r.regX((byte) ((r.regX()-1)&0xff));
                setNZforX(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0xe8: //INX
                r.regX((byte) ((r.regX()+1)&0xff));
                setNZforX(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0xa8: //TAY
                r.regY(r.regA());
                setNZforY(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0x98: //TYA
                r.regA(r.regY());
                setNZforA(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0x88: //DEY
                r.regY((byte) ((r.regY()-1)&0xff));
                setNZforY(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0xc8: //INY
                r.regY((byte) ((r.regY()+1)&0xff));
                setNZforY(cpu);
                cpu.clock().consumeTicks(2);
                break;
         }
    }
    
    /**
     * Flag Instructions
     */
    private static final void flags(byte instr, int pos, CPU cpu){
        Flags f = cpu.flags();
        switch(instr&0xff){
            case 0x18: //CLC
                f.c((byte)0);
                cpu.clock().consumeTicks(2);
                break;
            case 0x38: //SEC
                f.c((byte)1);
                cpu.clock().consumeTicks(2);
                break;
            case 0x58: //CLI
                f.i((byte)0);
                cpu.clock().consumeTicks(2);
                break;
            case 0x78: //SEI
                f.i((byte)1);
                cpu.clock().consumeTicks(2);
                break;
            case 0xb8: //CLV
                f.v((byte)0);
                cpu.clock().consumeTicks(2);
                break;
            case 0xd8: //CLD
                f.d((byte)0);
                cpu.clock().consumeTicks(2);
                break;
            case 0xf8: //SED
                f.c((byte)1);
                cpu.clock().consumeTicks(2);
                break;
        }
    }
    
    /**
     * Stack Instructions
     */
    private static final void stack(byte instr, int pos, CPU cpu){
        Registers r = cpu.registers();
        Memory m = cpu.memory();
        switch(instr&0xff){
            case 0x9a: //TXS
                r.regSP(r.regX());
                cpu.clock().consumeTicks(2);
                break;
            case 0xba: //TSX
                r.regX(r.regSP());
                setNZforX(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0x48: //PHA
                pushStack(cpu, r.regA());
                cpu.clock().consumeTicks(3);
                break;
            case 0x68: //PLA
                r.regA(popStack(cpu));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x08: //PHP
                pushStack(cpu, cpu.flags().value());
                cpu.clock().consumeTicks(3);
                break;
            case 0x28: //PLP
                cpu.flags().value();
                cpu.clock().consumeTicks(4);
                break;
        }
    }
    
    /**
     * Jump instructions
     */
    private static final void jump(byte instr, int pos, CPU cpu){
        Registers r = cpu.registers();
        switch(instr&0xff){
            case 0x4c: //JMP Absolute
                r.regPC((short) (cpu.popWord()&0xffff));
                cpu.clock().consumeTicks(3);
                break;
            case 0x6c: //JMP Indirect
                r.regPC((short)(cpu.memory().getWord(cpu.popWord())));
                cpu.clock().consumeTicks(5);
                break;
            case 0x20: //JSR
                int addr = cpu.popWord();
                int currAddr = r.regPC()-1;
                pushStack(cpu, (byte) ((currAddr>>8)&0xff) );
                pushStack(cpu, (byte) (currAddr&0xff));
                r.regPC((short) (addr&0xffff));
                cpu.clock().consumeTicks(6);
                break;
        }
    }
    
    /**
     * Compare accumulator, x and y registers
     */
    private static final void compare(byte instr, int pos, CPU cpu){
        Registers r = cpu.registers();
        switch(instr&0xff){
            //Compare A
            case 0xC9: //Immediate
                doCompare(cpu, r.regA(), cpu.popByte());
                cpu.clock().consumeTicks(2);
                break;
            case 0xC5:  //Zero P
                doCompare(cpu, r.regA(),
                        cpu.memory().get(cpu.popByte()));
                cpu.clock().consumeTicks(3);
                break;
            case 0xD5: //Zero P, X
                cpu.clock().consumeTicks(4);
                doCompare(cpu, r.regA(),
                        cpu.memory().get( (cpu.popByte() + r.regX())&0xff ));
                break;
            case 0xCD: //Absolute
                doCompare(cpu, r.regA(),
                        cpu.memory().get(cpu.popWord()));
                cpu.clock().consumeTicks(4);
                break;
            case 0xDD: //Absolute, X
                int addrdd = cpu.popWord() + r.regX();
                doCompare(cpu, r.regA(), cpu.memory().get(addrdd));
                cpu.clock().consumeTicks(4);
                break;
            case 0xD9: //Absolute, Y
                int addrd9 = cpu.popWord() + r.regY();
                doCompare(cpu, r.regA(), cpu.memory().get(addrd9));
                cpu.clock().consumeTicks(4);
                break;
            case 0xC1: //Indirect, X
                int zpc1 = (cpu.popByte() + r.regX() )&0xff;
                int addrc1 = cpu.memory().getWord(zpc1);
                doCompare(cpu, r.regA(), cpu.memory().get(addrc1));
                cpu.clock().consumeTicks(6);
                break;
            case 0xD1: //Indirect, Y
                int zpd1 = cpu.popByte();
                int addrd1 = cpu.memory().getWord(zpd1) + r.regY();
                doCompare(cpu, r.regA(), cpu.memory().get(addrd1));
                cpu.clock().consumeTicks(5);
                break;
            //Compare X
            case 0xe0: //Immediate
                doCompare(cpu, r.regX(), cpu.popByte());
                cpu.clock().consumeTicks(2);
                break;
            case 0xe4: //Zero Page
                doCompare(cpu, r.regX(),
                        cpu.memory().get(cpu.popByte()));
                cpu.clock().consumeTicks(3);
                break;
            case 0xec: //Absolute
                doCompare(cpu, r.regX(),
                        cpu.memory().get(cpu.popWord()));
                cpu.clock().consumeTicks(4);
                break;
            //Compare Y
            case 0xc0: //Immediate
                doCompare(cpu, r.regY(), cpu.popByte());
                cpu.clock().consumeTicks(2);
                break;
            case 0xc4: //Zero Page
                doCompare(cpu, r.regY(),
                        cpu.memory().get(cpu.popByte()));
                cpu.clock().consumeTicks(3);
                break;
            case 0xcc: //Absolute
                doCompare(cpu, r.regY(),
                        cpu.memory().get(cpu.popWord()));
                cpu.clock().consumeTicks(4);
        }
    }
    
    /**
     * Load Accumulator, X and Y registers
     */
    private static final void load(byte instr, int pos, CPU cpu) {
        final Registers r = cpu.registers();
        
        switch(instr&0xff) {
            //Accumulator
            case 0xa9: //Immediate
                //Loads the popped byte into the A register
                r.regA(
                        cpu.popByte()
                );
                setNZforA(cpu);
                cpu.clock().consumeTicks(2);
                break;
                
            case 0xa5: //Zero Page
                r.regA(
                        cpu.popZeroPage()
                );
                cpu.clock().consumeTicks(3);
                break;
                
            case 0xb5: //Zero page plus XRegister
                r.regA((byte)
                        cpu.popZeroPageX()
                 );
                cpu.clock().consumeTicks(4);
                break;
                
            case 0xad: //Absolute
                r.regA(
                        cpu.memory().get(cpu.popWord())
                );
                cpu.clock().consumeTicks(4);
                break;
                
            case 0xbd: //Absolute, X
                r.regA(cpu.memory().get(cpu.popWord()+r.regX()
                        )
                );
                cpu.clock().consumeTicks(4); //Ignore page boundary crossed
                break;
                
            case 0xb9: //Absolute, Y
                r.regA(cpu.memory().get(cpu.popWord()+r.regY()
                        )
                );
                cpu.clock().consumeTicks(4); //Ignore page boundary crossed
                break;
            
            case 0xa1: //Indirect, X
                int zp_x = (cpu.popByte() + r.regX() ) & 0xff;
                int addr_x = cpu.memory().getWord(zp_x);
                
                r.regA(
                        cpu.memory().get(addr_x)
                );
                cpu.clock().consumeTicks(6);
                break;
            
            case 0xb1: //Indirect, Y
                int zp_y = cpu.popByte();
                int addr_y = cpu.memory().getWord(zp_y) + r.regY();
                
                r.regA(
                        cpu.memory().get(addr_y)
                );
                cpu.clock().consumeTicks(5); //Ignore page boundary crossed
                break;
            //X
            case 0xa2: //Immediate
                r.regX(cpu.popImmediate());
                setNZforX(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0xa6: //Zero page
                r.regX(cpu.popZeroPage());
                setNZforX(cpu);
                cpu.clock().consumeTicks(3);
                break;
            case 0xb6: //Zero Page, Y
                r.regX(cpu.popZeroPage());
                setNZforX(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0xae: //Absolute
                r.regX(cpu.popAbsolute());
                setNZforX(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0xbe: //Absolute, Y
                r.regX(cpu.popAbsoluteY());
                setNZforX(cpu);
                cpu.clock().consumeTicks(4);
                break;
            //Y
            case 0xa0: //Immediate
                r.regY(cpu.popImmediate());
                setNZforY(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0xa4: //Zero Page
                r.regY(cpu.popZeroPage());
                setNZforY(cpu);
                cpu.clock().consumeTicks(3);
                break;
            case 0xb4: //Zero Page, X
                r.regY(cpu.popZeroPageX());
                setNZforY(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0xac: //Absolute
                r.regY(cpu.popAbsolute());
                setNZforY(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0xbc: //Absolute, X
                r.regY(cpu.popAbsoluteX());
                setNZforY(cpu);
                cpu.clock().consumeTicks(4);
                break;
        }
        
    }
    
    /**
     * Store Accumulator, X and Y registers
     */
    private static final void store(byte instr, int pos, CPU cpu){
        final Memory m = cpu.memory();
        final Registers r = cpu.registers();
        
        switch(instr&0xff){
            //Accumulator
            case 0x85: //Zero Page
                m.put(cpu.popByte(), r.regA());
                cpu.clock().consumeTicks(3);
                break;
            case 0x95: //Zero Page, X
                m.put(( cpu.popByte() + r.regX() )&0xff,
                        r.regA()
                );
                cpu.clock().consumeTicks(4);
                break;
            case 0x8d: //Absolute
                m.put(cpu.popWord(), r.regA());
                cpu.clock().consumeTicks(4);
                break;
            case 0x9d: //Absolute, X
                m.put(cpu.popWord() + r.regX(),
                        r.regA());
                cpu.clock().consumeTicks(5);
                break;
            case 0x99: //Absolute, Y
                m.put(cpu.popWord()+r.regY(),
                        r.regA()
                );
                cpu.clock().consumeTicks(5);
                break;
            case 0x81: //Indirect, X
                int zp81 = ( cpu.popByte() + r.regX() ) &0xff;
                int addr81 = m.getWord(zp81);
                m.put(addr81, r.regA());
                cpu.clock().consumeTicks(6);
                break;
            case 0x91: //Indirect, Y
                int zp91 = cpu.popByte();
                int addr91 = m.getWord(zp91) + r.regY();
                m.put(addr91, r.regA());
                cpu.clock().consumeTicks(6);
                break;
            //X
            case 0x86: //Zero Page
                m.put(cpu.popByte()&0xff, r.regX());
                cpu.clock().consumeTicks(3);
                break;
            case 0x96: //Zero Page, Y
                m.put((cpu.popByte() + r.regY())&0xff, r.regX());
                cpu.clock().consumeTicks(4);
                break;
            case 0x8e: //Absolute
                m.put(cpu.popWord(), r.regX());
                cpu.clock().consumeTicks(4);
                break;
            //Y
            case 0x84: //Zero Page
                m.put(cpu.popByte()&0xff, r.regY());
                cpu.clock().consumeTicks(3);
                break;
            case 0x94: //Zero Page, X
                m.put((cpu.popByte() + r.regX())&0xff, r.regY());
                cpu.clock().consumeTicks(4);
                break;
            case 0x8c: //Absolute
                m.put(cpu.popWord(), r.regY());
                cpu.clock().consumeTicks(4);
                break;
        }
    }
    
    /**
     * Add with Carry
     */
    private static final void adc(byte instr, int pos, CPU cpu) {
        Registers r = cpu.registers();
        Flags f = cpu.flags();
        switch(instr&0xff){
            case 0x69: //Immediate
                byte val69 = cpu.popByte();
                doADC(r, val69, f, cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0x65: //Zero Page
                byte addr65 = cpu.popByte();
                byte val65 = cpu.memory().get(addr65);
                doADC(r, val65, f, cpu);
                cpu.clock().consumeTicks(3);
                break;
            case 0x75: //Zero Page, X
                int addr75 = (cpu.popByte() + r.regX())&0xff;
                byte val75 = cpu.memory().get(addr75);
                doADC(r, val75, f, cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x6d: //Absolute
                int addr6d = cpu.popWord();
                byte val6d = cpu.memory().get(addr6d);
                doADC(r, val6d, f, cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x7d: //Absolute, X
                int addr7d = cpu.popWord();
                byte val7d = cpu.memory().get(addr7d+r.regX());
                doADC(r, val7d, f, cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x79: //Absolute, Y
                int addr79 = cpu.popWord();
                byte val79 = cpu.memory().get(addr79+r.regY());
                doADC(r, val79, f, cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x61: //Indirect, X
                int zp61 = (cpu.popByte()+r.regX())&0xff;
                int addr61 = cpu.memory().getWord(zp61);
                byte val61 = cpu.memory().get(addr61);
                doADC(r, val61, f, cpu);
                cpu.clock().consumeTicks(6);
                break;
            case 0x71: //Indirect, Y+
                int zp71 = (cpu.popByte()+r.regX())&0xff;
                int addr71 = cpu.memory().getWord(zp71);
                byte val71 = cpu.memory().get(addr71);
                doADC(r, val71, f, cpu);
                cpu.clock().consumeTicks(5);
                break;
        }
    }
    
    /**
     * Bitwise AND with accumulator
     */
    private static final void and(byte instr, int pos, CPU cpu) {
        Registers r = cpu.registers();
        Memory m = cpu.memory();
        switch(instr&0xff){
            case 0x29: //Immediate
                r.regA((byte) (r.regA() & cpu.popByte()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0x25: //Zero Page
                r.regA((byte) (
                        r.regA() & m.get(cpu.popByte() /*Zero Page*/)
                ));
                setNZforA(cpu);
                cpu.clock().consumeTicks(3);
                break;
            case 0x35: //Zero Page, X
                r.regA((byte) (
                        r.regA() & m.get(cpu.popByte() /*Zero Page*/ + r.regX())
                ));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x2D: //Absolute
                r.regA((byte) (
                        r.regA() & m.get(cpu.popWord())
                ));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x3D: //Absolute, X
                r.regA((byte) (
                        r.regA() & m.get(cpu.popWord() + r.regX())
                ));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x39: //Absolute, Y
                r.regA((byte) (
                        r.regA() & m.get(cpu.popWord() + r.regY())
                ));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x21: //Indirect, X
                int zp21 = (cpu.popByte() + r.regX())&0xff;
                int addr21 = m.getWord(zp21);
                r.regA((byte)(
                        r.regA() & m.get(addr21)
                ));
                setNZforA(cpu);
                cpu.clock().consumeTicks(6);
                break;
            case 0x31: //Indirect, Y
                int zp31 = cpu.popByte();
                int addr31 = m.getWord(zp31) + r.regY();
                r.regA((byte)(
                        r.regA() & m.get(addr31)
                ));
                setNZforA(cpu);
                cpu.clock().consumeTicks(5);
                break;
        }
    }
    
    /**
     * No Operation
     */
    private static final void nop(byte instr, int pos, CPU cpu) {
        switch(instr&0xff){
            case 0xea: //NOP
                cpu.clock().consumeTicks(2);
                break;
        }
    }
    
    /**
     * Substract with carry
     */
    private static final void sbc(byte instr, int pos, CPU cpu) {
        switch(instr&0xff){
            case 0xe9: //Immediate
                doSBC(cpu, cpu.popByte());
                cpu.clock().consumeTicks(2);
                break;
            case 0xe5: //Zero Page
                doSBC(cpu, cpu.popZeroPage());
                cpu.clock().consumeTicks(3);
                break;
            case 0xf5: //Zero Page, X
                doSBC(cpu, cpu.popZeroPageX());
                cpu.clock().consumeTicks(4);
                break;
            case 0xed: //Absolute
                doSBC(cpu, cpu.popAbsolute());
                cpu.clock().consumeTicks(4);
                break;
            case 0xfd: //Absolute, X
                doSBC(cpu, cpu.popAbsoluteX());
                cpu.clock().consumeTicks(4);
                break;
            case 0xf9: //Absolute, Y
                doSBC(cpu, cpu.popAbsoluteY());
                cpu.clock().consumeTicks(4);
                break;
            case 0xe1: //Indirect, X
                doSBC(cpu, cpu.popIndirectX());
                cpu.clock().consumeTicks(6);
                break;
            case 0xf1: //Indirect, Y
                doSBC(cpu, cpu.popIndirectY());
                cpu.clock().consumeTicks(5);
                break;
        }
    }
    
    /**
     * Exclusive OR
     */
    private static final void eor(byte instr, int pos, CPU cpu) {
        Registers r = cpu.registers();
        switch(instr&0xff){
            case 0x49: //Immediate
                r.regA((byte) (r.regA()^cpu.popByte()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0x45: //Zero Page
                r.regA((byte) (r.regA()^cpu.popZeroPage()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(3);
                break;
            case 0x55: //Zero Page, X
                r.regA((byte) (r.regA()^cpu.popZeroPage()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x4d: //Absolute
                r.regA((byte) (r.regA()^cpu.popAbsolute()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x5d: //Absolute, X
                r.regA((byte) (r.regA()^cpu.popAbsoluteX()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x59: //Absolute, Y
                r.regA((byte) (r.regA()^cpu.popAbsoluteY()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x41: //Indirect, X
                r.regA((byte) (r.regA()^cpu.popIndirectX()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(6);
                break;
            case 0x51: //Indirect, Y
                r.regA((byte) (r.regA()^cpu.popIndirectY()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(5);
                break;
                
        }
    }
    
    /**
     * OR with accumulator
     */
    private static final void ora(byte instr, int pos, CPU cpu) {
        Registers r = cpu.registers();
        switch(instr&0xff) {
            case 0x9: //Immediate
                r.regA((byte) (r.regA() | cpu.popByte()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(2);
                break;
            case 0x5: //Zero Page
                r.regA((byte) (r.regA() | cpu.popZeroPage()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(3);
                break;
            case 0x15: //Zero Page, X
                r.regA((byte) (r.regA() | cpu.popZeroPageX()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0xd: //Absolute
                r.regA((byte) (r.regA() | cpu.popAbsolute()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x1d: //Absolute, X
                r.regA((byte) (r.regA() | cpu.popAbsoluteX()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x19: //Absolute, Y
                r.regA((byte) (r.regA() | cpu.popAbsoluteY()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(4);
                break;
            case 0x1: //Indirect, X
                r.regA((byte) (r.regA() | cpu.popIndirectX()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(6);
                break;
            case 0x11: //Indirect, Y
                r.regA((byte) (r.regA() | cpu.popIndirectY()));
                setNZforA(cpu);
                cpu.clock().consumeTicks(5);
                break;
                
        }
    }
    
    /**
     * Break
     */
    private static final void brk(byte instr, int pos, CPU cpu){
        //Nada
        cpu.clock().consumeTicks(7);
    }
    
    /**
     * Return from interrupt
     */
    private static final void rti(byte instr, int pos, CPU cpu){
        switch(instr&0xff){
            case 0x40:
                int regP = cpu.flags().value();
                byte b1 = popStack(cpu), b2 = popStack(cpu), b3 = popStack(cpu);
                cpu.flags().value((byte) (regP | b1));
                cpu.registers().regPC(b2 | (b3<<8) );
                cpu.clock().consumeTicks(6);
                break;
        }
    }
    
    /**
     * Return from subroutine
     */
    private static final void rts(byte instr, int pos, CPU cpu){
        switch(instr&0xff){
            case 0x60:
                byte b1 = popStack(cpu), b2 = popStack(cpu);
                cpu.registers().regPC( (b1 | (b2<<8)) + 1);
                cpu.clock().consumeTicks(6);
                break;
        }
    }
    
    /**
     * Increment Decrement operations
     */
    private static final void incdec(byte instr, int pos, CPU cpu){
        switch(instr&0xff){
            //Dec
            case 0xc6: //Zero Page
                decrement(cpu, cpu.popByte());
                cpu.clock().consumeTicks(5);
                break;
            case 0xd6: //Zero Page, X
                decrement(cpu, ( cpu.popByte() + cpu.registers().regX() ) &0xff);
                cpu.clock().consumeTicks(6);
                break;
            case 0xce: //Absolute
                decrement(cpu, cpu.popWord());
                cpu.clock().consumeTicks(6);
                break;
            case 0xde: //Absolute, X
                decrement(cpu, cpu.popWord() + cpu.registers().regX());
                cpu.clock().consumeTicks(7);
                break;
            //Inc
            case 0xe6: //Zero Page
                increment(cpu, cpu.popByte());
                cpu.clock().consumeTicks(5);
                break;
            case 0xf6: //Zero Page, X
                increment(cpu, ( cpu.popByte() + cpu.registers().regX() ) &0xff);
                cpu.clock().consumeTicks(6);
                break;
            case 0xee: //Absolute
                increment(cpu, cpu.popWord());
                cpu.clock().consumeTicks(6);
                break;
            case 0xfe: //Absolute, X
                increment(cpu, cpu.popWord() + cpu.registers().regX());
                cpu.clock().consumeTicks(7);
                break;
        }
    }
    
    //---------------------------------------------------------
    
    private static final void decrement(CPU cpu, int pos){
        int val = cpu.memory().get(pos);
        val--;
        val&=0xff;
        setNZ(cpu, (byte) val);
        cpu.memory().put(pos, (byte) (val&0xff));
    }
    
    private static final void increment(CPU cpu, int pos){
        int val = cpu.memory().get(pos);
        val--;
        val&=0xff;
        setNZ(cpu, (byte) val);
        cpu.memory().put(pos, (byte) (val&0xff));
    }
    
    private static final int doRor(CPU cpu , byte val){
        int carrySet = cpu.flags().c();
        setCarryFlagForBit0(val, cpu);
        val>>=1;
        if (carrySet!=0) {
            val|=0x80;
        }
        setNZ(cpu, val);
        return val;
    }
    
    private static final int doRol(CPU cpu , byte val){
        int carrySet = cpu.flags().c();
        setCarryFlagForBit7(val, cpu);
        val<<=1;
        val+=carrySet;
        setNZ(cpu, val);
        return val;
    }
    
    private static final void doSBC(CPU cpu , byte val){
        int tmp, w;
        int regA = cpu.registers().regA();
        
        if ( ((regA^val)&0x80) !=0) {
            cpu.flags().v((byte)1);
        }else{
            cpu.flags().v((byte)0);
        }
        
        //If Decimal Mode Enabled
        if (cpu.flags().d()!= 0 ) {
            tmp = 0xf + (regA & 0xf) - (val & 0xf) + cpu.flags().c();
            if (tmp < 0x10) {
                w = 0;
                tmp -= 6;
            } else {
                w = 0x10;
                tmp -= 0x10;
            }
            
            w += 0xf0 + (regA & 0xf0) - (val & 0xf0);
            
            if (w < 0x100) {
                cpu.flags().c((byte)0);
                if ( /*Overflow set*/ cpu.flags().v()!=0 && w < 0x80) {
                    cpu.flags().v((byte)0);
                }
                w -= 0x60;
            } else {
                cpu.flags().c((byte)1); //Set carry
                if (/*Overflow set*/cpu.flags().v()!=0 && w >= 0x180) {
                    cpu.flags().v((byte)0);
                }
            }
            w += tmp;
        } else {
            w = 0xff + regA - val + cpu.flags().c();
            if (w < 0x100) {
                cpu.flags().c((byte)1);//Clear carry
                
                if (/*Overflow set*/ cpu.flags().v()!=0 && w < 0x80) {
                    cpu.flags().v((byte)0); //Clear overflow
                }
            } else {
                cpu.flags().c((byte)1); //Set carry
                if (/*Overflow set*/ cpu.flags().v()!=0 && w >= 0x180) {
                    cpu.flags().v((byte)0); //Clear overflow
                }
            }
        }
        cpu.registers().regA((byte) (w&0xff));
        setNZforA(cpu);
    }
    
    private static final void BIT(CPU cpu , byte val){
        int regP = cpu.flags().value();
        //Match Bit 8
        if ((val&0x80) != 0){
            regP|=0x80;
        }else{
            regP&=0x7f;
        }
        //Match Bit 7
        if ((val&0x40) != 0){
            regP|=0x40;
        }else{
            regP&= ~0x40;
        }
        //Match regA and value
        if ( (cpu.registers().regA() & val) != 0){
            regP&=0xfd;
        }else{
            regP|=0x02;
        }
        cpu.flags().value((byte) (regP&0xff));
    }
    
    private static final void setCarryFlagForBit0(byte val, CPU cpu){
        cpu.flags().c((byte) (val&0x1));
    }
    
    private static final void setCarryFlagForBit7(byte val, CPU cpu){
        cpu.flags().c((byte) ((val>>7)&0x1));
        
    }
    
    private static final void doCompare(CPU cpu, byte v1, byte v2){
        if (v1>= v2){
            cpu.flags().c((byte)1); //Set carry
        }else{
            cpu.flags().c((byte)0); //Clear carry
        }
        v2 = (byte) (v1-v2);
        setNZ(cpu, v2);
    }
    
    private static final void doADC(Registers r, byte val, Flags f, CPU cpu) {
        //Check if either regA or value have the most significant bit to 1
        if (((r.regA() ^ val)&0x80) != 0){
            f.v((byte)0);
        }else{
            f.v((byte)1);
        }
        
        int tmp = 0;
        
        //Lets do the sum
        if (f.d()!=0){//Decimal mode
            //  4-Bytes of RegA, 4 of value, and 1 if carry set
            tmp= r.regA()&0xf + val&0xf + f.c();
            
            if(tmp>=10){ //Diez porque es decimal :/
                tmp = 0x10 | ((tmp + 6) & 0xf);
            }
            
            tmp += (r.regA() & 0xf0) + (val & 0xf0);
            
            if (tmp >= 160) {
                f.c((byte)1); //Set carry
                if (f.v()!=0 && tmp >= 0x180) { f.v((byte)0); }
                tmp += 0x60;
            } else {
                f.c((byte)0);//Clear carry
                if (f.v()!=0 && tmp < 0x80) { f.v((byte)0); } //Clear overflow
            }
            tmp = r.regA() + val + f.c();
            
        }else{
            if (tmp >= 0x100) {
                f.c((byte)1); //Set carry
                if (f.v()!=0 && tmp >= 0x180) {  f.v((byte)0); } //Clear overflow
            } else {
                f.c((byte)0);//Clear carry;
                if (f.v()!=0 && tmp < 0x80) {  f.v((byte)0); } //Clear overflow
            }
        }
        
        r.regA((byte) (tmp&0xff));
        setNZforA(cpu);
    }
    
    private static final void pushStack(CPU cpu, byte val){
        cpu.memory().put( (cpu.registers().regSP()&0xff) + 0x100,
                        val);
    }
    
    private static final byte popStack(CPU cpu) {
        cpu.registers().incRegSP();
        return cpu.memory().get(cpu.registers().regSP()+0x100);
    }
    
    private static void jumpBranch(CPU cpu, int offset) {
        if (offset > 0x7f) { //Negative
            cpu.registers().regPC(
                    (short) (cpu.registers().regPC() - ( 0x100 - offset))
            );
        }else{
            cpu.registers().regPC(
                    (short) (cpu.registers().regPC() + offset)
            );
        }
    }
    
    /**
     * Sets negative or zero
     */
    private static void setNZ(CPU cpu, byte val) {
        if (val==0){
            cpu.flags().z((byte)1);
        }else{
            cpu.flags().z((byte)0);
        }
        
        //If negative bit, aka: 8th bit, is 0, the number is positive
        if( (val&0x80) == 0) {
            cpu.flags().n((byte)0);
        }else{
            cpu.flags().n((byte)1);
        }
    }
    
    private static void setNZforA(CPU cpu) {
        setNZ(cpu, cpu.registers().regA());
    }
    
    private static void setNZforX(CPU cpu) {
        setNZ(cpu, cpu.registers().regX());
    }
    
    private static void setNZforY(CPU cpu) {
        setNZ(cpu, cpu.registers().regY());
    }
    
    static interface Instruction{
        public void consume(byte instr, int pos, CPU cpu);
    }
}
