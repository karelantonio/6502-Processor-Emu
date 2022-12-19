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
public final class Clock {
    private boolean noSleep;
    private long sleepTime;
    private final Object sync = new Object();

    /**
     * Default constructor, initializes with sleepTime=0
     */
    public Clock() {
        //No Sleep
        noSleep=true;
        sleepTime=0;
    }

    /**
     * Second constructor, initialices with the given sleepTime
     * @param sleepTime 
     */
    public Clock(long sleepTime) {
        if (sleepTime<0){
            throw new IllegalArgumentException("Sleep time cannot be negative!");
        }
        if (sleepTime==0){
            noSleep=true;
            this.sleepTime=sleepTime;
        }else{
            noSleep=false;
            this.sleepTime=sleepTime;
        }
    }
    
    /**
     * Consumes one clock tick
     * @return  This
     */
    public Clock consumeTick() {
        if(noSleep)return this;
        
        synchronized(sync){
            try {
                sync.wait(sleepTime);
            } catch (InterruptedException ex) {
                //Pass, it was interrupted
            }
        }
        return this;
    }
    
    /**
     * Consumes some clock ticks
     * @param tickNum   The number of ticks
     * @return          This
     */
    public Clock consumeTicks(int tickNum) {
        if(noSleep)return this;
        
        synchronized(sync) {
            try {
                sync.wait(sleepTime*tickNum);
            } catch (InterruptedException ex) {
                //Pass, it was interrupted
            }
        }
        return this;
    }
    
    /**
     * Returns the sleep time, in milliseconds
     * @return The sleep time
     */
    public long sleepTime() {
        return sleepTime;
    }
    
    /**
     * Specifies the sleep time, in milliseconds
     * @param sleep The sleep time in milliseconds
     * @return      This
     */
    public Clock sleepTime(long sleep) {
        if (sleep<0) {
            throw new IllegalArgumentException("Sleep time cannot be negative!");
        }
        
        if (sleep==0){
            noSleep=true;
            sleepTime=sleep; //0
        }else{
            noSleep=false;
            sleepTime=sleep;
        }
        return this;
    }
    
    /**
     * Kills the current Clock sleep
     * @return This
     */
    public Clock killSleep() {
        synchronized(sync) {
            sync.notifyAll();
        }
        return this;
    }

    /**
     * Check wheter this object is equals OBJ
     * @param obj   Another object
     * @return      If equals or not
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Clock && ((Clock) obj).sleepTime==sleepTime;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"[sleepTime="+sleepTime+"]@"+hashCode();
    }
}
