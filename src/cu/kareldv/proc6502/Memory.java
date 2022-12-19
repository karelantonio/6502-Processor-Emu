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

import cu.kareldv.proc6502.utils.Range;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Karel
 */
public final class Memory {
    /**
     * Memory available 64 KB
     */
    public static final int MEMORY = 64 * 1024;
    private final byte[] data = new byte[MEMORY];
    private final Map<Range, OnValueInRangeChanged> events = new HashMap<>();
    
    /**
     * Default constructor, creates an empty array
     */
    public Memory() {
    }
    
    /**
     * Copies the memory from {@code other}
     * @param other Other memory to copy data from
     */
    public Memory(Memory other) {
        System.arraycopy(other.data, 0, data, 0, MEMORY);
    }
    
    /**
     * Copies the data from the given array
     * @param data      Byte set, the data
     * @param startFrom Position where to start the copy
     */
    public Memory(byte[] data, int startFrom) {
        System.arraycopy(data, startFrom, this.data,
                Math.max(0, Math.min(data.length-startFrom, MEMORY)),
                MEMORY);
    }
    
    /**
     * Gets a byte from the data
     * @param pos   The position in range [0;MEMORY)
     * @return      The byte at the given position
     */
    public byte get(int pos){
        return data[Math.min(MEMORY-1, pos)];
    }
    
    /**
     * Gets a word from the data. A word is a number composed by two bytes
     * @param pos   Position
     * @return      The word at given position
     */
    public int getWord(int pos) {
        return (( get(pos)<<8 ) + ( get(pos+1)&0xff ))&0xffff;
    }
    
    /**
     * Puts a byte into the data, at given pos
     * @param pos   Position
     * @param b     Value to insert
     * @return      This
     */
    public Memory put(int pos, byte b) {
        data[Math.min(MEMORY-1, pos)]=b;
        notifyMemory(pos);
        return this;
    }
    
    
    /**
     * Dumps this memory's data as an hexdump
     */
    public String dump() {
        return dump(0, MEMORY-1, 16);
    }
    
    /**
     * Dumps this memory's data into a string, as an hexdump
     */
    public String dump(int start, int end, int lineSize) {
        StringBuilder bld = new StringBuilder();
        
        for (int i = start; i<end; i++) {
            
            //Line Number
            if(i%lineSize == 0){
                bld.append(String.format("%08X:", i+start));
            }
            
            //The bytes
            bld.append(String.format(" %02X", data[i]));
            
            //Insert last line break
            if ((i+1)%lineSize==0 && (i+1)!= end){
                bld.append(System.lineSeparator());
            }
        }
        
        return bld.toString();
    }
    
    public Memory addRangeWatched(int start, int end, OnValueInRangeChanged evnt){
        assert (start<end && start>=0 && end<MEMORY); //Assume
        assert (evnt!=null);
        events.put(new Range(start, end, true, true), evnt);
        return this;
    }
    
    public Memory removeRangeWatched(OnValueInRangeChanged evnt) {
        assert(evnt!=null);
        Range toDel = null;
        for (Range range : events.keySet()) {
            if (events.get(range).equals(evnt)){
                toDel=range;
                break;
            }
        }
        if(toDel!=null){
            events.remove(toDel);
        }
        return this;
    }
    
    private void notifyMemory(int pos){
        for (Range range : events.keySet()) {
            if(range.contains(pos)){
                events.get(range).changed(this, pos, get(pos));
            }
        }
    }
    
    /**
     * Loads the given bytes into memory
     * @param data  The bytes
     * @param len   Amount of bytes
     * @param pos   Position of memory
     * @return      This
     */
    public Memory loadBytes(byte[] data, int len, int pos) {
        System.arraycopy(data, 0, this.data, pos, len);
        return this;
    }
    
    /**
     * Loads the bytes from FILE and store at memory
     * @param data  File
     * @param len   How much bytes to read
     * @param pos   Position of the memory
     * @throws IOException  When an IO Error occurred
     * @return      This
     */
    public Memory loadBytes(File data, int len, int pos) throws IOException{
        //Assume the developer already checked the bounds of Len and Pos to avoid
        //ArrayIndexOutOfBounds, and checked it exists/is not a folder :)
        FileInputStream in = new FileInputStream(data);
        byte[] dataB = new byte[len];
        in.read(dataB);
        in.close();
        System.arraycopy(dataB, 0, this.data, pos, len);
        return this;
    }
    
    /**
     * Loads some bytes from the input stream and stores it at given position
     * @param in    Input stream
     * @throws IOException  When an IO Error occurred
     * @return      This
     */
    public Memory loadBytes(InputStream in, int len, int pos) throws IOException{
        //Assume Pos is in range [60,MEMORY)
        //could cause an ArrayIndexOutOfBounds
        for (int i = 0; i < len; i++) {
            data[pos+i]=(byte) (in.read()&0xff);
        }
        //Done
        return this;
    }
    
    /**
     * Gets the memory
     * @return The bytes of the memory
     */
    public byte[] memory(){
        return data;
    }
    
    /**
     * Resets this memory, filling any position with 0
     * @return  This
     */
    public Memory reset(){
        for (int i = 0; i < data.length; i++) {
            data[i]=0;
        }
        return this;
    }
    
    /**
     * Fills all the positions with the given VAL
     * @param val   Value to fill the memory positions
     * @return      This
     */
    public Memory reset(byte val){
        for (int i = 0; i < data.length; i++) {
            data[i]=val;
        }
        return this;
    }
    
    /**
     * Interface that notifies when a value in a given range has changed
    **/
    public static interface OnValueInRangeChanged{
        public void changed(Memory mem, int pos, byte newVal);
    }
}
