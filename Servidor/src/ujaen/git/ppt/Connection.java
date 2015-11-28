package ujaen.git.ppt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;


public class Connection implements Runnable {

	
	//PDU formada por: Version+secuencia+tipo+comando+payload
	
		//Comandos utilizados en el protocolo
		public static final String CRLF="\r\n";
		public static final String OK="+OK";
		public static final String ERR="-ERR";
		public static final String QUIT="QUIT";
		//Tipos de mensajes
		public static final byte MSG_LOGIN=0x01; 
		public static final byte MSG_OPERACION=0x02;
		public static final byte MSG_FIN=0X04;
	
	Socket mSocket;
	private byte version;
	private int secuencia;
	private byte type;
	private String cmd;
	private String payload;
	public static String MSG_WELCOME = OK+" Bienvenido al servidor de pruebas"+CRLF;


	public Connection(Socket s) {
		mSocket = s;
	}

	@Override
	public void run() {
		String inputData = null;
		String outputData = "";
		String comando,user="",pass="";
		String payload;
		byte version=0, tipo=0,VERSION=1;
		int estado = 0,secuencia;
		Authentication auth = new Authentication("");
		if (mSocket != null) {
			try {

				DataOutputStream output = new DataOutputStream(mSocket.getOutputStream());
				//BufferedReader input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
				DataInputStream input = new DataInputStream(mSocket.getInputStream());
				int number=0;
				output.writeUTF(MSG_WELCOME);
				
				output.flush();

				while (mSocket.isConnected()) {
					try{
					version = input.readByte();
					if(version!=1){
						outputData = ERR+" [" + version + "] VERSION NO VALIDA"+CRLF;
						break;

					}
					} catch(NumberFormatException e){
						//Acciones asociadas a una version invalida
						outputData = ERR+" [" + version + "] FORMATO DE VERSION NO VALIDO"+CRLF;
						break;
					}
					secuencia=input.readInt();
					type=input.readByte();
					cmd=input.readUTF();
					payload=input.readUTF();
					switch (estado) {

					case 0:
					if (version==1 && type== MSG_LOGIN && cmd.equalsIgnoreCase(OK)) {
						//Comprobamos el usuario y su contraseña
						String[] credencial = payload.split("_");
						user=credencial[0];
						pass = credencial[1];
						auth = new Authentication(user);
						//Comprobamos que exista el usuario y que la contraseña sea correcta
						if (auth.open(user)==true && auth.checkKey(pass) == true) {	
							outputData = VERSION+ " " + secuencia+ " "+ MSG_LOGIN + OK +" "+ "Realiza la operacion"+CRLF;
							
							estado++;
						}
						
						else if (auth.open(user)==false){
							outputData = ERR+" [" + user + "] NO ES UN USUARIO REGISTRADO"+CRLF;
							
						}
						
						else if (auth.open(user)==true && auth.checkKey(pass) == false){
							outputData = ERR+" [" + user + "] CONTRASENA INCORRECTA"+CRLF;
						}
						
						
						
					//Hacer comprobacion version tipo y comando
					}
					
					else if (tipo!=MSG_LOGIN){
						outputData = ERR+" [" + inputData + "] TIPO DE MENSAJE INCORRECTO"+CRLF;
						
					}
					
					else if(cmd!=OK){
						outputData = ERR+" [" + cmd + "] COMANDO INCORRECTO"+CRLF;

					}
					break;
					
					
					case 1:

						if (cmd.length() == 3 && version==1 && type==MSG_OPERACION) {
							if (cmd.equalsIgnoreCase("SIN")) {

								try {
									String sin = String
											.valueOf(Math.sin(Double.parseDouble(payload)));
									outputData = "OK " + user + " el seno  es = " + sin + CRLF;

								} catch (NumberFormatException ex) {
									outputData = ERR+" FORMATO DE NUMERO INCORRECTO"+CRLF;
								}
							} else if(cmd.equalsIgnoreCase("COS")){
								try {
									String cos = String
											.valueOf(Math.cos(Double.parseDouble(payload)));
									outputData = CRLF+"OK " + user + " el coseno  es = " + cos + CRLF;

								} catch (NumberFormatException ex) {
									outputData = ERR+" FORMATO DE NUMERO INCORRECTO"+CRLF;
								}
							}
							
							else
								outputData = ERR+" COMANDO DESCONOCIDO"+CRLF;
								
						}
						
						
						else if (tipo!=MSG_OPERACION)
							outputData = ERR+" TIPO DE MENSAJE INCORRECTO"+CRLF;
						
						else
							outputData = ERR+" COMANDO DESCONOCIDO"+CRLF;
						break;
					
					
					}
					
					System.out.println("Servidor [Recibido]> " + version +" "+secuencia+" "+type+" "+cmd+" "+payload);
					
					

					output.writeUTF(outputData);
					output.flush();

				}

				/*while ((inputData = input.readLine()) != "" || (inputData = input.readLine()) != "QUIT" ) {
					System.out.println("Servidor [Recibido]> " + inputData);
					String[] campos = inputData.split(" ");
					
					if (campos.length==1){
						if(inputData.equalsIgnoreCase("")||inputData.equalsIgnoreCase(QUIT)){
							outputData="Has salido";
							break;
						}
						else outputData = ERR+" [" + inputData + "] NO ES UN COMANDO VALIDO"+CRLF;
					}

					else if (campos.length == 5) {
						try{
						version = Byte.parseByte(campos[0]);
						if(version!=1){
							outputData = ERR+" [" + version + "] VERSION NO VALIDA"+CRLF;
							break;

						}
						} catch(NumberFormatException e){
							//Acciones asociadas a una version invalida
							outputData = ERR+" [" + version + "] FORMATO DE VERSION NO VALIDO"+CRLF;
							break;
						}
						secuencia = Integer.parseInt(campos[1]);
						tipo = Byte.parseByte(campos[2]);
						comando = campos[3];
						payload = campos [4];
						ByteArrayInputStream bais = new ByteArrayInputStream(inputData.getBytes()); 
						DataInputStream dis = new DataInputStream(bais);
						this.version=dis.readByte();
						this.secuencia=dis.readInt();
						this.type=dis.readByte();
						this.cmd=dis.readUTF();
						this.payload=dis.readUTF();
						switch (estado) {

						case 0:
							if (version==1 && tipo== MSG_LOGIN && comando.equalsIgnoreCase(OK)) {
								//Comprobamos el usuario y su contraseña
								String[] credencial = payload.split("_");
								user=credencial[0];
								pass = credencial[1];
								auth = new Authentication(user);
								//Comprobamos que exista el usuario y que la contraseña sea correcta
								if (auth.open(user)==true && auth.checkKey(pass) == true) {	
									outputData = VERSION+ " " + secuencia+ " "+ MSG_LOGIN + OK +" "+ "Realiza la operacion"+CRLF;
									
									estado++;
								}
								
								else if (auth.open(user)==false){
									outputData = ERR+" [" + user + "] NO ES UN USUARIO REGISTRADO"+CRLF;
									
								}
								
								else if (auth.open(user)==true && auth.checkKey(pass) == false){
									outputData = ERR+" [" + user + "] CONTRASENA INCORRECTA"+CRLF;
								}
								
								
								
							//Hacer comprobacion version tipo y comando
							}
							
							else if (tipo!=MSG_LOGIN){
								outputData = ERR+" [" + inputData + "] TIPO DE MENSAJE INCORRECTO"+CRLF;
								
							}
							
							else if(comando!=OK){
								outputData = ERR+" [" + comando + "] COMANDO INCORRECTO"+CRLF;

							}
							break;
						

						case 1:

							if (comando.length() == 3 && version==1 && tipo==MSG_OPERACION) {
								if (comando.equalsIgnoreCase("SIN")) {

									try {
										String sin = String
												.valueOf(Math.sin(Double.parseDouble(payload)));
										outputData = "OK " + user + " el seno  es = " + sin + CRLF;

									} catch (NumberFormatException ex) {
										outputData = ERR+" FORMATO DE NUMERO INCORRECTO"+CRLF;
									}
								} else if(comando.equalsIgnoreCase("COS")){
									try {
										String cos = String
												.valueOf(Math.cos(Double.parseDouble(payload)));
										outputData = CRLF+"OK " + user + " el coseno  es = " + cos + CRLF;

									} catch (NumberFormatException ex) {
										outputData = ERR+" FORMATO DE NUMERO INCORRECTO"+CRLF;
									}
								}
								
								else
									outputData = ERR+" COMANDO DESCONOCIDO"+CRLF;
									
							}
							
							
							else if (tipo!=MSG_OPERACION)
								outputData = ERR+" TIPO DE MENSAJE INCORRECTO"+CRLF;
							
							else
								outputData = ERR+" COMANDO DESCONOCIDO"+CRLF;
							break;
						}
						
					} else
						outputData = ERR+" [" + inputData + "] NO ES UN COMANDO VALIDO"+CRLF;

					output.write(outputData.getBytes());

				}*/
				
				System.out.println(
						"Servidor [Finalizado]> " + mSocket.getInetAddress().toString() + ":" + mSocket.getPort());

				input.close();
				output.close();
				mSocket.close();
			} catch (SocketException se) {

				se.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}