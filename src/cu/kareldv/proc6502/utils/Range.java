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
package cu.kareldv.proc6502.utils;

/**
 *
 * @author Karel
 */
public final class Range {
    private int start, end;
    private boolean leftIncl, rightIncl;

    public Range(int start, int end, boolean leftIncl, boolean rightIncl) {
        this.start = start;
        this.end = end;
        this.leftIncl = leftIncl;
        this.rightIncl = rightIncl;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean isLeftIncl() {
        return leftIncl;
    }

    public void setLeftIncl(boolean leftIncl) {
        this.leftIncl = leftIncl;
    }

    public boolean isRightIncl() {
        return rightIncl;
    }

    public void setRightIncl(boolean rightIncl) {
        this.rightIncl = rightIncl;
    }
    
    public boolean contains(int val){
        boolean left;
        
        if(leftIncl){
            left=val>=start;
        }else left = val>start;
        
        boolean right;
        
        if(rightIncl){
            right=val<=end;
        }else right = val<end;
        
        return left&&right;
    }
}
