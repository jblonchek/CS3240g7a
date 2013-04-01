import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;


public class Packet implements Serializable {

	public static final int HEADERSIZE = 28;
	
	private static final long serialVersionUID = -5821959415344041309L;

	public final static int EACKSEQNO = 0;
	public final static int ENACKSEQNO = -1;
	
	private static int lastSeqno = 0;

	public Date timestamp;
	public int seqno;
	public int ackno;
	public int checksum;

	public enum PacketType {
		COMMANDTYPE, STATETYPE, DEBUGCMDTYPE, ERRORTYPE
	};

	PacketType type;

	public Message msg;
	
	public Packet(PacketType packetType, Message msg){
		timestamp = new Date();
		seqno =  ++ lastSeqno;
		ackno = 0;
		type = packetType;
		this.msg = msg;
		checksum = 0;
	}
	
	//used for creating EACK/ENACK packets
	public Packet(PacketType packetType, Message msg, int seqno, int ackno){
		timestamp = new Date();
		this.seqno = seqno;
		this.ackno = ackno;
		type = packetType;
		this.msg = msg;
		checksum = 0;
	}

	/*
	 * Serializes to a byte array of variable length. 0:7 timestamp 8:11
	 * sequence number 12:15 ack number 16:19 checksum 20:23 packet type 24:27
	 * message length 28:?? message
	 */
	public byte[] seralize() {
		byte[] msgbytes = msg.serialize();
		String bytes = "";
		

		checksum = 0;
		
		bytes += new String(longToByteArray(System.currentTimeMillis()));
		bytes += new String(intToByteArray(seqno));
		bytes += new String(intToByteArray(ackno));
		bytes += new String(intToByteArray(checksum));
		bytes += new String(intToByteArray(type.ordinal()));
		bytes += new String(intToByteArray(msgbytes.length));
		bytes += new String(msgbytes);

		byte[] res = bytes.getBytes();
		for (int i = 0; i < res.length; i++) {
			if (i < 16 || i > 19) {
				checksum += res[i];
			}
		}
		bytes = bytes.substring(0, 16) + new String(intToByteArray(checksum)) + bytes.substring(20);
		while(bytes.length() < HEADERSIZE + msgbytes.length){
			bytes+= 0x00;
		}
		return bytes.getBytes();
	}

	public static Packet deserialize(InputStream is) {
		// Read the first 28 bytes
		byte[] header = new byte[HEADERSIZE];
		try {
			is.read(header, 0, HEADERSIZE);
			return new Packet(header, is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public Packet(byte[] header, InputStream is) throws IOException {
		try{
			long ts = byteArrayToLong(slice(header, 0, 8));
			
			timestamp = new Date();
			timestamp.setSeconds((int) ((ts/1000)%60));
			timestamp.setMinutes((int) ((ts/1000)/60)%60);
			timestamp.setHours((int) ((((ts/1000)/60)/60)%24));
			seqno = byteArrayToInt(slice(header,8, 4));
			ackno = byteArrayToInt(slice(header, 12, 4));
			checksum = byteArrayToInt(slice(header, 16, 4));
			type = PacketType.values()[byteArrayToInt(slice(header, 20, 4))];
			msg = Message.deserialize(byteArrayToInt(slice(header, 24, 4)), is);
		}catch(Exception e){
			throw new IOException("");
		}
		// compute checksum
		int nowcheck = msg.checksum;
		for (int i = 0; i < header.length; i++) {
			if (i < 16 || i > 19) {
				nowcheck += header[i];
			}
		}
		// NB: checksum is the sum of all *bytes*, not all *ints*

		if (checksum != nowcheck) {
			throw new IOException(
					"Checksum of received message did not match received data");
		}

	}

	public String toString() {
		return "[timestamp=" + timestamp + ", seqno=" + seqno
				+ ", ackno=" + ackno + ", checksum=" + checksum + ", type="
				+ type + ", msg=" + msg + "]";
	}
	
	private byte[] longToByteArray(long l){
		byte[] by = new byte[8];
		long tmp = 0;
		for (int i = by.length-1; i >= 0; i--)
		{	
			by[i] = (byte)(l & 0xff);
			l = l >> 8;
		}

		return by;
	}
	
	private byte[] intToByteArray(int l){
		byte[] by = new byte[4];
		for (int i = by.length-1; i >= 0; i--)
		{			
			by[i] = (byte)(l & 0x000000ff);
			l = l >> 8;
		}
		return by;
	}
	
	private long byteArrayToLong(byte[] b){
		long value = 0;
		
		for(int i = 0; i < 8; i++){
			value = (value << 8) + b[i];
		}
		
		
		return value;
	}
	
	private int byteArrayToInt(byte[] b){
		int value = 0;
		
		for(int i = 0; i < 4; i++){
			value = (value << 8) + b[i];
		}
		
		
		return value;
	}
	
	private byte[] slice(byte[] b, int offset, int length) {
		byte[] ret = new byte[length];
		for(int i = 0; i < length; i++){
			ret[i] = b[offset + i];
		}
		return ret;
		
	}

	
}
