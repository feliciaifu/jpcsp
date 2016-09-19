/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.HLE.modules;

import org.apache.log4j.Logger;

import jpcsp.HLE.HLEFunction;
import jpcsp.HLE.HLEModule;
import jpcsp.HLE.HLEUnimplemented;
import jpcsp.HLE.Modules;
import jpcsp.HLE.TPointer8;

public class sceChkreg extends HLEModule {
    public static Logger log = Modules.getLogger("sceChkreg");

    @HLEUnimplemented
    @HLEFunction(nid = 0x54495B19, version = 150)
    public int sceChkregCheckRegion() {
    	return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0x59F8491D, version = 150)
    public int sceChkregGetPsCode() {
    	return 0;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0x6894A027, version = 150)
    public int sceChkreg_driver_6894A027(TPointer8 unknown1, int unknown2) {
    	unknown1.setValue(1); // Fake value

    	return 0;
    }
}
