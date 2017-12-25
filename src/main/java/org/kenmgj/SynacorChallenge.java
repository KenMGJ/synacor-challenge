package org.kenmgj;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class SynacorChallenge implements Serializable {

	protected static final long serialVersionUID = -7476856753722639572L;
	protected static final Logger logger = LoggerFactory.getLogger(SynacorChallenge.class);

	protected static final int ARRAY_SIZE = 32768;
	protected static final int REGISTER_COUNT = 8;
	protected static final int MAX_REGISTER = 32775;
	protected static final String FILE_NAME = "/Users/kgiroux/Desktop/synacor-challenge/challenge.bin";

	protected transient BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	protected transient LinkedList<Character> charQueue = new LinkedList<Character>();
	protected transient Stack<String> history = new Stack<>();

	protected transient boolean seen = false;

	protected static final ImmutableMap<Integer, String> REGISTER_LOOKUP = ImmutableMap.<Integer, String>builder()
			.put(32768, "[r0]")
			.put(32769, "[r1]")
			.put(32770, "[r2]")
			.put(32771, "[r3]")
			.put(32772, "[r4]")
			.put(32773, "[r5]")
			.put(32774, "[r6]")
			.put(32775, "[r7]")
			.build();

	// ***************************************************
	// State Items
	// ***************************************************

	// Memory locations
	protected int[] memory = new int[ARRAY_SIZE];
	protected int[] registers = new int[REGISTER_COUNT];
	protected Stack<Integer> stack = new Stack<>();

	protected boolean halt = false;
	protected boolean debug = true;

	protected int ptr = 0;

	// **********************************************************

	public SynacorChallenge(String fileName) throws IOException {
		readBinaryFile(fileName);
	}

	public void run() throws IOException {
		while (!halt) {
			int code = memory[ptr];
			int a, b, c;

			if (ptr == 5451 && seen == false) {
				save();
				seen = true;
			}

			if (ptr == 6027 && seen == false) {
				save();
				seen = true;
			}

			switch (code) {
			case 0:
				halt = true;
				printLine("halt");
				break;
			case 1:
				a = memory[ptr + 1];
				b = getValue(memory[ptr + 2]);
				registers[a - ARRAY_SIZE] = b;
				printLine("set", a, b);
				ptr += 3;
				break;
			case 2:
				a = getValue(memory[ptr + 1]);
				stack.push(a);
				printLine("push", a);
				ptr += 2;
				break;
			case 3:
				a = memory[ptr + 1];
				setValue(a, stack.pop());
				printLine("pop", a);
				ptr += 2;
				break;
			case 4:
				a = memory[ptr + 1];
				b = getValue(memory[ptr + 2]);
				c = getValue(memory[ptr + 3]);
				setValue(a, b == c ? 1: 0);
				printLine("eq", a, b, c);
				ptr += 4;
				break;
			case 5:
				a = memory[ptr + 1];
				b = getValue(memory[ptr + 2]);
				c = getValue(memory[ptr + 3]);
				setValue(a, b > c ? 1: 0);
				printLine("gt", a, b, c);
				ptr += 4;
				break;
			case 6:
				a = getValue(memory[ptr + 1]);
				printLine("jmp", a);
				ptr = a;
				break;
			case 7:
				a = getValue(memory[ptr + 1]);
				b = getValue(memory[ptr + 2]);
				printLine("jt", a, b);
				ptr = a != 0 ? b : ptr + 3;
				break;
			case 8:
				a = getValue(memory[ptr + 1]);
				b = getValue(memory[ptr + 2]);
				printLine("jf", a, b);
				ptr = a == 0 ? b : ptr + 3;
				break;
			case 9:
				a = memory[ptr + 1];
				b = getValue(memory[ptr + 2]);
				c = getValue(memory[ptr + 3]);
				setValue(a, (b + c) % ARRAY_SIZE);
				printLine("add", a, b, c);
				ptr += 4;
				break;
			case 10:
				a = memory[ptr + 1];
				b = getValue(memory[ptr + 2]);
				c = getValue(memory[ptr + 3]);
				setValue(a, (b * c) % ARRAY_SIZE);
				printLine("mult", a, b, c);
				ptr += 4;
				break;
			case 11:
				a = memory[ptr + 1];
				b = getValue(memory[ptr + 2]);
				c = getValue(memory[ptr + 3]);
				setValue(a, b % c % ARRAY_SIZE);
				printLine("mod", a, b, c);
				ptr += 4;
				break;
			case 12:
				a = memory[ptr + 1];
				b = getValue(memory[ptr + 2]);
				c = getValue(memory[ptr + 3]);
				setValue(a, b & c);
				printLine("and", a, b, c);
				ptr += 4;
				break;
			case 13:
				a = memory[ptr + 1];
				b = getValue(memory[ptr + 2]);
				c = getValue(memory[ptr + 3]);
				setValue(a, b | c);
				printLine("or", a, b, c);
				ptr += 4;
				break;
			case 14:
				a = memory[ptr + 1];
				b = memory[ptr + 2];

				b = getValue(b);
				printLine("not", a, b);

				b = ~b;
				b = b << 17;
				b = b >>> 17;

				setValue(a, b);
				ptr += 3;
				break;
			case 15:
				a = memory[ptr + 1];
				b = getValue(memory[ptr + 2]);
				setValue(a, memory[b]);
				printLine("rmem", a, b);
				ptr += 3;
				break;
			case 16:
				a = getValue(memory[ptr + 1]);
				b = getValue(memory[ptr + 2]);
				memory[a] = b;
				printLine("wmem", a, b);
				ptr += 3;
				break;
			case 17:
				a = getValue(memory[ptr + 1]);
				stack.push(ptr + 2);
				printLine("call", a);
				ptr = a;
				break;
			case 18:
				printLine("ret");
				ptr = getValue(stack.pop());
				break;
			case 19:
				a = getValue(memory[ptr + 1]);
				System.out.print((char) a);
				printLine("out", a);
				ptr += 2;
				break;
			case 20:
				a = memory[ptr + 1];

				if (charQueue.isEmpty()) {
					String line = reader.readLine();
					history.push(line);

					// Print current pointer
					if (line.startsWith("ptr")) {
						String[] tokens = line.split(" ");
						if (tokens.length == 1) {
							System.out.println("Pointer: " + ptr);
						}
						else {
							ptr = Integer.parseInt(tokens[1]);
						}
						break;
					}
					else if ("dsm".equals(line)) {
						save();
						break;
					}
					else if ("save".equals(line)) {
						saveState();
						break;
					}
					else if ("restore".equals(line)) {
						restoreState();
						break;
					}
					else if ("history".equals(line)) {
						System.out.println(history.toString());
						break;
					}
					else if (line.startsWith("peek")) {
						int loc = Integer.parseInt(line.split(" ")[1]);
						if (loc < ARRAY_SIZE) {
							System.out.println("memory[" + loc + "] = " + memory[loc]);
						}
						else {
							System.out.println("register[" + (loc - ARRAY_SIZE) + "] = " + registers[loc - ARRAY_SIZE]);
						}
						break;
					}
					else if (line.startsWith("poke")) {
						String[] vals = line.split(" ");
						int loc = Integer.parseInt(vals[1]);
						int val = Integer.parseInt(vals[2]);
						setValue(loc, val);
						System.out.println("set memory[" + loc + "] = " + val);
						break;
					}
					else if (line.startsWith("debug")) {
						String flag = line.split(" ")[1];
						if ("on".equals(flag)) {
							debug = true;
						}
						else {
							debug = false;
						}
						break;
					}

					for (char cr : line.toCharArray()) {
						charQueue.add(cr);
					}
					charQueue.add('\n');
				}

				int cval = charQueue.remove(0);
				setValue(a, cval);
				printLine("in", a);
				ptr += 2;
				break;
			case 21:
				printLine("noop");
				ptr += 1;
				break;
			default:
				logger.error("********** ERROR UNSUPPORTED OPERATION: " + ptr);
				break;
			}
		}
	}

	protected void printLine(String op) {
		printLine(op, null, null, null);
	}

	protected void printLine(String op, Integer val1) {
		printLine(op, val1, null, null);
	}

	protected void printLine(String op, Integer val1, Integer val2) {
		printLine(op, val1, val2, null);
	}

	protected void printLine(String op, Integer val1, Integer val2, Integer val3) {
		if (debug) {

			String sval1;
			if (val1 == null) {
				sval1 = "";
			}
			else {
				sval1 = " " + val1 + getChar(val1);
			}

			String sval2;
			if (val2 == null) {
				sval2 = "";
			}
			else {
				sval2 = " "+ val2 + getChar(val2);
			}

			String sval3;
			if (val3 == null) {
				sval3 = "";
			}
			else {
				sval3 = " " + val3 + getChar(val3);
			}

			StringBuilder log = new StringBuilder()
					.append("[")
					.append(String.format("%05d", ptr))
					.append("] " + op + " " + sval1 + sval2 + sval3 + " [")
					;

			for (int i : registers) {
				log.append(" " + i);
			}
			log.append("]");
			logger.info(log.toString());
		}
	}

	protected String getChar(int val) {
		if (val >= 32 && val <= 126) {
			return " (" + (char) val + ")";
		}
		else if (val == '\n') {
			return " (NL)";
		}
		return "";
	}

	protected int getValue(int value) {
		if (value < ARRAY_SIZE) {
			return value;
		}
		else if (value <= MAX_REGISTER) {
			return registers[value - ARRAY_SIZE];
		}
		else {
			throw new IndexOutOfBoundsException();
		}
	}

	protected void setValue(int location, int value) {
		if (location < ARRAY_SIZE) {
			memory[location] = value;
		}
		else if (location <= MAX_REGISTER) {
			registers[location - ARRAY_SIZE] = value;
		}
		else {
			throw new IndexOutOfBoundsException();
		}
	}

	protected void readBinaryFile(String fileName) throws IOException {
		// Read binary file
		Path path = Paths.get(fileName);
		byte[] file = Files.readAllBytes(path);

		for (int i = 0; i < file.length; i += 2) {
			ByteBuffer bb = ByteBuffer.allocate(4);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.put(file[i]);
			bb.put(file[i + 1]);
			bb.putShort((short) 0);
			memory[i/2] = bb.getInt(0);
		}
	}

	protected void save() {
		int i = 0;

		logger.info("********** Instruction Pointer **********");
		logger.info("ptr = " + String.format("%05d", ptr));

		logger.info("*************** Registers ***************");
		while (i < 8) {
			logger.info("[" + (i + ARRAY_SIZE) + "] = r[" + i + "] = " + registers[i]);
			i++;
		}

		logger.info("***************** Stack *****************");
		for (i = 0; i < stack.size(); i++) {
			int s = stack.get(i);
			logger.info("stack[" + i + "] = " + s + getChar(s));
		}

		logger.info("***********       Memory        **********");
		i = 0;
		while (i < ARRAY_SIZE) {
			String loc = "[" + String.format("%05d", i) + "] ";

			int i1 = i + 1 < ARRAY_SIZE ? memory[i+1] : 0;
			String s1 = REGISTER_LOOKUP.containsKey(i1) ? REGISTER_LOOKUP.get(i1) : i1 + "";

			int i2 = i + 2 < ARRAY_SIZE ? memory[i+2] : 0;
			String s2 = REGISTER_LOOKUP.containsKey(i2) ? REGISTER_LOOKUP.get(i2) : i2 + "";

			int i3 = i + 3 < ARRAY_SIZE ?  memory[i+3] : 0;
			String s3 = REGISTER_LOOKUP.containsKey(i3) ? REGISTER_LOOKUP.get(i3) : i3 + "";

			switch (memory[i]) {
			case 0:
				logger.info(loc + "halt");
				i += 1;
				break;
			case 1:
				logger.info(loc + "set " + s1 + " " + s2);
				i += 3;
				break;
			case 2:
				logger.info(loc + "push " + s1);
				i += 2;
				break;
			case 3:
				logger.info(loc + "pop " + s1);
				i += 2;
				break;
			case 4:
				logger.info(loc + "eq " + s1 + " " + s2 + " " + s3);
				i += 4;
				break;
			case 5:
				logger.info(loc + "gt " + s1 + " " + s2 + " " + s3);
				i += 4;
				break;
			case 6:
				logger.info(loc + "jmp " + s1);
				i += 2;
				break;
			case 7:
				logger.info(loc + "jt " + s1 + " " + s2);
				i += 3;
				break;
			case 8:
				logger.info(loc + "jf " + s1 + " " + s2);
				i += 3;
				break;
			case 9:
				logger.info(loc + "add " + s1 + " " + s2 + " " + s3);
				i += 4;
				break;
			case 10:
				logger.info(loc + "mult " + s1 + " " + s2 + " " + s3);
				i += 4;
				break;
			case 11:
				logger.info(loc + "mod " + s1 + " " + s2 + " " + s3);
				i += 4;
				break;
			case 12:
				logger.info(loc + "and " + s1 + " " + s2 + " " + s3);
				i += 4;
				break;
			case 13:
				logger.info(loc + "or " + s1 + " " + s2 + " " + s3);
				i += 4;
				break;
			case 14:
				logger.info(loc + "not " + s1 + " " + s2);
				i += 3;
				break;
			case 15:
				logger.info(loc + "rmem " + s1 + " " + s2);
				i += 3;
				break;
			case 16:
				logger.info(loc + "wmem " + s1 + " " + s2);
				i += 3;
				break;
			case 17:
				logger.info(loc + "call " + s1);
				i += 2;
				break;
			case 18:
				logger.info(loc + "ret");
				i += 1;
				break;
			case 19:
				logger.info(loc + "out " + getChar(memory[i+1]));
				i += 2;
				break;
			case 20:
				logger.info(loc + "in " + s1);
				i += 2;
				break;
			case 21:
				logger.info(loc + "noop");
				i += 1;
				break;
			default:
				i += 1;
				break;
			}
		}
	}

	protected void saveState() {
		try {
			FileOutputStream fileOut = new FileOutputStream("/tmp/challenge.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			System.out.println("Serialized data is saved in /tmp/challenge.ser");
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	protected void restoreState() {
		try {
			FileInputStream fileIn = new FileInputStream("/tmp/challenge.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			SynacorChallenge s = (SynacorChallenge) in.readObject();
			this.memory = s.memory;
			this.registers = s.registers;
			this.stack = s.stack;
			this.halt = s.halt;
			this.debug = s.debug;
			this.ptr = s.ptr;

			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("SynacorChallenge class not found");
			c.printStackTrace();
			return;
		}
	}

	public static void main(String[] args) throws IOException {
		SynacorChallenge process = new SynacorChallenge(FILE_NAME);
		process.run();
	}

}
