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
 * The flag register, also called processor status, is one of the six registers on the 6502
 * family. Composed by six one-bit registers, some instructions change it or leave it unchanged
 * @author Karel
 */
public final class Flags {
    
    private byte value;
    
    /**
     * Default constructor, all flags set to zero
     */
    public Flags() {
        value=0;
    }
    
    /**
     * Second constructor, copies the flags from {@code other}
     * @param other 
     */
    public Flags(Flags other){
        this.value=other.value;
    }
    
    /**
     * Gets the flags register, as a byte
     * @return the flags as a byte
     */
    public byte value(){
        return value;
    }
    
    /**
     * Sets the flags register
     * 
     */
    public Flags value(byte val){
        this.value=val;
        return this;
    }
    
    /**
     * Carry flag
     * - After ADC, this is the carry result of the addition
     * - After SBC o CMP this flag will be set if no borrow was the result, or alternatively a greater then or requal result
     * - After shift instruction (ASL, LSR, ROL, ROR), this contains the bit that was shifted out
     * - Increment/Decrement instructions doesnt affect the carry flag
     * - Can be set or cleared directly by SEC, CLC
     * @return 1 or 0
     */
    public byte c() {
        return get(0);
    }
    
    /**
     * Sets Carry flag
     * @see #c() 
     * @param val   0 or 1
     * @return      this
     */
    public Flags c(byte val) {
        set(0, val);
        return this;
    }
    
    /**
     * Zero flag
     * After most instructions that have a result, this flag will be either set or cleared
     * based on wheter or not that value is equal to zero
     * @return 1 or 0
     */
    public byte z() {
        return get(1);
    }
    
    /**
     * Sets Zero flag
     * @see #z() 
     * @param val   0 or 1
     * @return      this
     */
    public Flags z(byte val) {
        set(1, val);
        return this;
    }
    
    /**
     * Interrupt Disable:
     * - When set, all interrupts except NMI are disabled
     * - Can be set or cleared directly with SEI, CLI
     * - Automatically set by CPU when an IRQ is triggered, and restored to its previous state by RTI
     * - If the IRQ line is low (IRQ pending) when this flag is cleared, an interrupt will immediately be triggered
     * @return 1 or 0
     */
    public byte i() {
        return get(2);
    }
    
    /**
     * Sets interrupt flag
     * @see #i() 
     * @param val   1 or 0
     * @return      this
     */
    public Flags i(byte val){
        set(2, val);
        return this;
    }
    
    /**
     * Decimal
     * - On the NES, this flag has no effect
     * - On the original 6502, this flag causes some arithmetic instruction use binary-coded decimal representation to make base10 calculations easier
     * - Can be set or cleared directly with SED CLD
     * @return 1 or 0
     */
    public byte d() {
        return get(3);
    }
    
    /**
     * Sets decimal flag
     * @see #d() 
     * @param val   1 or 0
     * @return      this
     */
    public Flags d(byte val){
        set(3, val);
        return this;
    }
    
    /**
     * The B Flag
     * This doesnt represent an actual CPU register
     * @return 1 or 0
     */
    public byte b() {
        return get(4);
    }
    
    /**
     * Sets B flag
     * @see #b() 
     * @param val   1 or 0
     * @return      this
     */
    public Flags b(byte val){
        set(4, val);
        return this;
    }
    
    /**
     * Overflow
     * - ADC and SBC will set this flag if the signed result would be invalid, necessary for making signed comparisons
     * - BIT will load bit 6 of the addressed value directly into the V flag
     * - Can be ceared directly with CLV. There is no corresponding set instruction
     * @return 1 or 0
     */
    public byte v() {
        return get(5);
    }
    
    /**
     * Sets Overflow flag
     * @see #v() 
     * @param val   1 or 0
     * @return      this
     */
    public Flags v(byte val){
        set(5, val);
        return this;
    }
    
    /**
     * Negative
     * - After most instructions that have a value result, this flag will contain bit 7 of that result
     * - BIT will load bit 7 of the addressed value directly into the N flag
     * @return 1 or 0
     */
    public byte n() {
        return get(6);
    }

    /**
     * Sets Negative flag
     * @see #n() 
     * @param val   1 or 0
     * @return      this
     */
    public Flags n(byte val){
        set(6, val);
        return this;
    }
    
    /**
     * Internal use
     * Get the bit at given position
     * @param i Position
     * @return  1 or 0
     */
    private byte get(int i) {
        return (byte) ((value>>i) & 0x01);
    }
    
    /**
     * Internal use
     * Sets the given bit in the given position
     * @param pos
     * @param val 
     */
    private void set(int pos, byte val){
        // Example, set(2, 1)
        // 1111 1101 << 2+1 = 1111 1000 (AND value)
        // 1111 1101 >> 8-2 = 0000 0011 (AND value)
        //                  = 0000 0100 (val <<pos)
        
        int lpos = 0xff<<(pos+1);
        int rpos = 0xff>>(8-pos);
        value=(byte)(
                (value&lpos) +
                (val<<pos) +
                (value&rpos)
        );
    }
}
