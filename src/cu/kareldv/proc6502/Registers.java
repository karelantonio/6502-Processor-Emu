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
public final class Registers {
    private byte regA, regX, regY, regSP, regSR;
    private int regPC;

    public Registers() {
        this((byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(short)0);
    }

    public Registers(byte regA, byte regX, byte regY, byte regSP, byte regSR, short regPC) {
        this.regA = regA;
        this.regX = regX;
        this.regY = regY;
        this.regSP = regSP;
        this.regSR = regSR;
        this.regPC = regPC;
    }
    
    
    /**
     * Gets the accumulator register
     * @return Accumulator value
     */
    public byte regA() {
        return regA;
    }
    
    /**
     * Sets the Accumulator Register
     * @param value New Value
     * @return      This
     */
    public Registers regA(byte value) {
        regA=value;
        return this;
    }
    
    /**
     * Gets the X Register
     * @return X Register value
     */
    public byte regX(){
        return regX;
    }
    
    /**
     * Sets the X Register Value
     * @param regX  New value
     * @return      This
     */
    public Registers regX(byte regX){
        this.regX=regX;
        return this;
    }
    
    /**
     * Returns the Y register
     * @return  Y register value
     */
    public byte regY() {
        return regY;
    }
    
    /**
     * Sets the Y register
     * @param val   Value
     * @return      This
     */
    public Registers regY(byte val) {
        this.regY=val;
        return this;
    }
    
    /**
     * Gets the Program Counter Register
     * @return  The program counter
     */
    public int regPC() {
        return regPC;
    }
    
    /**
     * Sets the Program Counter register
     * @param val   New Value
     * @return      This
     */
    public Registers regPC(int val) {
        regPC = val;
        return this;
    }
    
    /**
     * Increments the Program Counter register, then returns it
     * @return  The Program Counter + 1
     */
    public int incRegPC() {
        regPC++;
        return regPC;
    }
    
    /**
     * Gets Stack Pointer
     * @return  The stack pointer
     */
    public byte regSP(){
        return regSP;
    }
    
    /**
     * Sets the stack pointer
     * @param sp    New stack pointer
     * @return      This
     */
    public Registers regSP(byte sp){
        regSP=sp;
        return this;
    }
    
    /**
     * Decrements the stack pointer
     * @return  This
     */
    public Registers decRegSP() {
        regSP--;
        return this;
    }
    
    /**
     * Increments the stack pointer
     * @return  This
     */
    public Registers incRegSP(){
        regSP++;
        return this;
    }
    
    public byte regSR(){
        return regSR;
    }
    
    public Registers regSR(byte regsr){
        regSR=regsr;
        return this;
    }
}
