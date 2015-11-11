/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.sinpo.xnfc.nfc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import org.apache.http.util.EncodingUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Util {
	private final static char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private Util() {
	}

	public static String getTime(){
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("",
				Locale.SIMPLIFIED_CHINESE);
		dateFormat.applyPattern("yyyy");
		dateFormat.applyPattern("MM");
		dateFormat.applyPattern("dd");
		dateFormat.applyPattern("[yyyy-MM-dd HH:mm:ss]");
		String time = dateFormat.format(date);
		return time;
	}

	public static byte[] toBytes(int a) {
		return new byte[] { (byte) (0x000000ff & (a >>> 24)),
				(byte) (0x000000ff & (a >>> 16)),
				(byte) (0x000000ff & (a >>> 8)), (byte) (0x000000ff & (a)) };
	}

	public static boolean testBit(byte data, int bit) {
		final byte mask = (byte) ((1 << bit) & 0x000000FF);

		return (data & mask) == mask;
	}

	public static int toInt(byte[] b, int s, int n) {
		int ret = 0;

		final int e = s + n;
		for (int i = s; i < e; ++i) {
			ret <<= 8;
			ret |= b[i] & 0xFF;
		}
		return ret;
	}

	public static int toIntR(byte[] b, int s, int n) {
		int ret = 0;

		for (int i = s; (i >= 0 && n > 0); --i, --n) {
			ret <<= 8;
			ret |= b[i] & 0xFF;
		}
		return ret;
	}

	public static int toInt(byte... b) {
		int ret = 0;
		for (final byte a : b) {
			ret <<= 8;
			ret |= a & 0xFF;
		}
		return ret;
	}

	public static int toIntR(byte... b) {
		return toIntR(b, b.length - 1, b.length);
	}

	public static String toHexString(byte... d) {
		return (d == null || d.length == 0) ? "" : toHexString(d, 0, d.length);
	}

	public static String toHexString(byte[] d, int s, int n) {
		final char[] ret = new char[n * 2];
		final int e = s + n;

		int x = 0;
		for (int i = s; i < e; ++i) {
			final byte v = d[i];
			ret[x++] = HEX[0x0F & (v >> 4)];
			ret[x++] = HEX[0x0F & v];
		}
		return new String(ret);
	}

	public static String toHexStringR(byte[] d, int s, int n) {
		final char[] ret = new char[n * 2];

		int x = 0;
		for (int i = s + n - 1; i >= s; --i) {
			final byte v = d[i];
			ret[x++] = HEX[0x0F & (v >> 4)];
			ret[x++] = HEX[0x0F & v];
		}
		return new String(ret);
	}

	public static String ensureString(String str) {
		return str == null ? "" : str;
	}

	public static String toStringR(int n) {
		final StringBuilder ret = new StringBuilder(16).append('0');

		long N = 0xFFFFFFFFL & n;
		while (N != 0) {
			ret.append((int) (N % 100));
			N /= 100;
		}

		return ret.toString();
	}

	public static int parseInt(String txt, int radix, int def) {
		int ret;
		try {
			ret = Integer.valueOf(txt, radix);
		} catch (Exception e) {
			ret = def;
		}

		return ret;
	}

	public static int BCDtoInt(byte[] b, int s, int n) {
		int ret = 0;

		final int e = s + n;
		for (int i = s; i < e; ++i) {
			int h = (b[i] >> 4) & 0x0F;
			int l = b[i] & 0x0F;

			if (h > 9 || l > 9)
				return -1;

			ret = ret * 100 + h * 10 + l;
		}

		return ret;
	}

	public static int BCDtoInt(byte... b) {
		return BCDtoInt(b, 0, b.length);
	}

	//��SD�е��ļ�
	public static String readFileSdcardFile(String fileName) throws IOException {
		String res="";
		try{
			FileInputStream fin = new FileInputStream(fileName);

			int length = fin.available();

			byte [] buffer = new byte[length];
			fin.read(buffer);

			res = EncodingUtils.getString(buffer, "UTF-8");

			fin.close();
		}

		catch(Exception e){
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 检测网络是否可用
	 * @return
	 */
	public static boolean isNetworkConnected(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			Log.i("NetWorkState", "Unavailabel");
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						Log.i("NetWorkState", "Availabel");
						return true;
					}
				}
			}
		}
		return false;
	}

	//д��ݵ�SD�е��ļ�
	public static void writeFileSdcardFile(String fileName,String write_str) throws IOException{
		try{
			FileOutputStream fout = new FileOutputStream(fileName);
			byte [] bytes = write_str.getBytes();

			fout.write(bytes);
			fout.close();
		}

		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static boolean initDownPath(String path){
		if(Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
			File file = new File(path);
			if(!file.exists()){
				file.mkdirs();
				return true;
			}
		}
		return false;
	}

	public static void writeFileSdcardFile(String fileName,String write_str,boolean append) throws IOException{
		try{
			FileOutputStream fout = new FileOutputStream(fileName,append);
			byte [] bytes = write_str.getBytes();

			fout.write(bytes);
			fout.close();
		}

		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
	 *
	 * @return 应用程序是/否获取Root权限
	 */
	public static boolean upgradeRootPermission(String pkgCodePath) {
		Process process = null;
		DataOutputStream os = null;
		try {
			String cmd="chmod 777 " + pkgCodePath;
			process = Runtime.getRuntime().exec("su"); //切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		return true;
	}

	public static String execRootCmd(String cmd) {
		String result = "";
		DataOutputStream dos = null;
		DataInputStream dis = null;

		try {
			Process p = Runtime.getRuntime().exec("su");
			dos = new DataOutputStream(p.getOutputStream());
			dis = new DataInputStream(p.getInputStream());

			dos.writeBytes(cmd + "\n");
			dos.flush();
			dos.writeBytes("exit\n");
			dos.flush();
			String line = null;
			while ((line = dis.readLine()) != null) {
				result += line+"\r\n";
			}
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
