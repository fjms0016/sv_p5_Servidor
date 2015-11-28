package ujaen.git.ppt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 Para almacenar la clave del usuario se debe crear en el directorio de trabajo
 un fichero user.key, donde user es el identificador del usuario concreto. Ese
 fichero tan solamente debe contener una línea con una cadena sin espacios que
 será la clave.
 
 Directorio actual: C:\Users\javi\Documents\GitHub\sv_p5_Servidor\Servidor
 */

public class Authentication {
	
	/**
	 * Identificador de usuario
	 */
	protected String mUser = "";

	/**
	 * Se inicializa el buzón con el nombre del usuario
	 * 
	 * @param user
	 */
	public Authentication(String user) {
		mUser = user;

	}
	
	/**
	 * Comprueba si existe el directorio
	 * 
	 * @param user
	 * @return
	 */
	protected boolean open(String user) {
		File file = new File(user+".key");
		if (file.exists()) {
			return true;
		} else
			return false;
	}
	
	/**
	 * Comprueba si la clave pasada en el parámetro es correcta
	 * 
	 * @param pass Clave a comprobar
	 * @return TRUE si las claves son correctas, FALSE en otro caso.
	 */
	public boolean checkKey(String pass) {
		File file = new File(mUser + ".key");
		if (file.exists()) {
			byte[] data = new byte[(int) file.length()];

			try {
				String key = null;
				FileInputStream fis = new FileInputStream(file);
				fis.read(data);
				fis.close();
				key = new String(data);
				return (key.compareTo(pass) == 0) ? true : false;

			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}

		return false;
	}

}
