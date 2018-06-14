package com.xyd.demo.simplessl2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class JKSFile_SSLSocket {
	// 定义要连接的服务器名和端口号
	private static final int DEFAULT_PORT = 54321;
	private static final String DEFAULT_HOST = "localhost";
	// 相关的 jks 文件及其密码定义
	private final static String TRUST_STORE="d:\\server.keystore"; 
	private final static String TRUST_STORE_PASSWORD="123456";
	 
	public static void main(String args[]) {
		SSLSocket socket = null;
		// 使用默认的方式获取工厂实例
		//SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			// 载入 jks 文件
			 FileInputStream f_trustStore=new FileInputStream(TRUST_STORE); 
			 KeyStore ks=KeyStore.getInstance("jks"); 
			 ks.load(f_trustStore, TRUST_STORE_PASSWORD.toCharArray()); 
			 f_trustStore.close(); 
			 
			 // 创建并初始化信任库工厂
			 String alg=TrustManagerFactory.getDefaultAlgorithm(); 
			 TrustManagerFactory tmFact=TrustManagerFactory.getInstance(alg); 
			 tmFact.init(ks); 
			 
			 TrustManager[] tms=tmFact.getTrustManagers(); 
			 
			 // 创建并初始化 SSLContext 实例
			 SSLContext context=SSLContext.getInstance("SSL"); 
			 context.init(null, tms, null); 
			 SSLSocketFactory sf=context.getSocketFactory();
			 
			// 连接服务端的端口，完成握手过程
			socket = (SSLSocket) sf.createSocket(DEFAULT_HOST, DEFAULT_PORT);
			socket.startHandshake();
			System.out.println("Connected to " + DEFAULT_HOST + ":" + DEFAULT_PORT + " !");
			// 从控制台输入要发送给服务端的文字
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			Writer writer = new OutputStreamWriter(socket.getOutputStream());
			// 可以反复向服务端发送消息
			boolean done = false;
			while (!done) {
				System.out.print("Send Message: ");
				String line = reader.readLine();
				if (line != null) {
					writer.write(line + "\n");
					writer.flush();
				} else {
					done = true;
				}
			}
			socket.close();
		} catch (Exception e) {
			System.out.println("Connection failed: " + e);
			try {
				socket.close();
			} catch (IOException ioe) {
			}
			socket = null;
		}
	}
}
