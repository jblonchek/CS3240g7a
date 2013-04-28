package robot.mainapp;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;


public class Packet implements Serializable {

	public static final int HEADERSIZE = 28;
	
	private static final long serialVersionUID = -5821959415344041309L;

	public final static int EACKSEQNO = 0;
	public final static int ENACKSEQNO = -1;
	
	private static int lastSeqno = 0;

	public long timestamp;
	public int seqno;
	public int ackno;
	public int checksum;

	public enum PacketType {
		COMMANDTYPE, STATETYPE, DEBUGCMDTYPE, ERRORTYPE
	};

	PacketType type;

	public Message msg;
	
	/* Constructor for normal packets in the system. No updates to ackno. */
	public Packet(PacketType packetType, Message msg){
		timestamp = System.currentTimeMillis();
		seqno =  ++ lastSeqno;
		ackno = 0;
		type = packetType;
		this.msg = msg;
		checksum = 0;
	}
	
	/* Constructor used for creating EACK/ENACK packets */
	public Packet(PacketType packetType, Message msg, int seqno, int ackno){
		timestamp = System.currentTimeMillis();
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
		
		bytes += new String(ByteArrayUtils.longToByteArray(System.currentTimeMillis()));
		bytes += new String(ByteArrayUtils.intToByteArray(seqno));
		bytes += new String(ByteArrayUtils.intToByteArray(ackno));
		bytes += new String(ByteArrayUtils.intToByteArray(checksum));
		bytes += new String(ByteArrayUtils.intToByteArray(type.ordinal()));
		bytes += new String(ByteArrayUtils.intToByteArray(msgbytes.length));
		bytes += new String(msgbytes);

		byte[] res = bytes.getBytes();
		for (int i = 0; i < res.length; i++) {
			if (i < 16 || i > 19) {
				checksum += res[i];
			}
		}
		bytes = bytes.substring(0, 16) + new String(ByteArrayUtils.intToByteArray(checksum)) + bytes.substring(20);
		while(bytes.length() < HEADERSIZE + msgbytes.length){
			bytes+= 0x00;
		}
		return bytes.getBytes();
	}

	/* Reads a sequence of bytes from an InputStream (synchronously) and decodes them into a Packet. */
	public static Packet deserialize(InputStream is) {
		// Read the first 28 bytes
		byte[] header = new byte[HEADERSIZE];
		try {
			is.read(header, 0, HEADERSIZE);
			return new Packet(header, is);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*Decodes a byte array into a packet header, then (if the Packet has a nonzero messsage length) 
	 * synchronously reads more bytes from the given InputStream to construct an encapsulated Message.
	 */
	public Packet(byte[] header, InputStream is) throws IOException {
		try{
			
			timestamp = ByteArrayUtils.byteArrayToLong(ByteArrayUtils.slice(header, 0, 8));
			seqno = ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(header,8, 4));
			ackno = ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(header, 12, 4));
			checksum = ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(header, 16, 4));
			type = PacketType.values()[ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(header, 20, 4))];
			msg = Message.deserialize(ByteArrayUtils.byteArrayToInt(ByteArrayUtils.slice(header, 24, 4)), is);
		}catch(Exception e){
			throw new IOException("");
		}

		int nowcheck = msg.checksum;
		for (int i = 0; i < header.length; i++) {
			if (i < 16 || i > 19) {
				nowcheck += header[i];
			}
		}

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
	

	
}
