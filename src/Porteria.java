
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * <h1>Porteria</h1> La aplicacion de la porteria gestiona las entradas y salidas
 * por consola del la interfaz de usuario de la porteria
 * <p>
 * <b>Note:</b> La aplicacion fuciona por consola
 *
 * @author JOS
 * @version 1.0
 */
public class Porteria implements Runnable{
	
	private Socket cliente;
	private BufferedReader lector;
	private PrintWriter escritor;
	private boolean terminado;
	/**
	 * Constructor para la clase Porteria
	 */
	public Porteria() {
		this.terminado = false;
	}
	/**
	 * Este metodo cierra la conexion entre el cliente y el servidor y se ejecuta si
	 * se digita el comando quit
	 */
	public void apagar() {
		terminado = true;
		try {
			if (lector != null) 
				lector.close();
			
			if (escritor != null)
				escritor.close();
			
			if (!cliente.isClosed()) 
				cliente.close();
		} catch(IOException e) {
			
		}
	}

	/**
	 * Este metodo muestra la interfaz de usuario y lee por consola los comandos de
	 * la aplicacion de la porteria
	 */
	@Override
	public void run() {
		try {
			System.out.println("Recuerda,si quiere solitar un acceso hagalo por este comando (Porteria:nombre del apartamento:nombre de la persona)"
						+ " a ingresar" );
			InetAddress ia = InetAddress.getLocalHost();
			cliente = new Socket(ia.getHostAddress(), 9999);
			escritor = new PrintWriter(cliente.getOutputStream(), true);
			lector = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			
			AdministradorEntradaDatos aed = new AdministradorEntradaDatos();
			Thread t = new Thread(aed);
			t.start();
			
			String mensaje;
			while ((mensaje = lector.readLine()) != null) {
				System.out.println(mensaje);
			}
		} catch(IOException e) {
			apagar();
		}
	}
	
	class AdministradorEntradaDatos implements Runnable {

		/**
		 * Este metodo lee las determinadas opciones por la linea de comandos y llama a
		 * los metodos correspondientes
		 */
		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				
				while (!terminado) {
					String mensaje = in.readLine();
					escritor.println(mensaje);
				}
			} catch(IOException e) {
				apagar();
			}
		}
	}
	
	/**
	 * Este metodo main crea un objeto porteria y ejecuta su metodo run
	 * 
	 * @param args Unused.
	 */
	public static void main(String[] args) {
		Porteria p = new Porteria();
		p.run();
	}

}
