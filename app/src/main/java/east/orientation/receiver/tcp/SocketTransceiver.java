package east.orientation.receiver.tcp;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Socket收发器 通过Socket发送数据，并使用新线程监听Socket接收到的数据
 * 
 * @author jzj1993
 * @since 2015-2-22
 */
public abstract class SocketTransceiver implements Runnable {
	private final static String TAG = "SocketTransceiver";
	protected Socket socket;
	protected InetAddress addr;
	protected DataInputStream in;
	protected DataOutputStream out;
	private boolean runFlag;

	/**
	 * 实例化
	 * 
	 * @param socket
	 *            已经建立连接的socket
	 */
	public SocketTransceiver(Socket socket) {
		this.socket = socket;
		this.addr = socket.getInetAddress();
	}

	/**
	 * 获取连接到的Socket地址
	 * 
	 * @return InetAddress对象
	 */
	public InetAddress getInetAddress() {
		return addr;
	}

	/**
	 * 开启Socket收发
	 * <p>
	 * 如果开启失败，会断开连接并回调{@code onDisconnect()}
	 */
	public void start() {
		runFlag = true;
		new Thread(this).start();
	}

	/**
	 * 断开连接(主动)
	 * <p>
	 * 连接断开后，会回调{@code onDisconnect()}
	 */
	public void stop() {
		runFlag = false;
		try {
			socket.shutdownInput();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送字符串
	 * 
	 * @param s
	 *            字符串
	 * @return 发送成功返回true
	 */
	public boolean send(String s) {
		if (out != null) {
			try {
				out.writeUTF(s);
				out.flush();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 监听Socket接收的数据(新线程中运行)
	 */
	@Override
	public void run() {
		try {
			in = new DataInputStream(this.socket.getInputStream());
			out = new DataOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			runFlag = false;
		}
		while (runFlag) {
			try {

//				byte[] length = readByte(in, 4);
//				if (length.length == 0){
//					continue;
//				}
//				int buffLength = bytesToInt(length);
//				byte[] buff = readByte(in, buffLength);
				//int cmd = in.readInt();
				int len = in.readInt();
				Log.e(TAG,"len "+len);
				byte[] buff;
				if (len>0){
					buff = new byte[len];
					in.readFully(buff,0,len);
				}else {
					buff = new byte[0];
				}

				this.onReceive(addr, buff);
			} catch (IOException e) {
				Log.e(TAG,"disconnect "+e);
				// 连接被断开(被动)
				runFlag = false;
			}
		}
		// 断开连接
		try {
			in.close();
			out.close();
			socket.close();
			in = null;
			out = null;
			socket = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.onDisconnect(addr);
	}

	/**
	 * 保证从流里读到指定长度数据
	 *
	 * @param is
	 * @param readSize
	 * @return
	 * @throws Exception
	 */
	private byte[] readByte(InputStream is, int readSize) throws IOException {
		byte[] buff = new byte[readSize];
		int len = 0;
		int eachLen = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (len < readSize) {
			eachLen = is.read(buff);
			if (eachLen != -1) {
				len += eachLen;
				baos.write(buff, 0, eachLen);
			} else {
				break;
			}
			if (len < readSize) {
				buff = new byte[readSize - len];
			}
		}
		byte[] b = baos.toByteArray();
		baos.close();
		return b;
	}

	/**
	 * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
	 *
	 * @param src byte数组
	 * @return int数值
	 */
	public static int bytesToInt(byte[] src) {
		int value;
		value = (int) ((src[0] & 0xFF)
				| ((src[1] & 0xFF) << 8)
				| ((src[2] & 0xFF) << 16)
				| ((src[3] & 0xFF) << 24));
		return value;
	}


	/**
	 * 接收到数据
	 * <p>
	 * 注意：此回调是在新线程中执行的
	 * 
	 * @param addr 连接到的Socket地址
	 *
	 * @param cmd 命令
	 *
	 * @param bytes 收到的字符串
	 *
	 */
	public abstract void onReceive(InetAddress addr, byte[] bytes);

	/**
	 * 连接断开
	 * <p>
	 * 注意：此回调是在新线程中执行的
	 * 
	 * @param addr
	 *            连接到的Socket地址
	 */
	public abstract void onDisconnect(InetAddress addr);
}
