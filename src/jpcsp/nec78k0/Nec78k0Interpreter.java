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
package jpcsp.nec78k0;

import org.apache.log4j.Logger;

import jpcsp.Emulator;

/**
 * @author gid15
 *
 */
public class Nec78k0Interpreter {
	public static Logger log = Nec78k0Processor.log;
	private Nec78k0Processor processor;
	public static final int PC_END_RUN = 0xFFFF;
	private boolean exitInterpreter;
	private boolean inInterpreter;

	public Nec78k0Interpreter(Nec78k0Processor processor) {
		this.processor = processor;
		processor.setInterpreter(this);
	}

	public void run() {
		if (log.isDebugEnabled()) {
			log.debug(String.format("Interpreting 0x%04X", processor.getNextInstructionPc()));
		}

		inInterpreter = true;
		processor.checkPendingInterrupt();
		while (!Emulator.pause && !exitInterpreter && !processor.isNextInstructionPc(PC_END_RUN)) {
			processor.interpret();
		}
		inInterpreter = false;

		if (log.isDebugEnabled()) {
			log.debug(String.format("Exiting Nec78k0Interpreter loop at 0x%04X", processor.getNextInstructionPc()));
		}
		exitInterpreter = false;
	}

	public void exitInterpreter() {
		if (log.isDebugEnabled()) {
			log.debug(String.format("Request to exit Nec78k0Interpreter inInterpreter=%b", inInterpreter));
		}

		if (inInterpreter) {
			exitInterpreter = true;
		}
	}
}
