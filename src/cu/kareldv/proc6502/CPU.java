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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Karel
 */
public final class CPU {
    private Memory memory;
    private InstructionMap instMap;
    private Flags flags;
    private Registers registers;
    private Clock clock;
    private final List<PreInstruction> preInstr = new ArrayList<>();
    private final List<PostInstruction> postInstr = new ArrayList<>();
    private final ReentrantLock instructionLock = new ReentrantLock();
    
    public static final CPU newInstance() {
        CPU cpu = new CPU();
        //Setup the basics
        
        cpu.memory=new Memory();
        cpu.instMap=new InstructionMap();
        cpu.flags=new Flags();
        cpu.registers=new Registers();
        cpu.clock=new Clock(10);
        return cpu;
    }
    
    CPU(){
    }
    
    public Memory memory() {
        return memory;
    }
    
    public InstructionMap instructionMap() {
        return instMap;
    }
    
    public Flags flags() {
        return flags;
    }
    
    public Registers registers() {
        return registers;
    }
    
    public Clock clock() {
        return clock;
    }
    
    /**
     * Setups this Processor quickly
     * @return This
     */
    public CPU setup(boolean clearMem, boolean clearFlags, boolean clearRegisters, int newPC){
        if (clearMem){
            memory.reset();
        }
        if (clearFlags){
            flags=new Flags();
        }
        if(clearRegisters){
            registers=new Registers();
        }
        registers.regPC(newPC);
        return this;
    }
    
    /**
     * Setups this processor quickly
     * @return This
     */
    public CPU setup(boolean clearMem, boolean clearFlags, boolean clearRegisters){
        if (clearMem){
            memory.reset();
        }
        if (clearFlags){
            flags=new Flags();
        }
        if(clearRegisters){
            registers=new Registers();
        }
        return this;
    }
    
    /**
     * Executes the code until que processor notifies a Stop (BRK or error)
     * I do not recommend to use this, you should make your own way to execute
     * the code and get a better control of when to stop the execution, obtain
     * debug info, etc
     * @param onFinished    Runnable to execute when finished
     * @return  This
     */
    public CPU executeAsync(Runnable onFinished){
        new Thread(() -> {
            while(step()){
                //Nothing
            }
            if (onFinished!=null){
                onFinished.run();
            }
        }).start();
        return this;
    }
    
    /**
     * Executes the instruction and increments the program counter
     * @return 
     */
    public boolean step(){
        instructionLock.lock();
        try {
            final byte instr = popByte();
            callPreInstr(instr);
            final boolean result = instMap.eval(instr, this);
            callPostInstr(instr);
            return result;
        } finally {
            instructionLock.unlock();
        }
    }
    
    /**
     * Gets a byte from zero page
     * @return  The byte at pos: pop
     */
    protected byte popZeroPage() {
        return memory.get(popByte());
    }
    
    /**
     * Gets a byte from zeropage + X
     * @return  The byte at pos: pop+X
     */
    protected byte popZeroPageX(){
        return memory.get( (popByte() + registers.regX()) &0xff);
    }
    
    /**
     * Gets a byte from zeropage + Y
     * @return  The byte at pos: pop+Y
     */
    protected byte popZeroPageY(){
        return memory.get( (popByte() + registers.regY()) &0xff);
    }
    
    protected byte popAbsolute(){
        return memory.get( popWord() );
    }
    
    protected byte popAbsoluteY(){
        return memory.get( popWord() + registers.regY());
    }
    
    protected byte popAbsoluteX(){
        return memory.get( popWord() + registers.regY());
    }
    
    /**
     * Pops the immediate byte
     * @return The immediate byte
     */
    protected byte popImmediate() {
        return popByte();
    }
    
    protected byte popIndirectX(){
        int zp = ( popZeroPage() + registers.regX()) &0xff;
        int pos = memory.getWord(zp);
        return memory.get(pos);
    }
    
    protected byte popIndirectY(){
        int zp = popZeroPage() ;
        int pos = memory.getWord(zp);
        return memory.get(pos + registers.regY());
    }
    
    /**
     * Pops a byte from memory
     */
    protected byte popByte() {
        return memory.get(registers.incRegPC());
    }
    
    /**
     * Pops a word from memory
     */
    protected int popWord() {
        try{
            return memory.getWord(registers.incRegPC());
        }finally{
            registers.incRegPC();
        }
    }

    private void callPreInstr(byte instr) {
        preInstr.forEach((preInstruction) -> {
            preInstruction.execute(this, instr);
        });
    }

    private void callPostInstr(byte instr) {
        postInstr.forEach((postInstruction) -> {
            postInstruction.execute(this, instr);
        });
    }

    
    /**
     * Interface that gets called before an instruction is executed
     */
    public static interface PreInstruction{
        public void execute(CPU cpu, byte instr);
    }
    
    /**
     * Interface that gets called after an instruction is executed
     */
    public static interface PostInstruction{
        public void execute(CPU cpu, byte instr);
    }
}
