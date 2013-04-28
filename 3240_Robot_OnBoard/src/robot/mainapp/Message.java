package robot.mainapp;
import java.io.IOException;
import java.io.InputStream;

import robot.sensors.RobotData;
import robot.sensors.Sensor;

public abstract class Message {

	/**
	 * Performs a compression of this Message's unique (non-inherited) fields into a byte array.
	 */
	protected abstract byte[] subSerialize();

	/**
	 * Converts the fields of this Message subclass into a human-readable format.
	 */
	public abstract String printFields();


	/**
	 * Converts this Message into a byte-array.
	 */
	public byte[] serialize() {
		byte[] sub = this.subSerialize();
		String b = "";
		b += new String(ByteArrayUtils.intToByteArray(type.ordinal()));
		b += new String(ByteArrayUtils.intToByteArray(pri.ordinal()));
		b += new String(sub);
		while (b.length() < sub.length + Integer.SIZE / 8) {
			b += 0x00;
		}
		return b.getBytes();
	}

	/** 
	 * An enumeration of the possible types of message. 
	 * See Comm. Protocol for details.
	 */
	public enum MessageType {
		NULLMSG, // A null (empty) message
		MOVE, // A move (forward/backward) command
		TURN, // A turn (left/right) command
		CLEARQ, // Clear queue command
		STARTQ, // Start queue command
		ABORTQ, // Abort queue command (stop and clear)
		PAUSEQ, // Pause queue command
		STEPQ, // Step queue - execute one instruction in paused queue, then
		// pause again
		GOTOS, // Conditional GOTO on sensor value
		READS, // Read sensor and report results
		ITER, // Iterate: go to this instruction, this many times
		READR // Read a robot data parameter
	};

	/**
	 * Indicates the priority of a message, as per the Communications Protocol Document.
	 */
	public enum Priority {
		IMMEDIATE, QUEUE, BACKGROUND
	}

	public MessageType type = MessageType.NULLMSG;
	public Priority pri;
	public int checksum = 0;
	
	/**
	 * NULLMSG: a message with no semantics or parameters. 
	 * A Packet that encapsulates a Message that is 0 bytes long, encapsulates a NULLMSG.
	 */	
	public static class NullMessage extends Message {

		protected byte[] subSerialize() {
			return new byte[0];
		}

		public NullMessage() {
			type = MessageType.NULLMSG;
		}

		public String toString() {
			return "NullMessage [type=" + type + ", pri=" + pri + ", checksum="
					+ checksum + "]";
		}

		public String printFields() {
			return "NULL";
		}

		public byte[] serialize() {
			return new byte[0];
		}
	}

	/**
	 * Represents a message of type MOVE.
	 */
	public static class MoveMessage extends Message {
		public double dist; // in meters

		public MoveMessage(double d) {
			dist = d;
			this.type = MessageType.MOVE;
		}

		public String toString() {
			return "MoveMessage [type=" + type + ", pri=" + pri + ", checksum="
					+ checksum + ", dist=" + dist + "]";
		}

		public String printFields() {
			return "[dist=" + dist + "]";
		}

		protected byte[] subSerialize() {

			return ByteArrayUtils.doubleToByteArray(dist);

		}
	}

	/**
	 * Represents a message of type TURN.
	 */
	public static class TurnMessage extends Message {
		public double angle; // in meters

		public TurnMessage(double d) {
			angle = d;
			type = MessageType.TURN;
		}

		public String toString() {
			return "TurnMessage [type=" + type + ", pri=" + pri + ", checksum="
					+ checksum + ", angle=" + angle + "]";
		}

		public String printFields() {
			return "[angle=" + angle + "]";
		}

		protected byte[] subSerialize() {

			return ByteArrayUtils.doubleToByteArray(angle);
		}
	}

	/**
	 * Represents a message of type CLEARQ, STARTQ, ABORTQ, PAUSEQ, or STEPQ.
	 * Comm. Protocol contains details.
	 */
	public static class QueueMessage extends Message {
		public QueueMessage(MessageType t) {
			type = t;
		}

		public String toString() {
			return "QueueMessage [type=" + type + ", pri=" + pri
					+ ", checksum=" + checksum + ", type=" + type + "]";
		}

		public String printFields() {
			return "[type=" + type + "]";
		}

		protected byte[] subSerialize() {
			return new byte[0];
		}

	}
	
	/** 
	 * Represents a message of type either GOTOS or ITER.
	 */
	public static abstract class JumpMessage extends Message {
		public int destination;
	}
	
	/**
	 * Represents a message of type GOTOS.
	 */
	public static class GotosMessage extends JumpMessage {
		public Sensor sensor;
		public double threshold;
		public double epsilon;

		

		/**
		 * Enumerates the possible comparison operations to perform.
		 * GT: 	sensor value is greater than threshold + epsilon
		 * LT: 	sensor value is less than threshold - epsilon
		 * EQ: 	sensor value is between threshold + epsilon and threshold - epsilon
		 * GTE: sensor value is greater than threshold - epsilon
		 * LTE: sensor value is less than threshold + epsilon
		 * NE: 	sensor value is either greater than threshold + epsilon or less than threshold - epsilon
		 */
		public enum Condition {
			GT, 
			LT, 
			EQ, 
			GTE, 
			LTE, 
			NE 
		}

		public Condition compareMode;

		public GotosMessage(byte[] b) {
			destination = ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(b, 0, 4));
			compareMode = Condition.values()[ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(b, 4, 4))];
			sensor = Sensor.values()[ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(b, 8, 4))];
			threshold = ByteArrayUtils.byteArrayToDouble(ByteArrayUtils.slice(b, 12, 8));
			epsilon = ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(b, 20, 8));
			type = MessageType.GOTOS;
		}

		public GotosMessage(int destination, Condition cmp, Sensor s,
				double threshold, double epsilon) {
			this.destination = destination;
			this.compareMode = cmp;
			this.sensor = s;
			this.threshold = threshold;
			this.epsilon = epsilon;
			this.type = MessageType.GOTOS;
		}

		public String toString() {
			return "GotosMessage [type=" + type + ", pri=" + pri
					+ ", checksum=" + checksum + ", sensor=" + sensor
					+ ", condition=" + compareMode + ", threshold=" + threshold
					+ ", epsilon=" + epsilon + "]";
		}

		public String printFields() {
			return "[sensor=" + sensor + ", threshold=" + threshold
					+ ", epsilon=" + epsilon + "]";
		}

		protected byte[] subSerialize() {

			String b = "";

			b += new String(ByteArrayUtils.intToByteArray(destination));
			b += new String(ByteArrayUtils.intToByteArray(compareMode.ordinal()));
			b += new String(ByteArrayUtils.intToByteArray(sensor.ordinal()));
			b += new String(ByteArrayUtils.doubleToByteArray(threshold));
			b += new String(ByteArrayUtils.doubleToByteArray(epsilon));

			return b.getBytes();
		}

	}

	/**
	 * Represents a message of type READS.
	 */
	public static class ReadsMessage extends Message {
		public Sensor s;
		double value;

		public ReadsMessage(byte[] b) {
			s = Sensor.values()[ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(b, 0, 4))];
			value = ByteArrayUtils.byteArrayToDouble(ByteArrayUtils.slice(b, 4, 8));
			type = MessageType.READS;
		}

		public ReadsMessage(Sensor sensor, double value) {
			s = sensor;
			this.value = value;
			type = MessageType.READS;
		}

		public String toString() {
			return "ReadsMessage [type=" + type + ", pri=" + pri
					+ ", checksum=" + checksum + ", s=" + s + ", value="
					+ value + "]";
		}

		public String printFields() {
			return "[s=" + s + ", value=" + value + "]";
		}

		protected byte[] subSerialize() {
			String b = "";
			b += new String(ByteArrayUtils.intToByteArray(s.ordinal()));
			b += new String(ByteArrayUtils.doubleToByteArray(value));

			return b.getBytes();
		}

	}

	/**
	 * Represents a message of type ITER.
	 */
	public static class IterMessage extends JumpMessage {
		public int count;

		public IterMessage(byte[] b) {
			destination = ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(b, 0, 4));
			count = ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(b, 4, 4));
			type = MessageType.ITER;
		}

		public IterMessage(int destination, int cnt) {
			this.destination = destination;
			this.count = cnt;
			type = MessageType.ITER;
		}

		public String toString() {
			return "IterMessage [type=" + type + ", pri=" + pri + ", checksum="
					+ checksum + ", destination=" + destination + ", count="
					+ count + "]";
		}

		public String printFields() {
			return "[destination=" + destination + ", count=" + count + "]";
		}

		protected byte[] subSerialize() {
			String b = "";
			b += new String(ByteArrayUtils.intToByteArray(destination));
			b += new String(ByteArrayUtils.intToByteArray(count));

			byte[] trailingZeros = new byte[2 * Integer.SIZE / 8 - b.length()];
			b += new String(trailingZeros);
			return b.getBytes();
		}
	}

	/**
	 * Represents a message of type READR.
	 */
	public static class ReadrMessage extends Message {
		public RobotData param;
		double value;

		public ReadrMessage(byte[] b) {
			param = RobotData.values()[ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(b, 0, 4))];
			value = ByteArrayUtils.byteArrayToDouble(ByteArrayUtils.slice(b, 4, 8));
			type = MessageType.READR;
		}

		public ReadrMessage(RobotData param, double value) {
			this.param = param;
			this.value = value;
			type = MessageType.READS;
		}

		public String toString() {
			return "ReadrMessage [type=" + type + ", pri=" + pri
					+ ", checksum=" + checksum + ", param=" + param
					+ ", value=" + value + "]";
		}

		public String printFields() {
			return "param=" + param + ", value=" + value + "]";
		}

		protected byte[] subSerialize() {

			String b = "";
			b += new String(ByteArrayUtils.intToByteArray(param.ordinal()));
			b += new String(ByteArrayUtils.doubleToByteArray(value));
			byte[] trailingZeros = new byte[Integer.SIZE / 8 + Double.SIZE / 8
					- b.length()];
			b += new String(trailingZeros);
			return b.getBytes();
		}

	}

	/**
	 * Sets the priority of this message.
	 * @param priority the priority to send this message at.
	 */
	public void setPriority(Message.Priority priority) {
		this.pri = priority;
	}

	/**
	 * Reads bytes from an input stream and converts them to a Message of the appropriate subclass.
	 * The first byte in the input stream should be the encoded type of the message.
	 * @param length the number of bytes to read from the input stream; should include all of the bytes in this Message. Zero if the message is a NullMessage.
	 * @param is the InputStream from which to (synchronously) read the bytes that describe this message.
	 * @return an instance of a Message subclass on success
	 * @throws IOException if a Message cannot be generated from the number of given 
	 */
	public static Message deserialize(int length, InputStream is)
			throws IOException {
		if (length == 0) {
			return new NullMessage();
		} else {
			byte[] bytes = new byte[length];
			if (!(is.read(bytes, 0, length) == length)) {
				throw new IOException("Could not read complete message");
			}

			// Check checksum
			int check = 0;
			for (int i = 0; i < bytes.length; i++) {
				check += bytes[i];
			}

			Message ret = new NullMessage();

			MessageType type = MessageType.values()[ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(bytes,
					0, 4))];
			Priority pri = Priority.values()[ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(bytes, 4, 4))];
			switch (type) { // Construct a Message based on the bytecode
			case MOVE:
				ret = new MoveMessage(ByteArrayUtils.byteArrayToDouble(ByteArrayUtils.slice(bytes, 8, 8)));
				break;
			case TURN:
				ret = new TurnMessage(ByteArrayUtils.byteArrayToDouble(ByteArrayUtils.slice(bytes, 8, 8)));
				break;
			case CLEARQ:
				ret = new QueueMessage(type);
				break;
			case STARTQ:
				ret = new QueueMessage(type);
				break;
			case ABORTQ:
				ret = new QueueMessage(type);
				break;
			case PAUSEQ:
				ret = new QueueMessage(type);
				break;
			case STEPQ:
				ret = new QueueMessage(type);
				break;
			case GOTOS:
				ret = new GotosMessage(ByteArrayUtils.slice(bytes, 8, bytes.length - 8));
				break;
			case READS:
				ret = new ReadsMessage(ByteArrayUtils.slice(bytes, 8, bytes.length - 8));
				break;
			case READR:
				ret = new ReadrMessage(ByteArrayUtils.slice(bytes, 8, bytes.length - 8));
				break;
			case ITER:
				ret = new IterMessage(ByteArrayUtils.slice(bytes, 8, bytes.length - 8));
				break;
			default:
				throw new IOException("Unrecognized type for message");
			}

			ret.checksum = check;
			ret.pri = pri;
			return ret;
		}
	}

	public String toString() {
		return "Message [type=" + type + ", pri=" + pri + ", checksum="
				+ checksum + "]";
	}

}
