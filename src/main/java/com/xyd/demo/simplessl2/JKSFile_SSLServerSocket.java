package com.xyd.demo.simplessl2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class JKSFile_SSLServerSocket {
	// 定义了监听端口号
	private final static int LISTEN_PORT = 54321;
	// 相关的 jks 文件及其密码定义
	private final static String CERT_STORE="d:\\server.keystore"; 
	private final static String CERT_STORE_PASSWORD="123456";

	public static void main(String args[]) throws IOException {
		SSLServerSocket serverSocket = null;
		SSLSocket clientSocket = null;
		System.setProperty("javax.net.debug", "ssl,handshake");
		
		// 使用默认方式获取套接字工厂实例
//		SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		try {
			// 载入 jks 文件
			FileInputStream f_certStore=new FileInputStream(CERT_STORE); 
			KeyStore ks=KeyStore.getInstance("jks"); 
			ks.load(f_certStore, CERT_STORE_PASSWORD.toCharArray()); 
			f_certStore.close(); 
			 
			// 创建并初始化证书库工厂
			String alg=KeyManagerFactory.getDefaultAlgorithm(); 
			KeyManagerFactory kmFact=KeyManagerFactory.getInstance(alg); 
			kmFact.init(ks, CERT_STORE_PASSWORD.toCharArray()); 
			 
			KeyManager[] kms=kmFact.getKeyManagers(); 
			 
			// 创建并初始化 SSLContext 实例
			SSLContext context=SSLContext.getInstance("SSL"); 
			context.init(kms, null, null); 
			SSLServerSocketFactory ssf=(SSLServerSocketFactory)context.getServerSocketFactory();
			
			serverSocket = (SSLServerSocket) ssf.createServerSocket(LISTEN_PORT);
			// 设置不需要验证客户端身份
			serverSocket.setNeedClientAuth(false);
			System.out.println("SSLServer is listening on " + LISTEN_PORT + " port");
			// 循环监听端口，如果有客户端连入就新开一个线程与之通信
			while (true) {
				// 接受新的客户端连接
				clientSocket = (SSLSocket) serverSocket.accept();
				ClientConnection clientConnection = new ClientConnection(clientSocket);
				// 启动一个新的线程
				Thread clientThread = new Thread(clientConnection);
				System.out.println("Client " + clientThread.getId() + " is connected");
				clientThread.run();
			}
		} catch (IOException ioExp) {
			ioExp.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			serverSocket.close();
		}
	}
}

class ClientConnection implements Runnable {
	private Socket clientSocket = null;

	public ClientConnection(SSLSocket sslsocket) {
		clientSocket = sslsocket;
	}

	public void run() {
		BufferedReader reader = null;
		// 将接收到的来自客户端的文字打印出来
		try {
			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					System.out.println("Communication end.");
					break;
				}
				System.out.println("Receive message: " + line);
			}
			reader.close();
			clientSocket.close();
		} catch (IOException ioExp) {
			ioExp.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
