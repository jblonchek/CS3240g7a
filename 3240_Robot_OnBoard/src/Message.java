import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

public abstract class Message {

	protected abstract byte[] subSerialize();
	public abstract String printFields();

	private static byte[] longToByteArray(long l){
		byte[] by = new byte[8];
		for (int i = by.length-1; i >= 0; i--)
		{	
			by[i] = (byte)(l & 0x00ff);
			l = l >>> 8;
		}

		return by;
	}
	
	private static byte[] doubleToByteArray(double d){
		byte[] by = new byte[8];
		long l = Double.doubleToLongBits(d);
		for (int i = by.length-1; i >= 0; i--)
		{	
			by[i] = (byte)(l & 0x00ff);
			l = l >>> 8;
		}

		return by;
	}
	
	private static byte[] intToByteArray(int l){
		byte[] by = new byte[4];
		for (int i = by.length-1; i >= 0; i--)
		{			
			by[i] = (byte)(l & 0x000000ff);
			l = l >>> 8;
		}
		return by;
	}
	
	private static long byteArrayToLong(byte[] b){
		long value = 0;
		
		for(int i = 0; i < 8; i++){
			value = (value << 8) | (0x00ff & (long)b[i]);
		}
		
		
		return value;
		//return (new BigInteger(b)).longValue();

	}
	
	private static double byteArrayToDouble(byte[] b){
		long value = 0;
		
		for(int i = 0; i < 8; i++){
			value = (value << 8) + (0x00ff & (long)b[i]);
		}
		
		
		return Double.longBitsToDouble(value);
	}
	
	private static int byteArrayToInt(byte[] b){
		int value = 0;
		
		for(int i = 0; i < 4; i++){
			value = (value << 8) + (0x00ff & (int)b[i]);
		}
		
		
		return value;
		//return (new BigInteger(b)).intValue();

	}
	
	private static byte[] slice(byte[] b, int offset, int length) {
		byte[] ret = new byte[length];
		for(int i = 0; i < length; i++){
			ret[i] = b[offset + i];
		}
		return ret;
		
	}
	
	
	public byte[] serialize() {
		byte[] sub = this.subSerialize();
		//ByteBuffer b = ByteBuffer.allocate(sub.length + 2 * Integer.SIZE/8);
		
		String b = "";
		b += new String(intToByteArray(type.ordinal()));
		b += new String(sub);
		while(b.length() < sub.length + Integer.SIZE/8){
			b += 0x00;
		}
		
		//b.putInt(type.ordinal());
		//b.putInt(pri.ordinal());
		//b.put(sub);
		return b.getBytes();
	}

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

	public enum Priority {
		IMMEDIATE, QUEUE, BACKGROUND
	}

	public MessageType type = MessageType.NULLMSG;
	public Priority pri;
	public int checksum = 0;

	// / An empty message (0 length)
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
		
		public String printFields(){
			return "NULL";
		}

		public byte[] serialize() {
			return new byte[0];
		}
	}
	
	// / MOVE <dist>
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
		
		public String printFields(){
			return "[dist=" + dist + "]";
		}

		protected byte[] subSerialize() {

			return doubleToByteArray(dist);

		}
	}

	// / TURN <angle>
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
		
		public String printFields(){
			return "[angle=" + angle + "]";
		}
		
		protected byte[] subSerialize() {
			
			return doubleToByteArray(angle);
		}
	}

	// / CLEARQ, STARTQ, ABORTQ, PAUSEQ, STEPQ
	public static class QueueMessage extends Message {
		public QueueMessage(MessageType t) {
			type = t;
		}
		
		public String toString() {
			return "QueueMessage [type=" + type + ", pri=" + pri + ", checksum="
					+ checksum + ", type=" + type + "]";
		}

		public String printFields(){
			return "[type=" + type + "]";
		}
		
		protected byte[] subSerialize() {
			return new byte[0];
		}

	}

	public static abstract class JumpMessage extends Message {
		public int destination;
	}

	public static class GotosMessage extends JumpMessage {
		public Sensor sensor;
		public double threshold;
		public double epsilon;

		public enum Condition {
			GT, // Greater than
			LT, // Less than
			EQ, // Equal to
			GTE, // Greater than or equal to
			LTE, // Less than or equal to
			NE // Not equal
		}

		public Condition compareMode;

		public GotosMessage(byte[] b) {
			destination = byteArrayToInt(slice(b,0,4));//b.getInt();
			compareMode = Condition.values()[byteArrayToInt(slice(b,4,4))];//b.getInt()];
			sensor = Sensor.values()[byteArrayToInt(slice(b,8,4))];//b.getInt()];
			threshold = byteArrayToDouble(slice(b,12,8));//b.getDouble();
			epsilon = byteArrayToInt(slice(b,20,8));//b.getDouble();
			type = MessageType.GOTOS;
		}
		
		public GotosMessage(int destination, Condition cmp, Sensor s, double threshold, double epsilon){
			this.destination = destination;
			this.compareMode = cmp;
			this.sensor = s;
			this.threshold = threshold;
			this.epsilon = epsilon;
			this.type = MessageType.GOTOS;
		}
		
		public String toString() {
			return "GotosMessage [type=" + type + ", pri=" + pri + ", checksum="
					+ checksum + ", sensor=" + sensor + ", condition=" + compareMode + ", threshold=" + threshold + ", epsilon=" + epsilon + "]";
		}
		
		public String printFields(){
			return "[sensor=" + sensor + ", threshold=" + threshold + ", epsilon=" + epsilon + "]";
		}

		protected byte[] subSerialize() {
//			ByteBuffer b = ByteBuffer.allocate(Integer.SIZE/8 * 3 + Double.SIZE/8
//					* 2);
//			
//			b.putInt(destination);
//			b.putInt(compareMode.ordinal());
//			b.putInt(sensor.ordinal());
//			b.putDouble(threshold);
//			b.putDouble(epsilon);

			String b = "";
			
			b += new String(intToByteArray(destination));
			b += new String(intToByteArray(compareMode.ordinal()));
			b += new String(intToByteArray(sensor.ordinal()));
			b += new String(doubleToByteArray(threshold));
			b += new String(doubleToByteArray(epsilon));
			
			
			


			return b.getBytes();//b.array();
		}

	}

	public static class ReadsMessage extends Message {
		public Sensor s;
		double value;

		public ReadsMessage(byte[] b) {
			s = Sensor.values()[byteArrayToInt(slice(b, 0, 4))];//b.getInt()];
			value = byteArrayToDouble(slice(b, 4, 8));//b.getDouble();
			type = MessageType.READS;
		}
		
		public ReadsMessage(Sensor sensor, double value){
			s = sensor;
			this.value = value;
			type = MessageType.READS;
		}
		
		public String toString() {
			return "ReadsMessage [type=" + type + ", pri=" + pri + ", checksum="
					+ checksum + ", s=" + s + ", value=" + value + "]";
		}

		public String printFields(){
			return "[s=" + s + ", value=" + value + "]";
		}
		
		protected byte[] subSerialize() {
//			ByteBuffer b = ByteBuffer.allocate(Integer.SIZE/8 + Double.SIZE/8);
//			b.putInt(s.ordinal());
//			b.putDouble(value);
			String b = "";
			b += new String(intToByteArray(s.ordinal()));
			b += new String(doubleToByteArray(value));
			
			return b.getBytes();
		}

	}

	public static class IterMessage extends JumpMessage {
		public int count;

		public IterMessage(byte[] b) {
			destination = byteArrayToInt(slice(b, 0, 4));//b.getInt();
			count = byteArrayToInt(slice(b, 4, 4));//b.getInt();
			type = MessageType.ITER;
		}
		
		public IterMessage(int destination, int cnt){
			this.destination = destination;
			this.count = cnt;
			type = MessageType.ITER;
		}
		
		public String toString() {
			return "IterMessage [type=" + type + ", pri=" + pri + ", checksum="
					+ checksum + ", destination=" + destination + ", count=" + count + "]";
		}

		public String printFields(){
			return "[destination=" + destination + ", count=" + count + "]";
		}
		
		protected byte[] subSerialize() {
			//ByteBuffer b = ByteBuffer.allocate(Integer.SIZE/8 * 2);
			String b = "";
			b += new String(intToByteArray(destination));
			b += new String(intToByteArray(count));
			
			//b.putInt(destination);
			//b.putInt(count);
			byte[] trailingZeros = new byte[2 * Integer.SIZE / 8 - b.length()];
			b += new String(trailingZeros);
			return b.getBytes();
		}
	}

	public static class ReadrMessage extends Message {
		public RoboData param;
		double value;

		public ReadrMessage(byte[] b) {
			param = RoboData.values()[byteArrayToInt(slice(b, 0, 4))];//b.getInt()];
			value = byteArrayToDouble(slice(b, 4, 8));//b.getDouble();
			type = MessageType.READR;
		}
		
		public ReadrMessage(RoboData param, double value){
			this.param = param;
			this.value = value;
			type = MessageType.READS;
		}

		public String toString() {
			return "ReadrMessage [type=" + type + ", pri=" + pri + ", checksum="
					+ checksum + ", param=" + param + ", value=" + value + "]";
		}
		
		public String printFields(){
			return "param=" + param + ", value=" + value + "]";
		}
		
		protected byte[] subSerialize() {
			//ByteBuffer b = ByteBuffer.allocate(Integer.SIZE/8 + Double.SIZE/8);
			
			String b = "";
			b += new String(intToByteArray(param.ordinal()));
			b += new String(doubleToByteArray(value));
			
			
			//b.putInt(param.ordinal());
			//b.putDouble(value);
			byte[] trailingZeros = new byte[Integer.SIZE/8 + Double.SIZE/8 - b.length()];
			b += new String(trailingZeros);
			return b.getBytes();
		}

	}
	
	public void setPriority(Message.Priority priority){
		this.pri = priority;
	}

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

			//ByteBuffer b = ByteBuffer.wrap(bytes);
			MessageType type = MessageType.values()[byteArrayToInt(slice(bytes, 0, 4))];//b.getInt()];
			Priority pri = Priority.values()[byteArrayToInt(slice(bytes, 4,4))];
			switch (type) { // Construct a Message based on the bytecode
			case MOVE:
				ret = new MoveMessage(byteArrayToDouble(slice(bytes, 8, 8)));//b.getDouble());
				break;
			case TURN:
				ret = new TurnMessage(byteArrayToDouble(slice(bytes, 8, 8)));//b.getDouble());
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
				ret = new GotosMessage(slice(bytes, 8, bytes.length - 8));
				break;
			case READS:
				ret = new ReadsMessage(slice(bytes, 8, bytes.length - 8));
				break;
			case READR:
				ret = new ReadrMessage(slice(bytes, 8, bytes.length - 8));
				break;
			case ITER:
				ret = new IterMessage(slice(bytes, 8, bytes.length - 8));
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
