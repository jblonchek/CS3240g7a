
public class CommandQueue extends Thread {

	// TO DO:
	// ITER- done??
	// GOTOS- done??
	// ABORTQ- done
	// STEPQ-
	// PAUSEQ- done
	// STARTQ- done??
	// CLEARQ- done
	// ENQUEUE- done
	// reads
	// readr
	// move
	// turn

	private Message[] queue;
	private byte PC, queueEnd;
	private boolean paused;
	private CommThread comm;

	public CommandQueue(CommThread comm) {
		queue = new Message[100];
		queueEnd = 0;
		PC = 0;
		paused = true;
		this.comm = comm;
	}

	public void run() {
		while (!(paused || this.interrupted())) {
			stepQ();
		}
	}

	public void enqueue(Message m) {

		queue[queueEnd] = m;
		queueEnd++;

	}

	public void startQ() {
		paused = false;
		this.start();
	}

	public void stepQ() {

		Message curInst = queue[PC];
		if (curInst.type == Message.MessageType.MOVE) {
			comm.lejosBrick.move(0, ((Message.MoveMessage) curInst).dist, 0);
		} else if (curInst.type == Message.MessageType.TURN) {
			comm.lejosBrick.turn(((Message.TurnMessage) curInst).angle);
		} else if (curInst.type == Message.MessageType.GOTOS) {
			gotos();
		} else if (curInst.type == Message.MessageType.ITER) {
			iterQ();
		} else if (curInst.type == Message.MessageType.PAUSEQ) {
			pauseQ();
		} else if (curInst.type == Message.MessageType.READS) {
			comm.enqueuePacketToSend(comm
					.makeReadSensorPacket((Message.ReadsMessage) curInst));
		} else if (curInst.type == Message.MessageType.READR) {
			comm.enqueuePacketToSend(comm
					.makeReadRoboPacket((Message.ReadrMessage) curInst));
		}

		PC++;

	}

	public void pauseQ() {
		paused = true;
		try {
			this.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.interrupt();
	}

	public void abortQ() {
		pauseQ();
		clearQ();

	}

	public void clearQ() {
		queue = new Message[100];
		PC = 0;
		queueEnd = 0;
		paused = true;
	}

	public void iterQ() {
		Message.IterMessage m = (Message.IterMessage) queue[PC];
		int curIterInst = PC;
		for (int i = 0; i < m.count; i++) {
			PC = (byte) m.destination;
			while (PC < curIterInst) {
				stepQ();
			}
		}

	}

	public void gotos() {
		Message.GotosMessage m = (Message.GotosMessage) queue[PC];

		double sdata = getData(m.sensor);
		double cmpVal = 0;
		if (m.compareMode == Message.GotosMessage.Condition.LT) {

			cmpVal = (sdata < m.threshold + m.epsilon) ? 1 : 0;
			// t > d
		} else if (m.compareMode == Message.GotosMessage.Condition.LTE) {

			cmpVal = (sdata <= m.threshold + m.epsilon) ? 1 : 0;

			// t >= d
		} else if (m.compareMode == Message.GotosMessage.Condition.EQ) {

			cmpVal = (sdata < m.threshold + m.epsilon && sdata > m.threshold
					- m.epsilon) ? 1 : 0;

			// t = d
		} else if (m.compareMode == Message.GotosMessage.Condition.GTE) {

			cmpVal = (sdata >= m.threshold - m.epsilon) ? 1 : 0;

			// t <= d
		} else if (m.compareMode == Message.GotosMessage.Condition.GT) {

			cmpVal = (sdata > m.threshold - m.epsilon) ? 1 : 0;

			// t < d
		} else if (m.compareMode == Message.GotosMessage.Condition.NE) {

			cmpVal = (sdata > m.threshold + m.epsilon || sdata < m.threshold
					- m.epsilon) ? 1 : 0;

			// t != d
		}

		if (cmpVal == 1) {
			PC = (byte) m.destination;
		}

	}

	private double getData(Sensor s) {

		SensorData sdata = null;
		if (s == Sensor.SENSORCS) {
			sdata = new ColorSensorClass(); // need a dummy value in the
											// ColorSensorClass we use;									// explained in detail in comment in
											// CommThread2
		} else if (s == Sensor.SENSORLS) {
			sdata = new LightSensorClass();
		} else if (s == Sensor.SENSORSS) {
			sdata = new SoundSensorClass();
		} else if (s == Sensor.SENSORTS) {
			sdata = new TouchSensorClass();
		} else if (s == Sensor.SENSORUS) {
			sdata = new UltrasonicSensorClass();
		}

		return sdata.update();

	}
}
