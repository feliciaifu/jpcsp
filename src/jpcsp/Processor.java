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
package jpcsp;

import static jpcsp.Allegrex.BcuState.jumpTarget;
import static jpcsp.Allegrex.GprState.NUMBER_REGISTERS;

import java.nio.ByteBuffer;

import jpcsp.Allegrex.Common.Instruction;
import jpcsp.Allegrex.compiler.RuntimeContext;
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.Allegrex.Common;
import jpcsp.Allegrex.Cp0State;
import jpcsp.Allegrex.CpuState;
import jpcsp.Allegrex.Decoder;
import jpcsp.Allegrex.Instructions;

import org.apache.log4j.Logger;

public class Processor {
    public CpuState cpu = new CpuState();
    public Cp0State cp0 = new Cp0State();
    public static final Memory memory = Memory.getInstance();
    protected Logger log = Logger.getLogger("cpu");
    private boolean interruptsEnabled;

    public Processor() {
    	setLogger(log);
        reset();
    }

    protected void setLogger(Logger log) {
    	this.log = log;
    	cpu.setLogger(log);
    }

    public Logger getLogger() {
    	return log;
    }

    public void setCpu(CpuState cpu) {
    	this.cpu = cpu;
    }

    public void reset() {
    	interruptsEnabled = true;
        cpu.reset();
    }

    public void load(ByteBuffer buffer) {
        cpu.pc = buffer.getInt();
        cpu.npc = buffer.getInt();

        for (int i = 0; i < NUMBER_REGISTERS; i++) {
            cpu.setRegister(i, buffer.getInt());
        }
    }

    public void save(ByteBuffer buffer) {
        buffer.putInt(cpu.pc);
        buffer.putInt(cpu.npc);

        for (int i = 0; i < NUMBER_REGISTERS; i++) {
            buffer.putInt(cpu.getRegister(i));
        }
    }

    public Instruction interpret() {
        int opcode = cpu.fetchOpcode();
        Instruction insn = Decoder.instruction(opcode);
        if (log.isTraceEnabled()) {
        	log.trace(String.format("Interpreting 0x%08X: [0x%08X] - %s", cpu.pc - 4, opcode, insn.disasm(cpu.pc - 4, opcode)));
        }
        insn.interpret(this, opcode);

    	if (RuntimeContext.debugCodeBlockCalls) {
    		if (insn == Instructions.JAL) {
    			RuntimeContext.debugCodeBlockStart(cpu, cpu.pc);
    		} else if (insn == Instructions.JR && ((opcode >> 21) & 31) == Common._ra) {
    			int opcodeCaller = cpu.memory.read32(cpu._ra - 8);
    			Instruction insnCaller = Decoder.instruction(opcodeCaller);
    			int codeBlockStart = cpu.pc;
    			if (insnCaller == Instructions.JAL) {
    				codeBlockStart = jumpTarget(cpu.pc, (opcodeCaller) & 0x3FFFFFF);
    			}
				RuntimeContext.debugCodeBlockEnd(cpu, codeBlockStart, cpu._ra);
    		}
    	}

    	return insn;
    }

    public void interpretDelayslot() {
        int opcode = cpu.nextOpcode();
        Instruction insn = Decoder.instruction(opcode);
        if (log.isTraceEnabled()) {
        	log.trace(String.format("Interpreting 0x%08X: [0x%08X] - %s", cpu.pc - 4, opcode, insn.disasm(cpu.pc - 4, opcode)));
        }
        insn.interpret(this, opcode);
        cpu.nextPc();
    }

	public boolean isInterruptsEnabled() {
		return interruptsEnabled;
	}

	public boolean isInterruptsDisabled() {
		return !isInterruptsEnabled();
	}

	public void setInterruptsEnabled(boolean interruptsEnabled) {
		if (this.interruptsEnabled != interruptsEnabled) {
			this.interruptsEnabled = interruptsEnabled;

			if (interruptsEnabled) {
				// Interrupts have been enabled
				IntrManager.getInstance().onInterruptsEnabled();
			}
		}
	}

	public void enableInterrupts() {
		setInterruptsEnabled(true);
	}

	public void disableInterrupts() {
		setInterruptsEnabled(false);
	}

	public void step() {
        interpret();
    }

	public static boolean isInstructionInDelaySlot(Memory memory, int address) {
		int previousInstruction = memory.read32(address - 4);
		switch ((previousInstruction >> 26) & 0x3F) {
			case AllegrexOpcodes.J:
			case AllegrexOpcodes.JAL:
			case AllegrexOpcodes.BEQ:
			case AllegrexOpcodes.BNE:
			case AllegrexOpcodes.BLEZ:
			case AllegrexOpcodes.BGTZ:
			case AllegrexOpcodes.BEQL:
			case AllegrexOpcodes.BNEL:
			case AllegrexOpcodes.BLEZL:
			case AllegrexOpcodes.BGTZL:
				return true;
			case AllegrexOpcodes.SPECIAL:
				switch (previousInstruction & 0x3F) {
					case AllegrexOpcodes.JR:
					case AllegrexOpcodes.JALR:
						return true;
				}
				break;
			case AllegrexOpcodes.REGIMM:
				switch ((previousInstruction >> 16) & 0x1F) {
					case AllegrexOpcodes.BLTZ:
					case AllegrexOpcodes.BGEZ:
					case AllegrexOpcodes.BLTZL:
					case AllegrexOpcodes.BGEZL:
					case AllegrexOpcodes.BLTZAL:
					case AllegrexOpcodes.BGEZAL:
					case AllegrexOpcodes.BLTZALL:
					case AllegrexOpcodes.BGEZALL:
						return true;
				}
				break;
			case AllegrexOpcodes.COP1:
				switch ((previousInstruction >> 21) & 0x1F) {
					case AllegrexOpcodes.COP1BC:
						switch ((previousInstruction >> 16) & 0x1F) {
							case AllegrexOpcodes.BC1F:
							case AllegrexOpcodes.BC1T:
							case AllegrexOpcodes.BC1FL:
							case AllegrexOpcodes.BC1TL:
								return true;
						}
						break;
				}
				break;
		}

		return false;
	}
}